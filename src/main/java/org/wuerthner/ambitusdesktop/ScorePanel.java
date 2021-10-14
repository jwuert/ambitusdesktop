package org.wuerthner.ambitusdesktop;

import org.wuerthner.ambitus.model.AmbitusFactory;
import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.model.MidiTrack;
import org.wuerthner.ambitus.model.NoteEvent;
import org.wuerthner.ambitus.tool.SelectionTools;
import org.wuerthner.ambitusdesktop.score.AmbitusScoreCanvas;
import org.wuerthner.ambitusdesktop.score.AmbitusScoreLayout;
import org.wuerthner.ambitusdesktop.service.MidiService;
import org.wuerthner.ambitusdesktop.ui.ArrangementConfig;
import org.wuerthner.ambitusdesktop.ui.BarConfig;
import org.wuerthner.ambitusdesktop.ui.Wizard;
import org.wuerthner.cwn.api.*;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.Location;
import org.wuerthner.cwn.score.ScoreBuilder;
import org.wuerthner.cwn.score.ScorePresenter;
import org.wuerthner.cwn.score.ScoreUpdate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;

public class ScorePanel extends JPanel implements MouseMotionListener, MouseListener {
    private final AmbitusFactory factory = new AmbitusFactory();
    private final MidiService midiService = new MidiService();
    private final ScoreModel scoreModel;
    private final PanelUpdater panelUpdater;
    private final ToolbarUpdater toolbarUpdater;
    private final SelectionTools selectionTools = new SelectionTools();
    private Optional<Location> pressLocation = Optional.empty();
    private Optional<Location> releaseLocation = Optional.empty();
    private Optional<Location> dragLocation = Optional.empty();
    private Optional<Integer> key = Optional.empty();
    private long startDisplayPosition = 0;
    private long endDisplayPosition = 0;
    private JTextField lyricsField = null;

    public ScorePanel(ScoreModel scoreModel, PanelUpdater panelUpdater, ToolbarUpdater toolbarUpdater) {
        this.scoreModel = scoreModel;
        this.panelUpdater = panelUpdater;
        this.toolbarUpdater = toolbarUpdater;
        this.lyricsField = new JTextField();
        lyricsField.setBounds(0,0,0,0);
        this.lyricsField.setVisible(false);
        this.add(lyricsField);
        lyricsField.setBounds(0,0,0,0);
        lyricsField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        lyricsField.setEditable(false);

        InputMap imap = lyricsField.getInputMap(JComponent.WHEN_FOCUSED);
        imap.put(KeyStroke.getKeyStroke("SPACE"), "spaceAction");
        ActionMap amap = lyricsField.getActionMap();
        amap.put("spaceAction", new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                org.wuerthner.ambitus.model.Event event = setLyrics(e);
                if (event!=null) {
                    MidiTrack track = (MidiTrack) event.getParent();
                    selectionTools.moveCursorRight(track, scoreModel.getSelection(), NoteEvent.class);
                    ScorePanel.this.updateUI();
                }
            }
        });
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterAction");
        amap.put("enterAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setLyrics(e);
                scoreModel.setLyrics(false);
                ScorePanel.this.updateUI();
            }
        });

