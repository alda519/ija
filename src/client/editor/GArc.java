package client.editor;

import java.awt.geom.Line2D;

/**
 * Reprezentace hrany. Cara.
 */
 public class GArc extends Line2D.Float {

	public GArc(float x1, float y1, float x2, float y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}
    public void addX1(float x) {
        this.x1 += x;
    }
    public void addX2(float x) {
        this.x2 += x;
    }
    public void addY1(float y) {
        this.y1 += y;
    }
    public void addY2(float y) {
        this.y2 += y;
    }

}