package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.model.*;
import org.wuerthner.ambitus.model.Event;
import org.wuerthner.ambitus.service.*;
import org.wuerthner.ambitus.tool.MidiImportService;
import org.wuerthner.ambitus.type.NamedRange;
import org.wuerthner.ambitusdesktop.*;
import org.wuerthner.ambitusdesktop.service.ExportService;
import org.wuerthner.ambitusdesktop.service.MidiService;
import org.wuerthner.ambitusdesktop.service.PrintService;
import org.wuerthner.cwn.api.CwnTrack;
import org.wuerthner.cwn.api.Markup;
import org.wuerthner.cwn.api.Trias;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.ScoreUpdate;
import org.wuerthner.cwn.util.Harmony;
import org.wuerthner.sport.api.Modifier;
import org.wuerthner.sport.core.XMLReader;
import org.wuerthner.sport.core.XMLWriter;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionToolBar implements PositionUpdater {
    private final JToolBar functionToolbar;
    private final MidiService midiService = new MidiService();
    private final PrintService printService = new PrintService();
    private final ExportService exportService = new ExportService();
    private final Map<String,JButton> toolMap = new HashMap<>();
    private final ScoreModel scoreModel;
    private final ScoreUpdater scoreUpdater;
    private final PanelUpdater panelUpdater;
    private final ToolbarUpdater toolbarUpdater;
    private final JTextField positionField;
    private JFileChooser fileChooser = null;
    private RecentFileChooser.RecentFileList recentFileList = null;

    public FunctionToolBar(ScoreModel scoreModel, ScoreUpdater scoreUpdater, ToolbarUpdater toolbarUpdater, PanelUpdater panelUpdater, JPanel content, int WIDTH) {
        this.scoreModel = scoreModel;
        this.scoreUpdater = scoreUpdater;
        this.toolbarUpdater = toolbarUpdater;
        this.panelUpdater = panelUpdater;
        this.functionToolbar = new JToolBar();

        createFileChooser();
        
        // New Document Wizard!
        JButton newDocumentBtn = makeButton("toolbar/newDocument", "new Document", 24);
        AbstractAction newDocumentAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Arrangement newArrangement = Wizard.createArrangement(scoreModel, content);
                if (newArrangement != null) {
                    scoreModel.setArrangement(newArrangement);
                }
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
                int result = fileChooser.showOpenDialog(content);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    recentFileList.add(selectedFile);
                    try {
                        FileInputStream inputStream = new FileInputStream(selectedFile);
                        // XmlElementReader reader = new XmlElementReader(scoreModel.factory, Arrangement.TYPE);
                        XMLReader reader = new XMLReader(scoreModel.factory, Arrangement.TYPE, "root");
                        Arrangement root = (Arrangement) reader.run(inputStream);
                        if (root != null) {
                            scoreModel.setArrangement(root);
                            scoreModel.setFile(selectedFile);
                            scoreModel.updateScoreParameter();
                            scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                            panelUpdater.updatePanel();
                            updateToolbar();
                            updatePosition();
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

        // Open Recent
        JButton openShftBtn = makeButton("toolbar/open_recent", "Open Most Recent File", 0);
        AbstractAction openShft = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<File> optFile = recentFileList.getMostRecent();
                if (optFile.isPresent()) {
                    File selectedFile = optFile.get();
                    try {
                        FileInputStream inputStream = new FileInputStream(selectedFile);
                        XMLReader reader = new XMLReader(scoreModel.factory, Arrangement.TYPE, "root");
                        Arrangement root = (Arrangement) reader.run(inputStream);
                        if (root != null) {
                            scoreModel.setArrangement(root);
                            scoreModel.setFile(selectedFile);
                            scoreModel.updateScoreParameter();
                            scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                            panelUpdater.updatePanel();
                            updateToolbar();
                            updatePosition();
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        };
        openShftBtn.addActionListener(openShft);
        openShftBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "openShft");
        openShftBtn.getActionMap().put("openShft", openShft);
        toolMap.put("openShft", openShftBtn);
        functionToolbar.add(openShftBtn);

        // Open MIDI
        JButton openMidiBtn = makeButton("toolbar/importMidi", "Open Midi File", 24);
        AbstractAction openMidi = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = fileChooser.showOpenDialog(content);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    recentFileList.add(selectedFile);
                    try {
                        FileInputStream inputStream = new FileInputStream(selectedFile);
                        // TODO: import midi
                        MidiImportService midiImportService = new MidiImportService();
                        Arrangement root = midiImportService.readMidiFile(inputStream, scoreModel.factory);
                        if (root != null) {
                            scoreModel.setArrangement(root);
                            //scoreModel.setFile(selectedFile);
                            scoreModel.updateScoreParameter();
                            scoreModel.getScoreParameter().setBarOffset(scoreModel.getArrangement().getBarOffset());
                            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                            panelUpdater.updatePanel();
                            updateToolbar();
                            updatePosition();
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            }
        };
        openMidiBtn.addActionListener(openMidi);
        // openMidiBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), "openMidi");
        openMidiBtn.getActionMap().put("openMidi", openMidi);
        toolMap.put("openMidi", openMidiBtn);
        functionToolbar.add(openMidiBtn);

        // Save
        JButton writeBtn = makeButton("toolbar/write", "Save File",24);
        AbstractAction writeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File fileToSave = scoreModel.getFile();
                saveFile(fileToSave, content, "Save file");
                if (scoreModel.getFile() == null) {
                    scoreModel.setFile(fileToSave);
                }
            }
        };
        writeBtn.addActionListener(writeAction);
        writeBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "write");
        writeBtn.getActionMap().put("write", writeAction);
        toolMap.put("write", writeBtn);
        functionToolbar.add(writeBtn);

        // Save As...
        JButton writeAsBtn = makeButton("toolbar/writeAs", "Save File As",24);
        AbstractAction writeAsAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile(null, content, "Save file as...");
            }
        };
        writeAsBtn.addActionListener(writeAsAction);
        writeAsBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK), "writeAs");
        writeAsBtn.getActionMap().put("writeAs", writeAsAction);
        toolMap.put("writeAs", writeAsBtn);
        functionToolbar.add(writeAsBtn);

        // Print
        JButton printBtn = makeButton("toolbar/print", "Print",24);
        AbstractAction printAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printService.print(scoreModel.getArrangement());
            }
        };
        printBtn.addActionListener(printAction);
        printBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), "print");
        printBtn.getActionMap().put("print", printAction);
        toolMap.put("print", printBtn);
        functionToolbar.add(printBtn);

        // Close Document
        JButton closeDocument = makeButton("toolbar/close", "Close Document",0);
        AbstractAction closeDocumentAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean clear = false;
                if (scoreModel.getArrangement().hasUndo()) {
                    int dialogResult = JOptionPane.showConfirmDialog(content, "Close and lose unsaved changes?", "Warning", JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        clear = true;
                    }
                } else {
                    clear = true;
                }
                if (clear) {
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
        propBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "prop");
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

        // Remove Track
        JButton removeTrack = makeButton("toolbar/deleteTrack", "Delete Track",24);
        AbstractAction removeTrackAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.removeTrack();
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD)); // TODO: restrict to new track!
                panelUpdater.updatePanel();
                updateToolbar();
            }
        };
        removeTrack.addActionListener(removeTrackAction);
        //removeTrack.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "removeTrack");
        removeTrack.getActionMap().put("removeTrack", removeTrackAction);
        toolMap.put("removeTrack", removeTrack);
        functionToolbar.add(removeTrack);

        // SEPARATOR
        functionToolbar.addSeparator(new Dimension(10, 40));

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
        // addLyrics.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "addLyrics");
        addLyrics.getActionMap().put("addLyrics", addLyricsAction);
        toolMap.put("addLyrics", addLyrics);
        functionToolbar.add(addLyrics);

        // Add Accents
        JButton addAccents = makeButton("toolbar/accents", "Add Accents",24);
        AbstractAction addAccentsAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean accents =scoreModel.toggleAccents();
                if (!accents) {
                    if (!scoreModel.getNoteSelector().isNote()) {
                        scoreModel.setNoteSelector(NoteSelector.N4);
                    }
                }
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                updateToolbar();
                toolbarUpdater.updateNoteOrAccent();
            }
        };
        addAccents.addActionListener(addAccentsAction);
        // addAccents.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "addAccents");
        addAccents.getActionMap().put("addAccents", addAccentsAction);
        toolMap.put("addAccents", addAccents);
        functionToolbar.add(addAccents);

        // Add Chord
        JButton addChord = makeButton("toolbar/chord", "Add Chord",24);
        AbstractAction addChordAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (scoreModel.getSelection().hasSingleSelection()) {
                    Event event = scoreModel.getSelection().getSingleSelection();
                    CwnTrack track = scoreModel.getTrackList().get(0);
                    Trias trias = PositionTools.getTrias(track, event.getPosition());
                    String position = trias.toFormattedString();
                    String chord = "";
                    List<String> modeList = Arrays.asList(Harmony.Fix.CHORD.name(), Harmony.Fix.HARMONY.name(), Harmony.Fix.ALL.name(), Harmony.Fix.CHORD.name());
                    ParameterDialog pd = new ParameterDialog(new String[]{"Add Chord"},
                            new String[]{"Position", "Chord", "Mode"},
                            new Object[]{position, chord, modeList.toArray(new String[]{})},
                            content);
                    String[] parameters = pd.getParameters();
                    if (parameters != null) {
                        long newPosition = PositionTools.getPosition(track, parameters[0]);
                        String newChord = parameters[1];
                        String newMode = parameters[2];
                        scoreModel.addOrRemoveInfoEvent(newPosition, newChord, newMode);
                    }
                    scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                    updateToolbar();
                }

