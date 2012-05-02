package petrinet;

import java.util.List;

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
	 * Test splneni podminky.
	 * @return Vraci true, pokud je podminka splnena, jinak false.
	 */
	public boolean valid(int vector[], List<Arc> arcs) {
		int op1, op2;
		op1 = 0;
		op2 = 0;
		
		// vyhledat hodnotu pro zdrojovy operand
		for(int i = 0; i < arcs.size(); ++i) {
			Arc a = arcs.get(i);
			if(a.getName().equals(src)) {
				op1 = a.getValue(i);
			}
		}
		
		// druha hodnota muze byt cislice nebo jmeno promenne zase
		try {
			op2 = Integer.parseInt(dst);
		} catch (NumberFormatException e) {
			// vyhledat druhou hodnotu
			for(int i = 0; i < arcs.size(); ++i) {
				Arc a = arcs.get(i);
				if(a.getName().equals(src)) {
					op2 = a.getValue(i);
				}
			}
		}
		return this.operation.check(op1, op2);
	}
	
}