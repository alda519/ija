package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import javax.swing.*;
import java.awt.Dimension;

import java.util.*;
/**
 * Tato trida je potom soucasti okna, je to vlastne jedinej
 * @author alda
 *
 */
public class MovingScaling extends JPanel {


    // mam kolekci elipsicek
    private List<ZEllipse> ellipses = new ArrayList<ZEllipse>();
	// a jednu vyberovou
    private ZEllipse zell = null;
    private List<ZLine> lines = new ArrayList<ZLine>();
    
    /**
     * Jen nastaveni zpracovani udalosti a vytvoreni elipsicky
     */
    public MovingScaling() {
    	setPreferredSize(new Dimension(800, 600));
        MovingAdapter ma = new MovingAdapter();

        addMouseMotionListener(ma);
        addMouseListener(ma);
        
        for(int i=0; i < 10; ++i) {
        	ellipses.add(new ZEllipse(50 + (i%5)*80, 70 + ((i+1)%2)*80, 80, 80, i*25));
        }

        //setDoubleBuffered(true);
    }

    /**
     * Sem musi patrit vykresleni veskereho neporadku, tzn vsech elipsicek.
     */
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        // vymalovat kazdou elipsicku
        for(ZEllipse ell : ellipses) {
        	g2d.setColor(new Color(ell.red, 200, 0));
        	g2d.fill(ell);
        	g2d.draw(new ZLine(200,200,ell.x+ell.width/2, ell.y+ell.height/2));
        }
    }

    /**
     * Ta elipsa ma jen metody na detekci chyceni a nejake posuny
     */
    class ZEllipse extends Ellipse2D.Float {
    	public int red;
        public ZEllipse(float x, float y, float width, float height, int color) {
            setFrame(x, y, width, height);
            red = color;
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
        }

        public void addY(float y) {
            this.y += y;
        }
    }

    /**
     * Reprezentace cary
     */
    class ZLine extends Line2D.Float {
    	public ZLine(float x1, float y1, float x2, float y2) {
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

    /**
     * Nastavuje, co se deje, kdyz se hejbe mysou. 
     */
    class MovingAdapter extends MouseAdapter {

        private int x;
        private int y;

        public void mousePressed(MouseEvent e) {
            x = e.getX();
            y = e.getY();
            
            // pokud jsem kliknul na elipsicku, tak si ji ulozim
            for(ZEllipse ell : ellipses) {
            	if(ell.isHit(x, y)) {
            		zell = ell;
            		return;
            	}
            }
            zell = null;
            
            // kdyz kliknu do pole, tak se prida kolesko
            ellipses.add(new ZEllipse(x-20,y-20,40,40,255));
            repaint();
        }

        // kompromis mezi rychle x pekne
        //public void mouseReleased(MouseEvent e) {
        public void mouseDragged(MouseEvent e) {

        	if(zell == null)
        		return;

            int dx = e.getX() - x;
            int dy = e.getY() - y;
            
            zell.addX(dx);
            zell.addY(dy);
            repaint();

            x += dx;
            y += dy;
        }
    }

    /**
     * Vyvotereni okna atd.
     */
    public static void main(String[] args) {

        JFrame frame = new JFrame("Moving and Scaling");
        
        // obsah se obali skrolovatkem a muze se kreslit vsude, pro jednoduchost asi vynechame zatim
        JScrollPane scroo = new JScrollPane(new MovingScaling());
        frame.add(scroo);
        
        // jak na ty taby blbe?
        
        //frame.add(new MovingScaling());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

