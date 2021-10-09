package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.model.Event;
import org.wuerthner.ambitus.model.MidiTrack;
import org.wuerthner.ambitus.model.NoteEvent;
import org.wuerthner.ambitus.service.Quantize;
import org.wuerthner.ambitus.type.NamedRange;
import org.wuerthner.ambitusdesktop.*;
import org.wuerthner.ambitusdesktop.service.MidiService;
import org.wuerthner.cwn.api.CwnTrack;
import org.wuerthner.cwn.api.Trias;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.ScoreUpdate;
import org.wuerthner.sport.api.Modifier;
import org.wuerthner.sport.core.XMLElementWriter;
import org.wuerthner.sport.core.XMLReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionToolBar {
    private final JToolBar functionToolbar;
    private final MidiService midiService = new MidiService();
    private final Map<String,JButton> toolMap = new HashMap<>();
    private final ScoreModel scoreModel;
    private final ScoreUpdater scoreUpdater;
    private final PanelUpdater panelUpdater;
    private final ToolbarUpdater toolbarUpdater;

    public FunctionToolBar(ScoreModel scoreModel, ScoreUpdater scoreUpdater, ToolbarUpdater toolbarUpdater, PanelUpdater panelUpdater, JPanel content, int WIDTH) {
        this.scoreModel = scoreModel;
        this.scoreUpdater = scoreUpdater;
        this.toolbarUpdater = toolbarUpdater;
        this.panelUpdater = panelUpdater;
        this.functionToolbar = new JToolBar();
        
        // New Document Wizard!
        JButton newDocumentBtn = makeButton("toolbar/newDocument", "new Document", 24);
        AbstractAction newDocumentAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.setArrangement(Wizard.createArrangement(scoreModel, content));
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                panelUpdater.updatePanel();
                updateToolbar();
            }
        };
        newDocumentBtn.addActionListener(newDocumentAction);
        newDocumentBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), "newDocument");
        newDocumentBtn.getActionMap().put("newDocument", newDocumentAction);
        functionToolbar.add(newDocumentBtn);
        toolMap.put("newDocument", newDocumentBtn);

        // Open
        JButton openBtn = makeButton("toolbar/open", "Open File", 24);
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
                            scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                            panelUpdater.updatePanel();
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
        functionToolbar.add(openBtn);

        // Write
        JButton writeBtn = makeButton("toolbar/write", "Save File",24);
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
                            panelUpdater.updatePanel();
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
        functionToolbar.add(writeBtn);

        // Close Document
        JButton closeDocument = makeButton("toolbar/close", "Close Document",24);
        AbstractAction closeDocumentAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dialogResult = JOptionPane.showConfirmDialog (content, "Quit and lose unsaved changes?","Warning", JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION){
                    scoreModel.clear(WIDTH);
                }
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                panelUpdater.updatePanel();
                updateToolbar();
            }
        };
        closeDocument.addActionListener(closeDocumentAction);
        closeDocument.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK), "closeDocument");
        closeDocument.getActionMap().put("closeDocument", closeDocumentAction);
        toolMap.put("closeDocument", closeDocument);
        functionToolbar.add(closeDocument);

        // Arrangement Properties
        JButton propBtn = makeButton("toolbar/arrangementConfiguration", "Arrangement Properties",24);
        AbstractAction propAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ArrangementConfig(scoreModel, content);
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                panelUpdater.updatePanel();
                updateToolbar();
            }
        };
        propBtn.addActionListener(propAction);
        propBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), "prop");
        propBtn.getActionMap().put("prop", propAction);
        toolMap.put("prop", propBtn);
        functionToolbar.add(propBtn);

        // Add Track
        JButton addTrack = makeButton("toolbar/newTrack", "Add Track",24);
        AbstractAction addTrackAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.addTrack();
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD)); // TODO: restrict to new track!
                panelUpdater.updatePanel();
                updateToolbar();
            }
        };
        addTrack.addActionListener(addTrackAction);
        addTrack.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "addTrack");
        addTrack.getActionMap().put("addTrack", addTrackAction);
        toolMap.put("addTrack", addTrack);
        functionToolbar.add(addTrack);

        // Add Lyrics
        JButton addLyrics = makeButton("toolbar/lyrics", "Add Lyrics",24);
        AbstractAction addLyricsAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.toggleLyrics();
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
//                panelUpdater.updatePanel();
                updateToolbar();
            }
        };
        addLyrics.addActionListener(addLyricsAction);
        addLyrics.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "addLyrics");
        addLyrics.getActionMap().put("addLyrics", addLyricsAction);
        toolMap.put("addLyrics", addLyrics);
        functionToolbar.add(addLyrics);

        // SEPARATOR
        functionToolbar.addSeparator(new Dimension(10, 40));

        // Undo
        JButton undoBtn = makeButton("toolbar/undo", "Undo",24);
        AbstractAction undoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.undo();
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD)); // TODO
                panelUpdater.updatePanel();
                updateToolbar();
            }
        };
        undoBtn.addActionListener(undoAction);
        undoBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo");
        undoBtn.getActionMap().put("undo", undoAction);
        toolMap.put("undo", undoBtn);
        functionToolbar.add(undoBtn);

        // Redo
        JButton redoBtn = makeButton("toolbar/redo", "Redo",24);
        AbstractAction redoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.redo();
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD)); // TODO
                panelUpdater.updatePanel();
                updateToolbar();
            }
        };
        redoBtn.addActionListener(redoAction);
        redoBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redo");
        redoBtn.getActionMap().put("redo", redoAction);
        toolMap.put("redo", redoBtn);
        functionToolbar.add(redoBtn);

        // SEPARATOR
        functionToolbar.addSeparator(new Dimension(10, 40));

        // Delete
        JButton deleteBtn = makeButton("toolbar/eraser", "Delete Selection",24);
        AbstractAction deleteAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                java.util.List<CwnTrack> trackList = scoreModel.deleteSelection();
                scoreUpdater.update(new ScoreUpdate(trackList, scoreModel.getSelection()));
                panelUpdater.updatePanel();
                updateToolbar();
            }
        };
        deleteBtn.addActionListener(deleteAction);
        deleteBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.CTRL_DOWN_MASK), "delete");
        deleteBtn.getActionMap().put("delete", deleteAction);
        toolMap.put("delete", deleteBtn);
        functionToolbar.add(deleteBtn);

        // Cut
        JButton cutBtn = makeButton("toolbar/cut", "Cut Selection",24);
        Action cutAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<CwnTrack> trackList = scoreModel.cutSelection();
                scoreUpdater.update(new ScoreUpdate(trackList, scoreModel.getSelection()));
                panelUpdater.updatePanel();
                updateToolbar();
            }
        };
        cutBtn.addActionListener(cutAction);
        cutBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), "cut");
        cutBtn.getActionMap().put("cut", cutAction);
        toolMap.put("cut", cutBtn);
        functionToolbar.add(cutBtn);

        // Copy
        JButton copyBtn = makeButton("toolbar/copy", "Copy Selection",24);
        Action copyAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.copySelection();
                scoreUpdater.update(new ScoreUpdate());
                panelUpdater.updatePanel();
                updateToolbar();
            }
        };
        copyBtn.addActionListener(copyAction);
        copyBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "copy");
        copyBtn.getActionMap().put("copy", copyAction);
        toolMap.put("copy", copyBtn);
        functionToolbar.add(copyBtn);

        // Paste
        JButton pasteBtn = makeButton("toolbar/paste", "Paste Selection",24);
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
//                    for (org.wuerthner.ambitus.model.Event ev: scoreModel.getSelection().getSelection()) {
//                        System.out.println("~~ " + ev.getPosition() + ", " + ev.getType());
//                    }
                    long newPosition = PositionTools.getPosition(selectedMidiTrack, newPositionTrias);
                    long oldPosition = scoreModel.getClipboard().getElements().get(0).getPosition();
                    long deltaPosition = newPosition - oldPosition;
                    Modifier<Event> modifier = new PasteModifier(deltaPosition);
                    scoreModel.pasteSelection(modifier);
                    scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                    panelUpdater.updatePanel();
                    updateToolbar();
                }
            }
        };
        pasteBtn.addActionListener(pasteAction);
        pasteBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "paste");
        pasteBtn.getActionMap().put("paste", pasteAction);
        toolMap.put("paste", pasteBtn);
        functionToolbar.add(pasteBtn);

        // Clear Selection
        JButton clearSelectionBtn = makeButton("toolbar/clear", "Clear Selection", 24);
        AbstractAction clearSelectionAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.getSelection().clear();
                scoreModel.setLyrics(false);
                scoreUpdater.update(new ScoreUpdate());
                updateToolbar();
            }
        };
        clearSelectionBtn.addActionListener(clearSelectionAction);
        clearSelectionBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSelection");
        clearSelectionBtn.getActionMap().put("clearSelection", clearSelectionAction);
        toolMap.put("clearSelection", clearSelectionBtn);
        functionToolbar.add(clearSelectionBtn);

        // SEPARATOR
        functionToolbar.addSeparator(new Dimension(10, 40));

        // Quantize
        JButton quantizeBtn = makeButton("toolbar/quantize", "Quantize", 24);
        AbstractAction quantizeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Quantize.run(scoreModel.getArrangement(), scoreModel.getGridTicks());
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                updateToolbar();
            }
        };
        quantizeBtn.addActionListener(quantizeAction);
        quantizeBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK), "quantize");
        quantizeBtn.getActionMap().put("quantize", quantizeAction);
        toolMap.put("quantize", quantizeBtn);
        functionToolbar.add(quantizeBtn);

        // SEPARATOR
        functionToolbar.addSeparator(new Dimension(10, 40));

        // Rewind
        JButton firstBtn = makeButton("toolbar/first", "Rewind", 24);
        firstBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().setOffsetToFirstBar();
                scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                updateToolbar();
            }
        });
        functionToolbar.add(firstBtn);

        // Previous
        JButton prevBtn = makeButton("toolbar/previous", "Previous Bar", 24);
        prevBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().decreaseBarOffset(1);
                scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                updateToolbar();
            }
        });
        functionToolbar.add(prevBtn);

        // Play
        JButton playBtn = makeButton("toolbar/play", "Play",24);
        AbstractAction playAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                midiService.play(scoreModel.getArrangement(), scoreModel.getSelection(), false,false);
                new Thread(new ScorePlayer(content, toolbarUpdater)).start();
                updateToolbar();
            }
        };
        playBtn.addActionListener(playAction);
        playBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "play");
        playBtn.getActionMap().put("play", playAction);
        toolMap.put("play", playBtn);
        functionToolbar.add(playBtn);

        // Play Selection
        JButton playSelBtn = makeButton("toolbar/playSelection", "Play Selection",24);
        AbstractAction playSelAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                midiService.play(scoreModel.getArrangement(), scoreModel.getSelection(), true, false);
                new Thread(new ScorePlayer(content, toolbarUpdater)).start();
                updateToolbar();
            }
        };
        playSelBtn.addActionListener(playSelAction);
        playSelBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "playSelection");
        playSelBtn.getActionMap().put("playSelection", playSelAction);
        toolMap.put("playSelection", playSelBtn);
        functionToolbar.add(playSelBtn);

        // Stop
        JButton stopBtn = makeButton("toolbar/stop", "Stop",24);
        AbstractAction stopAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                midiService.stop();
                updateToolbar();
            }
        };
        stopBtn.addActionListener(stopAction);
        stopBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "stop");
        stopBtn.getActionMap().put("stop", stopAction);
        toolMap.put("stop", stopBtn);
        functionToolbar.add(stopBtn);

        // Next
        JButton nextBtn = makeButton("toolbar/next", "Next Bar",24);
        nextBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().increaseBarOffset(1);
                scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                updateToolbar();
            }
        });
        functionToolbar.add(nextBtn);

        // End
        JButton lastBtn = makeButton("toolbar/last", "Last Bar", 24);
        lastBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getArrangement().setOffsetToLastBar();
                scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                updateToolbar();
            }
        });
        functionToolbar.add(lastBtn);

        // SEPARATOR
        functionToolbar.addSeparator(new Dimension(10, 40));

        // Zoom
        JButton zoomBtn = makeButton("toolbar/zoom", "Zoom",24);
        zoomBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.toggleZoom();
                scoreUpdater.update(new ScoreUpdate());
                updateToolbar();
            }
        });
        functionToolbar.add(zoomBtn);

        // List (Multi-Project?)
        JButton listBtn = makeButton("toolbar/list", "Toggle Sheet/System", 24);
        listBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.toggleList();
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                updateToolbar();
            }
        });
        functionToolbar.add(listBtn);

