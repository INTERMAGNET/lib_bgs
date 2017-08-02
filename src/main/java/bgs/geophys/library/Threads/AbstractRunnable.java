/*
 * AbstractRunnable.java
 *
 * Created on 29 April 2004, 12:02
 */

package bgs.geophys.library.Threads;

/**
 *
 * @author  Ies
 */
public class AbstractRunnable implements Runnable{
    protected Object arg1, arg2, arg3;
    protected Object args;
    
    /** Creates a new instance of AbstractRunnable */
    public AbstractRunnable()
    {
        
    }
    
    public AbstractRunnable(Object arg1)
    {
        this.arg1 = arg1;
    }
    
    public AbstractRunnable(Object arg1, Object arg2)
    {
        this.arg1 = arg1;
        this.arg2 = arg2;
    }
    
    public AbstractRunnable(Object arg1, Object arg2, Object arg3)
    {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
    }
    
    public AbstractRunnable(Object[] args)
    {
        this.args = args;
    }
    
    public void run()
    {
        
    }
    
}
