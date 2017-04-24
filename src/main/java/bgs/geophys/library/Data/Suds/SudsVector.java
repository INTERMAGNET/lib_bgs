/*
 * SudsVector.java
 *
 * Created on 29 November 2003, 13:27
 */

package bgs.geophys.library.Data.Suds;


import java.io.*;

/**
 *
 * @author  Ies
 */
public class SudsVector extends Object
{
    private double fx;
    private double fy;
    
    /** Creates a new instance of SudsVector */
    public SudsVector()
    {
        
    }
    
    public SudsVector(double fx, double fy)
    {
        setFX(fx);
        setFY(fy);
    }
    
    public void readFromFile(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        setFX(SudsFunctions.readFloat(file, swapBytes));
        setFY(SudsFunctions.readFloat(file, swapBytes));
    }
    
    public double getFX()
    {
        return fx;
    }
    
    public double getFY()
    {
        return fy;
    }
    
    public void setFX(double fx)
    {
        this.fx = fx;
    }
    
    public void setFY(double fy)
    {
        this.fy = fy;
    }
}
