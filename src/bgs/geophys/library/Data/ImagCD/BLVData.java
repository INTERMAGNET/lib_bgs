/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author jex
 */
public class BLVData {
   public static final int COMP_0 = 0;
   public static final int COMP_1 = 1;
   public static final int COMP_2 = 2;
   public static final int SCALAR_F = 3;
   public static final int DIFF_F = 4;

   public ArrayList<BLVDiscontinuity> discontinuities = new ArrayList<BLVDiscontinuity>();
   public int nSeperatePlots =1; // this will be one more than the number of discontinuites found

    private String format;
    private String sourceFile;
    private String componentOrder;
    private Integer meanHComponent;
    private Integer meanFComponent; //IBFV2.0 only
    private String observatoryID;
    private Integer year;
    private List<BLVObservedValue> observedValues = new ArrayList<BLVObservedValue>();
    private List<BLVAdoptedValue> adoptedValues = new ArrayList<BLVAdoptedValue>();
    private List<String> comments = new ArrayList<String>();
    private Double[] meanH;
    private Double[] meanD;
    private Double[] meanU;
    private Double[] meanX;
    private Double[] meanI;
    private Double[] meanV;
    private Double[] meanY;
    private Double[] meanZ;
    private Double[] meanF;
    private Double[] meanS;
    private Double[] meanDeltaF;
    
    // the following scale value is different for old and new formats
    // it is set when the format is discovered after the first line read
    private static Double scalingFromFile = null; // values have implicit ecimal point for old format;
    
    private static Double nanoTeslaRange = 20.0; // standard mapping display scales
    private static Double angleScaleFactor = 0.25; // ditto
    private static String nanoTeslaUnitLabel = "nT";
    private static String angleUnitLabel = "min";
    private static String deltaFUnitLabel = "nT";
    private static String deltaFNameLabel = "Delta F";





    
/**
 * Main method for getting the data from a BLV file
 * All data is collected in this method and
 * mean values for each component are calculated
 * @param fileName full pathname of the BLV file
 * @throws bgs.geophys.library.Data.ImagCD.BLVException 
 */
    public void readInData(String fileName) throws BLVException{

        File bLVFile = new File(fileName);
        String buf = new String();
        int line = 0;  //for error reporting

// set observatory and year from filename. These are replaced later
// by values in the file if they exist
        this.observatoryID = bLVFile.getName().substring(0,3);
        this.year = Integer.parseInt(bLVFile.getName().substring(3,5))+1900;
        if (bLVFile.exists()) {
            this.setSourceFile(fileName);
            BufferedReader in;
            try {
                in = new BufferedReader(new FileReader(fileName));
                // read in component
                buf = in.readLine();
                line++;
                StringTokenizer st = new StringTokenizer(buf); 
//                System.out.println("buf: "+buf+" st num: "+st.countTokens()+" filename: "+fileName);
                // find out which format (must be done before start using the tokens).. ...
                setFormat(st);
                setComponentOrder(st.nextToken());
                setMeanHComponent((Integer) Integer.parseInt(st.nextToken()));
//                System.out.println("Format is: "+format);
                if(format.equalsIgnoreCase("IBFV2.00")){
//                    System.out.println("New format...");
                    setMeanFComponent((Integer) Integer.parseInt(st.nextToken()));                    
                }
                if(buf.length()>13){
                    setObservatoryID(st.nextToken());
                }
                if(buf.length()>18){
                    setYear((Integer)Integer.parseInt(st.nextToken()));
                }
                 
                // read in observed value,including cr & lf
                buf = in.readLine();
                line++;
                  while (buf.charAt(0)!='*'){
                   BLVObservedValue ov = new BLVObservedValue();
                    ov.readBLV(buf.toCharArray(), this.getComponentOrder(),this.getFormat());
                    addObservedValue(ov);
   //                 ov.printBLV();
                    buf = in.readLine();
                    line++;
                }
                  buf = in.readLine();
                    line++;
                 // read in adopted values ...
                 while(buf != null && buf.charAt(0)!= '*'){
                    BLVAdoptedValue av = new BLVAdoptedValue();
                     av.readBLVAdoptedValue(buf, this.getComponentOrder(),this.getFormat());
                     if(format.equalsIgnoreCase("IBFV2.00"))
                            checkDiscontinuity(buf, av.getDay(), av);

                     addAdoptedValue(av);
//                   av.printBLV();
                    buf = in.readLine();
                    line++;
                }


                // read in comments
                  buf = in.readLine();
                  while(buf != null){
                      getComments().add(buf+"\n");
                      buf = in.readLine();
                    line++;
                }

                //  System.out.println(comments.size() + comments.toString());   
                in.close();
//                System.out.println("Finished reading in ...");
            } catch (Exception e) {
       //TODO: close in
       //         e.printStackTrace();
                throw new BLVException("Error reading from " + bLVFile.getAbsolutePath()+
                                       "\nError message: "+e.getMessage()+
                                       "\nCurrent line number "+line+
//                                       "\nCurrent line content: "+String.valueOf(buf,0,80));
                                       "\nCurrent Line content: "+ buf);
            }

        }

        setMeanValues();
//        for(int j=0;j<nSeperatePlots-1;j++){
//            System.out.println("Discontinuity at: "+discontinuities.get(j));
//
//        }
//        System.out.println("Finished reading..");
    }

