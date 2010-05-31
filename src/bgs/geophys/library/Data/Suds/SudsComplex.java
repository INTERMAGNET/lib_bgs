/*
 * SudsComplex.java
 *
 * Created on 29 November 2003, 13:28
 */

package bgs.geophys.library.Data.Suds;

import java.io.*;

/**
 *
 * @author  Ies
 */
public class SudsComplex extends Object
{
    private double cr;
    private double ci;
    
    /** Creates a new instance of SudsComplex */
    public SudsComplex() {
    }
    
    public SudsComplex(double cr, double ci)
    {
        setCR(cr);
        setCI(ci);
    }
    
    public void readFromFile(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        setCR(SudsFunctions.readDouble(file, swapBytes));
        setCI(SudsFunctions.readDouble(file, swapBytes));
    }
    
    public double getCR()
    {
        return cr;
    }
    
    public double getCI()
    {
        return ci;
    }
    
    public void setCR(double cr)
    {
        this.cr = cr;
    }
    
    public void setCI(double ci)
    {
        this.ci = ci;
    }
}
