package client.editor;

import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import petrinet.Place;

/**
 * Graficka reprezentace mista.
 * Je to elipsa.
 */
public class GPlace extends Ellipse2D.Float {

	/** Polomer kruhu */
	protected final int RADIUS = 80;

	/** Seznam vstupnich vystupnich hran */
    protected List<GArc> arcsIn = new ArrayList<GArc>();
    protected List<GArc> arcsOut = new ArrayList<GArc>();

    protected Place place;
    
    /**
     * Konstruktor
     * @param place misto site, ktere tento graficky prvek reprezentuje
     */
	public GPlace(Place place)
	{
		this.x = place.x;
		this.y = place.y;
		this.place = place;
		setFrame(x, y, RADIUS, RADIUS);
	}

    public void addInArc(GArc line) {
        arcsIn.add(line);
    }
    public void addOutArc(GArc line) {
        arcsOut.add(line);
    }

    public boolean isHit(float x, float y) {
        if (getBounds2D().contains(x, y)) {
            return true;
        } else {
            return false;
        }
    }

    public void addX(float x) {
        this.x += x;
        for(GArc line : this.arcsIn) {
            line.addX1(x);
        }
        for(GArc line : this.arcsOut) {
            line.addX2(x);
        }
        this.place.x = (int) this.x;
    }
 
    public void addY(float y) {
        this.y += y;
        for(GArc line : this.arcsIn) {
            line.addY1(y);
        }
        for(GArc line : this.arcsOut) {
            line.addY2(y);
        }
        this.place.y = (int) this.y;
    }

    /** Vraci true, pokud tato hranu reprezentuje dane misto */
    public boolean contains(Place p) {
    	return p == this.place;
    }
    
    /** Vraci retezec vsech hodnot v miste. */
    public String getValues() {
    	return this.place.getValues(); 
    }
}