package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitusdesktop.ScoreModel;
import org.wuerthner.cwn.score.Score;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class FieldFocusListener implements FocusListener {
    static final Border etchlBorder   = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),BorderFactory.createEmptyBorder(4,4,4,4));
    static final Border loweredBorder = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),BorderFactory.createEmptyBorder(0,4,0,4));
    private final ScoreModel scoreModel;
    private final Color fgColor;

    public FieldFocusListener(ScoreModel scoreModel) {
        this(scoreModel, Color.black);
    }

    public FieldFocusListener(ScoreModel scoreModel, Color fgColor) {
        this.scoreModel = scoreModel;
        this.fgColor = fgColor;
    }

    public void focusGained(FocusEvent e) {
        scoreModel.setKeyboardShortcutsActive(false);
        JComponent f = (JComponent) e.getSource();
        f.setBackground(Color.gray);
        f.setForeground(Color.white);
        f.setBorder(loweredBorder);
    }

    public void focusLost(FocusEvent e) {
        JComponent f = (JComponent) e.getSource();
        f.setBackground(Color.lightGray);
        f.setForeground(fgColor);
        f.setBorder(etchlBorder);
        scoreModel.setKeyboardShortcutsActive(true);
    }
}
