/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

/**
 * An interface that is used by ImagCDF when writing data to inform of progress
 * 
 * @author smf
 */
public interface IMCDFWriteProgressListener 
{

    public void percentComplete (int percent_complete);
    
}
