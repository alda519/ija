/**
 * IJA - Simulator Petriho siti
 *
 * Reprezentace prechodu.
 *
 * @author xcupak04
 * @author xdujic01
 */

package petrinet;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Reprezentace prechodu site.
 * Obsahuje seznamy vstupu, vytupu a strazi a metody pro simulaci.
 */
public class Transition {
	
	/** Seznam vstupnich hran */
	protected List <Arc> in = new ArrayList<Arc>();
	/** Seznam vystupnich hran */
	protected List <Arc> out = new ArrayList<Arc>();

	/** Seznam podminek */
	protected List <Condition> conditions = new ArrayList<Condition>();
	
	/** Vystupni operace */
	protected String operation = "";

	/** Souradnice pro kresleni */
	public int x, y;

	/**
	 * Pridani vstupni hrany do prechodu
	 * @param a vstupni hrana
	 */
	public void addInArc(Arc a) {
		in.add(a);	
	}

	/**
	 * Pridani vystupni hrany do prechodu
	 * @param a vystupni hrana
	 */
	public void addOutArc(Arc a) {
		out.add(a);
	}

	/**
	 * Ziskani vsech vstupnich hran
	 * @return seznam vstupnich hran
	 */
	public List<Arc> getInArcs() {
		return this.in;
	}

	/**
	 * Ziskani vsech vystupnich hran
	 * @return seznam vystupnich hran
	 */
	public List<Arc> getOutArcs() {
		return this.out;
	}

	/**
	 * Odstraneni hrany
	 * @param a hrana k odstraneni
	 * @return vraci true pokud se dana hrana odstranila, jinak false
	 */
	public boolean removeArc(Arc a) {
		return in.remove(a) || out.remove(a);
	}

	/**
	 * Nastaveni vyrazu pro vypocet vystupu
	 * @param expr vyraz
	 */
	public void setExpr(String expr) {
		this.operation = expr;
	}
	/** Vraci operaci prechodu */
	public String getExpr() {
		return this.operation;
	}
	/** Vraci textove vsechny sve straze */
	public String getGuards() {
		return this.conditions.toString();
	}
	/** Vraci vektor vsech strazi */
	public Vector<Condition> getListGuards() {
		return new Vector<Condition>(this.conditions);
	}

	/**
	 * Pridani nove straze.
	 * @param c straz k pridani do prechodu
	 */
	public void addGuard(Condition c) {
		this.conditions.add(c);
	}
	/**
	 * Odebrani straze
	 * @param c straz k odstraneni
	 */
	public void dropGuard(Condition c) {
		this.conditions.remove(c);
	}

	/** Vektor vybranych cisel z prechodu. */
	public int vector [];

	/**
	 * Pokusi se uskutecnit prechod, pokud je to mozne, provede ho.
	 * @return Metoda vraci true, pokud je prechod uskutecnen, jinak false.
	 */
	public boolean tryTransition() {
		// pole cisel velke jako pocet vstupnich hran
		vector = new int [in.size()];
		
		// pokud je uspesny vyber vstupnich dat
		if(mapValues(0)) {
			// vypocet vyrazu
			Expression e = new Expression(this.operation);
			int output = e.eval(vector, in);
			// odebrat vstupy z mist
			for(int i = 0; i < in.size(); ++i) {
				Arc a = in.get(i);
				a.getPlace().removeValue(vector[i]);
			}
			// do vsech vystupnich mist pridat vysledek
			for(Arc a : out) {
				if(a instanceof ConstArc) {
					// z konstantnich hran pridat cislo rovnou
					a.getPlace().addValue(Integer.parseInt(a.getName()));
				} else {
					// prirazeny vysledku
					if(a.getName().equals(e.getOutput())) {
						// pouziti vysledku z vyrazu
						a.getPlace().addValue(output);
					} else {
						// vyhledani vysledku ve vstupnich mistech
						for(Arc ai : in) {
							if(ai.getName().equals(a.getName())) {
								a.getPlace().addValue(vector[in.indexOf(ai)]);
							}
						}
					}
				}
			}
			vector = null;
			return true;
		}
		vector = null;
		return false;
	}

