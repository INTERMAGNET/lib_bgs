/*
 * sudsStructureDetector.java
 *
 * Created on 29 November 2003, 13:10
 */

package bgs.geophys.library.Data.Suds;


import bgs.geophys.library.Data.Suds.StructureObjects.SudsMuxDataParser;
import bgs.geophys.library.Data.Suds.StructureObjects.SudsStationCompParser;
import java.io.*;

/**
 *
 * @author  Ies
 */
public class SudsStructureDetector {
    
    private static final String MAGIC_CHAR = "S";
    private String machineID;
    private int structureID;
    private boolean swapBytes;
    private int structureLength;
    private int dataLength;
    
    /** Creates a new instance of sudsStructureDetector */
    public SudsStructureDetector()
    {
        
    }
    
    public SudsObject getNextStructure(RandomAccessFile dataFile)
    throws Exception
    {
        SudsStructureParser parser;
        reset();
        getHeader(dataFile);
        
        // Check known structure types here
        parser = new SudsMuxDataParser();
        if (parser.getStructureID() == structureID)
            return parser.parse(dataFile, swapBytes, structureLength, dataLength);
        
        parser = new SudsStationCompParser();
        if (parser.getStructureID() == structureID)
            return parser.parse(dataFile, swapBytes, structureLength, dataLength);
        
        // If no matches move to end of structure/data and return null
        dataFile.skipBytes(structureLength + dataLength);
        return null;
    }
    
    private void getHeader(RandomAccessFile dataFile)
    throws Exception
    {
        if (String.valueOf(SudsFunctions.readChar(dataFile)).compareTo(MAGIC_CHAR) != 0)
        {
            throw (new Exception("Synchronization of file lost."));
        }
        
        machineID = String.valueOf(SudsFunctions.readChar(dataFile));
        setSwapBytes();
        structureID = SudsFunctions.readShort(dataFile, swapBytes);
        structureLength = SudsFunctions.readLong(dataFile, swapBytes);
        dataLength = SudsFunctions.readLong(dataFile, swapBytes);
    }
    
    private void setSwapBytes()
    {
        if (machineID.compareTo("6") == 0) swapBytes = true;
        else swapBytes = false;
    }
    
    private void reset()
    {
        machineID = "";
        structureID = 0;
        swapBytes = false;
        structureLength = 0;
        dataLength = 0;
    }
    
}
