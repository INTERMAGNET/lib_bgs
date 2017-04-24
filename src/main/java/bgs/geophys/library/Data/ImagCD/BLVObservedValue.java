/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;



/**
 *
 * @author jex
 */

/**
 * Class containing information about one observed value.
*/ 
  
  public class BLVObservedValue {



    private Integer day;

//    private Integer hComponent;
    public Double hComponent;
    public Double dComponent;
    public Double uComponent;
    public Double xComponent;
    public Double iComponent;
    public Double vComponent;
    public Double yComponent;
    public Double zComponent;
    public Double fComponent;
    public Double sComponent;  //IBFV2.00 onwards only, scalar F value

 
/**
 * Reads an observed value from buf
 * If the component is not recognised, a BLVException is created
 * @param buf character string from the file
 * @param componentOrder (already read from file)
 * @see  bgs.geophys.lib_BaseLineValue#BLVData
 * @see bgs.geophys.lib_BaseLineValue#BLVException 
 */
public void readBLV(char[] buf, String componentOrder,String format) throws BLVException{
    setHComponent((Double)null);
    setDComponent((Double)null);
    setUComponent((Double)null);
    setXComponent((Double)null);
    setIComponent((Double)null);
    setVComponent((Double)null);
    setYComponent((Double)null);
    setZComponent((Double)null);
    setFComponent((Double)null);
    setSComponent((Double)null);
//    for(int i=0;i<29;i++){
//    System.out.print(buf[i]);}
//    System.out.println();
    Integer comp1;
    Integer comp2;
    Integer comp3;
    
    Double c1,c2,c3,cs = null;
   
//   System.out.println("Component order: " + componentOrder);

    try{
      setDay((Integer) Integer.parseInt(String.copyValueOf(buf,0,3).replace("+"," ").trim()));
      
      if(format.equalsIgnoreCase("IBFV1.11")){
      comp1 = (Integer)Integer.parseInt(String.copyValueOf(buf,4,7).replace("+"," ").trim());
      comp2 = (Integer) Integer.parseInt(String.copyValueOf(buf,12,7).replace("+"," ").trim());
      comp3 = (Integer) Integer.parseInt(String.copyValueOf(buf,20,7).replace("+"," ").trim());
      c1 = new Double(comp1);
      c2 = new Double(comp2);
      c3 = new Double(comp3);
      }
      else{
       c1 = Double.parseDouble(String.copyValueOf(buf,4,9));
       c2 = Double.parseDouble(String.copyValueOf(buf,14,9));
       c3 = Double.parseDouble(String.copyValueOf(buf,24,9));
       cs = Double.parseDouble(String.copyValueOf(buf,34,9));
      }
    
      switch(componentOrder.charAt(0)){
            case ('H'): 
             setHComponent(c1);
             break;
            case ('D'):
             setDComponent(c1);
             break;
            case ('U'): 
             setUComponent(c1);
             break;
            case ('X'):
             setXComponent(c1);
             break;
            default:
             throw new BLVException("First Component not recognised from component order");
             
        }
     
        switch(componentOrder.charAt(1)){
            case ('D'):
             setDComponent(c2);
             break;
            case ('I'): 
             setIComponent(c2);
             break;
            case ('V'):
             setVComponent(c2);
             break;
            case ('Y'):
             setYComponent(c2);
             break;
            default:
             throw new BLVException("Second Component not recognised from component order");
             
        }
         
         switch(componentOrder.charAt(2)){
            case ('Z'):
             setZComponent(c3);
             break;
            case ('F'):
             setFComponent(c3);
             break;
            default:
             throw new BLVException("Third Component not recognised from component order");
             
        }
      // put in S Component if new format
      if(format.equalsIgnoreCase("IBFV2.00")){
          setSComponent(cs);
      }
    } /* end of try - problem parsing */
    
    catch(NumberFormatException e){
      throw new BLVException("Unable to read from "+ buf);  
      
    }
        }

/**
 * Used for debugging purposes
 */
public void printBLV(){
    System.out.println("Day of reading is " + getDay());
    if(hComponent!=null) System.out.println("H: "+getHComponent());
    if(sComponent!=null) System.out.println("S: "+getSComponent());
//    if(getDComponent()!=null) System.out.println("D: "+getDComponent());
//    if(getUComponent()!=null) System.out.println("U: "+getUComponent());
//    if(getXComponent()!=null) System.out.println("X: "+getXComponent());
//    if(getIComponent()!=null) System.out.println("I: "+getIComponent());
//    if(getVComponent()!=null) System.out.println("V: "+getVComponent());
//    if(getYComponent()!=null) System.out.println("Y: "+getYComponent());
//    if(getZComponent()!=null) System.out.println("Z: "+getZComponent());
//    if(getFComponent()!=null) System.out.println("F: "+getFComponent());
////    System.out.print(" First component is: "+ getHDUXValue());
//    System.out.print(" Second component is: "+ getDIVYValue());
//    System.out.println(" Third component is: "+getZFValue());
}

/**
 * 
 * @return day of year of the reading
 */
    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }
