package petrinet;

import java.util.List;
import java.util.ArrayList;

/**
 * Trida pro zpracovani vypoctu v prechodu
 */
public class Expression {

	protected List<String> plus = new ArrayList<String>();
	protected List<String> minus = new ArrayList<String>();
	
	public Expression(String exp) {
		// TODO: rozparsovat exp
	}
	
	public int eval(String expression) {
		int x = 0;
		// TODO: projit plus a poscitat to
		// TODO: projit minus a poodecitat to
		// bud je to cislo, nebo jmeno a to se hleda pres hrany v poli
		return x;
	}
	
}
