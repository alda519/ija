package petrinet;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;

import java.io.*;
import org.dom4j.*;
import org.dom4j.dom.*;
import org.dom4j.io.*;

public class PetriNet {

	/** Seznam vsech mist a prechodu v siti */
	protected List<Component> components = new ArrayList<Component>();
	/** Seznam vsech hran v siti */
	protected List<Arc> arcs = new ArrayList<Arc>();

	/** Konstrukce site ze stringu */
    public PetriNet(String net) {
    	
    }

    /** Konstrukce site ze souboru */
    public PetriNet(File file) {
    	try {
    		SAXReader xmlReader = new SAXReader();
    		Document doc = xmlReader.read(file);
    		
    		Element root = doc.getRootElement();
    		//root.
    		
    	} catch (DocumentException e) {}
    }
}
