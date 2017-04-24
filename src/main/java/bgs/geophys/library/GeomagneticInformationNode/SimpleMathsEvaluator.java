/*
 * SimpleMathsEvaluator.java
 *
 * Created on 08 November 2006, 14:16
 */

package bgs.geophys.library.GeomagneticInformationNode;

import java.util.*;
import java.text.*;

/**
 * Class to evaluate a numerical expression given in a string.
 * 
 * Expression consists of the operators +, -, *, / and
 * constants (real or integer). Some operators require a
 * constant to follow (binary operators), some operators don't
 * (unary operators). The evaluation takes place from left to right.
 * Operators and constants may be separated by whitespace.
 *
 * The binary operators are:
 *	+		add
 *	-		subtract
 *	/		divide
 *	*		multiply
 *	pow		raise to the power
 *
 * The unary operators are:
 *	sin		sine
 *	cos		cosine
 *	tan		tangent
 *	asin		inverse sine
 *	acos		inverse cosine
 *	atan		inverse tangent
 *	radtodeg	convert from radians to degrees
 *	radtomin	convert from radians to minutes
 *	degtorad	convert from degrees to radians
 *	degtomin	convert from degrees to minutes
 *	mintorad	convert from minutes to radians
 *	mintodeg	convert from minutes to degrees
 *	neg		negate
 *
 * eg. variable contains 9.0, expression contains "+1 *10 -20 /4" would
 * yield 9+1=10, 10*10=100, 100-20=80, 80/4=20
 *
 * eg. variable contains 10.0, expression contains "-9.0 sin", result
 * is sin(1.0).
 *
 *
 * @author  smf
 */
public class SimpleMathsEvaluator 
{

    // test code
//    public static void main (String args[])
//    {
//        SimpleMathsEvaluator eval;
//        
//        try
//        {
//            eval = new SimpleMathsEvaluator ("sin");
//            System.out.println (eval.evaluate(1));
//            System.out.println (Math.sin (1.0));
//            System.out.println (eval.evaluate(10));
//            System.out.println (Math.sin (10.0));
//        }
//        catch (ParseException e) { e.printStackTrace(); }
//    }
    
    // private class to hold tokens and associated values
    private class token_value
    {
        public int token;
        public double value;
    }
    
    // tokens for internal token parser
    private static final int TOKEN_PLUS         = 1;
    private static final int TOKEN_MINUS	= 2;
    private static final int TOKEN_MULTIPLY	= 3;
    private static final int TOKEN_DIVIDE	= 4;
    private static final int TOKEN_SIN          = 10;
    private static final int TOKEN_COS          = 11;
    private static final int TOKEN_TAN          = 12;
    private static final int TOKEN_ASIN         = 13;
    private static final int TOKEN_ACOS         = 14;
    private static final int TOKEN_ATAN         = 15;
    private static final int TOKEN_SQRT         = 16;
    private static final int TOKEN_POW          = 17;
    private static final int TOKEN_NEG          = 18;
    private static final int TOKEN_DEGTORAD	= 100;
    private static final int TOKEN_DEGTOMIN	= 101;
    private static final int TOKEN_MINTORAD	= 102;
    private static final int TOKEN_MINTODEG	= 103;
    private static final int TOKEN_RADTODEG	= 104;
    private static final int TOKEN_RADTOMIN	= 105;

    // array of tokens and values
    private Vector<token_value> token_value_lists;
    
    /** Creates an empty instance of SimpleMathsEvaluator */
    public SimpleMathsEvaluator () 
    {
        token_value_lists = new Vector<token_value> ();
    }
    
