/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Swing.WellPathPlot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * All the data needed to plot a well path. This object provides the data model
 * (in Swing terminology) for the WellPathPlot component. Replotting of the
 * well path is triggered by making changes to the data in this model
 * 
 * @author smf
 */
public class WellPathPlotDataModel 
{
    
    private List<WellPathPlotChangeListener> change_listeners;
    private String plot_title;
    private String md_units;
    private String mag_angle_units;
    private String mag_field_units;
    private List<WellPathPlotDataPoint> data_points;
    private boolean stats_calculated;
    private WellPathPlotDataPoint min_data_vals, max_data_vals, range_data_vals;
 
    /** create an empty data model for the WellPathPlot object */
    public WellPathPlotDataModel ()
    {
        change_listeners = new ArrayList ();
        this.plot_title = "Well Path Plot";
        this.md_units = "Feet";
        this.mag_angle_units = "Degrees";
        this.mag_field_units = "nT";
        this.data_points = new ArrayList <> ();
        this.stats_calculated = false;
    }
    /** create a data model for the WellPathPlot object
     * 
     * @param plot_title a title for the plot
     * @param md_units the units for measured depth, e.g. "feet", "metres", ...
     * @param mag_angle_units the units for magnetic field angles, e.g. "degrees", "minutes", ...
     * @param mag_field_units the units for magnetic field strengths, e.g. "nT", ...
     * @param data_points an array of points to plot
     */
    public WellPathPlotDataModel (String plot_title, String md_units, String mag_angle_units, 
                                  String mag_field_units, List data_points)
    {
        change_listeners = new ArrayList ();
        this.plot_title = plot_title;
        this.md_units = md_units;
        this.mag_angle_units = mag_angle_units;
        this.mag_field_units = mag_field_units;
        this.data_points = data_points;
        this.stats_calculated = false;
    }
    
    public String getPlotTitle () { return plot_title; }
    public String getMeasuredDepthUnits () { return md_units; }
    public String getMagAngleUnits () { return mag_angle_units; }
    public String getMagFieldUnits () { return mag_field_units; }
    public Iterator<WellPathPlotDataPoint> getDataPointIterator () { return data_points.iterator(); }
    
    public void setPlotTitle (String plot_title, boolean update_view)
    {
        this.plot_title = plot_title;
        if (update_view) fireChangeListneners (false);
    }
    
    public void setMeasuredDepthUnits (String md_units, boolean update_view)
    {
        this.md_units = md_units;
        if (update_view) fireChangeListneners (false);
    }
    
    public void setMagAngleUnits (String mag_angle_units, boolean update_view)
    {
        this.mag_angle_units = mag_angle_units;
        if (update_view) fireChangeListneners (false);
    }
    
    public void setMagFieldUnits (String mag_field_units, boolean update_view)
    {
        this.mag_field_units = mag_field_units;
        if (update_view) fireChangeListneners (false);
    }
    
    public void appendDataPoint (WellPathPlotDataPoint data_point, boolean update_view)
    {
        this.data_points.add(data_point);
        stats_calculated = false;
        if (update_view) fireChangeListneners (true);
    }
    
    public WellPathPlotDataPoint getMinDataValues () { calcDataStats(); return min_data_vals; }
    public WellPathPlotDataPoint getMaxDataValues () { calcDataStats(); return max_data_vals; }
    public WellPathPlotDataPoint getRangeDataValues () { calcDataStats(); return range_data_vals; }
        
    private void calcDataStats ()
    {
        if (! stats_calculated)
        {
            // calculate max and min
            double min_md, min_d, min_i, min_f;
            double max_md, max_d, max_i, max_f;
            if (data_points.size() <= 0)
            {
                min_md = min_d = min_i = min_f = -1.0;
                max_md = max_d = max_i = max_f = 1.0;
            }
            else
            {
                min_md = min_d = min_i = min_f = Double.MAX_VALUE;
                max_md = max_d = max_i = max_f = - Double.MAX_VALUE;
                for (WellPathPlotDataPoint data_point : data_points)
                {
                    if (data_point.getMeasuredDepth() > max_md)
                        max_md = data_point.getMeasuredDepth();
                    if (data_point.getMeasuredDepth() < min_md)
                        min_md = data_point.getMeasuredDepth();
                    if (data_point.getMagD()> max_d)
                        max_d = data_point.getMagD();
                    if (data_point.getMagD() < min_d)
                        min_d = data_point.getMagD();
                    if (data_point.getMagI()> max_i)
                        max_i = data_point.getMagI();
                    if (data_point.getMagI() < min_i)
                        min_i = data_point.getMagI();
                    if (data_point.getMagF()> max_f)
                        max_f = data_point.getMagF();
                    if (data_point.getMagF() < min_f)
                        min_f = data_point.getMagF();
                }
            }
            
            // if the first MD measurement is within 10% of the top of the well, include zero 
            if (min_md <= (max_md - min_md) / 10.0)
                min_md = 0.0;
            
            // create data points that contains the stats
            min_data_vals = new WellPathPlotDataPoint(min_md, min_d, min_i, min_f);
            max_data_vals = new WellPathPlotDataPoint(max_md, max_d, max_i, max_f);
            range_data_vals = new WellPathPlotDataPoint(max_md - min_md, max_d - min_d, max_i - min_i, max_f - min_f);
            stats_calculated = true;
        }
    }
    
    // ***************************************************************************************
    // * Code to manage notifying listeners of changes to the model
    // ***************************************************************************************
    public void addChangeListener (WellPathPlotChangeListener l)
    {
        change_listeners.add (l);
    }
    
    public void removeChangeListener (WellPathPlotChangeListener l)
    {
        change_listeners.remove(l);
    }
    
    public void fireChangeListneners (boolean new_data)
    {
        for (WellPathPlotChangeListener l : change_listeners) 
            l.stateChanged (new_data);
    }
    
}
