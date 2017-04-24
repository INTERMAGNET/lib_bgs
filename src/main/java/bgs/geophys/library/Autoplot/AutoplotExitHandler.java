/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Autoplot;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that will close running autoplot instances
 * @author smf
 */
public class AutoplotExitHandler 
extends Thread
{
    
    private List <AutoplotInstance> autoplot_instance_list;
    
    public AutoplotExitHandler ()
    {
        autoplot_instance_list = new ArrayList<> ();
    }
    
    public void addAutoplotInstance (AutoplotInstance autoplot_instance)
    {
        autoplot_instance_list.add (autoplot_instance);
    }
    
    @Override
    public void run ()
    {
        for (AutoplotInstance autoplot_instance : autoplot_instance_list)
        {
            if (autoplot_instance.isAlive())
            {
                autoplot_instance.exit ();
            }
        }
    }
    
}
