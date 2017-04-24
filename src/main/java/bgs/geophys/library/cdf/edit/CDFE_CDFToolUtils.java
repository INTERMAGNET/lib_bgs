package bgs.geophys.library.cdf.edit;

/******************************************************************************
* Copyright 1996-2014 United States Government as represented by the
* Administrator of the National Aeronautics and Space Administration.
* All Rights Reserved.
******************************************************************************/

import java.text.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.lang.reflect.*;
import gsfc.nssdc.cdf.*;
import gsfc.nssdc.cdf.util.*;

//
//  This class provides several common utility methods for CDF Tool classes
//

public class CDFE_CDFToolUtils implements CDFConstants {

/**
 *  This method parses a string and returns an object of an array of primitive type 
 *  based upon the data type
 */

    public static Object parseContents (String data, long dataType) 
					throws NumberFormatException {

	data = data.trim();
	Vector vec = new Vector();
        StringTokenizer st = new StringTokenizer(data, ",");
	if (st.countTokens() == -1) {
	  return null;
	}
	  
        while (st.hasMoreTokens()) {
	  vec.addElement(st.nextToken());
        }
	int filterElementsNum = vec.size();
        if (dataType == CDF_CHAR || dataType == CDF_UCHAR) {
	    // move in the data as is
	    String aStr = data;
            return aStr;
	} else if (dataType == CDF_BYTE || dataType == CDF_INT1) {
          if (filterElementsNum == 1) {
	    byte[] aByte = {Byte.parseByte(data)};
	    return aByte;
          } else {
            byte[] aByte = new byte[filterElementsNum];
            for (int i = 0; i < filterElementsNum; i++) 
              aByte[i] = Byte.parseByte(((String)(vec.elementAt(i))).trim());
	    return aByte;
          }
	} else if (dataType == CDF_UINT1 || dataType == CDF_INT2) {
          if (filterElementsNum == 1) {
	    short[] aShort = {Short.parseShort(data)};
	    if (dataType == CDF_UINT1) {
	      if (aShort[0] < 0 || aShort[0] > 255) {
		aShort = null;
		throw new NumberFormatException(data);
	      }
	    }		
	    return aShort;
          } else {
            short[] aShort = new short[filterElementsNum];
            for (int i = 0; i < filterElementsNum; i++) {
              aShort[i] = Short.parseShort(((String)(vec.elementAt(i))).trim());
              if (dataType == CDF_UINT1) {
                if (aShort[i] < 0 || aShort[i] > 255) {
		  aShort = null;
		  throw new NumberFormatException(((String)(vec.elementAt(i))).trim());
		}
              }
	    }
	    return aShort;
          }
	} else if (dataType == CDF_UINT2 || dataType == CDF_INT4) {
	  if (filterElementsNum == 1) {
	    int[] aInteger = {Integer.parseInt(data)};
            if (dataType == CDF_UINT2) {
              if (aInteger[0] < 0 || aInteger[0] > 65535) {
		aInteger = null;
		throw new NumberFormatException(data);
	      }
            }
	    return aInteger;
	  } else {
	    int[] aInteger = new int[filterElementsNum];
	    for (int i = 0; i < filterElementsNum; i++) {
	      aInteger[i] = Integer.parseInt(((String)(vec.elementAt(i))).trim());
              if (dataType == CDF_UINT2) {
                if (aInteger[i] < 0 || aInteger[i] > 65535) {
		  aInteger = null;
		  throw new NumberFormatException(((String)(vec.elementAt(i))).trim());
		}
              }
	    }
	    return aInteger;
	  }
	} else if (dataType == CDF_UINT4 || dataType == CDF_INT8) {
          if (filterElementsNum == 1) {
	    long[] aLong = {Long.parseLong(data)};
            if (dataType == CDF_UINT4) {
              if (aLong[0] < 0 || aLong[0] > 4294967285L) {
		aLong = null;
		throw new NumberFormatException(data);
	      }
            }

	    return aLong;
          } else {
            long[] aLong = new long[filterElementsNum];
            for (int i = 0; i < filterElementsNum; i++) {
              aLong[i] = Long.parseLong(((String)(vec.elementAt(i))).trim());
              if (dataType == CDF_UINT4) {
                if (aLong[i] < 0 || aLong[i] > 4294967285L) {
		  aLong = null;
		  throw new NumberFormatException(((String)(vec.elementAt(i))).trim());
		}
              }
	    }
	    return aLong;
          }
	} else if (dataType == CDF_FLOAT || dataType == CDF_REAL4) {
          if (filterElementsNum == 1) {
	    float[] aFloat = {Float.valueOf(data).floatValue()};
	    return aFloat;
          } else {
	    float[] aFloat = new float[filterElementsNum];
            for (int i = 0; i < filterElementsNum; i++) 
              aFloat[i] = Float.valueOf(((String)(vec.elementAt(i))).trim()).floatValue();
	    return aFloat;
          }
	} else if (dataType == CDF_REAL8 || dataType == CDF_DOUBLE) {
          if (filterElementsNum == 1) {
	    double[] aDouble = {Double.valueOf(data).doubleValue()};
	    return aDouble;
          } else {
            double[] aDouble = new double[filterElementsNum];
            for (int i = 0; i < filterElementsNum; i++) 
              aDouble[i] = Double.valueOf(((String)(vec.elementAt(i))).trim()).doubleValue();
	    return aDouble;
          }
	} else if (dataType == CDF_EPOCH) {
	  try {
            if (filterElementsNum == 1) {
	      double[] aDouble = new double[1];
	      if (data.indexOf(":") == -1) // not an EPOCH string
                aDouble[0] = Double.valueOf(data).doubleValue();
	      else
	        aDouble[0] = Epoch.parse(data);
              return aDouble;
            } else {
              double[] aDouble = new double[filterElementsNum];
              for (int i = 0; i < filterElementsNum; i++) {
	        String ttt = ((String) vec.elementAt(i)).trim();
	        if (ttt.indexOf(":") == -1)
                  aDouble[i] = Double.valueOf(ttt).doubleValue();
	        else
	          aDouble[i] = Epoch.parse(ttt);
	      }	
              return aDouble;
            }
	  } catch (CDFException e) {
            System.err.println("error parsing Epoch ");
            return null;
	  }           
        } else if (dataType == CDF_EPOCH16) {
          try {
            if (filterElementsNum == 1) {
              double[] aDouble = new double[2];
              if (data.indexOf(":") == -1) { // not an EPOCH string
                aDouble[0] = Double.valueOf(data).doubleValue();
		aDouble[1] = 0.0;
	      }
              else
                aDouble = (double[]) Epoch16.parse((String)data);
              return aDouble;
            } else {
              double[][] aDouble = new double[filterElementsNum][2];
	      double[] aaa = new double[2];
              for (int i = 0; i < filterElementsNum; i++) {
                String ttt = ((String) vec.elementAt(i)).trim();
                if (ttt.indexOf(":") == -1) {
                  aDouble[i][0] = Double.valueOf(ttt).doubleValue();
		  aDouble[i][1] = 0.0;
		}
                else {
		  aaa = (double[]) Epoch16.parse(ttt);
                  aDouble[i][0] = aaa[0];
		  aDouble[i][1] = aaa[1];
		}
              }
              return aDouble;
            }
          } catch (CDFException f) {
            System.err.println("error parsing Epoch ");
            return null;
          }           
        } else if (dataType == CDF_TIME_TT2000) {
          try {
            if (filterElementsNum == 1) {
              long[] aLong = new long[1];
              aLong[0] = (long) CDFTT2000.fromUTCstring((String)data);
              return aLong;
            } else {
              long[] aLong = new long[filterElementsNum];
              for (int i = 0; i < filterElementsNum; i++) {
                String ttt = ((String) vec.elementAt(i)).trim();
		aLong[i] = (long) CDFTT2000.fromUTCstring(ttt);
              }
              return aLong;
            }
          } catch (CDFException g) { 
            System.err.println("error parsing TT2000 string ");
            return null;
	  }
	} 
	return null;
    }

/**
 *  This method parses a string and returns an object based upon the data type
 */

