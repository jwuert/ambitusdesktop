package org.wuerthner.ambitusdesktop;

import org.wuerthner.ambitus.model.MidiTrack;
import org.wuerthner.ambitus.model.NoteEvent;
import org.wuerthner.ambitus.tool.SelectionTools;
import org.wuerthner.ambitusdesktop.score.AmbitusSelection;
import org.wuerthner.cwn.score.ScoreUpdate;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ScoreKeyListener {
    private final JPanel panel;
    private final ScoreModel scoreModel;
    private final ToolbarUpdater toolbarUpdater;
    private final SelectionTools selectionTools = new SelectionTools();
    private final String kLeft = "left";
    private final String kLeftCtrl = "leftCtrl";
    private final String kLeftShift = "leftShift";
    private final String kRight = "right";
    private final String kRightCtrl = "rightCtrl";
    private final String kRightShift = "rightShift";
    private final String kUp = "up";
    private final String kDown = "down";
    private final String kUpCtrl = "upCtrl";
    private final String kDownCtrl = "downCtrl";
    private final String kPgUp = "pgUp";
    private final String kPgDown = "pgDown";
    private final String kD = "d";
    private final String kH = "h";
    private final String kSharp = "#";
    private final String kFlat = "b";
    private final String kRefresh = "F5";

    public ScoreKeyListener(JPanel scorePanel, ScoreModel scoreModel, ToolbarUpdater toolbarUpdater) {
        this.panel = scorePanel;
        this.scoreModel = scoreModel;
        this.toolbarUpdater = toolbarUpdater;
        ActionMap actionMap = scorePanel.getActionMap();
        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap inputMap = scorePanel.getInputMap(condition );

        add(scorePanel, kLeft, KeyEvent.VK_LEFT, 0);
        add(scorePanel, kLeftCtrl, KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK);
        add(scorePanel, kLeftShift, KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK);
        add(scorePanel, kRight, KeyEvent.VK_RIGHT, 0);
        add(scorePanel, kRightCtrl, KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK);
        add(scorePanel, kRightShift, KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK);
        add(scorePanel, kUp, KeyEvent.VK_UP, 0);
        add(scorePanel, kUpCtrl, KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK);
        add(scorePanel, kDown, KeyEvent.VK_DOWN, 0);
        add(scorePanel, kDownCtrl, KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK);
        add(scorePanel, kD, KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK);
        add(scorePanel, kH, KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK);
        add(scorePanel, kPgUp, KeyEvent.VK_PAGE_UP, 0);
        add(scorePanel, kPgDown, KeyEvent.VK_PAGE_DOWN, 0);
        add(scorePanel, kSharp, KeyEvent.VK_NUMBER_SIGN, KeyEvent.CTRL_DOWN_MASK);
        add(scorePanel, kFlat, KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK);
        add(scorePanel, kRefresh, KeyEvent.VK_F5, 0);
    }

    private void add(JPanel panel, String key, int code, int modifiers) {
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(code, modifiers, true), key);
        panel.getActionMap().put(key, new KeyAction(key));
    }

    private class KeyAction extends AbstractAction {
        public KeyAction(String actionCommand) {
            putValue(ACTION_COMMAND_KEY, actionCommand);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvt) {
            String cmd = actionEvt.getActionCommand();
            AmbitusSelection selection = scoreModel.getSelection();
            MidiTrack track = scoreModel.getArrangement().getSelectedMidiTrack();
            int staffIndex = scoreModel.getSelection().getSelectedStaff();
            switch (cmd) {
                case kLeft:
                    selectionTools.moveCursorLeft(track, selection, NoteEvent.class);
                    break;
                case kRight:
                    selectionTools.moveCursorRight(track, selection, NoteEvent.class);
                    break;
                case kLeftCtrl:
                    selectionTools.moveNoteLeft(scoreModel.getArrangement(), scoreModel.getGridTicks());
                    scoreModel.getScoreBuilder().update(new ScoreUpdate(track, selection).extendRangeByOneBar());
                    break;
                case kRightCtrl:
                    selectionTools.moveNoteRight(scoreModel.getArrangement(), scoreModel.getGridTicks());
                    scoreModel.getScoreBuilder().update(new ScoreUpdate(track, selection).extendRangeByOneBar());
                    break;
                case kLeftShift:
                    selectionTools.shrinkNote(scoreModel.getArrangement());
                    scoreModel.getScoreBuilder().update(new ScoreUpdate(track, selection).extendRangeByOneBar());
                    break;
                case kRightShift:
                    selectionTools.extendNote(scoreModel.getArrangement());
                    scoreModel.getScoreBuilder().update(new ScoreUpdate(track, selection).extendRangeByOneBar());
                    break;
                case kUp:
                    selectionTools.moveCursorUp(track, selection);
                    break;
                case kDown:
                    selectionTools.moveCursorDown(track, selection);
                    break;
                case kUpCtrl:
                    selectionTools.moveNoteUp(scoreModel.getArrangement());
                    scoreModel.getScoreBuilder().update(new ScoreUpdate(track, selection));
                    break;
                case kDownCtrl:
                    selectionTools.moveNoteDown(scoreModel.getArrangement());
                    scoreModel.getScoreBuilder().update(new ScoreUpdate(track, selection));
                    break;
                case kPgUp:
                    staffIndex--;
                    if (staffIndex<0) { staffIndex = scoreModel.getArrangement().getNumberOfActiveMidiTracks() - 1; }
                    scoreModel.getSelection().set(staffIndex);
                    break;
                case kPgDown:
                    staffIndex++;
                    if (staffIndex == scoreModel.getArrangement().getNumberOfActiveMidiTracks()) { staffIndex = 0; }
                    scoreModel.getSelection().set(staffIndex);
                    break;
                case kD:
                    selectionTools.doubleDuration(scoreModel.getArrangement());
                    scoreModel.getScoreBuilder().update(new ScoreUpdate(track, selection).extendRangeByOneBar());
                    break;
                case kH:
                    selectionTools.halfDuration(scoreModel.getArrangement());
                    scoreModel.getScoreBuilder().update(new ScoreUpdate(track, selection).extendRangeByOneBar());
                    break;
                case kSharp:
                    selectionTools.sharp(scoreModel.getArrangement());
                    scoreModel.getScoreBuilder().update(new ScoreUpdate(track, selection));
                    break;
                case kFlat:
                    selectionTools.flat(scoreModel.getArrangement());
                    scoreModel.getScoreBuilder().update(new ScoreUpdate(track, selection));
                    break;
                case kRefresh:
                    scoreModel.getScoreBuilder().update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                    break;
                default:
            }
            panel.updateUI();
            toolbarUpdater.updateToolbar();
        }
    }
}
