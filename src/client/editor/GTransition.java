package client.editor;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import petrinet.Transition;

public class GTransition  extends Rectangle2D.Float {

	protected final int WIDTH = 160;
	protected final int HEIGHT = 80;
	
	/** Prechod, ktery tento prvek reprezentuje. */
	protected Transition transition;
	
	/** Seznam vstupnich vystupnich hran */
    protected List<GArc> arcsIn = new ArrayList<GArc>();
    protected List<GArc> arcsOut = new ArrayList<GArc>();

    public GTransition(Transition t) {
    	this.transition = t;
    	setFrame(t.x, t.y, WIDTH, HEIGHT);
    }
    
    public void addArcIn(GArc line) {
        arcsIn.add(line);
    }

    public void addArcOut(GArc line) {
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
        this.transition.x = (int) this.x;
    }
 
    public void addY(float y) {
        this.y += y;
        for(GArc line : this.arcsIn) {
            line.addY1(y);
        }
        for(GArc line : this.arcsOut) {
            line.addY2(y);
        }
        this.transition.y = (int) this.y;
    }
	
}
