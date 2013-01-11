/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Data.ImagCDF;

import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Variable;
import java.text.ParseException;
import java.util.Date;

/**
 * A class that holds an ImagCDF variable along with it's metadata
 * 
 * THE IMCDF ROUTINES SHOULD NOT HAVE DEPENDENCIES ON OTHER LIBRARY ROUTINES -
 * IT MUST BE POSSIBLE TO DISTRIBUTE THE IMCDF SOURCE CODE
 * 
 * @author smf
 */
public class ImagCDFVariable 
{
    
    
    // private member data - the type of data that this object holds
    private IMCDFVariableType variable_type;
    
    // private member data - the CDF variable attributes
    private String field_nam;
    private double valid_min;
    private double valid_max;
    private String units;
    private double fill_val;
    private Date start_date;
    private double samp_per;
    private String elem_rec;
    private double orig_freq;
    
    // private member data - the data array
    private double data [];
    
    /** create a ImagCDF variable from the contents of a CDF file
     * @param cdf the open CDF file encapsulated in an ImagCDFLowLevel object
     * @param variable_type the type of data this object should look for in the CDF file
     * @param element_no the element number, 0 for 1st element, ...
     * @throws CDFException if there is a problem reading the data */
    public ImagCDFVariable (ImagCDFLowLevel cdf, IMCDFVariableType variable_type, int element_no)
    throws CDFException, ParseException
    {
        Variable var;

        this.variable_type = variable_type;
        var = cdf.getVariable (variable_type.getCDFFileVariableName(element_no));
        
        field_nam =                             cdf.getVariableAttributeString("FIELDNAM",  var);
        valid_min =                             cdf.getVariableAttributeDouble("VALIDMIN",  var);
        valid_max =                             cdf.getVariableAttributeDouble("VALIDMAX",  var);
        units =                                 cdf.getVariableAttributeString("UNITS",     var);
        fill_val =                              cdf.getVariableAttributeDouble("FILLVAL",   var);
        start_date = ImagCDFLowLevel.parseDate (cdf.getVariableAttributeString("StartDate", var));
        samp_per =                              cdf.getVariableAttributeDouble("SampPer",   var);
        elem_rec =                              cdf.getVariableAttributeString("ElemRec",   var).toUpperCase();
        orig_freq =                             cdf.getVariableAttributeDouble("OrigFreq",  var);
        
        data = ImagCDFLowLevel.getDataArray (var);

        checkMetadata ();
    }

    /** create an ImagCDFVariable from data and metadata for subsequent writing to a file
     * @param variable_type the type of variable - geomagnetic element or temperature
     * @param field_nam set the "Geomagnetic Field Element " and a number or
     *                  "Temperature" and a number
     * @param valid_min the smallest possible value that the data can take
     * @param valid_max the largest possible value that the data can take
     * @param units name of the units that the data is in
     * @param fill_val the value that, when present in the data, shows that the data point was not recorded
     * @param samp_per the period between samples, in seconds
     * @param start_date the date/time of the first data sample
     * @param elem_rec for geomagnetic data the element the this data represents.
     *                 for temperature data the name of the location where temperature was recorded
     * @param orig_freq the frequency that the data was originally recorded at
     * @param data the data as an array
     * @throws CDFException if the elem_rec or samp_per are invalid */
    public ImagCDFVariable (IMCDFVariableType variable_type, String field_nam,
                            double valid_min, double valid_max,
                            String units, double fill_val, double samp_per, Date start_date,
                            String elem_rec, double orig_freq, double data [])
    throws CDFException
    {
        this.variable_type = variable_type;
        this.field_nam = field_nam;
        this.valid_min = valid_min;
        this.valid_max = valid_max;
        this.units = units;
        this.fill_val = fill_val;
        this.start_date = start_date;
        this.samp_per = samp_per;
        this.elem_rec = elem_rec;
        this.orig_freq = orig_freq;
        this.data = data;
        
        checkMetadata ();
    }

    /** write this data to a CDF file
     * @param cdf the CDF file to write into
     * @param element_no the number of this element (contiguous from one for each variable_type)
     * @throws CDFException if there is an error */
    public void write (ImagCDFLowLevel cdf, int element_no)
    throws CDFException
    {
        Variable var;
        
        var = cdf.createDataArray (variable_type.getCDFFileVariableName(element_no), data);

        cdf.addVariableAttribute ("FIELDNAM",  var, field_nam);
        cdf.addVariableAttribute ("VALIDMIN",  var, new Double (valid_min));
        cdf.addVariableAttribute ("VALIDMAX",  var, new Double (valid_max));
        cdf.addVariableAttribute ("UNITS",     var, units);
        cdf.addVariableAttribute ("FILLVAL",   var, new Double (fill_val));
        cdf.addVariableAttribute ("StartDate", var, ImagCDFLowLevel.formatDate(start_date));
        cdf.addVariableAttribute ("SampPer",   var, new Double (samp_per));
        cdf.addVariableAttribute ("ElemRec",   var, elem_rec);
        cdf.addVariableAttribute ("OrigFreq",  var, new Double (orig_freq));
    }
    
    public IMCDFVariableType getVariableType () { return variable_type; }
    public String getFieldName () { return field_nam; }
    public double getValidMinimum () { return valid_min; }
    public double getValidMaximum () { return valid_max; }
    public String getUnits () { return units; }
    public double getFillValue () { return fill_val; }
    public Date getStartDate () { return start_date; }
    public double getSamplePeriod () { return samp_per; }
    public String getElementRecorded () { return elem_rec; }
    public double getOriginalFrequency () { return orig_freq; }
    
    public double [] getData () { return data; }

    public boolean isVectorGeomagneticData ()
    {
        if (variable_type.getCode() != IMCDFVariableType.VariableTypeCode.GeomagneticFieldElement)
            return false;
        switch (elem_rec.toUpperCase().charAt(0))
        {
            case 'X':
            case 'Y':
            case 'Z':
            case 'H':
            case 'D':
            case 'E':
            case 'V':
            case 'I':
            case 'F':
                return true;
        }
        return false;
    }
    
    public boolean isScalarGeomagneticData ()
    {
        if (variable_type.getCode() != IMCDFVariableType.VariableTypeCode.GeomagneticFieldElement)
            return false;
        switch (elem_rec.toUpperCase().charAt(0))
        {
            case 'S':
            case 'G':
                return true;
        }
        return false;
    }
    
    private void checkMetadata ()
    throws CDFException
    {
        if (variable_type.getCode() == IMCDFVariableType.VariableTypeCode.GeomagneticFieldElement)
        {
            if (elem_rec.length() != 1 || "XYZHDEVIFSG".indexOf (elem_rec) < 0)
                throw new CDFException ("Data array '" + field_nam + "' contains an invalid element code: " + elem_rec);
        }
        if (samp_per <= 0.0)
            throw new CDFException ("Data array '" + field_nam + "' has an invlaid sample period: " + Double.toString(samp_per));
    }
    
}
