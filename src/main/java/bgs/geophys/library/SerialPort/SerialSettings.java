/*
 * SerialSettings.java
 *
 * Created on 06 June 2004, 21:55
 */

package bgs.geophys.library.SerialPort;

/**
 * Mainly just a place to hold serial settings in a group
 * @author  Administrator
 */
public class SerialSettings 
{
    
        public String port_name;    // e.g. "COM1", "/dev/term/a"
        public int baud;            // standard rates
        public int data_bits;       // 5,6,7,8
        public int stop_bits;       // 1,2
        public String parity_code;  // none,even,odd,mark,space
        public int in_mask;         // mask which is ANDed with incoming data
        public int out_mask;        // mask which is ANDed with outgoing data
        public int in_buffer_size;
        public int out_buffer_size;
        
        /** construct a SerialSettings with default values */
        public SerialSettings ()
        {
            this.port_name = "COM1";
            this.baud = 9600;
            this.data_bits = 8;
            this.stop_bits = 1;
            this.parity_code = "none";
            this.in_mask = 0xff;
            this.out_mask = 0xff;
            this.in_buffer_size = 1024;
            this.out_buffer_size = 1024;
        }
        
        /** construct a SerialSettings with given values */
        public SerialSettings (String port_name,
                               int baud, int data_bits, int stop_bits,
                               String parity_code, int in_buffer_size, int out_buffer_size)
        {
            this.port_name = new String (port_name);
            this.baud = baud;
            this.data_bits = data_bits;
            this.stop_bits = stop_bits;
            this.parity_code = parity_code;
            this.in_mask = 0xff;
            this.out_mask = 0xff;
            this.in_buffer_size = in_buffer_size;
            this.out_buffer_size = out_buffer_size;
        }
        
        /** construct a SerialSettings with given values */
        public SerialSettings (String port_name,
                               int baud, int data_bits, int stop_bits,
                               String parity_code, int in_buffer_size, int out_buffer_size,
                               int in_mask, int out_mask)
        {
            this.port_name = new String (port_name);
            this.baud = baud;
            this.data_bits = data_bits;
            this.stop_bits = stop_bits;
            this.parity_code = parity_code;
            this.in_mask = in_mask;
            this.out_mask = out_mask;
            this.in_buffer_size = in_buffer_size;
            this.out_buffer_size = out_buffer_size;
        }
}
