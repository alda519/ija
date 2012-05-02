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
     * Konstruktor site z XML popisu.
     * @param xml
     */
    public PetriNet(String xml) {
    	// TODO: parsexml etc.
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
