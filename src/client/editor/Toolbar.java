/**
 * IJA - Simulator Petriho siti
 *
 * Vycet modu editoru.
 *
 * @author xcupak04
 * @author xdujic01
 */

package client.editor;

/**
 * Vycet Toolbar reprezentuje vybranou operaci v nabidce modu editoru.
 */
public enum Toolbar {

	EDIT, ADDPLACE, ADDTRANSITION, ARCEDIT, DELETE;
	
	/**
	 * Na zaklade hodnoty z vyberu vraci operaci.
	 * @param action oparace z vysuvneho menu
	 * @return jaka se bude delat operace
	 */
	public static Toolbar getAction(String action) {
		if(action.equals("Editace")) {
			return EDIT;
		} else if(action.equals("Kreslení míst")) {
			return ADDPLACE;
		} else if(action.equals("Kreslení přechodů")) {
			return ADDTRANSITION;
		} else if(action.equals("Editace hran")) {
			return ARCEDIT;
		} else if(action.equals("Rušení objektů")) {
			return DELETE;
		} else
			return EDIT;
	}
}
