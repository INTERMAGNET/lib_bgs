/*
 * HttpAuthenticate.java
 *
 * Created on 16 January 2008, 09:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.jsp;

import java.io.IOException;
import java.util.StringTokenizer;
// import javax.servlet.http.HttpServletResponse;

import bgs.geophys.library.Crypto.Base64;

/**
 * Class created to do BASIC and DIGEST http authentication, before I realised
 * you could do them in Tomcat. Class is unfinished - it probably works
 * with BASIC authentication, but needs (much) more code for DIGEST.
 * Still may be some useful stuff in here.
 * @author smf
 */
public class HttpAuthenticate 
{
    /** interface that user must implement to check received passwords */
    public interface CheckPassword
    {
        public boolean checkPassword (HttpAuthenticate authenticate);
    }
    
    /** code - the type of authentication: NONE */
    public static final int HTTP_AUTHTYPE_NONE = 0;
    /** code - the type of authentication: BASIC */
    public static final int HTTP_AUTHTYPE_BASIC = 1;
    /** code - the type of authentication: DIGEST */
    public static final int HTTP_AUTHTYPE_DIGEST = 2;
    
    /** code - return from authenticate(): user/password decoded and checked OK */
    public static final int HTTP_AUTHENTICATE_PASSWORD_OK = 0;
    /** code - return from authenticate(): user/password decoded, but doesn't check */
    public static final int HTTP_AUTHENTICATE_BAD_PASSWORD = 1;
    /** code - return from authenticate(): missing (null) authentication string */
    public static final int HTTP_AUTHENTICATE_NONE = 2;
    /** code - return from authenticate(): format error in authentication string */
    public static final int HTTP_AUTHENTICATE_FORMAT_ERROR = 3;
    /** code - return from authenticate(): previous failures prevent access */
    public static final int HTTP_AUTHENTICATE_LOCKED_OUT = 4;
    private String user;
    private String password;
    private int required_type;
    private int received_type;
    private String required_realm;
    private String received_realm;
    private int n_failed_attempts;
    private int max_attempts;
    private CheckPassword check_password;
    
    /** Creates a new instance of HttpAuthenticate
     * @param required_type the type of authenticate required - one of the
     *        HTTP_AUTHTYPE_ codes
     * @param required_realm the realm required for this authentication
     * @param max_attempts maximum number of attempts to send password
     *        before user is locked out
     * @param passwordListener interface that is called to check passwords */
    public HttpAuthenticate (int required_type, String required_realm,
                             int max_attempts, CheckPassword check_password)
    {
        this.required_type = required_type;
        this.required_realm = required_realm;
        user = password = received_realm = null;
        this.received_type = HTTP_AUTHTYPE_NONE;
        n_failed_attempts = 0;
        this.max_attempts = max_attempts;
        this.check_password = check_password;
    }
    /* decode an authorization header - this does not authenticate the
     * user - the caller needs to check the username and
     * password first - if this routine returns TRUE, call checkUser ()
     * @param authorization the authorization header from the http request -
     *        get this from request.getHeader("Authorization")
     * @return one of the HTTP_AUTHENTICATE_ codes */
    public int authenticate (String authorization)
    {
        String authType, userInfo, nameAndPassword;
        StringTokenizer tokens;
        
        // check if we are locked out
        if (n_failed_attempts > max_attempts) return HTTP_AUTHENTICATE_LOCKED_OUT;
        
        // check if this request includes an authorization
        if (authorization == null) return HTTP_AUTHENTICATE_NONE;
        // Authorization header looks like "<type> <data>",
        // where <type> is BASIC or DIGEST and <data>
        // is the username/password, encoded as follows:
        // BASIC: base64 encoded username and password formatted as "user:pass"
        // DIGEST: 
        tokens = new StringTokenizer (authorization);
        if (tokens.countTokens() < 2) 
        {
            n_failed_attempts ++;
            return HTTP_AUTHENTICATE_FORMAT_ERROR;
        }
        authType = tokens.nextToken().toUpperCase();
        userInfo = tokens.nextToken();
        if (authType.equals("BASIC"))
        {
            nameAndPassword = new String (Base64.decode (userInfo));
            tokens = new StringTokenizer (nameAndPassword, ":");
            if (tokens.countTokens() < 2) return HTTP_AUTHENTICATE_FORMAT_ERROR;
            received_realm = getRequiredRealm();
            received_type = HTTP_AUTHTYPE_BASIC;
            user = tokens.nextToken();
            password = tokens.nextToken();
            while (tokens.hasMoreTokens()) password += ":" + tokens.nextToken();
        }
        else if (authType.equals("DIGEST"))
        {
                
        }
        else
        {
            n_failed_attempts ++;
            return HTTP_AUTHENTICATE_FORMAT_ERROR;
        }
        
        // check the password
        if (required_type != received_type ||
            ! check_password.checkPassword (this))
        {
            n_failed_attempts ++;
            return HTTP_AUTHENTICATE_BAD_PASSWORD;
        }
        n_failed_attempts = 0;
        return HTTP_AUTHENTICATE_PASSWORD_OK;
    }
    
    /** set up the servlet response to ask for a password
     * @param response the servlet response */
//    public void askForPassword (HttpServletResponse response)
//    {
//        String type_string;
//        
//        response.setStatus (response.SC_UNAUTHORIZED); // I.e., 401
//        if (required_type == HTTP_AUTHTYPE_BASIC) type_string = "BASIC";
//        else type_string = "DIGEST";
//        response.setHeader ("WWW-Authenticate", 
//                            type_string + " realm=\"" + required_realm + "\"");
//    }
    
    public String getUser() { return user; }
    public String getPassword() { return password; }
    public int getRequiredType() { return required_type; }
    public int getReceivedType() {  return received_type; }
    public String getRequiredRealm() { return required_realm; }
    public String getReceivedRealm() { return received_realm; }
    public int getNFailedAttempts() { return n_failed_attempts; }
}
