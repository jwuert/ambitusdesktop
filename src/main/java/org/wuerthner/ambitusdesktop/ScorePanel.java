package org.wuerthner.ambitusdesktop;

import org.wuerthner.ambitus.model.AmbitusFactory;
import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.model.NoteEvent;
import org.wuerthner.ambitusdesktop.score.AmbitusScoreCanvas;
import org.wuerthner.ambitusdesktop.service.MidiService;
import org.wuerthner.ambitusdesktop.ui.ArrangementConfig;
import org.wuerthner.ambitusdesktop.ui.BarConfig;
import org.wuerthner.ambitusdesktop.ui.Wizard;
import org.wuerthner.cwn.api.*;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.Location;
import org.wuerthner.cwn.score.ScoreBuilder;
import org.wuerthner.cwn.score.ScorePresenter;

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
    private Optional<Location> pressLocation = Optional.empty();
    private Optional<Location> releaseLocation = Optional.empty();
    private Optional<Location> dragLocation = Optional.empty();
    private Optional<Integer> key = Optional.empty();
    private long startDisplayPosition = 0;
    private long endDisplayPosition = 0;

    public ScorePanel(ScoreModel scoreModel, PanelUpdater panelUpdater, ToolbarUpdater toolbarUpdater) {
        this.scoreModel = scoreModel;
        this.panelUpdater = panelUpdater;
        this.toolbarUpdater = toolbarUpdater;
    }

    public void touchScore() {
        scoreModel.touchScore();
        this.updateUI();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        ScoreBuilder builder = scoreModel.getScoreBuilder(this.getWidth());
        double zoom = scoreModel.getZoom();
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
                        System.out.println("yes");
                        endDisplayPosition = scoreModel.getArrangement().findLastPosition();
                        long newStartPosition = presenter.getFirstPositionPartiallyOutOfDisplay();
                        int newBarOffset = PositionTools.getTrias(firstTrack, newStartPosition).bar;
                        scoreModel.getArrangement().setTransientBarOffset(newBarOffset);
                        // recalculateScore();
                        touchScore();
                    }
                } else {
                    endDisplayPosition = 0;
                    // cacheScore = false;
                }
            }
        } else {
            System.err.println("layout: " + (scoreLayout!=null) + ", builder: " + (builder!=null) + ", sel: " + scoreModel.getSelection());
        }
    }

    private int getX(MouseEvent e) {
        // return (int) ((e.getX()-this.getLocation().getX())/scoreModel.getZoom()) + 1;
        return (int)( e.getX()/scoreModel.getZoom() + 1 );
    }

    private int getY(MouseEvent e) {
        // int y = (int) ((e.getY()-this.getLocation().getY())/scoreModel.getZoom());
        int y = (int) (e.getY()/scoreModel.getZoom()) + 26;
        // y = (scoreModel.getZoom()==1 ? y-9 : y+9);
        return y;
    }

    private Location getLocation(MouseEvent e) {
        int x = getX(e);
        int y = getY(e);
        int ticks = scoreModel.getGridTicks();
        Location location = scoreModel.getScoreBuilder(this.getWidth()).findPosition(x, y, ticks);
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
                new Wizard(scoreModel, this);
                this.touchScore();
                panelUpdater.updatePanel();
            } else {
                new ArrangementConfig(scoreModel, this);
                this.touchScore();
                panelUpdater.updatePanel();
            }
        } else if (location.barConfig) {
            new BarConfig(scoreModel, this, location);
            this.touchScore();
            panelUpdater.updatePanel();
        } else {
            if (location.scoreBar != null) {
                int pitch = location.pitch + enharmonicShift;
                int staff = location.staffIndex;
                long pos = location.position;
                long duration = (long) NoteSelector.getNoteLength(scoreModel.getPPQ(), scoreModel.getNoteSelector());
                scoreModel.addOrSelectNoteEvent(pos, pitch, enharmonicShift, staff);
                touchScore();
                NoteEvent note = factory.createElement(NoteEvent.TYPE);
                note.performTransientSetAttributeValueOperation(NoteEvent.pitch, pitch);
                note.performTransientSetAttributeValueOperation(NoteEvent.duration, duration);
                note.performTransientSetAttributeValueOperation(NoteEvent.velocity, 100);
                midiService.playPitch(note);
            } else if (location.staffIndex >= 0) {
                scoreModel.getSelection().set(location.staffIndex);
                touchScore();
            }
        }
        toolbarUpdater.updateToolbar();
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
        touchScore();
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
        touchScore();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