    public static Object parseContent (String data, long dataType, long numElems) throws CDFException, NumberFormatException {

        data = data.trim();
        if (dataType == CDF_CHAR || dataType == CDF_UCHAR) {
            // move in the data as is
           String tmpString;
           if (data.substring(0,1).equals("\""))
	     if (data.substring(data.length()-1).equals("\""))
               tmpString = new StringBuffer(data).substring(1,data.length()-1);
	     else
               tmpString = data;
           else
             tmpString =  data;
           if (tmpString.length() > numElems) throw new CDFException(BAD_NUM_ELEMS);
           else return tmpString;
        } else 
	   return CDFE_CDFToolUtils.parseContent (data, dataType);
    }

/**
 *  This method parses a string and returns an object based upon the data type
 */

    public static Object parseContent (String data, long dataType) throws NumberFormatException {

        data = data.trim();

        if (dataType == CDF_CHAR || dataType == CDF_UCHAR) {
            // move in the data as is
	   String tmpString;
	   if (data.substring(0,1).equals("\"")) 
	     if (data.substring(data.length()-1).equals("\""))
	       tmpString = new StringBuffer(data).substring(1,data.length()-1);
	     else
	       tmpString = data;
	   else
	     tmpString =  data;
	   return tmpString;
        } else if (dataType == CDF_BYTE || dataType == CDF_INT1) {
            Byte aByte = new Byte(data);
            return aByte;
        } else if (dataType == CDF_UINT1 || dataType == CDF_INT2) {
            Short aShort = new Short(data);
            if (dataType == CDF_UINT1) {
              if (aShort.shortValue() < 0 || aShort.shortValue() > 255) {
		aShort = null;
		throw new NumberFormatException(data);
	      }
            }           
            return aShort;
        } else if (dataType == CDF_UINT2 || dataType == CDF_INT4) {
            Integer aInteger = new Integer(data);
            if (dataType == CDF_UINT2) {
              if (aInteger.shortValue() < 0 || aInteger.intValue() > 65535) {
		aInteger = null;
		throw new NumberFormatException(data);
	      }
            }           
            return aInteger;
        } else if (dataType == CDF_UINT4 || dataType == CDF_INT8) {
            Long aLong = new Long(data);
            if (dataType == CDF_UINT4) {
              if (aLong.shortValue() < 0 || aLong.longValue() > 4294967285L) {
		aLong = null;
		throw new NumberFormatException(data);
	      }
            }           
            return aLong;
        } else if (dataType == CDF_FLOAT || dataType == CDF_REAL4) {
            Float aFloat = new Float(data);
            return aFloat;
        } else if (dataType == CDF_REAL8 || dataType == CDF_DOUBLE) {
            Double aDouble = new Double(data);
            return aDouble;
        } else if (dataType == CDF_EPOCH) {
	  try {
	    Double aDouble = new Double(Epoch.parse(data));
            return aDouble;
          } catch (CDFException e) {
            System.err.println("error parsing epoch ");
            return null;
          }
        } else if (dataType == CDF_EPOCH16) {
          try {
            double[] aDouble = (double[]) Epoch16.parse(data);
            return aDouble;
          } catch (CDFException e) {
            System.err.println("error parsing epoch16 ");
            return null;
          }
        } else if (dataType == CDF_TIME_TT2000) {
	  try {
	    Long aLong = new Long(CDFTT2000.fromUTCstring(data));
            return aLong;
          } catch (CDFException e) {
            System.err.println("error parsing TT2000 ");
            return null;
          }
        }
        return null;
    }

/**
  *  Breakdown the value(s) of the given data.  Data can be a java primitive
  *  data type, Java Object (non-array), or 1-dimensional array of
  *  primitive Java data type.
  *
  */
    public static String breakdownData (Object data) {
	return breakdownData (data, 0);
    }

/**
  *  Breakdown the value(s) of the given data.  Data can be a java primitive
  *  data type, Java Object (non-array), or 1-dimensional array of
  *  primitive Java data type.
  *  Valid values for argument which:
  *  1 - CDF_EPOCH type
  *  2 - CDF_EPOCH16 type
  *  3 - CDF_TIME_TT2000 type
  *  0 - all others
  *
  */
    public static String breakdownData (Object data, int which) {

        int  i, arrayLength;
        String signature = CDFUtils.getSignature(data);
	StringBuffer temp = new StringBuffer();
	int jj = 0;
	if (which == 2) jj = 2;

        if (signature.charAt(0) == '[') {
            arrayLength = Array.getLength(data);
            for (i=0; i < arrayLength; i=i+jj) {
                 if (i > 0) temp.append(",");
                 if (signature.charAt(1) == 'B')
                     temp.append(Array.getByte(data,i));

                 else if (signature.charAt(1) == 'S')
                     temp.append(Array.getShort(data,i));

                 else if (signature.charAt(1) == 'I')
                     temp.append(Array.getInt(data,i));

                 else if (signature.charAt(1) == 'J')
                     if (which == 0)
                       temp.append(Array.getLong(data,i));
                     else
                       temp.append(CDFTT2000.toUTCstring(Array.getLong(data,i)));

                 else if (signature.charAt(1) == 'F')
                     temp.append(Array.getFloat(data,i));

                 else if (signature.charAt(1) == 'D') {
		     if (which == 1)
		       temp.append(Epoch.encode(Array.getDouble(data,i)));
		     else if (which == 2) {
		       double[] mmm = new double[2];
		       mmm[0] = Array.getDouble(data,2*i);
		       mmm[1] = Array.getDouble(data,2*i+1);
		       temp.append(Epoch16.encode(mmm));
		     } else
                       temp.append(Array.getDouble(data,i));
		}
            }
	    temp.setLength(temp.length() - 1);
        } else {
	  if (which == 1)
            temp.append(Epoch.encode(((Double)data).doubleValue()));
	  else if (which == 2) 
	    temp.append(Epoch16.encode((double[])data));
	  else if (which == 3)
            temp.append(CDFTT2000.toUTCstring(((Long)data).longValue()));
          else
	    temp.append(data);
	}
        return temp.toString();
    }

/**
  *  Return an object representing the value in an array.  Data must be a 
  *  java primitive data type or string in a 1-dimensional array.
  */
    public static Object retrieveData (Object data, int index) {
	return retrieveData (data, index, 0);
    }

/**
  *  Return an object representing the value in an array.  Data must be a
  *  java primitive data type or string in a 1-dimensional array.
  */
    public static Object retrieveData (Object data, int index, int which) {

        String signature = CDFUtils.getSignature(data);

        if (signature.charAt(0) == '[') {
          if (signature.charAt(1) == 'B')
              return new Byte(Array.getByte(data,index));

          else if (signature.charAt(1) == 'Z')
              return new Boolean(Array.getBoolean(data,index));

          else if (signature.charAt(1) == 'C')
              return new Character(Array.getChar(data,index));

          else if (signature.charAt(1) == 'S')
              return new Short(Array.getShort(data,index));

          else if (signature.charAt(1) == 'I')
              return new Integer(Array.getInt(data,index));

          else if (signature.charAt(1) == 'J')
              return new Long(Array.getLong(data,index));

          else if (signature.charAt(1) == 'F')
              return new Float(Array.getFloat(data,index));

          else if (signature.charAt(1) == 'D') {
	      if (which == 2) {
		double[] mmm = new double[2];
		mmm[0] = Array.getDouble(data,2*index);
		mmm[1] = Array.getDouble(data,2*index+1);
		return mmm;
	      } else
                return new Double(Array.getDouble(data,index));
	  }
          else if (signature.indexOf("String") != -1)
              return new String(((String[])data)[index]);

          else
	      return null;
        } else
          return null;
    }

/** get the format for the variable and convert it to C style if it's Fortran style.
  */

