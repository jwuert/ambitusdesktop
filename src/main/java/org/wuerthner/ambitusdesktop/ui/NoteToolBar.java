package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.attribute.PositionAttribute;
import org.wuerthner.ambitus.model.Event;
import org.wuerthner.ambitus.model.NoteEvent;
import org.wuerthner.ambitusdesktop.NoteSelector;
import org.wuerthner.ambitusdesktop.ScoreModel;
import org.wuerthner.ambitusdesktop.ScorePanel;
import org.wuerthner.ambitusdesktop.ScoreUpdater;
import org.wuerthner.ambitusdesktop.score.AmbitusSelection;
import org.wuerthner.cwn.api.CwnNoteEvent;
import org.wuerthner.cwn.api.CwnTrack;
import org.wuerthner.cwn.api.Trias;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.ScoreUpdate;
import org.wuerthner.sport.api.Attribute;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Enumeration;

public class NoteToolBar {
    private ButtonGroup gridButtonGroup;
    private ButtonGroup tupletButtonGroup;
    private final ScoreModel scoreModel;
    private final ScoreUpdater scoreUpdater;
    private JToolBar toolbar2;
    private JTextField positionField;
    private JTextField durationField;
    private JTextField velocityField;
    private JTextField voiceField;

    static final Border etchlBorder   = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),BorderFactory.createEmptyBorder(4,4,4,4));
    static final Border loweredBorder = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),BorderFactory.createEmptyBorder(0,4,0,4));

    public NoteToolBar(ScoreModel scoreModel, ScoreUpdater scoreUpdater) {
        this.scoreModel = scoreModel;
        this.scoreUpdater = scoreUpdater;
        //
        // NOTES
        //
        toolbar2 = new JToolBar();
        toolbar2.add(new JLabel("Note: "));
        // note length selector
        ButtonGroup buttonGroup = new ButtonGroup();
        JToggleButton nb0Btn = makeGroupButton("images/buttons/nb0", buttonGroup, NoteSelector.N1, toolbar2);
        makeAction(nb0Btn, KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N1);
            updateSelector(buttonGroup, NoteSelector.N1);
            // updateSelector(gridButtonGroup, NoteSelector.N4);
            // scoreModel.setGridSelector(NoteSelector.N4);
            });
        JToggleButton nb1Btn = makeGroupButton("images/buttons/nb1", buttonGroup, NoteSelector.N2, toolbar2);
        makeAction(nb1Btn, KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N2);
            updateSelector(buttonGroup, NoteSelector.N2);
            // updateSelector(gridButtonGroup, NoteSelector.N4);
            // scoreModel.setGridSelector(NoteSelector.N4);
            });
        JToggleButton nb2Btn = makeGroupButton("images/buttons/nb2", buttonGroup, NoteSelector.N4, toolbar2);
        makeAction(nb2Btn, KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N4);
            updateSelector(buttonGroup, NoteSelector.N4);
            // updateSelector(gridButtonGroup, NoteSelector.N8);
            // scoreModel.setGridSelector(NoteSelector.N8);
            });
        JToggleButton nb3Btn = makeGroupButton("images/buttons/nb3", buttonGroup, NoteSelector.N8, toolbar2);
        makeAction(nb3Btn, KeyEvent.VK_4, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N8);
            updateSelector(buttonGroup, NoteSelector.N8);
            // updateSelector(gridButtonGroup, NoteSelector.N16);
            // scoreModel.setGridSelector(NoteSelector.N16);
        });
        JToggleButton nb4Btn = makeGroupButton("images/buttons/nb4", buttonGroup, NoteSelector.N16, toolbar2);
        makeAction(nb4Btn, KeyEvent.VK_5, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N16);
            updateSelector(buttonGroup, NoteSelector.N16);
            // updateSelector(gridButtonGroup, NoteSelector.N16);
            // scoreModel.setGridSelector(NoteSelector.N16);
        });
        JToggleButton nb5Btn = makeGroupButton("images/buttons/nb5", buttonGroup, NoteSelector.N32, toolbar2);
        makeAction(nb5Btn, KeyEvent.VK_6, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N32);
            updateSelector(buttonGroup, NoteSelector.N32);
            // updateSelector(gridButtonGroup, NoteSelector.N32);
            // scoreModel.setGridSelector(NoteSelector.N32);
        });
        JToggleButton nb6Btn = makeGroupButton("images/buttons/nb6", buttonGroup, NoteSelector.N64, toolbar2);
        makeAction(nb6Btn, KeyEvent.VK_7, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N64);
            updateSelector(buttonGroup, NoteSelector.N64);
            // updateSelector(gridButtonGroup, NoteSelector.N64);
            // scoreModel.setGridSelector(NoteSelector.N64);
        });

        //
        // GRID
        //
        toolbar2.addSeparator(new Dimension(10, 40));
        toolbar2.add(new JLabel("Grid: "));
        // grid length selector
        gridButtonGroup = new ButtonGroup();
        JToggleButton nb1Btn2 = makeGroupButton("images/buttons/nb1", gridButtonGroup, NoteSelector.N2, toolbar2);
        makeAction(nb1Btn2, KeyEvent.VK_2, KeyEvent.SHIFT_DOWN_MASK, () -> {
            scoreModel.setGridSelector(NoteSelector.N2);
            updateSelector(gridButtonGroup, NoteSelector.N2);
        });
        JToggleButton nb2Btn2 = makeGroupButton("images/buttons/nb2", gridButtonGroup, NoteSelector.N4, toolbar2);
        makeAction(nb2Btn2, KeyEvent.VK_3, KeyEvent.SHIFT_DOWN_MASK, () -> {
            scoreModel.setGridSelector(NoteSelector.N4);
            updateSelector(gridButtonGroup, NoteSelector.N4);
        });
        JToggleButton nb3Btn2 = makeGroupButton("images/buttons/nb3", gridButtonGroup, NoteSelector.N8, toolbar2);
        makeAction(nb3Btn2, KeyEvent.VK_4, KeyEvent.SHIFT_DOWN_MASK, () -> {
            scoreModel.setGridSelector(NoteSelector.N8);
            updateSelector(gridButtonGroup, NoteSelector.N8);
        });
        JToggleButton nb4Btn2 = makeGroupButton("images/buttons/nb4", gridButtonGroup, NoteSelector.N16, toolbar2);
        makeAction(nb4Btn2, KeyEvent.VK_5, KeyEvent.SHIFT_DOWN_MASK, () -> {
            scoreModel.setGridSelector(NoteSelector.N16);
            updateSelector(gridButtonGroup, NoteSelector.N16);
        });
        JToggleButton nb5Btn2 = makeGroupButton("images/buttons/nb5", gridButtonGroup, NoteSelector.N32, toolbar2);
        makeAction(nb5Btn2, KeyEvent.VK_6, KeyEvent.SHIFT_DOWN_MASK, () -> {
            scoreModel.setGridSelector(NoteSelector.N32);
            updateSelector(gridButtonGroup, NoteSelector.N32);
        });
        JToggleButton nb6Btn2 = makeGroupButton("images/buttons/nb6", gridButtonGroup, NoteSelector.N64, toolbar2);
        makeAction(nb6Btn2, KeyEvent.VK_7, KeyEvent.SHIFT_DOWN_MASK, () -> {
            scoreModel.setGridSelector(NoteSelector.N64);
            updateSelector(gridButtonGroup, NoteSelector.N64);
        });

        //
        // TUPLETS
        //
        toolbar2.addSeparator(new Dimension(10, 40));
        toolbar2.add(new JLabel("Tuplets: "));

        tupletButtonGroup = new ButtonGroup();
        JToggleButton tupletBtn1 = makeGroupButton("images/buttons/empty", tupletButtonGroup, NoteSelector.T1, toolbar2);
        makeAction(tupletBtn1, KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK, () -> {
            scoreModel.setTupletSelector(NoteSelector.T1);
            updateSelector(tupletButtonGroup, NoteSelector.T1);
        });

        JToggleButton tupletBtn2 = makeGroupButton("images/buttons/nbTuplet2", tupletButtonGroup, NoteSelector.T2, toolbar2);
        makeAction(tupletBtn2, KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, () -> {
            scoreModel.setTupletSelector(NoteSelector.T2);
            updateSelector(tupletButtonGroup, NoteSelector.T2);
        });

        JToggleButton tupletBtn3 = makeGroupButton("images/buttons/nbTuplet3", tupletButtonGroup, NoteSelector.T3, toolbar2);
        makeAction(tupletBtn3, KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK, () -> {
            scoreModel.setTupletSelector(NoteSelector.T3);
            updateSelector(tupletButtonGroup, NoteSelector.T3);
        });

        JToggleButton tupletBtn4 = makeGroupButton("images/buttons/nbTuplet4", tupletButtonGroup, NoteSelector.T4, toolbar2);
        makeAction(tupletBtn4, KeyEvent.VK_4, KeyEvent.ALT_DOWN_MASK, () -> {
            scoreModel.setTupletSelector(NoteSelector.T4);
            updateSelector(tupletButtonGroup, NoteSelector.T4);
        });

        JToggleButton tupletBtn5 = makeGroupButton("images/buttons/nbTuplet5", tupletButtonGroup, NoteSelector.T5, toolbar2);
        makeAction(tupletBtn5, KeyEvent.VK_5, KeyEvent.ALT_DOWN_MASK, () -> {
            scoreModel.setTupletSelector(NoteSelector.T5);
            updateSelector(tupletButtonGroup, NoteSelector.T5);
        });

        JToggleButton tupletBtn6 = makeGroupButton("images/buttons/nbTuplet6", tupletButtonGroup, NoteSelector.T6, toolbar2);
        makeAction(tupletBtn6, KeyEvent.VK_6, KeyEvent.ALT_DOWN_MASK, () -> {
            scoreModel.setTupletSelector(NoteSelector.T6);
            updateSelector(tupletButtonGroup, NoteSelector.T6);
        });

        JToggleButton tupletBtn7 = makeGroupButton("images/buttons/nbTuplet7", tupletButtonGroup, NoteSelector.T7, toolbar2);
        makeAction(tupletBtn7, KeyEvent.VK_7, KeyEvent.ALT_DOWN_MASK, () -> {
            scoreModel.setTupletSelector(NoteSelector.T7);
            updateSelector(tupletButtonGroup, NoteSelector.T7);
        });


        // default values
        updateSelector(buttonGroup, scoreModel.getNoteSelector());
        updateSelector(gridButtonGroup, scoreModel.getGridSelector());
        updateSelector(tupletButtonGroup, scoreModel.getTupletSelector());

        //
        toolbar2.addSeparator(new Dimension(20, 40));
        positionField = createTextField(NoteEvent.position, toolbar2);
        durationField = createTextField(NoteEvent.duration, toolbar2);
        velocityField = createTextField(NoteEvent.velocity, toolbar2);
        voiceField = createTextField(NoteEvent.voice, toolbar2);

    }

    private JTextField createTextField(Attribute<?> attribute, JToolBar toolbar) {
        String label = attribute.getLabel() + ": ";
        toolbar.addSeparator(new Dimension(10, 40));
        toolbar.add(new JLabel(label));
        JTextField field = new JTextField(12);
        field.setMaximumSize(new Dimension(120, 24));
        field.setBorder(etchlBorder);
        field.addActionListener(new FieldActionListener(field, attribute));
        field.addFocusListener(new MainFocusListener());
        toolbar.add(field);
        return field;
    }

    private void clearField(JTextField field) {
        field.setText("");
        field.setEnabled(false);
        field.setBackground(Color.lightGray);
        field.setForeground(Color.black);
        field.setBorder(etchlBorder);
    }

    private void setField(JTextField field, String text) {
        field.setEnabled(true);
        field.setText(text);
    }

    public void setSelection(AmbitusSelection selection) {
        if (!selection.hasSingleSelection()) {
            clearField(positionField);
            clearField(durationField);
            clearField(velocityField);
            clearField(voiceField);
            toolbar2.getRootPane().requestFocus();
        } else {
            Event event = selection.getSingleSelection();
            CwnTrack track = scoreModel.getTrackList().get(0);
            Trias trias = PositionTools.getTrias(track, event.getPosition());
            setField(positionField, trias.toFormattedString());
            setField(durationField, ""+event.getDuration());
            if (event instanceof CwnNoteEvent) {
                CwnNoteEvent noteEvent = (CwnNoteEvent) event;
                setField(velocityField, ""+noteEvent.getVelocity());
                setField(voiceField, ""+noteEvent.getVoice());
            } else {
                clearField(velocityField);
                clearField(voiceField);
            }
        }
    }

    public JToolBar getToolBar() {
        return toolbar2;
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
    private JToggleButton makeGroupButton(String function, ButtonGroup buttonGroup, Object data, JToolBar toolbar) {
        return makeGroupButton(function, buttonGroup, data, toolbar, -1);
    }

    private JToggleButton makeGroupButton(String function, ButtonGroup buttonGroup, Object data, JToolBar toolbar, int size) {
        URL url = getClass().getResource("/" + function + ".png");
        ImageIcon icon = new ImageIcon(url);
        if (size > 0) {
            icon = new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
        }
        JToggleButton button = new JToggleButton(icon);
        button.setFocusPainted(false);
        button.putClientProperty("data", data);
        buttonGroup.add(button);
        toolbar.add(button);
        return button;
    }
    private void updateSelector(ButtonGroup group, NoteSelector grid) {
        Enumeration<AbstractButton> elements = group.getElements();
        while (elements.hasMoreElements()) {
            AbstractButton button = elements.nextElement();
            NoteSelector buttonGrid = (NoteSelector) button.getClientProperty("data");
            if (grid==buttonGrid) {
                button.setSelected(true);
            }
        }
    }

    private class FieldActionListener<T> implements ActionListener {
        private final JTextField field;
        private final Attribute<T> attribute;

        public FieldActionListener(JTextField field, Attribute<T> attribute) {
            this.field = field;
            this.attribute = attribute;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (scoreModel.getSelection().hasSingleSelection()) {
                CwnTrack track = scoreModel.getTrackList().get(0);
                Event event = scoreModel.getSelection().getSingleSelection();
                String value = field.getText();
                if (attribute==NoteEvent.position) {
                    long position = PositionTools.getPosition(track, value);
                    scoreModel.getArrangement().setEventAttribute(event, (PositionAttribute) attribute, position);
                } else {
                    scoreModel.getArrangement().setEventAttribute(event, attribute, attribute.getValue(value));
                }

                scoreUpdater.update(new ScoreUpdate(track, scoreModel.getSelection()).extendRangeByOneBar());
            }
            toolbar2.getRootPane().requestFocus();
        }
    }

    private class MainFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            JComponent f = (JComponent) e.getSource();
            f.setBackground(Color.gray);
            f.setForeground(Color.white);
            f.setBorder(loweredBorder);
        }

        public void focusLost(FocusEvent e) {
            JComponent f = (JComponent) e.getSource();
            f.setBackground(Color.lightGray);
            f.setForeground(Color.black);
            f.setBorder(etchlBorder);
        }
    }
}