//                boolean accents =scoreModel.toggleAccents();
//                if (!accents) {
//                    if (!scoreModel.getNoteSelector().isNote()) {
//                        scoreModel.setNoteSelector(NoteSelector.N4);
//                    }
//                }
//                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
//                updateToolbar();
//                toolbarUpdater.updateNoteOrAccent();
            }
        };
        addChord.addActionListener(addChordAction);
        // addChord.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "addChord");
        addChord.getActionMap().put("addChord", addChordAction);
        toolMap.put("addChord", addChord);
        functionToolbar.add(addChord);

        // SEPARATOR
        functionToolbar.addSeparator(new Dimension(10, 40));

        // Undo
        JButton undoBtn = makeButton("toolbar/undo", "Undo",24);
        AbstractAction undoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scoreModel.undo();
                scoreModel.updateScoreParameter();
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
                scoreModel.updateScoreParameter();
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
                //if (selectedMidiTrack!=null) {

                    String posString = (String) JOptionPane.showInputDialog(
                            content,
                            "Enter position (format: bar.beat.ticks)",
                            "Paste",
                            JOptionPane.PLAIN_MESSAGE
                    );
                    if (posString != null) {
                        Trias newPositionTrias = new Trias(posString);
                        long newPosition = PositionTools.getPosition(selectedMidiTrack==null ? scoreModel.getTrackList().get(0) : selectedMidiTrack, newPositionTrias);
                        long oldPosition = scoreModel.getClipboard().getElements().get(0).getPosition();
                        long deltaPosition = newPosition - oldPosition;
                        Modifier<Event> modifier = new PasteModifier(deltaPosition);
                        scoreModel.pasteSelection(modifier);
                        scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                        panelUpdater.updatePanel();
                        updateToolbar();
                    }
                //}
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
                Scope scope = new OperationDialog("Quantize", content, scoreModel.getSelection().isEmpty()).show();
                if (scope != null) {
                    Quantize.run(scoreModel.getArrangement(), scoreModel.getGridTicks(), scope);
                    scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                    updateToolbar();
                }
            }
        };
        quantizeBtn.addActionListener(quantizeAction);
        quantizeBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK), "quantize");
        quantizeBtn.getActionMap().put("quantize", quantizeAction);
        toolMap.put("quantize", quantizeBtn);
        functionToolbar.add(quantizeBtn);

        // Insert Space
        JButton insertBtn = makeButton("toolbar/insertSpace", "Insert Space", 24);
        AbstractAction insertAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OperationDialog od = new OperationDialog("Insert Space", content, scoreModel.getSelection().isEmpty());
                od.add("Number of beats", "0");
                Scope scope = od.show();
                if (scope != null) {
                    try {
                        int numberOfBeats = Integer.valueOf(od.getValue());
                        InsertSpace.run(scoreModel.getArrangement(), numberOfBeats, scope);
                    } catch (NumberFormatException ne) {
                        JOptionPane.showConfirmDialog(content, "Wrong format for 'Number of beats': " + od.getValue());
                    }
                    scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                    updateToolbar();
                }
            }
        };
        insertBtn.addActionListener(insertAction);
        // insertBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK), "insertSpace");
        insertBtn.getActionMap().put("insertSpace", insertAction);
        toolMap.put("insertSpace", insertBtn);
        functionToolbar.add(insertBtn);

        // Scale
        JButton scaleBtn = makeButton("toolbar/scale", "Scale", 24);
        AbstractAction scaleAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OperationDialog od = new OperationDialog("Scale", content, scoreModel.getSelection().isEmpty());
                od.add("Scale factor", "1.0");
                Scope scope = od.show();
                if (scope != null) {
                    try {
                        double factor = Double.valueOf(od.getValue());
                        Scale.run(scoreModel.getArrangement(), factor, scope);
                    } catch (NumberFormatException ne) {
                        JOptionPane.showConfirmDialog(content, "Wrong format for 'Scale factor': " + od.getValue());
                    }
                    scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                    updateToolbar();
                }
            }
        };
        scaleBtn.addActionListener(scaleAction);
        // scaleBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK), "scale");
        scaleBtn.getActionMap().put("scale", scaleAction);
        toolMap.put("scale", scaleBtn);
        functionToolbar.add(scaleBtn);

        // Transpose
        JButton transposeBtn = makeButton("toolbar/transpose", "Transpose", 24);
        AbstractAction transposeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OperationDialog od = new OperationDialog("Transpose", content, scoreModel.getSelection().isEmpty());
                od.add("Transpose value", "0");
                Scope scope = od.show();
                if (scope != null) {
                    String transposeString = od.getValue();
                    try {
                        int transposeValue = Integer.valueOf(transposeString);
                        Transpose.run(scoreModel.getArrangement(), transposeValue, scope);
                        scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                        panelUpdater.updatePanel();
                        updateToolbar();
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showConfirmDialog(content, "Wrong format for 'Transpose value': " + od.getValue());
                    }
                }
            }
        };
        transposeBtn.addActionListener(transposeAction);
        transposeBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), "transpose");
        transposeBtn.getActionMap().put("transpose", transposeAction);
        toolMap.put("transpose", transposeBtn);
        functionToolbar.add(transposeBtn);

        if (scoreModel.debug()) {
            // Clean Up
            JButton cleanupBtn = makeButton("toolbar/cleanup", "Clean Up", 24);
            AbstractAction cleanupAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Scope scope = new OperationDialog("Clean Up", content, scoreModel.getSelection().isEmpty()).show();
                    CleanUp.run(scoreModel.getArrangement(), scope);
                    scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                    panelUpdater.updatePanel();
                    updateToolbar();
                }
            };
            cleanupBtn.addActionListener(cleanupAction);
            cleanupBtn.getActionMap().put("cleanup", cleanupAction);
            toolMap.put("cleanup", cleanupBtn);
            functionToolbar.add(cleanupBtn);
        }

        // AutoShift
        JButton autoShiftBtn = makeButton("toolbar/autoshift", "Auto Shift", 24);
        AbstractAction autoShiftAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Scope scope = new OperationDialog("Auto Shift", content, scoreModel.getSelection().isEmpty()).show();
                AutoShift.run(scoreModel.getArrangement(), scope);
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                panelUpdater.updatePanel();
                updateToolbar();
            }
        };
        autoShiftBtn.addActionListener(autoShiftAction);
        autoShiftBtn.getActionMap().put("autoshift", autoShiftAction);
        toolMap.put("autoshift", autoShiftBtn);
        functionToolbar.add(autoShiftBtn);

        // Velocity
        JButton velocityBtn = makeButton("toolbar/velocity", "Velocity", 24);
        AbstractAction velocityAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OperationDialog od = new OperationDialog("Velocity", content, scoreModel.getSelection().isEmpty());
                od.add("Velocity", "0");
                Scope scope = od.show();

                String velocityString = od.getValue();
                try {
                    int velocityValue = Integer.valueOf(velocityString);
                    Velocity.run(scoreModel.getArrangement(), velocityValue, scope);
                    scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                    panelUpdater.updatePanel();
                    updateToolbar();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showConfirmDialog(content, "Wrong format for 'Velocity value': " + od.getValue());
                }
            }
        };
        velocityBtn.addActionListener(velocityAction);
        velocityBtn.getActionMap().put("velocity", velocityAction);
        toolMap.put("velocity", velocityBtn);
        functionToolbar.add(velocityBtn);

        // SEPARATOR
        functionToolbar.addSeparator(new Dimension(10, 40));

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
        firstBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.ALT_DOWN_MASK), "rewind");
        firstBtn.getActionMap().put("rewind", firstAction);
        toolMap.put("rewind", firstBtn);
        functionToolbar.add(firstBtn);

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
        prevBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK), "previous");
        prevBtn.getActionMap().put("previous", prevAction);
        toolMap.put("previous", prevBtn);
        functionToolbar.add(prevBtn);

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
        positionField.addFocusListener(new FieldFocusListener(scoreModel, positionFieldColor));
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
            }
        });
        functionToolbar.add(positionField);

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
        nextBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK), "next");
        nextBtn.getActionMap().put("next", nextAction);
        toolMap.put("next", nextBtn);
        functionToolbar.add(nextBtn);

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
        lastBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.ALT_DOWN_MASK), "last");
        lastBtn.getActionMap().put("last", lastAction);
        toolMap.put("last", lastBtn);
        functionToolbar.add(lastBtn);

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

        // Play
        JButton playBtn = makeButton("toolbar/play", "Play",24);
        AbstractAction playAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                midiService.play(scoreModel.getArrangement(), scoreModel.getSelection(), false,false);
                new Thread(new ScorePlayer(content, toolbarUpdater, FunctionToolBar.this)).start();
                updateToolbar();
            }
        };
        playBtn.addActionListener(playAction);
        playBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "play");
        playBtn.getActionMap().put("play", playAction);
        toolMap.put("play", playBtn);
        functionToolbar.add(playBtn);

        // Play Config
        JButton playConfBtn = makeButton("toolbar/playConfig", "Play Configuration",24);
        AbstractAction playConfAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tempo = "" + scoreModel.getPlayTempo();
                String strength = "" + scoreModel.getPlayStrength();

                List<String> exposeList = new ArrayList<>();
                exposeList.add("-");
                exposeList.addAll(scoreModel.getTrackList().stream().map(track -> track.getName()).collect(Collectors.toList()));
                exposeList.add(scoreModel.getPlayExpose() < 0 ? "-" : exposeList.get(scoreModel.getPlayExpose() + 1));
                List<String> strengthList = new ArrayList<>();
                strengthList.addAll(Arrays.asList("1", "2", "3", "4"));
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
                        int newTempo = Integer.valueOf(newTempoS);
                        int newStrength = Integer.valueOf(newStrengthS);
                        int newExpose = exposeList.indexOf(newExposeS) - 1;
                        scoreModel.setPlayExpose(newExpose);
                        scoreModel.setPlayStrength(newStrength);
                        scoreModel.setPlayTempo(newTempo);
                        midiService.play(scoreModel.getArrangement(), scoreModel.getSelection(), false, false, newTempo, newExpose, newStrength);
                        new Thread(new ScorePlayer(content, toolbarUpdater, FunctionToolBar.this)).start();
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
        functionToolbar.add(playConfBtn);

        // Play Selection
        JButton playSelBtn = makeButton("toolbar/playSelection", "Play Selection",24);
        AbstractAction playSelAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                midiService.play(scoreModel.getArrangement(), scoreModel.getSelection(), true, false);
                new Thread(new ScorePlayer(content, toolbarUpdater, FunctionToolBar.this)).start();
                updateToolbar();
            }
        };
        playSelBtn.addActionListener(playSelAction);
        playSelBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "playSelection");
        playSelBtn.getActionMap().put("playSelection", playSelAction);
        toolMap.put("playSelection", playSelBtn);
        functionToolbar.add(playSelBtn);

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
        functionToolbar.add(exportBtn);


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

