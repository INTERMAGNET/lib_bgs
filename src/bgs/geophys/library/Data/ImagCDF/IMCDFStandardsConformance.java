/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import java.text.ParseException;

/**
 * An object that represents the standards conformance of the data (which, if any, standard it
 * conforms to)
 * 
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class IMCDFStandardsConformance 
{

    /** code for the standards conformance:
     *      NONE - does not conform to a standard;
     *      INTERMAGNET_1MINUTE - conforms to the INTERMAGNET standard for 1-minute data;
     *      INTERMAGNET_1SECOND - conforms to the INTERMAGNET standard for 1-second data; */
    public enum StandardsConformance {NONE, INTERMAGNET_1MINUTE, INTERMAGNET_1SECOND};

    // the publication state represented by this object
    private StandardsConformance stand_conform;
    
    /** create a standards performance code from the enumeration of the codes
     * @param code - one of the enumeration values for the code */
    public IMCDFStandardsConformance (StandardsConformance stand_conform)
    {
        this.stand_conform = stand_conform;
    }
    
    /** create a publication state code from a string
     * @param code_string one of "raw", "adjusted" or "definitive"
     * @throws ParseException if the string could not be recognised */
    public IMCDFStandardsConformance (String stand_conform_string)
    throws ParseException
    {
        if (stand_conform_string.equalsIgnoreCase("none"))
            stand_conform = StandardsConformance.NONE;
        else if (stand_conform_string.equalsIgnoreCase("INTERMAGNET-1Minute"))
            stand_conform = StandardsConformance.INTERMAGNET_1MINUTE;
        else if (stand_conform_string.equalsIgnoreCase("INTERMAGNET-1Second"))
            stand_conform = StandardsConformance.INTERMAGNET_1SECOND;
        else
            throw new ParseException("Invalid standards conformance code: " + stand_conform_string, 0);
    }
    
    public StandardsConformance getStandardsConformance () { return stand_conform; }
    
    /** get a string representation of the code
     * @return one of "Raw", "Adjusted" or "Definitive" */
    @Override
    public String toString ()
    {
        switch (stand_conform)
        {
            case NONE: return "None";
            case INTERMAGNET_1MINUTE: return "INTERMANGET-1Second";
            case INTERMAGNET_1SECOND: return "INTERMAGNET-1Minute";
        }
        return "Unknown";
    }    
    
}
