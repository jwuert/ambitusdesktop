package org.wuerthner.ambitusdesktop.score;

import org.wuerthner.cwn.api.ScoreCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

public class AmbitusScoreCanvas implements ScoreCanvas {
    final private Graphics2D g;
    final private Map<String,Image> imageMap = new HashMap<>();
    final private Map<String,Font> fontMap = new HashMap<>();
    final private double zoom;
    final private int height;
    final private BasicStroke stroke;
    final private Map<String,Integer> correctionMap = new HashMap<>();
    private boolean outsideDrawArea = false;

    public AmbitusScoreCanvas(Graphics graphics, double zoom, int height) {
        this.g = (Graphics2D) graphics;
        this.zoom = zoom;
        this.height = height;
        this.stroke = new BasicStroke((int)zoom);
        // ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/note1.png"));
        init();
    }

    @Override
    public void open() {
        g.setStroke(stroke);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        g.drawLine((int)(x1*zoom), (int)(y1*zoom), (int)(x2*zoom), (int)(y2*zoom));
    }

    @Override
    public void drawString(String text, String fontName, int x, int y, String align) {
        int offset = 0;
        if (align.equals("right")) {
            FontMetrics fm = g.getFontMetrics(fontMap.get(fontName));
            Rectangle2D textsize = fm.getStringBounds(text, g);
            double wd = textsize.getWidth();
            offset = (int)(-wd);
        } else if (align.equals("center")) {
            FontMetrics fm = g.getFontMetrics(fontMap.get(fontName));
            Rectangle2D textsize = fm.getStringBounds(text, g);
            double wd = textsize.getWidth();
            offset = (int)(-0.5*wd);
        }
        g.setFont(fontMap.get(fontName));
        g.drawString(text, (int)(offset + (x+1)*zoom), (int)((y-2)*zoom));
    }

    @Override
    public void drawImage(String name, int x, int y, boolean alternative) {
        x = (int) (x * zoom);
        y = (int) (y * zoom);
        if (zoom==2) {
            x = x -1;
            y = (int) (y + correctionMap.getOrDefault(name,0));
        }
        g.drawImage(imageMap.get( (zoom==2 ? "d-" : "") + name + (alternative ? "-h" : "")), x, y, null);
        checkDrawArea(y);
    }

