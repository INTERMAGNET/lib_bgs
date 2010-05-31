/*
 * SudsStructureParser.java
 *
 * Created on 29 November 2003, 13:11
 */

package bgs.geophys.library.Data.Suds;


import java.io.*;

/**
 *
 * @author  Ies
 */
public interface SudsStructureParser
{

    public int getStructureID();

    public SudsObject parse(RandomAccessFile file, boolean swapBytes, int structureLength, int dataLength)
    throws IOException, Exception;
    
}
