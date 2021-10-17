package Forture.v1.reg_sys;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.patriques.output.timeseries.data.StockData;

import Forture.v1.ValuePointManager;

/**
 *  Displays the info for the company's stock and uses JPanel for the guiContext
 *
 *  @author  Ananth Pilaka, Chinmay Gowdru, Snehith Nayak
 *  @version May 27, 2019
 *  @author  Period: 1
 *  @author  Assignment: The Amazing Stock Steroid
 *
 *  @author  Sources: Our Team
 */
public class ValuePoint extends Ellipse2D.Double
{
    
    private StockData info;
    private JPanel guiContext;
    private double x, y;
    public static ValuePointManager pMngr;
    
    /**
     * Constructor for ValuePoint
     * @param x double x parameter
     * @param y double y parameter
     * @param width int width parameter
     * @param point StockData point parameter
     * @param gui StockGraph gui parameter
     */
    public ValuePoint(double x, double y, int width, StockData point, StockGraph gui )
    {
        super(x - width/2 , y - width/2, width, width);
        info = point;
        guiContext = gui;
        this.x = x;
        this.y = y;
        //pMngr = new ValuePointManager();
    }

    public double getX()
    {
        return x;
    }
    
    public double getY()
    {
        return y;
    }
    
    public StockData getData()
    {
        return info;
    }
    
    public JPanel getParComponent()
    {
        return guiContext;
    }
    
    /**
     * Displays the open, high, low, close, high, volume, and date for the 
     * Quote System stock for the company.
     */
    public void displayInfo()
    {
        System.out.println( "SDKjlakjdsfljlskdjf;lasd");
//        String infor = 
//                        "Open: " + info.getOpen() + '\n'
//                        + "High: " + info.getHigh() + '\n'
//                        + "Low: " + info.getLow() + '\n'
//                        + "Close: " + info.getClose() + '\n'
//                        + "Volume: " + info.getVolume() + 'n'
//                        + "% Change: ??%" + '\n'
//                        + "Date: " + info.getDateTime();
//        JLabel infoAtPoint = new JLabel();
//        infoAtPoint.setPreferredSize( new Dimension( 50, 50) );
//        infoAtPoint.setLocation( new Point( (int)(x + 0.5), (int)(y + 0.5) ) );
//        infoAtPoint.setText( infor );
//        infoAtPoint.setVisible( true );
//        guiContext.add( infoAtPoint );
    }
}
