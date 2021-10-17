package Forture.v1.predict_sys;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;

import Forture.v1.analytics.StockTool;
import Forture.v1.reg_sys.StockGraph;
/**
 *  Extends StockGraph and uses the PredictionSystem. Plots the predicted stock 
 *  onto the graph
 *
 *  @author  Ananth Pilaka, Chinmay Gowdru, Snehith Nayak
 *  @version May 27, 2019
 *  @author  Period: 1
 *  @author  Assignment: The Amazing Stock Steroid
 *
 *  @author  Sources: Our Team
 */
public class PredictionGraph extends StockGraph
{

    private double forecast;
    private StockTool tool;
    public String title;
    
    /**
     * Constructor for PredictionGraph class
     * @param symbol String symbol parameter
     * @param forecast double forecast parameter
     */
    public PredictionGraph( String symbol, double forecast)
    {
        super( symbol, true);
        this.forecast = forecast;
        title = symbol;
    }
    
    /* (non-Javadoc)
     * @see Final_Finalized.reg_sys.StockGraph#getForecast()
     */
    @Override
    public double getForecast()
    {
        return forecast;
    }
    
    /* (non-Javadoc)
     * @see Final_Finalized.reg_sys.StockGraph#minValue()
     */
    @Override
    public double minValue()
    {
        double minValue = Double.MAX_VALUE;

        for ( Double value : values )
        {
            if ( value < minValue )
                minValue = value;
        }

        return Math.min( getForecast(), minValue ) - 2;
    }

    /* (non-Javadoc)
     * @see Final_Finalized.reg_sys.StockGraph#maxValue()
     */
    @Override
    public double maxValue()
    {
        double maxValue = Double.MIN_VALUE;

        for ( Double value : values )
        {
            if ( value > maxValue )
                maxValue = value;
        }

        return Math.max( maxValue, getForecast() ) + 2;
    }

}
