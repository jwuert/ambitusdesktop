package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.attribute.PositionAttribute;
import org.wuerthner.ambitus.model.Event;
import org.wuerthner.ambitus.model.NoteEvent;
import org.wuerthner.ambitus.model.SymbolEvent;
import org.wuerthner.ambitusdesktop.NoteSelector;
import org.wuerthner.ambitusdesktop.ScoreModel;
import org.wuerthner.ambitusdesktop.ScoreUpdater;
import org.wuerthner.ambitusdesktop.ToolbarUpdater;
import org.wuerthner.ambitusdesktop.score.AmbitusSelection;
import org.wuerthner.cwn.api.*;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.ScoreUpdate;
import org.wuerthner.sport.api.Attribute;

import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.List;

public class SymbolToolBar {
    private final ScoreModel scoreModel;
    private final ScoreUpdater scoreUpdater;
    private final ToolbarUpdater toolbarUpdater;
    private final JToolBar toolbar2;
    private final ButtonGroup buttonGroup;
    private JTextField positionField;
    private JTextField durationField;
    private JTextField parameter1Field;
    private JTextField parameter2Field;
    private Map<String,NoteSelector> accentMap = new LinkedHashMap<>();
    private Map<String,NoteSelector> symbolMap = new LinkedHashMap<>();

    static final Color buttonColor = new Color(236,236,236);

    public SymbolToolBar(ScoreModel scoreModel, ScoreUpdater scoreUpdater, ToolbarUpdater toolbarUpdater) {
        this.scoreModel = scoreModel;
        this.scoreUpdater = scoreUpdater;
        this.toolbarUpdater = toolbarUpdater;
        init();
        buttonGroup = new ButtonGroup();
        //
        // Accents
        //
        toolbar2 = new JToolBar();
        toolbar2.add(new JLabel("Accent: "));
        // accent selector
        List<String> accentList = new ArrayList<>(accentMap.keySet());
        for (int i=0; i<accentList.size(); i=i+2) {
            JPanel pair = new JPanel();
            pair.setLayout(new BoxLayout(pair, BoxLayout.Y_AXIS));
            for (int j=0; j<2; j++) {
                String accent = accentList.get(i+j);
                NoteSelector accentSelector = accentMap.get(accent);
                JToggleButton btn = makeGroupButton("images/accents/acc" + accent, buttonGroup, accentSelector, pair, 14);
                if (accentSelector!=null) {
                    makeAction(btn, KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK, () -> {
                        scoreModel.setNoteSelector(accentSelector);
                        // updateSelector(accentSelector);
                        toolbarUpdater.updateNoteOrAccent();
                    });
                }
            }
            pair.setMaximumSize(new Dimension(29, 50));
            toolbar2.add(pair);
        }

        //
        // GRID
        //
        List<String> symbolList = new ArrayList<>(symbolMap.keySet());
        toolbar2.addSeparator(new Dimension(10, 40));
        toolbar2.add(new JLabel("Symbol: "));
        // grid length selector
        for (int i=0; i<symbolList.size(); i=i+2) {
            JPanel pair = new JPanel();
            pair.setLayout(new BoxLayout(pair, BoxLayout.Y_AXIS));
            for (int j=0; j<2; j++) {
                String symbol = symbolList.get(i + j);
                NoteSelector symbolSelector = symbolMap.get(symbol);
                JToggleButton nb1Btn2 = makeGroupButton("images/symbols/sym" + symbol, buttonGroup, symbolSelector, pair, 14);
                if (symbolSelector!=null) {
                    makeAction(nb1Btn2, KeyEvent.VK_2, KeyEvent.SHIFT_DOWN_MASK, () -> {
                        scoreModel.setNoteSelector(symbolSelector);
                        // updateSelector(symbolSelector);
                        toolbarUpdater.updateNoteOrAccent();
                    });
                }
            }
            pair.setMaximumSize(new Dimension(29, 50));
            toolbar2.add(pair);
        }

        //
        toolbar2.addSeparator(new Dimension(20, 40));

//        positionField = createTextField(SymbolEvent.position, toolbar2, 120);
//        durationField = createTextField(SymbolEvent.duration, toolbar2, 40);
//
//        parameter1Field = createTextField(SymbolEvent.verticalOffset, toolbar2, 40);
//        parameter2Field = createTextField(SymbolEvent.parameter, toolbar2, 40);

    }

    public JToolBar getToolBar() {
        return toolbar2;
    }

    public void setVisible(boolean visible) {
        toolbar2.setVisible(visible);
    }