//        // List (Multi-Project?)
//        JButton listBtn = makeButton("toolbar/list", "Toggle Sheet/System", 24);
//        listBtn.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                scoreModel.toggleList();
//                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
//                updateToolbar();
//            }
//        });
//        functionToolbar.add(listBtn);

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
//        JButton markupBtn = makeButton("toolbar/mark", "Toggle Markup", 24);
//        markupBtn.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                List<Markup> list = scoreModel.getScoreBuilder().getScoreParameter().markup;
//                ParameterDialog pd = new ParameterDialog(new String[]{"Select Markup Elements"},
//                        new String[]{"Ambitus", "Attributes", "Parallels", "Intervals", "Crossings", "Lyrics", "Note Attributes", "Color Voices"},
//                        new Object[]{
//                                list.contains(Markup.AMBITUS),
//                                list.contains(Markup.ATTRIBUTES),
//                                list.contains(Markup.PARALLELS),
//                                list.contains(Markup.INTERVALS),
//                                list.contains(Markup.CROSSINGS),
//                                list.contains(Markup.LYRICS),
//                                list.contains(Markup.NOTE_ATTRIBUTES),
//                                list.contains(Markup.COLOR_VOICES)
//                        },
//                        content);
//                String[] parameters = pd.getParameters();
//                boolean update = false;
//                if (parameters != null) {
//                    List<Markup> newList = new ArrayList<>();
//                    if (Boolean.valueOf(parameters[0])) newList.add(Markup.AMBITUS);
//                    if (Boolean.valueOf(parameters[1])) newList.add(Markup.ATTRIBUTES);
//                    if (Boolean.valueOf(parameters[2])) newList.add(Markup.PARALLELS);
//                    if (Boolean.valueOf(parameters[3])) newList.add(Markup.INTERVALS);
//                    if (Boolean.valueOf(parameters[4])) newList.add(Markup.CROSSINGS);
//                    if (Boolean.valueOf(parameters[5])) newList.add(Markup.LYRICS);
//                    if (Boolean.valueOf(parameters[6])) newList.add(Markup.NOTE_ATTRIBUTES);
//                    if (Boolean.valueOf(parameters[7])) newList.add(Markup.COLOR_VOICES);
//                    update = !newList.equals(list);
//                    scoreModel.getScoreBuilder().getScoreParameter().markup = newList;
//                }
//                if (update) {
//                    scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
//                    updateToolbar();
//                }
//            }
//        });
//        functionToolbar.add(markupBtn);

        // Range
        JButton rangeBtn = makeButton("toolbar/bookmark", "Bookmark",24);
        AbstractAction rangeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MidiTrack track = scoreModel.getArrangement().getActiveMidiTrackList().get(0);
                long startNo = scoreModel.getSelection().isEmpty() ? scoreModel.getArrangement().getBarOffsetPosition() : scoreModel.getSelection().getSelection().get(0).getPosition();
                // long endNo = scoreModel.getSelection().getSelection().get(scoreModel.getSelection().getSelection().size()-1).getPosition();
                Trias startTrias = PositionTools.getTrias(track, startNo);
                // Trias endTrias = PositionTools.getTrias(track, endNo);

                ParameterDialog pd = new ParameterDialog(new String[]{"Create Range"},
                        new String[]{"Name", "Position"},
                        new Object[]{"", startTrias.toString()},
                        content);
                String[] parameters = pd.getParameters();
                if (parameters!=null) {
                    String name = parameters[0];
                    long start = PositionTools.getPosition(track, new Trias(parameters[1]));
                    // long end = PositionTools.getPosition(track, new Trias(parameters[2]));
                    scoreModel.getArrangement().addRange(new NamedRange(name, start));
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

        // About
        JButton aboutBtn = makeButton("toolbar/about", "About",24);
        AbstractAction aboutAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame infoFrame = new JFrame("Info");
                infoFrame.setLayout(new BorderLayout());

                JLabel label = new JLabel("Ambitus, J. Würthner (c) 2022");
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setPreferredSize(new Dimension(520, 32));
                infoFrame.getContentPane().add(label, BorderLayout.PAGE_START);

                Vector<String> columnData = new Vector<>();
                columnData.add("Key");
                columnData.add("Description");

                Vector<Vector<String>> rowData = new Vector<>();

                rowData.add(new Vector<String>(Arrays.asList("Left Arrow", "Select event at previous position")));
                rowData.add(new Vector<String>(Arrays.asList("Right Arrow", "Select event at following position")));
                rowData.add(new Vector<String>(Arrays.asList("Up Arrow", "Select following event")));
                rowData.add(new Vector<String>(Arrays.asList("Down Arrow", "Select previous event")));
                rowData.add(new Vector<String>(Arrays.asList("Page Up", "Select previous staff")));
                rowData.add(new Vector<String>(Arrays.asList("Page Down", "Select the following staff")));
                rowData.add(new Vector<String>(Arrays.asList("Alt Left Arrow", "Decrease bar offset")));
                rowData.add(new Vector<String>(Arrays.asList("Alt Right Arrow", "Increase bar offset")));
                rowData.add(new Vector<String>(Arrays.asList("Alt Home", "Reset bar offset to zero")));
                rowData.add(new Vector<String>(Arrays.asList("Alt End", "Set bar offset to end")));

                rowData.add(new Vector<String>(Arrays.asList("Ctrl Left Arrow", "Move note left")));
                rowData.add(new Vector<String>(Arrays.asList("Ctrl Right Arrow", "Move note right")));
                rowData.add(new Vector<String>(Arrays.asList("Ctrl Up Arrow", "Move note up")));
                rowData.add(new Vector<String>(Arrays.asList("Ctrl Down Arrow", "Move note down")));
                rowData.add(new Vector<String>(Arrays.asList("Shift Left Arrow", "Decrease note length (grid)")));
                rowData.add(new Vector<String>(Arrays.asList("Shift Right Arrow", "Increase note length (grid)")));
                rowData.add(new Vector<String>(Arrays.asList("Ctrl #", "Increase enharmonic shift")));
                rowData.add(new Vector<String>(Arrays.asList("Ctrl b", "Decrease enharmonic shift")));
                rowData.add(new Vector<String>(Arrays.asList("Ctrl d", "Double note length")));
                rowData.add(new Vector<String>(Arrays.asList("Ctrl h", "Halve note length")));
                rowData.add(new Vector<String>(Arrays.asList("Ctrl e", "Open editor dialog")));

                rowData.add(new Vector<String>(Arrays.asList("Ctrl .", "Toggle none, one, two or three dots")));
                rowData.add(new Vector<String>(Arrays.asList("Ctrl m", "Mute/unmute selected track")));
                rowData.add(new Vector<String>(Arrays.asList("F5", "Refresh display")));

                JTable table = new JTable(rowData, columnData);
                JScrollPane scrollPane = new JScrollPane(table);
                table.setFillsViewportHeight(true);

                infoFrame.getContentPane().add(table, BorderLayout.CENTER);

                infoFrame.setSize(520, 460);
                infoFrame.setLocationRelativeTo(content);
                infoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                infoFrame.setTitle("About");
                infoFrame.setVisible(true);
            }
        };
        aboutBtn.addActionListener(aboutAction);
        aboutBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('?', KeyEvent.CTRL_DOWN_MASK), "about");
        aboutBtn.getActionMap().put("about", aboutAction);
        toolMap.put("about", aboutBtn);
        functionToolbar.add(aboutBtn);

        if (scoreModel.debug()) {
            JButton outputBtn = makeButton("toolbar/output", "Output", 24);
            outputBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFrame infoFrame = new JFrame("Info");
                    infoFrame.setLayout(new BorderLayout());
                    JButton ok = new JButton("ok");
                    ok.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            infoFrame.dispose();
                        }
                    });

                    JTabbedPane tabbedPane = new JTabbedPane();

                    //
                    // Journal
                    //
                    JTextArea ta = new JTextArea();
                    ta.setText(scoreModel.journal());
                    tabbedPane.add("Journal", ta);

                    //
                    // Score
                    //
                    JTextArea scoreTextArea = new JTextArea();
                    scoreTextArea.setText(scoreModel.getScoreBuilder().toString());
                    JScrollPane scoreScrollPane = new JScrollPane(scoreTextArea);
                    tabbedPane.add("Score", scoreScrollPane);

                    //
                    // Event Table
                    //
                    for (int i = 0; i < scoreModel.getArrangement().getTrackList().size(); i++) {
                        CwnTrack cwnTrack = scoreModel.getArrangement().getTrackList().get(i);
                        Vector<Vector<String>> rowData = new Vector<>();
                        Vector<String> columnData = createColumnData(cwnTrack, rowData);
                        JTable table = new JTable(rowData, columnData);
                        JScrollPane scrollPane = new JScrollPane(table);
                        table.setFillsViewportHeight(true);
                        table.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyReleased(KeyEvent e) {
                                super.keyReleased(e);
                                if (e.getKeyCode() == KeyEvent.VK_DELETE && cwnTrack instanceof MidiTrack) {
                                    int index = table.getSelectedRow();
                                    List<Event> eventList = new ArrayList<>();
                                    eventList.add(((MidiTrack) cwnTrack).get(table.getSelectedRow()));
                                    scoreModel.getArrangement().deleteElements(eventList);
                                    scoreUpdater.update(new ScoreUpdate(scoreModel.getTrackList(), scoreModel.getSelection()));
                                    panelUpdater.updatePanel();
                                    updateToolbar();
                                    ((DefaultTableModel) table.getModel()).removeRow(index);
                                    table.repaint();
                                }
                            }
                        });
                        tabbedPane.add(scoreModel.getArrangement().getTrackList().get(i).getName(), scrollPane);
                    }

                    infoFrame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
                    infoFrame.getContentPane().add(ok, BorderLayout.PAGE_END);
                    infoFrame.setSize(1000, 600);
                    infoFrame.setLocationRelativeTo(content);
                    infoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    infoFrame.setTitle("Info");
                    infoFrame.setVisible(true);

