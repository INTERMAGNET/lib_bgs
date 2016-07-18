/*
 * SudsTensor.java
 *
 * Created on 29 November 2003, 13:28
 */

package bgs.geophys.library.Data.Suds;


import java.io.*;

/**
 *
 * @author  Ies
 */
public class SudsTensor extends Object
{
    private double xx;
    private double yy;
    private double xy;
    
    /** Creates a new instance of SudsTensor */
    public SudsTensor()
    {
        
    }
    
    
    public SudsTensor(double xx, double yy, double xy)
    {
        setXX(xx);
        setYY(yy);
        setXY(xy);
    }
    
    public void readFromFile(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        setXX(SudsFunctions.readFloat(file, swapBytes));
        setYY(SudsFunctions.readFloat(file, swapBytes));
        setXY(SudsFunctions.readFloat(file, swapBytes));
    }
    
    public double getXX()
    {
        return xx;
    }
    
    public double getYY()
    {
        return yy;
    }
    
    public double getXY()
    {
        return xy;
    }
    
    public void setXX(double xx)
    {
        this.xx = xx;
    }
    
    public void setYY(double yy)
    {
        this.yy = yy;
    }
    
    public void setXY(double xy)
    {
        this.xy = xy;
    }

}
