/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.RMI;

import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 *
 * @author smf
 */
public class RMIRegistry 
{

    // the registry that the server is using - for clients this will be null
    private Registry registry;
    
    /** create an RMI registry object by looking for a registry
     * on the default port on the local address - if none is
     * present then start one
     * @throws RemoteException if the registry could not be located or started */
    public RMIRegistry ()
    throws RemoteException 
    {
        this (Registry.REGISTRY_PORT);
    }
    
    /** create an RMI registry object by looking for a registry
     * on the given port on the local address - if none is
     * present then start one
     * @throws RemoteException if the registry could not be located or started */
    public RMIRegistry (int ip_port)
    throws RemoteException 
    {
        // see if a registry already exists - the getRegistry call
        // will succeed, even if no registry exists, so you need to
        // test the registry once you've got it
        try
        {
            registry = LocateRegistry.getRegistry(ip_port);
            try { registry.lookup ("qqq"); }
            catch (NotBoundException e) { }
        }
        catch (RemoteException e)
        {
            registry = LocateRegistry.createRegistry(ip_port);
        }
    }
    
    /** create an RMI registry object by looking for a registry
     * on the default port of the given address
     * @throws RemoteException if the registry could not be located */
    public RMIRegistry (String ip_address)
    throws RemoteException 
    {
        this (ip_address, Registry.REGISTRY_PORT);
    }
    
    /** create an RMI registry object by looking for a registry
     * on the given port of the given address
     * @throws RemoteException if the registry could not be located */
    public RMIRegistry (String ip_address, int ip_port)
    throws RemoteException 
    {
        // see if a registry exists - the getRegistry call
        // will succeed, even if no registry exists, so you need to
        // test the registry once you've got it
        registry = LocateRegistry.getRegistry(ip_address, ip_port);
        try { registry.lookup ("qqq"); }
        catch (NotBoundException e) { }
    }
    
    /** bind a service name in a registry - the service name is automatically
     * constructed from the class name - the method will not through an
     * exception if the service is already bound, it will rebind it */
    public void bind (Remote o)
    throws MalformedURLException, RemoteException, AccessException
    {
        registry.rebind (makeServiceName (o), o);
    }
    
    /** find an RMI service
     * @param type the class that is expected */
    public Remote lookup (Class type)
    throws NotBoundException, MalformedURLException, RemoteException, AccessException
    {
        return registry.lookup (makeServiceName (type));
    }
    
    /** create an RMI service name from an object */
    public static String makeServiceName (Remote o)
    {
        return makeServiceName (o.getClass());
    }
    
    /** create an RMI service name from a class name */
    public static String makeServiceName (Class c)
    {
        String name;
        
        name = c.getCanonicalName();
        if (name == null) name = "Anonymous";
        return name + "_Service";
    }
 
}
