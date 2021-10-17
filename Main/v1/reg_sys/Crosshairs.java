package Forture.v1.reg_sys;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.event.MouseInputAdapter;

/**
 * This class represents the pointer used to track points on the graph
 * representation of the stockInfo. The pointer is held as three separate
 * shapes in a <code>Shape[]</code> and is animated by updating the shape and 
 * calling repaint, simulating an animation.
 *
 *  @author  Ananth Pilaka, Chinmay Gowdru, Snehith Nayak
 *  @version May 27, 2019
 *  @author  Period: 1
 *  @author  Assignment: Forture(v1.4.0)
 *
 *  @author  Sources: none
 */
public class Crosshairs extends MouseInputAdapter
{
    
    private double x;
    private double y;
    public int padding;
    public int labelPadding;
    private int pointWidth;
    private StockGraph panel;
    private Shape[] shape;
    public ArrayList<Point2D.Double> points;
    private JLabel label;
    
    
    /**
     * Crosshairs constructor 
     * @param padd - padding to take into account for x- and y- axis calcs
     * @param labelPadd - labelPadding to account for in x-axis calculations
     * @param pointWid - width of points
     * @param pointList - all the graph-translated points 
     * @param g - graphics component used in stockGraph
     * @param panel - context to which this object should be added to
     */
    public Crosshairs( int padd, int labelPadd, int pointWid, ArrayList<Point2D.Double> pointList, Graphics2D g, StockGraph panel)
    {
        this.x = padd + labelPadd + 50;
        this.y =  100;
        padding = padd;
        labelPadding = labelPadd;
        pointWidth = pointWid;
        this.panel = panel;
        points = pointList;
        shape = new Shape[3];
        shape[0] = new Line2D.Double(x, padding , x, panel.getHeight() - padding - labelPadding);
        shape[1] = new Line2D.Double(padding + labelPadding + 1, y, panel.getWidth() - padding, y);
        shape[2] = new Ellipse2D.Double( x - pointWidth/2, y - pointWidth/2, pointWidth, pointWidth  );
        label = new JLabel("( "+x+", "+y+" )");
        panel.add( label );
        label.setVisible(true);
        
        draw(g);
    }
    
    /**
     * puts individual pieces of the shape, held in the shape array, together
     * @param g - graphics compenent used to draw StockGraph
     */
    public void draw(Graphics2D g)// double x, double y )
    {
        g.setColor( Color.DARK_GRAY );
        for ( int i = 0; i < shape.length; i++ )
        {
           g.draw( shape[i] );
        }
    }
    
    /**
     * creates the animation upon the call for repaint of parent component
     */
    public void updateShape()
    {
        shape[0] = new Line2D.Double(x, padding , x, panel.getHeight() - padding - labelPadding);
        shape[1] = new Line2D.Double(padding + labelPadding + 1, y, panel.getWidth() - padding, y);
        shape[2] = new Ellipse2D.Double( x - 3, y - 3, 6, 6  );
    }
    

    /* (non-Javadoc)
     * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved( MouseEvent e )
    {
        x = e.getX();
        //y = e.getY();
        
        double minDiff = Double.MAX_VALUE;
        int index = 0;
        for ( int i = 0; i < points.size(); i++ )
        {
            if ( Math.abs(x - points.get(i).getX()) < minDiff )
            {
                index = i;
                minDiff = Math.abs( x - points.get(i).getX() );
            }
        }
        
        x = points.get( index ).getX();
        y = points.get( index ).getY();
        
        if ( x < padding + labelPadding  )
        {
            x = padding + labelPadding ;
        }
        else if ( x > panel.getWidth() - padding )
        {
            x = panel.getWidth() - padding;
        }  
//        }
        updateShape();  // = move();
        panel.repaint();
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked( MouseEvent arg0 )
    {
        System.out.println( "CLICKED" );
        
    }

}