    public static String getCFormat(Variable var) {

        String format = null;
        try {
          format = var.getEntryData("FORMAT").toString();
        } catch (CDFException e) {
          format = null;
        }

        if (format != null) {
          format = format.trim();
          if (format.equals("")) format = null;
          else {
            if (!format.startsWith("%")) {
                  format = CDFE_CDFToolUtils.FtoCformat(format);
            }
          }
        }
        return format;
    }

/******************************************************************************
* FtoCformat.
* Returns an C format string if valid FORTRAN format specification.
*         null otherwise.
******************************************************************************/

public static String FtoCformat (String fFormat) {
  StringBuffer cFormat = new StringBuffer();
  int nFound;
  /****************************************************************************
  * Encode C format specification.
  ****************************************************************************/
  switch (fFormat.charAt(0)) {
    /**************************************************************************
    * Integer/decimal.
    **************************************************************************/
    case 'I':
    case 'i':
      nFound = fFormat.indexOf('.'); 
      if (nFound == -1) {
	  String ww = fFormat.substring(1);
	  if (ww.length() == 0) 
	    cFormat.append("%").append("d");
	  else 
	    cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("d");
      } else {
          String xx = fFormat.substring(1, nFound);
	  String ww = null;
	  if (nFound+1 < fFormat.length()) ww = fFormat.substring(nFound+1);
          if (ww == null || xx.length() == 0) {
	    if (xx.length() == 0)
              cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("d");
	    if (ww == null)
	      cFormat.append("%").append(Integer.valueOf(xx).intValue()).append("d");
          } else 
            cFormat.append("%").append(Integer.valueOf(xx).intValue()).
    	            append(".").append(Integer.valueOf(ww).intValue()).append("d");
      }
      break;

    /**************************************************************************
    * Integer/octal.
    **************************************************************************/
    case 'O':
    case 'o':
      nFound = fFormat.indexOf('.');
      if (nFound == -1) {
          String ww = fFormat.substring(1);
          if (ww.length() == 0)
            cFormat.append("%").append("o");
          else
            cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("o");
      } else {
          String xx = fFormat.substring(1, nFound);
          String ww = null;
	  if (nFound+1 < fFormat.length()) ww = fFormat.substring(nFound+1);
          if (ww == null || xx.length() == 0) {
	    if (xx.length() == 0)
              cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("o");
            if (ww == null)
              cFormat.append("%").append(Integer.valueOf(xx).intValue()).append("o");
          } else
            cFormat.append("%").append(Integer.valueOf(xx).intValue()).
                    append(".").append(Integer.valueOf(ww).intValue()).append("o");
      }
      break;

    /**************************************************************************
    * Integer/hexadecimal.
    **************************************************************************/
    case 'Z':
    case 'z':
      nFound = fFormat.indexOf('.');
      if (nFound == -1) {
          String ww = fFormat.substring(1);
          if (ww.length() == 0)
            cFormat.append("%").append("X");
          else
            cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("X");
      } else {
          String xx = fFormat.substring(1, nFound);
          String ww = null;
          if (nFound+1 < fFormat.length()) ww = fFormat.substring(nFound+1);
          if (ww == null || xx.length() == 0) {
	    if (xx.length() == 0)
              cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("X");
            if (ww == null)
              cFormat.append("%").append(Integer.valueOf(xx).intValue()).append("X");
          } else
            cFormat.append("%").append(Integer.valueOf(xx).intValue()).
                    append(".").append(Integer.valueOf(ww).intValue()).append("X");
      }
      break;

    /**************************************************************************
    * Floating-point/non-scientific notation (which is called...
    **************************************************************************/
    case 'F':
    case 'f':
      nFound = fFormat.indexOf('.');
      if (nFound == -1) {
          String ww = fFormat.substring(1);
          if (ww.length() == 0)
            cFormat.append("%").append("f");
          else
            cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("f");
      } else {
          String xx = fFormat.substring(1, nFound);
          String ww = null;
          if (nFound+1 < fFormat.length()) ww = fFormat.substring(nFound+1); 
          if (ww == null || xx.length() == 0) {
	    if (xx.length() == 0)
              cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("f");
            if (ww == null)
              cFormat.append("%").append(Integer.valueOf(xx).intValue()).append("f");
          } else
            cFormat.append("%").append(Integer.valueOf(xx).intValue()).
                    append(".").append(Integer.valueOf(ww).intValue()).append("f");
      }
      break;

    /**************************************************************************
    * Floating-point/scientific notation.
    **************************************************************************/
    case 'E':
    case 'e':
    case 'D':
    case 'd':
      nFound = fFormat.indexOf('.');
      if (nFound == -1) {
          String ww = fFormat.substring(1);
          if (ww.length() == 0)
            cFormat.append("%").append("e");
          else
            cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("e");
      } else {
          String xx = fFormat.substring(1, nFound);
          String ww = null;
          if (nFound+1 < fFormat.length()) ww = fFormat.substring(nFound+1); 
          if (ww == null || xx.length() == 0) {
            if (xx.length() == 0)
	      cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("e");
            if (ww == null)
              cFormat.append("%").append(Integer.valueOf(xx).intValue()).append("e");
          } else
            cFormat.append("%").append(Integer.valueOf(xx).intValue()).
                    append(".").append(Integer.valueOf(ww).intValue()).append("e");
      }
      break;

    /**************************************************************************
    * Floating-point/notation depends on value.
    **************************************************************************/
    case 'G':
    case 'g':
      nFound = fFormat.indexOf('.');
      if (nFound == -1) {
          String ww = fFormat.substring(1);
          if (ww.length() == 0)
            cFormat.append("%").append("g");
          else
            cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("g");
      } else {
          String xx = fFormat.substring(1, nFound);
          String ww = null;
          if (nFound+1 < fFormat.length()) ww = fFormat.substring(nFound+1); 
          if (ww == null || xx.length() == 0) {
	    if (xx.length() == 0)
              cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("g");
            if (ww == null)
              cFormat.append("%").append(Integer.valueOf(xx).intValue()).append("g");
          } else
            cFormat.append("%").append(Integer.valueOf(xx).intValue()).
                    append(".").append(Integer.valueOf(ww).intValue()).append("g");
      }
      break;

    /**************************************************************************
    * Character.
    **************************************************************************/
    case 'A':
    case 'a':
      nFound = fFormat.indexOf('.');
      if (nFound == -1) {
          String ww = fFormat.substring(1);
          if (ww.length() == 0)
            cFormat.append("%").append("s");
          else
            cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("s");
      } else {
          String xx = fFormat.substring(1, nFound);
          String ww = null;
          if (nFound+1 < fFormat.length()) ww = fFormat.substring(nFound+1); 
          if (ww == null || xx.length() == 0) {
	    if (xx.length() == 0) 
              cFormat.append("%").append(Integer.valueOf(ww).intValue()).append("s");
            if (ww == null)
              cFormat.append("%").append(Integer.valueOf(xx).intValue()).append("s");
          } else
            cFormat.append("%").append(Integer.valueOf(xx).intValue()).
                    append(".").append(Integer.valueOf(ww).intValue()).append("s");
      }
      break;
    
    default:
      break;
  }
  if (cFormat.length() == 0) return null;
  else return cFormat.toString();
}

/******************************************************************************
* VariableWidth.
******************************************************************************/

public static int VariableWidth (int epoch, Variable var, long dataType, 
                                 String format) {
 
  int width;
  /****************************************************************************
  * Check for a string data type.  In this case the format is ignored.
  ****************************************************************************/
  if (dataType == CDF_CHAR || dataType == CDF_UCHAR) {
    int numElems = (int) var.getNumElements();
    return Math.max(10, numElems+2)+1;
  }
  /****************************************************************************
  * Check for an EPOCH data type.  Depending on the style the format may be
  * ignored.
  ****************************************************************************/
  if (dataType == CDF_EPOCH) {
    switch (epoch) {
      case 0: // EPOCH0_STYLE
        return 24;
      case 1: // EPOCH1_STYLE
        return 16;
      case 2: // EPOCH2_STYLE
        return 14;
      case 3: // EPOCH3_STYLE
        return 24;
      case 4: // EPOCHf_STYLE
        return 50;
      case 5: // EPOCHx_STYLE
        return 68;
      case 6: // EPOCH4_STYLE
        return 23;
    }
  }
  /****************************************************************************
  * Check for an EPOCH16 data type.  Depending on the style the format may be
  * ignored.
  ****************************************************************************/
  else if (dataType == CDF_EPOCH16) {
    switch (epoch) {
      case 0: // EPOCH0_STYLE_EXTEND
        return 36;
      case 1: // EPOCH1_STYLE_EXTEND
        return 24;
      case 2: // EPOCH2_STYLE_EXTEND
        return 14;
      case 3: // EPOCH3_STYLE_EXTEND
        return 36;
      case 4: // EPOCHf_STYLE_EXTEND
        return 50;
      case 5: // EPOCHx_STYLE_EXTEND
        return 50;
      case 6: // EPOCH4_STYLE_EXTEND
        return 32;
    }
  }
  else if (dataType == CDF_TIME_TT2000) {
    switch (epoch) {
      case 0: // TT2000
      case 1: 
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
        return 29;
    }
  }
  /****************************************************************************
  * Check if a format exists.
  ****************************************************************************/
  if (format != null) {
    width = CDFE_CDFToolUtils.FormatWidth(format);
    if (width != 0) return Math.max(10, width) + 1;
  }
  /****************************************************************************
  * Either a format doesn't exist or is illegal.  Use the default width for
  * the data type.
  ****************************************************************************/
  switch ((int)dataType) {
    case (int)CDF_BYTE:
    case (int)CDF_INT1:
      width = 5; // = 4
      break;
    case (int)CDF_UINT1:
      width = 4; // = 3
      break;
    case (int)CDF_INT2:
      width = 7; // = 6
      break;
    case (int)CDF_UINT2:
      width = 6; // = 5
      break;
    case (int)CDF_INT4:
      width = 12; // = 11
      break;
    case (int)CDF_UINT4:
      width = 12; // = 10
      break;
    case (int)CDF_INT8:
      width = 20; // = 10
      break;
    case (int)CDF_REAL4:
    case (int)CDF_FLOAT:
      width = 15;
      break;
    case (int)CDF_REAL8:
    case (int)CDF_DOUBLE:
      width = 18;
      break;
    case (int)CDF_EPOCH:     /* Format (C/Fortran) style. */
      width = 24;
      break;
    case (int)CDF_EPOCH16:   /* Format (C/Fortran) style. */
      width = 36;
      break;
    case (int)CDF_TIME_TT2000: /* TT2000 type 3 style. */
      width = 29;
      break;
    default:            /* Unknown data type. */
      width = 0;
  }
  return Math.max(10, width) + 1;
}

/******************************************************************************
* FormatWidth.
*    Returns width of a format specifier [or zero if the width is unknown or
* illegal].
******************************************************************************/

public static int FormatWidth (String format) {

  /****************************************************************************
  * Skip past Fortran repeat count (eg. the `20' in `20I8' or `20(I8)').
  * Note that this won't skip past a C `%'.
  ****************************************************************************/
  int toEnd = format.length();
  int ind;
  char aChar;
  for (ind = 0; ind <= toEnd; ind++) {
    if (ind == toEnd) return 0;
    aChar = format.charAt(ind);
    if (aChar != '(' && aChar != ' ' && !Character.isDigit(aChar)) break;
  }

  /****************************************************************************
  * Skip past Fortran format type (eg. the `F' in `F4.1') or C `%' and/or
  * flags (one of which might be a `0').
  ****************************************************************************/
  for (; ind <= toEnd; ind++) {
    if (ind == toEnd) return 0;
    aChar = format.charAt(ind);
    if (aChar != '0' && Character.isDigit(aChar)) break;
  }

  /****************************************************************************
  * Decode format width.
  ****************************************************************************/
  int ix = 0;
  for (int iy = ind; iy < toEnd; iy++ ) {
    aChar = format.charAt(iy);
    if (Character.isDigit(aChar)) ix++;
    else break;
  }
  if (ix == 0) return 0;
  else {
    return Integer.parseInt(format.substring(ind, ind+ix));
  }
}

/******************************************************************************
* StandardWidth.
******************************************************************************/

public static int StandardWidth (int epoch, long dataType, long numElems) {

  switch ((int)dataType) {
    case (int)CDF_CHAR:
    case (int)CDF_UCHAR: 
	return (int) (1 + numElems + 1);
    case (int)CDF_BYTE:
    case (int)CDF_INT1:
    case (int)CDF_UINT1:
    case (int)CDF_INT2:
    case (int)CDF_UINT2:
    case (int)CDF_INT4:
    case (int)CDF_INT8:
    case (int)CDF_UINT4:
    case (int)CDF_REAL4:
    case (int)CDF_FLOAT:
    case (int)CDF_REAL8:
    case (int)CDF_DOUBLE: 
	return CDFE_CDFToolUtils.FormatWidth(CDFE_CDFToolUtils.StandardFormat(epoch, dataType));
    case (int)CDF_EPOCH:
      switch (epoch) {
        case 0: return 24;
        case 1: return 16;
        case 2: return 14;
        case 3: return 24;
        case 4: return 25;
        case 5: return 13;
        case 6: return 23;
	default: break;
      }
      break;
    case (int)CDF_EPOCH16:
      switch (epoch) {
        case 0: return 36;
        case 1: return 24;
        case 2: return 14;
        case 3: return 36;
        case 4: return 50;
        case 5: return 68;
        case 6: return 32;
	default: break;
      }
      break;
    case (int)CDF_TIME_TT2000:
      switch (epoch) {
        case 0: return 30;
        case 1: return 19;
        case 2: return 14;
        case 3: return 29;
	default: break;
      }
      break;
    default: break;
  }
  return 0;
}

/******************************************************************************
* StandardFormat.
******************************************************************************/

public static String StandardFormat (int epoch, long dataType) {

  switch ((int)dataType) {
    case (int)CDF_CHAR:
    case (int)CDF_UCHAR: return null;
    case (int)CDF_BYTE:
    case (int)CDF_INT1: return new String("%4d");
    case (int)CDF_UINT1: return new String("%3u");
    case (int)CDF_INT2: return new String("%6d");
    case (int)CDF_UINT2: return new String("%5u");
    case (int)CDF_INT4: return new String("%11ld");
    case (int)CDF_INT8: return new String("%20ld");
    case (int)CDF_UINT4: return new String("%10ld");
    case (int)CDF_REAL4:
    case (int)CDF_FLOAT: return new String("%16.9e");
    case (int)CDF_REAL8:
    case (int)CDF_DOUBLE: return new String("%25.17e");
    case (int)CDF_EPOCH:
      switch (epoch) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 6: return null;
        case 4: return new String("%25.17e");
        case 5: return new String("*Unsupported*");
	default: break;
      }
      break;
    case (int)CDF_EPOCH16:
      switch (epoch) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 6: return null;
        case 4: return new String("%25.17e %25.17e");
        case 5: return new String("*Unsupported*");
	default: break;
      }
      break;
    case (int)CDF_TIME_TT2000:
      switch (epoch) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6: return new String("%20ld");
	default: break;
      }
      break;
    default: break;
  }
  return null;
}

