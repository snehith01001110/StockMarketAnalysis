
package Forture.v1;

import javax.swing.SwingUtilities;

import Forture.v1.reg_sys.QuoteSystem;

public class Coordinator
{
    private static QuoteSystem qs;
    public void start()
    {
            qs = new QuoteSystem("Quote Search:");
    }
    
    public QuoteSystem getSystem()
    {
        return qs;
    }
    
   //UNCOMMENT TO RUN PROGRAM
    public static void main( String[] args )
    {
        new Coordinator().start();
    }
}
