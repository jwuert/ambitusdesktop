package org.wuerthner.ambitusdesktop;

import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.model.MidiTrack;
import org.wuerthner.ambitus.model.NoteEvent;
import org.wuerthner.ambitus.service.Quantize;
import org.wuerthner.ambitus.type.NamedRange;
import org.wuerthner.ambitusdesktop.service.MidiService;
import org.wuerthner.ambitusdesktop.ui.ArrangementConfig;
import org.wuerthner.ambitusdesktop.ui.ParameterDialog;
import org.wuerthner.ambitusdesktop.ui.TrackWidget;
import org.wuerthner.ambitusdesktop.ui.Wizard;
import org.wuerthner.cwn.api.CwnBarEvent;
import org.wuerthner.cwn.api.CwnSelection;
import org.wuerthner.cwn.api.Trias;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.Location;
import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.api.Modifier;
import org.wuerthner.sport.core.XMLElementWriter;
import org.wuerthner.sport.core.XMLReader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Ambitus implements PanelUpdater, ToolbarUpdater {
    static int WIDTH = 1600;
    static int HEIGHT = 1024;

    private final ScorePanel content;
    private final JScrollPane trackPanel;
    private final JPanel navigation;
    private final JPanel rangePanel;
    private final ScoreModel scoreModel = new ScoreModel();
    private final MidiService midiService = new MidiService();
    private ButtonGroup gridButtonGroup;
    private final Map<String,JButton> toolMap = new HashMap<>();

    public Ambitus() {
        final JFrame frame = new JFrame("Ambitus");
        JComponent toolbar = makeToolBar();

        navigation = new JPanel();
        BoxLayout layout = new BoxLayout(navigation, BoxLayout.Y_AXIS);
        navigation.setLayout(layout);
        navigation.setAlignmentX(Component.LEFT_ALIGNMENT);

        updatePanel();
        trackPanel = new JScrollPane(navigation, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        content = makeContent();

        rangePanel = new JPanel();
        rangePanel.setPreferredSize(new Dimension(200, HEIGHT));


        frame.setLayout(new BorderLayout());
        frame.getContentPane().add(toolbar, BorderLayout.PAGE_START);
        JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitpane.setLeftComponent(trackPanel);
        splitpane.setRightComponent(content);
        splitpane.setDividerLocation(160);
        frame.getContentPane().add(splitpane, BorderLayout.CENTER);
        frame.getContentPane().add(rangePanel, BorderLayout.EAST);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(WIDTH, HEIGHT);
        frame.setVisible(true);
        content.addMouseMotionListener(content);
        content.addMouseListener(content);
        new ScoreKeyListener(content, scoreModel);
    }

    private JComponent makeToolBar() {
        //
        // 1st toolbar
        //
        JToolBar commandToolbar = new JToolBar();

        // New Document Wizard!
        JButton newDocumentBtn = makeButton("toolbar/newDocument", 24);
        AbstractAction newDocumentAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Wizard(scoreModel, content);
                content.touchScore();
                updatePanel();
                updateToolbar();
            }
        };
        newDocumentBtn.addActionListener(newDocumentAction);
        newDocumentBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), "newDocument");
        newDocumentBtn.getActionMap().put("newDocument", newDocumentAction);
        commandToolbar.add(newDocumentBtn);
        toolMap.put("newDocument", newDocumentBtn);

        // Open
        JButton openBtn = makeButton("toolbar/open", 24);
        AbstractAction open = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser  fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                Action details = fileChooser.getActionMap().get("viewTypeDetails");
                details.actionPerformed(null);
                int result = fileChooser.showOpenDialog(content);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        FileInputStream inputStream = new FileInputStream(selectedFile);
                        // XmlElementReader reader = new XmlElementReader(scoreModel.factory, Arrangement.TYPE);
                        XMLReader reader = new XMLReader(scoreModel.factory, Arrangement.TYPE, "root");
                        Arrangement root = (Arrangement) reader.run(inputStream);
                        if (root != null) {
                            scoreModel.setArrangement(root);
                            content.touchScore();
                            updatePanel();
                            updateToolbar();
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        };
        openBtn.addActionListener(open);
        openBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), "open");
        openBtn.getActionMap().put("open", open);
        toolMap.put("open", openBtn);
        commandToolbar.add(openBtn);

        // Write
        JButton writeBtn = makeButton("toolbar/write", 24);
        AbstractAction writeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                // fileChooser.setDialogTitle("Specify a file to save");
                Action details = fileChooser.getActionMap().get("viewTypeDetails");
                details.actionPerformed(null);
                int userSelection = fileChooser.showSaveDialog(content);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    if (fileToSave!=null) {
                        try {
                            FileOutputStream outputStream = new FileOutputStream(fileToSave);
                            Arrangement arrangement = scoreModel.getArrangement();
                            XMLElementWriter w = new XMLElementWriter();
                            w.run(arrangement, outputStream);
                            scoreModel.getArrangement().clearHistory();
                            updateToolbar();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }
            }
        };
        writeBtn.addActionListener(writeAction);
        writeBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "write");
        writeBtn.getActionMap().put("write", writeAction);
        toolMap.put("write", writeBtn);
        commandToolbar.add(writeBtn);

        // Arrangement Properties
        JButton propBtn = makeButton("toolbar/arrangementConfiguration", 24);
        AbstractAction propAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ArrangementConfig(scoreModel, content);
                content.touchScore();
                updatePanel();
                updateToolbar();
            }
        };
        propBtn.addActionListener(propAction);
        propBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), "prop");
        propBtn.getActionMap().put("prop", propAction);
        toolMap.put("prop", propBtn);
        commandToolbar.add(propBtn);

        // Add Track
        JButton addTrack = makeButton("toolbar/newTrack", 24);
        AbstractAction addTrackAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.addTrack();
                content.touchScore();
                updatePanel();
                updateToolbar();
            }
        };
        addTrack.addActionListener(addTrackAction);
        addTrack.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "addTrack");
        addTrack.getActionMap().put("addTrack", addTrackAction);
        toolMap.put("addTrack", addTrack);
        commandToolbar.add(addTrack);

        // SEPARATOR
        commandToolbar.addSeparator(new Dimension(10, 40));

        // Undo
        JButton undoBtn = makeButton("toolbar/undo", 24);
        AbstractAction undoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.undo();
                content.touchScore();
                updatePanel();
                updateToolbar();
            }
        };
        undoBtn.addActionListener(undoAction);
        undoBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo");
        undoBtn.getActionMap().put("undo", undoAction);
        toolMap.put("undo", undoBtn);
        commandToolbar.add(undoBtn);

        // Redo
        JButton redoBtn = makeButton("toolbar/redo", 24);
        AbstractAction redoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.redo();
                content.touchScore();
                updatePanel();
                updateToolbar();
            }
        };
        redoBtn.addActionListener(redoAction);
        redoBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redo");
        redoBtn.getActionMap().put("redo", redoAction);
        toolMap.put("redo", redoBtn);
        commandToolbar.add(redoBtn);

        // SEPARATOR
        commandToolbar.addSeparator(new Dimension(10, 40));

        // Delete
        JButton deleteBtn = makeButton("toolbar/eraser", 24);
        AbstractAction deleteAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.deleteSelection();
                content.touchScore();
                updatePanel();
                updateToolbar();
            }
        };
        deleteBtn.addActionListener(deleteAction);
        deleteBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.CTRL_DOWN_MASK), "delete");
        deleteBtn.getActionMap().put("delete", deleteAction);
        toolMap.put("delete", deleteBtn);
        commandToolbar.add(deleteBtn);

        // Cut
        JButton cutBtn = makeButton("toolbar/cut", 24);
        Action cutAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.cutSelection();
                content.touchScore();
                updatePanel();
                updateToolbar();
            }
        };
        cutBtn.addActionListener(cutAction);
        cutBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), "cut");
        cutBtn.getActionMap().put("cut", cutAction);
        toolMap.put("cut", cutBtn);
        commandToolbar.add(cutBtn);

        // Copy
        JButton copyBtn = makeButton("toolbar/copy", 24);
        Action copyAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.copySelection();
                content.touchScore();
                updatePanel();
                updateToolbar();
            }
        };
        copyBtn.addActionListener(copyAction);
        copyBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "copy");
        copyBtn.getActionMap().put("copy", copyAction);
        toolMap.put("copy", copyBtn);
        commandToolbar.add(copyBtn);

        // Paste
        JButton pasteBtn = makeButton("toolbar/paste", 24);
        AbstractAction pasteAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MidiTrack selectedMidiTrack = scoreModel.getArrangement().getSelectedMidiTrack();
                if (selectedMidiTrack!=null) {
                    String posString = (String) JOptionPane.showInputDialog(
                            content,
                            "Enter position (format: bar.beat.ticks)",
                            "Paste",
                            JOptionPane.PLAIN_MESSAGE
                    );
                    Trias newPositionTrias = new Trias(posString);
                    for (org.wuerthner.ambitus.model.Event ev: scoreModel.getSelection().getSelection()) {
                        System.out.println("~~ " + ev.getPosition() + ", " + ev.getType());
                    }
                    long newPosition = PositionTools.getPosition(selectedMidiTrack, newPositionTrias);
                    long oldPosition = scoreModel.getClipboard().getElements().get(0).getPosition();
                    long deltaPosition = newPosition - oldPosition;
                    System.out.println("-> " +oldPosition + ", " + newPosition + "," + deltaPosition);
                    Modifier<org.wuerthner.ambitus.model.Event> modifier = new PasteModifier(deltaPosition);
                    scoreModel.pasteSelection(modifier);
                    content.touchScore();
                    updatePanel();
                    updateToolbar();
                }
            }
        };
        pasteBtn.addActionListener(pasteAction);
        pasteBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "paste");
        pasteBtn.getActionMap().put("paste", pasteAction);
        toolMap.put("paste", pasteBtn);
        commandToolbar.add(pasteBtn);

        // Clear Selection
        JButton clearSelectionBtn = makeButton("toolbar/clear", 24);
        AbstractAction clearSelectionAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.getSelection().clear();
                content.touchScore();
                updateToolbar();
            }
        };
        clearSelectionBtn.addActionListener(clearSelectionAction);
        clearSelectionBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSelection");
        clearSelectionBtn.getActionMap().put("clearSelection", clearSelectionAction);
        toolMap.put("clearSelection", clearSelectionBtn);
        commandToolbar.add(clearSelectionBtn);

        // SEPARATOR
        commandToolbar.addSeparator(new Dimension(10, 40));

        // Quantize
        JButton quantizeBtn = makeButton("toolbar/quantize", 24);
        AbstractAction quantizeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Quantize.run(scoreModel.getArrangement(), scoreModel.getGridTicks());
                content.touchScore();
                updateToolbar();
            }
        };
        quantizeBtn.addActionListener(quantizeAction);
        quantizeBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK), "quantize");
        quantizeBtn.getActionMap().put("quantize", quantizeAction);
        toolMap.put("quantize", quantizeBtn);
        commandToolbar.add(quantizeBtn);

        // SEPARATOR
        commandToolbar.addSeparator(new Dimension(10, 40));

        // Rewind
        JButton firstBtn = makeButton("toolbar/first", 24);
        firstBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().setOffsetToFirstBar();
                content.touchScore();
                updateToolbar();
            }
        });
        commandToolbar.add(firstBtn);

        // Previous
        JButton prevBtn = makeButton("toolbar/previous", 24);
        prevBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().decreaseBarOffset(4);
                content.touchScore();
                updateToolbar();
            }
        });
        commandToolbar.add(prevBtn);

        // Play
        JButton playBtn = makeButton("toolbar/play", 24);
        AbstractAction playAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                midiService.play(scoreModel.getArrangement(), scoreModel.getSelection(), false,false);
                new Thread(new ScorePlayer(content, Ambitus.this)).start();
                updateToolbar();
            }
        };
        playBtn.addActionListener(playAction);
        playBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "play");
        playBtn.getActionMap().put("play", playAction);
        toolMap.put("play", playBtn);
        commandToolbar.add(playBtn);

        // Play Selection
        JButton playSelBtn = makeButton("toolbar/playSelection", 24);
        AbstractAction playSelAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                midiService.play(scoreModel.getArrangement(), scoreModel.getSelection(), true, false);
                new Thread(new ScorePlayer(content, Ambitus.this)).start();
                updateToolbar();
            }
        };
        playSelBtn.addActionListener(playSelAction);
        playSelBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "playSelection");
        playSelBtn.getActionMap().put("playSelection", playSelAction);
        toolMap.put("playSelection", playSelBtn);
        commandToolbar.add(playSelBtn);

        // Stop
        JButton stopBtn = makeButton("toolbar/stop", 24);
        AbstractAction stopAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                midiService.stop();
                updateToolbar();
            }
        };
        stopBtn.addActionListener(stopAction);
        stopBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, 0), "stop");
        stopBtn.getActionMap().put("stop", stopAction);
        toolMap.put("stop", stopBtn);
        commandToolbar.add(stopBtn);

        // Next
        JButton nextBtn = makeButton("toolbar/next", 24);
        nextBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().increaseBarOffset(4);
                content.touchScore();
                updateToolbar();
            }
        });
        commandToolbar.add(nextBtn);

        // End
        JButton lastBtn = makeButton("toolbar/last", 24);
        lastBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().setOffsetToLastBar();
                content.touchScore();
                updateToolbar();
            }
        });
        commandToolbar.add(lastBtn);

        // SEPARATOR
        commandToolbar.addSeparator(new Dimension(10, 40));

        // Zoom
        JButton zoomBtn = makeButton("toolbar/zoom", 24);
        zoomBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.toggleZoom();
                content.touchScore();
                updateToolbar();
            }
        });
        commandToolbar.add(zoomBtn);

        // Range
        JButton rangeBtn = makeButton("toolbar/bookmark", 24);
        AbstractAction rangeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MidiTrack track = scoreModel.getArrangement().getActiveMidiTrackList().get(0);
                long startNo = scoreModel.getSelection().getSelection().get(0).getPosition();
                long endNo = scoreModel.getSelection().getSelection().get(scoreModel.getSelection().getSelection().size()-1).getPosition();
                Trias startTrias = PositionTools.getTrias(track, startNo);
                Trias endTrias = PositionTools.getTrias(track, endNo);

                ParameterDialog pd = new ParameterDialog(new String[]{"Create Range"},
                        new String[]{"Name", "Start Position", "End Position"},
                        new Object[]{"", startTrias.toString(), endTrias.toString()},
                        content);
                String[] parameters = pd.getParameters();
                if (parameters!=null) {
                    String name = parameters[0];
                    long start = PositionTools.getPosition(track, new Trias(parameters[1]));
                    long end = PositionTools.getPosition(track, new Trias(parameters[2]));
                    scoreModel.getArrangement().addRange(new NamedRange(name, start, end));
                    updatePanel();
                }
            }
        };
        rangeBtn.addActionListener(rangeAction);
        rangeBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "range");
        rangeBtn.getActionMap().put("range", rangeAction);
        toolMap.put("range", rangeBtn);
        commandToolbar.add(rangeBtn);

        // SEPARATOR
        commandToolbar.addSeparator(new Dimension(10, 40));

        if (scoreModel.debug()) {
            JButton outputBtn = makeButton("toolbar/output", 24);
            outputBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scoreModel.output();
                }
            });
            commandToolbar.add(outputBtn);

            JButton debugScoreBtn = makeButton("toolbar/select_all", 24);
            debugScoreBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scoreModel.setDebugScore(!scoreModel.debugScore());
                    content.touchScore();
                    updateToolbar();
                }
            });
            commandToolbar.add(debugScoreBtn);

            // SEPARATOR
            commandToolbar.addSeparator(new Dimension(10, 40));
        }

        JButton exitBtn = makeButton("toolbar/shutdown", 24);
        exitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // JOptionPane.showMessageDialog(frame, "Exit");
                System.exit(0);
            }
        });
        commandToolbar.add(exitBtn);

        //
        // 2nd toolbar
        //
        JToolBar toolbar2 = new JToolBar();

        // note length selector
        ButtonGroup buttonGroup = new ButtonGroup();
        JToggleButton nb0Btn = makeGroupButton("images/buttons/nb0", buttonGroup, NoteSelector.N1, toolbar2);
        makeAction(nb0Btn, () -> { scoreModel.setNoteSelector(NoteSelector.N1); updateSelector(gridButtonGroup, NoteSelector.N4); scoreModel.setGridSelector(NoteSelector.N4); });
        JToggleButton nb1Btn = makeGroupButton("images/buttons/nb1", buttonGroup, NoteSelector.N2, toolbar2);
        makeAction(nb1Btn, () -> { scoreModel.setNoteSelector(NoteSelector.N2); updateSelector(gridButtonGroup, NoteSelector.N4); scoreModel.setGridSelector(NoteSelector.N4); });
        JToggleButton nb2Btn = makeGroupButton("images/buttons/nb2", buttonGroup, NoteSelector.N4, toolbar2);
        makeAction(nb2Btn, () -> { scoreModel.setNoteSelector(NoteSelector.N4); updateSelector(gridButtonGroup, NoteSelector.N8); scoreModel.setGridSelector(NoteSelector.N8); });
        JToggleButton nb3Btn = makeGroupButton("images/buttons/nb3", buttonGroup, NoteSelector.N8, toolbar2);
        makeAction(nb3Btn, () -> { scoreModel.setNoteSelector(NoteSelector.N8); updateSelector(gridButtonGroup, NoteSelector.N16); scoreModel.setGridSelector(NoteSelector.N16); });
        JToggleButton nb4Btn = makeGroupButton("images/buttons/nb4", buttonGroup, NoteSelector.N16, toolbar2);
        makeAction(nb4Btn, () -> { scoreModel.setNoteSelector(NoteSelector.N16); updateSelector(gridButtonGroup, NoteSelector.N16); scoreModel.setGridSelector(NoteSelector.N16); });
        JToggleButton nb5Btn = makeGroupButton("images/buttons/nb5", buttonGroup, NoteSelector.N32, toolbar2);
        makeAction(nb5Btn, () -> { scoreModel.setNoteSelector(NoteSelector.N32); updateSelector(gridButtonGroup, NoteSelector.N32); scoreModel.setGridSelector(NoteSelector.N32); });
        JToggleButton nb6Btn = makeGroupButton("images/buttons/nb6", buttonGroup, NoteSelector.N64, toolbar2);
        makeAction(nb6Btn, () -> { scoreModel.setNoteSelector(NoteSelector.N64); updateSelector(gridButtonGroup, NoteSelector.N64); scoreModel.setGridSelector(NoteSelector.N64); });

        toolbar2.addSeparator(new Dimension(10, 40));
        // grid length selector
        gridButtonGroup = new ButtonGroup();
        JToggleButton nb1Btn2 = makeGroupButton("images/buttons/nb1", gridButtonGroup, NoteSelector.N2, toolbar2);
        makeAction(nb1Btn2, () -> { scoreModel.setGridSelector(NoteSelector.N2); });
        JToggleButton nb2Btn2 = makeGroupButton("images/buttons/nb2", gridButtonGroup, NoteSelector.N4, toolbar2);
        makeAction(nb2Btn2, () -> { scoreModel.setGridSelector(NoteSelector.N4); });
        JToggleButton nb3Btn2 = makeGroupButton("images/buttons/nb3", gridButtonGroup, NoteSelector.N8, toolbar2);
        makeAction(nb3Btn2, () -> { scoreModel.setGridSelector(NoteSelector.N8); });
        JToggleButton nb4Btn2 = makeGroupButton("images/buttons/nb4", gridButtonGroup, NoteSelector.N16, toolbar2);
        makeAction(nb4Btn2, () -> { scoreModel.setGridSelector(NoteSelector.N16); });
        JToggleButton nb5Btn2 = makeGroupButton("images/buttons/nb5", gridButtonGroup, NoteSelector.N32, toolbar2);
        makeAction(nb5Btn2, () -> { scoreModel.setGridSelector(NoteSelector.N32); });
        JToggleButton nb6Btn2 = makeGroupButton("images/buttons/nb6", gridButtonGroup, NoteSelector.N64, toolbar2);
        makeAction(nb6Btn2, () -> { scoreModel.setGridSelector(NoteSelector.N64); });

        // default values
        updateSelector(buttonGroup, scoreModel.getNoteSelector());
        updateSelector(gridButtonGroup, scoreModel.getGridSelector());

        // ToolPanel
        JPanel toolPanel = new JPanel();
        toolPanel.setLayout(new BorderLayout());
        toolPanel.add(commandToolbar, BorderLayout.PAGE_START);
        toolPanel.add(toolbar2, BorderLayout.PAGE_END);
        return toolPanel;
    }

    public void updateToolbar() {
        int noOfTracks = scoreModel.getArrangement().getNumberOfActiveMidiTracks();
        int staff = scoreModel.getSelection().getSelectedStaff();
        boolean hasSelection = !scoreModel.getSelection().getSelection().isEmpty();
        boolean hasClipboard = !scoreModel.getClipboard().getElements().isEmpty();
        boolean dirty = scoreModel.getArrangement().hasUndo();
        boolean hasRedo = scoreModel.getArrangement().hasRedo();
        boolean notPlaying = !MidiService.isRunning();
        toolMap.get("newDocument").setEnabled(!dirty && notPlaying);
        toolMap.get("open").setEnabled(!dirty && notPlaying);
        toolMap.get("write").setEnabled(dirty);
        toolMap.get("prop").setEnabled(true);
        toolMap.get("addTrack").setEnabled(notPlaying);
        toolMap.get("undo").setEnabled(dirty && notPlaying);
        toolMap.get("redo").setEnabled(hasRedo && notPlaying);
        toolMap.get("delete").setEnabled(hasSelection && notPlaying);
        toolMap.get("cut").setEnabled(hasSelection && notPlaying);
        toolMap.get("copy").setEnabled(hasSelection && notPlaying);
        toolMap.get("paste").setEnabled(hasClipboard && notPlaying);
        toolMap.get("clearSelection").setEnabled(hasSelection);
        toolMap.get("quantize").setEnabled(noOfTracks>0 && notPlaying);
        toolMap.get("play").setEnabled(noOfTracks>0 && notPlaying);
        toolMap.get("playSelection").setEnabled(noOfTracks>0 && notPlaying);
        toolMap.get("stop").setEnabled(! notPlaying);
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

    private void makeAction(JButton button, Runnable runnable) {
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runnable.run();
            }
        });
    }

    private void makeAction(JToggleButton button, Runnable runnable) {
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runnable.run();
            }
        });
    }

    private JButton makeButton(String function) {
        return makeButton(function, -1);
    }

    private JButton makeButton(String function, int size) {
        URL url = getClass().getResource("/" + function + ".png");
        ImageIcon icon = new ImageIcon(url);
        if (size > 0) {
            icon = new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
        }
        JButton button = new JButton(icon);
        button.setFocusPainted(false);
        return button;
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

    public void updatePanel() {
        navigation.removeAll();
        JLabel label = new JLabel("Track Mixer");
        label.setBorder(BorderFactory.createEmptyBorder(8, 20, 2, 2));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigation.add(label);
        for (MidiTrack track : scoreModel.getArrangement().getActiveMidiTrackList()) {
            JPanel track1 = TrackWidget.createTrack(track, content);
            track1.setAlignmentX(Component.LEFT_ALIGNMENT);
            navigation.add(track1);
        }
        JLabel space = new JLabel();
        space.setPreferredSize(new Dimension(20, HEIGHT));
        space.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigation.add(space);
        // range
        List<NamedRange> rangeList = scoreModel.getArrangement().getAttributeValue(Arrangement.rangeList);
        if (rangePanel!=null) {
            rangePanel.removeAll();
            if (rangeList.isEmpty()) {
                rangePanel.setVisible(false);
            } else {
                rangePanel.setVisible(true);
                for (NamedRange range : rangeList) {
                    JButton rangeBtn = new JButton(range.name);
                    rangeBtn.setPreferredSize(new Dimension(180, 20));
                    rangeBtn.setFocusPainted(false);
                    AbstractAction rangeAction = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int max = scoreModel.getArrangement().getNumberOfActiveMidiTracks() - 1;
                            scoreModel.select(
                                    new Location(null, range.start, 0, 0, 0, false, 0, 0, 0),
                                    new Location(null, range.end, 0, max, 0, false, 0, 0, 0));
                            Trias trias = PositionTools.getTrias(scoreModel.getTrackList().get(0), range.start);
                            int offset = (trias.bar - 1 < 0 ? trias.bar : trias.bar - 1);
                            scoreModel.getArrangement().setTransientBarOffset(offset);
                            content.touchScore();
                        }
                    };
                    rangeBtn.addActionListener(rangeAction);
                    rangePanel.add(rangeBtn);
                }
            }
            rangePanel.updateUI();
        }
        // toolbar
        updateToolbar();
    }

    private ScorePanel makeContent() {
        ScorePanel scorePanel = new ScorePanel(scoreModel, this, this);
        return scorePanel;
    }

    public static void main(String[] args) {
        Ambitus ambitus = new Ambitus();
    }
}