//    @Override
//    public void keyTyped(KeyEvent e) {
//
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        key = Optional.of(e.getKeyCode());
//        System.out.println(e.getKeyCode() + ", " + e.getExtendedKeyCode() + ", " + e.getKeyChar() + ", " + e.getModifiers() + ", " + e.getModifiersEx());
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//        key = Optional.empty();
//    }

//    public static class TheSelection implements CwnSelection<CwnEvent> {
//        private final CwnPointer pointer = new ThePointer();
//
//        @Override
//        public boolean contains(CwnEvent event) {
//            // TODO Auto-generated method stub
//            return false;
//        }
//
//        @Override
//        public boolean hasStaffSelected(int index) {
//            // TODO Auto-generated method stub
//            return false;
//        }
//
//        @Override
//        public boolean isEmpty() {
//            // TODO Auto-generated method stub
//            return false;
//        }
//
//        @Override
//        public boolean hasCursor() {
//            // TODO Auto-generated method stub
//            return false;
//        }
//
//        @Override
//        public long getCursorPosition() {
//            // TODO Auto-generated method stub
//            return 0;
//        }
//
//        @Override
//        public boolean hasMouseDown() {
//            // TODO Auto-generated method stub
//            return false;
//        }
//
//        @Override
//        public long getMouseLeftPosition() {
//            // TODO Auto-generated method stub
//            return 0;
//        }
//
//        @Override
//        public long getMouseRightPosition() {
//            // TODO Auto-generated method stub
//            return 0;
//        }
//
//        @Override
//        public int getMouseStaff() {
//            // TODO Auto-generated method stub
//            return 0;
//        }
//
//        @Override
//        public CwnSelection.SelectionType getSelectionType() {
//            return SelectionType.NOTE;
//        }
//
//        @Override
//        public CwnPointer getPointer() {
//            return pointer;
//        }
//
//        public void setCursor(Location location) {
//            if (location==null) {
//                pointer.clear();
//            } else {
//                pointer.setPosition(location.position);
//                pointer.setPitch(location.pitch);
//                pointer.setStaffIndex(location.staffIndex);
//                if (location.barConfig) {
//                    pointer.setRegion(CwnPointer.Region.CONFIG);
//                } else {
//                    pointer.setRegion(CwnPointer.Region.SCORE);
//                }
//            }
//        }
//    }
//
//    public static class ThePointer implements CwnPointer {
//        private long position;
//        private int pitch;
//        private Region region;
//        private int staffIndex;
//
//        @Override
//        public Region getRegion() {
//            return region;
//        }
//
//        @Override
//        public long getPosition() {
//            return position;
//        }
//
//        @Override
//        public void setPosition(long position) {
//            this.position = position;
//        }
//
//        @Override
//        public int getPitch() {
//            return pitch;
//        }
//
//        @Override
//        public void setPitch(int pitch) {
//            this.pitch = pitch;
//        }
//
//        @Override
//        public void setRegion(Region region) {
//            this.region = region;
//        }
//
//        @Override
//        public int getStaffIndex() {
//            return staffIndex;
//        }
//
//        @Override
//        public void setStaffIndex(int index) {
//            this.staffIndex = index;
//        }
//
//        @Override
//        public void clear() {
//            position = -1;
//            region = NONE;
//        }
//    }
}
