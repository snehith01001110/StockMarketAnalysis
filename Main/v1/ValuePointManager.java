package Forture.v1;

import java.util.ArrayList;

import Forture.v1.reg_sys.ValuePoint;

/**
 *  Helps display the points that are shown by the graph of the quote system 
 *  graph and the prediction graph
 *
 *  @author  Ananth Pilaka, Chinmay Gowdru, Snehith Nayak
 *  @version May 26, 2019
 *  @author  Period: 1
 *  @author  Assignment: The Amazing Stock Steroid
 *
 *  @author  Sources: Our Team
 */
public class ValuePointManager
{
    private static ArrayList<ValuePoint> points = new ArrayList<>();

    /**
     * Prints the size of the ArrayList, points. For each valuepoint in the 
     * arraylist, check if x and y are the center points of the rectangle and 
     * returns p if it is
     * @param x double x parameter
     * @param y double y parameter
     * @return the values in points that are equal to x and y, or null if none
     */
    public ValuePoint pointAt(double x, double y)
    {
        System.out.println( points.size() );
        for ( ValuePoint p : points )
        {
            if (x == p.getCenterX() && y == p.getCenterY())
            {
                System.out.println( "adsfajsdlkfjl" );
                return p;
            }
        }
        return null;
    }
    
    /**
     * Adds point p to the ArrayList, points
     * @param p ValuePoint parameter p
     */
    public void register(ValuePoint p)
    {
        points.add( p );
    }
    
    /**
     * Clears the points in the ArrayList, points
     */
    public void emptyPoints()
    {
        points.clear();
    }
    
}
