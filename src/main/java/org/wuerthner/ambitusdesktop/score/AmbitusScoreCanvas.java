package org.wuerthner.ambitusdesktop.score;

import org.wuerthner.cwn.api.CwnAccent;
import org.wuerthner.cwn.api.CwnSymbolEvent;
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
    final private Map<String,Integer> correctionMapM = new HashMap<>();
    final private Image background = getImage("paper5");
    private boolean outsideDrawArea = false;


    public AmbitusScoreCanvas(Graphics graphics, double zoom, int height) {
        this.g = (Graphics2D) graphics;
        this.zoom = zoom;
        this.height = height;
        this.stroke = new BasicStroke((int)zoom);
        // g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        init();
        g.drawImage(background, 00,00,null);
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
    public void drawString(String text, String fontName, int x, int y, String align, boolean alternative) {
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
        if (fontName.endsWith("Muted")) {
            g.setColor(Color.LIGHT_GRAY);
        } else if (alternative) {
            g.setColor(Color.RED);
        }
        g.setFont(fontMap.get(fontName));
        g.drawString(text, (int)(offset + (x+1)*zoom), (int)((y-2)*zoom));
        if (fontName.endsWith("Muted") || alternative) {
            g.setColor(Color.BLACK);
        }
    }

    @Override
    public void drawImage(String name, int x, int y, boolean alternative) {
        x = (int) (x * zoom);
        y = (int) (y * zoom);
        if (zoom==2) {
            x = x -1;
            y = (int) (y + correctionMap.getOrDefault(name,0));
        } else if (zoom==1.5) {
            y = (int) (y + correctionMapM.getOrDefault(name,0));
        }
        g.drawImage(imageMap.get( (zoom==1.5 ? "m-" : zoom==2 ? "d-" : "") + name + (alternative ? "-h" : "")), x, y, null);
        checkDrawArea(y);
    }

    @Override
    public void drawDot(int x, int y) {
        x = (int) (x * zoom);
        y = (int) (y * zoom);
        // g.drawLine(x,y,x,y);
        g.setColor(Color.BLACK);
        g.fillOval(x, y, (int)(zoom*3),(int)(zoom*3));
    }

    @Override
    public void drawDot(int x, int y, int color) {
        x = (int) (x * zoom)+2;
        y = (int) (y * zoom)+1;
        g.setColor(color==0 ? Color.MAGENTA : Color.GREEN);
        g.fillOval(x, y, (int)zoom+4,(int)zoom+4);
        g.setColor(Color.BLACK);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, String color) {
        //System.out.println("drawLine: " + color);
        switch(color) {
            case "red": g.setColor(Color.RED); break;
            case "grey": g.setColor(Color.LIGHT_GRAY); break;
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
        if (x1==x2) {
            g.drawLine((int) (x1 * zoom) + 1, (int) (y1 * zoom), (int) (x2 * zoom) + 1, (int) (y2 * zoom));
        } else {
            g.drawLine((int) (x1 * zoom) + 1, (int) (y1 * zoom), (int) (x2 * zoom) - 1, (int) (y2 * zoom));
        }
        g.setStroke(stroke);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2, boolean alternative) {
        g.setStroke(new BasicStroke((int)(zoom*1.4)));
        if (alternative) {
            g.setColor(Color.RED);
            g.drawLine((int)(x1*zoom), (int)(y1*zoom), (int)(x2*zoom), (int)(y2*zoom));
            g.setColor(Color.BLACK);
        } else {
            g.drawLine((int) (x1 * zoom), (int) (y1 * zoom), (int) (x2 * zoom), (int) (y2 * zoom));
        }
        g.setStroke(stroke);
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
//        y1 = y1 - 3;
//        y2 = y2 - 3;

//        g.setColor(Color.RED);
//        g.drawArc((int)(x1*zoom)-1,(int)(y1*zoom)-1,3,3,0,360);
//        g.setColor(Color.BLUE);
//        g.drawArc((int)(x2*zoom)-1,(int)(y2*zoom)-1,3,3,0,360);
//        g.setColor(Color.BLACK);

        int slope = 0;
        int yTop = y1;
        int yBottom = y2;
        if (y2 < y1) {
            // goes up
            // y2 = 2*y1 - y2;
            slope = -1;
            yTop = y2;
            yBottom = y1;
        } else if (y1 < y2) {
            // goes down
            slope = 1;
        }

        int extraVerticalSpace = 8;
        int width = x2-x1;
        int height = yBottom-yTop ;
        yTop -= extraVerticalSpace;
        height += extraVerticalSpace;
        if (alternative) {
            g.setColor(Color.RED);
        }
        if (direction==1) {
//          g.drawRect((int)((x1-2)*zoom), (int)((yTop-2)*zoom), (int)((width+4)*zoom), (int)((height+4)*zoom));
            g.drawArc((int)(x1*zoom), (int)(yTop*zoom), (int)(width*zoom), (int)(height*zoom),
                    slope>0 ?  10 : slope<0 ? 40 : 10,
                    slope>0 ? 130 : slope<0 ? 130 : 160);
            g.setStroke(new BasicStroke((float)(1.4*zoom)));
            g.drawArc((int)(x1*zoom), (int)(yTop*zoom), (int)(width*zoom), (int)(height*zoom),
                    slope>0 ?  30 : slope<0 ? 60 : 40,
                    slope>0 ? 90 : slope<0 ? 90 : 100);
        } else {
            yTop += 2*extraVerticalSpace;
//          g.drawRect((int)((x1-2)*zoom), (int)((yTop-2)*zoom), (int)((width+4)*zoom), (int)((height+4)*zoom));
            g.drawArc((int)(x1*zoom), (int)(yTop*zoom), (int)(width*zoom), (int)(height*zoom),
                    slope>0 ?  -40 : slope<0 ? -10 : -10,
                    slope>0 ? -130 : slope<0 ? -130 : -160);
            g.setStroke(new BasicStroke((float)(1.4*zoom)));
            g.drawArc((int)(x1*zoom), (int)(yTop*zoom), (int)(width*zoom), (int)(height*zoom),
                    slope>0 ?  -60 : slope<0 ? -30 : -40,
                    slope>0 ? -90 : slope<0 ? -90 : -100);
        }
        if (alternative) {
            g.setColor(Color.BLACK);
        }
        g.setStroke(stroke);
    }

    private void init() {
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
        imageMap.put("nat", getImage("sgnNat"));
        imageMap.put("nat-h", getImage("sgnNat-h"));
        //
        imageMap.put("sign-2", getImage("sgnFlat2"));
        imageMap.put("sign-1", getImage("sgnFlat"));
        imageMap.put("sign1", getImage("sgnSharp"));
        imageMap.put("sign2", getImage("sgnSharp2"));
        imageMap.put("sign3", getImage("sgnNat"));

        // accents
        for (String accentName : CwnAccent.ACCENTS) {
            imageMap.put(accentName, getImage("accents/sacc" + accentName));
        }
        // symbols
        for (String symbolName : CwnSymbolEvent.SYMBOLS) {
            imageMap.put(symbolName, getImage("symbols/ssym" + symbolName));
            imageMap.put(symbolName + "-h", getImage("symbols/ssym" + symbolName + "-h"));
        }

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
        imageMap.put("d-sharp-h", getImage("sgnSharp-d-h"));
        imageMap.put("d-flat", getImage("sgnFlat-d"));
        imageMap.put("d-flat-h", getImage("sgnFlat-d-h"));
        imageMap.put("d-nat", getImage("sgnNat-d"));
        imageMap.put("d-nat-h", getImage("sgnNat-d-h"));
        //
        imageMap.put("d-sign-2", getImage("sgnFlat2-d"));
        imageMap.put("d-sign-1", getImage("sgnFlat-d"));
        imageMap.put("d-sign1", getImage("sgnSharp-d"));
        imageMap.put("d-sign2", getImage("sgnSharp2-d"));
        imageMap.put("d-sign3", getImage("sgnNat-d"));

        // accents
        for (String accentName : CwnAccent.ACCENTS) {
            imageMap.put("d-"+accentName, getImage("accents/sacc" + accentName + "-d"));
        }
        // symbols
        for (String symbolName : CwnSymbolEvent.SYMBOLS) {
            imageMap.put("d-"+symbolName, getImage("symbols/ssym" + symbolName + "-d"));
            imageMap.put("d-"+symbolName + "-h", getImage("symbols/ssym" + symbolName + "-d-h"));
        }

        //
        // medium size (1.5)
        //
        imageMap.put("m-head1", getImage("note-m-2"));
        imageMap.put("m-head2", getImage("note-m-0"));
        imageMap.put("m-head4", getImage("note-m-1"));
        imageMap.put("m-head8", getImage("note-m-1"));
        imageMap.put("m-head16", getImage("note-m-1"));
        imageMap.put("m-head32", getImage("note-m-1"));
        imageMap.put("m-head64", getImage("note-m-1"));
        //
        imageMap.put("m-head1-h", getImage("note-m-2-h"));
        imageMap.put("m-head2-h", getImage("note-m-0-h"));
        imageMap.put("m-head4-h", getImage("note-m-1-h"));
        imageMap.put("m-head8-h", getImage("note-m-1-h"));
        imageMap.put("m-head16-h", getImage("note-m-1-h"));
        imageMap.put("m-head32-h", getImage("note-m-1-h"));
        imageMap.put("m-head64-h", getImage("note-m-1-h"));
        // rests
        imageMap.put("m-rest1", getImage("b-m-1"));
        imageMap.put("m-rest2", getImage("b-m-2"));
        imageMap.put("m-rest4", getImage("b-m-4"));
        imageMap.put("m-rest8", getImage("b-m-8"));
        imageMap.put("m-rest16", getImage("b-m-16"));
        imageMap.put("m-rest32", getImage("b-m-32"));
        imageMap.put("m-rest64", getImage("b-m-64"));
        //
        imageMap.put("m-flag1", getImage("flagX-m"));
        imageMap.put("m-flag-1", getImage("flagI-m"));
        //
        for (int c = 0; c < 14; c++) {
            imageMap.put("m-clef" + c, getImage("clef-m-" + c));
            imageMap.put("m-clef" + c + "-h", getImage("clef-m-" + c + "-h"));
        }
        imageMap.put("m-sharp", getImage("sgnSharp-m"));
        imageMap.put("m-sharp-h", getImage("sgnSharp-m-h"));
        imageMap.put("m-flat", getImage("sgnFlat-m"));
        imageMap.put("m-flat-h", getImage("sgnFlat-m-h"));
        imageMap.put("m-nat", getImage("sgnNat-m"));
        imageMap.put("m-nat-h", getImage("sgnNat-m-h"));
        //
        imageMap.put("m-sign-2", getImage("sgnFlat2-m"));
        imageMap.put("m-sign-1", getImage("sgnFlat-m"));
        imageMap.put("m-sign1", getImage("sgnSharp-m"));
        imageMap.put("m-sign2", getImage("sgnSharp2-m"));
        imageMap.put("m-sign3", getImage("sgnNat-m"));

        // accents
        for (String accentName : CwnAccent.ACCENTS) {
            imageMap.put("m-"+accentName, getImage("accents/sacc" + accentName + "-m"));
        }
        // symbols
        for (String symbolName : CwnSymbolEvent.SYMBOLS) {
            imageMap.put("m-"+symbolName, getImage("symbols/ssym" + symbolName + "-m"));
            imageMap.put("m-"+symbolName + "-h", getImage("symbols/ssym" + symbolName + "-m-h"));
        }

        // Piano Staff
        imageMap.put("pianostaff1", getImage("pianostaff1"));
        imageMap.put("pianostaff2", getImage("pianostaff2"));
        imageMap.put("m-pianostaff1", getImage("m-pianostaff1"));
        imageMap.put("m-pianostaff2", getImage("m-pianostaff2"));
        imageMap.put("d-pianostaff1", getImage("d-pianostaff1"));
        imageMap.put("d-pianostaff2", getImage("d-pianostaff2"));

        //
        // CORRECTIONS
        //

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

        correctionMap.put("flat", 0);
        correctionMap.put("sharp", 0);
        correctionMap.put("nat", 0);
        correctionMap.put("sign-2", -2);
        correctionMap.put("sign-1", -2);
        correctionMap.put("sign1", -2);
        correctionMap.put("sign2", -2);
        correctionMap.put("sign3", -2);

        // Y-Pos Correction (Medium, zoom=1.5):
        // rests
        correctionMapM.put("rest1", 2);
        correctionMapM.put("rest2", 3);
        correctionMapM.put("rest4", 3);
        correctionMapM.put("rest8", 4);
        correctionMapM.put("rest16", 3);
        correctionMapM.put("rest32", 3);
        correctionMapM.put("rest64", 3);

        correctionMapM.put("flat", 3);
        correctionMapM.put("sharp", 3);
        correctionMapM.put("nat", 3);
        correctionMapM.put("sign-2", 2);
        correctionMapM.put("sign-1", 2);
        correctionMapM.put("sign1", 2);
        correctionMapM.put("sign2", 2);
        correctionMapM.put("sign3", 2);

        // FONTS

        fontMap.put("title", new Font("Arial", Font.PLAIN, (int)(30*zoom)));
        fontMap.put("subtitle", new Font("Arial", Font.PLAIN, (int)(24*zoom)));
        fontMap.put("barNumber", new Font("Arial", Font.ITALIC, (int)(9*zoom)));
        fontMap.put("timeSignature", new Font("Arial", Font.BOLD, (int)(13*zoom)));
        fontMap.put("text", new Font("Arial", Font.PLAIN, (int)(9*zoom)));
        fontMap.put("rangeName", new Font("Arial", Font.BOLD, (int)(10*zoom)));
        fontMap.put("nole", new Font("Arial", Font.ITALIC, (int)(7*zoom)));
        fontMap.put("track", new Font("Arial", Font.PLAIN, (int)(18*zoom)));
        fontMap.put("trackMuted", new Font("Arial", Font.PLAIN, (int)(18*zoom)));
        fontMap.put("lyrics", new Font("Arial", Font.PLAIN, (int)(9*zoom)));
        fontMap.put("markup", new Font("Arial", Font.ITALIC, (int)(9*zoom)));
        fontMap.put("markupBold", new Font("Arial", Font.ITALIC | Font.BOLD, (int)(9*zoom)));
    }

    public Image getImage(String name) {
        try {
            return new ImageIcon(getClass().getResource("/images/" + name + ".png")).getImage();
        } catch (Exception e) {
            System.err.println("NOT FOUND: " + name);
            return null;
        }
    }
}

