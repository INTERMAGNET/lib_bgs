/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.ImagCD;

/**
 *
 * @author jex
 */


/** an exception used when there is an error with the BLV */
public class BLVException extends Exception
{

  public BLVException () { super (); }
  public BLVException (String msg) { super ( msg);}
  public BLVException (Throwable e) { super ( e); }
  public BLVException (String msg, Throwable e) { super (msg, e); }

}


