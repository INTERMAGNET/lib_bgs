/*
 * PersistantSessionBean.java
 *
 * Created on 15 January 2007, 14:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package bgs.geophys.library.jsp;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Class the helps in making a JSP bean persistant using HTTP cookies
 *
 * @author smf
 */
public class PersistantSessionBean 
{

    /** the bean must implement this interfaces */
    public interface Bean2Cookie
    {
        /** create a string that holds the bean contents */
        public String createCookieString ();
        /** load the bean from a string */
        public void loadFromCookieString (String cookieValue);
    }
    
    private Bean2Cookie bean;
    private String sessionAttributeName;
    private String cookieName;
    
    /** create a persistant session bean
     * @param httpRequest the servlet request object where the bean may have been put
     * @param sessionAttributeName the name for the bean in the servlet session
     * @param cookieName the name for the cookie where the bean is stored */
    public PersistantSessionBean (HttpServletRequest httpRequest,
                                  String sessionAttributeName,
                                  String cookieName)
    {
        Object object;
        HttpSession session;
        
        this.sessionAttributeName = sessionAttributeName;
        this.cookieName = cookieName;
    
        // get the bean from the session
        session = httpRequest.getSession();
        object = session.getAttribute(sessionAttributeName);
        bean = null;
        if (object != null)
        {
            if (object instanceof Bean2Cookie) bean = (Bean2Cookie) object;
        }
    } 
    
    /** get the bean - if the bean was not found in the session, then this
     * method may return null, in which case you should call setBean. */
    public Bean2Cookie getBean() 
    {
        return bean;
    }

    /** set the bean and optionally load it from a cookie
     * @param httpRequest the servlet request object where the bean is to be put
     * @param loadFlag true to attempt to load from a cookie */
    public void setBean(Bean2Cookie bean, HttpServletRequest httpRequest,
                        boolean loadFlag)
    {
        int count;
        HttpSession session;
        Cookie cookies[];

        // store the bean, locally and in the session
        session = httpRequest.getSession();
        this.bean = bean;
        session.setAttribute (sessionAttributeName, bean);
        
        // see if a ginSessionBean cookie exists
        if (loadFlag)
        {
            cookies = httpRequest.getCookies();
            if (cookies != null)
            {
                for (count=0; count<cookies.length; count++)
                {
                    if (cookies[count].getName().equals(cookieName))
                    {
                        bean.loadFromCookieString(cookies[count].getValue());
                        break;
                    }
                }
            }
        }
    }
    
    /** save the bean to a cookie 
     * @param httpResponse the servlet response where the cookie will be saved */
    public void saveBean (HttpServletResponse httpResponse)
    {
        Cookie cookie;
        
        cookie = new Cookie (cookieName, bean.createCookieString());
        cookie.setComment ("JSP session persistant bean");
        cookie.setMaxAge (86400 * 365);
        httpResponse.addCookie (cookie);
    }
}