    @Override
    public void drawDot(int x, int y) {
        x = (int) (x * zoom);
        y = (int) (y * zoom);
        // g.drawLine(x,y,x,y);
        g.setColor(Color.BLACK);
        g.fillOval(x, y, (int)zoom+3,(int)zoom+3);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, String color) {
        //System.out.println("drawLine: " + color);
        switch(color) {
            case "red": g.setColor(Color.RED); break;
            case "grey": g.setColor(Color.GRAY); break;
            case "blue": g.setColor(Color.BLUE); break;
            case "green": g.setColor(Color.GREEN); break;
            case "yellow": g.setColor(Color.YELLOW); break;
            default: g.setColor(Color.BLACK);
        }
        g.drawLine((int)(x1*zoom), (int)(y1*zoom), (int)(x2*zoom), (int)(y2*zoom));
        g.setColor(Color.BLACK);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, int width) {
        g.setStroke(new BasicStroke((int)(width*zoom)));
        g.drawLine((int)(x1*zoom)+1, (int)(y1*zoom), (int)(x2*zoom)-1, (int)(y2*zoom));
        g.setStroke(stroke);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, boolean alternative) {
        if (alternative) {
            g.setColor(Color.RED);
            g.drawLine((int)(x1*zoom), (int)(y1*zoom), (int)(x2*zoom), (int)(y2*zoom));
            g.setColor(Color.BLACK);
        } else {
            g.drawLine((int) (x1 * zoom), (int) (y1 * zoom), (int) (x2 * zoom), (int) (y2 * zoom));
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void drawRect(int x1, int y1, int x2, int y2) {
        g.drawRect((int)(x1*zoom), (int)(y1*zoom), (int)((x2-x1)*zoom), (int) ((y2-y1)*zoom));
    }

    @Override
    public boolean outsideDrawArea() {
        return outsideDrawArea;
    }

    private void checkDrawArea(int y) {
        outsideDrawArea = outsideDrawArea || (y > height);
    }

    @Override
    public void drawArc(int x1, int y1, int x2, int y2, int direction, int delta, boolean alternative) {
        y1 = y1 - 3;
        y2 = y2 - 3;
        int width = x2-x1;
        int height = y2-y1;
        if (height==0) height = 3;
        if (direction==1)
            g.drawArc((int)(x1*zoom), (int)(y1*zoom), (int)(width*zoom), (int) (height*zoom), 0, 180);
        else
            g.drawArc((int)(x1*zoom), (int)(y1*zoom), (int)(width*zoom), (int) (height*zoom), 0, -180);
    }

    private void init() {
        // imageMap.put("background", getImage("paper4"));
        //
        // single size
        //
        imageMap.put("head1", getImage("note2"));
        imageMap.put("head2", getImage("note0"));
        imageMap.put("head4", getImage("note1"));
        imageMap.put("head8", getImage("note1"));
        imageMap.put("head16", getImage("note1"));
        imageMap.put("head32", getImage("note1"));
        imageMap.put("head64", getImage("note1"));
        //
        imageMap.put("head1-h", getImage("note2-h"));
        imageMap.put("head2-h", getImage("note0-h"));
        imageMap.put("head4-h", getImage("note1-h"));
        imageMap.put("head8-h", getImage("note1-h"));
        imageMap.put("head16-h", getImage("note1-h"));
        imageMap.put("head32-h", getImage("note1-h"));
        imageMap.put("head64-h", getImage("note1-h"));
        // rests
        imageMap.put("rest1", getImage("b1"));
        imageMap.put("rest2", getImage("b2"));
        imageMap.put("rest4", getImage("b4"));
        imageMap.put("rest8", getImage("b8"));
        imageMap.put("rest16", getImage("b16"));
        imageMap.put("rest32", getImage("b32"));
        imageMap.put("rest64", getImage("b64"));
        //
        imageMap.put("flag1", getImage("flagX"));
        imageMap.put("flag-1", getImage("flagI"));
        //
        for (int c = 0; c < 14; c++) {
            imageMap.put("clef" + c, getImage("clef-" + c));
            imageMap.put("clef" + c + "-h", getImage("clef-" + c + "-h"));
        }
        imageMap.put("sharp", getImage("sgnSharp"));
        imageMap.put("sharp-h", getImage("sgnSharp-h"));
        imageMap.put("flat", getImage("sgnFlat"));
        imageMap.put("flat-h", getImage("sgnFlat-h"));
        //
        imageMap.put("sign-2", getImage("sgnFlat2"));
        imageMap.put("sign-1", getImage("sgnFlat"));
        imageMap.put("sign1", getImage("sgnSharp"));
        imageMap.put("sign2", getImage("sgnSharp2"));
        imageMap.put("sign3", getImage("sgnNat"));
        //
        // double size
        //
        imageMap.put("d-head1", getImage("note-d-2"));
        imageMap.put("d-head2", getImage("note-d-0"));
        imageMap.put("d-head4", getImage("note-d-1"));
        imageMap.put("d-head8", getImage("note-d-1"));
        imageMap.put("d-head16", getImage("note-d-1"));
        imageMap.put("d-head32", getImage("note-d-1"));
        imageMap.put("d-head64", getImage("note-d-1"));
        //
        imageMap.put("d-head1-h", getImage("note-d-2-h"));
        imageMap.put("d-head2-h", getImage("note-d-0-h"));
        imageMap.put("d-head4-h", getImage("note-d-1-h"));
        imageMap.put("d-head8-h", getImage("note-d-1-h"));
        imageMap.put("d-head16-h", getImage("note-d-1-h"));
        imageMap.put("d-head32-h", getImage("note-d-1-h"));
        imageMap.put("d-head64-h", getImage("note-d-1-h"));
        // rests
        imageMap.put("d-rest1", getImage("b-d-1"));
        imageMap.put("d-rest2", getImage("b-d-2"));
        imageMap.put("d-rest4", getImage("b-d-4"));
        imageMap.put("d-rest8", getImage("b-d-8"));
        imageMap.put("d-rest16", getImage("b-d-16"));
        imageMap.put("d-rest32", getImage("b-d-32"));
        imageMap.put("d-rest64", getImage("b-d-64"));
        //
        imageMap.put("d-flag1", getImage("flagX-d"));
        imageMap.put("d-flag-1", getImage("flagI-d"));
        //
        for (int c = 0; c < 14; c++) {
            imageMap.put("d-clef" + c, getImage("clef-d-" + c));
            imageMap.put("d-clef" + c + "-h", getImage("clef-d-" + c + "-h"));
        }
        imageMap.put("d-sharp", getImage("sgnSharp-d"));
        imageMap.put("d-flat", getImage("sgnFlat-d"));
        //
        imageMap.put("d-sign-2", getImage("sgnFlat2-d"));
        imageMap.put("d-sign-1", getImage("sgnFlat-d"));
        imageMap.put("d-sign1", getImage("sgnSharp-d"));
        imageMap.put("d-sign2", getImage("sgnSharp2-d"));
        imageMap.put("d-sign3", getImage("sgnNat-d"));

        // Y-Pos Correction:
        correctionMap.put("head1", -2);
        correctionMap.put("head2", -2);
        correctionMap.put("head4", -2);
        correctionMap.put("head8", -2);
        correctionMap.put("head16", -2);
        correctionMap.put("head32", -2);
        correctionMap.put("head64", -2);
        // rests
        correctionMap.put("rest1", 0);
        correctionMap.put("rest2", 0);
        correctionMap.put("rest4", 0);
        correctionMap.put("rest8", 2);
        correctionMap.put("rest16", 2);
        correctionMap.put("rest32", 2);
        correctionMap.put("rest64", 2);


        // FONTS

        fontMap.put("title", new Font("Arial", Font.PLAIN, (int)(30*zoom)));
        fontMap.put("subtitle", new Font("Arial", Font.PLAIN, (int)(24*zoom)));
        fontMap.put("barNumber", new Font("Arial", Font.ITALIC, (int)(9*zoom)));
        fontMap.put("timeSignature", new Font("Arial", Font.BOLD, (int)(13*zoom)));
        fontMap.put("text", new Font("Arial", Font.PLAIN, (int)(9*zoom)));
        fontMap.put("nole", new Font("Arial", Font.PLAIN, (int)(7*zoom)));
        fontMap.put("track", new Font("Arial", Font.PLAIN, (int)(18*zoom)));
        fontMap.put("lyrics", new Font("Arial", Font.PLAIN, (int)(9*zoom)));
    }

    private Image getImage(String name) {
        try {
            return new ImageIcon(getClass().getResource("/images/" + name + ".png")).getImage();
        } catch (Exception e) {
            System.err.println("NOT FOUND: " + name);
            return null;
        }
    }
}

