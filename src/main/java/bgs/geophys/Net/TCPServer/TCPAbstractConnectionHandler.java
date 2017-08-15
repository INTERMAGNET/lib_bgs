/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.Net.TCPServer;

/**
 * An object that must be extended in order to handle a connection with a
 * client. The work is started in a new thread. Shutdown of the connection 
 * is managed by this object
 * 
 * @author smf
 */
public abstract class TCPAbstractConnectionHandler extends Thread
{
    
    protected TCPConnectionProtocol connection;
    private Boolean shutdown_started;
    
    private class ShutdownHandler extends Thread
    {
        @Override
        public void run ()
        {
            synchronized (shutdown_started)
            {
                shutdown_started = true;
                connection.close();
            }
        }
    }
    
    public TCPAbstractConnectionHandler (TCPConnectionProtocol connection) { super (); commonConstruction(connection); }
    public TCPAbstractConnectionHandler (TCPConnectionProtocol connection, String name) { super (name); commonConstruction(connection); }
    public TCPAbstractConnectionHandler (TCPConnectionProtocol connection, ThreadGroup group, String name) { super (group, name); commonConstruction(connection); }
    private void commonConstruction (TCPConnectionProtocol connection)
    {
        this.connection = connection;
        shutdown_started = false;
        Runtime.getRuntime().addShutdownHook(new ShutdownHandler());
    }

    @Override
    public void start ()
    {
        synchronized (shutdown_started)
        {
            if (! shutdown_started)
            {
                this.connection = connection;
                super.start();
            }
        }
    }
    
    @Override
    public abstract void run ();
    
}
