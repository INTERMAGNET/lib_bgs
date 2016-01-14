/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Autoplot;

import java.io.File;

/**
 * A utility to help build URI strings for Autoplot
 * 
 * URIs are assembled using the constructor and the add... methods
 * the extracted using the getURI() method
 * 
 * Currently only coded to handle 'DAT' resource types
 * 
 * @author smf
 */
public class AutoplotURIBuilder 
{

    public enum URIType {VAP_DAT_FILE}
    
    // general options
    private URIType uri_type;
    private String location;

    // DAT sub-system options
    private Integer dat_skip_lines;
    private String dat_time_field_name;
    private String dat_column_field_name;
    private String dat_time_format;
    private Double dat_fill_value;
    private String dat_title;
    private String dat_label;
    private String dat_units;
    
    public AutoplotURIBuilder (URIType uri_type, File file)
    {
        this.uri_type = uri_type;
        this.location = file.getAbsolutePath().replace ("\\", "/");
        
        // initialise DAT options
        dat_skip_lines = null;
        dat_time_field_name = null;
        dat_column_field_name = null;
        dat_time_format = null;
        dat_fill_value = null;
        dat_title = null;
        dat_label = null;
        dat_units = null;
    }
    
    // DAT options
    public void addDatSkipLines (int skip_lines) { this.dat_skip_lines = new Integer (skip_lines); }
    public void addDatTimeFieldName (String time_field_name) { this.dat_time_field_name = time_field_name; }
    public void addDatColumnFieldName (String column_field_name) { this.dat_column_field_name = column_field_name; }
    public void addDatTimeFormat (String time_format) { this.dat_time_format = time_format; }
    public void addDatFillValue (double fill_value) { this.dat_fill_value = new Double (fill_value); }
    public void addDatTitle (String title) { this.dat_title = title; }
    public void addDatLabel (String label) { this.dat_label = label; }
    public void addDatUnits (String units) { this.dat_units = units; }
    
    public String getURI ()
    {
        String uri;
        
        switch (uri_type)
        {
            case VAP_DAT_FILE: 
                uri = "vap+dat:file:/"; 
                uri += location + "?";
                int uri_base_length = uri.length();
                uri = addOption (uri, "skipLines",  dat_skip_lines,        uri.length() == uri_base_length);
                uri = addOption (uri, "time",       dat_time_field_name,   uri.length() == uri_base_length);
                uri = addOption (uri, "column",     dat_column_field_name, uri.length() == uri_base_length);
                uri = addOption (uri, "timeFormat", dat_time_format,       uri.length() == uri_base_length);
                uri = addOption (uri, "fill",       dat_fill_value,        uri.length() == uri_base_length);
                uri = addOption (uri, "title",      dat_title,             uri.length() == uri_base_length);
                uri = addOption (uri, "label",      dat_label,             uri.length() == uri_base_length);
                uri = addOption (uri, "units",      dat_units,             uri.length() == uri_base_length);
                break;
            default: 
                return null;
        }
        
        return uri;
    }
 
    private String addOption (String uri, String field_name, Object option, boolean is_first_option)
    {
        if (option == null) return uri;
        if (is_first_option) return uri + field_name + "=" + option.toString();
        return uri + "&" + field_name + "=" + option.toString();
    }
}
