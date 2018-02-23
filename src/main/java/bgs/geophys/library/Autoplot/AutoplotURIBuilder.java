/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Autoplot;

import java.io.File;
import java.util.Date;

/**
 * A utility to help build URI strings for Autoplot
 * 
 * URIs are assembled using the constructor and the add... methods
 * the extracted using the getURI() method
 * 
 * Valid elements for a DAT (ASCII) file are listed here:
 * http://autoplot.org/help#ASCII_Table
 * 
 * Currently only coded to handle 'DAT' resource types
 * 
 * @author smf
 */
public class AutoplotURIBuilder 
{

    public enum URIType {VAP_DAT_FILE, VAP_CDF_FILE}
    
    // general options
    private URIType uri_type;
    private String location;

    // common options
    private String timerange;

    // DAT sub-system options
    private Integer dat_skip_lines;
    private String dat_time_field_name;
    private String dat_column_field_name;
    private String dat_time_format;
    private Double dat_fill_value;
    private Double dat_valid_min;
    private Double dat_valid_max;
    private String dat_title;
    private String dat_label;
    private String dat_units;
    
    // CDF sub-system options
    private String cdf_var_name;

    /** create a URI for a single file
     * @param uri_type - the type of autoplot URI
     * @param file - the file to plot
     */
    public AutoplotURIBuilder (URIType uri_type, File file)
    {
        this.uri_type = uri_type;
        this.location = file.toURI().toString();
        initialiseOptions();
    }
    
    /** create a URI for a autoplot data file aggregation
     * @param uri_type - the type of autoplot URI
     * @param filename_pattern - the pattern for Autoplot to use to identify data files
     * @param timerange - the initial data to show
     */
    public AutoplotURIBuilder (URIType uri_type, File file, String timerange)
    {
        this.uri_type = uri_type;
        this.location = file.toURI().toString();
        initialiseOptions();
        addTimerange (timerange);
    }

    private void initialiseOptions ()
    {
        timerange = null;
        
        dat_skip_lines = null;
        dat_time_field_name = null;
        dat_column_field_name = null;
        dat_time_format = null;
        dat_fill_value = null;
        dat_valid_min = null;
        dat_valid_max = null;
        dat_title = null;
        dat_label = null;
        dat_units = null;
        
        cdf_var_name = null;
    }       
    
    // common options
    public void addTimerange (String timerange) { this.timerange = timerange; }
    
    // DAT options
    public void addDatSkipLines (int skip_lines) { this.dat_skip_lines = new Integer (skip_lines); }
    public void addDatTimeFieldName (String time_field_name) { this.dat_time_field_name = time_field_name; }
    public void addDatColumnFieldName (String column_field_name) { this.dat_column_field_name = column_field_name; }
    public void addDatTimeFormat (String time_format) { this.dat_time_format = time_format; }
    public void addDatFillValue (double fill_value) { this.dat_fill_value = new Double (fill_value); }
    public void addDatValidMin (double valid_min) { this.dat_valid_min = new Double (valid_min); }
    public void addDatValidMax (double valid_max) { this.dat_valid_max = new Double (valid_max); }
    public void addDatTitle (String title) { this.dat_title = title; }
    public void addDatLabel (String label) { this.dat_label = label; }
    public void addDatUnits (String units) { this.dat_units = units; }
    
    // CDF options
    public void addCDFVarName (String var_name) { this.cdf_var_name = var_name; }
    
    public String getURI ()
    {
        String uri;
        int uri_base_length;
        
        switch (uri_type)
        {
            case VAP_DAT_FILE: 
                uri = "vap+dat:"; 
                uri += location + "?";
                uri_base_length = uri.length();
                uri = addOption (uri, "skipLines",  dat_skip_lines,        uri.length() == uri_base_length);
                uri = addOption (uri, "time",       dat_time_field_name,   uri.length() == uri_base_length);
                uri = addOption (uri, "column",     dat_column_field_name, uri.length() == uri_base_length);
                uri = addOption (uri, "timeFormat", dat_time_format,       uri.length() == uri_base_length);
                uri = addOption (uri, "fill",       dat_fill_value,        uri.length() == uri_base_length);
                uri = addOption (uri, "validMin",   dat_valid_min,         uri.length() == uri_base_length);
                uri = addOption (uri, "validMax",   dat_valid_max,         uri.length() == uri_base_length);
                uri = addOption (uri, "title",      dat_title,             uri.length() == uri_base_length);
                uri = addOption (uri, "label",      dat_label,             uri.length() == uri_base_length);
                uri = addOption (uri, "units",      dat_units,             uri.length() == uri_base_length);
                break;
            case VAP_CDF_FILE:
                uri = "vap+cdf:"; 
                uri += location + "?";
                uri_base_length = uri.length();
                uri = addOption (uri, null,         cdf_var_name,          uri.length() == uri_base_length);
                break;
            default: 
                return null;
        }
        
        uri = addOption (uri, "timerange",  timerange,             uri.length() == uri_base_length);
        
        return uri;
    }
 
    private String addOption (String uri, String field_name, Object option, boolean is_first_option)
    {
        if (option == null) return uri;
        if (field_name == null) field_name = "";
        else field_name = field_name + "=";
        if (is_first_option) return uri + field_name + option.toString();
        return uri + "&" + field_name + option.toString();
    }
}
