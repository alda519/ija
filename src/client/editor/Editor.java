package client.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    protected PetriNet petrinet;
    
    protected Toolbar action = Toolbar.EDIT;
    
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

        /** Toolbar */
        JComboBox modeSel = new JComboBox( new String [] {"Editace", "Kreslení míst",
        		"Kreslení přechodů", "Editace hran", "Rušení objektů",} );
        modeSel.addActionListener(new ChangeMode());
        add(modeSel);
        add(new JButton("Simulovat krok"));
        add(new JButton("Simulovat úplně"));
        
        //setDoubleBuffered(true);
        reloadNet();
    }

    /**
     * Tato metoda procte sit, kterou ma editovat a vytvori pro ni graficke prvky
     * hodi se na restart zobrazeni
     */
    public void reloadNet() {
    	places = new ArrayList<GPlace>();
    	transitions = new ArrayList<GTransition>();
    	for(Place p : this.petrinet.getPlaces()) {
    		places.add(new GPlace(p));
    	}
    	for(Transition t : this.petrinet.getTransitions()) {
    		GTransition ngt = new GTransition(t);
    		transitions.add(ngt);
    		// prochazi se vsechny hrany kazdeho prechodu
    		for(Arc a : t.getInArcs()) {
    			// vytvori se cara
    			GArc na = new GArc(t.x + 80, t.y + 40, a.getPlace().x + 40, a.getPlace().y + 40, a.getName());
    			// ted hledam GPlace, ktery obsahuje a.getPlace()
    			for(GPlace gpl : this.places) {
    				if(gpl.contains(a.getPlace())) {
    					// do togo gpalcu tu hranu taky pridam
    					gpl.addOutArc(na);
    				}
    			}
    			ngt.addArcIn(na);
    		}
    		for(Arc a : t.getOutArcs()) {
    			// vytvori se cara
    			GArc na = new GArc(a.getPlace().x + 40, a.getPlace().y + 40, t.x + 80, t.y + 40, a.getName());
    			// ted hledam GPlace, ktery obsahuje a.getPlace()
    			for(GPlace gpl : this.places) {
    				if(gpl.contains(a.getPlace())) {
    					// do togo gpalcu tu hranu taky pridam
    					gpl.addInArc(na);
    				}
    			}
    			ngt.addArcOut(na);
    		} 
    	}
    }
    
    /**
     * Prekresleni celeho okna.
     */
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.setColor(new Color(255, 0, 0));
        g2d.setStroke(new BasicStroke(4));
        // vstupni hrany tluste cervene
        for(GTransition gt : transitions) {
        	for(GArc ga : gt.getArcsIn()) {
        		g2d.draw(ga);
        	}
        }
        // vystupni tenke, modre
    	g2d.setColor(new Color(0, 0, 255));
        g2d.setStroke(new BasicStroke(2));
        for(GTransition gt : transitions) {
        	for(GArc ga : gt.getArcsOut()) {
        		g2d.draw(ga);
        	}
        }
        // mista a prechody
        g2d.setColor(new Color(130, 140, 250));
        for(GPlace gp : places) {
        	g2d.fill(gp); 
        }
        for(GTransition gt : transitions) {
        	g2d.fill(gt);
        }
        // popisky
        g2d.setColor(new Color(0, 0, 0));
        for(GPlace gp : places) {
        	// hodnoty v mistech
        	g2d.drawString(gp.getValues(), gp.x+5, gp.y+40); 
        }
        for(GTransition gt : transitions) {
        	// straze
        	g2d.drawString(gt.getGuards(), gt.x+5, gt.y+30);
        	// operace
        	g2d.drawString(gt.getExpr(), gt.x+5, gt.y+60);
        	// popisky hran
        	for(GArc ga : gt.getArcsIn()) {
        		// nad stred vypsat
        		g2d.drawString(ga.desc, (ga.x1 + ga.x2) / 2, (ga.y1 + ga.y2) / 2 - 10); 
        	}
        	for(GArc ga : gt.getArcsOut()) {
        		// pod stred vypsat
        		g2d.drawString(ga.desc, (ga.x1 + ga.x2) / 2, (ga.y1 + ga.y2) / 2 + 20);
        	}
        }
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
            
            switch(action) {
            	case EDIT:
            		break;
            	case ADDPLACE:
            		Place newP = new Place(1);
                    newP.x = x - 40;
                    newP.y = y - 40;
                    petrinet.addPlace(newP);
            		break;
            	case ADDTRANSITION:
            		Transition newT = new Transition();
                    newT.x = x - 80;
                    newT.y = y - 40;
                    petrinet.addTransition(newT);
            		break;
            	case ARCEDIT:
            		break;
            	case DELETE:
            		break;
            }
            
            reloadNet();
            repaint();
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

    /**
     * Zmena editacniho modu vyberem z listu
     */
    class ChangeMode implements ActionListener {
	    public void actionPerformed(ActionEvent e) {
	    	JComboBox cb = (JComboBox)e.getSource();
	        String name = (String)cb.getSelectedItem();
	    	action = Toolbar.getAction(name);
	    }
    }
    
    
    
    /**
     * Ulozeni prave editovane site.
     * @param file Soubor k ulozeni
     */
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


    // bordel na smazani pomalu
    public static void main(String[] args) {

    	// nacist sit z XML
    	PetriNet net = PetriNet.PetriNetFactory(new File("examples/net1.xml"));
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
