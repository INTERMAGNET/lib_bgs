/*
 * BLVPanel.java
 *
 * Created on 26 November 2008, 13:44
 */

package bgs.geophys.library.Magnetogram.BLV;

import bgs.geophys.library.Data.ImagCD.BLVData;
import bgs.geophys.library.Swing.ExcelAdapter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Component;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

/**
 *
 * @author  jex
 */
public class BLVPanel extends javax.swing.JPanel {
    public JFreeChart finalChart;
    BLVData finalBLV;
    boolean auto;
    boolean scaleDefault;
    Double scale;
    Integer numberOfScrollSteps;
    boolean addFilename;
    String finalPlotTitle;
    public float font_size_multiplier;

    
    /**
     * 
     */
    public Component currentSelectedTab;
    public boolean isPlotSelected;
    public boolean isTableSelected;
    
    private boolean viewAxes = true;
    /** Creates new form BLVPanel
     * @param bLV 
     */
    

    
    
    public BLVPanel(final BLVData bLV, boolean addFileName,
                    String plotTitle,
                    String adoptedPanelTitle,
                    String observedPanelTitle,
                    String commentsPanelTitle,
                    float font_size_multiplier) {
        this.font_size_multiplier = font_size_multiplier;
        initComponents();       
        drawScaleOptions();
        if(bLV.nSeperatePlots==1)viewAxesCheckBox.setEnabled(false);
        else viewAxesCheckBox.setEnabled(true);
    
        finalBLV = bLV;
        auto = false;
        scaleDefault = true;
        scale = BLVData.getNanoTeslaRange();
        numberOfScrollSteps = 0;
        addFilename = addFileName;
        finalPlotTitle = plotTitle;
        
        
//        this.setTitle("Base Line Values Plotter");
        
//        System.out.println("Plotter.....");
//        bLV.printBLVData();
        
        this.tabbedPane.addTab("Plots" ,plot(bLV, addFilename, plotTitle));
        if(commentsPanelTitle!=null){
            this.tabbedPane.addTab("Comments" ,
                 new CommentsPanel(bLV, commentsPanelTitle));
        }else{
          this.tabbedPane.addTab("Comments" ,
                 new CommentsPanel(bLV));  
        }
        if(adoptedPanelTitle!=null){
            this.tabbedPane.addTab("Adopted Values",
                    new AdoptedValuesPanel(bLV,adoptedPanelTitle));
        }else{
           this.tabbedPane.addTab("Adopted Values",
                    new AdoptedValuesPanel(bLV)); 
        }
        if(observedPanelTitle!=null){
            this.tabbedPane.addTab("Observed Values" ,
               new ObservedValuesPanel(bLV,observedPanelTitle));
        }else{
            this.tabbedPane.addTab("Observed Values" ,
               new ObservedValuesPanel(bLV));
        }  
       
    }
    

    public void addTabbedPaneChangeListener (ChangeListener listener)
    {
        tabbedPane.addChangeListener(listener);
    }
    
    public void removeTabbedPaneChangeListener (ChangeListener listener)
    {
        tabbedPane.removeChangeListener(listener);
    }
    
    public void addToButtonPanel (JButton button)
    {
        ButtonPanel.add (button);
    }
    
    public boolean isPlotPanelSelected ()
    {
        if(tabbedPane.getSelectedIndex()==0){
          return true;
        }else{
        return false;
        }
    }
    
        public boolean isTablePanelSelected ()
    {
        if(tabbedPane.getSelectedIndex()==2 ||
           tabbedPane.getSelectedIndex()==3 ){
          return true;
        }else{
        return false;
        }
    }
        
       
        
