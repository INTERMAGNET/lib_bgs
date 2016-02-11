/*
 * SudsFunctions.java
 *
 * Created on 29 November 2003, 13:17
 */

package bgs.geophys.library.Data.Suds;


import java.io.*;
import java.util.*;

/**
 *
 * @author  Ies
 */
public class SudsFunctions {
    
    private static final int LENGTH_OF_SHORT = 2;
    private static final int LENGTH_OF_LONG = 4;
    private static final int LENGTH_OF_DOUBLE = 8;
    private static final int LENGTH_OF_FLOAT = 4;
    
    
    
    /** Creates a new instance of SudsFunctions */
    public SudsFunctions()
    {
    }
    
    public static int readShort(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        return (int)readData(file, swapBytes, LENGTH_OF_SHORT).readShort();
    }
    
    public static int readLong(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        return readData(file, swapBytes, LENGTH_OF_LONG).readInt();
    }
    
    public static char readChar(RandomAccessFile file)
    throws IOException
    {
        int c = file.readByte();
        return (char)c;
    }
    
    public static String readString(RandomAccessFile file, int length)
    throws IOException
    {
        String s = new String();
        for (int i = 0; i < length; i++)
            s = s + readChar(file);
        return s;
    }
    
    public static Date readMsTime(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        long time = (long)(readDouble(file, swapBytes) * 1000);
        return new Date(time);
    }
    
    public static Date readStTime(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        long time = (long)(((long)readLong(file, swapBytes)) * 1000);
        return new Date(time);
    }
    
    public static double readDouble(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        return readData(file, swapBytes, LENGTH_OF_DOUBLE).readDouble();
    }
    
    public static float readFloat(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        return readData(file, swapBytes, LENGTH_OF_FLOAT).readFloat();
    }
    
    public static double readLonLat(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        return readDouble(file, swapBytes);
    }
    
    public static SudsVector readVector(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        SudsVector toReturn = new SudsVector();
        toReturn.readFromFile(file, swapBytes);
        return toReturn;
    }
    
    public static SudsComplex readComplex(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        SudsComplex toReturn = new SudsComplex();
        toReturn.readFromFile(file, swapBytes);
        return toReturn;
    }
    
    public static SudsTensor readTensor(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        SudsTensor toReturn = new SudsTensor();
        toReturn.readFromFile(file, swapBytes);
        return toReturn;
    }
    public static SudsStatIdent readStatIdent(RandomAccessFile file, boolean swapBytes)
    throws IOException
    {
        SudsStatIdent toReturn = new SudsStatIdent();
        toReturn.readFromFile(file, swapBytes);
        return toReturn;
    }
    
    private static DataInputStream readData(RandomAccessFile file, boolean swapBytes, int size)
    throws IOException
    {
        byte[] b = new byte[size];
        
        if (swapBytes)
        {
            for (int i = 0; i < size; i++) b[size - i - 1] = file.readByte();
        }
        else
        {
            for (int i = 0; i < size; i++) b[i] = file.readByte();
        }
        
        return new DataInputStream(new ByteArrayInputStream(b));
    }
}
