package Forture.v1.reg_sys;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.net.ssl.ExtendedSSLSession;
import javax.swing.*;

import org.patriques.AlphaVantageConnector;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.Interval;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.timeseries.IntraDay;
import org.patriques.output.timeseries.data.StockData;

import Forture.v1.analytics.StockTool;
import Forture.v1.predict_sys.PredictionSystem;



public class QuoteSystem extends JFrame implements ActionListener
{
    private static JFrame thisWindow;
    public JPanel panel;
    private StockGraph graph;
    public StockTool stockTool;
    public JTextField textField;
    public JTextArea textArea;
    private JMenuItem openItem, exitItem;
    private JDialog dialog;
    private boolean shift = false;
    public int height;
    public int width;
    public JLabel label;
    public JProgressBar progress;
    
    
    public QuoteSystem(String purpose){
        super(purpose);
        
        width = Toolkit.getDefaultToolkit().getScreenSize().width;
        height = Toolkit.getDefaultToolkit().getScreenSize().height;
        thisWindow = this;
        setBounds(0, 0, width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(null);
        
        progress = new JProgressBar();
        progress.setBounds( 50, 350, 200, 20 );
        progress.setIndeterminate( true );
        panel.add( progress );
        progress.setVisible( false );
        progress.setStringPainted( true );
        progress.setString( "Building graph..." );
        
        addComponents();
        setVisible(true);
        stockTool = new StockTool();
        
    }
    
    
    private void addComponents()
    { 
        
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("View");
        fileMenu.setMnemonic('v');
        
        openItem = new JMenuItem("Open...");
        openItem.setMnemonic('o');
        openItem.addActionListener(new ActionListener()
            {
                String pathName = System.getProperty("c.dir") + "/";
                @Override
                public void actionPerformed( ActionEvent e )
                {
                    JFileChooser fileChooser = new JFileChooser(pathName);
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    int result = fileChooser.showOpenDialog(thisWindow);
                    if (result == JFileChooser.CANCEL_OPTION)
                        return;

                    File file = fileChooser.getSelectedFile();
                    if (file != null)
                        pathName = file.getAbsolutePath();

                    BufferedReader inputFile;
                    try
                    {
                        inputFile = new BufferedReader(new FileReader(pathName), 1024);
                    }
                    catch (FileNotFoundException ex)
                    {
                        JOptionPane.showMessageDialog(thisWindow, "Invalid File Name",
                            "Cannot open " + pathName, JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try
                    {
                        inputFile.close();
                    }
                    catch (IOException ex)
                    {
                        System.err.println("Error closing " + pathName + "\n");
                        return;
                    }

                    textArea.setText("Indexed " + pathName);
                    
                }
            
            });
        exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('x');
        exitItem.addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed( ActionEvent e )
                {
                    System.exit(0);
                }
            
            });
        
        fileMenu.add(makeChangedItem());
        fileMenu.addSeparator();
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        
        ImageIcon refresh = new ImageIcon("src/main/resources/refresh_icon.png");
        JButton button = new JButton("Refresh Graph  ", refresh);
        button.setBounds(140, 310, 94 + 69, refresh.getIconHeight());
        button.setHorizontalTextPosition(JButton.RIGHT );
        button.setVerticalTextPosition(JButton.CENTER);
        panel.add(button);
        button.setActionCommand("makegraph");
        button.addActionListener(this);
        
        JButton button2 = new JButton("Get Data");
        button2.setBounds(20, 310, 100, refresh.getIconHeight());
        panel.add(button2);
        button2.setActionCommand("getquote");
        button2.addActionListener(this);
        
        textField = new JTextField();
        textField.setBounds(20, 40, 300, 20);
        textField.addKeyListener( new KeyListener()
            {

                @Override
                public void keyPressed( KeyEvent e )
                {
                    
                    if ( e.getKeyCode() == KeyEvent.VK_SHIFT )
                    {
                        shift = true;
                    }
                    else if ( !shift && e.getKeyCode() == KeyEvent.VK_ENTER)
                    {
                        if ( !textField.getText().toString().trim().equals( "" ))
                        {
                            processQuote();
                        }
                        else
                        {
                            JOptionPane.showMessageDialog( dialog, "Please enter a stock name or symbol" );
                        }
                    }
                    else if ( shift && e.getKeyCode() == KeyEvent.VK_ENTER )
                    {
                        processGraph();
                    }
                        
                }

                @Override
                public void keyReleased( KeyEvent e )
                {
                    if ( e.getKeyCode() == KeyEvent.VK_SHIFT)
                    {
                        shift = false;
                    }
                }

                @Override
                public void keyTyped( KeyEvent e )
                {

                    
                }
            
            });
        panel.add(textField);       
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(20, 80, 300, 200);
        panel.add(scrollPane);
        
        textArea = new JTextArea();
        scrollPane.setViewportView(textArea);
        
        graph = new StockGraph();
        label = new JLabel( graph.title.toUpperCase() + " - 4 Days");
        label.setBounds( 330 + (width - 330)/2, 20 + (height - 200), 200, 20 );
        panel.add( label );
        panel.add( graph );
        
        
        JLabel label = new JLabel("Type in the Company Name or Symbol:");
        label.setBounds(20, 20, 250, 20);
        panel.add(label);
        
        JLabel label2 = new JLabel("Data:");
        label2.setBounds(20, 60, 200, 20);
        panel.add(label2);
    }
    
    public JMenuItem makeChangedItem()
    {
        JMenuItem changedItem = new JMenuItem("Open Predictive System");
        changedItem.setMnemonic('p');
        changedItem.addActionListener( new ActionListener() 
        {

            @Override
            public void actionPerformed( ActionEvent e )
            {
                new PredictionSystem();
            } 
            
        });
        return changedItem;
    }
    
    public void actionPerformed(ActionEvent e){
        
        if(e.getActionCommand().equals("makegraph"))
        {
            processGraph();
        }
        else if (e.getActionCommand().equals("getquote"))
        {
            processQuote();
        }
        
        
    }
    
    public String getQuote(String symbol)
    {
        int iLoveYou = 3000;
        AlphaVantageConnector apiConnection = new AlphaVantageConnector( StockTool.API_KEY, iLoveYou );
        TimeSeries stockTimeSeries = new TimeSeries( apiConnection );

        StockData quote = null;
        try
        {
            IntraDay response = stockTimeSeries.intraDay( symbol, Interval.ONE_MIN, OutputSize.COMPACT );

            List<StockData> stockData = response.getStockData();
            quote = response.getStockData().get( 0 );
            
        }
        catch ( AlphaVantageException e )
        {
            
        }
        String name = stockTool.translateToName( symbol ).toUpperCase();
        return name + ": (" + symbol.toUpperCase() + ")" + '\n'
                        + "  Price: " + quote.getClose() + '\n' 
                        + "  High: " + quote.getHigh() + '\n'
                        + "  Low: " + quote.getLow() + '\n'
                        + "  Volume: " + quote.getVolume() + '\n'
                        + "  Date: " + quote.getDateTime().toString().substring( 0, 10 ) + '\n'
                        + "  Time: " + quote.getDateTime().toString().substring( 11 ) ;
        
    }
    
    public void processGraph()
    {
        if ( stockTool.translateToName( textField.getText().toString().toLowerCase() ) != null )// means textField contains symbol
        {
            progress.setVisible( true );
            panel.remove( graph );
            if ( label != null )
                panel.remove( label );
            panel.repaint();
            //loading symbol
            graph = new StockGraph(textField.getText().toString().toLowerCase(), false);
            graph.setBounds( 330, 20, width - 330, height - 200 );
            panel.add( graph );
            label = new JLabel( stockTool.translateToName( graph.title ).toUpperCase() + " - 4 Days");
            label.setBounds( 330 + (width - 330)/2, 20 + (height - 200), 200, 20 );
            panel.add( label );
            progress.setVisible( false );
            panel.repaint();
        }
        else if ( stockTool.translateToSymbol( textField.getText().toString().toLowerCase() ) != null )
        {
            progress.setVisible( true );
            panel.remove( graph );
            if ( label != null )
                panel.remove( label );
            panel.repaint();
            graph = new StockGraph(stockTool.translateToSymbol(textField.getText().toString().toLowerCase()), false);
//            JFrame frame = new JFrame(stockTool.translateToName( graph.title ).toUpperCase() + " - 4 Days");
//            graph.setPreferredSize( new Dimension( 1000, 600) );
//            frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
//            frame.getContentPane().add( graph );
//            frame.pack();
//            frame.setResizable( false );
//            frame.setLocationRelativeTo(null);
//            frame.setLayout( new FlowLayout() );
//            frame.setVisible( true );
            graph.setBounds( 330, 20, width - 330, height - 200 );
            panel.add( graph );
            label = new JLabel( stockTool.translateToName( graph.title ).toUpperCase() + " - 4 Days");
            label.setBounds( 330 + (width - 330)/2, 20 + (height - 200), 200, 20 );
            panel.add( label );
            new Thread(new Runnable(){
                @Override
                public void run()
                {
                    progress.setVisible( false );   
                }
            }).start();
            panel.repaint();
        }
        else
        {
            JOptionPane.showMessageDialog( dialog, "Stock not available" );
        }

    }

    public void processQuote()
    {
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                progress.setVisible( true );   
            }
        }).start();
        if ( stockTool.translateToName( textField.getText().toString().toLowerCase() ) != null )// means textField contains symbol
        {
            progress.setVisible( true );
            textArea.setText(getQuote(textField.getText().toString().toLowerCase()));
            new Thread(new Runnable(){
                @Override
                public void run()
                {
                    progress.setVisible( false );   
                }
            }).start();
        }
        else if ( stockTool.translateToSymbol( textField.getText().toString().toLowerCase() ) != null )
        {
            textArea.setText(
                getQuote(stockTool
                .translateToSymbol( textField
                    .getText()
                    .toString()
                    .toLowerCase())));
            new Thread(new Runnable(){
                @Override
                public void run()
                {
                    progress.setVisible( false );   
                }
            }).start();
        }
        else
        {
            JOptionPane.showMessageDialog( dialog, "Stock not available" );
            new Thread(new Runnable(){
                @Override
                public void run()
                {
                    progress.setVisible( false );   
                }
            }).start();
        }
    }
    
    public JFrame getInstance()
    {
        return thisWindow;
    }
}














