/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.AnnualMeanViewer;

import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.PlotOptionsDialog;
import bgs.geophys.ImagCD.ImagCDViewer.Dialogs.PlotOptionsDialog;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.library.Data.GeomagAbsoluteValue;
import bgs.geophys.library.Data.ImagCD.YearMean;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Vector;

/**
 * Class holding options for annual mean display - some options are
 * stored in the class, some come from the PlotOptionsDialog
 * 
 * @author smf
 */
public class AnnualMeanOptions 
{
    
    /** a class holding details on the options for a single trace */
    public class AnnualMeanTraceOptions
    {
        private int component_code;
        
        /** create a new set of trace options
         * @param component_code from GeomagAbsoluteValue */
        public AnnualMeanTraceOptions (int component_code)
        {
            this.component_code = component_code;
        }
        public int getComponentCode () { return component_code; }
        public String getTraceName (boolean short_form, boolean is_diff) 
        { 
            String name;
            
            name = GeomagAbsoluteValue.getComponentName(component_code, short_form);
            if (is_diff) name = "d" + name + "/dt";
            return name; 
        }
        public String getTraceUnits (boolean is_diff)
        {
            String code;
            
            switch (component_code)
            {
                case GeomagAbsoluteValue.COMPONENT_D:
                case GeomagAbsoluteValue.COMPONENT_I:
                    code = "min.";
                    break;
                case GeomagAbsoluteValue.COMPONENT_X:
                case GeomagAbsoluteValue.COMPONENT_Y:
                case GeomagAbsoluteValue.COMPONENT_Z:
                case GeomagAbsoluteValue.COMPONENT_H:
                case GeomagAbsoluteValue.COMPONENT_F:
                    code = "nT";
                    break;
                default:
                    code = "unknown";
                    break;
            }
            if (is_diff) code = code + "/year";
            return code;
        }
        public Color getTraceColor () { return PlotOptionsDialog.getColourFromConfiguration(getTraceName(true, false)); }
        public double getRange () { return PlotOptionsDialog.getScaleFromConfiguration(getTraceName(true, false)); }
        public DecimalFormat getFormatter ()
        {
            switch (component_code)
            {
                case GeomagAbsoluteValue.COMPONENT_D:
                case GeomagAbsoluteValue.COMPONENT_I:
                    return angle_format;
            }
            return field_format;
        }
    }
    
    private YearMean.YearMeanType display_type;
    private boolean show_jumps;
    private boolean mark_means;
    private Vector<AnnualMeanTraceOptions> trace_list;
    private DecimalFormat field_format;            
    private DecimalFormat angle_format;
    
    public AnnualMeanOptions ()
    {
        String string;
        
        field_format = new DecimalFormat ("#####0.0");
        angle_format = new DecimalFormat ("#####0.0");
        setDisplayType (GlobalObjects.configuration.getProperty (getDisplayTypeOptionName(), "ALL_DAYS_PLUS_INCOMPLETE"));
        show_jumps = Boolean.parseBoolean (GlobalObjects.configuration.getProperty(getShowJumpsOptionName(), "true"));
        mark_means = Boolean.parseBoolean (GlobalObjects.configuration.getProperty(getMarkMeansOptionName(), "true"));
        trace_list = new Vector<AnnualMeanTraceOptions> ();
        loadComponentsShown();
    }
    
    public static String getDisplayTypeOptionName () { return "AnnualMean.DisplayType"; }
    public static String getShowJumpsOptionName () { return "AnnualMean.ShowJumps"; }
    public static String getMarkMeansOptionName () { return "AnnualMean.MarkMeans"; }

    public void loadComponentsShown ()
    {
        trace_list.removeAllElements();
        if (PlotOptionsDialog.isShownFromConfiguration("D"))
            trace_list.add (new AnnualMeanTraceOptions(GeomagAbsoluteValue.COMPONENT_D));
        if (PlotOptionsDialog.isShownFromConfiguration("I"))
            trace_list.add (new AnnualMeanTraceOptions(GeomagAbsoluteValue.COMPONENT_I));
        if (PlotOptionsDialog.isShownFromConfiguration("H"))
            trace_list.add (new AnnualMeanTraceOptions(GeomagAbsoluteValue.COMPONENT_H));
        if (PlotOptionsDialog.isShownFromConfiguration("X"))
            trace_list.add (new AnnualMeanTraceOptions(GeomagAbsoluteValue.COMPONENT_X));
        if (PlotOptionsDialog.isShownFromConfiguration("Y"))
            trace_list.add (new AnnualMeanTraceOptions(GeomagAbsoluteValue.COMPONENT_Y));
        if (PlotOptionsDialog.isShownFromConfiguration("Z"))
            trace_list.add (new AnnualMeanTraceOptions(GeomagAbsoluteValue.COMPONENT_Z));
        if (PlotOptionsDialog.isShownFromConfiguration("F"))
            trace_list.add (new AnnualMeanTraceOptions(GeomagAbsoluteValue.COMPONENT_F));
    }
    
    public int getNComponentsShown () { return trace_list.size(); }
    public AnnualMeanTraceOptions getComponentOptions (int index) { return trace_list.get(index); }
    
    public YearMean.YearMeanType getDisplayType () { return display_type; }
    public boolean isShowJumps () { return show_jumps; }
    public boolean isMarkMeans () { return mark_means; }
    
    public void setDisplayType (YearMean.YearMeanType display_type)
    {
        this.display_type = display_type;
        GlobalObjects.configuration.setProperty(getDisplayTypeOptionName(), display_type.toString());
    }
    public void setDisplayType (String display_type)
    {
        this.display_type = YearMean.parseMeanType(display_type);
        GlobalObjects.configuration.setProperty(getDisplayTypeOptionName(), this.display_type.toString());
    }
    public void setShowJumps (boolean show_jumps)
    {
        this.show_jumps = show_jumps;
        GlobalObjects.configuration.setProperty(getShowJumpsOptionName(), Boolean.toString(show_jumps));
    }
    public void setMarkMeans (boolean mark_means)
    {
        this.mark_means = mark_means;
        GlobalObjects.configuration.setProperty(getMarkMeansOptionName(), Boolean.toString(mark_means));
    }
}
