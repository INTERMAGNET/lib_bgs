/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Swing.WellPathPlot;

/**
 * A class to hold a single data point for well path plotting
 * 
 * @author smf
 */
public class WellPathPlotDataPoint 
{

    // codes for the three geomagnetic elements to plot
    enum MagElements {MAG_D, MAG_I, MAG_F};
    
    private double measured_depth;
    private double mag_d;
    private double mag_i;
    private double mag_f;

    public WellPathPlotDataPoint (double measured_depth, double mag_d, double mag_i, double mag_f)
    {
        this.measured_depth = measured_depth;
        this.mag_d = mag_d;
        this.mag_i = mag_i;
        this.mag_f = mag_f;
    }
    
    public double getMeasuredDepth () { return measured_depth; }
    public double getMagD () { return mag_d; }
    public double getMagI () { return mag_i; }
    public double getMagF () { return mag_f; }
    public double getElement (MagElements element)
    {
        switch (element)
        {
            case MAG_D: return mag_d;
            case MAG_I: return mag_i;
            case MAG_F: return mag_f;
        }
        throw new IllegalArgumentException();
    }
    
}