/******************************************************************************
* CatToString.
******************************************************************************/

public static StringBuffer CatToString (StringBuffer string, String cat, int length, 
                                        int justify, String more) {

  int catL = cat.length();
  int moreL = more.length();
  if (catL > length)
    if (moreL >= length)
      string = CDFE_CDFToolUtils.CatNcharacters (string, length, '*');
    else {
      string = CDFE_CDFToolUtils.strcatX (string, cat, string.length() + (length - moreL));
      string = CDFE_CDFToolUtils.strcatX (string, more, 0);
    }
  else
    switch (justify) {
      case 1: // LEFT_JUSTIFY
        string = CDFE_CDFToolUtils.strcatX (string, cat, 0);
        string = CDFE_CDFToolUtils.CatNcharacters (string, length - catL, ' ');
        break;
      case 2: // CENTER_JUSTIFY
        int after = (length - catL) / 2;
        int before = (length - catL) - after;
        string = CDFE_CDFToolUtils.CatNcharacters (string, before, ' ');
        string = CDFE_CDFToolUtils.strcatX (string, cat, 0);
        string = CDFE_CDFToolUtils.CatNcharacters (string, after, ' ');
        break;
      case 3: // RIGHT_JUSTIFY
        string = CDFE_CDFToolUtils.CatNcharacters (string, length - catL, ' ');
        string = CDFE_CDFToolUtils.strcatX (string, cat, 0);
        break;
    }
  return string;
}

