/**************************************************************************
*
* A Base64 Encoder/Decoder.
*
* This class is used to encode and decode data in Base64 format
* as described in RFC 1521.
*
* <p>
* Copyright 2003: Christian d'Heureuse, Inventec Informatik AG, Switzerland.<br>
* License: This is "Open Source" software and released under the <a href="http://www.gnu.org/licenses/lgpl.html" target="_top">GNU/LGPL</a> license.
* It is provided "as is" without warranty of any kind. Please contact the author for other licensing arrangements.<br>
* Home page: <a href="http://www.source-code.biz" target="_top">www.source-code.biz</a><br>
*
* <p>
* Version history:<br>
* 2003-07-22 Christian d'Heureuse (chdh): Module created.<br>
* 2005-08-11 chdh: Lincense changed from GPL to LGPL.
* 2006-02-28 smf: converted base64 encoded output arrays from char to byte
*
**************************************************************************/

package bgs.geophys.library.Crypto;

public class Base64 
{

    // Mapping table from 6-bit nibbles to Base64 characters.
    private static byte[] map1 = new byte[64];

    static 
    {
        int i=0;
        char c;
        
        for (c='A'; c<='Z'; c++) map1[i++] = (byte) c;
        for (c='a'; c<='z'; c++) map1[i++] = (byte) c;
        for (c='0'; c<='9'; c++) map1[i++] = (byte) c;
        map1[i++] = '+'; map1[i++] = '/'; 
    }

    // Mapping table from Base64 characters to 6-bit nibbles.
    private static byte[] map2 = new byte[128];

    static 
    {
        int i;
        
        for (i=0; i<map2.length; i++) map2[i] = -1;
        for (i=0; i<64; i++) map2[map1[i]] = (byte)i; 
    }

    /**
    * Encodes a string into Base64 format.
    * No blanks or line breaks are inserted.
    * @param s  a String to be encoded.
    * @return   A String with the Base64 encoded data.
    */
    public static String encode (String s) 
    {
        return new String(encode(s.getBytes())); 
    }

    /**
    * Encodes a byte array into Base64 format.
    * No blanks or line breaks are inserted.
    * @param in  an array containing the data bytes to be encoded.
    * @return    A character array with the Base64 encoded data.
    */
    public static byte[] encode (byte[] in) 
    {
        int i0, i1, i2, o0, o1, o2, o3;
        int iLen = in.length;
        int oDataLen = (iLen*4+2)/3;       // output length without padding
        int oLen = ((iLen+2)/3)*4;         // output length including padding
        byte[] out = new byte[oLen];
        int ip = 0;
        int op = 0;

        while (ip < iLen) 
        {
            i0 = in[ip++] & 0xff;
            i1 = ip < iLen ? in[ip++] & 0xff : 0;
            i2 = ip < iLen ? in[ip++] & 0xff : 0;
            o0 = i0 >>> 2;
            o1 = ((i0 &   3) << 4) | (i1 >>> 4);
            o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
            o3 = i2 & 0x3F;
            out[op++] = map1[o0];
            out[op++] = map1[o1];
            out[op] = op < oDataLen ? map1[o2] : (byte) '='; op++;
            out[op] = op < oDataLen ? map1[o3] : (byte) '='; op++; 
        }
        return out; 
    }

    /**
    * Decodes a Base64 string.
    * @param s  a Base64 String to be decoded.
    * @return   A String containing the decoded data.
    * @throws   IllegalArgumentException if the input is not valid Base64 encoded data.
    */
    public static String decode (String s) 
    {
        return new String(decode(s.getBytes())); 
    }

    /**
     * Decodes Base64 data.
     * No blanks or line breaks are allowed within the Base64 encoded data.
     * @param in  a character array containing the Base64 encoded data.
     * @return    An array containing the decoded data bytes.
     * @throws    IllegalArgumentException if the input is not valid Base64 encoded data.
     */
    public static byte[] decode (byte[] in) 
    {
        int i0, i1, i2, i3, b0, b1, b2, b3, o0, o1, o2;
        int iLen = in.length;
        if (iLen%4 != 0) throw new IllegalArgumentException ("Length of Base64 encoded input string is not a multiple of 4.");
        while (iLen > 0 && in[iLen-1] == '=') iLen--;
        int oLen = (iLen*3) / 4;
        byte[] out = new byte[oLen];
        int ip = 0;
        int op = 0;
        
        while (ip < iLen) 
        {
            i0 = in[ip++];
            i1 = in[ip++];
            i2 = ip < iLen ? in[ip++] : 'A';
            i3 = ip < iLen ? in[ip++] : 'A';
            if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127)
                throw new IllegalArgumentException ("Illegal character in Base64 encoded data.");
            b0 = map2[i0];
            b1 = map2[i1];
            b2 = map2[i2];
            b3 = map2[i3];
            if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0)
                throw new IllegalArgumentException ("Illegal character in Base64 encoded data.");
            o0 = ( b0       <<2) | (b1>>>4);
            o1 = ((b1 & 0xf)<<4) | (b2>>>2);
            o2 = ((b2 &   3)<<6) |  b3;
            out[op++] = (byte)o0;
            if (op<oLen) out[op++] = (byte)o1;
            if (op<oLen) out[op++] = (byte)o2; 
        }
        return out; 
    }

}