//                    System.out.println("------------------------------------------");
//                    System.out.println(scoreModel.getScoreBuilder().toString());
//                    System.out.println("------------------------------------------");
                }
            });
            functionToolbar.add(outputBtn);
        }

        // SEPARATOR
        functionToolbar.add(new JSeparator());

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
        exitBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "exit");
        exitBtn.getActionMap().put("exit", exitAction);
        toolMap.put("exit", exitBtn);
        functionToolbar.add(exitBtn);

        if (scoreModel.debug()) {
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
        }
    }

    private Vector<String> createColumnData(CwnTrack cwnTrack, Vector<Vector<String>> rowData) {
        Vector<String> columnData = new Vector<>();
        if (cwnTrack instanceof MidiTrack) {
            columnData.add("Type");
            columnData.add("Position");
            columnData.add("Duration");
            columnData.add("Pitch/Value");
            columnData.add("Velocity");
            columnData.add("Lyrics");
            columnData.add("Enh Shift");
            MidiTrack track = (MidiTrack) cwnTrack;
            for (Event event : track.getChildrenByClass(Event.class)) {
                Vector<String> row = new Vector<>();
                row.add(event.getType());
                Trias trias = PositionTools.getTrias(track, event.getPosition());

                row.add(trias.toString());
                row.add("" + (event.getDuration() == 0 ? "" : event.getDuration()));
                row.add(event instanceof NoteEvent ? "" + ((NoteEvent) event).getCPitch() + " (" + ((NoteEvent) event).getPitch() + ")" :
                        event instanceof TempoEvent ? "" + ((TempoEvent) event).getTempo() :
                                event instanceof org.wuerthner.ambitus.model.KeyEvent ? "" +
                                        Arrangement.KEYS[
                                                ((org.wuerthner.ambitus.model.KeyEvent) event).getGenus() == 1
                                                        ? (((org.wuerthner.ambitus.model.KeyEvent) event).getKey() + 7 + 3) % 15
                                                        : ((org.wuerthner.ambitus.model.KeyEvent) event).getKey() + 7
                                                ] :
                                        event instanceof ClefEvent ? "" + Arrangement.CLEFS[((ClefEvent) event).getClef()] :
                                                event instanceof TimeSignatureEvent ? "" + ((TimeSignatureEvent) event).getTimeSignature() : "");
                row.add(event instanceof NoteEvent ? "" + ((NoteEvent) event).getVelocity() :
                        event instanceof TempoEvent ? "" + ((TempoEvent) event).getLabel() :
                                event instanceof org.wuerthner.ambitus.model.KeyEvent ? "" + Arrangement.GENUS[((org.wuerthner.ambitus.model.KeyEvent) event).getGenus()] :
                                        event instanceof ClefEvent ? "" + ((ClefEvent) event).getClef() : "");
                row.add(event instanceof NoteEvent ? "" + ((NoteEvent) event).getLyrics() : "");
                row.add(event instanceof NoteEvent ? "" + ((NoteEvent) event).getEnharmonicShift() : "");
                rowData.add(row);
            }
        } else if (cwnTrack.isInfoTrack()) {
            columnData.add("Type");
            columnData.add("Position");
            columnData.add("Info");
            columnData.add("Parameter");
            InfoTrack track = (InfoTrack) cwnTrack;
            for (InfoEvent event : track.getChildrenByClass(InfoEvent.class)) {
                Vector<String> row = new Vector<>();
                row.add(event.getType());
                Trias trias = PositionTools.getTrias(track, event.getPosition());
                row.add(trias.toString());
                row.add(event.getInfo(0));
                row.add(event.getInfo(1));
                rowData.add(row);
            }
        }
        return columnData;
    }

    private void createFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.setPreferredSize(new Dimension(800,500));
        Action details = fileChooser.getActionMap().get("viewTypeDetails");
        details.actionPerformed(null);
        recentFileList = new RecentFileChooser.RecentFileList(fileChooser);
        recentFileList.load();
        fileChooser.setAccessory(recentFileList);
        fileChooser.getAccessory().setPreferredSize(new Dimension(240,500));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter filter = new FileNameExtensionFilter("Ambitus",ScoreModel.FILE_EXTENSION);
        fileChooser.setFileFilter(filter);
    }

    private void saveFile(File fileToSave, JPanel content, String title) {
        if (fileToSave == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(title);
            // fileChooser.setDialogTitle("Specify a file to save");
            Action details = fileChooser.getActionMap().get("viewTypeDetails");
            details.actionPerformed(null);
            int userSelection = fileChooser.showSaveDialog(content);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().endsWith("." + ScoreModel.FILE_EXTENSION)) {
                    fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + "." + ScoreModel.FILE_EXTENSION);
                }
            }
        }
        if (fileToSave!=null) {
            try {
                recentFileList.add(fileToSave);
                FileOutputStream outputStream = new FileOutputStream(fileToSave);
                Arrangement arrangement = scoreModel.getArrangement();
                XMLWriter writer = new XMLWriter();
                writer.run(arrangement, outputStream);
                scoreModel.getArrangement().clearHistory();
                scoreModel.setFile(fileToSave);
                scoreModel.updateScoreParameter();
                scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REDRAW));
                panelUpdater.updatePanel();
                updateToolbar();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private JButton makeButton(String function, String tooltip) {
        return makeButton(function, tooltip, -1);
    }

    private JButton makeButton(String function, String tooltip, int size) {
        URL url = getClass().getResource("/" + function + ".png");
        ImageIcon icon = new ImageIcon(url);
        JButton button;
        if (size > 0) {
            icon = new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
            button = new JButton(icon);
        } else {
            button = new JButton("");
            button.setPreferredSize(new Dimension(1,24));
        }
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
        toolMap.get("openShft").setEnabled(!dirty && notPlaying);
        // toolMap.get("openShft").setVisible(false);
        toolMap.get("write").setEnabled(dirty);
        toolMap.get("writeAs").setEnabled(noOfTracks > 0 && notPlaying);
        toolMap.get("print").setEnabled(noOfTracks > 0 && notPlaying);
        toolMap.get("closeDocument").setEnabled(notPlaying);
        toolMap.get("prop").setEnabled(notPlaying);
        toolMap.get("addTrack").setEnabled(notPlaying);
        toolMap.get("addLyrics").setEnabled(hasSingleSelection && notPlaying);
        toolMap.get("addAccents").setEnabled(noOfTracks > 0 && notPlaying && !scoreModel.lyrics());
        toolMap.get("addChord").setEnabled(hasSingleSelection && notPlaying);
        toolMap.get("undo").setEnabled(dirty && notPlaying);
        toolMap.get("redo").setEnabled(hasRedo && notPlaying);
        toolMap.get("delete").setEnabled(hasSelection && notPlaying);
        toolMap.get("cut").setEnabled(hasSelection && notPlaying);
        toolMap.get("copy").setEnabled(hasSelection && notPlaying);
        toolMap.get("paste").setEnabled(hasClipboard && notPlaying);
        toolMap.get("clearSelection").setEnabled(hasSelection);
        toolMap.get("quantize").setEnabled(noOfTracks>0 && notPlaying);
        toolMap.get("insertSpace").setEnabled(noOfTracks>0 && notPlaying);
        toolMap.get("scale").setEnabled(noOfTracks>0 && notPlaying);
        toolMap.get("transpose").setEnabled(noOfTracks>0 && notPlaying);
        if (scoreModel.debug()) toolMap.get("cleanup").setEnabled(noOfTracks>0 && notPlaying);
        toolMap.get("autoshift").setEnabled(noOfTracks>0 && notPlaying);
        toolMap.get("rewind").setEnabled(noOfTracks > 0);
        toolMap.get("previous").setEnabled(noOfTracks > 0);
        toolMap.get("next").setEnabled(noOfTracks > 0);
        toolMap.get("last").setEnabled(noOfTracks > 0);
        toolMap.get("play").setEnabled(noOfTracks > 0 && notPlaying);
        toolMap.get("playConfiguration").setEnabled(noOfTracks > 0 && notPlaying);
        toolMap.get("playSelection").setEnabled(noOfTracks > 0 && notPlaying);
        toolMap.get("export").setEnabled(noOfTracks > 0 && notPlaying);
        toolMap.get("stop").setEnabled(! notPlaying);
        toolMap.get("range").setEnabled(noOfTracks > 0);
        updatePosition();
    }

    public JToolBar getFunctionToolbar() {
        return functionToolbar;
    }

    public Map<String,JButton> getToolMap() {
        return toolMap;
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
}
