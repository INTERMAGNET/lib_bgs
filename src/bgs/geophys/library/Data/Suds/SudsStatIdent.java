/*
 * SudsStatIdent.java
 *
 * Created on 29 November 2003, 20:51
 */

package bgs.geophys.library.Data.Suds;


import java.io.*;

/**
 *
 * @author  Ies
 */
public class SudsStatIdent extends Object
{
    private String network;
    private String stationName;
    private String component;
    private int instrumentType;
    
    /** Creates a new instance of SudsTensor */
    public SudsStatIdent()
    {
        
    }
    
    public void readFromFile(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        setNetwork(SudsFunctions.readString(file, 4));
        setStationName(SudsFunctions.readString(file, 5));
        setComponent(SudsFunctions.readChar(file));
        setInstrumentType(SudsFunctions.readShort(file, swapBytes));
    }
    
    public String getNetwork()
    {
        return network;
    }
    
    public String getStationName()
    {
        return stationName;
    }
    
    public String getComponent()
    {
        return component;
    }
    
    public int getInstrumentType()
    {
        return instrumentType;
    }
    
    public void setNetwork(String network)
    {
        this.network = network.trim();
    }
    
    public void setStationName(String stationName)
    {
        this.stationName = stationName.trim();
    }
    
    public void setComponent(char component)
    {
        this.component = String.valueOf(component);
    }

    public void setInstrumentType(int instrumentType)
    {
        this.instrumentType = instrumentType;
    }
}
