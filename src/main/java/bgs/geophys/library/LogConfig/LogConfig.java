/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.LogConfig;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;

/**
 *
 * @author smf
 */
public class LogConfig 
{

    static 
    {
        // only need to run this once - it prevents Log4j from printing an error
        // if no configuration can be found
        StatusLogger.getLogger().setLevel(Level.OFF);
    }
    
    public static Logger getLogger (Class c)
    {
        Logger logger = LogManager.getLogger(c.getName());
        return logger;
    }
    
}
