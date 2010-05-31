/*
 * MuxData.java
 *
 * Created on 29 November 2003, 17:43
 */
package bgs.geophys.library.Data.Suds.StructureObjects;

import bgs.geophys.library.Data.Suds.*;
import java.util.*;

import java.util.Date;

/**
 *
 * @author  Ies
 */
public class SudsMuxData extends SudsObject
{
    private String netName;
    private Date beginTime;
    private float sampleRate;
    private int numChannels;
    private String dataType;
    private String dataDescription;
    private int numSamples;
    private int localTime;
    private int blockSize;

    private int[] data;
    private double[] doubleData;
    private List objectData;
    
    
    /** Creates a new instance of MuxData */
    public SudsMuxData()
    {
        
    }
    
    public void setNetName(String netName)
    {
        this.netName =  netName;
    }
    
    public void setBeginTime(Date beginTime)
    {
        this.beginTime =  new Date(beginTime.getTime());
    }
    
    public void setSampleRate(float sampleRate)
    {
        this.sampleRate = sampleRate;
    }
    
    public void setBlockSize(int blockSize)
    {
        this.blockSize = blockSize;
    }
    
    public void setNumChannels(int numChannels)
    {
        this.numChannels =  numChannels;
    }
    
    public void setDataType(String dataType)
    {
        this.dataType =  dataType;
    }
    
    public void setDataDescription(String dataDescription)
    {
        this.dataDescription =  dataDescription;
    }
    
    public void setNumSamples(int numSamples)
    {
        this.numSamples =  numSamples;
    }
    
    public void setLocalTime(int localTime)
    {
        this.localTime = localTime;
    }
    
    public void setData(int[] data)
    {
        this.data = data;
    }
    
    public void setData(double[] doubleData)
    {
        this.doubleData = doubleData;
    }
    
    public void setObjectData(List objectData)
    {
        this.objectData = objectData;
    }
    
    public String getNetName()
    {
        return netName;
    }
    
    public Date getBeginTime()
    {
        return new Date(beginTime.getTime());
    }
    
    public float getSampleRate()
    {
        return sampleRate;
    }
    
    public int getBlockSize()
    {
        return blockSize;
    }
    
    public int getNumChannels()
    {
        return numChannels;
    }
    
    public String getDataType()
    {
        return dataType;
    }
    
    public String getDataDescription()
    {
        return dataDescription;
    }
    
    public int getNumSamples()
    {
        return numSamples;
    }
    
    public int getLocalTime()
    {
        return localTime;
    }
    
    public int[] getData()
    {
        return data;
    }
    
    public double[] getDoubleData()
    {
        return doubleData;
    }
    
    public List getObjectData()
    {
        return objectData;
    }
    
}
