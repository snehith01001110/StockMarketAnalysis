package Forture.v1.reg_sys;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import org.patriques.AlphaVantageConnector;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.Interval;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.timeseries.IntraDay;
import org.patriques.output.timeseries.data.StockData;

import Forture.v1.analytics.StockTool;


public class StockGraph extends JPanel
{
    public String title;
    private int label = 25, padding = 30, axisYIncr = 10, width = 5;
    public List<Double> values;
    private ArrayList<Point2D.Double> graphingPoints;
    private Color graphColor = new Color( 100, 190, 255 );
    private Color gridColor = new Color( 200, 200, 200, 200 );    
    private List<StockData> stockInfo = null;
    private Crosshairs cross;
    private Graphics2D g2;
    private JFrame frame;
    private StockTool tool;
    private ArrayList<LocalDateTime> dates = new ArrayList<>();
    private boolean predict;
    private boolean blank;

    
    public StockGraph( String symbol, boolean pred )
    {
        title = symbol;
        this.values = stockValues(symbol);
        graphingPoints = new ArrayList<Point2D.Double>();
        tool = new StockTool();
        predict = pred;
        blank = false;
    }
    
    
    public StockGraph()
    {
        title = "Blank Graph";
        this.values = new ArrayList<>();
        this.values.add( 0.0 );
        graphingPoints = new ArrayList<Point2D.Double>();
        tool = new StockTool();
        predict = false;
        blank = true;
    }