        public void getExcelInfo () {
          if (tabbedPane.getSelectedIndex()==2){ 
              ((AdoptedValuesPanel)tabbedPane.getSelectedComponent()).getExcelAdapter().copySelection();
          }
          else if (tabbedPane.getSelectedIndex()==3){
          //    System.out.println("getting observed values..");
            ((ObservedValuesPanel)tabbedPane.getSelectedComponent()).getExcelAdapter().copySelection(); 
          }
        }        
    
        
        public ChartPanel plot(final BLVData bLV,boolean addFilename,
                               String plotTitle) {
        
// draws the graph
//            System.out.println("plotting...");
        finalChart = createCombinedChart(bLV,auto,scale,
                                         numberOfScrollSteps,scaleDefault,
                                         addFilename, plotTitle, font_size_multiplier,viewAxes);
        ChartPanel chartPanel = new ChartPanel(finalChart, true, false, false, true, true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
//        chartPanel.setToolTipText("Zoom in with right click and drag");
        chartPanel.setDisplayToolTips(true);
        
      //  this.setContentPane(chartPanel);
      //  this.tabbedPane.addTab("Plots " ,chartPanel);
        
        return chartPanel;
        
       
    }

    
        private static JFreeChart createCombinedChart(final BLVData bLV,
                                                      boolean auto,
                                                      Double scale,
                                                      Integer scrollSteps,
                                                      boolean defaultScale,
                                                      boolean addFilename,
                                                      String plotTitle,
                                                      float font_size_multiplier,
                                                      boolean viewAxes) {

//            System.out.println("createcombined chart...");
        // make a scalarF plot if the format is new
        Boolean scalarFPlot = bLV.getFormat().equalsIgnoreCase("IBFV2.00");

        XYDataset[] dataset1 = createDataset(bLV,0);
        XYDataset[] dataset2 = createDataset(bLV,1);
        XYDataset[] dataset3 = createDataset(bLV,2);
        XYDataset[] fDiffData = createDataset(bLV,BLVData.DIFF_F);
        XYDataset[] scalarFData = null;
        JFreeChart chartScalarF;
        XYPlot subplotScalarF = null;

        if(scalarFPlot) {
            scalarFData = createDataset(bLV,BLVData.SCALAR_F);
            chartScalarF = createScatterChart(scalarFData, bLV,BLVData.SCALAR_F,auto,scale,scrollSteps,defaultScale, font_size_multiplier,viewAxes);
            subplotScalarF = (XYPlot) chartScalarF.getPlot();  
            subplotScalarF.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        }
        
        // create subplot 1...

        JFreeChart chart1 = createScatterChart(dataset1, bLV,0,auto,scale,scrollSteps,defaultScale, font_size_multiplier,viewAxes);
        XYPlot subplot1 = (XYPlot) chart1.getPlot();  
        subplot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
//        subplot1.setToolTipGenerator();
//        subplot1.getRenderer().setToolTipGenerator(XYToolTipGenerator generator);
        

        // create subplot 2...
        
        JFreeChart chart2 = createScatterChart(dataset2, bLV,1,auto,scale,scrollSteps,defaultScale, font_size_multiplier,viewAxes);
        XYPlot subplot2 = (XYPlot) chart2.getPlot();
        subplot2.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        
        // create subplot 3...
        
        JFreeChart chart3 = createScatterChart(dataset3, bLV,2,auto,scale,scrollSteps,defaultScale, font_size_multiplier,viewAxes);
        XYPlot subplot3 = (XYPlot) chart3.getPlot();
        subplot3.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        // create subplot FDiff...
        
        JFreeChart chartFDiff = createScatterChart(fDiffData, bLV,BLVData.DIFF_F,auto,scale,scrollSteps,defaultScale, font_size_multiplier,viewAxes);
        XYPlot subplotFDiff = (XYPlot) chartFDiff.getPlot();
        subplotFDiff.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        

        
        // parent plot...
        NumberAxis na = new NumberAxis("Day of Year");
        na.setLabelFont(sizeFont (NumberAxis.DEFAULT_AXIS_LABEL_FONT, font_size_multiplier));
        na.setTickLabelFont(sizeFont (NumberAxis.DEFAULT_TICK_LABEL_FONT, font_size_multiplier));
        CombinedDomainXYPlot plot = new CombinedDomainXYPlot(na);
        plot.setGap(10.0);
        
        // add the subplots...
        plot.add(subplot1, 1);
        plot.add(subplot2, 1);
        plot.add(subplot3, 1);
        if(scalarFPlot)plot.add(subplotScalarF,1);
        plot.add(subplotFDiff, 1);
        
        plot.setOrientation(PlotOrientation.VERTICAL);                 
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRange(false);
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        domainAxis.setRange(0,366);
        domainAxis.setLabelFont(sizeFont (NumberAxis.DEFAULT_AXIS_LABEL_FONT, font_size_multiplier));
        domainAxis.setTickLabelFont(sizeFont (NumberAxis.DEFAULT_TICK_LABEL_FONT, font_size_multiplier));
        plot.setDomainAxis(domainAxis);
        

        
        // return a new chart containing the overlaid plot...
        JFreeChart chart = new JFreeChart(
         plotTitle,
         sizeFont (JFreeChart.DEFAULT_TITLE_FONT, font_size_multiplier), 
         plot, true //legend or not
         );
        
        // add the filename if required ...
       if(addFilename){
        TextTitle source = new TextTitle("Source: "+ bLV.getSourceFile());
        source.setPosition(RectangleEdge.BOTTOM);
        source.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        source.setFont(sizeFont (TextTitle.DEFAULT_FONT, font_size_multiplier));
        chart.addSubtitle(source);
       }
//        TextTitle observedLegend = new TextTitle("Observed Values");
//        observedLegend.setPaint(Color.RED);
//        observedLegend.setPosition(RectangleEdge.BOTTOM);
//        observedLegend.setHorizontalAlignment(HorizontalAlignment.LEFT);
//        TextTitle adoptedLegend = new TextTitle("Adopted Values");
//        adoptedLegend.setPaint(Color.BLUE);
//        adoptedLegend.setPosition(RectangleEdge.BOTTOM);        
//        adoptedLegend.setHorizontalAlignment(HorizontalAlignment.LEFT);
//        chart.addSubtitle(adoptedLegend);
//        chart.addSubtitle(observedLegend);
//        
       
        return chart;

    }

// 
///**
//* Creates dataset.
//*
// index - first three components are 0,1,2
// index - 3 - scalarF
// index 4 - FDiff
//* @return The dataset.
//*/
    private static XYDataset[] createDataset(BLVData bLV, int index) {

        System.out.println("creating dataset..");
        int nplots = bLV.nSeperatePlots;  //number of discontinuities
        XYSeries[] datasetArrayObserved = new XYSeries[nplots];
        XYSeries[] datasetArrayAdopted = new XYSeries[nplots];

        XYSeriesCollection[] dataset = new XYSeriesCollection[bLV.nSeperatePlots*2];
        Double observedValueReal;
        Double adoptedValueReal;
       
        // plot the days and the first value
        // not for FDiff
    for(int series=0,nextDiscontinuity=0;nextDiscontinuity<nplots;series+=2,nextDiscontinuity++){
        dataset[series] = new XYSeriesCollection();
        dataset[series+1] = new XYSeriesCollection();
        // put in adopted values....
        System.out.println("Adding adopted values: "+index+" "+series);
         // get the indexs of the start and finish of the discontinuity
         int start = bLV.getDiscontinuityStartIndex(nextDiscontinuity);
         int end = bLV.getDiscontinuityEndIndex(nextDiscontinuity);
         System.out.println("start and end "+start +" "+end);

         datasetArrayAdopted[nextDiscontinuity] = new XYSeries("Adopted Values");
//        for(int i=0;i<bLV.getAdoptedValues().size();i++){
        for(int i=start;i<end;i++){

     if(index != BLVData.DIFF_F){
      try{
//       adoptedValue = bLV.getAdoptedValue(i).getComponentValue(bLV.getComponentAt(index));
//       adoptedValueReal = adoptedValue/BLVData.getScalingFromFile();
       adoptedValueReal = bLV.getAdoptedValue(i).getComponentValue(bLV.getComponentAt(index));
       adoptedValueReal /= BLVData.getScalingFromFile();

       datasetArrayAdopted[nextDiscontinuity].add(bLV.getAdoptedValue(i).getDay(),
                adoptedValueReal);
       }
      catch(Exception e){
          //doesn't get added if null
      }
      }
     else /* FDiff */{

       try{
//      adoptedValue = bLV.getAdoptedValue(i).getDeltaF();
//      adoptedValueReal = adoptedValue/BLVData.getScalingFromFile();
      adoptedValueReal = bLV.getAdoptedValue(i).getDeltaFScaled(BLVData.getScalingFromFile());
//      adoptedValueReal /= BLVData.getScalingFromFile();

      datasetArrayAdopted[nextDiscontinuity].add(bLV.getAdoptedValue(i).getDay(),
                adoptedValueReal);
         }
      catch(Exception e){}
     }


        }
//     System.out.println("Adding to dataset..");
     dataset[series].addSeries(datasetArrayAdopted[nextDiscontinuity]);
//        System.out.println("finished adding..");
 

       if(index != BLVData.DIFF_F){
//           System.out.println("Adding observed values.."+index);
//         int start = bLV.getDiscontinuityStartIndex(n);
//         int end = bLV.getDiscontinuityEndIndex(n);
//         System.out.println("start and end "+start +" "+end);

         datasetArrayObserved[nextDiscontinuity] = new XYSeries("Observed Values");
        for(int i=start;i<=end;i++){

       try{
//          observedValue = bLV.getObservedValue(i).getComponentValue(bLV.getComponentAt(index));
//          observedValueReal = observedValue.doubleValue()/BLVData.getScalingFromFile();
           int nvals = bLV.getNumberObservedValuesAtDay(i);
           if(nvals >0 ) {
               System.out.println("nvals for day "+i+" "+nvals);
            if(bLV.getObservedValueAtDay(i) != null){
             for(int j=0;j<nvals;j++){
               observedValueReal = bLV.getObservedValueAtDay(i,j).getComponentValue(bLV.getComponentAt(index));
               observedValueReal /= BLVData.getScalingFromFile();

               datasetArrayObserved[nextDiscontinuity].add(bLV.getObservedValueAtDay(i).getDay(),
                  observedValueReal);
             }
           }
          }
       }
       catch(Exception e){ //just don't add it to the dataset if it is null
           }
        }
        
        dataset[series+1].addSeries(datasetArrayObserved[nextDiscontinuity]);
       }


    }
//        XYSeriesCollection dataset = new XYSeriesCollection();
//        dataset.addSeries(datasetObserved);
//        dataset.addSeries(dataset2);

//        System.out.println("dataset size is: "+dataset.length);

//      for(Integer plotNumber=0,series=0;plotNumber<bLV.nSeperatePlots;plotNumber++,series+=2){
//        try{
//        System.out.println("series is create dataset: "+series);
//        System.out.println(dataset[series].getXValue(0,0)+" "+dataset[series].getYValue(0,0));
//        
//        System.out.println(dataset[series+1].getXValue(0,0)+" "+dataset[series+1].getYValue(0,0));
//        }catch(Exception e){System.out.println("null value: "+e.getMessage());}
//
//         }
        return dataset;
        
    }
//
///**
//* Creates a chart.
//*
//* @param dataset a dataset.
//*
//* @return The chart.
//*/
private static JFreeChart createScatterChart(XYDataset[] dataset,
                             final BLVData bLV, int index,
                             boolean auto, Double scale,
                             Integer scrollSteps,
                             boolean scaleDefault,
                             float font_size_multiplier,
                             boolean viewAxes){
      


       // create the chart...
String rangeLabel;
Double thisScale= new Double(scale*bLV.getScaleAtComponent(index));
rangeLabel = new String(bLV.getComponentAt(index) + " ("+bLV.getUnitLabelAtComponent(index)+") ");
if(index ==BLVData.DIFF_F) rangeLabel = new String(BLVData.getDeltaFNameLabel()+" ("+
                                      BLVData.getDeltaFUnitLabel()+") ");


JFreeChart chart = ChartFactory.createScatterPlot(
"Base Line Values at "+ bLV.getObservatoryID().toUpperCase()+ "in "+bLV.getYear(), // chart title
"Day of the Year", // domain axis label
rangeLabel, // range axis label
dataset[0], // data
PlotOrientation.VERTICAL, // orientation
false, // include legend
true, // tooltips
false // urls
);

XYPlot plot = (XYPlot) chart.getPlot();

//add each subsequent dataset (each discontinuity) with it's own axis ..
  for(Integer plotNumber=0,series=0;plotNumber<bLV.nSeperatePlots;plotNumber++,series+=2){
//      System.out.println("series is in scatter chart: "+series);
//      System.out.println(dataset[series].getXValue(0,0)+" "+dataset[series].getYValue(0,0));
      try{
//      System.out.println(dataset[series+1].getXValue(0,0)+" "+dataset[series+1].getYValue(0,0));
      }catch(Exception e){System.out.println("null value: "+e.getMessage());}
      NumberAxis rangeAxisAdopted, rangeAxisObserved;
      rangeAxisObserved = new NumberAxis(rangeLabel+plotNumber.toString()+"O");
   if(plotNumber==0) {
       rangeAxisAdopted = (NumberAxis) plot.getRangeAxis(0);
       plot.setRangeAxis(1, rangeAxisObserved);
//       rangeAxisAdopted./*setLabel(null);*/setLabelAngle(Math.PI/2.0);
   }
   else {
     rangeAxisAdopted = new NumberAxis(rangeLabel+" Dis "+plotNumber.toString());
//     rangeAxisAdopted.setLabelAngle(Math.PI/2.0);
//     rangeAxisAdopted.setLabel(null);
     plot.setRangeAxis(series, rangeAxisAdopted);
     plot.setRangeAxis(series+1, rangeAxisObserved);
     }

     plot.setDataset(series,dataset[series]);
     plot.mapDatasetToRangeAxis(series, series);
     plot.setDataset(series+1,dataset[series+1]);
     plot.mapDatasetToRangeAxis(series+1, series+1);

     rangeAxisAdopted.setAutoRangeIncludesZero(false);
     rangeAxisObserved.setAutoRangeIncludesZero(false);
     //  rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
     if (! scaleDefault) rangeAxisAdopted.setTickLabelPaint(Color.RED);
    if (! auto){
      rangeAxisAdopted.setAutoRange(false);
      rangeAxisObserved.setAutoRange(false);
      try{
      rangeAxisAdopted.setRange(bLV.getComponentMeanAtIndex(plotNumber, index)-thisScale*(0.5+scrollSteps/5.0),
                        bLV.getComponentMeanAtIndex(plotNumber, index)+thisScale*(0.5-scrollSteps/5.0));
      rangeAxisObserved.setRange(bLV.getComponentMeanAtIndex(plotNumber, index)-thisScale*(0.5+scrollSteps/5.0),
                        bLV.getComponentMeanAtIndex(plotNumber, index)+thisScale*(0.5-scrollSteps/5.0));
     }catch(Exception e){
       rangeAxisObserved.setAutoRange(true);
       rangeAxisObserved.setAutoRangeIncludesZero(false);
       Range r = rangeAxisObserved.getRange();
       rangeAxisAdopted.setRange(r);
       if(index ==BLVData.DIFF_F){
         rangeAxisAdopted.setAutoRange(true);
         rangeAxisAdopted.setAutoRangeIncludesZero(false);       
      }
     }
    }
  else{
      rangeAxisObserved.setAutoRange(true);
      rangeAxisObserved.setAutoRangeIncludesZero(false);
      Range r = rangeAxisObserved.getRange();
      rangeAxisAdopted.setRange(r);
       if(index ==BLVData.DIFF_F){
         rangeAxisAdopted.setAutoRange(true);
         rangeAxisAdopted.setAutoRangeIncludesZero(false);       
      }
  }// end of scaling

float label_size_multiplier;
if(plotNumber==0)label_size_multiplier = (float) ((font_size_multiplier));
else label_size_multiplier = (float) ((font_size_multiplier)/1.3);
rangeAxisAdopted.setLabelFont(sizeFont (NumberAxis.DEFAULT_AXIS_LABEL_FONT, label_size_multiplier));
rangeAxisAdopted.setTickLabelFont(sizeFont (NumberAxis.DEFAULT_TICK_LABEL_FONT, font_size_multiplier));
rangeAxisObserved.setVisible(false);
if(plotNumber!=0 && !viewAxes) rangeAxisAdopted.setVisible(false);

 XYLineAndShapeRenderer rendererAdopted = new XYLineAndShapeRenderer();
 XYLineAndShapeRenderer rendererObserved = new XYLineAndShapeRenderer();
 plot.setRenderer(series, rendererAdopted);
 plot.setRenderer(series+1, rendererObserved);
// = (XYLineAndShapeRenderer) plot.getRenderer(series);

Double d = rendererAdopted.lookupSeriesShape(0).getBounds().getWidth()/1.3;
Shape dot = new Ellipse2D.Double(0,0,d,d);
Shape tinyDot = new Ellipse2D.Double(0,0,d/2,d/2);

   if(index==0&&series==0){
//       System.out.println("setting in legend: ");
       rendererAdopted.setSeriesVisibleInLegend(0,true);
       rendererObserved.setSeriesVisibleInLegend(0,true);
   }
   else{
       rendererAdopted.setSeriesVisibleInLegend(0, false);
       rendererObserved.setSeriesVisibleInLegend(0, false);
    }

  switch(index){
    case(BLVData.DIFF_F):
     rendererAdopted.setSeriesShapesVisible(0,false);
     rendererAdopted.setSeriesLinesVisible(0,true);
     rendererAdopted.setSeriesPaint(0,Color.BLUE);
     rendererAdopted.setSeriesVisibleInLegend(0,false);

    break;
    case (0):case(1):case(2):case(BLVData.SCALAR_F):default:
     rendererAdopted.setSeriesLinesVisible(0, false);
     rendererAdopted.setSeriesShapesVisible(0,true);
     rendererObserved.setSeriesLinesVisible(0, false);
     rendererObserved.setSeriesShapesVisible(0,true);
     rendererAdopted.setDrawOutlines(false);
//     if(theseAreObservedValues){
      rendererObserved.setSeriesShape(0,dot);
      rendererObserved.setSeriesPaint(0,Color.RED);
//     }else{
      rendererAdopted.setSeriesShape(0,tinyDot);
      rendererAdopted.setSeriesPaint(0,Color.BLUE);
//     }
    break;

  }

  XYToolTipGenerator ttg = new StandardXYToolTipGenerator();
  rendererAdopted.setSeriesToolTipGenerator(0,ttg);
  rendererObserved.setSeriesToolTipGenerator(0,ttg);
//   System.out.println("ttg: "+ttg);
   
// turn the range axis labels around

//chart.setBackgroundPaint(Color.white);



//for(int series=0;series<dataset.getSeriesCount();series++){




// there are two series for each plot in each discontinuity
// one for adopted and one for observed values
// The series alternate between observed and adopted
//boolean theseAreObservedValues = false;

//System.out.println("Number of series: "+bLV.nSeperatePlots);

//int series=0;
//for(int nplot=0;nplot<bLV.nSeperatePlots;nplot++){
//    System.out.println("renderer: nplot,series,index, value: "+nplot+" "
//                       +series+" "+index+" ");

//set just the first observed and adopted in the legend

//   if(nplot==0||(nplot % 2) == 0 ||index==BLVData.DIFF_F) theseAreObservedValues = false;
//   else theseAreObservedValues = true;
//   System.out.println("observed = "+theseAreObservedValues);


//}
//  series+=2;

//code pinched from medit
//plot.setDataset(i,dataForPlot[i]);
//                    NumberAxis axis = new NumberAxis(dataNames[i]);
//                    plot.setRangeAxis(i, axis);
//                    plot.mapDatasetToRangeAxis(i, i);
//end code pinched..
// change the auto tick unit selection to integer units only...
//NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(series);

// set the axes for adopted values only ..
//if(! theseAreObservedValues){
//    System.out.println("Axis for series: "+series);
//    if(index==BLVData.DIFF_F)System.out.println("diff axis for: "+series);
//  axisNumber++;
//    System.out.println("plot range axes count: "+plot.getRangeAxisCount());
//  System.out.println("Mapping "+series+" and "+(series+1)+"to axis "+axisNumber);
//  plot.setDataset(series,(XYDataset) ((XYSeriesCollection)dataset).getSeries(series));
//  plot.setDataset(series+1,(XYDataset) ((XYSeriesCollection)dataset).getSeries(series+1));
//  plot.mapDatasetToRangeAxis(series, axisNumber); //map adopted
//  plot.mapDatasetToRangeAxis(series+1, axisNumber);//map observed
//  plot.mapDatasetToRangeAxis(series, axisNumber); //map adopted
//  plot.mapDatasetToRangeAxis(series+1, axisNumber);//map observed
////plot.setRangeAxis(series, rangeAxis);
//}
}
// mark the discontinuities on
for(int j=0;j<bLV.discontinuities.size();j++){
    Marker discMark = new ValueMarker(bLV.discontinuities.get(j).getIndex());
    discMark.setPaint(Color.RED);
//    System.out.println("comp at index: " + bLV.getComponentAt(index));
    discMark.setLabel(bLV.discontinuities.get(j).getStepValueString(bLV.getComponentAt(index)));
//    discMark.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
    discMark.setLabelTextAnchor(TextAnchor.HALF_ASCENT_RIGHT);
    plot.addDomainMarker(discMark);
}

return chart;
}
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tabbedPane = new javax.swing.JTabbedPane();
        ButtonPanel = new javax.swing.JPanel();
        viewAxesCheckBox = new javax.swing.JCheckBox();
        zoomButton = new javax.swing.JButton();
        scalingLabel = new javax.swing.JLabel();
        scalingComboBox = new javax.swing.JComboBox();
        resetScrollButton = new javax.swing.JButton();
        scrollUpButton = new javax.swing.JButton();
        scrollDownButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        tabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });
        add(tabbedPane, java.awt.BorderLayout.CENTER);

        ButtonPanel.setLayout(new java.awt.GridBagLayout());

        viewAxesCheckBox.setSelected(true);
        viewAxesCheckBox.setText("View All Axes");
        viewAxesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewAxesCheckBoxActionPerformed(evt);
            }
        });
        ButtonPanel.add(viewAxesCheckBox, new java.awt.GridBagConstraints());

        zoomButton.setText("Reset Zoom");
        zoomButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        ButtonPanel.add(zoomButton, gridBagConstraints);

        scalingLabel.setText("Scaling");
        ButtonPanel.add(scalingLabel, new java.awt.GridBagConstraints());

        scalingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        scalingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scalingComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        ButtonPanel.add(scalingComboBox, gridBagConstraints);

        resetScrollButton.setText("Reset Scroll");
        resetScrollButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetScrollButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(resetScrollButton, new java.awt.GridBagConstraints());

        scrollUpButton.setText("Scroll Up");
        scrollUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scrollUpButtonActionPerformed(evt);
            }
        });
        ButtonPanel.add(scrollUpButton, new java.awt.GridBagConstraints());

        scrollDownButton.setText("Scroll Down");
        scrollDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scrollDownButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 20);
        ButtonPanel.add(scrollDownButton, gridBagConstraints);

        add(ButtonPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void zoomButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomButtonActionPerformed
       // reset the zoom
        update();

    }//GEN-LAST:event_zoomButtonActionPerformed

    private void update(){
        tabbedPane.removeTabAt(0);
        tabbedPane.insertTab("Plots",null,
                   plot(finalBLV,addFilename,finalPlotTitle),null,0);
        tabbedPane.setSelectedIndex(0);  
    }
    
    private void scalingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scalingComboBoxActionPerformed
       // change the scaling
        switch (scalingComboBox.getSelectedIndex()){
            case (0):  // default scaling
             this.auto = false;
             this.scale = BLVData.getNanoTeslaRange();
             this.scaleDefault = true;
             break;
            case (1):
             this.auto = false;
             this.scale = BLVData.getNanoTeslaRange()*2;
             this.scaleDefault = false;
             break;
            case (2):
             this.auto = false;
             this.scale = BLVData.getNanoTeslaRange()*4;
             this.scaleDefault = false;
             break;
            case (3):  // auto scaling
             this.auto = true;
             this.scaleDefault = false;
             break;
        }
       // disable scrolling on auto scale, enable otherwise
       this.scrollDownButton.setEnabled(!auto);
       this.scrollUpButton.setEnabled(!auto);
       // need to re-draw the plot
        update();
