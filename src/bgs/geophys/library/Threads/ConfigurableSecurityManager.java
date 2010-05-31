/*
 * ConfigurableSecurityManager.java
 *
 * Created on 21 November 2003, 18:33
 */

package bgs.geophys.library.Threads;

import java.util.*;
import java.security.*;

/**
 * This class can be installed as a security manager. By default it allows
 * all permissions. Permissions can be removed and added during execution.
 *
 * @author  smf
 */
public class ConfigurableSecurityManager extends SecurityManager 
{
    
    private Vector<Permission> permission_list;
    private boolean debug;
    
    /** Creates a new instance of ConfigurableSecurityManager */
    public ConfigurableSecurityManager() 
    {
        super ();
        permission_list = new Vector<Permission> ();
        debug = false;
    }
    
    /** Creates a new instance of ConfigurableSecurityManager
      @param debug true to display debugging information */
    public ConfigurableSecurityManager(boolean debug) 
    {
        super ();
        permission_list = new Vector<Permission> ();
        this.debug = debug;
    }
    
    /** Remove the applications ability to execute the specified permission
     * @param perm the permission to remove */
    public synchronized void removePermission (Permission perm)
    {
        permission_list.add (perm);
    }

    /** Add the applications ability to execute the specified permission
     * @param perm the permission to remove */
    public synchronized void addPermission (Permission perm)
    {
        int count;
        Permission test_perm;
        
        for (count=0; count<permission_list.size(); count++)
        {
            test_perm = permission_list.get (count);
            if (test_perm.equals (perm))
            {
                permission_list.remove(count);
                count --;
            }
        }
    }
    
    /** Remove all permission restrictions */
    public synchronized void addAllPermissions ()
    {
        permission_list.removeAllElements();
    }

    /** Override checkPermission to programtically control acccess
     * @param perm the permission
     * @throws SecurityException to prevent the action from taking place */
    public void checkPermission (Permission perm)
    throws SecurityException
    {
        checkPermission (perm, this);
    }

    /** Override checkPermission to programtically control acccess
     * @param perm the permission
     * @param context an object that sets the context 
     * @throws SecurityException to prevent the action from taking place */
    public synchronized void checkPermission (Permission perm, Object context)
    throws SecurityException
    {
        int count;
        Permission test_perm;
        
        for (count=0; count<permission_list.size(); count++)
        {
            test_perm = permission_list.get (count);
            if (test_perm.equals (perm))
            {
                if (debug) System.err.println ("Permission not granted: " + perm.getName() + " " + perm.getActions());
                throw new SecurityException ("Permission not granted: " + perm.getName() + " " + perm.getActions());
            }
        }
    }
    
}
