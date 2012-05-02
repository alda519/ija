package petrinet;

import org.dom4j.Element;

/**
 * Reprezentace hrany s konstantou.
 */
public class ConstArc implements Arc {
	
	/** Hodnota na hrane */
	protected int value;
	
	/** Misto odkud/kam prechod vede */
	protected Place place;
	
	/**
	 * Konstruktor hrany s hodnotou.
	 * @param value Hodnota na hrane
	 */
	public ConstArc(Place place, int value) {
		this.place = place;
		this.value = value;
	}

	/**
	 * Metoda vraci true, pokud hrana nese stejnou hodnotu jako vybrane cislo z mista.
	 * Jinak false.
	 */
	public boolean ok(int x) {
		return (x == this.value);
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
	
	/** Vraci vzdy null, nema jmeno */
	public String getName() {
		return null;
	}
	
	/** Generuje XML popis hrany */
	public void toXML(Element t, String io) {
		t.addAttribute("io", io);
		t.addAttribute("name", ""+this.value);
		t.addAttribute("place", ""+this.place.getId());
	}
}