/******************************************************************************
* CatNcharacters.
*   Concatenates some number of a specified character to a string.
******************************************************************************/

public static StringBuffer CatNcharacters (StringBuffer string, int nChars, char chr) {

  for (int i = 0; i < nChars; i++) string.append(chr);
  return string;
}

/******************************************************************************
* strcatX.
*    Concatenates from the source to the destination but only up to the
* maximum number of characters specified.  Then the destination
* either at the actual end of the concatenated source string or at the maximum
* number of characters.  If the maximum number of characters is zero, then a
* normal `strcat' is done.
******************************************************************************/

public static StringBuffer strcatX (StringBuffer dst, String src, int max) {
 
  if (max > 0) {
    int i = dst.length();
    int j = src.length();
    if ((i + j) <= max) 
      dst.append(src);
    else
      dst.append(src.substring(0, max-i));
  } else
    dst.append(src);
  return dst;
}

/******************************************************************************
* Justify.
* Left or rigth justify the string.
******************************************************************************/

public static StringBuffer Justify (StringBuffer string, int minWidth) {

/* If minWidth is zero, no minimum width.  If positive, right justified.
   If negative, left justified. */
 
  if (minWidth < 0) {
    int pad = -minWidth - string.length();
    if (pad > 0) string = CDFE_CDFToolUtils.CatNcharacters (string, pad, ' ');
  }
  else {
    if (minWidth > 0) {
      int i, stringL = string.length();
      int shift = minWidth - stringL;
      if (shift > 0) 
        for (i = 0; i < shift; i++) string.insert(0, ' ');
    }
  }
  return string;
}

