package petrinet;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;

import java.io.*;
import org.dom4j.*;
import org.dom4j.dom.*;
import org.dom4j.io.*;

public class PetriNet {

	/** Seznamy vsech mist a prechodu v siti */
	protected List<Place> places = new ArrayList<Place>();
	protected List<Transition> transitions = new ArrayList<Transition>();

	/** Atributy site */
	protected String author;
	protected String name;
	protected String description;
	protected String version;
	protected List<String> simulations = new ArrayList<String>();

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
     * Vyhleda misto podle jeho identifikator
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
    			// nastaveni vystupni operace
    			newTransition.setExpr(transition.attributeValue("expr"));
    			// nacteni strazi
    			List <Element> guards = transition.elements("guard");
    			for(Element guard : guards ) {
    				newTransition.addGuard(new Condition(guard.attributeValue("src"), guard.attributeValue("op"), guard.attributeValue("dst")));
    			}
    			// nacteni vstupu
    			List <Element> inputs = transition.elements("input");
    			for(Element input : inputs) {
    				Arc newArc;
    				Place p = findPlace(Integer.parseInt(input.attributeValue("place")));
    				// bud je hrana s promennou, nebo konstantou
    				try {
    					int v = Integer.parseInt(input.attributeValue("name"));
    					newArc = new ConstArc(p, v);
    				} catch (NumberFormatException e) {
    					newArc = new VarArc(p, input.attributeValue("name"));
    				}
    				// pridat
    				newTransition.addInArc(newArc);
    			}
    			// nacteni vystupu
    			List <Element> outputs = transition.elements("output");
    			for(Element output : outputs) {
    				Arc newArc;
    				Place p = findPlace(Integer.parseInt(output.attributeValue("place")));
    				// bud je hrana s promennou, nebo konstantou
    				try {
    					int v = Integer.parseInt(output.attributeValue("name"));
    					newArc = new ConstArc(p, v);
    				} catch (NumberFormatException e) {
    					newArc = new VarArc(p, output.attributeValue("name"));
    				}
    				// pridat
    				newTransition.addOutArc(newArc);
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
	    	return null;
	    }
    	return new PetriNet(doc.asXML());
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
	    // pridani vsech prechodu
	    Element transitions = root.addElement("transitions");
	    for(Transition t : this.transitions) {
	    	t.toXML(transitions);
	    }
	    // pridani vsech mist
	    Element places = root.addElement("places");
	    for(Place p : this.places) {
	    	p.toXML(places);
	    }
	    // a nakonec seznam simulaci
	    Element simulations = root.addElement("simulations");
	    for(String s : this.simulations) {
	    	simulations.addElement("simulation").addText(s);
	    }
	    return doc;
    }
}