//        lyricsField.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (scoreModel.getSelection().hasSingleSelection()) {
//                    JTextField field = (JTextField) e.getSource();
//                    String value = field.getText();
//                    org.wuerthner.ambitus.model.Event event = scoreModel.getSelection().getSingleSelection();
//                    if (event instanceof NoteEvent) {
//                        scoreModel.getArrangement().setLyrics((NoteEvent)event, value);
//                    }
//                    MidiTrack track = (MidiTrack) event.getParent();
//                    selectionTools.moveCursorRight(track, scoreModel.getSelection(), NoteEvent.class);
//                    ScorePanel.this.updateUI();
//                }
//            }
//        });
    }

    private org.wuerthner.ambitus.model.Event setLyrics(ActionEvent e) {
        org.wuerthner.ambitus.model.Event event = null;
        if (scoreModel.getSelection().hasSingleSelection()) {
            JTextField field = (JTextField) e.getSource();
            String value = field.getText().trim();
            event = scoreModel.getSelection().getSingleSelection();
            if (event instanceof NoteEvent) {
                scoreModel.getArrangement().setLyrics((NoteEvent)event, value);
            }
        }
        return event;
    }

    public void updateScore(ScoreUpdate update) {
        scoreModel.getScoreBuilder().update(update);
        this.updateUI();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        double zoom = scoreModel.getZoom();
        scoreModel.updateWidth((int)(this.getWidth()/zoom));
        scoreModel.updateScoreParameter();
        ScoreBuilder builder = scoreModel.getScoreBuilder();
        int barOffset = scoreModel.getArrangement().getAttributeValue(Arrangement.offset);
        AmbitusScoreCanvas scoreCanvas = new AmbitusScoreCanvas(g, zoom, this.getHeight());
        ScoreLayout scoreLayout = scoreModel.getLayout();
        ScorePresenter presenter = new ScorePresenter(builder, scoreCanvas, scoreLayout, (CwnSelection<CwnEvent>)(CwnSelection<?>) scoreModel.getSelection());
        if (scoreModel.debugScore()) presenter.debug();

        if (scoreLayout!=null && builder!=null && scoreModel.getSelection()!=null) {
            presenter.present(scoreModel.getTitle(), scoreModel.getSubtitle(), scoreModel.getComposer(), barOffset);
            if (scoreModel.getArrangement().getActiveMidiTrackList().isEmpty()) {
                String text = "Welcome to Ambitus!";
                String text2 = "Click to open Wizard!";
                scoreCanvas.drawString(text, "lyrics", (int)(0.5*this.getWidth()/zoom), (int)(0.5*this.getHeight()/zoom), "center");
                scoreCanvas.drawString(text2, "lyrics", (int)(0.5*this.getWidth()/zoom), (int)(0.5*this.getHeight()/zoom + 12), "center");
            } else {
                if (MidiService.isRunning()) {
                    // cacheScore = true;
                    int endBar = presenter.getFirstBarCompletelyOutOfDisplay();
                    CwnTrack firstTrack = scoreModel.getTrackList().get(0);
                    endDisplayPosition = PositionTools.getPosition(firstTrack, new Trias(endBar, 0, 0));

                    // System.out.println("-> " + scoreModel.getSelection().getCursorPosition() + " ? " + presenter.getFirstPositionPartiallyOutOfDisplay());
                    if (scoreModel.getSelection().getCursorPosition() >= presenter.getFirstPositionPartiallyOutOfDisplay()) {
                        // System.out.println("yes");
                        endDisplayPosition = scoreModel.getArrangement().findLastPosition();
                        long newStartPosition = presenter.getFirstPositionPartiallyOutOfDisplay();
                        int newBarOffset = PositionTools.getTrias(firstTrack, newStartPosition).bar;
                        scoreModel.getArrangement().setTransientBarOffset(newBarOffset);
                        scoreModel.getScoreParameter().setBarOffset(newBarOffset);
                        // recalculateScore();
                        updateScore(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                    }
                } else {
                    endDisplayPosition = 0;
                    // cacheScore = false;
                    if (scoreModel.getSelection().hasSingleSelection() && scoreModel.lyrics()) {
                        org.wuerthner.ambitus.model.Event event = scoreModel.getSelection().getSingleSelection();
                        if (event instanceof NoteEvent) {
                            long pos = event.getPosition();
                            ScoreBuilder.Coordinates coor = scoreModel.getScoreBuilder().findCoordinates(pos, scoreModel.getSelection().getSelectedStaff());
                            lyricsField.setBounds((int)(coor.X * scoreModel.getZoom()),
                                    (int)(coor.Y * scoreModel.getZoom()),
                                    (int)(32 * scoreModel.getZoom()),
                                    (int)(11 * scoreModel.getZoom()));
                            lyricsField.setEditable(true);
                            lyricsField.setText(((NoteEvent)event).getLyrics());
                            lyricsField.setVisible(true);
                            lyricsField.grabFocus();
                            //lyricsField.paintAll(g);
                        }
                    } else {
                        lyricsField.setEditable(false);
                        lyricsField.setVisible(false);
                    }
                }
            }
            toolbarUpdater.updateNoteBar();
        } else {
            System.err.println("layout: " + (scoreLayout!=null) + ", builder: " + (builder!=null) + ", sel: " + scoreModel.getSelection());
        }
    }

    private int getX(MouseEvent e) {
        return (int)( e.getX()/scoreModel.getZoom() + 1 );
    }

    private int getY(MouseEvent e) {
        int y = (int) (e.getY()/scoreModel.getZoom()) + 26;
        return y;
    }

    private Location getLocation(MouseEvent e) {
        int x = getX(e);
        int y = getY(e);
        int ticks = scoreModel.getGridTicks();
        long startPosition = (scoreModel.getTrackList().isEmpty() ? 0 :
                PositionTools.getPosition(scoreModel.getTrackList().get(0), new Trias(scoreModel.getArrangement().getBarOffset(), 0, 0)));
        Location location = scoreModel.getScoreBuilder().findPosition(x, y, ticks, startPosition);
        return location;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Location location = getLocation(e);
        scoreModel.getSelection().setCursor(location);
        this.updateUI();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Location location = getLocation(e);
        boolean ctrl = (e.getModifiers() & KeyEvent.CTRL_MASK) > 0;
        boolean shft = (e.getModifiers() & KeyEvent.SHIFT_MASK) > 0;
        int enharmonicShift = (shft?1:ctrl?-1:0);
        if (location==null) {
            if (scoreModel.getArrangement().getNumberOfActiveMidiTracks()==0) {
                Arrangement newArrangement = Wizard.createArrangement(scoreModel, this);
                if (newArrangement != null) {
                    scoreModel.setArrangement(newArrangement);
                }
                this.updateScore(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                panelUpdater.updatePanel();
            } else {
                new ArrangementConfig(scoreModel, this);
                this.updateScore(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
                panelUpdater.updatePanel();
            }
        } else if (location.barConfig) {
            new BarConfig(scoreModel, this, location);
            this.updateScore(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
            panelUpdater.updatePanel();
        } else {
            if (location.scoreBar != null) {
                int pitch = location.pitch + enharmonicShift;
                int staff = location.staffIndex;
                long pos = location.position;
                CwnTrack track = scoreModel.getTrackList().get(staff);
                long duration = (long) NoteSelector.getNoteLength(scoreModel.getPPQ(), scoreModel.getNoteSelector());
                scoreModel.addOrSelectNoteEvent(pos, pitch, enharmonicShift, staff);
                // this.updateScore(new ScoreUpdate(scoreModel.getArrangement().getSelectedMidiTrack(), pos, pos+duration));
                // this.updateScore((new ScoreUpdate(ScoreUpdate.Type.REBUILD).restrictToTrack(track).restrictToRange(pos, pos+duration)));
                NoteEvent note = factory.createElement(NoteEvent.TYPE);
                note.performTransientSetAttributeValueOperation(NoteEvent.pitch, pitch);
                note.performTransientSetAttributeValueOperation(NoteEvent.duration, duration);
                note.performTransientSetAttributeValueOperation(NoteEvent.velocity, 100);
                midiService.playPitch(note);
            } else if (location.staffIndex >= 0) {
                scoreModel.getSelection().set(location.staffIndex);
                this.updateScore(new ScoreUpdate());
            }
        }
        toolbarUpdater.updateToolbar();
        this.updateUI();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressLocation = Optional.ofNullable(getLocation(e));
        dragLocation = Optional.ofNullable(getLocation(e));
        releaseLocation = Optional.empty();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        dragLocation = Optional.ofNullable(getLocation(e));
        if (pressLocation.isPresent() && dragLocation.isPresent()) {
            scoreModel.getSelection().setMouseFrame(-1, pressLocation.get().position, dragLocation.get().position);
        }
        updateScore(new ScoreUpdate());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        releaseLocation = Optional.ofNullable(getLocation(e));
        if (pressLocation.isPresent() && releaseLocation.isPresent()) {
            if (! (pressLocation.get().barConfig && releaseLocation.get().barConfig)) {
                scoreModel.select(pressLocation.get(), releaseLocation.get());
            } else {
                scoreModel.getSelection().getPointer().clear();
            }
        }
        dragLocation = Optional.empty();
        scoreModel.getSelection().unsetMouseFrame();
        updateScore(new ScoreUpdate(ScoreUpdate.Type.REDRAW));
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
