package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.model.Event;
import org.wuerthner.ambitus.model.NoteEvent;
import org.wuerthner.ambitus.model.SymbolEvent;
import org.wuerthner.ambitusdesktop.*;
import org.wuerthner.ambitusdesktop.score.AmbitusSelection;
import org.wuerthner.cwn.api.CwnNoteEvent;
import org.wuerthner.cwn.api.CwnTrack;
import org.wuerthner.cwn.api.Markup;
import org.wuerthner.cwn.api.Trias;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.ScoreUpdate;
import org.wuerthner.sport.api.Attribute;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Enumeration;

public class NoteToolBar {
    private ButtonGroup noteButtonGroup;
    private ButtonGroup gridButtonGroup;
    private ButtonGroup tupletButtonGroup;
    private ButtonGroup voiceButtonGroup;
    private ButtonGroup levelButtonGroup;
    private final ScoreModel scoreModel;
    private final ScoreUpdater scoreUpdater;
    private final ToolbarUpdater toolbarUpdater;
    private JToolBar toolbar2;
//    private JTextField positionField;
//    private JTextField durationField;
//    private JTextField velocityField;
//    private JTextField voiceField;
    private JLabel positionText;
    private JLabel durationText;
    private JLabel velocityText;
    private JLabel voiceText;
    private Event selectedEvent = null;

    private JToggleButton flagBtn1;
    private JToggleButton flagBtn2;
    private JToggleButton flagBtn3;
    private JToggleButton flagBtn4;
    private JToggleButton flagBtn5;
    private JToggleButton flagBtn6;
    private JToggleButton flagBtn7;
    private JToggleButton flagBtn8;
    private JToggleButton flagBtn9;
    private JToggleButton flagBtn10;
    private JToggleButton flagBtn11;

