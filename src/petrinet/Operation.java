package petrinet;

/**
 * Reprezentace binarni operace v podmince v prechodu 
 */
public enum Operation {

	LESS, GREATER, LESSEQ, GREATEREQ, EQUAL, NEQUAL, UNKNOWN;
	
	public static Operation getOperation(String op) {
		if(op.equals("<")) {
			return LESS;
		}else if (op.equals(">")){
			return GREATER;
		}else if (op.equals("<=")){
			return LESSEQ;
		} else if (op.equals(">=")){
			return GREATEREQ;
		} else if (op.equals("=")) {
			return EQUAL;
		} else if (op.equals("!=")) {
			return NEQUAL;
		} else {
			return UNKNOWN;
		}
	}

	/**
	 * Overi splneni podminky.
	 * @param x Prvni operand
	 * @param y Druhy operand
	 * @return Vraci true, pokud je vysledek dane oprace pravdivy, jinak false.
	 */
	public boolean check(int x, int y) {
		switch(this) {
			case LESS: return x < y;
			case GREATER: return x > y;
			case LESSEQ: return x <= y;
			case GREATEREQ: return x >= y;
			case EQUAL: return x == y;
			case NEQUAL: return x != y;
			default: return false;
		}
	}
}
