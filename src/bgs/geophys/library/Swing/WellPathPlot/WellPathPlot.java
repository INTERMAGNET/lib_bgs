/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.Swing.WellPathPlot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;

/**
 * An object that plots well paths. Data for a plot is held in a data model
 * which can be supplied at construction. Alternatively a default data model
 * can be created which you can edit to add the elements that you want.
 * 
 * @author smf
 */
public class WellPathPlot extends JComponent
implements WellPathPlotChangeListener, MouseListener, MouseMotionListener
{
    // some constants:
    // number of traces to plot
    private static final int N_TRACES = 3;

    // length of a tick mark in pixels
    private static final int AXIS_TICK_LENGTH = 5;

    // size of symblos used to mark waypoints, in pixels
    private static final int WAYPOINT_CIRCLE_INNER_RADIUS = 4;
    private static final int WAYPOINT_CIRCLE_OUTER_RADIUS = 6;
    
    // percent extra of the measured depth range to set the axis to
    private static final double AXIS_MD_PERCENT_EXTRA = 5;
    
    // percent extra of the magnetic data range to set the axis to
    private static final double AXIS_MAG_ELEMENT_PERCENT_EXTRA = 5;
    
    // symbolic codes for each of the icons on the widget
    private enum IconID { RESET_ZOOM, GRID_TOGGLE, PAN_LINK, SHOW_INFO }
    
    /** a class used to hold information about the extent of 'real world' dimension that is
     * going to be mapped to a graphical space. Manipulating the min/max values in this
     * class will allow pan/zoom of the image */
    private class AxisExtents
    {
        // the extents - the minimum and maximum viable data values
        private double min;
        private double max;
        private double range;
        public AxisExtents (double min, double max)
        {
            this.min = min;
            this.max = max;
            this.range = max - min;
        }
        public AxisExtents (double min, double max, double percent_extend)
        {
            this.min = min - (((max - min) * percent_extend) / 100);
            this.max = max + (((max - min) * percent_extend) / 100);
            this.range = this.max - this.min;
        }
        public double getMin () { return min; }
        public double getMax () { return max; }
        public double getRange () { return range; }
        public void zoom (double percent)
        {
            min = min + (range * percent / 100.0);
            max = max - (range * percent / 100.0);
            range = max - min;
        }
        public void pan (double amount)
        {
            min = min + amount;
            max = max + amount;
        }
        @Override
        public String toString () { return "Min " + min + ", Max " + max + ", Range " + range; }
    }

    /** a class used to hold an Icon image and also record it's position */
    private class IconAtLocation extends ImageIcon
    {
        private int x;
        private int y;
        private IconID icon_id;
        private ImageIcon alternate_icon;
        private boolean show_alternate_icon;
        private boolean is_outlined;
        public IconAtLocation (URL location, String description, int x, int y, 
                               IconID icon_id, ImageIcon alternate_icon)
        {
            super (location, description);
            this.x = x;
            this.y = y;
            this.icon_id = icon_id;
            this.alternate_icon = alternate_icon;
            this.show_alternate_icon = false;
            this.is_outlined = false;
        }
        public int getX () { return x; }
        public int getY () { return y; }
        public IconID getIconID () { return icon_id; }
        public ImageIcon getAlternateIcon () { return alternate_icon; }
        public boolean isShowAlternateIcon () { return show_alternate_icon; }
        public boolean isOutlined () { return is_outlined; }
        public void setShowAlternateIcon (boolean show_alternate_icon) { this.show_alternate_icon = show_alternate_icon; }
        public void setOutlined (boolean is_outlined) { this.is_outlined = is_outlined; }
        public boolean isPointInIcon (int x, int y)
        {
            if (x < this.x) return false;
            if (y < this.y) return false;
            if (x >= (this.x + this.getIconWidth())) return false;
            if (y >= (this.y + this.getIconHeight())) return false;
            return true;
        }
        public void drawIcon (Graphics g)
        {
            if (show_alternate_icon && alternate_icon != null)
            {
                g.drawImage(alternate_icon.getImage(), x, y, null);
                if (is_outlined)
                    g.drawRoundRect(x, y, alternate_icon.getIconWidth(), alternate_icon.getIconHeight(), 2, 2);
            }
            else
            {
                g.drawImage(getImage(), x, y, null);
                if (is_outlined)
                    g.drawRoundRect(x, y, getIconWidth(), getIconHeight(), 2, 2);
            }                
        }
    }
    
    /** a class that holds a graphics object along with the width and height of
     * it's parent canvas */
    private class GraphicsWithExtent
    {
        private Graphics g;
        private int width;
        private int height;
        public GraphicsWithExtent (Graphics g, int width, int height)
        {
            this.g = g;
            this.width = width;
            this.height = height;
        }
        public Graphics getGraphics () { return g; }
        public int getWidth () { return width; }
        public int getHeight () { return height; }
        public Rectangle getRectangle () { return new Rectangle (0, 0, width, height); }
        public GraphicsWithExtent create (int x, int y, int width, int height)
        {
            return new GraphicsWithExtent (g.create (x, y, width, height), width, height);
        }
        public boolean intersects (GraphicsWithExtent g)
        {
            return getRectangle().intersects(g.getRectangle());
        }
    }
    
    // some colours
    private Color d_trace_colour;
    private Color i_trace_colour;
    private Color f_trace_colour;
    private Color trace_background_colour;
    
    // show grid lines on the plot
    private boolean show_grid_lines;
    
    // icons for navigation
    private IconAtLocation icons [];
    
    // the axes scale objects - these control pan and zoom
    private AxisExtents md_axis_extent;
    private AxisExtents element_axis_extent [];
    
    // the position of the traces on the canvas
    private Rectangle trace_position [];
    private int trace_width;
    private int trace_height;
    
    // flag for linking traces when panning
    private boolean link_when_panning;
    
    // the data model that this object views
    private WellPathPlotDataModel model;
    
    /** construct a WellPathPlot with an empty data model */
    public WellPathPlot ()
    {
        this (new WellPathPlotDataModel());
    }
    
    /** construct a WellPathPlot with the given date model */
    public WellPathPlot (WellPathPlotDataModel model)
    {
        // store the model
        this.model = model;
        model.addChangeListener(this);
        
        // set up mouse event handling
        addMouseListener(this);
        addMouseMotionListener(this);
        link_when_panning = true;

        // Register the component on the tooltip manager
        // So that #getToolTipText(MouseEvent) gets invoked when the mouse
        // hovers the component
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.registerComponent(this);      
        ttm.setInitialDelay(100);
        
        // load icons
        icons = new IconAtLocation[4];
        icons[0] = createImageIcon ("icons/Trace.gif",       "icons/TraceNoGrid.gif", "Toggle grid",                      2,  2, IconID.GRID_TOGGLE);
        icons[1] = createImageIcon ("icons/Reset.gif",       null,                    "Reset zoom",                      28,  2, IconID.RESET_ZOOM);
        icons[2] = createImageIcon ("icons/Lock.gif",        "icons/Unlock.gif",      "Link/unlink traces for panning",   2, 28, IconID.PAN_LINK);
        icons[3] = createImageIcon ("icons/Information.gif", null,                    "Show information",                28, 28, IconID.SHOW_INFO);
        
        // default settings - can be overriden by calling these same methods on this object
        this.d_trace_colour = Color.RED;
        this.i_trace_colour = Color.GREEN;
        this.f_trace_colour = Color.BLUE;
        this.trace_background_colour = Color.WHITE;
        this.show_grid_lines = true;

        // set default pan and zoom
        element_axis_extent = new AxisExtents[N_TRACES];
        trace_position = new Rectangle[N_TRACES];
        resetAxes ();
    }
    
    
    public WellPathPlotDataModel getModel () { return model; }
    public Color getDTraceColour () { return d_trace_colour; }
    public Color getITraceColour () { return i_trace_colour; }
    public Color getFTraceColour () { return f_trace_colour; }
    public Color getTraceBackgroundColour () { return trace_background_colour; }
    public boolean isShowGridLines () { return show_grid_lines; }

    public void setModel (WellPathPlotDataModel model) 
    {
        this.model.removeChangeListener(this);
        this.model = model; 
        this.model.addChangeListener(this);
        stateChanged (true);
    }
    public void setDTraceColour (Color d_trace_colour) 
    {
        this.d_trace_colour = d_trace_colour; 
        stateChanged (false);
    }
    public void setITraceColour (Color i_trace_colour) 
    {
        this.i_trace_colour = i_trace_colour; 
        stateChanged (false);
    }
    public void setFTraceColour (Color f_trace_colour) 
    {
        this.f_trace_colour = f_trace_colour; 
        stateChanged (false);
    }
    public void setTraceBackgroundColour (Color trace_background_colour) 
    {
        this.trace_background_colour = trace_background_colour; 
        stateChanged (false);
    }
    public void setShowGridLines (boolean show_grid_lines)
    {
        this.show_grid_lines = show_grid_lines;
        stateChanged (false);
    }
    

    @Override
    /**
     * draw this component
     * 
     * @param g - the graphics object to draw with - from the JavaDoc for
     * JComponent: "If you override this in a subclass you should not make 
     * permanent changes to the passed in Graphics. For example, you should 
     * not alter the clip Rectangle or modify the transform. If you need to 
     * do these operations you may find it easier to create a new Graphics 
     * from the passed in Graphics and manipulate it. Further, if you do not 
     * invoker super's implementation you must honor the opaque property, 
     * that is if this component is opaque, you must completely fill in 
     * the background in a non-opaque color. If you do not honor the opaque 
     * property you will likely see visual artifacts. The passed in Graphics 
     * object might have a transform other than the identify transform 
     * installed on it. In this case, you might get unexpected results if 
     * you cumulatively apply another transform." */
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        // get some fonts
        Font annotation_font = new JTextField ().getFont();
        FontMetrics annotation_font_metrics = g.getFontMetrics(annotation_font);
        Font title_font = new Font (annotation_font.getFontName(), Font.BOLD | Font.ITALIC, 
                                    (annotation_font.getSize() * 3) / 2);
        FontMetrics title_font_metrics = g.getFontMetrics(title_font);
        
        // find some useful widths and heights
        String max_md_string = String.format (getYAxisNumberFormatter(), model.getRangeDataValues().getMeasuredDepth());
        int y_axis_title_width = annotation_font_metrics.stringWidth ("W") * 2;
        int y_axis_width = max_md_string.length() * annotation_font_metrics.stringWidth("9") + (AXIS_TICK_LENGTH * 2);
        int title_height = (title_font_metrics.getHeight() * 3 ) / 2;
        int x_axis_title_height = annotation_font_metrics.getHeight() * 2;
        int x_axis_height = annotation_font_metrics.getHeight() + (AXIS_TICK_LENGTH * 2);
        int all_traces_width = getSize().width - (y_axis_title_width + y_axis_width);
        trace_width = all_traces_width / 3;
        trace_height = getSize().height - (title_height + x_axis_title_height + x_axis_height);
        
        // divide the canvas up into plotting spaces
        GraphicsWithExtent g_base = new GraphicsWithExtent(g, getSize().width, getSize().height);
        GraphicsWithExtent g_y_axis_title = g_base.create (0, 
                                            title_height + x_axis_title_height, 
                                            y_axis_title_width, 
                                            trace_height);
        GraphicsWithExtent g_y_axis = g_base.create (y_axis_title_width, 
                                      title_height + x_axis_title_height, 
                                      y_axis_width, 
                                      trace_height);
        GraphicsWithExtent g_title = g_base.create (y_axis_title_width + y_axis_width,
                                     0,
                                     trace_width * N_TRACES,
                                     title_height);
        GraphicsWithExtent g_x_axis_titles [] = new GraphicsWithExtent[N_TRACES];
        GraphicsWithExtent g_traces [] = new GraphicsWithExtent[N_TRACES];
        GraphicsWithExtent g_x_axes [] = new GraphicsWithExtent[N_TRACES];
        for (int count=0; count<N_TRACES; count++)
        {
            g_x_axis_titles [count] = g_base.create (y_axis_title_width + y_axis_width + (count * trace_width),
                                                     title_height,
                                                     trace_width,
                                                     x_axis_title_height);
            trace_position [count] = new Rectangle (y_axis_title_width + y_axis_width + (count * trace_width),
                                                    title_height + x_axis_title_height,
                                                    trace_width,
                                                    trace_height);
            g_traces [count] = g_base.create (trace_position [count].x, trace_position[count].y,
                                         trace_position [count].width, trace_position [count].height);
            g_x_axes [count] = g_base.create (y_axis_title_width + y_axis_width + (count * trace_width),
                                              title_height + x_axis_title_height + trace_height,
                                              trace_width,
                                              x_axis_height);
        }
        GraphicsWithExtent g_buttons = g_base.create (0, 0, y_axis_title_width + y_axis_width, title_height + x_axis_title_height);

        // set font attributes in the various graphics contexts - these graphics contexts
        // can be modified without having to restore state, as they are (deep) copies of
        // the original graphics context passed to thie method
        g_title.getGraphics().setFont (title_font);
        g_y_axis.getGraphics().setFont (annotation_font);
        g_y_axis_title.getGraphics().setFont(annotation_font);
        for (int count=0; count<N_TRACES; count++)
        {
            g_x_axis_titles[count].getGraphics().setFont(annotation_font);
            g_traces[count].getGraphics().setFont(annotation_font);
            g_x_axes[count].getGraphics().setFont(annotation_font);
        }
        
        // clear the background and plot the buttons
        clearRect(g);
        if (g_base.intersects(g_buttons)) drawIcons (g);
        
        // plot the titles
        if (g_base.intersects(g_title))
            plotTitle (g_title);
        if (g_base.intersects(g_x_axis_titles [0]))
            plotXAxisTitle (WellPathPlotDataPoint.MagElements.MAG_D, g_x_axis_titles [0],
                            "D (" + model.getMagAngleUnits() + ")", getForeground());
        if (g_base.intersects(g_x_axis_titles [1]))
            plotXAxisTitle (WellPathPlotDataPoint.MagElements.MAG_I, g_x_axis_titles [1],
                            "I (" + model.getMagAngleUnits() + ")", getForeground());
        if (g_base.intersects(g_x_axis_titles [2]))
            plotXAxisTitle (WellPathPlotDataPoint.MagElements.MAG_F, g_x_axis_titles [2],
                            "F (" + model.getMagFieldUnits() + ")", getForeground());
        if (g_base.intersects(g_y_axis_title))
            plotYAxisTitle (g_y_axis_title);
        
        // plot the axes and data
        List<Integer> y_ticks = plotYAxis (g_y_axis, md_axis_extent);
        if (g_base.intersects(g_x_axes [0]) || g_base.intersects(g_traces [0]))
        {
            List<Integer> x_ticks = plotXAxis (WellPathPlotDataPoint.MagElements.MAG_D, g_x_axes [0], element_axis_extent [0]);
            plotData (WellPathPlotDataPoint.MagElements.MAG_D, g_traces [0], 0, 
                      element_axis_extent [0], md_axis_extent, d_trace_colour, x_ticks, y_ticks);
        }
        if (g_base.intersects(g_x_axes [1]) || g_base.intersects(g_traces [1]))
        {
            List<Integer> x_ticks = plotXAxis (WellPathPlotDataPoint.MagElements.MAG_I, g_x_axes [1], element_axis_extent [1]);
            plotData (WellPathPlotDataPoint.MagElements.MAG_I, g_traces [1], 0, 
                      element_axis_extent [1], md_axis_extent, i_trace_colour, x_ticks, y_ticks);
        }
        if (g_base.intersects(g_x_axes [2]) || g_base.intersects(g_traces [2]))
        {
            List<Integer> x_ticks = plotXAxis (WellPathPlotDataPoint.MagElements.MAG_F, g_x_axes [2], element_axis_extent [2]);
            plotData (WellPathPlotDataPoint.MagElements.MAG_F, g_traces [2], 1, 
                      element_axis_extent [2], md_axis_extent, f_trace_colour, x_ticks, y_ticks);
        }
    }

    @Override
    public void stateChanged(boolean new_data) 
    {
        if (new_data) resetAxes();
        repaint ();
    }

    // ***************************************************************************
    // *** Code to do the drawing of the individual parts
    // ***************************************************************************
    private void plotTitle (GraphicsWithExtent g)
    {
        g.getGraphics().setColor (this.getForeground());
        FontMetrics fm = g.getGraphics().getFontMetrics();
        int x = (g.getWidth() - fm.stringWidth(model.getPlotTitle())) / 2; 
        int y = ((g.getHeight() - fm.getHeight()) / 2) + fm.getAscent ();
        g.getGraphics().drawString (model.getPlotTitle(), x, y);
    }
    
    private void plotYAxisTitle (GraphicsWithExtent g)
    {
        String title = "Measured Depth (" + model.getMeasuredDepthUnits() + ")";
        g.getGraphics().setColor (getForeground());
        
        // if we can rotate, do so, otherwise print the characters one by one in a vertical line
        FontMetrics fm = g.getGraphics().getFontMetrics();
        if (g.getGraphics() instanceof Graphics2D)
        {
            Graphics2D g2 = (Graphics2D) g.getGraphics();
            AffineTransform orig_transform = g2.getTransform();
            int x_pos = (g.getHeight() + fm.stringWidth(title)) / 2;
            int y_pos = (g.getWidth() / 2) + ((fm.getAscent()) / 2);
            g2.rotate (- Math.PI/2.0);
            g2.drawString (title, - x_pos, y_pos);
            g2.setTransform(orig_transform);
        }
        else
        {
            int x_pos = (g.getWidth() - fm.stringWidth("W")) / 2;
            int y_pos = (g.getHeight() - (fm.getHeight() * title.length())) / 2;
            for (int count=0; count<title.length(); count++)
            {
                String character = title.substring (count, count +1);
                g.getGraphics().drawString (character, x_pos, y_pos);
                y_pos += fm.getHeight();
            }
        }
    }
    
    private List<Integer> plotYAxis (GraphicsWithExtent g, AxisExtents md_axis_extent)
    {
        // how much vertical space do we allocate to a tick mark?
        FontMetrics fm = g.getGraphics().getFontMetrics();
        int min_label_height = (fm.getHeight() * 3) / 2;
        
        // work out the tick spacing - start by dividing the mesured depth range into 100 parts
        // (which would give rougly 100 tick marks, more than we'll ever need)
        // then work through spacings of 5 * 10 ^^ x, 2 * 10 ^^ x, 1 * 10 ^^ x, 5 * 10 ^^ (x-1), ...
        double tick_space_pow10 [] = findPow10 (md_axis_extent.getRange() / 100.0);
        if (tick_space_pow10[0] > 5.0) tick_space_pow10[0] = 5.0;
        else if (tick_space_pow10[0] > 2.0) tick_space_pow10[0] = 2.0;
        else tick_space_pow10[0] = 1.0;
        double tick_space_md, tick_space_pixels;
        int n_ticks;
        do
        {
            if (tick_space_pow10[0] <= 1.0) tick_space_pow10[0] = 2.0;
            else if (tick_space_pow10[0] <= 2.0) tick_space_pow10[0] = 5.0;
            else
            {
                tick_space_pow10[0] = 1.0;
                tick_space_pow10[1] += 1.0;
            }
            tick_space_md = tick_space_pow10 [0] * Math.pow (10.0, tick_space_pow10[1]);
            tick_space_pixels = tick_space_md / md_axis_extent.getRange() * (double) g.getHeight();
            n_ticks = (int) ((double) g.getHeight() / tick_space_pixels);
        } while ((tick_space_pixels < min_label_height) && (n_ticks > 2));
        
        // find the first tick mark, then iterate over the tick marks
        List<Integer> ticks = new ArrayList<> ();
        String formatter = getYAxisNumberFormatter();
        g.getGraphics().setColor (this.getForeground());
        int tick_mark_x = g.getWidth ();
        int y_text_offset = fm.getAscent() / 2;
        double tick_mark_md_value = md_axis_extent.getMin() - (md_axis_extent.getMin() % tick_space_md);
        do
        {
            int tick_mark_y = (int) ((tick_mark_md_value - md_axis_extent.getMin()) / md_axis_extent.getRange() * (double) g.getHeight());
            int y_text_origin = tick_mark_y + y_text_offset;
            if (y_text_origin < g.getHeight() && 
                y_text_origin - fm.getAscent() > 0)
            {
                ticks.add (tick_mark_y);
                g.getGraphics().drawLine (tick_mark_x, tick_mark_y, tick_mark_x - AXIS_TICK_LENGTH, tick_mark_y);
                String label = String.format(formatter, tick_mark_md_value);
                int x_text_origin = tick_mark_x - fm.stringWidth(label) - (2 * AXIS_TICK_LENGTH);
                g.getGraphics().drawString (label, x_text_origin, y_text_origin);
            }    
            tick_mark_md_value += tick_space_md;
        } while (tick_mark_md_value < md_axis_extent.getMax());
        
        return ticks;
    }
    
    private void plotXAxisTitle (WellPathPlotDataPoint.MagElements element, GraphicsWithExtent g, String title, Color element_colour)
    {
        FontMetrics fm = g.getGraphics().getFontMetrics();
        g.getGraphics().setColor(element_colour);
        int x_pos = (g.getWidth() - fm.stringWidth(title)) / 2;
        int y_pos = (g.getHeight() / 2) + ((fm.getAscent()) / 2);
        g.getGraphics().drawString (title, x_pos, y_pos);
    }
    
    private List<Integer> plotXAxis (WellPathPlotDataPoint.MagElements element, GraphicsWithExtent g, AxisExtents element_axis_extent)
    {
        // how much horizontal space do we allocate to a tick mark?
        FontMetrics fm = g.getGraphics().getFontMetrics();
        String formatter = getXAxisNumberFormatter(element);
        String label = String.format(formatter, model.getMaxDataValues().getElement(element));
        int min_label_width = (fm.stringWidth(label) * 3) / 2;
        
        // work out the tick spacing - start by dividing the mesured depth range into 50 parts
        // (which would give rougly 50 tick marks, more than we'll ever need)
        // then work through spacings of 5 * 10 ^^ x, 2 * 10 ^^ x, 1 * 10 ^^ x, 5 * 10 ^^ (x-1), ...
        double tick_space_pow10 [] = findPow10 (element_axis_extent.getRange() / 50.0);
        if (tick_space_pow10[0] > 5.0) tick_space_pow10[0] = 5.0;
        else if (tick_space_pow10[0] > 2.0) tick_space_pow10[0] = 2.0;
        else tick_space_pow10[0] = 1.0;
        double tick_space_element, tick_space_pixels;
        int n_ticks;
        do
        {
            if (tick_space_pow10[0] <= 1.0) tick_space_pow10[0] = 2.0;
            else if (tick_space_pow10[0] <= 2.0) tick_space_pow10[0] = 5.0;
            else
            {
                tick_space_pow10[0] = 1.0;
                tick_space_pow10[1] += 1.0;
            }
            tick_space_element = tick_space_pow10 [0] * Math.pow (10.0, tick_space_pow10[1]);
            tick_space_pixels = tick_space_element / element_axis_extent.getRange() * (double) g.getWidth();
            n_ticks = (int) ((double) g.getWidth() / tick_space_pixels);
        } while ((tick_space_pixels < min_label_width) && (n_ticks > 2));
        
        // find the first tick mark, then iterate over the tick marks
        List<Integer> ticks = new ArrayList<> ();
        g.getGraphics().setColor (this.getForeground());
        int tick_mark_y = 0;
        int y_text_offset = ((AXIS_TICK_LENGTH * 3) / 2) + fm.getAscent();
        double tick_mark_element_value = element_axis_extent.getMin() - (element_axis_extent.getMin() % tick_space_element);
        do
        {
            label = String.format(formatter, tick_mark_element_value);
            int tick_mark_x = (int) ((tick_mark_element_value - element_axis_extent.getMin()) / element_axis_extent.getRange() * (double) g.getWidth());
            int label_width = fm.stringWidth(label);
            int x_text_origin = tick_mark_x - (label_width / 2);
            if ((x_text_origin >= 0) &&
                ((x_text_origin + label_width) < g.getWidth()))
            {
                ticks.add (tick_mark_x);
                g.getGraphics().drawLine (tick_mark_x, tick_mark_y, tick_mark_x, tick_mark_y + AXIS_TICK_LENGTH);
                g.getGraphics().drawString (label, x_text_origin, tick_mark_y + y_text_offset);
            }    
            tick_mark_element_value += tick_space_element;
        } while (tick_mark_element_value < element_axis_extent.getMax());
 
        return ticks;
    }
    
    private void plotData (WellPathPlotDataPoint.MagElements element, GraphicsWithExtent g, int bounding_box_width_offset,
                           AxisExtents element_axis_extent, AxisExtents md_axis_extent, Color element_colour,
                           List<Integer> x_grid_points, List<Integer> y_grid_points)
    {
        clearRect (g.getGraphics(), trace_background_colour);
        
        // draw a rectangle round the plot
        g.getGraphics().setColor(getForeground());
        g.getGraphics().drawRect (0, 0, g.getWidth() - bounding_box_width_offset, g.getHeight() -1);

        // draw grid lines
        if (show_grid_lines)
        {
            Graphics2D g2 = null;
            Stroke old_stroke = null;
            if (g.getGraphics() instanceof Graphics2D)
            {
                g2 = (Graphics2D) g.getGraphics();
                old_stroke = g2.getStroke();
                g2.setStroke (new BasicStroke (0.0f, BasicStroke.JOIN_ROUND, BasicStroke.CAP_ROUND, 10.f, new float[] { 2.0f, 10.0f }, 0.0f));
            }
            g.getGraphics().setColor (g.getGraphics().getColor().brighter().brighter());
            for (int x_grid_point : x_grid_points)
                g.getGraphics().drawLine(x_grid_point, 0, x_grid_point, g.getHeight ());
            for (int y_grid_point : y_grid_points)
                g.getGraphics().drawLine(0, y_grid_point, g.getWidth (), y_grid_point);
            if (g2 != null) g2.setStroke(old_stroke);
        }
        
        // draw data points
        g.getGraphics().setColor (element_colour);
        Iterator<WellPathPlotDataPoint> data_point_iterator = model.getDataPointIterator();
        int last_xpos = 0;
        int last_ypos = 0;
        for (int count=0; data_point_iterator.hasNext(); count++)
        {
            WellPathPlotDataPoint data_point = data_point_iterator.next();
            int x_pos = (int) ((data_point.getElement(element) - element_axis_extent.getMin()) / element_axis_extent.getRange() * (double) g.getWidth());
            int y_pos = (int) ((data_point.getMeasuredDepth() - md_axis_extent.getMin()) / md_axis_extent.getRange() * (double) g.getHeight());
            g.getGraphics().fillOval (x_pos - WAYPOINT_CIRCLE_INNER_RADIUS, y_pos - WAYPOINT_CIRCLE_INNER_RADIUS, WAYPOINT_CIRCLE_INNER_RADIUS * 2, WAYPOINT_CIRCLE_INNER_RADIUS * 2);
            g.getGraphics().drawOval (x_pos - WAYPOINT_CIRCLE_OUTER_RADIUS, y_pos - WAYPOINT_CIRCLE_OUTER_RADIUS, WAYPOINT_CIRCLE_OUTER_RADIUS * 2, WAYPOINT_CIRCLE_OUTER_RADIUS * 2);
            if (count > 0)
                g.getGraphics().drawLine (x_pos, y_pos, last_xpos, last_ypos);
            last_xpos = x_pos;
            last_ypos = y_pos;
        }
        
    }    
    
    // ***************************************************************************
    // code to do with zooming and panning
    // ***************************************************************************
    private void resetAxes ()
    {
        md_axis_extent = new AxisExtents (model.getMinDataValues().getMeasuredDepth(),
                                          model.getMaxDataValues().getMeasuredDepth(),
                                          AXIS_MD_PERCENT_EXTRA);
        element_axis_extent[0] = new AxisExtents (model.getMinDataValues().getElement(WellPathPlotDataPoint.MagElements.MAG_D),
                                                  model.getMaxDataValues().getElement(WellPathPlotDataPoint.MagElements.MAG_D),
                                                  AXIS_MAG_ELEMENT_PERCENT_EXTRA);
        element_axis_extent[1] = new AxisExtents (model.getMinDataValues().getElement(WellPathPlotDataPoint.MagElements.MAG_I),
                                                  model.getMaxDataValues().getElement(WellPathPlotDataPoint.MagElements.MAG_I),
                                                  AXIS_MAG_ELEMENT_PERCENT_EXTRA);
        element_axis_extent[2] = new AxisExtents (model.getMinDataValues().getElement(WellPathPlotDataPoint.MagElements.MAG_F),
                                                  model.getMaxDataValues().getElement(WellPathPlotDataPoint.MagElements.MAG_F),
                                                  AXIS_MAG_ELEMENT_PERCENT_EXTRA);
    }
    
    private void zoom (double percent)
    {
        md_axis_extent.zoom (percent);
        for (int count=0; count<element_axis_extent.length; count ++)
            element_axis_extent[count].zoom(percent);
    }
    
    private void pan (double percent_x, double percent_y)
    {
        md_axis_extent.pan (percent_y);
        for (int count=0; count<element_axis_extent.length; count ++)
            element_axis_extent[count].pan(percent_x);
    }
    
    private int findTraceAtPoint (int x, int y)
    {
        for (int count=0; count<trace_position.length; count ++)
        {
            if (x < trace_position[count].x) continue;
            if (y < trace_position[count].y) continue;
            if (x >= (trace_position[count].x + trace_position[count].width)) continue;
            if (y >= (trace_position[count].y + trace_position[count].height)) continue;
            return count;
        }
        return -1;
    }
    
    // ***************************************************************************
    // *** Some maths utilities
    // ***************************************************************************
    
    /** find the exponent of a number: n = y * 10 ^ x
     * where n is the number passed to this routine x is an integer 
     * and y is between 1.0 and 10.0
     * 
     * @param number the number to find the exponent of
     * @return a two element array: element 0 = y, element 1 = x
     */
    private double [] findPow10 (double number)
    {
        double exponent = 0;
        while (number < 1.0)
        {
            number *= 10.0;
            exponent -= 1.0;
        }
        while (number > 10.0)
        {
            number /= 10.0;
            exponent += 1.0;
        }
        double ret_val [] = new double [2];
        ret_val [0] = number;
        ret_val [1] = exponent;
        return ret_val;
    }

    private String getYAxisNumberFormatter ()
    {
        if (model.getMaxDataValues().getMeasuredDepth() > 1.0) return "%.1f";
        double pow10 [] = findPow10 (model.getMaxDataValues().getMeasuredDepth());
        int precision = (int) ((- pow10[1]) + 2.0);
        return "%." + precision + "f";
    }
    
    private String getXAxisNumberFormatter (WellPathPlotDataPoint.MagElements element)
    {
        switch (element)
        {
            case MAG_D:
            case MAG_I:
                return "%.3f";
        }
        return "%.1f";
    }
    
    
    // ***************************************************************************
    // *** Some icon utilities
    // ***************************************************************************
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    private IconAtLocation createImageIcon (String path, String alt_path, String description,
                                            int x, int y, IconID icon_id) 
    {
        ImageIcon alt_icon = null;
        if (alt_path != null)
        {
            URL imgURL = getClass().getResource(alt_path);
            if (imgURL != null)
                alt_icon = new ImageIcon (imgURL, description);
            
        }
        URL imgURL = getClass().getResource(path);
        if (imgURL != null)
            return new IconAtLocation (imgURL, description, x, y, icon_id, alt_icon);
        return null;
    }
    
    public void drawIcons (Graphics g)
    {
        for (int count=0; count<icons.length; count++) 
            if (icons[count] != null)
                icons [count].drawIcon(g);
    }
    
    private IconAtLocation findIconAtPoint (int x, int y)
    {
        for (int count=0; count<icons.length; count ++)
        {
            if (icons[count].isPointInIcon(x, y)) return icons [count];
        }
        return null;
    }
    
    public IconAtLocation findIcon (IconID id)
    {
        for (int count=0; count<icons.length; count++)
            if (icons[count].getIconID() == id)
                return icons[count];
        return null;
    }
    
    public boolean clearAllIconOutlines ()
    {
        boolean ret_val = false;
        for (int count=0; count<icons.length; count++)
        {
            if (icons[count].isOutlined())
            {
                icons[count].setOutlined(false);
                ret_val = true;
            }
        }
        return ret_val;
    }
    
    
    private void clearRect (Graphics g)
    {
        clearRect (g, this.getBackground());
    }
    
    private void clearRect (Graphics g, Color c)
    {
        Color old_colour = g.getColor();
        g.setColor (c);
        Rectangle r = g.getClipBounds();
        g.fillRect (r.x, r.y, r.width, r.height);
        g.setColor(old_colour);
    }

    
    // ***************************************************************************
    // *** Mouse event handling and tool tips
    // ***************************************************************************
    
    @Override
    public void mouseClicked(MouseEvent e) 
    {
        // was this a double click
        if (e.getClickCount() == 2)
        {
            // was the mouse clicked in a trace?
            int trace_no = findTraceAtPoint(e.getX(), e.getY());
            if (trace_no >= 0)
            {
                // was this the left button with no modifiers?
                if (e.getButton() == MouseEvent.BUTTON1 &&
                    ! (e.isAltDown() | e.isAltGraphDown() | e.isControlDown() | e.isMetaDown() | e.isShiftDown()))
                    zoom (10.0);
                else
                    zoom (-10.0);
                repaint ();
            }
        }
        
        // was the mouse clicked on an icon?
        IconAtLocation icon = findIconAtPoint(e.getX(), e.getY());
        if (icon != null)
        {
            switch (icon.getIconID())
            {
                case GRID_TOGGLE: 
                    IconAtLocation grid_toggle = findIcon(IconID.GRID_TOGGLE);
                    grid_toggle.setShowAlternateIcon(! grid_toggle.isShowAlternateIcon());
                    setShowGridLines(! isShowGridLines()); 
                    repaint ();
                    break;
                case RESET_ZOOM: 
                    resetAxes(); 
                    repaint (); 
                    break;
                case PAN_LINK: 
                    link_when_panning = ! link_when_panning; 
                    IconAtLocation pan_link = findIcon(IconID.PAN_LINK);
                    pan_link.setShowAlternateIcon(! pan_link.isShowAlternateIcon());
                    repaint ();
                    break;
                case SHOW_INFO: 
                    JOptionPane.showMessageDialog(this, 
                                                  "<html><body><h1>Well Path Viewer</h1>" +
                                                  "<ul>" +
                                                  "<li>To zoom in, double click the mouse in one of the traces." +
                                                  "<li>To zoom out, double click the right mouse button." +
                                                  "<li>Hold the mouse button and drag the plot to move" +
                                                  "    the part of the plot that is visible." +
                                                  "<li>Click on the lock icon to turn off the connection between" +
                                                  "    traces when moving the visible part of the plot" + 
                                                  "<li>Grid lines can be turned off by clicking on the" +
                                                  "    chart icon." +
                                                  "</ul>" +
                                                  "<p>Icons courtesy of www.aha-soft.com",
                                                  "Well Plot Path - Information", JOptionPane.INFORMATION_MESSAGE);
                    break;
            }
        }
    }

    private int dragged_trace_no = -1;
    private MouseEvent mouse_drag_event = null;
    @Override
    public void mousePressed(MouseEvent e) 
    {
        dragged_trace_no = findTraceAtPoint(e.getX(), e.getY());
        if (dragged_trace_no >= 0) mouse_drag_event = e;
        else mouse_drag_event = null;
    }
    @Override
    public void mouseDragged(MouseEvent e) { dragTraces (e); }
    @Override
    public void mouseReleased(MouseEvent e) 
    {
        dragTraces (e); 
        mouse_drag_event = null; 
        dragged_trace_no = -1;
    }
    private void dragTraces (MouseEvent e)
    {
        if (mouse_drag_event != null)
        {
            // calculate the amount to move in pixels
            int delta_x = mouse_drag_event.getX() - e.getX();
            int delta_y = mouse_drag_event.getY() - e.getY();
            
            // convert to MD / field units
            int start, end;
            if (link_when_panning) { start = 0; end = N_TRACES; }
            else { start = dragged_trace_no; end = start +1; }
            for (int count=start; count<end; count++)
            {
                double delta_element = (double) delta_x / (double) trace_width * element_axis_extent[count].getRange();
                element_axis_extent[count].pan(delta_element);
            }
            double delta_md = (double) delta_y / (double) trace_height * md_axis_extent.getRange();
            md_axis_extent.pan(delta_md);
            mouse_drag_event = e;
            
            // redraw
            repaint();
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) 
    { 
        boolean do_repaint = false;
        IconAtLocation icon = findIconAtPoint(e.getX(), e.getY());
        if (icon == null)
        {
            if (clearAllIconOutlines()) do_repaint = true;
        }
        else
        {
            if (! icon.isOutlined())
            {
                clearAllIconOutlines();
                icon.setOutlined(true);
                do_repaint = true;
            }
        }
        if (do_repaint) repaint ();
    }
    
    @Override
    public void mouseEntered(MouseEvent e) { }
    @Override
    public void mouseExited(MouseEvent e) 
    {
        if (clearAllIconOutlines()) repaint ();
    }

    @Override
    public String getToolTipText (MouseEvent e)
    {
        // is the mouse over a trace?
        if (findTraceAtPoint(e.getX(), e.getY()) >= 0)
            return "Double click to zoom, drag to pan";
        
        // is the mouse over an icon
        IconAtLocation icon = findIconAtPoint(e.getX(), e.getY());
        if (icon != null)
            return icon.getDescription();
        
        // no tool tip
        return null;
    }

}