    public NoteToolBar(ScoreModel scoreModel, ScoreUpdater scoreUpdater, ToolbarUpdater toolbarUpdater, JPanel content) {
        this.scoreModel = scoreModel;
        this.scoreUpdater = scoreUpdater;
        this.toolbarUpdater = toolbarUpdater;
        //
        // NOTES
        //
        toolbar2 = new JToolBar();
        toolbar2.add(new JLabel("Note: "));
        // note length selector
        noteButtonGroup = new ButtonGroup();
        JToggleButton nb0Btn = makeGroupButton("images/buttons/nb0", noteButtonGroup, NoteSelector.N1, toolbar2);
        makeAction(nb0Btn, KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N1);
            toolbarUpdater.updateNoteOrAccent();
            // updateSelector(noteButtonGroup, NoteSelector.N1);
            // updateSelector(gridButtonGroup, NoteSelector.N4);
            // scoreModel.setGridSelector(NoteSelector.N4);
            });
        JToggleButton nb1Btn = makeGroupButton("images/buttons/nb1", noteButtonGroup, NoteSelector.N2, toolbar2);
        makeAction(nb1Btn, KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N2);
            toolbarUpdater.updateNoteOrAccent();
            // updateSelector(noteButtonGroup, NoteSelector.N2);
            // updateSelector(gridButtonGroup, NoteSelector.N4);
            // scoreModel.setGridSelector(NoteSelector.N4);
            });
        JToggleButton nb2Btn = makeGroupButton("images/buttons/nb2", noteButtonGroup, NoteSelector.N4, toolbar2);
        makeAction(nb2Btn, KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N4);
            toolbarUpdater.updateNoteOrAccent();
            // updateSelector(noteButtonGroup, NoteSelector.N4);
            // updateSelector(gridButtonGroup, NoteSelector.N8);
            // scoreModel.setGridSelector(NoteSelector.N8);
            });
        JToggleButton nb3Btn = makeGroupButton("images/buttons/nb3", noteButtonGroup, NoteSelector.N8, toolbar2);
        makeAction(nb3Btn, KeyEvent.VK_4, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N8);
            toolbarUpdater.updateNoteOrAccent();
            // updateSelector(noteButtonGroup, NoteSelector.N8);
            // updateSelector(gridButtonGroup, NoteSelector.N16);
            // scoreModel.setGridSelector(NoteSelector.N16);
        });
        JToggleButton nb4Btn = makeGroupButton("images/buttons/nb4", noteButtonGroup, NoteSelector.N16, toolbar2);
        makeAction(nb4Btn, KeyEvent.VK_5, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N16);
            toolbarUpdater.updateNoteOrAccent();
            // updateSelector(noteButtonGroup, NoteSelector.N16);
            // updateSelector(gridButtonGroup, NoteSelector.N16);
            // scoreModel.setGridSelector(NoteSelector.N16);
        });
        JToggleButton nb5Btn = makeGroupButton("images/buttons/nb5", noteButtonGroup, NoteSelector.N32, toolbar2);
        makeAction(nb5Btn, KeyEvent.VK_6, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N32);
            toolbarUpdater.updateNoteOrAccent();
            // updateSelector(noteButtonGroup, NoteSelector.N32);
            // updateSelector(gridButtonGroup, NoteSelector.N32);
            // scoreModel.setGridSelector(NoteSelector.N32);
        });
        JToggleButton nb6Btn = makeGroupButton("images/buttons/nb6", noteButtonGroup, NoteSelector.N64, toolbar2);
        makeAction(nb6Btn, KeyEvent.VK_7, KeyEvent.CTRL_DOWN_MASK, () -> {
            scoreModel.setNoteSelector(NoteSelector.N64);
            toolbarUpdater.updateNoteOrAccent();
            // updateSelector(noteButtonGroup, NoteSelector.N64);
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

//        JToggleButton tupletBtn2 = makeGroupButton("images/buttons/nbTuplet2", tupletButtonGroup, NoteSelector.T2, toolbar2);
//        makeAction(tupletBtn2, KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK, () -> {
//            scoreModel.setTupletSelector(NoteSelector.T2);
//            updateSelector(tupletButtonGroup, NoteSelector.T2);
//        });

        JToggleButton tupletBtn3 = makeGroupButton("images/buttons/nbTuplet3", tupletButtonGroup, NoteSelector.T3, toolbar2);
        makeAction(tupletBtn3, KeyEvent.VK_3, KeyEvent.ALT_DOWN_MASK, () -> {
            scoreModel.setTupletSelector(NoteSelector.T3);
            updateSelector(tupletButtonGroup, NoteSelector.T3);
        });

//        JToggleButton tupletBtn4 = makeGroupButton("images/buttons/nbTuplet4", tupletButtonGroup, NoteSelector.T4, toolbar2);
//        makeAction(tupletBtn4, KeyEvent.VK_4, KeyEvent.ALT_DOWN_MASK, () -> {
//            scoreModel.setTupletSelector(NoteSelector.T4);
//            updateSelector(tupletButtonGroup, NoteSelector.T4);
//        });

        JToggleButton tupletBtn5 = makeGroupButton("images/buttons/nbTuplet5", tupletButtonGroup, NoteSelector.T5, toolbar2);
        makeAction(tupletBtn5, KeyEvent.VK_5, KeyEvent.ALT_DOWN_MASK, () -> {
            scoreModel.setTupletSelector(NoteSelector.T5);
            updateSelector(tupletButtonGroup, NoteSelector.T5);
        });

//        JToggleButton tupletBtn6 = makeGroupButton("images/buttons/nbTuplet6", tupletButtonGroup, NoteSelector.T6, toolbar2);
//        makeAction(tupletBtn6, KeyEvent.VK_6, KeyEvent.ALT_DOWN_MASK, () -> {
//            scoreModel.setTupletSelector(NoteSelector.T6);
//            updateSelector(tupletButtonGroup, NoteSelector.T6);
//        });
//
//        JToggleButton tupletBtn7 = makeGroupButton("images/buttons/nbTuplet7", tupletButtonGroup, NoteSelector.T7, toolbar2);
//        makeAction(tupletBtn7, KeyEvent.VK_7, KeyEvent.ALT_DOWN_MASK, () -> {
//            scoreModel.setTupletSelector(NoteSelector.T7);
//            updateSelector(tupletButtonGroup, NoteSelector.T7);
//        });


        //
        // VOICES
        //
        toolbar2.addSeparator(new Dimension(10, 40));
        toolbar2.add(new JLabel("Voice: "));

        voiceButtonGroup = new ButtonGroup();
        JToggleButton voiceBtn1 = makeGroupButton("images/buttons/v1", voiceButtonGroup, NoteSelector.V1, toolbar2);
        makeAction(voiceBtn1, KeyEvent.VK_1, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            scoreModel.setVoiceSelector(NoteSelector.V1);
            updateSelector(voiceButtonGroup, NoteSelector.V1);
        });

        JToggleButton voiceBtn2 = makeGroupButton("images/buttons/v2", voiceButtonGroup, NoteSelector.V2, toolbar2);
        makeAction(voiceBtn2, KeyEvent.VK_2, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            scoreModel.setVoiceSelector(NoteSelector.V2);
            updateSelector(voiceButtonGroup, NoteSelector.V2);
        });


        toolbar2.addSeparator(new Dimension(20, 40));
        toolbar2.add(new JLabel("Editor: "));
        //
        JButton noteAttributesBtn = makeButton("toolbar/edit", "Note Attributes",12);
        AbstractAction noteAttributesAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedEvent instanceof NoteEvent) {
                    new AttributeEditorNote((NoteEvent) selectedEvent, scoreModel, scoreUpdater).init(content);
                } else if (selectedEvent instanceof SymbolEvent) {
                    new AttributeEditorSymbol((SymbolEvent) selectedEvent, scoreModel, scoreUpdater).init(content);
                }

            }
        };
        noteAttributesBtn.addActionListener(noteAttributesAction);
        noteAttributesBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK), "Editor");
        noteAttributesBtn.getActionMap().put("Editor", noteAttributesAction);
        toolbar2.add(noteAttributesBtn);

        //
        // METRIC GROUP LEVEL
        //
        toolbar2.addSeparator(new Dimension(20, 40));
        toolbar2.add(new JLabel("Group Level: "));

        levelButtonGroup = new ButtonGroup();
        JToggleButton levelBtn1 = makeGroupButton("images/buttons/v1", levelButtonGroup, NoteSelector.L1, toolbar2);
        makeAction(levelBtn1, KeyEvent.VK_NUMPAD1, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            scoreModel.setLevelSelector(1);
            updateSelector(levelButtonGroup, NoteSelector.L1);
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
        });

        JToggleButton levelBtn2 = makeGroupButton("images/buttons/v2", levelButtonGroup, NoteSelector.L2, toolbar2);
        makeAction(levelBtn2, KeyEvent.VK_NUMPAD2, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            scoreModel.setLevelSelector(2);
            updateSelector(levelButtonGroup, NoteSelector.L2);
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
        });

        JToggleButton levelBtn3 = makeGroupButton("images/buttons/v3", levelButtonGroup, NoteSelector.L3, toolbar2);
        makeAction(levelBtn3, KeyEvent.VK_NUMPAD3, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            scoreModel.setLevelSelector(3);
            updateSelector(levelButtonGroup, NoteSelector.L3);
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
        });


        // default values
        updateSelector(noteButtonGroup, scoreModel.getNoteSelector());
        updateSelector(gridButtonGroup, scoreModel.getGridSelector());
        updateSelector(tupletButtonGroup, scoreModel.getTupletSelector());
        updateSelector(voiceButtonGroup, scoreModel.getVoiceSelector());
        int level = scoreModel.getGroupLevel();
        updateSelector(levelButtonGroup, level==1 ? NoteSelector.L1 : level==2 ? NoteSelector.L2 : level==3 ? NoteSelector.L3 : NoteSelector.N1);
        //


        // MARKUP
        toolbar2.addSeparator(new Dimension(20, 40));
        toolbar2.add(new JLabel("Markup: "));

        // FLAGS:   AMBITUS, ATTRIBUTES, PARALLELS, INTERVALS, CROSSINGS, LYRICS, NOTE_ATTRIBUTES, COLOR_VOICES, HARMONY, RIEMANN, VELOCITY

        flagBtn1 = makeToggleButton("toolbar/flagAmbitus", "Ambitus", 12);
        makeAction(flagBtn1, KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            if (!flagBtn1.isSelected()) {
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.AMBITUS);
            } else {
                scoreModel.getScoreBuilder().getScoreParameter().markup.add(Markup.Type.AMBITUS);
            }
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
        });
        toolbar2.add(flagBtn1);

        flagBtn2 = makeToggleButton("toolbar/flagAttributes", "Track Attributes", 12);
        makeAction(flagBtn2, KeyEvent.VK_T, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            if (!flagBtn2.isSelected()) {
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.ATTRIBUTES);
            } else {
                scoreModel.getScoreBuilder().getScoreParameter().markup.add(Markup.Type.ATTRIBUTES);
            }
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
        });
        toolbar2.add(flagBtn2);

        flagBtn7 = makeToggleButton("toolbar/flagNoteAttributes", "Note Attributes", 12);
        makeAction(flagBtn7, KeyEvent.VK_N, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            if (!flagBtn7.isSelected()) {
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.NOTE_ATTRIBUTES);
            } else {
                scoreModel.getScoreBuilder().getScoreParameter().markup.add(Markup.Type.NOTE_ATTRIBUTES);
            }
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
        });
        toolbar2.add(flagBtn7);

        flagBtn8 = makeToggleButton("toolbar/flagColors", "Color Voices", 12);
        makeAction(flagBtn8, KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            if (!flagBtn8.isSelected()) {
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.COLOR_VOICES);
            } else {
                scoreModel.getScoreBuilder().getScoreParameter().markup.add(Markup.Type.COLOR_VOICES);
            }
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
        });
        toolbar2.add(flagBtn8);

        toolbar2.addSeparator(new Dimension(10, 40));

        // flagBtn: 3 (Par), 4 (Int), 5 (Cros), 6 (Lyr), 9 (Harmony), 10 (Riemann), 11 (Velocity) belong to one radio group!
        flagBtn3 = makeToggleButton("toolbar/flagParallels", "Parallels", 12);
        makeAction(flagBtn3, KeyEvent.VK_P, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            if (!flagBtn3.isSelected()) {
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.PARALLELS);
            } else {
                scoreModel.getScoreBuilder().getScoreParameter().markup.add(Markup.Type.PARALLELS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.INTERVALS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.CROSSINGS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.LYRICS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.RIEMANN);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.HARMONY);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.VELOCITY);
            }
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
        });
        toolbar2.add(flagBtn3);

        flagBtn4 = makeToggleButton("toolbar/flagIntervals", "Intervals", 12);
        makeAction(flagBtn4, KeyEvent.VK_I, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            if (!flagBtn4.isSelected()) {
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.INTERVALS);
            } else {
                scoreModel.getScoreBuilder().getScoreParameter().markup.add(Markup.Type.INTERVALS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.PARALLELS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.CROSSINGS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.LYRICS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.RIEMANN);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.HARMONY);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.VELOCITY);
            }
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
        });
        toolbar2.add(flagBtn4);

        flagBtn5 = makeToggleButton("toolbar/flagCrossings", "Crossings", 12);
        makeAction(flagBtn5, KeyEvent.VK_X, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            if (!flagBtn5.isSelected()) {
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.CROSSINGS);
            } else {
                scoreModel.getScoreBuilder().getScoreParameter().markup.add(Markup.Type.CROSSINGS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.PARALLELS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.INTERVALS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.LYRICS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.RIEMANN);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.HARMONY);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.VELOCITY);
            }
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
        });
        toolbar2.add(flagBtn5);

        flagBtn6 = makeToggleButton("toolbar/flagLyrics", "Lyrics", 12);
        makeAction(flagBtn6, KeyEvent.VK_L, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            if (!flagBtn6.isSelected()) {
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.LYRICS);
            } else {
                scoreModel.getScoreBuilder().getScoreParameter().markup.add(Markup.Type.LYRICS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.PARALLELS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.INTERVALS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.CROSSINGS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.RIEMANN);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.HARMONY);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.VELOCITY);
            }
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
        });
        toolbar2.add(flagBtn6);

        flagBtn9 = makeToggleButton("toolbar/flagHarmony", "Harmony", 12);
        makeAction(flagBtn9, KeyEvent.VK_H, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            if (!flagBtn9.isSelected()) {
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.HARMONY);
            } else {
                scoreModel.getScoreBuilder().getScoreParameter().markup.add(Markup.Type.HARMONY);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.RIEMANN);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.PARALLELS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.INTERVALS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.LYRICS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.CROSSINGS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.VELOCITY);
            }
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
        });
        toolbar2.add(flagBtn9);

        flagBtn10 = makeToggleButton("toolbar/flagRiemann", "Riemann", 12);
        makeAction(flagBtn10, KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            if (!flagBtn10.isSelected()) {
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.RIEMANN);
            } else {
                scoreModel.getScoreBuilder().getScoreParameter().markup.add(Markup.Type.RIEMANN);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.HARMONY);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.PARALLELS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.INTERVALS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.LYRICS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.CROSSINGS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.VELOCITY);
            }
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
        });
        toolbar2.add(flagBtn10);

        flagBtn11 = makeToggleButton("toolbar/flagVelocity", "Velocity", 12);
        makeAction(flagBtn11, KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, () -> {
            if (!flagBtn11.isSelected()) {
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.VELOCITY);
            } else {
                scoreModel.getScoreBuilder().getScoreParameter().markup.add(Markup.Type.VELOCITY);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.RIEMANN);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.HARMONY);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.PARALLELS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.INTERVALS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.LYRICS);
                scoreModel.getScoreBuilder().getScoreParameter().markup.remove(Markup.Type.CROSSINGS);
            }
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
        });
        toolbar2.add(flagBtn11);

        updateFlags();
    }

    private JButton makeButton(String function, String tooltip, int size) {
        URL url = getClass().getResource("/" + function + ".png");
        ImageIcon icon = new ImageIcon(url);
        if (size > 0) {
            icon = new ImageIcon(icon.getImage().getScaledInstance((int)(1.6*size), size, Image.SCALE_SMOOTH));
        }
        JButton button = new JButton(icon);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        return button;
    }

    private JToggleButton makeToggleButton(String function, String tooltip, int size) {
        URL url = getClass().getResource("/" + function + ".png");
        ImageIcon icon = new ImageIcon(url);
        if (size > 0) {
            icon = new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
        }
        JToggleButton button = new JToggleButton(icon);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        return button;
    }

    public void setSelection(AmbitusSelection selection) {
        if (selection.hasSingleSelection()) {
            Event event = selection.getSingleSelection();
            this.selectedEvent = event;
        } else {
            this.selectedEvent = null;
        }
    }

    public void clearSelection() {
        this.selectedEvent = null;
    }

    public JToolBar getToolBar() {
        return toolbar2;
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

    public void updateSelector(NoteSelector selector) {
        updateSelector(noteButtonGroup, selector);
    }

    private void updateSelector(ButtonGroup group, NoteSelector grid) {
        group.clearSelection();
        Enumeration<AbstractButton> elements = group.getElements();
        while (elements.hasMoreElements()) {
            AbstractButton button = elements.nextElement();
            NoteSelector buttonGrid = (NoteSelector) button.getClientProperty("data");
            if (grid==buttonGrid) {
                button.setSelected(true);
            }
        }
    }

    public void updateFlags() {
        flagBtn1.setSelected(scoreModel.getScoreBuilder().getScoreParameter().markup.contains(Markup.Type.AMBITUS));
        flagBtn2.setSelected(scoreModel.getScoreBuilder().getScoreParameter().markup.contains(Markup.Type.ATTRIBUTES));
        flagBtn3.setSelected(scoreModel.getScoreBuilder().getScoreParameter().markup.contains(Markup.Type.PARALLELS));
        flagBtn4.setSelected(scoreModel.getScoreBuilder().getScoreParameter().markup.contains(Markup.Type.INTERVALS));
        flagBtn5.setSelected(scoreModel.getScoreBuilder().getScoreParameter().markup.contains(Markup.Type.CROSSINGS));
        flagBtn6.setSelected(scoreModel.getScoreBuilder().getScoreParameter().markup.contains(Markup.Type.LYRICS));
        flagBtn7.setSelected(scoreModel.getScoreBuilder().getScoreParameter().markup.contains(Markup.Type.NOTE_ATTRIBUTES));
        flagBtn8.setSelected(scoreModel.getScoreBuilder().getScoreParameter().markup.contains(Markup.Type.COLOR_VOICES));
        flagBtn9.setSelected(scoreModel.getScoreBuilder().getScoreParameter().markup.contains(Markup.Type.HARMONY));
        flagBtn10.setSelected(scoreModel.getScoreBuilder().getScoreParameter().markup.contains(Markup.Type.RIEMANN));
        flagBtn11.setSelected(scoreModel.getScoreBuilder().getScoreParameter().markup.contains(Markup.Type.VELOCITY));
    }

    public void updateToolbar() {
        int level = scoreModel.getGroupLevel();
        updateSelector(levelButtonGroup, level==1 ? NoteSelector.L1 : level==2 ? NoteSelector.L2 : level==3 ? NoteSelector.L3 : NoteSelector.N1);
    }
}
