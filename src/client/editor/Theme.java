/**
 * IJA - Simulator Petriho siti
 *
 * Trida uchovavajici aktualni nastaveni vzhledu.
 *
 * @author xcupak04
 * @author xdujic01
 */

package client.editor;

import java.io.File;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Trida pro reprezentaci grafickeho vzhledu editoru.
 * Vsechny vlastnoti jsou staticke, tedy jedny pro celou aplikaci.
 */
public class Theme {

	/** Brava mist */
	public static int PLACE_R = 130;
	public static int PLACE_G = 140;
	public static int PLACE_B = 250;

	/** Barvy prechodu */
	public static int TRANSITION_R = 130;
	public static int TRANSITION_G = 140;
	public static int TRANSITION_B = 250;

	/** Barvy vstupni a vystupnich hran */
	public static int ARC_IN_R = 255;
	public static int ARC_IN_G;
	public static int ARC_IN_B;
	public static int ARC_OUT_R;
	public static int ARC_OUT_G;
	public static int ARC_OUT_B = 255;

	/** Barvy textu */
	public static int TEXT_R;
	public static int TEXT_G;
	public static int TEXT_B;

	/** Font */
	public static String FONT = "Serif";
	public static int FONT_SIZE = 12;

	public static void loadTheme(File file) {
		try {
			SAXReader xmlReader = new SAXReader();
			Document doc = xmlReader.read(file);
			Element root = doc.getRootElement();
			if(! root.getName().equals("theme"))
				throw new DocumentException();

			Element el = root.element("arc-in");
			ARC_IN_R = Integer.parseInt(el.attributeValue("red"));
			ARC_IN_G = Integer.parseInt(el.attributeValue("green"));
			ARC_IN_B = Integer.parseInt(el.attributeValue("blue"));
			el = root.element("arc-out");
			ARC_OUT_R = Integer.parseInt(el.attributeValue("red"));
			ARC_OUT_G = Integer.parseInt(el.attributeValue("green"));
			ARC_OUT_B = Integer.parseInt(el.attributeValue("blue"));
			el = root.element("place");
			PLACE_R = Integer.parseInt(el.attributeValue("red"));
			PLACE_G = Integer.parseInt(el.attributeValue("green"));
			PLACE_B = Integer.parseInt(el.attributeValue("blue"));
			el = root.element("transition");
			TRANSITION_R = Integer.parseInt(el.attributeValue("red"));
			TRANSITION_G = Integer.parseInt(el.attributeValue("green"));
			TRANSITION_B = Integer.parseInt(el.attributeValue("blue"));
			el = root.element("text");
			TEXT_R = Integer.parseInt(el.attributeValue("red"));
			TEXT_G = Integer.parseInt(el.attributeValue("green"));
			TEXT_B = Integer.parseInt(el.attributeValue("blue"));
			FONT = el.attributeValue("font");
			FONT_SIZE = Integer.parseInt(el.attributeValue("size"));
		} catch (DocumentException e) {
			System.err.print("Nevalidni XML dokumnet");
		} catch (NumberFormatException e) {
			System.err.print("Neplatna ciselna hodnota v XML dokumentu");
		}
	}
}