/******************************************************************************
* Initiate the file chooser and strip the extension for the selected file
* if necessary
******************************************************************************/

    public static String doFileChooser (String title, int which, 
					CDFE_SimpleFileFilter filter, String dir,
					Component parent) {
        JFileChooser fileChooser = (JFileChooser) new MyCDFFileChooser();
	CDFE_ExampleFileView fileView = new CDFE_ExampleFileView();
        fileChooser.setCurrentDirectory(new File(dir));
        fileChooser.setDialogTitle(title);
        if (filter != null) {
          fileChooser.addChoosableFileFilter(filter);
          fileChooser.setFileFilter(filter);
        }
        try {
           fileView.putIcon("cdf",
                            new ImageIcon(((JFrame)parent).getClass().getResource("CDF_Logo3.gif")));
           fileView.putIcon("CDF",
                            new ImageIcon(((JFrame)parent).getClass().getResource("CDF_Logo3.gif")));
           fileChooser.setFileView(fileView);
        } catch (Exception e) {}
        int returnValue = fileChooser.showDialog((JFrame)parent, "OK");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
          File selectedFile = fileChooser.getSelectedFile();
          if (selectedFile != null) {
              int ind;
              String myfile = selectedFile.getAbsolutePath();
              if (which == 1) { // for .cdf file
                ind = myfile.toLowerCase().lastIndexOf(".cdf");
                if (ind != -1) myfile = myfile.substring(0, ind);
              } else { // for .skt file
                ind = myfile.toLowerCase().lastIndexOf(".skt");
                if (ind != -1) myfile = myfile.substring(0, ind);
              }
              return myfile;
          }
        }
        return null;
    }

