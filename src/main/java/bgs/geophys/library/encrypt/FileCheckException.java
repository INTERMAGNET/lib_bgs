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

public class FileCheckException extends Exception {

  public FileCheckException () { super (); }
  public FileCheckException (String msg) { super ( msg);}
  public FileCheckException (Throwable e) { super ( e); }
  public FileCheckException (String msg, Throwable e) { super (msg, e); }

}
