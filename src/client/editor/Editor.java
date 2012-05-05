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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

	/** Editovana sit */
    protected PetriNet petrinet;

    /** Mod editoru */
    protected Toolbar action = Toolbar.EDIT;

    /** Minimalni a autualni rozmery plochy */
    protected final int MINWIDTH = 800;
    protected final int MINHEIGHT = 800;
    protected int width = MINWIDTH;
    protected int height = MINHEIGHT;

    /* seznamy mist a prechodu */
    protected List<GPlace> places = new ArrayList<GPlace>();
    protected List<GTransition> transitions = new ArrayList<GTransition>();

    protected GPlace selPlace;
    protected GTransition selTrans;

    /** Okna na upravu prechodu a mist. */
    protected JFrame trEdit;
    protected JFrame plEdit;

    /** true pokud je povolena simulace, jinak false */
    protected boolean simulationEnabled = true;

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

        JTextField newExpr; // input s novovou funkci
        JTextField src; // vstup pro nove straze
        JComboBox opList;
        JTextField dst;
        JComboBox guards; // vyber straze k odstraneni
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
        	trEdit.setSize(300, 320);
        	trEdit.setLayout(new GridLayout(0, 1));
        	//trEdit.setAlwaysOnTop(true);
        	trEdit.setLocationRelativeTo(null);

			JLabel exprLab = new JLabel("Operace: ");
			newExpr = new JTextField(selTrans.getExpr(), 20);
			trEdit.add(exprLab);
			trEdit.add(newExpr); //
			trEdit.add(new JSeparator());

        	JLabel newGLab = new JLabel("Nová stráž:");
        	trEdit.add(newGLab);
        	src  = new JTextField("", 5);
        	trEdit.add(src);
        	opList = new JComboBox(new String [] { "<", ">", "<=", ">=", "==", "!="} );
        	trEdit.add(opList);
        	dst = new JTextField("", 5);
        	trEdit.add(dst);
			JButton addButton = new JButton("Přidat stráž");
			addButton.addActionListener(new NewGuard());
			trEdit.add(addButton);
			trEdit.add(new JSeparator());

			guards = new JComboBox( selTrans.getTransition().getListGuards() );
        	trEdit.add(guards);

			JButton remButton = new JButton("Odebrat stráž");
			remButton.addActionListener(new DropGuard());
			trEdit.add(remButton);
			trEdit.add(new JSeparator());

			JButton okButton = new JButton("OK");
			okButton.addActionListener(new SetTransitionExpr());
			trEdit.add(okButton);

			//trEdit.pack();
			trEdit.setResizable(false);
			trEdit.setVisible(true);

			trEdit.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					trEdit = null;
			    }
			});
        }
        /** Obsluha nastaveni vyrazu v prechodu */
        class SetTransitionExpr implements ActionListener {
        	public void actionPerformed(ActionEvent e) {
        		selTrans.getTransition().setExpr(newExpr.getText());
        		trEdit.dispose();
        		trEdit = null;
        		repaint();
        	}
        }
        /** Obsluha pridani straze prechodu */
        class NewGuard implements ActionListener {
        	public void actionPerformed(ActionEvent e) {
        		Condition c = new Condition(src.getText(), (String)opList.getSelectedItem() ,dst.getText());
        		selTrans.getTransition().addGuard(c);
        		guards.addItem(c);
        		repaint();
        	}
        }
        /** Obsluha odstraneni straze prechodu */
        class DropGuard implements ActionListener {
        	public void actionPerformed(ActionEvent e) {
        		Condition c = (Condition) guards.getSelectedItem();
        		if(c != null) {
        			selTrans.getTransition().dropGuard(c);
        			guards.removeItem(c);
        		}
        		repaint();
        	}
        }
        
 
        JTextField newVal; // input pole s hodnout k pridani
        JComboBox values; // vyber hodnoty k odebrani
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
        	plEdit.setSize(300, 200);
        	plEdit.setLocationRelativeTo(null);
        	JLabel newVLab = new JLabel("Nová hodnota:");
        	plEdit.add(newVLab);
        	newVal  = new JTextField("", 5);
        	plEdit.add(newVal);
        	JButton addButton = new JButton("Přidat hodnotu");
			plEdit.add(addButton);
			addButton.addActionListener(new NewValue());
			plEdit.add(new JSeparator());

			values = new JComboBox( selPlace.getPlace().getListValues() );
        	plEdit.add(values);
        	JButton remButton = new JButton("Odebrat hodnotu");
        	remButton.addActionListener(new DropValue());
			plEdit.add(remButton);
			plEdit.add(new JSeparator());

        	JButton okButton = new JButton("OK");
			plEdit.add(okButton);
			okButton.addActionListener(new PlaceEditEnd());
        	plEdit.setVisible(true);

        	plEdit.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					plEdit = null;
			    }
			});
        }
        /** Obsluha ukonceni editace mista */
        class PlaceEditEnd implements ActionListener {
        	public void actionPerformed(ActionEvent e) {
        		plEdit.dispose();
        		plEdit = null;
        		repaint();
        	}
        }
        /** Obsluha pridani straze prechodu */
        class NewValue implements ActionListener {
        	public void actionPerformed(ActionEvent e) {
        		try {
        			Integer v = Integer.parseInt(newVal.getText());
        			selPlace.getPlace().addValue(v);
        			values.addItem(v);
        		} catch (NumberFormatException ex) {}
        		repaint();
        	}
        }
        /** Obsluha odstraneni straze prechodu */
        class DropValue implements ActionListener {
        	public void actionPerformed(ActionEvent e) {
        		Integer v = (Integer)values.getSelectedItem();
        		if(v != null) {
        			selPlace.getPlace().removeValue(v);
        		}
        		values.removeItem(v);
        		repaint();      			
        	}
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

    /**
     * Ziskani petriho site.
     * @return vraci upravovanou petriho sit.
     */
    public PetriNet getNet() {
    	return this.petrinet;
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

			netEditor.add(new JLabel("Jméno sítě:"));
			newName = new JTextField(petrinet.getName());
			netEditor.add(newName);

			netEditor.add(new JLabel("Autor: "));
			JTextField cnstAuth = new JTextField(petrinet.getAuthor());
			cnstAuth.setEnabled(false);
			netEditor.add(cnstAuth);

			netEditor.add(new JLabel("Popis sítě:"));
			newDesc = new JTextField(petrinet.getDescription());
			netEditor.add(newDesc);

			netEditor.add(new JLabel("Verze sítě:"));
			JTextField cnstVers = new JTextField(petrinet.getVersion());
			cnstVers.setEnabled(false);
			netEditor.add(cnstVers);

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
