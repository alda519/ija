package petrinet;

import org.dom4j.Element;


/**
 * Reprezentace hrany s pojmenovanou promennou.
 */
public class VarArc implements Arc {
	
	protected String name;
	
	protected Place place;
	
	/**
	 * Konstruktor hrany s promennou.
	 * @param name Promenna na hrane
	 */
	public VarArc(Place place, String name) {
		this.place = place;
		this.name = name;
	}
	
	/**
	 * Vraci vzdy true, promenna lze nahradit libovolnou hodnotou hodnotou z mista.
	 */
	public boolean ok(int x) {
		return true;
	}
	
	/**
	 * Metoda vraci pocet pripustnych hodnot z mista
	 */
	public int getOptions() {
		return this.place.size();
	}

	/**
	 * Vraci hodnotu z mista na danem indexu
	 */
	public int getValue(int i) {
		return this.place.getValue(i);
	}
	
	public String getName() {
		return name;
	}

	/**
	 * Vraci misto odkud/kam vede.
	 */
	public Place getPlace() {
		return this.place;
	}

	/** Generuje XML popis hrany */
	public void toXML(Element t, String io) {
		Element arc = t.addElement("arc");
		arc.addAttribute("io", io);
		arc.addAttribute("name", this.name);
		arc.addAttribute("place", ""+this.place.getId());
	}
}
