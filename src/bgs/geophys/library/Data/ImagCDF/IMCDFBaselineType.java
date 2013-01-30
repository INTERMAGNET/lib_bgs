/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import java.text.ParseException;

/**
 * An object that represents the type of baseline applied to a piece of geomagnetic data
 * and converts to and from string representations of the code
 * 
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class IMCDFBaselineType 
{

    /** code for the type of baseline that has been applied to the data:
     *      NONE - no baseline;
     *      CONSTANT - a constant value (arbitrary) baseline (eg an annual mean);
     *      QUASI_DEFINITIVE - a quasi-definitive baseline
     *      DEFINTIVE - a definitive baseline */
    public enum BaselineCode {NONE, PRELIMINARY, QUASI_DEFINITIVE, DEFINITIVE}

    // the baseline code represented by this object
    private BaselineCode code;

    /** create a baseline type code from the enumeration of the codes
     * @param code - one of the enumeration values for the code */
    public IMCDFBaselineType (BaselineCode code)
    {
        this.code = code;
    }

    /** create a baseline type code from a string
     * @param code_string one of "none", "preliminary", "quasi-definitive" or "definitive"
     * @throws ParseException if the string could not be recognised */
    public IMCDFBaselineType (String code_string)
    throws ParseException
    {
        if (code_string.equalsIgnoreCase("none"))
            code = BaselineCode.NONE;
        else if (code_string.equalsIgnoreCase("preliminary"))
            code = BaselineCode.PRELIMINARY;
        else if (code_string.equalsIgnoreCase("quasi-definitive") ||
                 code_string.equalsIgnoreCase("quasi-def") ||
                 code_string.equalsIgnoreCase("qd"))
            code = BaselineCode.QUASI_DEFINITIVE;
        else if (code_string.equalsIgnoreCase("definitive"))
            code = BaselineCode.DEFINITIVE;
        else
            throw new ParseException("Invalid baseline type code: " + code_string, 0);
    }

    public BaselineCode getCode () { return code; }

    /** get a string representation of the code
     * @return one of "None", "Preliminary", "Quasi-definitive" or "Definitive" */
    @Override
    public String toString ()
    {
        return toString (true);
    }
    
    /** get a string representation of the code
     * @return one of "None", "Preliminary", "Quasi-definitive" or "Definitive" or the short form of these */
    public String toString (boolean long_form)
    {
        switch (code)
        {
            case NONE: if (long_form) return "None"; return "N";
            case PRELIMINARY: if (long_form) return "Preliminary"; return "P";
            case QUASI_DEFINITIVE: if (long_form) return "Quasi-definitive"; return "Q";
            case DEFINITIVE: if (long_form) return "Definitive"; return "D";
        }
        if (long_form) return "Unknown";
        return "U";
    }

    /** convert an IMF or IAGA-2002 data type code to an IMCDF baseline type
     * @param type_code one of the IMF or IAGA-2002 data type codes - only
     *        the first letter is used to decode the code
     * @return the baseline type */
    public static IMCDFBaselineType getBaselineType (String type_code)
    {
        if (type_code.length() > 0)
        {
            switch (type_code.charAt(0))
            {
                case 'A': return new IMCDFBaselineType (IMCDFBaselineType.BaselineCode.PRELIMINARY);
                case 'R': return new IMCDFBaselineType (IMCDFBaselineType.BaselineCode.NONE);
                case 'Q': return new IMCDFBaselineType (IMCDFBaselineType.BaselineCode.QUASI_DEFINITIVE);
                case 'D': return new IMCDFBaselineType (IMCDFBaselineType.BaselineCode.DEFINITIVE);
                case 'T': return new IMCDFBaselineType (IMCDFBaselineType.BaselineCode.NONE);
                case 'Z': return new IMCDFBaselineType (IMCDFBaselineType.BaselineCode.DEFINITIVE);
            }
        }
        return new IMCDFBaselineType (IMCDFBaselineType.BaselineCode.NONE);
    }
    
    
}