//        tabbedPane.removeTabAt(0);
//        tabbedPane.insertTab("Plots",null,
//                   plot(finalBLV,addFilename,finalPlotTitle),null,0);
//        tabbedPane.setSelectedIndex(0);
        
                                         // TODO add your handling code here:
    }//GEN-LAST:event_scalingComboBoxActionPerformed

    private void scrollUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollUpButtonActionPerformed
        numberOfScrollSteps++;
        update();
    }//GEN-LAST:event_scrollUpButtonActionPerformed

    private void scrollDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollDownButtonActionPerformed
        numberOfScrollSteps--;
        update();
    }//GEN-LAST:event_scrollDownButtonActionPerformed

    private void resetScrollButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetScrollButtonActionPerformed
        numberOfScrollSteps = 0;
        update();
    }//GEN-LAST:event_resetScrollButtonActionPerformed

    private void viewAxesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewAxesCheckBoxActionPerformed

//          System.out.println("check box changed...");
        if (getViewAxesCheckBox().isSelected()) setViewAxes(true);
        if (!viewAxesCheckBox.isSelected())setViewAxes(false);
        
        update();
    }//GEN-LAST:event_viewAxesCheckBoxActionPerformed


    
    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {                                        
     currentSelectedTab = tabbedPane.getSelectedComponent();
     
     if(tabbedPane.getSelectedIndex()!= 0){ //cannot print or save the number tables,only chart tab

       resetScrollButton.setEnabled(false);
       zoomButton.setEnabled(false);
       scrollDownButton.setEnabled(false);
       scrollUpButton.setEnabled(false);
       this.scalingComboBox.setEnabled(false);
       isPlotSelected = false;
       this.scalingLabel.setVisible(false);
       
     } else {

       resetScrollButton.setEnabled(true);
       zoomButton.setEnabled(true);
       scrollDownButton.setEnabled(true);
       scrollUpButton.setEnabled(true);
       this.scalingComboBox.setEnabled(true);
       this.scalingLabel.setVisible(true);
       isPlotSelected = true;
       
     }
     if(tabbedPane.getSelectedIndex()==2 ||
        tabbedPane.getSelectedIndex()==3 )
      isTableSelected = true;
      else isTableSelected = false;
    }

    public boolean isViewAxes() {
        return viewAxes;
    }

    public void setViewAxes(boolean viewAxes) {
        this.viewAxes = viewAxes;
    }


    public javax.swing.JCheckBox getViewAxesCheckBox() {
        return viewAxesCheckBox;
    }

    public void setViewAxesCheckBox(javax.swing.JCheckBox viewAxesCheckBox) {
        this.viewAxesCheckBox = viewAxesCheckBox;
    }
     
