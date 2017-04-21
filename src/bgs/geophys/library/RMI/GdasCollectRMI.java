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
 * RMI methods for the GDASCollect group of projects
 * 
 * @author smf
 */
public interface GdasCollectRMI extends java.rmi.Remote 
{
    public String getStatus ()
    throws RemoteException, XMLException; 
    
    public String getConfig ()
    throws RemoteException, XMLException; 
    
    public void setConfig (String config)
    throws RemoteException, XMLException, ConfigException, FileNotFoundException, IOException; 
    
    public void setMRRD(String station, int gdas_number, String type, Date mrrd) 
    throws RemoteException, InputMismatchException;
    
    public void testAddress (String station, int gdas_number, String ip_address, int ip_port)
    throws RemoteException, InputMismatchException; 
    
    public String [] getLog (int daysAgo)
    throws RemoteException, IOException; 
    
    public void pause ()
    throws RemoteException, InputMismatchException; 
    
    public void resume ()
    throws RemoteException, InputMismatchException; 
    
    public void quit ()
    throws RemoteException; 
    
}
