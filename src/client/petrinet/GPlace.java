package client.petrinet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.*;

/**
 * Graficka reprezentace mista.
 */
public class GPlace extends JPanel
{
	/** Pozice objektu. */
	private int x, y;
	
	public GPlace(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
    public void paintComponent(Graphics g) {
		super.paintComponent(g); 
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(new Color(250, 20, 115));
		g2d.fillOval(x, y, 80+x, 80);
	}

}
