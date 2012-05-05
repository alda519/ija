/**
 * IJA - Simulator Petriho siti
 *
 * Reprezentace vystupniho vyrazu.
 *
 * @author xcupak04
 * @author xdujic01
 */

package petrinet;

import java.util.List;
import java.util.ArrayList;

/**
 * Trida pro zpracovani vypoctu v prechodu.
 */
public class Expression {

	/** Seznamy promennych ke scitani/odecitani */
	protected List<String> plus = new ArrayList<String>();
	protected List<String> minus = new ArrayList<String>();
	/** Jmeno vystupni promenne */
	protected String output;
	/** Bazova hodnota vystupu, zavisla na konstantach ve vyrazu */
	protected int base = 0;

	/**
	 * Konstruktor vyrazu.
	 * Zanalyzuje vyraz a vyhodnoti, ktere promenne se budou pricitat a ktere odecitat
	 * @param exp vyraz k analyze
	 */
	public Expression(String exp) {
		String parts [] = exp.split("=");
		// pokud nejsou 2 casti, je vyraz apriori spatne a vystup je nula
		if(parts.length != 2) {
			output = null;
			return;
		}
		// jinak je vystup pojmenovany a treba to i neco spocita potom
		output = parts[0].trim();

		int index = 0;
		// rozdeleni vyrazu podle +/-
		String rParts [] = parts[1].trim().split("[+-]");
		for(int i = 0; i < rParts.length; ++i) {
			String x = rParts[i].trim();
			if(i == 0) {
				if(x.equals("")) {
					// pokud prvni je pradny, pak tam bylo znaminko a jen ho preskocim
					continue;
				} else {
					// jinak je to kladna hodnota
					plus.add(x);
				}
			} else {
				// dalsi se pricita/odecita dle znaminka
				int inPlus = parts[1].indexOf('+', index); 
				int inMinus = parts[1].indexOf('-', index);
				if((inPlus < inMinus && inPlus != -1) || (inMinus == -1)) {
					// odliseni konstant od promennych
					try {
						this.base += Integer.parseInt(rParts[i].trim());
					} catch (NumberFormatException e) {
						plus.add(rParts[i].trim());
					}
					index = inPlus + 1;
				} else {
					try {
						this.base -= Integer.parseInt(rParts[i].trim());
					} catch (NumberFormatException e) {
						minus.add(rParts[i].trim());
					}
					index = inMinus + 1;
				}
			}
		}
		//System.out.println(plus);
		//System.out.println(minus);
		//System.out.println(base);
	}

	/**
	 * Vyhodnoceni vyrazu. Nezname promenne jsou povazovany za 0.
	 * @param vector vektor hodnot vybranych ze vstupu
	 * @param inputs seznam vstupnich hran
	 * @return vraci vyhodnoceni vyrazu s dosazenim hodnot ze vstupu
	 */
	public int eval(int vector[], List<Arc> inputs) {
		// pokud neni vystup nastaven, bude nula
		if(output == null)
			return 0;
		int x = this.base;
		for(String p : plus) {
			for(Arc a : inputs) {
				if(p.equals(a.getName())) {
					x += vector[inputs.indexOf(a)];
					break;
				}
			}
		}
		for(String p : minus) {
			for(Arc a : inputs) {
				if(p.equals(a.getName())) {
					x -= vector[inputs.indexOf(a)];
					break;
				}
			}
		}
		return x;
	}

	/** Ziskani jmena vystupni promenne */
	public String getOutput() {
		return this.output;
	}
}