    private void checkDiscontinuity(String buf, Integer day, BLVAdoptedValue av) {
      if(buf.charAt(52) == 'd' || buf.charAt(52) == 'D') {
          int last = adoptedValues.size()-1;
          discontinuities.add(new BLVDiscontinuity(day, av.difference(adoptedValues.get(last))));
          nSeperatePlots++;
      }
    }

    
    private void setMeanValues() {
         meanH = new Double[nSeperatePlots];
         meanD = new Double[nSeperatePlots];
         meanU = new Double[nSeperatePlots];
         meanX = new Double[nSeperatePlots];
         meanI = new Double[nSeperatePlots];
         meanV = new Double[nSeperatePlots];
         meanY = new Double[nSeperatePlots];
         meanZ  = new Double[nSeperatePlots];
         meanF  = new Double[nSeperatePlots];
         meanS  = new Double[nSeperatePlots];
         meanDeltaF  = new Double[nSeperatePlots];
        // Mean values are calculated from adopted values,
        // null values are ignored
        for(int plot = 0;plot<this.nSeperatePlots; plot++){
        for (int index = 0; index < 3; index++) {
            setComponentMean(plot, this.getComponentAt(index), calculateMean(index, plot));
        }
        if(format.equalsIgnoreCase("IBFV2.00")) setMeanS(plot,calculateMean(SCALAR_F, plot));
        setMeanDeltaF(plot, calculateMeanDeltaF(plot));
      }
    }


    private void setComponentMean(int plot, Character comp, Double mean) {

        switch (comp) {
            case 'H':
                setMeanH(plot, mean);
                break;
            case 'D':
                setMeanD(plot, mean);
                break;
            case 'U':
                setMeanU(plot, mean);
                break;
            case 'X':
                setMeanX(plot, mean);
                break;
            case 'I':
                setMeanI(plot, mean);
                break;
            case 'V':
                setMeanV(plot, mean);
                break;
            case 'Y':
                setMeanY(plot, mean);
                break;
            case 'Z':
                setMeanZ(plot, mean);
                break;
            case 'F':
                setMeanF(plot, mean);
                break;
            case 's':
                setMeanS(plot, mean);
                break;
            case 'f':
                this.setMeanDeltaF(plot, mean);
                break;
        }
    }

/**
 * 
 * @param comp capital letter denoting which component mean is required 
 * @return mean reading for comp. Null is returned if there are
 * no readings
 */
    public Double getComponentMean(int plot, Character comp) {

        switch (comp) {
            case 'H':
                return getMeanH(plot);
            case 'D':
                return getMeanD(plot);
            case 'U':
                return getMeanU(plot);
            case 'X':
                return getMeanX(plot);
            case 'I':
                return getMeanI(plot);
            case 'V':
                return getMeanV(plot);
            case 'Y':
                return getMeanY(plot);
            case 'Z':
                return getMeanZ(plot);
            case 'F':
                return getMeanF(plot);
            case 'f':
                return getMeanDeltaF(plot);
            case 's':
                return getMeanS(plot);
            default:
                return null;
        }
    }

