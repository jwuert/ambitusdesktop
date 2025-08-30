package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitusdesktop.*;
import org.wuerthner.ambitusdesktop.service.ExportService;
import org.wuerthner.ambitusdesktop.service.MidiService;
import org.wuerthner.cwn.api.CwnTrack;
import org.wuerthner.cwn.api.Trias;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.ScoreUpdate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerToolBar implements PositionUpdater {
    private final JTextField positionField;
    private final ScoreModel scoreModel;
    private final Map<String,JButton> toolMap = new HashMap<>();
    private final MidiService midiService = new MidiService();
    private final ExportService exportService = new ExportService();
    private final JDialog player;

    public PlayerToolBar(ScoreModel scoreModel, ScoreUpdater scoreUpdater, ToolbarUpdater toolbarUpdater, JPanel content) {
        this.scoreModel = scoreModel;
        JToolBar playerToolbar = new JToolBar();

        // Rewind
        JButton firstBtn = makeButton("toolbar/first", "Rewind", 24);
        AbstractAction firstAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().setOffsetToFirstBar();
                scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                updateToolbar();
                updatePosition();
            }
        };
        firstBtn.addActionListener(firstAction);
        // firstBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.ALT_DOWN_MASK), "rewind");
        firstBtn.getActionMap().put("rewind", firstAction);
        toolMap.put("rewind", firstBtn);
        playerToolbar.add(firstBtn);

        // Previous
        JButton prevBtn = makeButton("toolbar/previous", "Previous Bar", 24);
        AbstractAction prevAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().decreaseBarOffset(1);
                scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                updateToolbar();
                updatePosition();
            }
        };
        prevBtn.addActionListener(prevAction);
        // prevBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK), "previous");
        prevBtn.getActionMap().put("previous", prevAction);
        toolMap.put("previous", prevBtn);
        playerToolbar.add(prevBtn);

        // Position
        Color positionFieldColor = new Color(0,120,0);
        positionField = new JTextField();
        positionField.setMaximumSize(new Dimension(110, 36));
        positionField.setMinimumSize(new Dimension(110, 36));
        positionField.setPreferredSize(new Dimension(110, 36));
        positionField.setSize(new Dimension(110, 36));
        positionField.setEditable(true);
        positionField.setBorder(FieldFocusListener.etchlBorder);
        positionField.setBackground(Color.LIGHT_GRAY);
        positionField.setForeground(positionFieldColor);
        positionField.setFont(new Font("Dialog", Font.BOLD, 12));
        positionField.setText(new Trias(0,0,0).toFormattedString());
        positionField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                player.setFocusableWindowState(true);
                positionField.setText(positionField.getText().replaceAll("\\s*", ""));
            }
        });
        FieldFocusListener fieldFocusListener = new FieldFocusListener(scoreModel, positionFieldColor);
        positionField.addFocusListener(fieldFocusListener);
        positionField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = positionField.getText();
                if (text != null && !text.trim().equals("")) {
                    Trias trias = new Trias(text);
                    int newBar = trias.bar;
                    scoreModel.getArrangement().setTransientBarOffset(newBar);
                    scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                }
                positionField.getParent().getParent().requestFocus();
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                updateToolbar();
                updatePosition();

                content.requestFocus();
                player.setFocusableWindowState(false);
            }
        });
        playerToolbar.add(positionField);

        // Next
        JButton nextBtn = makeButton("toolbar/next", "Next Bar",24);
        AbstractAction nextAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().increaseBarOffset(1);
                scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                updateToolbar();
                updatePosition();
            }
        };
        nextBtn.addActionListener(nextAction);
        // nextBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK), "next");
        nextBtn.getActionMap().put("next", nextAction);
        toolMap.put("next", nextBtn);
        playerToolbar.add(nextBtn);

        // End
        JButton lastBtn = makeButton("toolbar/last", "Last Bar", 24);
        AbstractAction lastAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().setOffsetToLastBar();
                scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                updateToolbar();
                updatePosition();
            }
        };
        lastBtn.addActionListener(lastAction);
        // lastBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.ALT_DOWN_MASK), "last");
        lastBtn.getActionMap().put("last", lastAction);
        toolMap.put("last", lastBtn);
        playerToolbar.add(lastBtn);

        // Set Caret
        JButton caretBtn = makeButton("toolbar/caret", "Set Caret", 24);
        AbstractAction caretAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                long pos = 0;
                List<org.wuerthner.ambitus.model.Event> list = scoreModel.getSelection().getSelection();
                if (list.isEmpty()) {
                    pos = scoreModel.getArrangement().getBarOffsetPosition();
                } else {
                    pos = list.get(0).getPosition();
                }
                scoreModel.getArrangement().setTransientCaret(pos);
                scoreModel.getScoreParameter().setCaret(scoreModel.getArrangement().getCaret());
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                updateToolbar();
                updatePosition();
            }
        };
        caretBtn.addActionListener(caretAction);
        // caretBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.ALT_DOWN_MASK), "caret");
        caretBtn.getActionMap().put("caret", caretAction);
        toolMap.put("caret", caretBtn);
        playerToolbar.add(caretBtn);

        // Jump to Caret
        JButton rewindToCaretBtn = makeButton("toolbar/rewindToCaret", "Rewind to Caret", 24);
        AbstractAction rewindToCaretAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().setTransientBarOffsetPosition(scoreModel.getArrangement().getCaret());
                scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                updateToolbar();
                updatePosition();
            }
        };
        rewindToCaretBtn.addActionListener(rewindToCaretAction);
        // rewindToCaretBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.ALT_DOWN_MASK), "rewindToCaret");
        rewindToCaretBtn.getActionMap().put("rewindToCaret", rewindToCaretAction);
        toolMap.put("rewindToCaret", rewindToCaretBtn);
        playerToolbar.add(rewindToCaretBtn);

        // Stop
        JButton stopBtn = makeButton("toolbar/stop", "Stop",24);
        AbstractAction stopAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MidiService.stop();
                updateToolbar();
            }
        };
        stopBtn.addActionListener(stopAction);
        stopBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, 0), "stop");
        stopBtn.getActionMap().put("stop", stopAction);
        toolMap.put("stop", stopBtn);
        playerToolbar.add(stopBtn);

        // Play
        JButton playBtn = makeButton("toolbar/play", "Play",24);
        AbstractAction playAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                midiService.play(scoreModel.getArrangement(), scoreModel.getSelection(), false,false);
                new Thread(new ScorePlayer(content, toolbarUpdater, PlayerToolBar.this)).start();
                updateToolbar();
            }
        };
        playBtn.addActionListener(playAction);
        playBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, 0), "play");
        playBtn.getActionMap().put("play", playAction);
        toolMap.put("play", playBtn);
        playerToolbar.add(playBtn);

        // Play Config
        JButton playConfBtn = makeButton("toolbar/playConfig", "Play Configuration",24);
        AbstractAction playConfAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tempo = "" + scoreModel.getPlayTempo();
                String strength = "" + scoreModel.getPlayStrength();

                java.util.List<String> exposeList = new ArrayList<>();
                exposeList.add("-");
                exposeList.addAll(scoreModel.getTrackList().stream().map(CwnTrack::getName).collect(Collectors.toList()));
                exposeList.add(scoreModel.getPlayExpose() < 0 ? "-" : exposeList.get(scoreModel.getPlayExpose() + 1));
                List<String> strengthList = new ArrayList<>(Arrays.asList("1", "2", "3", "4"));
                strengthList.add(strength);
                ParameterDialog pd = new ParameterDialog(new String[]{"Play"},
                        new String[]{"Tempo", "Expose", "Strength"},
                        new Object[]{tempo, exposeList.toArray(new String[]{}), strengthList.toArray(new String[]{})},
                        content);
                String[] parameters = pd.getParameters();
                if (parameters!=null) {
                    String newTempoS = parameters[0];
                    String newExposeS = parameters[1];
                    String newStrengthS = parameters[2];
                    try {
                        int newTempo = Integer.parseInt(newTempoS);
                        int newStrength = Integer.parseInt(newStrengthS);
                        int newExpose = exposeList.indexOf(newExposeS) - 1;
                        scoreModel.setPlayExpose(newExpose);
                        scoreModel.setPlayStrength(newStrength);
                        scoreModel.setPlayTempo(newTempo);
                        midiService.play(scoreModel.getArrangement(), scoreModel.getSelection(), false, false, newTempo, newExpose, newStrength);
                        new Thread(new ScorePlayer(content, toolbarUpdater, PlayerToolBar.this)).start();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                updateToolbar();
            }
        };
        playConfBtn.addActionListener(playConfAction);
        // playConfBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK), "playConfiguration");
        playConfBtn.getActionMap().put("playConfiguration", playConfAction);
        toolMap.put("playConfiguration", playConfBtn);
        playerToolbar.add(playConfBtn);

        // Play Selection
        JButton playSelBtn = makeButton("toolbar/playSelection", "Play Selection",24);
        AbstractAction playSelAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                midiService.play(scoreModel.getArrangement(), scoreModel.getSelection(), true, false);
                new Thread(new ScorePlayer(content, toolbarUpdater, PlayerToolBar.this)).start();
                updateToolbar();
            }
        };
        playSelBtn.addActionListener(playSelAction);
        // playSelBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "playSelection");
        playSelBtn.getActionMap().put("playSelection", playSelAction);
        toolMap.put("playSelection", playSelBtn);
        playerToolbar.add(playSelBtn);

        // Export
        JButton exportBtn = makeButton("toolbar/exportCollection", "Export Collection",24);
        AbstractAction exportAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tempo = "80, 100";
                List<String> strengthList = Arrays.asList("-", "1", "2", "3", "4", "-");
                ParameterDialog pd = new ParameterDialog(new String[]{"Create Range"},
                        new String[]{"Tempi", "Strength"},
                        new Object[]{tempo, strengthList.toArray(new String[]{})},
                        content);
                String[] parameters = pd.getParameters();
                if (parameters != null) {
                    String tempi = parameters[0];
                    int strength = strengthList.indexOf(parameters[1]);
                    exportService.export(scoreModel.getArrangement(), scoreModel, strength, tempi);
                }
                updateToolbar();
            }
        };
        exportBtn.addActionListener(exportAction);
        // exportBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "playSelection");
        exportBtn.getActionMap().put("export", exportAction);
        toolMap.put("export", exportBtn);
        playerToolbar.add(exportBtn);


        // SEPARATOR
        // playerToolbar.addSeparator(new Dimension(10, 40));

        // JFrame player = new JFrame();
        player = new JDialog();
        player.add(playerToolbar);
        player.setAlwaysOnTop(true);
        player.setFocusableWindowState(false);
        player.pack();
        Point location = content.getLocationOnScreen();
        player.setLocation(location.x+20, location.y+20);
        player.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        player.setResizable(false);

        player.setTitle("Player");
        player.setVisible(true);
    }

    private JButton makeButton(String function, String tooltip, int size) {
        URL url = getClass().getResource("/" + function + ".png");
        JButton button;
        if (url!=null) {
            ImageIcon icon = new ImageIcon(url);
            if (size > 0) {
                icon = new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
                button = new JButton(icon);
            } else {
                button = new JButton("");
                button.setPreferredSize(new Dimension(1, 24));
            }
        } else {
            button = new JButton("function");
        }
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        return button;
    }

    @Override
    public void updatePosition() {
        long position = MidiService.getPosition();
        if (position == 0) {
            position = scoreModel.getArrangement().getBarOffsetPosition();
        }
        if (!scoreModel.getTrackList().isEmpty()) {
            CwnTrack track = scoreModel.getTrackList().get(0);
            Trias trias = PositionTools.getTrias(track, position);
            positionField.setText(trias.toFormattedString());
        }
    }

    public void updateToolbar() {
        int noOfTracks = scoreModel.getArrangement().getNumberOfActiveMidiTracks();
//        int staff = scoreModel.getSelection().getSelectedStaff();
//        boolean hasSelection = !scoreModel.getSelection().getSelection().isEmpty();
//        boolean hasSingleSelection = scoreModel.getSelection().hasSingleSelection();
//        boolean hasClipboard = !scoreModel.getClipboard().getElements().isEmpty();
//        boolean dirty = scoreModel.getArrangement().hasUndo();
//        boolean hasRedo = scoreModel.getArrangement().hasRedo();
        boolean notPlaying = !MidiService.isRunning();

        toolMap.get("rewind").setEnabled(noOfTracks > 0);
        toolMap.get("previous").setEnabled(noOfTracks > 0);
        toolMap.get("next").setEnabled(noOfTracks > 0);
        toolMap.get("last").setEnabled(noOfTracks > 0);
        toolMap.get("play").setEnabled(noOfTracks > 0 && notPlaying);
        toolMap.get("playConfiguration").setEnabled(noOfTracks > 0 && notPlaying);
        toolMap.get("playSelection").setEnabled(noOfTracks > 0 && notPlaying);
        toolMap.get("export").setEnabled(noOfTracks > 0 && notPlaying);
        toolMap.get("stop").setEnabled(! notPlaying);
        updatePosition();
    }
}
