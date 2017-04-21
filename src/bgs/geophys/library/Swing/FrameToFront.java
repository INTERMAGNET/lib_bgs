/*
 * FrameToFront.java
 *
 * Created on 22 March 2005, 16:46
 */

package bgs.geophys.library.Swing;

/**
 * A class whose sole task is to bring a JFrame to the top of the windows
 * stack after a time period has elapsed.
 * @author  smf
 */
public class FrameToFront
implements java.awt.event.ActionListener
{
    // private members
    private javax.swing.JFrame frame;
    private javax.swing.Timer timer;
    private int call_count;

    /** default delay before bringing frame to front (in mS) */
    public static final int DEFAULT_DELAY = 500;
    
     /** Creates a new instance of FrameToFront
      * @param frame the frame to force to the front
      * @param delay the number of milliseconds to wait */
     public FrameToFront (javax.swing.JFrame frame, int delay)
     {         
         this.frame = frame;
         call_count = 0;
         timer = new javax.swing.Timer (delay, this);
         timer.addActionListener (this);
         timer.setRepeats (false);
         timer.setInitialDelay (delay);
         timer.start ();
     }

     /** Creates a new instance of FrameToFront
      * @param frame the frame to force to the front */
     public FrameToFront (javax.swing.JFrame frame)
     {         
         this.frame = frame;
         call_count = 0;
         timer = new javax.swing.Timer (DEFAULT_DELAY, this);
         timer.addActionListener (this);
         timer.setRepeats (false);
         timer.setInitialDelay (DEFAULT_DELAY);
         timer.start ();
     }
     
     public void actionPerformed(java.awt.event.ActionEvent e) 
     {
         if (call_count ++ <= 0)
         {
           timer.stop();
           timer = null;
           frame.toFront ();
         }
     }
     
}