    /**
     * 
     * @param index - integer for first, second, third or fourth component
     * to be taken from the component order
     * @return mean at this component, mean of delta F is returned for index=4
     */
    public Double getComponentMeanAtIndex(int plot, Integer index) {
        if (index == DIFF_F) {
            return getMeanDeltaF(plot);
        }

        return this.getComponentMean(plot, getComponentAt(index));
    }

    private Double calculateMean(Integer index, Integer plotNumber) {
        Double mean = new Double(0);
        int nvalues = 0;

        nvalues = 0;
        int start = getDiscontinuityStartIndex(plotNumber);
        int end = getDiscontinuityEndIndex(plotNumber);
//         System.out.println("calc mean start and end "+start +" "+end);
//        for (int i = 0; i < this.getAdoptedValues().size(); i++) {
          for (int i = start; i < end; i++) {

            if (this.getAdoptedValue(i).getComponentValue(this.getComponentAt(index)) != null) {
                mean += this.getAdoptedValue(i).getComponentValue(this.getComponentAt(index));
                nvalues++;
            }
        }

        if (nvalues != 0) {
            return mean / nvalues / getScalingFromFile();
        } else {
            return null;
        }
    }

    private Double calculateMeanDeltaF(int plotNumber) {
        Double mean = new Double(0);
        int nvalues = 0;

        int start = getDiscontinuityStartIndex(plotNumber);
        int end = getDiscontinuityEndIndex(plotNumber);
        nvalues = 0;
//        for (int i = 0; i < this.getAdoptedValues().size(); i++) {
        for (int i = start; i < end; i++) {
            if (this.getAdoptedValue(i).getDeltaF() != null) {
                mean += this.getAdoptedValue(i).getDeltaF();
                nvalues++;
            }
        }

        if (nvalues != 0) {
            return mean / nvalues / scalingFromFile;
        } else {
            return null;
        }
    }

/**
 * 
 * @param index which component in the list
 * @return scale (depends whether component is field or angle)
 */
    public Double getScaleAtComponent(int index) {
        switch (getComponentAt(index)) {
            case ('H'):
            case ('U'):
            case ('X'):
            case ('V'):
            case ('Y'):
            case ('Z'):
            case ('F'):
            case ('s'):
                return 1.0;
            case ('D'):
            case ('I'):
                return BLVData.getAngleScaleFactor();
            default:
            case ('f'):
                return 1.0;
        }
    }

