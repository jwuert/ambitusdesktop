package org.wuerthner.ambitusdesktop;

import org.wuerthner.ambitus.model.*;
import org.wuerthner.ambitus.template.Template;
import org.wuerthner.ambitus.tool.AbstractSelection;
import org.wuerthner.ambitusdesktop.score.AmbitusScoreLayout;
import org.wuerthner.ambitusdesktop.score.AmbitusSelection;
import org.wuerthner.cwn.api.*;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.Location;
import org.wuerthner.cwn.score.Score;
import org.wuerthner.cwn.score.ScoreBuilder;
import org.wuerthner.cwn.score.ScoreUpdate;
import org.wuerthner.cwn.timesignature.SimpleTimeSignature;
import org.wuerthner.sport.api.*;
import org.wuerthner.sport.core.XMLElementWriter;
import org.wuerthner.sport.core.XmlElementReader;
import org.wuerthner.sport.operation.Transaction;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ScoreModel {
    public static final String FILE_EXTENSION = "amb";
    private static final List<Markup.Type> markup = Arrays.asList(Markup.Type.LYRICS, Markup.Type.NOTE_ATTRIBUTES);
    private static final boolean debug = false;
    private static boolean debugScore = false;

    private NoteSelector noteSelector = NoteSelector.N8;
    private NoteSelector gridSelector = NoteSelector.N8;
    private NoteSelector tupletSelector = NoteSelector.T1;
    private NoteSelector voiceSelector = NoteSelector.V1;

    private File file = null;
    private double zoom = 1.5; // 0=auto, 1, 2
    private boolean lyrics = false;
    private boolean accents = false;
    private boolean keyboardShortcutsActive = true;
    private int numberOfSystems = 9999;

    private Arrangement arrangement = null;
    private ScoreBuilder scoreBuilder;
    private AmbitusScoreLayout scoreLayout;
    private int width = 0;
    private int playTempo = 100;
    private int playStrength = 3;
    private int playExpose = -1;

    public final AmbitusFactory factory = new AmbitusFactory();

    public ScoreModel(int width) {
        arrangement = factory.createElement(Arrangement.TYPE);
        arrangement.setSelection(new AmbitusSelection());
        setNoteSelector(NoteSelector.N4);
        setTupletSelector(NoteSelector.T1);
        this.width = width;
        scoreLayout = new AmbitusScoreLayout((int) (width * 1.0 / getZoom()), getPPQ(), false);
        ScoreParameter scoreParameter = createScoreParameter(arrangement);
        scoreBuilder = new ScoreBuilder(arrangement, scoreParameter, scoreLayout, numberOfSystems);
        updateScoreParameter();
    }

    public void clear(int width) {
        arrangement = factory.createElement(Arrangement.TYPE);
        arrangement.setSelection(new AmbitusSelection());
        setNoteSelector(NoteSelector.N4);
        setTupletSelector(NoteSelector.T1);
        setVoiceSelector(NoteSelector.V1);
        this.width = width;
        scoreLayout = new AmbitusScoreLayout((int) (width * 1.0 / getZoom()), getPPQ(), getScoreParameter().markup.contains(Markup.Type.VELOCITY));
        ScoreParameter scoreParameter = createScoreParameter(arrangement);
        scoreBuilder = new ScoreBuilder(arrangement, scoreParameter, scoreLayout, numberOfSystems);
        updateScoreParameter();
        this.setFile(null);
    }

    public ScoreLayout getLayout() {
        return scoreLayout;
    }

    public AmbitusSelection getSelection() {
        return (AmbitusSelection) arrangement.getSelection();
    }

    public Clipboard<Event> getClipboard() { return arrangement.getClipboard(); }

    public ScoreParameter getScoreParameter() {
        return getScoreBuilder().getScoreParameter();
    }

    public boolean debug() {
        return debug;
    }

    public void setDebugScore(boolean d) {
        this.debugScore = d;
    }

    public boolean debugScore() {
        return debugScore;
    }

    public boolean getKeyboardShortcutsActive() { return keyboardShortcutsActive; }

    public void setKeyboardShortcutsActive(boolean active) { keyboardShortcutsActive = active; }

    public int getPPQ() {
        return arrangement==null ? 0 : arrangement.getPPQ();
    }

    public double getZoom() {
        if (zoom==0) {
            if (arrangement==null) {
                return 2;
            } else {
                if (arrangement.getLastPosition() < 30*arrangement.getPPQ()) {
                    return 2;
                } else {
                    return 1;
                }
            }
        } else {
            return zoom;
        }
    }

    public void toggleZoom() {
        zoom += 0.5;
        if (zoom==2.5)
            zoom = 1.0;
    }

    public void toggleList() {
        if (numberOfSystems==1) {
            numberOfSystems = 9999;
        } else {
            numberOfSystems = 1;
        }
        if (scoreBuilder!=null) { scoreBuilder.setNumberOfSystems(numberOfSystems); }
    }

    public void toggleLyrics() {
        lyrics = !lyrics;
    }

    public boolean toggleAccents() {
        accents = !accents;
        return accents;
    }

    public void toggleMute() {
        int selectedStaff = getSelection().getSelectedStaff();
        MidiTrack track = (MidiTrack) arrangement.getTrackList().get(selectedStaff);
        boolean mute = track.getMute();
        arrangement.setTrackMute(track, !mute);
    }

    public void setLyrics(boolean b) {
        lyrics = b;
    }

    public boolean lyrics() {
        return lyrics;
    }

    public boolean accents() { return accents; }

    public ScoreBuilder getScoreBuilder() {
        return scoreBuilder;
    }

    public List<CwnTrack> getTrackList() {
        List<CwnTrack> trackList;
        if (arrangement != null) {
            trackList = arrangement.getTrackList();
        } else {
            trackList = new ArrayList<>();
        }
        return trackList;
    }

    public void updateWidth(int width) {
        if (this.width!=width) {
            this.width = width;
            scoreLayout.setWidth(width);
            scoreBuilder.update(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
        }
    }

    public void setNoteSelector(NoteSelector noteSelector) {
        this.noteSelector = noteSelector;
    }

    public NoteSelector getNoteSelector() {
        return noteSelector;
    }

    public void setGridSelector(NoteSelector gridSelector) {
        this.gridSelector = gridSelector;
        if (arrangement!=null) {
            arrangement.setGrid(gridSelector.getNoteIndex());
        }
    }

    public NoteSelector getGridSelector() {
        return gridSelector;
    }

    public void setLevelSelector(int level) {
        if (arrangement!=null) {
            arrangement.setGroupLevel(level);
            updateScoreParameter();
        }
    }

    public int getGroupLevel() { return (arrangement!=null ? arrangement.getGroupLevel() : 0); }

    public void setTupletSelector(NoteSelector tupletSelector) {
        this.tupletSelector = tupletSelector;
    }

    public void setVoiceSelector(NoteSelector voiceSelector) {
        this.voiceSelector = voiceSelector;
    }

    public NoteSelector getTupletSelector() {
        return this.tupletSelector;
    }

    public NoteSelector getVoiceSelector() {
        return this.voiceSelector;
    }

    public int getGridTicks() {
        int ticks = (int) (gridSelector.getNoteLength()*4*getPPQ() * tupletSelector.getTupletFactor());
        return ticks;
    }

    public void addTrack() {
        if (arrangement!=null) {
            arrangement.addTrack(factory);
            // init(width);
        }
    }

    public void removeTrack() {
        if (arrangement!=null) {
            MidiTrack track = (MidiTrack) arrangement.getTrackList().get(getSelection().getSelectedStaff());
            arrangement.removeTrack(track);
        }
    }

    public Optional<NoteEvent> selectNoteEvent(long position, int pitch, int enharmonicShift, int staff) {
        Optional<NoteEvent> noteEventOptional = Optional.empty();
        if (arrangement != null) {
            AbstractSelection selection = arrangement.getSelection();
            MidiTrack track = arrangement.getChildrenByClass(MidiTrack.class).get(staff);
            NoteEvent note = track.findFirstEventAtPositionOrNull(position, NoteEvent.class);
            if (note != null && note.getPitch() == pitch) {
                // SELECT
                selection.set(note, staff, CwnSelection.SelectionType.NOTE, CwnSelection.SelectionSubType.NONE);
                noteEventOptional = Optional.of(note);
            }
        }
        return noteEventOptional;
    }

    public void addOrSelectNoteEvent(long position, int pitch, int enharmonicShift, int staff, boolean selectOnly) {
        if (arrangement != null) {
            int voice = voiceSelector==NoteSelector.V1 ? 0 : 1;
            AbstractSelection selection = arrangement.getSelection();
            MidiTrack track = arrangement.getChildrenByClass(MidiTrack.class).get(staff);
            NoteEvent note = track.findFirstNoteAtPositionInVoiceOrNull(position, voice);
            long duration = (long) (noteSelector.getNoteLength() * getPPQ() * 4 * tupletSelector.getTupletFactor());
            if (note!=null && note.getPitch()==pitch) {
                // SELECT
                selection.set(note, staff, CwnSelection.SelectionType.NOTE, CwnSelection.SelectionSubType.NONE);
            } else if (selectOnly) {
                note = track.findFirstNoteAtPositionOrNull(position);
                if (note != null) {
                    selection.set(note, staff, CwnSelection.SelectionType.NOTE, CwnSelection.SelectionSubType.NONE);
                }
            } else {
                // CREATE
                NoteEvent noteEvent = factory.createElement(NoteEvent.TYPE);
                noteEvent.performTransientSetAttributeValueOperation(NoteEvent.position, position);
                noteEvent.performTransientSetAttributeValueOperation(NoteEvent.duration, duration);
                noteEvent.performTransientSetAttributeValueOperation(NoteEvent.pitch, pitch);
                noteEvent.performTransientSetAttributeValueOperation(NoteEvent.shift, enharmonicShift);
                noteEvent.performTransientSetAttributeValueOperation(NoteEvent.velocity, 87);
                noteEvent.performTransientSetAttributeValueOperation(NoteEvent.voice, voice);
                arrangement.addEventWithCaution(track, noteEvent);
                selection.unsetMouseFrame();
                int i = arrangement.getChildrenByClass(MidiTrack.class).indexOf(track);
                selection.set(noteEvent, i, CwnSelection.SelectionType.NOTE, CwnSelection.SelectionSubType.NONE);
                scoreBuilder.update(new ScoreUpdate(track, selection));
            }
        }
    }

    public void addOrSelectSymbolEvent(Optional<Location> startPosition, Optional<Location> endPosition, String name) {
        if (arrangement != null && startPosition.isPresent() && endPosition.isPresent()) {
            long position = startPosition.get().position;
            long duration = endPosition.get().position - position;
            int voice = voiceSelector==NoteSelector.V1 ? 0 : 1;
            int staff = endPosition.get().staffIndex;
            int deltaY = endPosition.get().yRelative - startPosition.get().yRelative;
            int relativeY = startPosition.get().yRelative;
            AbstractSelection selection = arrangement.getSelection();
            MidiTrack track = arrangement.getChildrenByClass(MidiTrack.class).get(staff);
            SymbolEvent symbol = track.findFirstEventAtPositionOrNull(position, SymbolEvent.class);
            if (symbol!=null && symbol.getSymbolName().equals(name)) {
                // SELECT
                selection.set(symbol, staff, CwnSelection.SelectionType.NOTE, CwnSelection.SelectionSubType.NONE);
            } else {
                // CREATE
                int verticalOffset = 0;
                int parameter = 0;
                if (SymbolEvent.withinStaff(name)) {
                    verticalOffset = relativeY;
                    parameter = deltaY;
                }
                SymbolEvent symbolEvent = factory.createElement(SymbolEvent.TYPE);
                symbolEvent.performTransientSetAttributeValueOperation(SymbolEvent.duration, duration);
                symbolEvent.performTransientSetAttributeValueOperation(SymbolEvent.name, name);
                symbolEvent.performTransientSetAttributeValueOperation(SymbolEvent.position, position);
                symbolEvent.performTransientSetAttributeValueOperation(SymbolEvent.verticalOffset, verticalOffset);
                symbolEvent.performTransientSetAttributeValueOperation(SymbolEvent.parameter, parameter);
                symbolEvent.performTransientSetAttributeValueOperation(SymbolEvent.voice, voice);
                arrangement.addEvent(track, symbolEvent);

                selection.unsetMouseFrame();
                int i = arrangement.getChildrenByClass(MidiTrack.class).indexOf(track);
                selection.set(symbolEvent, i, CwnSelection.SelectionType.NOTE, CwnSelection.SelectionSubType.NONE);
                scoreBuilder.update(new ScoreUpdate(track, selection));
            }
        }
    }

    public void addOrRemoveInfoEvent(long newPosition, String newChord, String newMode) {
        if (arrangement != null) {
            arrangement.addOrRemoveInfoEvent(newPosition, newChord, newMode, factory);
        }
    }

    public void unsetMouseFrame() {
        arrangement.getSelection().unsetMouseFrame();
    }

    public void modifySelection(String property, int direction) {
        AbstractSelection selection = arrangement.getSelection();
        if (selection != null) {
            for (CwnEvent event : selection.getSelection()) {
                if (event instanceof NoteEvent) {
                    if (property.equals("pitch")) {
                        int pitch = ((NoteEvent) event).getPitch();
                        ((NoteEvent) event).performTransientSetAttributeValueOperation(NoteEvent.pitch, pitch + direction);
                    } else if (property.equals("position")) {
                        long position = ((Event) event).getPosition();
                        int unit = (int) (arrangement.getAttributeValue(Arrangement.pulsePerQuarter) * 4.0 / Math.pow(2, arrangement.getAttributeValue(Arrangement.resolution)));
                        long change = unit * direction;
                        ((NoteEvent) event).performTransientSetAttributeValueOperation(NoteEvent.position, position + change);
                    } else if (property.equals("duration")) {
                        long duration = ((Event) event).getDuration();
                        int unit = (int) (arrangement.getAttributeValue(Arrangement.pulsePerQuarter) * 4.0 / Math.pow(2, arrangement.getAttributeValue(Arrangement.resolution)));
                        long change = unit * direction;
                        ((NoteEvent) event).performTransientSetAttributeValueOperation(NoteEvent.duration, duration + change);
                    }
                }
            }
        }
    }

    public void undo() {
        arrangement.undo();
    }

    public void redo() {
        arrangement.redo();
    }

    public File getFile() { return file; }

    public void setFile(File file) { this.file = file; }

    public void play() {
//        MidiFileWriter writer = new MidiFileWriter(arrangement);
//        try {
//            File tmpFile = File.createTempFile("ambitus", ".mid");
//            writer.write(tmpFile);
//            MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.fromFile(tmpFile));
//            mediaPlayer.start();
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
//        }
    }

    public void setTrackProperties(String trackId, boolean mute, int volume, String name, String metric, int key, int genus, int clef, int bar, int tempo, int instrument, int channel, boolean newPiano) {
        arrangement.setTrackProperties(trackId, mute, volume, name, metric, key, genus, clef, CwnBarEvent.TYPES[bar], tempo, instrument, channel, newPiano, factory);
    }

    public void setBarProperties(String trackId, long barPosition, String metric, int key, int genus, int clef, int bar, int tempo) {
        arrangement.setBarProperties(trackId, barPosition, metric, key, genus, clef, CwnBarEvent.TYPES[bar], tempo, factory);
    }

    public String getMetric(String trackId, long barPosition) {
        return arrangement.getTrackBarMetric(trackId, barPosition);
    }

    public String getMetric(int trackIndex, long barPosition) {
        return arrangement.getTrackBarMetric(trackIndex, barPosition);
    }

    public int getKey(String trackId, long barPosition) {
        return arrangement.getTrackBarKey(trackId, barPosition);
    }

    public int getKey(int trackIndex, long barPosition) {
        return arrangement.getTrackBarKey(trackIndex, barPosition);
    }

    public int getGenus(int trackIndex, long barPosition) {
        return arrangement.getTrackBarKeyGenus(trackIndex, barPosition);
    }

    public int getBarIndex(int trackIndex, long barPosition) {
        Optional<Integer> trackBarEvent = arrangement.getTrackBarEvent(trackIndex, barPosition);
        return (trackBarEvent.isPresent() ? trackBarEvent.get() : 0);
    }

    public int getClef(String trackId, long barPosition) {
        return arrangement.getTrackBarClef(trackId, barPosition);
    }

    public int getClef(int trackIndex, long barPosition) {
        return arrangement.getTrackBarClef(trackIndex, barPosition);
    }

    public int getTempo(String trackId, long barPosition) {
        return arrangement.getTrackBarTempo(trackId, barPosition);
    }

    public int getTempo(int trackIndex, long barPosition) {
        return arrangement.getTrackBarTempo(trackIndex, barPosition);
    }

    public boolean getMute(int trackIndex) {
        return arrangement.getTrackList().get(trackIndex).getMute();
    }

    public boolean getPiano(int trackIndex) {
        return arrangement.getTrackList().get(trackIndex).getPiano();
    }

    public int getVolume(int trackIndex) {
        return arrangement.getTrackList().get(trackIndex).getVolume();
    }

    public int getChannel(int trackIndex) {
        return arrangement.getTrackList().get(trackIndex).getChannel();
    }

    public int getInstrument(int trackIndex) {
        return arrangement.getTrackList().get(trackIndex).getInstrument();
    }

//    public void changeNoteEvent(int pitchChange, int positionChange, int durationChange, int dotChange, int enhChange, int voiceChange) {
//        if (arrangement != null) {
//            List<CwnEvent> selectedElements = selection.getSelectedElements();
//            arrangement.setSelectionProperties(selectedElements, pitchChange, positionChange, durationChange, dotChange, enhChange, voiceChange);
//        }
//    }

//    public void changeNote(SelectionDialog.Modification property, int value) {
//        if (arrangement != null) {
//            List<CwnEvent> selectedElements = selection.getSelectedElements();
//            switch (property) {
//                case PITCH:
//                    arrangement.changeSelectionPitch(selectedElements, value);
//                    break;
//                case POSITION:
//                    arrangement.changeSelectionPosition(selectedElements, value);
//                    break;
//                case ENHARMONIC_SHIFT:
//                    arrangement.changeSelectionEnharmonicShift(selectedElements, value);
//                    break;
//                case VOICE:
//                    arrangement.changeSelectionVoice(selectedElements, value);
//                    break;
//                default:
//            }
//            refresh();
//        }
//    }

//    public void changeNote(SelectionDialog.Modification property, int value1, int value2) {
//        if (arrangement != null) {
//            List<CwnEvent> selectedElements = selection.getSelectedElements();
//            switch (property) {
//                case DURATION:
//                    arrangement.changeSelectionDuration(selectedElements, value1, value2);
//                    break;
//                default:
//            }
//            refresh();
//        }
//    }

    public Arrangement createArrangement(String title, String subtitle, String composer, int tempo, int keySelection, String timeSignature, int templateSelection) {
        Template template = Template.createTemplate(templateSelection);
        Arrangement arrangement = template.apply(keySelection, tempo, new SimpleTimeSignature(timeSignature), factory);
        arrangement.init(title, subtitle, composer);
        arrangement.setSelection(new AmbitusSelection());
        // updateWidth(width);
        return arrangement;
    }

    public void select(Location fromPosition, Location toPosition, boolean notesOnly) {
        long fromTick = fromPosition.position;
        long toTick = toPosition.position;
        List<ModelElement> eventList = new ArrayList<>();
        int index = 0;
        int minStaffIndex = Math.min(fromPosition.staffIndex, toPosition.staffIndex);
        int maxStaffIndex = Math.max(fromPosition.staffIndex, toPosition.staffIndex);
        List<ModelElement> trackList = arrangement.getChildrenByType(MidiTrack.TYPE);

        Class<? extends Event> clasz = notesOnly ? NoteEvent.class : Event.class;
        for (ModelElement midiTrack : trackList) {
            if (minStaffIndex <= index && index <= maxStaffIndex) {
                for (ModelElement midiEvent : midiTrack.getChildrenByClass(clasz)) {
                    Event event = (Event) midiEvent;
                    long start = event.getPosition();
                    if (fromTick <= start && start <= toTick) {
                        eventList.add(event);
                    }
                }
            }
            index++;
        }
        AbstractSelection selection = arrangement.getSelection();
        int staff = (minStaffIndex==maxStaffIndex ? minStaffIndex : -1);
        selection.set(eventList, staff, CwnSelection.SelectionType.NOTE, CwnSelection.SelectionSubType.NONE);
    }

    public String getTitle() {
        return arrangement==null ? "" : arrangement.getAttributeValue(Arrangement.name)==null ? "" : arrangement.getAttributeValue(Arrangement.name);
    }
    public String getSubtitle() {
        return arrangement==null ? "" : arrangement.getAttributeValue(Arrangement.subtitle)==null ? "" : arrangement.getAttributeValue(Arrangement.subtitle);
    }
    public String getComposer() {
        return arrangement==null ? "" : arrangement.getAttributeValue(Arrangement.composer)==null ? "" : arrangement.getAttributeValue(Arrangement.composer);
    }

    public List<CwnTrack> deleteSelection() {
        return arrangement.deleteElements(arrangement.getSelection().getSelection());
    }

    public List<CwnTrack> cutSelection() {
        return arrangement.cut();
    }

    public void copySelection() {
        arrangement.copy(factory);
    }

    public void pasteSelection(Modifier<Event> modifier) {
        arrangement.paste(factory, modifier);
    }

    public void save(OutputStream outputStream) {
        try {
            XMLElementWriter w = new XMLElementWriter();
            w.run(arrangement, outputStream);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void open(InputStream inputStream) {
        XmlElementReader reader = new XmlElementReader(factory, Arrangement.TYPE);
        Arrangement root = (Arrangement) reader.run(inputStream);
        if (root!=null) {
            arrangement = root;
        }
    }

    public String output() {
        StringBuffer buf = new StringBuffer();
        String SEP = System.lineSeparator();
        if (arrangement != null) {
            buf.append("Arrangement: " + arrangement.getId() + SEP);
            for (CwnTrack cwnTrack : arrangement.getActiveMidiTrackList()) {
                MidiTrack track = (MidiTrack) cwnTrack;
                buf.append("# Track " + track.getName() + SEP);
                for (Event event : track.getChildrenByClass(Event.class)) {
                    Trias trias = PositionTools.getTrias(track, event.getPosition());
                    buf.append("  " + trias + ": " + event.getType() + ", " + event.getId() + SEP);
                }
            }
            buf.append(SEP);
            if (!getClipboard().getElements().isEmpty()) {
                buf.append("Clipboard:" + SEP);
                for (Event event : getClipboard().getElements()) {
                    buf.append("- " + event + SEP);
                }
                buf.append(SEP);
            }
        }
        return buf.toString();
    }

    public String journal() {
        StringBuffer buf = new StringBuffer();
        String SEP = System.lineSeparator();
        if (arrangement != null) {
            buf.append("Journal:" + SEP);
            History history = arrangement.getHistory();
            for (Operation op : history.getFuture()) {
                buf.append("+ " + op.info() + SEP);
                if (op instanceof Transaction) {
                    for (Operation subOp : (Transaction) op) {
                        buf.append("  " + subOp.info() + SEP);
                    }
                }
            }
            for (Operation op : history.getHistory()) {
                buf.append("- " + op.info() + SEP);
                if (op instanceof Transaction) {
                    for (Operation subOp : (Transaction) op) {
                        buf.append("  " + subOp.info() + SEP);
                    }
                }
            }
        } else {
            buf.append("no arrangement found!" + SEP);
        }
        return buf.toString();
    }

    public Arrangement getArrangement() {
        return arrangement;
    }

    public void setArrangement(Arrangement arrangement) {
        this.arrangement = arrangement;
        arrangement.setSelection(new AmbitusSelection());
        scoreBuilder.setContainer(arrangement);
        this.setFile(null);
    }

    private ScoreParameter createScoreParameter(Arrangement arrangement) {
        List<DurationType> durationTypeList = new ArrayList<>();
        durationTypeList.add(DurationType.REGULAR);
        durationTypeList.add(DurationType.DOTTED);
        durationTypeList.add(DurationType.BIDOTTED);
        durationTypeList.add(DurationType.TRIDOTTED);
        durationTypeList.add(DurationType.TRIPLET);
        durationTypeList.add(DurationType.QUINTUPLET);
        ScoreParameter scoreParameter = new ScoreParameter(
                getPPQ(),
                arrangement.getResolutionInTicks(),
                arrangement.getAttributeValue(Arrangement.groupLevel),
                arrangement.getStretchFactor(),
                Score.ALLOW_DOTTED_RESTS | Score.SPLIT_RESTS | Score.MERGE_RESTS_IN_EMPTY_BARS,
                durationTypeList,
                new ArrayList<Markup.Type>(markup), 0, arrangement.getCaret());
        return scoreParameter;
    }

    public void updateScoreParameter() {
        if (arrangement != null) {
            getScoreParameter().setFlags(arrangement.getFlags());
            int stretchFactor = arrangement.getStretchFactor();
            int groupLevel = arrangement.getAttributeValue(Arrangement.groupLevel);
            getScoreParameter().setDisplayStretchFactor(stretchFactor);
            getScoreParameter().setMetricLevel(groupLevel);
            getScoreParameter().setResolutionInTicks(arrangement.getResolutionInTicks());
            getScoreParameter().setPPQ(arrangement.getPPQ());
            getScoreParameter().setFilename(this.file == null ? "" : this.file.getName());
            boolean bidotted = arrangement.getAttributeValue(Arrangement.durationBiDotted);
            if (bidotted) {
                if (!getScoreParameter().durationTypeList.contains(DurationType.BIDOTTED)) {
                    getScoreParameter().durationTypeList.add(DurationType.BIDOTTED);
                }
            } else {
                if (getScoreParameter().durationTypeList.contains(DurationType.BIDOTTED)) {
                    getScoreParameter().durationTypeList.remove(DurationType.BIDOTTED);
                }
            }
        }
    }

    public int getPlayTempo() {
        return playTempo;
    }

    public void setPlayTempo(int playTempo) {
        this.playTempo = playTempo;
    }

    public int getPlayStrength() {
        return playStrength;
    }

    public void setPlayStrength(int playStrength) {
        this.playStrength = playStrength;
    }

    public int getPlayExpose() {
        return playExpose;
    }

    public void setPlayExpose(int playExpose) {
        this.playExpose = playExpose;
    }

}
