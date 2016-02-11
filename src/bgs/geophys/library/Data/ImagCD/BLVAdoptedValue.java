/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;

import java.io.DataInputStream;
import java.util.StringTokenizer;

/**
 *
 * @author jex
 */


public class BLVAdoptedValue extends BLVObservedValue{
    private Double deltaF= null;
//    private Double deltaFDouble;

//public void readBLVAdoptedValue(char[] buf, String componentOrder){
//    this.readBLV(buf, componentOrder);
//        setDeltaF((Integer) Integer.parseInt(String.copyValueOf(buf,28,5).replace("+"," ").trim()));
//}

public void readBLVAdoptedValue(String buf, String componentOrder, String format) throws BLVException{
      // Create a data input stream
//     StringReader inbuf = new StringReader(buf);
    this.readBLV(buf.toCharArray(), componentOrder, format);

    StringTokenizer st = new StringTokenizer(buf); 
     // get 5th item for delta F in old format, 6th in IBFV2.00
    int pos;
    Integer idF;
    Double dF;
    if (format.equalsIgnoreCase("IBFV1.11")) pos = 4;
    else pos = 5;
    for(int i=0;i<pos;i++){
      try{
        st.nextToken();
      }
      catch(Exception e){
        // just skip the first 4 or 5
      }
    }
    try{
    if (format.equalsIgnoreCase("IBFV1.11")){
      idF = (Integer.parseInt(st.nextToken().replace("+"," ").trim()));
      dF = new Double(idF);
      }
      else{
       dF = Double.parseDouble(st.nextToken());
      }
    setDeltaF(dF);
    }
    
    catch(Exception e){ 
     setDeltaF((Double) null);
    }
}

public void printBLVAdoptedValue() {
    this.printBLV();
    System.out.println("Fourth component: "+ getDeltaF());
}

    public Double getDeltaF() {
//        if(deltaF == 9999 || deltaF == null || deltaF == 888.00
//           ||deltaF == 999.00||deltaF==99999 || deltaF==999999) return null;
        if(isFMissing(deltaF) || deltaF == null) return null;
        return deltaF;
    }
   

    private boolean isFMissing(Double val){
        if(super.isMissing(val)) return true;
        if(val>9998.9999 && val<9999.00001) return true;
        if(val>998.99999 && val<999.00001) return true;
        if(val>887.99999 && val<888.00001) return true;
        return false;
    }

    public Double getDeltaFScaled(Double scale) {
        if(getDeltaF() == null) return null;
        return deltaF/scale;
    }

    public void setDeltaF(Integer deltaF) {
        this.deltaF = new Double(deltaF);
    }
    
    public void setDeltaF(Double deltaF) {
        this.deltaF = deltaF;
    }
    
    @Override
    public Double getComponentValue(char comp){
        if(comp=='f') return getDeltaF();
        else return super.getComponentValue(comp);
    }
    
    public BLVAdoptedValue difference(BLVAdoptedValue avLast){
//        this.printBLVAdoptedValue();
//        avLast.printBLVAdoptedValue();
        BLVAdoptedValue diff = new BLVAdoptedValue();
        
        if(this.getFComponent()!=null &&avLast.getFComponent()!=null) diff.setFComponent(this.getFComponent()-avLast.getFComponent());
        if(this.getHComponent()!=null &&avLast.getHComponent()!=null) diff.setHComponent(this.getHComponent()-avLast.getHComponent());
        if(this.getDComponent()!=null &&avLast.getDComponent()!=null) diff.setDComponent(this.getDComponent()-avLast.getDComponent());
        if(this.getUComponent()!=null &&avLast.getUComponent()!=null)diff.setUComponent(this.getUComponent()-avLast.getUComponent());
        if(this.getXComponent()!=null &&avLast.getXComponent()!=null)diff.setXComponent(this.getXComponent()-avLast.getXComponent());
        if(this.getIComponent()!=null &&avLast.getIComponent()!=null)diff.setIComponent(this.getIComponent()-avLast.getIComponent());
        if(this.getSComponent() !=null &&avLast.getSComponent()!=null)diff.setSComponent(this.getSComponent()-avLast.getSComponent());
        if(this.getVComponent()!=null &&avLast.getVComponent()!=null)diff.setVComponent(this.getVComponent()-avLast.getVComponent());
        if(this.getYComponent()!=null &&avLast.getYComponent()!=null) diff.setYComponent(this.getYComponent()-avLast.getYComponent());
        if(this.getZComponent()!=null &&avLast.getZComponent()!=null)diff.setZComponent(this.getZComponent()-avLast.getZComponent());
        if(this.getDeltaF()!=null &&avLast.getDeltaF()!=null) diff.setDeltaF(this.getDeltaF() - avLast.getDeltaF());
        return diff;
    }
}

