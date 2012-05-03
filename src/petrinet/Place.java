package petrinet;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Reprezentace mista.
 */
public class Place {
	
	/** Seznam hodnot v miste. */
	protected List <Integer> values = new ArrayList<Integer>();
	/** Identifikator mista. */
	protected int id;

	/** Souradnice pro kresleni */
	public int x, y;

	/**
	 * Konstruktor
	 * @param id identifikator mista
	 * */
	public Place(int id) {
		this.id = id;
	}
	
	/** Vraci pocet hodnot v miste */
	public int size() {
		return this.values.size();
	}
	
	/** Vraci hodnotu daneho indexu */
	public int getValue(int index) {
		return this.values.get(index);
	}
	
	/** Vraci id */
	public int getId() {
		return id;
	}
	
	/**
	 * Pridani hodnoty do mista.
	 * @param val Hodnota, co bude do mista pridana
	 */
	public void addValue(int val) {
		this.values.add(val);
	}
	
	/**
	 * Odebrani ohnodty z mista.
	 * @param val Hodnota, co bude z mista odebrana.
	 */
	public void removeValue(int val) {
		int index = this.values.indexOf(val);
		this.values.remove(index);
	}
	
	/**
	 * Generuje XML popis mista.
	 * @param places Element dokumentu, do ktereho se generuje obsah.
	 */
	public void toXML(Element places) {
		// do seznamu mist, se prida misto s danym id
		Element place = places.addElement("place");
		place.addAttribute("id", ""+id);
		place.addAttribute("x", ""+this.x);
		place.addAttribute("y", ""+this.y);
		// do mista se pridaji vsechny hodnoty
		for(Integer i : values) {
			Element val = place.addElement("value");
			val.addText(i.toString());
		}
	}

	
	// testovaci bordel na inspiraci/smazani
	public static void main(String[] args) {
		Place p = new Place(42);
		
		p.addValue(40);
		p.addValue(41);
		p.addValue(42);
		
		
		Document document = DocumentHelper.createDocument();
	    Element root = document.addElement( "root" );
	    p.toXML(root);
	    
	    //System.out.println(document.asXML());
	    
	    try {
	    	FileWriter out = new FileWriter("foo.xml");
	        OutputFormat format = OutputFormat.createPrettyPrint();
	        XMLWriter writer = new XMLWriter(out, format);
	        writer.write( document );
	    	out.close();    
	    } catch (IOException w) {
	    	System.out.println("Sit se nepovedlo ulozit!");
	    }
	}
	
}
