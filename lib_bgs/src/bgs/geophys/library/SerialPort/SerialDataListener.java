/*
 * SerialDataListener.java
 *
 * Created on 07 June 2004, 13:18
 */

package bgs.geophys.library.SerialPort;

/**
 * An interface that defines how serial data is logged
 * @author  Administrator
 */
public interface SerialDataListener 
{

    /** codes for data type */
    public static int OPEN_PORT = 0, RX_DATA = 1, TX_DATA = 2, FLUSH_DATA = 3, CLOSE_PORT = 4;
    
    /** receive information about serial data
     * @param dev_name the name of the device attached to the serial port
     * @param prot_name the serial port name
     * @param data_type one of the codes above for data type (Tx, Rx or Flush)
     * @param data the serial data */
    public void serialDataEvent (String dev_name, String port_name, int data_type, String data);

}