/**
 * 
 * @return H component if there is one, null is returned otherwise
 */
//    public Integer getHComponent() {
//        if(hComponent == 999999 || hComponent == null) return null;
//          return hComponent;         
//    }
        public Double getHComponent() {
//        if(hComponent == 999999 || hComponent == 99999.0 || hComponent == null) return null;
          if (isMissing(hComponent) || hComponent==null) return null;
          return hComponent;         
    }
    
    /**
     * 
     * @param scale
     * @return H component divided by scale. Null is returned
     *        if there is no H component 
     */
    public Double getHComponentScaled(Double scale) {
        if(getHComponent() == null) return null;
        return hComponent/scale;
    }


    public void setHComponent(Integer hComponent) {
        this.hComponent = new Double(hComponent);
    }

       public void setHComponent(Double hComponent) {
        this.hComponent = hComponent;
    }
       
    public Double getDComponent() {
//        if(dComponent == 999999 || dComponent == 99999.0 || dComponent == null) return null;
          if (isMissing(dComponent) || dComponent==null) return null;
          return dComponent;
    }
    
    public Double getDComponentScaled(Double scale) {
        if(getDComponent() == null) return null;
        return dComponent/scale;
    }


    public void setDComponent(Integer dComponent) {
        this.dComponent = new Double(dComponent);
    }

    public void setDComponent(Double dComponent) {
        this.dComponent = dComponent;
    }

    public Double getUComponent() {
//        if(uComponent == 999999 || uComponent == 99999.0 || uComponent == null) return null;
    if (isMissing(uComponent) || uComponent==null) return null;
          return uComponent;
    }
    public Double getUComponentScaled(Double scale) {
        if(getUComponent() == null) return null;
        return uComponent/scale;
    }


    public void setUComponent(Integer uComponent) {
        this.uComponent = new Double(uComponent);
    }
    
    public void setUComponent(Double uComponent) {
        this.uComponent = uComponent;
    }

    public Double getXComponent() {
//        if(xComponent == 999999 || xComponent == 99999.0 || xComponent == null) return null;
          if (isMissing(xComponent) || xComponent==null) return null;
        return xComponent;
    }
    public Double getXComponentScaled(Double scale) {
        if(getXComponent()==null) return null;
        return xComponent/scale;
    }


    public void setXComponent(Integer xComponent) {
        this.xComponent = new Double(xComponent);
    }
    
    public void setXComponent(Double xComponent) {
        this.xComponent = xComponent;
    }

    public Double getIComponent() {
//        if(iComponent == 999999 || iComponent == 99999.0 || iComponent == null) return null;
    if (isMissing(iComponent) || iComponent==null) return null;
          return iComponent;
    }
         public Double getIComponentScaled(Double scale) {
        if(getIComponent() == null) return null;
        return iComponent/scale;
    }


    public void setIComponent(Integer iComponent) {
        this.iComponent = new Double(iComponent);
    }

    public void setIComponent(Double iComponent) {
        this.iComponent = iComponent;
    }

    public Double getVComponent() {
//        if(vComponent == 999999 || vComponent == 99999.0 || vComponent == null) return null;
          if (isMissing(vComponent) || vComponent==null) return null;
          return vComponent;
    }
     public Double getVComponentScaled(Double scale) {
        if(getVComponent() == null) return null;
        return vComponent/scale;
    }

    public void setVComponent(Integer vComponent) {
        this.vComponent = new Double(vComponent);
    }
    public void setVComponent(Double vComponent) {
        this.vComponent = vComponent;
    }

    public Double getYComponent() {
//        if(yComponent == 999999 || yComponent == 99999.0 || yComponent == null) return null;
          if (isMissing(yComponent) || yComponent==null) return null;
          return yComponent;
    }
    public Double getYComponentScaled(Double scale) {
        if(getYComponent() == null) return null;
        return yComponent/scale;
    }


    public void setYComponent(Integer yComponent) {
        this.yComponent = new Double(yComponent);
    }

    public void setYComponent(Double yComponent) {
        this.yComponent = yComponent;
    }

    public Double getZComponent() {
//        if(zComponent == 999999 || zComponent == 99999.0 || zComponent == null) return null;
          if (isMissing(zComponent) || zComponent==null) return null;
          return zComponent;
    }
     public Double getZComponentScaled(Double scale) {
        if(getZComponent() == null) return null;
        return zComponent/scale;
    }

    public void setZComponent(Integer zComponent) {
        this.zComponent = new Double(zComponent);
    }
    public void setZComponent(Double zComponent) {
        this.zComponent = zComponent;
    }

    public Double getFComponent() {
//        if(fComponent == 999999 || fComponent == 99999.0 || fComponent == null) return null;
          if (isMissing(fComponent) || fComponent==null) return null;
          return fComponent;
    }
    
     public Double getFComponentScaled(Double scale) {
        if(getFComponent() == null) return null;
        return fComponent/scale;
    }

    public void setFComponent(Integer fComponent) {
        this.fComponent = new Double(fComponent);
    }
    
    public void setFComponent(Double fComponent) {
        this.fComponent = fComponent;
    }
    
    public Double getSComponent() {
//        if(sComponent == 88888.00 || sComponent == null) return null;
          if (isMissing(sComponent) || sComponent==null) return null;
          return sComponent;
    }

     public Double getSComponentScaled(Double scale) {
        if(getSComponent() == null) return null;
        return sComponent/scale;
    }

    public void setSComponent(Integer sComponent) {
        this.sComponent = new Double(sComponent);
    }

    public void setSComponent(Double sComponent) {
//        if(!(sComponent==888.0 || sComponent==88888.0))
         this.sComponent = sComponent;
    }

    public Double getComponentValue(char comp){
        switch (comp){
            case('H'):
             return getHComponent();
            case('D'):
             return getDComponent();
            case('U'):
             return getUComponent();
            case('X'):
             return getXComponent();
            case('I'):
             return getIComponent();
            case('V'):
             return getVComponent();
            case('Y'):
             return getYComponent();
            case('Z'):
             return getZComponent();
            case('F'):
             return getFComponent();
            case('s'):
             return getSComponent();
            default:
             return null;
        }
        
    }

    public Double getComponentValueScaled(char comp, Double scale){
        switch (comp){
            case('H'):
             return getHComponentScaled(scale);
            case('D'):
             return getDComponentScaled(scale);
            case('U'):
             return getUComponentScaled(scale);
            case('X'):
             return getXComponentScaled(scale);
            case('I'):
             return getIComponentScaled(scale);
            case('V'):
             return getVComponentScaled(scale);
            case('Y'):
             return getYComponentScaled(scale);
            case('Z'):
             return getZComponentScaled(scale);
            case('F'):
             return getFComponentScaled(scale);
            default:
             return null;
        }
        
    }
    
    public void setComponent(char comp, Integer value){
        switch (comp){
            case('H'):
             setHComponent(value);
             break;
            case('D'):
             setDComponent(value);
             break;
            case('U'):
             setUComponent(value);
             break;
            case('X'):
             setXComponent(value);
             break;
            case('I'):
             setIComponent(value);
             break;
            case('V'):
             setVComponent(value);
             break;
            case('Y'):
             setYComponent(value);
             break;
            case('Z'):
             setZComponent(value);
             break;
            case('F'):
             setFComponent(value);
             break;
            default:
             break;
        }
        
    }
    
// takes a double value and compares it to the standard values used as missing values
// returns true if the value is a missing one
// missing values are 999999 (1.11), 99999.00, and 88888.00 (2.00)
protected boolean isMissing(Double compVal){
    if(compVal == null) return true;
    if(compVal> 999998.9999 && compVal<999999.00001) return true;
    if(compVal> 99998.9999 && compVal<99999.00001) return true;
    if(compVal> 88887.9999 && compVal<88888.00001) return true;
    
    return false;
}
}



