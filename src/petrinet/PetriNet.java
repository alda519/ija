package petrinet;

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

	/** Konstruktor uplne prazdne site. */
	public PetriNet() {
	}

    /**
     * Vytvoreni nove site.
     * @param name nazev site
     * @param author jmeno autora
     * @param description popis site
     */
    public PetriNet(String name, String author, String description) {
	    this.author = author;
	    this.name = name;
	    this.author = author;
    }

    /**
     * Konstruktor site z XML popisu.
     * @param xml
     */
    public PetriNet(String xml) {
    	try {
    		Document doc = DocumentHelper.parseText(xml);
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
    			Place newPlace = new Place(Integer.parseInt(place.attributeValue("id")));
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
    	return new PetriNet(net);
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
	    } catch (DocumentException e) {
	    	System.err.println("Nejde nacist soubor.");
	    	e.printStackTrace();
	    	return null;
	    }
    	return new PetriNet(doc.asXML());
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

    // pridavani mist
    // pridavani prechodu
    public void addTransition(Transition t) {
    	this.transitions.add(t);
    }
    // pridavani hran
    // odebirani vseho

 
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


    // zase testovani kod na nic!
    public static void main(String [] args) {
    	
    	PetriNet net = PetriNetFactory(new File("examples/net1.net"));
    	
    	Document doc = net.toXML();
    	System.out.print(doc.asXML());
    }
}