	/**
	 * Vypocita rekurzivne prvni umoznitelne nastaveni vstupnich dat
	 * @param n Index nastavovaneho vstupu
	 * @return Vraci true, pokud je mozny prechod
	 */
	protected boolean mapValues(int n) {
		if(n == 0)
			vector = new int [in.size()];
		if(n < vector.length) {
			Arc a = in.get(n);
			// zkousi se vsechny moznoti prirazeni z mista
			for(int i = 0; i < a.getOptions(); ++i) {
				// do vektoru se priradi `i`-ta moznost z mista odkud vede `a`
				vector[n] = a.getValue(i);
				// kontrola hodnoty pro konstantni hranu
				if(a.ok(vector[n]) == false) {
					continue;
				}
				// po prirazeni do posledniho policka se zkontroluji straze
				if(n == vector.length - 1) {
					// implicitne vsechny podminky plati
					boolean checked = true;
					// nektere mozna ne
					for(Condition c : conditions) {
						if(c.valid(vector, in) == false) {
							checked = false;
							break;
						}
					}
					// kontrola, zda vsechny stejne pojmenovane vstupy maji stejnou hodnotu
					for(int x = 0; x < in.size(); ++x) {
						for(int y = 0; y < in.size(); ++y) {
							// stejne pojmenovane hrany
							if(in.get(x).getName().equals(in.get(y).getName())) {
								// musi mit stejne hodnoty
								if(vector[x] != vector[y]) {
									checked = false;
									x = in.size(); // hackity hack, takovy supr break
									break;
								}
							}
						}
					}
					// pokud vse plati, mame reseni, jinak se zkousi dale
					if(checked) {
						return true;
					}
				} else {
					// pokud bylo nalezeno reseni, vraci se true, jinak se zkousi dalsi
					if(mapValues(n + 1)) {
						return true;
					}
				}
			}
			// po vyzkouseni vsech moznosti neni reseni 
			return false;
		}
		// mimo rozsah neni reseni
		return false;
	}
	
	/**
	 * Generuje XML reprezentaci.
	 * @param transitions Element dokumentu, kam se pridava obsah.
	 */
	public void toXML(Element transitions) {
		// do seznamu prechodu, se prida dany prechod
		Element transition = transitions.addElement("transition");
		// operace
		transition.addAttribute("expr", this.operation);
		transition.addAttribute("x", ""+this.x);
		transition.addAttribute("y", ""+this.y);
		// k prechodu se pridaji podminky
		for(Condition cond: this.conditions) {
			cond.toXML(transition);
		}
		// seznam vstupu a vystupu
		for(Arc ina : this.in ) {
			ina.toXML(transition, "in");			
		}
		for(Arc outa : this.out ) {
			outa.toXML(transition, "out");	
		}
	}

	// TODO: smazat - testovaci neporadek
	public static void main(String [] args) {
		Transition t = new Transition();
		Place p1 = new Place(1);
		Place p2 = new Place(2);
		Place p3 = new Place(3);
		Arc a1 = new VarArc(p1, "b");
		Arc a2 = new VarArc(p2, "b");
		Arc a3 = new VarArc(p3, "foo");
		t.addInArc(a1);
		t.addInArc(a2);
		t.addOutArc(a3);
		t.addGuard(new Condition("b", ">", "6"));
		//t.addGuard(new Condition("c", ">", "6"));
		t.setExpr("foo = - a + b + 4");

		p1.addValue(1);
		p1.addValue(5);
		p1.addValue(4);
		p1.addValue(7);
		p2.addValue(3);
		p2.addValue(6);
		p2.addValue(7);

		//boolean b = t.mapValues(0);
		boolean b = t.tryTransition();
		System.out.println(b);
		/*for(int x : t.vector) {
			System.out.println(x);
		}*/

		Document doc = DocumentHelper.createDocument();
		p3.toXML(doc.addElement("tr"));
		System.out.println(doc.asXML());
	}
}