package client.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import petrinet.*;

import java.util.*;


public class Editor extends JPanel {

    private PetriNet petrinet;
    
    /** Minimalni a autualni rozmery plochy */
    private final int MINWIDTH = 800;
    private final int MINHEIGHT = 800;
    private int width = MINWIDTH;
    private int height = MINHEIGHT;

    /* seznamy mist a prechodu */
    private List<GPlace> places = new ArrayList<GPlace>();
    private List<GTransition> transitions = new ArrayList<GTransition>();
    
    private GPlace selPlace;
    private GTransition selTrans;
    
    /**
     * Jen nastaveni zpracovani udalosti a vytvoreni elipsicky
     */
    public Editor(PetriNet net) {
    	this.petrinet = net;
    	setPreferredSize(new Dimension(width, height));
    	
    	// ke vsem komponentam ze site vygenerovat graficke prdiky
    	// a potom upravovat oboji?
    	
        MovingAdapter ma = new MovingAdapter();

        //setBackground(new Color(220, 220, 220));
        addMouseMotionListener(ma);
        addMouseListener(ma);

        //setDoubleBuffered(true);
        reloadNet();
    }

    /**
     * Tato metoda procte sit, kterou ma editovat a vytvori pro ni graficke prvky
     * hodi se na restart zobrazeni
     */
    public void reloadNet() {
    	for(Place p : this.petrinet.getPlaces()) {
    		places.add(new GPlace(p));
    	}
    	for(Transition t : this.petrinet.getTransitions()) {
    		transitions.add(new GTransition(t));
    	}
    	//TODO naloadovat cary
    }
    
    /**
     * Sem musi patrit vykresleni veskereho neporadku, tzn vsech elipsicek.
     */
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(130, 140, 250));
        g2d.setStroke(new BasicStroke(2));
        // TODO: vykreslit cary
        for(GPlace gp : places) {
        	g2d.fill(gp);        	
        }
        for(GTransition gt : transitions) {
        	g2d.fill(gt);
        }
        //g2d.draw(line);
    }

    /**
     * Nastavuje, co se deje, kdyz se hejbe mysou. 
     */
    class MovingAdapter extends MouseAdapter {

        private int x;
        private int y;

        public void mousePressed(MouseEvent e) {
            x = e.getX();
            y = e.getY();
            
            selTrans = null;
            selPlace = null;
            // pokud jsem kliknul na elipsicku, tak si ji ulozim
            for(GPlace p : places) {
            	if(p.isHit(x, y)) {
            		selPlace = p;
            		return;
            	}
            }
            for(GTransition t : transitions) {
            	if(t.isHit(x, y)) {
            		selTrans = t;
            		return;
            	}
            }
            // kdyz kliknu do pole, tak se prida kolesko
            //ellipses.add(new ZEllipse(x-20,y-20,40,40,255));
            //repaint(); // repaint po pridani
            
            // TODO: pridat kresleni mist atp.
        }

        // kompromis mezi rychle x pekne
        //public void mouseReleased(MouseEvent e) {
        public void mouseDragged(MouseEvent e) {

        	int dx = e.getX() - x;
            int dy = e.getY() - y;
            
        	if(selPlace == null && selTrans == null)
        		return;

        	if(selPlace != null) {
        		selPlace.addX(dx);
                selPlace.addY(dy);
        	}
        	if(selTrans != null) {
        		selTrans.addX(dx);
        		selTrans.addY(dy);
        	}
            repaint();
            x += dx;
            y += dy;
        }
    }

    public void saveNet(File file) {
    	try {
	    	FileWriter out = new FileWriter(file);
	        OutputFormat format = OutputFormat.createPrettyPrint();
	        XMLWriter writer = new XMLWriter(out, format);
	        writer.write(this.petrinet.toXML());
	    	out.close();
    	} catch (IOException e) {
    		System.err.println("Nelze soubor ulozit.");
    	}
    }
    
    /**
     * Vyvotereni okna atd.
     */
    public static void main(String[] args) {

    	// nacist sit z XML
    	PetriNet net = PetriNet.PetriNetFactory(new File("examples/net1.net"));
    	// predat editoru
    	
        JFrame frame = new JFrame("Moving and Scaling");
        // obsah se obali skrolovatkem a muze se kreslit vsude, pro jednoduchost asi vynechame zatim
        JScrollPane scroo = new JScrollPane(new Editor(net));
        JScrollPane scroo2 = new JScrollPane(new Editor(net));

        JTabbedPane tabbiky = new JTabbedPane();
        tabbiky.addTab("zalozka 1", scroo);
        tabbiky.addTab("zalozka 2", scroo2);

        frame.add(tabbiky);
        
        // jak na ty taby blbe?
        
        //frame.add(new MovingScaling());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
	
}
