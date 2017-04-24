/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Gdas.GdasCollect.Config;

/**
 * This class uniquely identifies a system, using its station
 * code and GDAS number. e.g. "ESK 1".
 * 
 * @author smf
 */
public class GDASName 
implements Comparable<GDASName>
{
    /** configuration variable: the station code */
    private String station_code;
    /** configuration variable: the number of the GDAS system */
    private int gdas_no;

    /** create an empty system name */
    public GDASName ()
    {
        this (null, -1);
    }    
    
    /** create a new system name */
    public GDASName (String station_code, int gdas_no)
    {
        this.station_code = station_code;
        this.gdas_no = gdas_no;
    }    
    
    /** create a system name as a copy of another */
    public GDASName (GDASName copy)
    {
        if (copy.station_code == null) this.station_code = null;
        else this.station_code = new String (copy.station_code);
        this.gdas_no = copy.gdas_no;
    }    
    
    // read methods for configuration variables
    public boolean isEmpty () 
    { 
        if (station_code == null) return true;
        if (gdas_no < 0) return true;
        return false;
    }
    public String getStationCode() { return station_code; }
    public int getGdasNumber() { return gdas_no; }
    
    // write methods for configuration variables
    public void setStationCode (String station_code) { this.station_code = station_code; }
    public void setGdasNumber (int gdas_no) { this.gdas_no = gdas_no; }
    
    /** show this class as a string */
    @Override
    public String toString () 
    {
        if (station_code == null) return "unknown";
        return station_code + ":" + Integer.toString (gdas_no); 
    }

    /** implementation of the comparable interface */
    public int compareTo (GDASName o) 
    {
        int ret_val;
        
        if (o == null) ret_val = 1;
        else
        {
            if (o.station_code == null)
            {
                if (this.station_code == null) ret_val = 0;
                else ret_val = 1;
            }
            else if (this.station_code == null)
                ret_val = -1;
            else
                ret_val = this.station_code.compareTo(o.station_code);
            if (ret_val == 0)
            {
                if (this.gdas_no < o.gdas_no) ret_val = -1;
                else if (this.gdas_no > o.gdas_no) ret_val = 1;
            }
        }
        
        return ret_val;
    }
    
    /** equality test */
    public boolean equals (GDASName o)
    {
        if (compareTo(o) == 0) return true;
        return false;
    }

}
