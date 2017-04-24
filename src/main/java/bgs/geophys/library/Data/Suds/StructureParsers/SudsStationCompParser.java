/*
 * SudsStationCompParser.java
 *
 * Created on 29 November 2003, 22:24
 */

package bgs.geophys.library.Data.Suds.StructureObjects;

import bgs.geophys.library.Data.Suds.*;

import java.io.*;

/**
 *
 * @author  Ies
 */
public class SudsStationCompParser implements SudsStructureParser
{
     private static final int structureID = 5;
     
    /** Creates a new instance of SudsStationCompParser */
    public SudsStationCompParser() 
    {
        
    }
    
    public int getStructureID()
    {
        return structureID;
    }
    
    public SudsObject parse(RandomAccessFile file, boolean swapBytes, int structureLength, int dataLength) 
    throws IOException, Exception 
    {
        SudsStationComp stationComp = new SudsStationComp();
        
        // Read structure
        stationComp.setScName(SudsFunctions.readStatIdent(file, swapBytes));
        stationComp.setAxim(SudsFunctions.readShort(file, swapBytes));
        stationComp.setIncid(SudsFunctions.readShort(file, swapBytes));
        stationComp.setLatitude(SudsFunctions.readLonLat(file, swapBytes));
        stationComp.setLongitude(SudsFunctions.readLonLat(file, swapBytes));
        stationComp.setElevation(SudsFunctions.readFloat(file, swapBytes));
        stationComp.setEnclosure(String.valueOf(SudsFunctions.readChar(file)));
        stationComp.setAnnotation(String.valueOf(SudsFunctions.readChar(file)));
        stationComp.setRecorder(String.valueOf(SudsFunctions.readChar(file)));
        stationComp.setRockClass(String.valueOf(SudsFunctions.readChar(file)));
        stationComp.setRockType(SudsFunctions.readShort(file, swapBytes));
        stationComp.setSiteCondition(String.valueOf(SudsFunctions.readChar(file)));
        stationComp.setSensorType(String.valueOf(SudsFunctions.readChar(file)));
        stationComp.setDataType(String.valueOf(SudsFunctions.readChar(file)));
        stationComp.setDataUnits(String.valueOf(SudsFunctions.readChar(file)));
        stationComp.setPolarity(String.valueOf(SudsFunctions.readChar(file)));
        stationComp.setStatus(String.valueOf(SudsFunctions.readChar(file)));
        stationComp.setMaxGain(SudsFunctions.readFloat(file, swapBytes));
        stationComp.setClipValue(SudsFunctions.readFloat(file, swapBytes));
        stationComp.setConversionMilliVolts(SudsFunctions.readFloat(file, swapBytes));
        stationComp.setChannel(SudsFunctions.readShort(file, swapBytes));
        stationComp.setAtodGain(SudsFunctions.readShort(file, swapBytes));
        stationComp.setEffectiveTime(SudsFunctions.readStTime(file, swapBytes));
        stationComp.setClockCorrect(SudsFunctions.readFloat(file, swapBytes));
        stationComp.setStationDelay(SudsFunctions.readFloat(file, swapBytes));
        
        file.skipBytes(dataLength);
        
        return stationComp;
    }
    
}
