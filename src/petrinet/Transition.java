package petrinet;

import java.util.List;
import java.util.ArrayList;

public class Transition {
	
	/** Seznam vstupnich hran */
	protected List <Arc> in = new ArrayList<Arc>();
	/** Seznam vystupnich hran */
	protected List <Arc> out = new ArrayList<Arc>();

	/** Seznam podminek */
	protected List <Condition> conditions = new ArrayList<Condition>();
	
	/** Vystupni operace */
	protected String operation;
	
	
	public Transition() {

	}

	public void addInArc()  {
	
	}

	public void addOutArc() {
		
	}

	public void removeArc() {
		
	}
	
	//public set

	/** Vektor vybranych cisel z prechodu. */
	protected int vector [];
	
	/**
	 * Pokusi se uskutecnit prechod.
	 * @return Metoda vraci true, pokud je prechod uskutecnen, jinak false.
	 */
	public boolean doIt() {
		// pole cisel velke jako pocet vstupnich hran
		vector = new int [in.size()];
		
		// pokud je uspesny vyber vstupnich dat
		if(mapValues(0)) {
			// TODO: spocitat vysledek, tzn parser vyrazu, zase prohledani hodnot spocitat, zvratit atd.
			//int result = 42;
			// TODO: odebrat vstupy z mist
			// TODO: do vsech vystupnich mist napchat vysledek
			vector = null;
			return true;
		}
		vector = null;
		return false;
	}

	/**
	 * Vypocita rekurzivne prvni umoznitelne nastaveni vstupnich dat
	 * @param n Index nastavovaneho vstupu
	 * @return Vraci true, pokud je 
	 */
	protected boolean mapValues(int n) {
		if(n < vector.length) {
			Arc a = in.get(n);
			// zkousi se vsechny moznoti prirazeni z mista
			for(int i = 0; i < a.getOptions(); ++i) {
				vector[n] = a.getValue(i);
				if(n ==  vector.length - 1) {
					// implicitne vsechny podminky plati
					boolean checked = true;
					// nektere mozna ne
					for(Condition c : conditions) {
						if(c.valid(vector, in) == false) {
							checked = false;
							break;
						}
					}
					// pokud vse plati, mame reseni
					if(checked)
						return true;
				} else {
					// pokud bylo nalezeno reseni, vraci se true, jinak se zkousi dalsi
					if(mapValues(n + 1))
						return true;
				}
			}
			// po vyzkouseni vsech moznosti neni reseni 
			return false;
		}
		// mimo rozsah neni reseni
		return false;
	}
}