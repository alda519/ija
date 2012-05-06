/**
 * IJA - Simulator Petriho siti
 *
 * Trida reprezentujici Petriho sit.
 *
 * @author xcupak04
 * @author xdujic01
 */

package petrinet;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

import org.dom4j.*;
import org.dom4j.io.*;

public class PetriNet {

	/** Seznamy vsech mist a prechodu v siti */
	protected List<Place> places = new ArrayList<Place>();
	protected List<Transition> transitions = new ArrayList<Transition>();

	/** Atributy site */
	protected String author = "";
	protected String name = "";
	protected String description = "";
	protected String version = "";
	protected List<String> simulations = new ArrayList<String>();

	/** pocitadlo mist, slouzi k pridelovani novych cisel */
	protected int placeCnt = -1;

	/** Konstruktor uplne prazdne site. */
	public PetriNet() {
	}

    /**
     * Vytvoreni nove site.
     * @param name nazev site
     * @param version verze site
     */
    public PetriNet(String name, String version) {
	    this.name = name;
	    this.version = version;
    }

    /**
     * Konstruktor site z XML popisu.
     * @param xml
     */
    public PetriNet(String xml) throws DocumentException {
  		this(DocumentHelper.parseText(xml));
    }
    
    public PetriNet(Document doc) {
    	try {
    		Element root = doc.getRootElement();
    		if(! root.getName().equals("petrinet"))
    			throw new DocumentException();
    		// vlastnosti site
    		this.name = root.attributeValue("name");
    		this.author = root.attributeValue("author");
    		this.version = root.attributeValue("version");
    		this.description = root.attributeValue("description");
    		// mista
    		List<Element> places =  root.elements("place");
    		for(Element place : places) {
    			// vytvoreni mista
    			int id = Integer.parseInt(place.attributeValue("id"));
    			if(id > this.placeCnt)
    				this.placeCnt = id;
    			Place newPlace = new Place(id);
    			// nastaveni umisteni
    			newPlace.x = Integer.parseInt(place.attributeValue("x"));
    			newPlace.y = Integer.parseInt(place.attributeValue("y"));
    			// misto muze obsahovat spousty hodnot
    			List <Element> values = place.elements("value");
    			for(Element value : values) {
    				newPlace.addValue(Integer.parseInt(value.getText()));
    			}
    			this.places.add(newPlace);
    		}
    		// prechody
    		List<Element> transitions = root.elements("transition");
    		for(Element transition : transitions) {
    			// vytvoreni noveho prechodu
    			Transition newTransition = new Transition();
    			// nastaveni polohy
    			newTransition.x = Integer.parseInt(transition.attributeValue("x"));
    			newTransition.y = Integer.parseInt(transition.attributeValue("y"));
    			// nastaveni vystupni operace
    			newTransition.setExpr(transition.attributeValue("expr"));
    			// nacteni strazi
    			List <Element> guards = transition.elements("guard");
    			for(Element guard : guards ) {
    				newTransition.addGuard(new Condition(guard.attributeValue("src"), guard.attributeValue("op"), guard.attributeValue("dst")));
    			}
    			// nacteni hran
    			List <Element> arcs = transition.elements("arc");
    			for(Element arc : arcs) {
    				Arc newArc;
    				Place p = findPlace(Integer.parseInt(arc.attributeValue("place")));
    				// bud je hrana s promennou, nebo konstantou
    				try {
    					int v = Integer.parseInt(arc.attributeValue("name"));
    					newArc = new ConstArc(p, v);
    				} catch (NumberFormatException e) {
    					newArc = new VarArc(p, arc.attributeValue("name"));
    				}
    				// pridat podle io
    				String io = arc.attributeValue("io");
    				if(io.equals("in"))
    					newTransition.addInArc(newArc);
    				else if(io.equals("out"))
    					newTransition.addOutArc(newArc);
    				else
    					throw new DocumentException("Neplatna hrana");
    			}
    			this.transitions.add(newTransition);
    		}
    		// vse ostatni se zahazuje
    	} catch (DocumentException e) {
    		System.err.println("Neplatny dokument se siti.");
    	}
    }


    /**
     * Staticka metoda, ktera vraci novou sit vzniklou analzou retezce.
     * @param net retezec s xml popisem site
     * @return vraci novou sit
     */
    public static PetriNet PetriNetFactory(String net) {
    	try {
    		return new PetriNet(net);
    	} catch (DocumentException e) {
    		return null;
    	}
    }

    /**
     * Staticka metoda, ktera vraci novou sit vznikla analyzou souboru.
     * @param file soubor s xml popisem site
     * @return vraci novou sit
     */
    public static PetriNet PetriNetFactory(File file) {
    	Document doc;
    	try {
	    	SAXReader xmlReader = new SAXReader();
	    	doc = xmlReader.read(file);
	    	return new PetriNet(doc.asXML());
	    } catch (DocumentException e) {
	    	System.err.println("Nejde nacist soubor.");
	    	e.printStackTrace();
	    	return null;
	    }
    }

    /**
     * Vyhleda misto podle jeho identifikatoru
     * @param id hledany identifikator
     * @return Place pokud nalezeno, jinak null
     */
    public Place findPlace(int id) {
    	for(Place place : this.places) {
    		if(id == place.getId())
    			return place; 
    	}
    	return null;
    }

