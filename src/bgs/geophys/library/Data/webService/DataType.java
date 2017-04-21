/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Data.webService;

/**
 * Represents data quality type.
 *
 * @author sani
 */
public enum DataType {

    /**
     * DataType enum literals.
     */
    PROVISIONAL("provisional", "p"),
    DEFINITIVE("Definitive", "d"),
    PRELIMINARY("preliminary", "p"),
    REPORTED("reported", "r"),
    ADJUSTED("adjusted","a"),
    VARIATION("Variation", "v");

    private final String typeString;
    private final String code;
    private DataType(String typeString, String code){
        this.typeString = typeString;
        this.code = code;
    }

    /**
     * Returns the a string representation fo the data quality.
     * @return Data quality type string.
     */
    public String typeString() {
        return typeString;
    }

    /**
     * Returns the code for the data quality type.
     * @return Data quality code.
     */
    public String code() {
        return code;
    }

}
