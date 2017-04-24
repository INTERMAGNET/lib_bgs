/*
 * SudsParser.java
 *
 * Created on 29 November 2003, 13:10
 */

package bgs.geophys.library.Data.Suds;


import java.io.*;
import java.util.*;

/**
 *
 * @author  Ies
 */
public class SudsParser {
    private File dataFile;
    private int missedStructures;
    private List<SudsObject> objects;
    
    /** Creates a new instance of SudsParser */
    public SudsParser(File dataFile)
    {
        this.dataFile = dataFile;
    }
    
    public List parse()
    throws IOException, Exception
    {
        RandomAccessFile file = new RandomAccessFile(dataFile, "r");
        SudsStructureDetector detector = new SudsStructureDetector();
        SudsObject sudsObject;
        objects = new LinkedList<SudsObject>();
        boolean keepReading = true;
        while (keepReading)
        {
            try {
                
                sudsObject = detector.getNextStructure(file);
                if (sudsObject == null)
                    missedStructures++;
                else
                    objects.add(sudsObject);
            }
            catch (EOFException eof)
            {
                keepReading = false;
            }
        }
        
        return objects;
    }
    
    public int getMissedStructures()
    {
        return missedStructures;
    }
}
