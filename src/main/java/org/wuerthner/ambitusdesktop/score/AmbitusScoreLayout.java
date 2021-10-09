package org.wuerthner.ambitusdesktop.score;

import org.wuerthner.cwn.api.ScoreLayout;

public class AmbitusScoreLayout implements ScoreLayout {
    private int width;
    private double ppt;
    private boolean hasLyrics;
    private int lyricsSpace = 5;
    private boolean showVelocity;

    public AmbitusScoreLayout(int width, int ppq, boolean showVelocity) {
        this.width = width;
        this.ppt = 0.004 * 960.0 / ppq;
        this.hasLyrics = true;
        this.showVelocity = showVelocity;
    }

    @Override
    public int getBorder() {
        return 15;
    }

    @Override
    public int getTitleHeight() {
        return 80;
    }

    @Override
    public int lyricsSpace() {
        return (hasLyrics ? lyricsSpace : 0);
    }

    @Override
    public int getSystemSpace() {
        return 20 + lyricsSpace();
    }

    @Override
    public int getSystemIndent() {
        return 80;
    }

    @Override
    public int getStemLength() {
        return 21;
    }

    @Override
    public double getPixelPerTick() {
        return ppt;
    }

    @Override
    public boolean showGrid() {
        return false;
    }

    @Override
    public boolean showVelocity() {
        return showVelocity;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public boolean hasFullTupletPresentation() {
        return false;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setPPQ(double ppq) {
        this.ppt = 0.004 * 960.0 / ppq;
    }

    public void setShowVelocity(boolean showVelocity) {
        this.showVelocity = showVelocity;
    }
}
