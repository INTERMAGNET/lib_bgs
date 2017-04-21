/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.webService;

import bgs.geophys.library.Data.GeomagAbsoluteValue;

/**
 * Represents magnetometer components.
 * @author sani
 */
public enum Components {

    /**
     * Components enum type literals.
     */
    ORIENTATION_XYZF("XYZF", GeomagAbsoluteValue.ORIENTATION_XYZ),
    ORIENTATION_DHZF("DHZF", GeomagAbsoluteValue.ORIENTATION_HDZ),
    ORIENTATION_DHIF("DHIF", GeomagAbsoluteValue.ORIENTATION_DIF),
    ORIENTATION_UNKNOWN("", GeomagAbsoluteValue.ORIENTATION_UNKNOWN);

    private final String componentsString;
    private final int componentsCode;
    private GeomagAbsoluteValue value;

    private Components(String componentsString, int componentsCode){
        this.componentsString = componentsString;
        this.componentsCode = componentsCode;
    }

    /**
     * Returns the GeomagAbsoluteValue.ORIENTATION_ constant for the components type.
     * @return
     */
    public int componentsCode() {
        return componentsCode;
    }

    /**
     * Returns the GeomagdataFormat.compCode string for the components type.
     * @return
     */
    public String componentsString() {
        return componentsString;
    }

    /**
     * Accepts a GeomaAbsoluteValue with values for the components.
     * @param value
     */
    public void setValue(GeomagAbsoluteValue value){
        this.value = value;
    }

    /**
     * Returns the value of the first component of the Components type.
     * @return The value of the first component.
     */
    public double getComponent1(){
        double comp=0.0;
        if(this == ORIENTATION_DHZF){
            comp = value.getDMinutes();
        }else if(this == ORIENTATION_XYZF){
            comp = value.getX();
        }else if(this == ORIENTATION_DHIF){
            comp = value.getDMinutes();
        }
        return comp;
    }

    /**
     * Returns the value of the second component of the Components type.
     * @return The value of the second component.
     */
    public double getComponent2(){
        double comp=0.0;
        if(this == ORIENTATION_DHZF){
            comp = value.getH();
        }else if(this == ORIENTATION_XYZF){
            comp = value.getY();
        }else if(this == ORIENTATION_DHIF){
            comp = value.getH();
        }
        return comp;
    }

    /**
     * Returns the value of the third component of the Components type.
     * @return The value of the third component.
     */
    public double getComponent3(){
        double comp=0.0;
        if(this == ORIENTATION_DHZF){
            comp = value.getZ();
        }else if(this == ORIENTATION_XYZF){
            comp = value.getZ();
        }else if(this == ORIENTATION_DHIF){
            comp = value.getIMinutes();
        }
        return comp;
    }

    /**
     * Returns the value of the fourth component of the Components type.
     * @return The value of the fourth component.
     */
    public double getComponent4(){
        return value.getF();
    }


}
