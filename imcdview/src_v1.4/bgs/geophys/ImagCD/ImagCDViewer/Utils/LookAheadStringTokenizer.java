/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.ImagCD.ImagCDViewer.Utils;

import java.util.StringTokenizer;

/**
 *
 * @author smf
 */
public class LookAheadStringTokenizer extends StringTokenizer
{
    private String peek_token;
    
    public LookAheadStringTokenizer (String str) 
    {
        super (str); 
        commonInit ();
    }
    public LookAheadStringTokenizer (String str, String delim) 
    {
        super (str, delim); 
        commonInit ();
    }
    public LookAheadStringTokenizer (String str, String delim, boolean returnDelims) 
    {
        super (str, delim, returnDelims); 
        commonInit ();
    }
    private void commonInit ()
    {
        peek_token = null;
    }

    /** get the next token, without extracting it
     * @return the token
     * @throws NoSuchElementException if there are no more tokens */
    public String peekToken ()
    {
        if (peek_token == null) peek_token = nextToken ();
        return peek_token;
    }
    
    /** get the next token, without extracting it
     * @param delim the delimiter to use - note this is only
     *        used if a read-ahead token has not already been extracted
     * @return the token
     * @throws NoSuchElementException if there are no more tokens */
    public String peekToken (String delim)
    {
        if (peek_token == null) peek_token = nextToken (delim);
        return peek_token;
    }
    public Object peekObject () { return peekToken (); }
    
    @Override
    public String nextToken ()
    {
        String string;
        
        if (peek_token != null)
        {
            string = peek_token;
            peek_token = null;
        }
        else string = super.nextToken();
        return string;
    }
    
    @Override
    public String nextToken (String delim)
    {
        String string;
        
        if (peek_token != null)
        {
            string = peek_token;
            peek_token = null;
        }
        else string = super.nextToken(delim);
        return string;
    }
    
    @Override
    public Object nextElement () { return nextToken (); }
}