/******************************************************************************
* Get the maximum written record number among the variables in the CDF. 
******************************************************************************/

    public static int getMaxRecNum (CDF cdf) {

        int vars = (int) cdf.getNumVars();
        int maxWritten = -1;

        for (int i = 0; i < vars; i++) {
          try {
            Variable var = cdf.getVariable((long) i);
            int max = (int) var.getMaxWrittenRecord();
            if (maxWritten < max) maxWritten = max;
          } catch (CDFException e) {
            System.err.println("Error: "+e);
          }
        }
	return maxWritten;
    }

/******************************************************************************
* EquivalentDataTypes.
******************************************************************************/

    public static boolean equivalentDataTypes (long dataType1, long dataType2) {
      switch ((int)dataType1) {
        case (int)CDF_BYTE:
        case (int)CDF_INT1:
        case (int)CDF_UINT1:
        case (int)CDF_CHAR:
        case (int)CDF_UCHAR:
          switch ((int)dataType2) {
            case (int)CDF_BYTE:
            case (int)CDF_INT1:
            case (int)CDF_UINT1:
            case (int)CDF_CHAR:
            case (int)CDF_UCHAR:
              return true;
            default:
              return false;
          }
        case (int)CDF_INT2:
        case (int)CDF_UINT2:
          switch ((int)dataType2) {
            case (int)CDF_INT2:
            case (int)CDF_UINT2:
              return true;
            default:
              return false;
          }
        case (int)CDF_INT4:
        case (int)CDF_UINT4:
          switch ((int)dataType2) {
            case (int)CDF_INT4:
            case (int)CDF_UINT4:
              return true;
            default:
              return false;
          }
        case (int)CDF_INT8:
        case (int)CDF_TIME_TT2000:
          switch ((int)dataType2) {
            case (int)CDF_INT8:
            case (int)CDF_TIME_TT2000:
              return true;
            default:
              return false;
          }
        case (int)CDF_REAL4:
        case (int)CDF_FLOAT:
          switch ((int)dataType2) {
            case (int)CDF_REAL4:
            case (int)CDF_FLOAT:
              return true;
            default:
              return false;
          }
        case (int)CDF_REAL8:
        case (int)CDF_DOUBLE:
        case (int)CDF_EPOCH:
          switch ((int)dataType2) {
            case (int)CDF_REAL8:
            case (int)CDF_DOUBLE:
            case (int)CDF_EPOCH:
              return true;
            default:
              return false;
          }
	case (int)CDF_EPOCH16:
          switch ((int)dataType2) {
            case (int)CDF_EPOCH16:
              return true;
            default:
              return false;
          }
      }
      return false;                 /* CDF_INTERNAL_ERROR or CORRUPTED_V2_CDF? */
    }

    private static class MyCDFFileChooser extends JFileChooser {

        static final long serialVersionUID = 1L;

	public String getDescription(File f) {
		if (f.getName().toLowerCase().endsWith(".cdf")) {
		    return f.getName();
		} else {
		    return super.getDescription(f);
		}
	}
 
	public String getTypeDescription(File f) {
		if (f.getName().toLowerCase().endsWith(".cdf")) {
		    return super.getTypeDescription(new File("dummy"));
		} else {
		    return super.getTypeDescription(f);
		}
	}
/* 
	public Icon getIcon(File f) {
		if (f.getName().toLowerCase().endsWith(".cdf")) {
		    return super.getIcon(new File("dummy"));
		} else {
		    return super.getIcon(f);
		}
	}
*/ 
	public boolean isTraversable(File f) {
		if (f.getName().toLowerCase().endsWith(".cdf")) {
		    return false;
		} else {
		    return super.isTraversable(f);
		}
	}
 
    }
}