    public ArrayList<Double> stockValues(String stockSymbol)
    {
        ArrayList<Double> values = new ArrayList<>();
        int iLoveYou = 3000;
        AlphaVantageConnector urlWrapper = new AlphaVantageConnector( StockTool.API_KEY, iLoveYou );
        TimeSeries series = new TimeSeries( urlWrapper );

        //Interval.FIFTEEN_MIN = 4 days back
        //Interval.FIVE_MIN = 24 hrs
        //
        try
        {
            IntraDay response = series.intraDay( stockSymbol, Interval.FIFTEEN_MIN, OutputSize.COMPACT );
            Map<String, String> metaData = response.getMetaData();

            List<StockData> stockData = response.getStockData();
            stockData.forEach( stock -> {
//                System.out.println( "date:   " + stock.getDateTime() );
//                System.out.println( "open:   " + stock.getOpen() );
//                System.out.println( "high:   " + stock.getHigh() );
//                System.out.println( "low:    " + stock.getLow() );
//                System.out.println( "close:  " + stock.getClose() );
//                System.out.println( "volume: " + stock.getVolume() );
                dates.add( stock.getDateTime() );
                values.add( stock.getClose() ); 
            } );
            stockInfo = stockData;
        }
        catch ( AlphaVantageException e )
        {}
        
        return values;
    }

    
    public void paintComponent( Graphics g )
    {
        super.paintComponent( g );
        g2 = (Graphics2D)g;

        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        
            double scaleX;
            double scaleY;
            
            if ( predict )
            {
                scaleX = ( (double)getWidth() - ( 2 * padding ) - label ) / ( values.size() + 28 );
                scaleY = ( (double)getHeight() - 2 * padding - label ) / ( maxValue() - minValue() );
            }
            else
            {
                scaleX = ( (double)getWidth() - ( 2 * padding ) - label ) / ( values.size() - 1 );
                scaleY = ( (double)getHeight() - 2 * padding - label ) / ( maxValue() - minValue() );
            }
            
            if ( values.size() == 0 )
            {
                JLabel error = new JLabel("Couldn't retrieve any data..." + '\n'
                    + " Check your internet connection or check that the company" + '\n'
                    + " referenced is public");
                add( error );
                error.setVisible( true );
                return;
            }
            for ( int i = 0; i < values.size(); i++ )
            {
                double x = i * scaleX + padding + label;
                double y = (maxValue() - values.get( values.size() - 1 - i ) ) * scaleY + padding;
    
                graphingPoints.add( new Point2D.Double( x, y ) );
            }
            if (predict)
            {
                graphingPoints.add( new Point2D.Double((values.size() + 26) * scaleX + padding + label,
                    scaleY * (maxValue() - getForecast()) + padding ) );
            }
    
            g2.setColor( Color.WHITE );

            g2.fillRect( padding + label,
                padding,
                getWidth() - 2 * padding - label,
                getHeight() - 2 * padding - label );
            if ( !blank )
            {
            g2.setColor( Color.BLUE );
    
            for ( int i = 0; i < axisYIncr + 1; i++ )
            {
                int x0 = padding + label;
                int x1 = width + padding + label;
                int y0 = getHeight()
                    - ( ( i * ( getHeight() - padding * 2 - label ) ) / axisYIncr + padding
                        + label );
    
                if ( values.size() >= 1 )
                {
                    g2.setColor( gridColor );
                    g2.drawLine( padding + label + 1 + width, y0, getWidth() - padding, y0 );
                    g2.setColor( Color.BLACK );
    
                    String yLabel = ( (int)( ( minValue()
                                    + ( maxValue() - minValue() ) * ( i * 1.0 / axisYIncr ) ) * 100 ) )
                                    / 100.0 + "";
                    FontMetrics metrics = g2.getFontMetrics();
                    int labelWidth = metrics.stringWidth( yLabel );
                    g2.drawString( yLabel, x0 - labelWidth - 6, y0 + ( metrics.getHeight() / 2 ) - 3 );
                }
                g2.drawLine( x0, y0, x1, y0 );
            }
    
            for ( int i = 0; i < values.size(); i++ )
            {
                if ( values.size() > 1 )
                {
                    int x0 = (int)(i * scaleX)
                        + padding + label;
                    int y0 = getHeight() - padding - label;
                    int y1 = y0 - width;
                    if ( ( i % ( (int)( ( values.size() / 8.0 ) ) + 3 ) ) == 0 )
                    {
                        g2.setColor( gridColor );
                        g2.drawLine( x0,
                            getHeight() - padding - label - 1 - width, x0, padding );
                        g2.setColor( Color.BLACK );
                        String xLabel1 = ("" + dates.get( dates.size() - 1 - i )).substring( 5, 10 );
                        String xLabel2 =  ("" + dates.get( dates.size() - 1 - i)).substring( 11 );
                        FontMetrics metrics = g2.getFontMetrics();
                        int labelWidth = metrics.stringWidth( xLabel1 );
                        if ( i % 2 == 0)
                        {
                            g2.drawString( xLabel1, x0 - labelWidth / 2, y0 + metrics.getHeight() + 2 );
                            g2.drawString( xLabel2, x0 - labelWidth / 2, y0 + 2 * metrics.getHeight() + 2  );
                        }
                        else
                        {
                            g2.drawString( xLabel1, x0 - labelWidth / 2, y0 + metrics.getHeight() + 20 );
                            g2.drawString( xLabel2, x0 - labelWidth / 2, y0 + 2 * metrics.getHeight() + 20  );
                        }
                        g2.draw( new Line2D.Double( x0, y0 + width, x0, y0 ) );
                    }
                    else
                    {
                        g2.draw( new Line2D.Double( x0, y0 + width / 2.0 , x0, y0 ) );
                    }
                }
            }
    
            g2.draw( new Line2D.Double( padding + label,
                getHeight() - padding - label,
                padding + label,
                padding ) );
            g2.draw( new Line2D.Double( padding + label,
                getHeight() - padding - label,
                getWidth() - padding,
                getHeight() - padding - label ) );
    
            Stroke previousStroke = g2.getStroke();
            g2.setColor( graphColor );
            g2.setStroke( new BasicStroke( 2f ) );
    
            for ( int i = 0; i < graphingPoints.size() - 1; i++ )
            {
                double x0 = graphingPoints.get( i ).getX();
                double y0 = graphingPoints.get( i ).getY();
                double x1 = graphingPoints.get( i + 1 ).getX();
                double y1 = graphingPoints.get( i + 1 ).getY();
                g2.draw( new Line2D.Double( x0, y0, x1, y1 ) );
            }
    
            g2.setStroke( previousStroke );
            g2.setColor( graphColor );
            double lastY = Double.MIN_VALUE;
            
            if ( cross == null)
            {
                cross = new Crosshairs( padding, label, width, graphingPoints, g2, this);
                addMouseListener( cross );
                addMouseMotionListener( cross );
            }
            else
            {
                cross.draw(g2);
            }
            
            
            StockData rat = new StockData(stockInfo.get( 0 ).getDateTime().plusDays( 2 ), 0, 0, 0, getForecast(), 0);
            ValuePoint p = null;
            
            for ( int i = 0; i < graphingPoints.size(); i++ )
            {
                double x = graphingPoints.get( i ).getX();
                double y = graphingPoints.get( i ).getY();
                
                g2.setColor( Color.BLACK );
                g2.draw( new Line2D.Double( x - width/4, 
                    getHeight() - padding - label - width * 2, 
                    x + width / 2, 
                    getHeight() - padding - label - width * 2) );
                
                if ( y < lastY )
                {
                    g2.setColor( Color.GREEN );
                }
                else
                {
                    g2.setColor( Color.RED );
                }
                
                p = new ValuePoint(x, y, width , i <= stockInfo.size() - 1 ? stockInfo.get( i ):rat, this);
                g2.fill( p );
                //ValuePoint.pMngr.register( p );
    
                g2.fill( new Rectangle2D.Double( x - width/4, 
                    getHeight() - padding - label - width * 2 - (scaleY*(lastY - y)/lastY), 
                    width / 2, width * 2 + (scaleY*(lastY - y)/lastY) ));
                lastY = y;
            }
        }
        
        g2.dispose();
        graphingPoints = new ArrayList<>();
    }

    
    public double AROC( double x0, double y0, double x1, double y1 )
    {
        return ( ( y1 - y0 ) / ( x1 - x0 ) );
    }
   
    
    public double minValue()
    {
        double minValue = Double.MAX_VALUE;

        for ( Double value : values )
        {
            if ( value < minValue )
                minValue = value;
        }

        return  minValue - 2;
    }

    
    public double maxValue()
    {
        double maxValue = Double.MIN_VALUE;

        for ( Double value : values )
        {
            if ( value > maxValue )
                maxValue = value;
        }

        return maxValue + 2;
    }

    
    public double getForecast()
    {
        return 0.0;
    }


    
    
////  In-Class Testing ONLY    
//   public static void main( String[] args )
//    {
//            SwingUtilities.invokeLater( new Runnable()
//            {
//                public void run()
//                {
//                    StockGraph graph = new StockGraph("MSFT");
//                    graph.show();
//                }
//            } );
//    }
    
    
    // For testing only
    
    public boolean getPredict()
    {
        return predict;
    }
    
    public List<Double> getValues()
    {
        return values;
    }
    
    public String getTitle()
    {
        return title;
    }
    

}
