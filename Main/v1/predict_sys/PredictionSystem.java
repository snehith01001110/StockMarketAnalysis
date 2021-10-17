package Forture.v1.predict_sys;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.patriques.AlphaVantageConnector;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.Interval;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.timeseries.IntraDay;
import org.patriques.output.timeseries.data.StockData;

import Forture.v1.analytics.ArticleRating;
import Forture.v1.analytics.StockTool;
import Forture.v1.reg_sys.QuoteSystem;
import Forture.v1.reg_sys.StockGraph;


/**
 * Predicts what the company's next stock point will be through article rating
 * score and the past stock data.
 *
 * @author 
 * @version May 18, 2019
 * @author Period: 1
 * @author Assignment: The Amazing Stock Steroid
 *
 * @author Sources: Our Team
 */
public class PredictionSystem extends QuoteSystem implements ActionListener
{
    public double rating = -2;

    public StockGraph graph;

    public double forecast = Double.MIN_VALUE;

    public HashMap<String, Double> forecasts;

    public HashMap<String, String> quotes;

    /**
     * Constructor: call constructor of QuoteSystem
     */
    public PredictionSystem()
    {
        super( "Prediction Search:" );
        super.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        graph = new StockGraph();
        forecasts = new HashMap<>();
        quotes = new HashMap<>();
    }


    /*
     * (non-Javadoc)
     * 
     * @see Forture.v1.QuoteSystem#makeChangedItem()
     */
    @Override
    public JMenuItem makeChangedItem()
    {
        JMenuItem changedItem = new JMenuItem( "Open Quote System" );
        changedItem.setMnemonic( 'p' );
        changedItem.addActionListener( new ActionListener()
        {

            @Override
            public void actionPerformed( ActionEvent e )
            {
                new QuoteSystem( "Quote Search:" );
            }

        } );
        return changedItem;
    }


    /*
     * (non-Javadoc)
     * 
     * @see Final_Finalized.QuoteSystem#getQuote(java.lang.String)
     */
    @Override
    public String getQuote( String symbol )
    {
        int iLoveYou = 3000;
        double max = Double.MIN_VALUE;
        List<Double> deriv = new ArrayList<>();
        List<Integer> criticalPoints = new ArrayList<>();
        int maxIndex = 0;
        int minIndex = 0;
        double min = Double.MAX_VALUE;
        double firQrtl = 0.0;
        double thirQrtl = 0.0;
        AlphaVantageConnector apiConnection = new AlphaVantageConnector( StockTool.API_KEY,
            iLoveYou );
        TimeSeries stockTimeSeries = new TimeSeries( apiConnection );

        StockData quote = null;

        try
        {
            IntraDay response = stockTimeSeries
                .intraDay( symbol, Interval.ONE_MIN, OutputSize.COMPACT );

            List<StockData> stockData = response.getStockData();
            quote = response.getStockData().get( 0 );

            deriv.add( Double.NaN );
            for ( int i = response.getStockData().size() - 2; i > 0; i-- )
            {
                deriv.add( -1 * AROC( i - 1,
                    response.getStockData().get( i - 1 ).getClose(),
                    i + 1,
                    response.getStockData().get( i + 1 ).getClose() ) );
            }

            double threshold = 0.02;
            for ( int i = deriv.size() - 1; i >= 72; i-- )
            {
                if ( Math.abs( deriv.get( i ) ) <= 0.02 )
                {
                    criticalPoints.add( Integer.valueOf( i ) );
                }
            }

            max = Double.MIN_VALUE;
            min = Double.MAX_VALUE;

            for ( int i = 0; i < criticalPoints.size(); i++ )
            {
                if ( response.getStockData()
                    .get( criticalPoints.get( i ).intValue() )
                    .getClose() > max )
                {
                    max = response.getStockData()
                        .get( criticalPoints.get( i ).intValue() )
                        .getClose();
                    maxIndex = criticalPoints.get( i ).intValue();
                }
                else if ( response.getStockData()
                    .get( criticalPoints.get( i ).intValue() )
                    .getClose() < min )
                {
                    min = response.getStockData()
                        .get( criticalPoints.get( i ).intValue() )
                        .getClose();
                    minIndex = criticalPoints.get( i ).intValue();
                }
            }

            forecast = analyze( response.getStockData(), symbol, max, min, maxIndex, minIndex );
        }
        catch ( AlphaVantageException e )
        {
        }

        String name = stockTool.translateToName( symbol ).toUpperCase();
        return name + ": (" + symbol.toUpperCase() + ")" + '\n' + "  Price: " + quote.getClose()
            + '\n' + "  High: " + quote.getHigh() + '\n' + "  Low: " + quote.getLow() + '\n'
            + "  Volume: " + quote.getVolume() + '\n' + "  Date: "
            + quote.getDateTime().toString().substring( 0, 10 ) + '\n' + "  Time: "
            + quote.getDateTime().toString().substring( 11 ) + '\n' + '\n' 
            + "  Rating: " + rating + '\n'
            + "  Prediction(1 Day): " + forecast;
        // + " Derivs: " + deriv.toString();
    }


