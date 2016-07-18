/*
 * SudsMuxDataParser.java
 *
 * Created on 29 November 2003, 17:45
 */

package bgs.geophys.library.Data.Suds.StructureObjects;


import bgs.geophys.library.Data.Suds.*;
import java.io.*;
import java.util.*;


/**
 *
 * @author  Ies
 */
public class SudsMuxDataParser implements SudsStructureParser
{
    private static final int structureID = 6;

    /** Creates a new instance of SudsMuxDataParser */
    public SudsMuxDataParser() 
    {
        
    }
    
    public int getStructureID()
    {
        return structureID;
    }
    
    public SudsObject parse(RandomAccessFile file, boolean swapBytes, int structureLength, int dataLength)
    throws IOException, Exception
    {
        SudsMuxData muxData = new SudsMuxData();
        
        // Read structure
        muxData.setNetName(SudsFunctions.readString(file, 4));
        muxData.setBeginTime(SudsFunctions.readMsTime(file, swapBytes));
        muxData.setLocalTime(SudsFunctions.readShort(file, swapBytes));
        muxData.setNumChannels(SudsFunctions.readShort(file, swapBytes));
        muxData.setSampleRate(SudsFunctions.readFloat(file, swapBytes));
        muxData.setDataType(String.valueOf(SudsFunctions.readChar(file)));
        muxData.setDataDescription(String.valueOf(SudsFunctions.readChar(file)));
        SudsFunctions.readShort(file, swapBytes); //Spare       
        muxData.setNumSamples(SudsFunctions.readLong(file, swapBytes));
        muxData.setBlockSize(SudsFunctions.readLong(file, swapBytes));
        

        
        // Read Data
        if (muxData.getDataType().compareTo("s") == 0)
            setData(file, swapBytes, dataLength, muxData);
        else if (muxData.getDataType().compareTo("l") == 0)
            setData(file, swapBytes, dataLength, muxData);
        else if (muxData.getDataType().compareTo("f") == 0)
            setDoubleData(file, swapBytes, dataLength, muxData);
        else if (muxData.getDataType().compareTo("d") == 0)
            setDoubleData(file, swapBytes, dataLength, muxData);
        else if (muxData.getDataType().compareTo("c") == 0)
            setObjectData(file, swapBytes, dataLength, muxData);
        else if (muxData.getDataType().compareTo("v") == 0)
            setObjectData(file, swapBytes, dataLength, muxData);
        else if (muxData.getDataType().compareTo("t") == 0)
            setObjectData(file, swapBytes, dataLength, muxData);
        else
            file.skipBytes(dataLength);
        
        return muxData;
    }
    
    private void setData(RandomAccessFile file, boolean swapBytes, int dataLength, SudsMuxData muxData)
    throws Exception
    {
        if (muxData.getDataType().compareTo("s") == 0)
        {
            int numChannels = muxData.getNumChannels();
            int blockSize = muxData.getBlockSize();
            int numCycles = dataLength / (blockSize * numChannels * 2);
            int data[][][] = new int[numCycles][numChannels][blockSize];
            for (int i = 0; i < numCycles; i++)
                for (int j = 0; j < numChannels; j++)
                    for (int k = 0; k < blockSize; k++)
                        data[i][j][k] = SudsFunctions.readShort(file, swapBytes);
            
            // Reorder data
            int reorderedData[] = new int[numCycles * numChannels * blockSize];
            int count = 0;
            for (int i = 0; i < numChannels; i++)
                for (int j = 0; j < numCycles; j++)
                    for (int k = 0; k < blockSize; k++)
                        reorderedData[count++] = data[j][i][k];
            muxData.setData(reorderedData);            
        } 
        else if (muxData.getDataType().compareTo("l") == 0)
        {
            int numChannels = muxData.getNumChannels();
            int blockSize = muxData.getBlockSize();
            int numCycles = dataLength / (blockSize * numChannels * 4);
            int data[][][] = new int[numCycles][numChannels][blockSize];
            for (int i = 0; i < numCycles; i++)
                for (int j = 0; j < numChannels; j++)
                    for (int k = 0; k < blockSize; k++)
                        data[i][j][k] = SudsFunctions.readLong(file, swapBytes);
            
            // Reorder data
            int reorderedData[] = new int[numCycles * numChannels * blockSize];
            int count = 0;
            for (int i = 0; i < numChannels; i++)
                for (int j = 0; j < numCycles; j++)
                    for (int k = 0; k < blockSize; k++)
                        reorderedData[count++] = data[j][i][k];
            muxData.setData(reorderedData);
            
        }
        
    }
    
    private void setDoubleData(RandomAccessFile file, boolean swapBytes, int dataLength, SudsMuxData muxData)
    throws Exception
    {        
        file.skipBytes(dataLength);
    }
    
    private void setObjectData(RandomAccessFile file, boolean swapBytes, int dataLength, SudsMuxData muxData)
    throws Exception
    {
        file.skipBytes(dataLength);        
    }
    
}
