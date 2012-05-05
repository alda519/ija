/**
 * IJA - Simulator Petriho siti
 *
 * Straz prechodu.
 *
 * @author xcupak04
 * @author xdujic01
 */

package petrinet;

import java.util.List;

import org.dom4j.Element;

/**
 * Reprezentace podminky v prechodu
 */
public class Condition {

	/** Jmeno prvni promenne */
	protected String src;
	/** Operator podminky */
	protected Operation operation;
	/** Jmeno nebo hodnota druheho operatoru */
	protected String dst;

	/** Konstruktor */
	public Condition(String src, String op, String dst) {
		this.src = src;
		this.operation = Operation.getOperation(op);
		this.dst = dst;
	}

	/**
	 * Test splneni podminky. Pokud se podminka tyka neexistujiciho vstupu,
	 * je vzdy povazovana za nesplnenou.
	 * @return Vraci true, pokud je podminka splnena, jinak false.
	 */
	public boolean valid(int vector[], List<Arc> arcs) {
		Integer op1, op2;
		op1 = null;
		op2 = null;
		// vyhledat hodnotu pro zdrojovy operand
		for(int i = 0; i < arcs.size(); ++i) {
			Arc a = arcs.get(i);
			if(a.getName().equals(src)) {
				op1 = vector[i];
			}
		}
		// druha hodnota muze byt cislice nebo jmeno promenne zase
		try {
			op2 = Integer.parseInt(dst);
		} catch (NumberFormatException e) {
			// vyhledat druhou hodnotu
			for(int i = 0; i < arcs.size(); ++i) {
				Arc a = arcs.get(i);
				if(a.getName().equals(dst)) {
					op2 = vector[i];
				}
			}
		}
		// pokud se podminka tyka neexistujiciho vstupu, neni splnena
		if(op1 == null || op2 == null)
			return false;
		return this.operation.check(op1, op2);
	}
	
	/**
	 * Vytvoreni XML reprezentace.
	 * @param trans element do ktereho se obsah vklada
	 */
	public void toXML(Element trans) {
		Element guard = trans.addElement("guard");
		guard.addAttribute("src", this.src);
		guard.addAttribute("op", this.operation.toString());
		guard.addAttribute("dst", this.dst);
	}
	
	/** Vraci textovy popis podminky */
	public String toString() {
		return src + operation.toString() + dst;
	}
}