    /** Creates a new instance of SimpleMathsEvaluator 
     * @param expression the operators to apply 
     * @throws ParseException if there was an eror parsing the expression */
    public SimpleMathsEvaluator (String expression) 
    throws ParseException
    {
        int token_count;
        String string_token;
        StringTokenizer tokens;
        token_value token;
        
        token_value_lists = new Vector<token_value> ();
        tokens = new StringTokenizer (expression);
        token_count = 0;
        try
        {
            while (tokens.hasMoreTokens())
            {
                string_token = tokens.nextToken();
                token_count ++;
                token = new token_value ();
                if (string_token.equalsIgnoreCase("+"))
                {
                    token.token = TOKEN_PLUS;
                    token.value = Double.parseDouble(tokens.nextToken());
                    token_count ++;
                }
                else if (string_token.equalsIgnoreCase("-"))
                {
                    token.token = TOKEN_MINUS;
                    token.value = Double.parseDouble(tokens.nextToken());
                    token_count ++;
                }
                else if (string_token.equalsIgnoreCase("/"))
                {
                    token.token = TOKEN_DIVIDE;
                    token.value = Double.parseDouble(tokens.nextToken());
                    token_count ++;
                }
                else if (string_token.equalsIgnoreCase("*"))
                {
                    token.token = TOKEN_MULTIPLY;
                    token.value = Double.parseDouble(tokens.nextToken());
                    token_count ++;
                }
                else if (string_token.equalsIgnoreCase("pow"))
                {
                    token.token = TOKEN_POW;
                    token.value = Double.parseDouble(tokens.nextToken());
                    token_count ++;
                }
                else if (string_token.equalsIgnoreCase("asin"))
           		token.token = TOKEN_ASIN;
                else if (string_token.equalsIgnoreCase("acos"))
                    token.token = TOKEN_ACOS;
                else if (string_token.equalsIgnoreCase("atan"))
                    token.token = TOKEN_ATAN;
                else if (string_token.equalsIgnoreCase("sin"))
                    token.token = TOKEN_SIN;
                else if (string_token.equalsIgnoreCase("cos"))
                    token.token = TOKEN_COS;
                else if (string_token.equalsIgnoreCase("tan"))
                    token.token = TOKEN_TAN;
                else if (string_token.equalsIgnoreCase("sqrt"))
                    token.token = TOKEN_SQRT;
                else if (string_token.equalsIgnoreCase("neg"))
                    token.token = TOKEN_NEG;
                else if (string_token.equalsIgnoreCase("degtorad"))
                    token.token = TOKEN_DEGTORAD;
                else if (string_token.equalsIgnoreCase("degtomin"))
                    token.token = TOKEN_DEGTOMIN;
                else if (string_token.equalsIgnoreCase("mintorad"))
                    token.token = TOKEN_MINTORAD;
                else if (string_token.equalsIgnoreCase("mintodeg"))
                    token.token = TOKEN_MINTODEG;
                else if (string_token.equalsIgnoreCase("radtodeg"))
                    token.token = TOKEN_RADTODEG;
                else if (string_token.equalsIgnoreCase("radtomin"))
                    token.token = TOKEN_RADTOMIN;
                else
                    throw new ParseException ("Unknown token: " + string_token, token_count);
                token_value_lists.add (token);
            }
        }
        catch (NumberFormatException e) { throw new ParseException ("Bad number", token_count); }
        catch (NoSuchElementException e) { throw new ParseException ("Missing number", token_count); }
    }
    
    /** evalute the expression
     * @param number the number to evalute the expression on 
     * @return the number with the expression evaluated */
    public double evaluate (double number)
    {
        int count;
        token_value token;
        
        for (count=0; count<token_value_lists.size(); count++)
        {
            token = (token_value) token_value_lists.get (count);
            switch (token.token)
            {
                case TOKEN_PLUS:
                    number += token.value;
                    break;
                case TOKEN_MINUS:
                    number -= token.value;
                    break;
                case TOKEN_MULTIPLY:
                    number *= token.value;
                    break;
                case TOKEN_DIVIDE:
                    if (token.value == 0.0) number = 0.0;
                    else number /= token.value;
                    break;
                case TOKEN_POW:
                    number = Math.pow (number, token.value);
                    break;
                case TOKEN_SIN:
                    number = Math.sin (number);
                    break;
                case TOKEN_COS:
                    number = Math.cos (number);
                    break;
                case TOKEN_TAN:
                    number = Math.tan (number);
                    break;
                case TOKEN_ASIN:
                    number = Math.asin (number);
                    break;
                case TOKEN_ACOS:
                    number = Math.acos (number);
                    break;
                case TOKEN_ATAN:
                    number = Math.atan (number);
                    break;
                case TOKEN_SQRT:
                    number = Math.sqrt (number);
                    break;
                case TOKEN_NEG:
                    number = - number;
                    break;
                case TOKEN_DEGTORAD:
                    number = (number * Math.PI) / 180.0;
                    break;
                case TOKEN_DEGTOMIN:
                    number = number * 60.0;
                    break;
                case TOKEN_MINTORAD:
                    number = (number * Math.PI) / (180.0 * 60.0);
                    break;
                case TOKEN_MINTODEG:
                    number = number / 60.0;
                    break;
                case TOKEN_RADTODEG:
                    number = (number * 180.0) / Math.PI;
                    break;
                case TOKEN_RADTOMIN:
                    number = (number * 180.0 * 60.0) / Math.PI;
                    break;
            }
        }
        
        return number;
    }
    
}