    /**
     * Returns difference between AROC of last min/max to endpoint.
     * 
     * @param list
     *            - List of StockData items that will be analyzed
     * @param symbol
     *            - the stock symbol of the company of interest
     * @param max
     *            - the max value
     * @param min
     *            - the min value
     * @param index
     *            - the index that should be used
     * @return the predicted point
     */
    public double analyze(
        List<StockData> list,
        String symbol,
        double max,
        double min,
        int minIndex,
        int maxIndex )
    {
        int endIndex = Math.max( minIndex, maxIndex );
        double maxmin = list.get( endIndex ).getClose();
        int midIndex = ( endIndex ) / 2;
        if ( rating == -2 )
        {
            rating = 0.360;// Microsoft0.352;//Facebook-0.062;//AMZN-0.091;//Nike:-0.076;//new
                            // ArticleRating( symbol ).getRating();
        }

        double arocEnd = AROC( 100, list.get( 0 ).getClose(), 100 - endIndex, maxmin );
        double arocMid = AROC( 100,
            list.get( 0 ).getClose(),
            100 - midIndex,
            list.get( midIndex ).getClose() );

        if ( Math.abs( arocEnd ) - Math.abs( arocMid ) >= 0 )
        {
            System.out.println( "slowing down" );
            return ( ( max - min ) * ( rating + 1.0 ) ) + list.get( 0 ).getClose();
        }
        else
        {
            System.out.println( arocMid + " :: " + arocEnd );
            System.out.println( endIndex + " index :: value " + maxmin );
            System.out.println( max - min );
            System.out.println( list.get( 0 ).getClose() );
            
            return ( ( (1 + ( ( arocMid - arocEnd ) / ( arocEnd * (endIndex - midIndex) ) ) )) * ( rating ) ) + list.get( 0 ).getClose();
//            return ( ( rating ) * ( max - min )
//                * ( 1 + ( ( arocMid - arocEnd ) / ( arocEnd * (endIndex - midIndex) ) ) ) ) + list.get( 0 ).getClose();
//            // (rating / Math.abs( rating ))
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see Forture.v1.reg_sys.QuoteSystem#actionPerformed(java.awt.event.
     * ActionEvent)
     */
    @Override
    public void actionPerformed( ActionEvent e )
    {

        if ( e.getActionCommand().equals( "makegraph" ) )
        {
            processGraph();
        }
        else if ( e.getActionCommand().equals( "getquote" ) )
        {
            processQuote();
        }

    }


    /*
     * (non-Javadoc)
     * 
     * @see Forture.v1.reg_sys.QuoteSystem#processGraph()
     */
    @Override
    public void processGraph()
    {

        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                progress.setVisible( true );
            }
        } ).start();
        try
        {
            Thread.sleep( 1000 );
        }
        catch ( InterruptedException e )
        {
            
            e.printStackTrace();
        }

        if ( stockTool.translateToName( textField.getText().toString().toLowerCase() ) != null )// means
                                                                                                // textField
                                                                                                // contains
                                                                                                // symbol
        {
            panel.remove( graph );
            if ( label != null )
                panel.remove( label );
            panel.repaint();

            // new ArticleRating(stockTool.translateToName(
            // textField.getText().toString().toLowerCase()
            // ).toUpperCase()).getRating()
            rating = ( rating == -2 ? -0.078 : rating );

            System.out.println( rating );
            graph = new PredictionGraph( textField.getText().toString().toLowerCase(), forecast );
            graph.setBounds( 330, 20, width - 330, height - 200 );
            panel.add( graph );
            label = new JLabel(
                textField.getText().toString().toUpperCase() + " - 4 Days & Prediction" );
            label.setBounds( 330 + ( width - 330 ) / 2, 20 + ( height - 200 ), 200, 20 );
            panel.add( label );
            new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    progress.setVisible( false );
                }
            } ).start();
            panel.repaint();
            rating = -2;
        }
        else if ( stockTool
            .translateToSymbol( textField.getText().toString().toLowerCase() ) != null )
        {
            panel.remove( graph );
            if ( label != null )
                panel.remove( label );
            panel.repaint();

            // new
            // ArticleRating(textField.getText().toString().toUpperCase()).getRating()
            rating = ( rating == -2 ? -0.078 : rating );

            graph = new PredictionGraph(
                stockTool.translateToSymbol( textField.getText().toString().toLowerCase() ),
                forecast );

            graph.setBounds( 330, 20, width - 330, height - 200 );
            panel.add( graph );
            label = new JLabel(
                textField.getText().toString().toUpperCase() + " - 4 Days & Prediction" );
            label.setBounds( 330 + ( width - 330 ) / 2, 20 + ( height - 200 ), 200, 20 );
            panel.add( label );
            new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    progress.setVisible( false );
                }
            } ).start();
            panel.repaint();
            rating = -2;
        }
        else
        {
            JOptionPane.showMessageDialog( this, "Stock not available" );
            progress.setVisible( false );
            rating = -2;
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see Forture.v1.reg_sys.QuoteSystem#processQuote()
     */
    @Override
    public void processQuote()
    {
        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                progress.setVisible( true );
            }
        } ).start();
        
        // means textField contains symbol
        if ( stockTool.translateToName( textField.getText().toString().toLowerCase() ) != null ) 
        {
            textArea.setText( getQuote( textField.getText().toString().toLowerCase() ) );
            new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    progress.setVisible( false );
                }
            } ).start();
        }
        else if ( stockTool
            .translateToSymbol( textField.getText().toString().toLowerCase() ) != null )
        {
            textArea.setText( getQuote(
                stockTool.translateToSymbol( textField.getText().toString().toLowerCase() ) ) );
            new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    progress.setVisible( false );
                }
            } ).start();
        }
        else
        {
            JOptionPane.showMessageDialog( this, "Stock not available" );
            progress.setVisible( false );
        }
    }


    /**
     * Caluclates the slope at between two points(Average rate of change)
     * 
     * @param x0
     *            first x coordinate
     * @param y0
     *            first y coordinate
     * @param x1
     *            second x coordinate
     * @param y1
     *            second y coordinate
     * @return the slope
     */
    public double AROC( double x0, double y0, double x1, double y1 )
    {
        return ( ( y1 - y0 ) / ( x1 - x0 ) );
    }

    // public static void main( String[] args )
    // {
    // new PredictionSystem();
    // }
}
