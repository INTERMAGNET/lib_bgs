/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Dialogs.AnnualMeanViewer;

import bgs.geophys.library.Data.ImagCD.YearMeanFile;
import javax.swing.JPanel;


/**
 * Abstract class containing methods common to all panels in the annual mean viewer
 * 
 * @author smf
 */
public abstract class AnnualMeanViewerPanel extends JPanel
{

    private String title = "";
    private String sub_title = "";
    
    public String getTitle () { return title; }
    public String getSubTitle () { return sub_title; }
    
    /** derived objects must implement the same update method */
    public abstract void update (String obsy_name, int year, 
                                 YearMeanFile year_mean_file,
                                 AnnualMeanOptions options);
    
    /** make the title for the plot - the title is put in the 'title' field
     * @param start_title the start of the title
     * @param obsy_name observatory name
     * @param year displayed date */
    protected void makeTitle (String start_title, AnnualMeanOptions options, String obsy_name, int year)
    {
        String calc_method;
        
        switch (options.getDisplayType())
        {
            case ALL_DAYS:
                calc_method = "mean calculated from all days";
                break;
            case DISTURBED_DAYS:
                calc_method = "mean calculated from disturbed days only";
                break;
            case QUIET_DAYS:
                calc_method = "mean calculated from quiet days only";
                break;
            default:
                calc_method = "mean calculated from all days, or from incomplete data";
                break;
        }
        title = start_title + ": " + obsy_name;
        sub_title = "As recorded in " + Integer.toString(year) + ", " + calc_method;
    }
}

