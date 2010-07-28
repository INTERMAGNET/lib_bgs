/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Utils;

import bgs.geophys.ImagCD.ImagCDViewer.Data.CDDatabase;
import bgs.geophys.ImagCD.ImagCDViewer.Data.CDObservatoryIterator;
import bgs.geophys.ImagCD.ImagCDViewer.GlobalObjects;
import bgs.geophys.library.Maths.BGSMath;
import java.util.Collections;
import java.util.Vector;

/**
 * A class that makes a list of observatories and sorts them by distance
 * from a single observatory
 * 
 * @author smf
 */
public class ObsyByDistance 
{

    /** class to hold an individual observatory */
    public class ObsyInfoWithDistance
    implements Comparable<ObsyInfoWithDistance>
    {
        private String obsy_code;
        private double latitude;
        private double longitude;
        private double distance;
        private String display_name;
        public ObsyInfoWithDistance (String obsy_code, double latitude,
                                     double longitude, double distance)
        {
            this.obsy_code = obsy_code;
            this.latitude = latitude;
            this.longitude = longitude;
            this.distance = distance;
            display_name = obsy_code + " (" + ((int) distance) + " km)";
        }
        public double getDistance() { return distance; }
        public String getObservatoryCode() { return obsy_code; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getDisplayName() { return display_name; }
        public String toString() { return display_name; }
        public int compareTo (ObsyInfoWithDistance o)
        {
            if (this.getDistance() < o.getDistance()) return -1;
            if (this.getDistance() > o.getDistance()) return 1;
            return 0;
        }
    }
    
    private Vector<ObsyInfoWithDistance> obsy_list;
    private String obsy_ref;
    
    /** create a list of observatories across all databases
     * @param obsy_ref the observatory to find the distance from
     * @throws IllegalArgumentException if obsy_read canot be found */
    public ObsyByDistance (String obsy_ref)
    throws IllegalArgumentException
    {
        CDObservatoryIterator i;
        
        this.obsy_ref = obsy_ref;
        i = GlobalObjects.database_list.GetObservatoryIterator();
        commonInitialisation (i);
    }

    /** create a list of observatories from a single database
     * @param obsy_ref the observatory to find the distance from
     * @param database the database to use 
     * @throws IllegalArgumentException if obsy_read canot be found */
    public ObsyByDistance (String obsy_ref, CDDatabase database)
    throws IllegalArgumentException
    {
        CDObservatoryIterator i;
        
        this.obsy_ref = obsy_ref;
        i = database.GetObservatoryIterator();
        commonInitialisation (i);
    }
    
    private void commonInitialisation (CDObservatoryIterator i)
    throws IllegalArgumentException
    {
        CDObservatoryIterator.ObservatoryInfo info, ref_info;
        double distance;

        obsy_list = new Vector<ObsyInfoWithDistance> ();
    
        // find the reference observatory
        ref_info = null;
        for (info=i.GetFirstObservatory(); info != null; info=i.GetNextObservatory())
        {
            if (info.GetObservatoryCode().equalsIgnoreCase(obsy_ref))
            {
                ref_info = info;
                break;
            }
        }
        if (ref_info == null) throw new IllegalArgumentException ();
        
        // sort the observatories by distance from the reference observatory
        for (info=i.GetFirstObservatory(); info != null; info=i.GetNextObservatory())
        {
            if (! info.GetObservatoryCode().equalsIgnoreCase(obsy_ref))
            {
                distance = BGSMath.greatCircleDistance (BGSMath.EARTH_RADIUS_KM,
                                                        ref_info.GetLatitude(), ref_info.GetLongitude(),
                                                        info.GetLatitude(), info.GetLongitude());
                obsy_list.add (new ObsyInfoWithDistance (info.GetObservatoryCode(),
                                                         info.GetLatitude(), info.GetLongitude(),
                                                         distance));
            }
        }
        Collections.sort (obsy_list);
    }
    
    public String getReferenceObservatory () { return obsy_ref; }
    public int getNObservatories () { return obsy_list.size(); }
    public Vector<ObsyInfoWithDistance> getObservatoryList () { return obsy_list; }
    public String getObservatoryCode (int index) { return obsy_list.get(index).getObservatoryCode(); }
    public double getLatitude (int index) { return obsy_list.get(index).getLatitude(); }
    public double getLongitude (int index) { return obsy_list.get(index).getLongitude(); }
    public double getDistance (int index) { return obsy_list.get(index).getDistance(); }
    public String getDisplayName (int index) { return obsy_list.get(index).getDisplayName(); }
}
