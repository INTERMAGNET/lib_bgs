/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import java.text.ParseException;

/**
 * A class to enumerate the types of data an ImagCDFVariable can hold
 * 
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class IMCDFVariableType 
{
    
    /** code for the type of data that can be held:
     * GeomagneticFieldElement - geomagnetic data
     * Temperature - temperature data */
    public enum VariableTypeCode {GeomagneticFieldElement, Temperature}

    // the variable type code represented by this object
    private VariableTypeCode code;

    /** create a variable type code from the enumeration of the codes
     * @param code - one of the enumeration values for the code */
    public IMCDFVariableType (VariableTypeCode code)
    {
        this.code = code;
    }

    /** create a baseline type code from a string
     * @param code_string one of "GeomagneticField", or "Temperature"
     * @throws ParseException if the string could not be recognised */
    public IMCDFVariableType (String code_string)
    throws ParseException
    {
        if (code_string.equalsIgnoreCase("GeomagneticFieldElement"))
            code = VariableTypeCode.GeomagneticFieldElement;
        else if (code_string.equalsIgnoreCase("Temperature"))
            code = VariableTypeCode.Temperature;
        else
            throw new ParseException("Invalid variable type code: " + code_string, 0);
    }

    public VariableTypeCode getCode () { return code; }

    /** get a string representation of the code
     * @return one of "GeomagneticFieldElement" or "Temperature" */
    @Override
    public String toString ()
    {
        switch (code)
        {
            case GeomagneticFieldElement: return "GeomagneticFieldElement";
            case Temperature: return "Temperature";
        }
        return "Unknown";
    }
    
    /** create the name of the variable that will be used in the CDF file
     * @param element_no the element number for this variable (starting at 0)
     * @return the variable name */
    public String getCDFFileVariableName (int element_no)
    {
        element_no ++;      // variable names are '1'-based
        return toString() + element_no;
    }
    
}
