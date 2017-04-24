/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.RMI;

import bgs.geophys.library.Gdas.GdasCollect.Config.ConfigException;
import bgs.geophys.library.Gdas.GdasCollect.XMLException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.InputMismatchException;

/**
 * An implementation of GdasCollectRMI that delegates all work to a
 * second implementation - the second implementation is installable,
 * so a server can implement the interface, then pass its implementation
 * to this object.
 * 
 * @author smf
 */
public class GdasCollectRMIImpl extends java.rmi.server.UnicastRemoteObject 
implements GdasCollectRMI 
{

    private GdasCollectRMI serverHandler;
    private Object server_lock;
    
    // Implementations must have an explicit constructor 
    // in order to declare the RemoteException exception 
    public GdasCollectRMIImpl () 
    throws java.rmi.RemoteException 
    { 
        super(); 
        server_lock = new Object ();
        serverHandler = null;
    } 
    
    /** set the object that will process server requests */
    public void setServerHandler (GdasCollectRMI serverHandler)
    {
        synchronized (server_lock)
        {
            this.serverHandler = serverHandler;
        }
    }
    
    /** clear the object that will process server requests */
    public void clearServerHandler ()
    {
        synchronized (server_lock)
        {
            this.serverHandler = null;
        }
    }
    
    public String getStatus()
    throws RemoteException, XMLException 
    {
        synchronized (server_lock)
        {
            if (this.serverHandler == null) throw new RemoteException ("Service not available");
            return serverHandler.getStatus();
        }
    }

    public String getConfig() 
    throws RemoteException, XMLException 
    {
        synchronized (server_lock)
        {
            if (this.serverHandler == null) throw new RemoteException ("Service not available");
            return serverHandler.getConfig();
        }
    }

    public void setConfig(String config)
    throws RemoteException, XMLException, ConfigException, FileNotFoundException, IOException
    {
        synchronized (server_lock)
        {
            if (this.serverHandler == null) throw new RemoteException ("Service not available");
            serverHandler.setConfig(config);
        }
    }

    public void setMRRD(String station, int gdas_number, String type, Date mrrd) 
    throws RemoteException, InputMismatchException
    {
        synchronized (server_lock)
        {
            if (this.serverHandler == null) throw new RemoteException ("Service not available");
            serverHandler.setMRRD(station, gdas_number, type, mrrd);
        }
    }

    public void testAddress(String station, int gdas_number, String ip_address, int ip_port)
    throws RemoteException, InputMismatchException
    {
        synchronized (server_lock)
        {
            if (this.serverHandler == null) throw new RemoteException ("Service not available");
            serverHandler.testAddress(station, gdas_number, ip_address, ip_port);
        }
    }

    public String [] getLog(int daysAgo) 
    throws RemoteException, IOException
    {
        synchronized (server_lock)
        {
            if (this.serverHandler == null) throw new RemoteException ("Service not available");
            return serverHandler.getLog(daysAgo);
        }
    }

    public void pause()
    throws RemoteException, InputMismatchException
    {
        synchronized (server_lock)
        {
            if (this.serverHandler == null) throw new RemoteException ("Service not available");
            serverHandler.pause();
        }
    }

    public void resume()
    throws RemoteException, InputMismatchException
    {
        synchronized (server_lock)
        {
            if (this.serverHandler == null) throw new RemoteException ("Service not available");
            serverHandler.resume();
        }
    }

    public void quit() throws RemoteException 
    {
        synchronized (server_lock)
        {
            if (this.serverHandler == null) throw new RemoteException ("Service not available");
            serverHandler.quit();
        }
    }

}
