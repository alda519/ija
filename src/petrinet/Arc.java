/**
 * IJA - Simulator Petriho siti
 *
 * Reprezetace hrany obecne, jen rozhranni.
 *
 * @author xcupak04
 * @author xdujic01
 */

package petrinet;

import org.dom4j.Element;

/**
 * Rozhrani hrany 
 */
public interface Arc {

	/**
	 * Metoda vraci true, pokud je parametr x pripustna hodnota pro hranu. 
	 * @param x
	 * @return vraci true pokud je hodnota pripustna, jinak false
	 */
	public boolean ok(int x);

	/** Vraci pocet hodnot v miste odkud hrana vede. */
	public int getOptions();
	
	/** Vraci hodnotu z mista na danem indexu. */
	public int getValue(int i);
	
	/** Vraci jmeno hrany */
	public String getName();

	/** Vraci misto odkud/kam vede */
	public Place getPlace();

	/** Generuje XML popis hrany */
	public void toXML(Element t, String io);
}
