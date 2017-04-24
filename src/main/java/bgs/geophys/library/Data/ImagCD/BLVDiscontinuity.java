/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;

/**
 *
 * @author jex
 */
public class BLVDiscontinuity {
  private Integer index; // position of discontinuity
  private BLVAdoptedValue stepValues; //difference between the points either side

    BLVDiscontinuity(Integer day, BLVAdoptedValue step) {
        setIndex(day);
        setStepValue(step);
    }
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public BLVAdoptedValue getStepValue() {
        return stepValues;
    }

    public String getStepValueString(char comp) {
//        System.out.println("getting step value...: "+stepValues.getComponentValue(comp));
        if(stepValues.getComponentValue(comp)==null) return "no step data";
        return String.format("step %.2f", stepValues.getComponentValue(comp));
         
    }
    public void setStepValue(BLVAdoptedValue stepValue) {
        this.stepValues = stepValue;
    }
                    
}
