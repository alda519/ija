/**
 * IJA - Simulator Petriho siti
 *
 * Editor Petriho site, soucast klienta.
 *
 * @author xcupak04
 * @author xdujic01
 */

package client.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.GridLayout;
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
    
    /** Okna na upravu prechodu a mist. */
    private JFrame trEdit;
    private JFrame plEdit;

    /**
     * Jen nastaveni zpracovani udalosti a vytvoreni elipsicky
     */
    public Editor(PetriNet net) {
    	this.petrinet = net;
    	setPreferredSize(new Dimension(width, height));

        MovingAdapter ma = new MovingAdapter();

        //setBackground(new Color(220, 220, 220));
        addMouseMotionListener(ma);
        addMouseListener(ma);

        /** Toolbar */
        JComboBox modeSel = new JComboBox( new String [] {"Editace", "Kreslení míst",
        		"Kreslení přechodů", "Editace hran", "Rušení objektů",} );
        modeSel.addActionListener(new ChangeMode());
        add(modeSel);
        JButton editButton = new JButton("Upravit");
        editButton.addActionListener(new NetPropertyEditor());
        add(editButton);

        add(new JButton("Simulovat krok"));
        add(new JButton("Simulovat úplně"));

        //setDoubleBuffered(true);
        reloadNet();
    }

    /**
     * Tato metoda procte sit, kterou ma editovat a vytvori pro ni graficke prvky,
     * hodi se na restart zobrazeni
     */
    protected void reloadNet() {
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
        g2d.setFont(new Font(Theme.FONT, Font.PLAIN, Theme.FONT_SIZE));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.setColor(new Color(Theme.ARC_IN_R, Theme.ARC_IN_G, Theme.ARC_IN_B));
        g2d.setStroke(new BasicStroke(4));
        // vstupni hrany tluste cervene
        for(GTransition gt : transitions) {
        	for(GArc ga : gt.getArcsIn()) {
        		g2d.draw(ga);
        	}
        }
        // vystupni tenke, modre
    	g2d.setColor(new Color(Theme.ARC_OUT_R, Theme.ARC_OUT_G, Theme.ARC_OUT_B));
        g2d.setStroke(new BasicStroke(2));
        for(GTransition gt : transitions) {
        	for(GArc ga : gt.getArcsOut()) {
        		g2d.draw(ga);
        	}
        }
        // mista
        g2d.setColor(new Color(Theme.PLACE_R, Theme.PLACE_G, Theme.PLACE_B));
        for(GPlace gp : places) {
        	g2d.fill(gp); 
        }
        // prechody
        g2d.setColor(new Color(Theme.TRANSITION_R, Theme.TRANSITION_G, Theme.TRANSITION_B));
        for(GTransition gt : transitions) {
        	g2d.fill(gt);
        }
        // popisky
        g2d.setColor(new Color(Theme.TEXT_R, Theme.TEXT_G, Theme.TEXT_B));
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
     * Nastavuje, co se deje, kdyz se hybe mysi. 
     */
    class MovingAdapter extends MouseAdapter {

        private int x;
        private int y;

        /**
         * Obsluha kliknuti mysi do site.
         */
        public void mousePressed(MouseEvent e) {
            x = e.getX();
            y = e.getY();
            
            selTrans = null;
            selPlace = null;
            // pokud jsem kliknul na elipsicku, tak si ji ulozim
            for(GPlace p : places) {
            	if(p.isHit(x, y)) {
            		selPlace = p;
            	}
            }
            for(GTransition t : transitions) {
            	if(t.isHit(x, y)) {
            		selTrans = t;
            	}
            }
            
            if(e.getButton() == MouseEvent.BUTTON3) {
            	System.out.println("button #");
            	if(selTrans != null)
            		editTransition();
            	else if(selPlace != null)
            		editPlace();
            	return;
            }

            // Na zaklade zvolene polozky v menu se deji ruzne veci..
            switch(action) {
            	case EDIT:
            		return;// Nedeje se nic, pravdepodobne jde jen o posun
            	case ADDPLACE:
            		if(selTrans != null || selPlace != null) {
            			return;
            		}
            		Place newP = new Place(0);
                    newP.x = x - 40;
                    newP.y = y - 40;
                    petrinet.addPlace(newP);
            		break;
            	case ADDTRANSITION:
            		if(selTrans != null || selPlace != null) {
            			return;
            		}
            		Transition newT = new Transition();
                    newT.x = x - 80;
                    newT.y = y - 40;
                    petrinet.addTransition(newT);
            		break;
            	case ARCEDIT:
                    // tohle bude kreslit hrany kde nejsou a rusit kde jsou
            		break;
            	case DELETE:
            		// Tohle bude killit prechody a mista
            		if(selTrans != null)
            			petrinet.removeTransition(selTrans.getTransition());
            		else if(selPlace != null)
            			petrinet.removePlace(selPlace.getPlace());
            		selTrans = null;
                    selPlace = null;
            		break;
            }
            
            reloadNet();
            repaint();
        }
        
        /**
         * Editacni okno na prechody.
         */
        public void editTransition() {
        	// pokud uz je nejaky prechod upravovan, nebude se nic delat
        	if(trEdit != null)
        		return;
        	// pridavani.odebirani strazi
        	// nastaveni vyrazu
        	trEdit = new JFrame("Editace přechodu");
        	trEdit.setSize(300, 300);
        	trEdit.setLayout(new GridLayout(0, 1));
        	//trEdit.setAlwaysOnTop(true);
        	trEdit.setLocationRelativeTo(null);

			JLabel exprLab = new JLabel("Operace: ");
			JTextField expr = new JTextField("expr", 20);
			trEdit.add(exprLab);
			trEdit.add(expr); //
			//trEdit.add(new JSeparator());
			
        	JLabel newGLab = new JLabel("Nová stráž:");
        	trEdit.add(newGLab);
        	JTextField src  = new JTextField("src", 5);
        	trEdit.add(src);
        	JComboBox opList = new JComboBox(new String [] { "<", ">", "<=", ">=", "==", "!="} );
        	trEdit.add(opList);
        	JTextField dst = new JTextField("dst", 5);
        	trEdit.add(dst);

			JButton addButton = new JButton("Přidat stráž");
			trEdit.add(addButton);
			//trEdit.add(new JSeparator());

        	JComboBox guards = new JComboBox(new String [] {"A", "B", "C", "D", "E", "F"});
        	//guards.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        	//guards.setLayoutOrientation(JList.VERTICAL);
        	trEdit.add(new JScrollPane(guards));

			JButton remButton = new JButton("Odebrat stráž");
			trEdit.add(remButton);
			//trEdit.add(new JSeparator());
			
			JButton okButton = new JButton("OK");
			trEdit.add(okButton);
			
			//trEdit.pack();
			trEdit.setResizable(false);
			trEdit.setVisible(true);
        }
 
        /**
         * Editacni okno na mista.
         */
        public void editPlace() {
        	// pokud uz je nejake misto upravovano, nebude se nic delat
        	if(plEdit != null)
        		return;
        	// pridavani/odebirani cisel
        	plEdit = new JFrame("Úprava místa");
        	plEdit.setLayout(new GridLayout(0, 1));
        	plEdit.setSize(300, 160);
        	plEdit.setLocationRelativeTo(null);
        	JLabel newVLab = new JLabel("Nová hodnota:");
        	plEdit.add(newVLab);
        	JTextField newVal  = new JTextField("val", 5);
        	plEdit.add(newVal);
        	JButton addButton = new JButton("Přidat hodnotu");
			plEdit.add(addButton);

			JComboBox values = new JComboBox(new String [] {"1", "5", "10", "11"});
        	plEdit.add(values);
        	JButton remButton = new JButton("Odebrat hodnotu");
			plEdit.add(remButton);

        	JButton okButton = new JButton("OK");
			plEdit.add(okButton);
        	plEdit.setVisible(true);
        }

        /**
         * Pri tazeni mysi se posouva vybrana komponenta site.
         */
        // kompromis mezi rychle x pekne
        //public void mouseReleased(MouseEvent e) {
        public void mouseDragged(MouseEvent e) {    		

            // kdyz neni nic vybrano, neni co posouvat
        	if(selPlace == null && selTrans == null)
        		return;
        	// posouvat pri editaci hran neni mozne
        	if(action == Toolbar.ARCEDIT)
        		return;
        	
        	int dx = e.getX() - x;
            int dy = e.getY() - y;

        	if(selPlace != null) {
        		selPlace.addX(dx);
                selPlace.addY(dy);
        	} else if(selTrans != null) {
        		selTrans.addX(dx);
        		selTrans.addY(dy);
        	}
            repaint();
            x += dx;
            y += dy;
        }
        
        /**
         * Dokonceni nove hrany.
         */
        public void mouseReleased(MouseEvent e) {
        	int x = e.getX();
            int y = e.getY();
        	// jen pri kresleni hrany
        	if(action != Toolbar.ARCEDIT)
        		return;
        	GPlace endPlace= null;
        	GTransition endTrans = null;
        	// vyhledani, kde hrana konci
        	for(GPlace p : places) {
            	if(p.isHit(x, y)) {
            		endPlace = p;
            	}
            }
            for(GTransition t : transitions) {
            	if(t.isHit(x, y)) {
            		endTrans = t;
            	}
            }
            // pridat nebo zrusit hranu
            if(selTrans != null && endPlace != null) {
            	if(!petrinet.removeArc(selTrans.getTransition(), endPlace.getPlace(), false)) {
            		String name = JOptionPane.showInputDialog(null, "Hodnota/proměnná na hraně", "Nová hrana", JOptionPane.PLAIN_MESSAGE);
            		if(name != null) {
            			petrinet.addArc(selTrans.getTransition(), endPlace.getPlace(), false, name);
            		}
            	}
            } else if (selPlace != null && endTrans != null) {
            	if(!petrinet.removeArc(endTrans.getTransition(), selPlace.getPlace(), true)) {
            		String name = JOptionPane.showInputDialog(null, "Hodnota/proměnná na hraně", "Nová hrana", JOptionPane.PLAIN_MESSAGE);
            		if(name != null) {
            			petrinet.addArc(endTrans.getTransition(), selPlace.getPlace(), true, name);
            		}
            	}
            }
            reloadNet();
            repaint();
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

    protected JTextField newName;
    protected JTextField newDesc;
    protected JFrame netEditor;
    /**
     * Obsluha udalosti zmeny vlastnosti site. Dialogove okno.
     */
    class NetPropertyEditor implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			netEditor = new JFrame("Upravit síť");
			netEditor.setLocationRelativeTo(null);
			netEditor.setSize(300, 200);
			netEditor.setLayout(new GridLayout(0, 1));
			newName = new JTextField(petrinet.getName());
			JTextField cnstAuth = new JTextField(petrinet.getAuthor());
			newDesc = new JTextField(petrinet.getDescription());
			netEditor.add(new JLabel("Jméno sítě:"));
			netEditor.add(newName);
			netEditor.add(new JLabel("Autor: "));
			cnstAuth.setEnabled(false);
			netEditor.add(cnstAuth);
			netEditor.add(new JLabel("Popis sítě:"));
			netEditor.add(newDesc);
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new NetPropertySet());
			netEditor.add(okButton);
			netEditor.setVisible(true);
		}
    }
    /**
     * Obsluha udalosti zmeny vlastnosti site. Potvrzeni zmeny.
     */
    class NetPropertySet implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			petrinet.setDescription(newDesc.getText());
			petrinet.setName(newName.getText());
			netEditor.dispose();
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
