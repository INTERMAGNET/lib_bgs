package bgs.geophys.library.Data.Suds.StructureObjects;

import bgs.geophys.library.Data.Suds.*;

import java.io.*;
import java.util.*;

public class SudsStationComp extends SudsObject
{

    private SudsStatIdent scName;
    private int axim;
    private int incid;
    private double latitude;
    private double longitude;
    private float elevation;
    private String enclosure;
    private String annotation;
    private String recorder;
    private String rockClass;
    private int rockType;
    private String siteCondition;
    private String sensorType;
    private String dataType;
    private String dataUnits;
    private String polarity;
    private String status;
    private float maxGain;
    private float clipValue;
    private float conversionMilliVolts;
    private int channel;
    private int atodGain;
    private Date effectiveTime;
    private float clockCorrect;
    private float stationDelay;

    public void SudsStationComp()
    {

    }

    public void setScName(SudsStatIdent scName)
    {
        this.scName = scName;
    }

    public void setAxim(int axim)
    {
        this.axim = axim;
    }

    public void setIncid(int incid)
    {
        this.incid = incid;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public void setElevation(float elevation)
    {
        this.elevation = elevation;
    }

    public void setEnclosure(String enclosure)
    {
        this.enclosure = enclosure;
    }

    public void setAnnotation(String annotation)
    {
        this.annotation = annotation;
    }

    public void setRecorder(String recorder)
    {
        this.recorder = recorder;
    }

    public void setRockClass(String rockClass)
    {
        this.rockClass = rockClass;
    }

    public void setRockType(int rockType)
    {
        this.rockType = rockType;
    }

    public void setSiteCondition(String siteCondition)
    {
        this.siteCondition = siteCondition;
    }

    public void setSensorType(String sensorType)
    {
        this.sensorType = sensorType;
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public void setDataUnits(String dataUnits)
    {
        this.dataUnits = dataUnits;
    }

    public void setPolarity(String polarity)
    {
        this.polarity = polarity;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public void setMaxGain(float maxGain)
    {
        this.maxGain = maxGain;
    }

    public void setClipValue(float clipValue)
    {
        this.clipValue = clipValue;
    }

    public void setConversionMilliVolts(float conversionMilliVolts)
    {
        this.conversionMilliVolts = conversionMilliVolts;
    }

    public void setChannel(int channel)
    {
        this.channel = channel;
    }

    public void setAtodGain(int atodGain)
    {
        this.atodGain = atodGain;
    }

    public void setEffectiveTime(Date effectiveTime)
    {
        this.effectiveTime = effectiveTime;
    }

    public void setClockCorrect(float clockCorrect)
    {
        this.clockCorrect = clockCorrect;
    }

    public void setStationDelay(float stationDelay)
    {
        this.stationDelay = stationDelay;
    }


    public SudsStatIdent getScName()
    {
        return scName;
    }

    public int getAxim()
    {
        return axim;
    }

    public int getIncid()
    {
        return incid;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public float getElevation()
    {
        return elevation;
    }

    public String getEnclosure()
    {
        return enclosure;
    }

    public String getAnnotation()
    {
        return annotation;
    }

    public String getRecorder()
    {
        return recorder;
    }

    public String getRockClass()
    {
        return rockClass;
    }

    public int getRockType()
    {
        return rockType;
    }

    public String getSiteCondition()
    {
        return siteCondition;
    }

    public String getSensorType()
    {
        return sensorType;
    }

    public String getDataType()
    {
        return dataType;
    }

    public String getDataUnits()
    {
        return dataUnits;
    }

    public String getPolarity()
    {
        return polarity;
    }

    public String getStatus()
    {
        return status;
    }

    public float getMaxGain()
    {
        return maxGain;
    }

    public float getClipValue()
    {
        return clipValue;
    }

    public float getConversionMilliVolts()
    {
        return conversionMilliVolts;
    }

    public int getChannel()
    {
        return channel;
    }

    public int getAtodGain()
    {
        return atodGain;
    }

    public Date getEffectiveTime()
    {
        return effectiveTime;
    }

    public float getClockCorrect()
    {
        return clockCorrect;
    }

    public float getStationDelay()
    {
        return stationDelay;
    }


}