//    /* writes out the scale options on the combo box */
//    
    
      private class BLVComboBoxModel extends DefaultComboBoxModel
    {
        public BLVComboBoxModel (String[] columnNames)
        {
            super (columnNames);
        }
            
      }
      
    private void drawScaleOptions(){
    
    this.scalingComboBox.setModel(new BLVComboBoxModel(
              new String[]{BLVData.getNanoTeslaRange().toString()+" "+
                           BLVData.getNanoTeslaUnitLabel()+ " / "+ 
                           (BLVData.getNanoTeslaRange()*BLVData.getAngleScaleFactor()) +" "+
                           BLVData.getAngleUnitLabel(), //20 nT / 5 min
                           BLVData.getNanoTeslaRange()*2+" "+
                           BLVData.getNanoTeslaUnitLabel()+ " / "+ 
                           (BLVData.getNanoTeslaRange()*BLVData.getAngleScaleFactor()*2) +" "+
                           BLVData.getAngleUnitLabel(), // 40 nT / 10 min,
                           BLVData.getNanoTeslaRange()*4+" "+
                           BLVData.getNanoTeslaUnitLabel()+ " / "+ 
                           (BLVData.getNanoTeslaRange()*BLVData.getAngleScaleFactor()*4) +" "+
                           BLVData.getAngleUnitLabel(), // 80 nT / 20 min,
                            "Auto Scale"}));
    
    }
  
        /**
     * @param args the command line arguments
     */

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ButtonPanel;
    private javax.swing.JButton resetScrollButton;
    private javax.swing.JComboBox scalingComboBox;
    private javax.swing.JLabel scalingLabel;
    private javax.swing.JButton scrollDownButton;
    private javax.swing.JButton scrollUpButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JCheckBox viewAxesCheckBox;
    private javax.swing.JButton zoomButton;
    // End of variables declaration//GEN-END:variables
    
    private static Font sizeFont (Font font, float font_size_multiplier)
    {
        if (font_size_multiplier == 1.0f) return font;
        return font.deriveFont ((float) font.getSize() * font_size_multiplier);
    }
}