//        // Refresh
//        JButton refreshBtn = makeButton("toolbar/refresh", 24);
//        refreshBtn.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
//                updateToolbar();
//            }
//        });
//        functionToolbar.add(refreshBtn);

        // Markup
        JButton markupBtn = makeButton("toolbar/mark", "Toggle Markup", 24);
        markupBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreModel.getScoreBuilder().getScoreParameter().markup = !scoreModel.getScoreBuilder().getScoreParameter().markup;
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                updateToolbar();
            }
        });
        functionToolbar.add(markupBtn);

        // Range
        JButton rangeBtn = makeButton("toolbar/bookmark", "Bookmark Range",24);
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
                    panelUpdater.updatePanel();
                    updateToolbar();
                }
            }
        };
        rangeBtn.addActionListener(rangeAction);
        rangeBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK), "range");
        rangeBtn.getActionMap().put("range", rangeAction);
        toolMap.put("range", rangeBtn);
        functionToolbar.add(rangeBtn);

        // SEPARATOR
        functionToolbar.addSeparator(new Dimension(10, 40));

        if (scoreModel.debug()) {
            JButton outputBtn = makeButton("toolbar/output", "Output", 24);
            outputBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scoreModel.output();
//                    System.out.println("------------------------------------------");
//                    System.out.println(scoreModel.getScoreBuilder().toString());
//                    System.out.println("------------------------------------------");
                }
            });
            functionToolbar.add(outputBtn);

            JButton debugScoreBtn = makeButton("toolbar/select_all", "Debug Score", 24);
            debugScoreBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    scoreModel.setDebugScore(!scoreModel.debugScore());
                    scoreUpdater.update(new ScoreUpdate());
                    panelUpdater.updatePanel();
                    updateToolbar();
                }
            });
            functionToolbar.add(debugScoreBtn);

            // SEPARATOR
            functionToolbar.addSeparator(new Dimension(10, 40));
        }

        JButton exitBtn = makeButton("toolbar/shutdown", "Quit",24);
        AbstractAction exitAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean exit = false;
                boolean dirty = scoreModel.getArrangement().hasUndo();
                if (dirty) {
                    int dialogResult = JOptionPane.showConfirmDialog (content, "Quit and lose unsaved changes?","Warning", JOptionPane.YES_NO_OPTION);
                    if(dialogResult == JOptionPane.YES_OPTION){
                        exit = true;
                    }
                } else {
                    exit = true;
                }
                if (exit) {
                    System.exit(0);
                }
            }
        };
        exitBtn.addActionListener(exitAction);
        exitBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK), "exit");
        exitBtn.getActionMap().put("exit", exitAction);
        toolMap.put("exit", exitBtn);
        functionToolbar.add(exitBtn);
    }

    private JButton makeButton(String function, String tooltip) {
        return makeButton(function, tooltip, -1);
    }

    private JButton makeButton(String function, String tooltip, int size) {
        URL url = getClass().getResource("/" + function + ".png");
        ImageIcon icon = new ImageIcon(url);
        if (size > 0) {
            icon = new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
        }
        JButton button = new JButton(icon);
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        return button;
    }

    public void updateToolbar() {
        int noOfTracks = scoreModel.getArrangement().getNumberOfActiveMidiTracks();
        int staff = scoreModel.getSelection().getSelectedStaff();
        boolean hasSelection = !scoreModel.getSelection().getSelection().isEmpty();
        boolean hasSingleSelection = scoreModel.getSelection().hasSingleSelection();
        boolean hasClipboard = !scoreModel.getClipboard().getElements().isEmpty();
        boolean dirty = scoreModel.getArrangement().hasUndo();
        boolean hasRedo = scoreModel.getArrangement().hasRedo();
        boolean notPlaying = !MidiService.isRunning();

        toolMap.get("newDocument").setEnabled(!dirty && notPlaying);
        toolMap.get("open").setEnabled(!dirty && notPlaying);
        toolMap.get("write").setEnabled(dirty);
        toolMap.get("closeDocument").setEnabled(dirty && notPlaying);
        toolMap.get("prop").setEnabled(true);
        toolMap.get("addTrack").setEnabled(notPlaying);
        toolMap.get("addLyrics").setEnabled(hasSingleSelection && notPlaying);
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

    public JToolBar getFunctionToolbar() {
        return functionToolbar;
    }

    public Map<String,JButton> getToolMap() {
        return toolMap;
    }
}
