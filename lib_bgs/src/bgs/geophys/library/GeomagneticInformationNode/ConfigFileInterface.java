/*
 * ConfigFileInterface.java
 *
 * Created on 01 November 2006, 15:22
 */

package bgs.geophys.library.GeomagneticInformationNode;

/**
 * an interface that (when implemented) holds a configuration file
 *
 * @author  smf
 */
public interface ConfigFileInterface 
{
        /** get the number of fields in this configuration file
         * @return the number of fields */
        public int getNFields ();
        
        /** set a field in this configuration file
         * @param row the row number for this field - in the implementation
         *        new rows should be allocated as required - ConfigFileReader
         *        guarantees that the first row will be 0, subsequent calls
         *        to this method will use the same row as the previous
         *        call or increment the row number by 1
         * @param column the column for this field (0..n_fields-1)
         * @param contents a string representation of the field (as read
         *        from the file) which the method should attempt to convert to
         *        the data type it expects
         * @param file_line_number needed when costructions ConfigFileExceptions
         * @throws ConfigFileException if row or column is invalid or the
         *         contents cannot be converted to the appropriate data type */
        public void setField (int row, int column, String contents, int file_line_number) 
        throws ConfigFileException;
}