    private void makeAction(JButton button, Runnable runnable) {
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runnable.run();
            }
        });
    }

    private void makeAction(JToggleButton button, int key, int modifier, Runnable runnable) {
        AbstractAction action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {

                runnable.run();
            }
        };
        button.addActionListener(action);
        if (key!=0) {
            String mapKey = "k"+(key+modifier);
            button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key, modifier), mapKey);
            button.getActionMap().put(mapKey, action);
        }
    }
    private JToggleButton makeGroupButton(String function, ButtonGroup buttonGroup, Object data, Container toolbar) {
        return makeGroupButton(function, buttonGroup, data, toolbar, -1);
    }

    private JToggleButton makeGroupButton(String function, ButtonGroup buttonGroup, Object data, Container toolbar, int size) {
        URL url = getClass().getResource("/" + function + ".png");
        JToggleButton button = null;
        try {
            ImageIcon icon = new ImageIcon(url);
            if (size > 0) {
                icon = new ImageIcon(icon.getImage().getScaledInstance((int)(size*1.4), size, Image.SCALE_SMOOTH));
            }
            button = new JToggleButton(icon);
            button.setMargin(new Insets(2, 4, 2, 4));
            button.setFocusPainted(false);
            button.putClientProperty("data", data);
            button.setBackground(buttonColor);
            button.setOpaque(true);
            // button.setBorder(etchlBorder);
            buttonGroup.add(button);
            toolbar.add(button);
        } catch (Exception e) {
            System.err.println("Not found: " + url + ", " + "/" + function + ".png");
            e.printStackTrace();
        }
        return button;
    }
    public void updateSelector(NoteSelector grid) {
        buttonGroup.clearSelection();
        Enumeration<AbstractButton> elements = buttonGroup.getElements();
        while (elements.hasMoreElements()) {
            AbstractButton button = elements.nextElement();
            NoteSelector buttonGrid = (NoteSelector) button.getClientProperty("data");
            if (grid==buttonGrid) {
                button.setSelected(true);
            }
        }
    }

    private void init() {
        for (String a : CwnAccent.ACCENTS) {
            accentMap.put(a, NoteSelector.get(a));
        }
        if (accentMap.size()%2!=0) {
            accentMap.put("Empty", null);
        }

        for (String s : CwnSymbolEvent.SYMBOLS) {
            symbolMap.put(s, NoteSelector.get(s));
        }
        if (symbolMap.size()%2!=0) {
            symbolMap.put("Empty", null);
        }
    }
/*
    private JTextField createTextField(Attribute<?> attribute, JToolBar toolbar, int size) {
        String label = attribute.getLabel() + ": ";
        toolbar.addSeparator(new Dimension(10, 40));
        toolbar.add(new JLabel(label));
        JTextField field = new JTextField(12);
        field.setMaximumSize(new Dimension(size, 24));
        field.setBorder(FieldFocusListener.etchlBorder);
        field.addActionListener(new FieldActionListener(field, attribute, scoreModel, scoreUpdater));
        field.addFocusListener(new FieldFocusListener(scoreModel));
        toolbar.add(field);
        return field;
    }

    private void clearField(JTextField field) {
        field.setText("");
        field.setEnabled(false);
        field.setBackground(Color.lightGray);
        field.setForeground(Color.black);
        field.setBorder(FieldFocusListener.etchlBorder);
    }

    private void setField(JTextField field, String text) {
        field.setEnabled(true);
        field.setText(text);
    }

    public void clearSelection() {
        clearField(positionField);
        clearField(durationField);
        clearField(parameter1Field);
        clearField(parameter2Field);
    }

    public void setSelection(AmbitusSelection selection) {
        if (!selection.hasSingleSelection()) {
            clearField(positionField);
            clearField(durationField);
            clearField(parameter1Field);
            clearField(parameter2Field);
            toolbar2.getRootPane().requestFocus();
        } else {
            Event event = selection.getSingleSelection();
            CwnTrack track = scoreModel.getTrackList().get(0);
            Trias trias = PositionTools.getTrias(track, event.getPosition());
            setField(positionField, trias.toFormattedString());
            setField(durationField, ""+event.getDuration());
            if (event instanceof CwnNoteEvent) {
                CwnNoteEvent noteEvent = (CwnNoteEvent) event;
                setField(parameter1Field, ""+noteEvent.getVelocity());
                setField(parameter2Field, ""+noteEvent.getVoice());
            } else {
                clearField(parameter1Field);
                clearField(parameter2Field);
            }
        }
    }
*/
    public void setSelection(AmbitusSelection selection) {
    }
    public void clearSelection() {
    }
}
