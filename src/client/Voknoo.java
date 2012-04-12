package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.SwingUtilities;

import java.util.Random;



class DrawPanel extends JPanel {

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);        

        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setColor(Color.lightGray);

        for (int i = 0; i <= 1000; i++) {
            Dimension size = getSize();
            Insets insets = getInsets();

            int w = size.width - insets.left - insets.right;
            int h = size.height - insets.top - insets.bottom;

            Random r = new Random();
            int x = Math.abs(r.nextInt()) % w;
            int y = Math.abs(r.nextInt()) % h;
            g2d.drawLine(x, y, x, y);
        }

        g2d.setColor(new Color(255, 60, 33));
        g2d.drawRect(10, 15, 90, 60);
        g2d.setColor(new Color(125, 167, 116));
        g2d.fillRect(10+1, 15+1, 90-1, 60-1);
    }
}

/*
 * Co chceme udelat:
 * - menu (otevrit, ulozit, pripojit, odpojit, simulovat, nove, help)
 * - drag&drop kolecka (mista), obdelnicky(prechody)
 * - cary (hrany) automaticky prekreslovane mezi kolecka a obdelnicky
 * - skrolovaci roztazitelna plocha? fixmi musi stacit kazdemu
 * - toolbar na vyber kreslicich nastroju?
 * - rclick editace vlastnosti?
 */

/**
 * Pokusna GUI trida. Proste piskoviste na hrani.
 * Co se da se pouzije v klientovi.
 */
public class Voknoo implements ActionListener
{

	public final int DEFAULT_WIDTH = 800;
	public final int DEFAULT_HEIGTH = 600;
	
    JFrame converterFrame;
    JPanel converterPanel;
    JTextField tempCelsius;
    JLabel celsiusLabel, fahrenheitLabel, label;
    JButton convertTemp, button;

    public Voknoo() {
    	
        //Create and set up the window.
        converterFrame = new JFrame("IJA - projekt");
        JTextArea textArea = new JTextArea(5, 30);
        JScrollPane scroo = new JScrollPane(textArea);
        
        /*
        DrawPanel dpnl = new DrawPanel();
        converterFrame.add(dpnl);
        */
        
        converterFrame.setResizable( false );
        converterFrame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGTH);
        converterFrame.setLocationRelativeTo(null);
        converterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        converterFrame.add(scroo);
        
        converterFrame.setVisible(true);
    }

    /**
     * Akce na tlacitko
     */
	public void actionPerformed(ActionEvent event) {
	    //Parse degrees Celsius as a double and convert to Fahrenheit.
	    int tempFahr = (int)((Double.parseDouble(tempCelsius.getText()))
	                         * 1.8 + 32);
	    fahrenheitLabel.setText(tempFahr + " Fahrenheit");
	}   

	/**
	 * Lazy akce na tlacitko
	 */
	class DoSomething implements ActionListener {
	    public void actionPerformed(ActionEvent event) {
	        try {
	            Thread.sleep(10000);
	        } catch (InterruptedException e) {}
	    }   
	}   

	/** 
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
	    //Make sure we have nice window decorations.
	    JFrame.setDefaultLookAndFeelDecorated(true);
	
	    new Voknoo();
	}

	/**
	 * Bordel!
	 * @param args
	 */
	public static void main(String [] args)
	{
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            	//new Voknoo();
            }   
        });
	}

}