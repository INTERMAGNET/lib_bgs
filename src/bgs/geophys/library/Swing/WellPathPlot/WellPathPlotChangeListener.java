/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Swing.WellPathPlot;

/**
 * An interface to allow objects to register interest in changes to a
 * WellPathPlotDataModel
 * 
 * @author smf
 */
public interface WellPathPlotChangeListener 
{
    public void stateChanged (boolean new_data);
}
