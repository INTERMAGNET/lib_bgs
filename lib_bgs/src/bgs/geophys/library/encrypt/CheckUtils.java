/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.encrypt;

/**
 *
 * @author jex
 */

// see FileCheckUtils for ussage
class CheckUtils {

    protected static byte[] getBkey() {

 
   byte[] bkey = new byte[8];
      float f = (float) 0.174;
      String kk = String.format("%d", Math.round(f*2000));
      Integer g = 74;
      float ff = (float) 7.67;
      String k = kk.concat(String.format("%d", g+1));
      k = k.concat(String.format("%d", Math.round((ff+0.02)*100)));
    for(int i=0;i<8;i++){
        bkey[i] = (byte)k.charAt(i);
    }
    return bkey;
  }
    }
    