    /**
     * Use for debugging purposes
     */
    public void printBLVData() {
        System.out.println("Component order is: " + getComponentOrder());
        System.out.println("Mean H Component is: " + getMeanHComponent());
        System.out.println("Observatory ID is: " + getObservatoryID());
        System.out.println("Year is " + getYear());

        System.out.println("Observed values: ");
        for (int i = 0; i < getObservedValues().size(); i++) {
            System.out.println(getObservedValue(i).getDay() + " " +
                    getObservedValue(i).getComponentValue(this.getComponentOrder().charAt(0)) + " " +
                    getObservedValue(i).getComponentValue(this.getComponentOrder().charAt(1)) + " " +
                    getObservedValue(i).getComponentValue(this.getComponentOrder().charAt(2)) + " ");
        }

        System.out.println("Adopted values: ");
        for (int i = 0; i < getAdoptedValues().size(); i++) {
            System.out.println(getAdoptedValues().get(i).getDay() + " " +
                    getAdoptedValue(i).getComponentValue(this.getComponentOrder().charAt(0)) + " " +
                    getAdoptedValue(i).getComponentValue(this.getComponentOrder().charAt(1)) + " " +
                    getAdoptedValue(i).getComponentValue(this.getComponentOrder().charAt(2)) + " " +
                    getAdoptedValue(i).getDeltaF() + " ");

        }
        for(int i=0;i<nSeperatePlots;i++){
        System.out.println("Mean 1: "+i+" " + this.getComponentMeanAtIndex(i,0));
        System.out.println("Mean 2: "+i+" "  + this.getComponentMeanAtIndex(i,1));
        System.out.println("Mean 3: "+i+" "  + this.getComponentMeanAtIndex(i,2));
        System.out.println("Mean Delta F: " +i+" " + this.getMeanDeltaF(i));
        }

    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    private String getComponentOrder() {
        return componentOrder;
    }

    public String getUnitLabelAtComponent(Integer index) {
        switch (getComponentAt(index)) {
            case ('H'):
            case ('U'):
            case ('X'):
            case ('V'):
            case ('Y'):
            case ('Z'):
            case ('F'):
            case ('s'):
                return BLVData.getNanoTeslaUnitLabel();
            case ('D'):
            case ('I'):
                return BLVData.getAngleUnitLabel();
            default:
            case ('f'):
                return BLVData.getNanoTeslaUnitLabel();
        }
    }

    public Character getComponentAt(Integer index) {
        if (index == DIFF_F) {
            return 'f';
        }
        if (index == SCALAR_F) {
            return 's';
        }
        return componentOrder.charAt(index);
    }

    public void setComponentOrder(String componentOrder) {
        this.componentOrder = componentOrder;
    }

    public Integer getMeanHComponent() {
        return meanHComponent;
    }

    public void setMeanHComponent(Integer meanHComponent) {
        this.meanHComponent = meanHComponent;
    }

    public String getObservatoryID() {
        return observatoryID;
    }

    public void setObservatoryID(String observatoryID) {
        this.observatoryID = observatoryID;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<BLVObservedValue> getObservedValues() {
        return observedValues;
    }

    public void setObservedValues(List<BLVObservedValue> observedValues) {
        this.observedValues = observedValues;
    }

    public BLVObservedValue getObservedValueAtDay(int day) {
      // look for observed value for that day
      for(int i=0;i<observedValues.size();i++){
       if(observedValues.get(i).getDay()==day) return observedValues.get(i);
      }
        return null;
    }

    public BLVObservedValue getObservedValueAtDay(int day, int observationNumber) {
      // look for observed value for that day, according to observationNumber
      for(int i=0, j=0;i<observedValues.size();i++){
       if(observedValues.get(i).getDay()==day) {
           if(j==observationNumber) return observedValues.get(i);
           j++;
       }
      }
        return null;
    }

    public int getNumberObservedValuesAtDay(int day) {
      // look for observed value for that day        
      int nvals = 0;
      for(int i=0;i<observedValues.size();i++){
       if(observedValues.get(i).getDay()==day) nvals++;
      }
        return nvals;
    }

    public BLVObservedValue getObservedValue(int i) {
       return observedValues.get(i);
    }

    public void addObservedValue(BLVObservedValue v) {
        this.observedValues.add(v);
    }

    public BLVAdoptedValue getAdoptedValue(int i) {
        return adoptedValues.get(i);
    }

    public void setAdoptedValue(int i, BLVAdoptedValue v) {
        this.adoptedValues.set(i, v);
    }

    public void addAdoptedValue(BLVAdoptedValue v) {
        this.adoptedValues.add(v);
    }

    public List<BLVAdoptedValue> getAdoptedValues() {
        return adoptedValues;
    }

    public void setAdoptedValues(List<BLVAdoptedValue> adoptedValues) {
        this.adoptedValues = adoptedValues;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public Double getMeanH(int plot) {
        return meanH[plot];
    }

    public void setMeanH(int plot, Double meanH) {
        this.meanH[plot] = meanH;
    }

    public Double getMeanD(int plot) {
        return meanD[plot];
    }

    public void setMeanD(int plot, Double meanD) {
        this.meanD[plot] = meanD;
    }

    public Double getMeanU(int plot) {
        return meanU[plot];
    }

    public void setMeanU(int plot, Double meanU) {
        this.meanU[plot] = meanU;
    }

    public Double getMeanX(int plot) {
        return meanX[plot];
    }

    public void setMeanX(int plot, Double meanX) {
        this.meanX[plot] = meanX;
    }

    public Double getMeanI(int plot) {
        return meanI[plot];
    }

    public void setMeanI(int plot, Double meanI) {
        this.meanI[plot] = meanI;
    }

    public Double getMeanV(int plot) {
        return meanV[plot];
    }

    public void setMeanV(int plot, Double meanV) {
        this.meanV[plot] = meanV;
    }

    public Double getMeanY(int plot) {
        return meanY[plot];
    }

    public void setMeanY(int plot, Double meanY) {
        this.meanY[plot] = meanY;
    }

    public Double getMeanZ(int plot) {
        return meanZ[plot];
    }

    public void setMeanZ(int plot, Double meanZ) {
        this.meanZ[plot] = meanZ;
    }

    public Double getMeanF(int plot) {
        return meanF[plot];
    }

    public Double getMeanS(int plot) {
        return meanS[plot];
    }

    public void setMeanF(int plot, Double meanF) {
        this.meanF[plot] = meanF;
    }

    public void setMeanS(int plot, Double meanS) {
        this.meanS[plot] = meanS;
    }

    public Double getMeanDeltaF(int plot) {
        return meanDeltaF[plot];
    }

    public void setMeanDeltaF(int plot, Double meanDeltaF) {
        this.meanDeltaF[plot] = meanDeltaF;
    }

    public String getFormat() {
        return format;
    }

    // this needs to be called before you have taken any tokens!
    // st should be the first line of the BLV file
    public void setFormat(StringTokenizer st) {
        if(st.countTokens()>4){
            format = "IBFV2.00";
            scalingFromFile = 1.0;
        }
        else{
            format = "IBFV1.11";
            scalingFromFile = 10.0;
        }
    }

    public Integer getMeanFComponent() {
        return meanFComponent;
    }

    public void setMeanFComponent(Integer meanFComponent) {
        this.meanFComponent = meanFComponent;
    }
/**
 *
 * @return label string for delta F field
 */
    public static String getDeltaFNameLabel() {
        return deltaFNameLabel;
    }

/**
 *
 * @return unit label for nano Tesla fields
 */
    public static String getNanoTeslaUnitLabel() {
        return nanoTeslaUnitLabel;
    }

/**
 *
 * @return unit label for Angle fields
 */
    public static String getAngleUnitLabel() {
        return angleUnitLabel;
    }

 /**
  *
  * @return unit label for Delta F field
  */
    public static String getDeltaFUnitLabel() {
        return deltaFUnitLabel;
    }

    /**
     *
     * @return scale factor from the data file - currently
     * 10.0 since all fields have an implicit decimal point
     */
    public static Double getScalingFromFile() {
        return scalingFromFile;
    }

    /**
     *
     * @return default range for displaying nano Tesla fields
     * currently 20 nT
     */
    public static Double getNanoTeslaRange() {
        return nanoTeslaRange;
    }


    /**
     *
     * @return factor to reduce scaling by for angle fields,
     * currently set to 0.25. Eg If nano Tesla display is 20 nT, then
     * angles are at 20.0*0.25 = 5.0 min
     */
    public static Double getAngleScaleFactor() {
        return angleScaleFactor;
    }

    /*
     * returns the start index of the nth discontinuity
     */
   public int getDiscontinuityStartIndex(int n) {
        if(n==0) return 0;
        return discontinuities.get(n-1).getIndex();
    }

    /*
     * returns the start index of the nth discontinuity
     */
   public int getDiscontinuityEndIndex(int n) {
        if(nSeperatePlots==1) return this.getAdoptedValues().size();
        if(nSeperatePlots-1==n) return this.getAdoptedValues().size();
        return discontinuities.get(n).getIndex()-1;
    }

}