    /**
     * Vraci seznam vsech mist site.
     * @return seznam mist
     */
    public List<Place> getPlaces() {
    	return this.places;
    }

    /**
     * Vraci seznam vsech prechodu site.
     * @return seznam prechodu
     */
    public List<Transition> getTransitions() {
    	return this.transitions;
    }

    /** Prida do site misto */
    public void addPlace(Place p) {
    	// nastaveni unikatniho id, tuto metodu stejne pouziva jen editor
    	p.setId(++this.placeCnt);
    	this.places.add(p);
    }

    /** Pridavani prechodu do site */
    public void addTransition(Transition t) {
    	this.transitions.add(t);
    }

    /** Ostraneni prechodu */
    public void removeTransition(Transition t) {
    	this.transitions.remove(t);
    }
    
    /** Ostraneni mista */
    public void removePlace(Place p) {
    	// TODO: neni tak jednoduche, je treba oddelat vsechny hrany z/do tohoto mista
    	for(Transition t : this.transitions) {
    		for(Iterator <Arc> iter = t.getInArcs().iterator(); iter.hasNext(); ) {
    			Arc a = iter.next();
    			if(a.getPlace() == p) {
    				iter.remove();
    			}
    		}
    		for(Iterator <Arc> iter = t.getOutArcs().iterator(); iter.hasNext(); ) {
    			Arc a = iter.next();
    			if(a.getPlace() == p) {
    				iter.remove();
    			}
    		}
    	}
    	this.places.remove(p);
    }
    
    /**
     * Pridani vstupni/vystupni hrany z prechodu t do mista p.
     * @param t prechod odkud/kam hrana vede
     * @param p misto odkud/kam hrana vede
     * @param input true pokud ma byt hrana pro prechod vstupni
     * @param name hodnota na hrane
     */
    public void addArc(Transition t, Place p, boolean input, String name) {
    	if(input) {
    		try {
    			int i = Integer.parseInt(name);
    			t.addInArc(new ConstArc(p, i));
    		} catch (NumberFormatException e) {
    			t.addInArc(new VarArc(p, name));
    		}
    	} else {
    		try {
    			int i = Integer.parseInt(name);
    			t.addOutArc(new ConstArc(p, i));
    		} catch (NumberFormatException e) {
    			t.addOutArc(new VarArc(p, name));
    		}
    	}
    }

    /**
     * Odebere hranu z mista p do prechodu t
     * @param p misto
     * @param t prechod
     * @param input true, pokud ma byt dana hrana vstupni
     * @return vraci true, pokud hrana byla odebrana, jinak false
     */
    public boolean removeArc(Transition t, Place p, boolean input) {
    	Arc todel = null;
    	// podle toho, zda je vstupni nebo vystupni se hleda hrana
    	for(Arc a : (input)?t.getInArcs():t.getOutArcs()) {
    		if(a.getPlace() == p) {
    			todel = a;
    			break;
    		}
    	}
    	return t.removeArc(todel);
    }

    /**
     * Nastaveni popisu site.
     * @param d novy popisek
     */
    public void setDescription(String d) {
    	this.description = d;
    }
    /**
     * Nastaveni nazvu site.
     * @param n novy nazev site
     */
    public void setName(String n) {
    	this.name = n;
    }
    /**
     * Ziskani popisu site.
     * @return vraci popis site
     */
    public String getDescription() {
    	return this.description;
    }
    /**
     * Ziskani jmena site.
     * @return vraci jmeno site
     */
    public String getName() {
    	return this.name;
    }
    /**
     * Ziskani jmena autora site.
     * @return jmeno autora
     */
    public String getAuthor() {
    	return this.author;
    }
    /**
     * Nastaveni jmena autora.
     * @param auth jmeno autora site
     */
    public void setAuthor(String auth) {
    	this.author = auth;
    }

    /**
     * Ziskani verze.
     * @return vraci verzi
     */
    public String getVersion() {
    	return this.version;
    }
    /**
     * Nastaveni verze.
     */
    public void setVersion(String v) {
    	this.version = v;
    }

    /**
     * Pokud udelat krok simulace.
     */
    public boolean stepSim() {
    	for(Transition t : transitions) {
    		if(t.tryTransition()) {
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * Vygenerovani XML popisu cele site.
     */
    public Document toXML() {
	    Document doc = DocumentHelper.createDocument();
	    // vytvoreni root elementu
	    Element root = doc.addElement( "petrinet" );
	    root.addAttribute("author", this.author);
	    root.addAttribute("description", this.description);
	    root.addAttribute("version", this.version);
	    root.addAttribute("name", this.name);
	    // pridani vsech prechodu
	    for(Transition t : this.transitions) {
	    	t.toXML(root);
	    }
	    // pridani vsech mist
	    for(Place p : this.places) {
	    	p.toXML(root);
	    }
	    // a nakonec seznam simulaci
	    Element simulations = root.addElement("simulations");
	    for(String s : this.simulations) {
	    	simulations.addElement("simulation").addText(s);
	    }
	    return doc;
    }


    // TODO: zase testovani kod na nic! smazat
    public static void main(String [] args) {
    	
    	PetriNet net = PetriNetFactory(new File("examples/net1.xml"));
    	
    	Document doc = net.toXML();
    	System.out.print(doc.asXML());
    }
}