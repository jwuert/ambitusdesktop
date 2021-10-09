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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScoreModel {
    private static final String DIRECTORY = "Ambitus";
    private static final boolean debug = true;
    private static boolean debugScore = false;

    private NoteSelector noteSelector = NoteSelector.N8;
    private NoteSelector gridSelector = NoteSelector.N8;
    private NoteSelector tupletSelector = NoteSelector.T1;

    private String fileName = null;
    private double zoom = 0; // 0=auto, 1, 2
    private boolean lyrics = false;
    private int numberOfSystems = 9999;

    private Arrangement arrangement = null;
    private ScoreBuilder scoreBuilder;
    private AmbitusScoreLayout scoreLayout;
    private int width = 0;

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
        this.width = width;
        scoreLayout = new AmbitusScoreLayout((int) (width * 1.0 / getZoom()), getPPQ(), false);
        ScoreParameter scoreParameter = createScoreParameter(arrangement);
        scoreBuilder = new ScoreBuilder(arrangement, scoreParameter, scoreLayout, numberOfSystems);
        updateScoreParameter();
    }

    private ScoreParameter createScoreParameter(Arrangement arrangement) {
        List<DurationType> durationTypeList = new ArrayList<>();

        durationTypeList.add(DurationType.REGULAR);
        durationTypeList.add(DurationType.DOTTED);
        ScoreParameter scoreParameter = new ScoreParameter(
                getPPQ(),
                arrangement.getResolutionInTicks(),
                arrangement.getAttributeValue(Arrangement.groupLevel),
                arrangement.getStretchFactor(),
                Score.ALLOW_DOTTED_RESTS | Score.SPLIT_RESTS,
                durationTypeList,
                false, 0);
        return scoreParameter;
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
        zoom++;
        if (zoom==3)
            zoom = 0;
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

    public void setLyrics(boolean b) {
        lyrics = b;
    }

    public boolean lyrics() {
        return lyrics;
    }

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

    public void setTupletSelector(NoteSelector tupletSelector) {
        this.tupletSelector = tupletSelector;
    }

    public NoteSelector getTupletSelector() {
        return this.tupletSelector;
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

    public void addOrSelectNoteEvent(long position, int pitch, int enharmonicShift, int staff) {
        if (arrangement != null) {
            AbstractSelection selection = arrangement.getSelection();
            MidiTrack track = arrangement.getChildrenByClass(MidiTrack.class).get(staff);
            NoteEvent note = track.findFirstEventAtPositionOrNull(position, NoteEvent.class);
            if (note!=null && note.getPitch()==pitch) {
                // SELECT
                selection.set(note, staff, CwnSelection.SelectionType.NOTE);
            } else {
                // CREATE
                long duration = (long) (noteSelector.getNoteLength() * getPPQ() * 4 * tupletSelector.getTupletFactor());
                NoteEvent noteEvent = factory.createElement(NoteEvent.TYPE);
                noteEvent.performTransientSetAttributeValueOperation(NoteEvent.position, position);
                noteEvent.performTransientSetAttributeValueOperation(NoteEvent.duration, duration);
                noteEvent.performTransientSetAttributeValueOperation(NoteEvent.pitch, pitch);
                noteEvent.performTransientSetAttributeValueOperation(NoteEvent.shift, enharmonicShift);
                noteEvent.performTransientSetAttributeValueOperation(NoteEvent.velocity, 87);
                arrangement.addEvent(track, noteEvent);
                selection.unsetMouseFrame();
                int i = arrangement.getChildrenByClass(MidiTrack.class).indexOf(track);
                selection.set(noteEvent, i, CwnSelection.SelectionType.NOTE);
                scoreBuilder.update(new ScoreUpdate(track, selection));
            }
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

    public void setTrackProperties(String trackId, boolean mute, String name, String metric, int key, int clef, int tempo, int instrument, int channel) {
        arrangement.setTrackProperties(trackId, mute, name, metric, key, clef, tempo, instrument, channel);
    }

    public void setBarProperties(String trackId, long barPosition, String metric, int key, int clef, int bar, int tempo) {
        // System.out.println("bar prop. " + trackId + ", " + barPosition + ", " + metric + ", key: " + key + ", clef: " + clef + ", bar: " + bar + ", tempo: " + tempo);
        arrangement.setBarProperties(trackId, barPosition, metric, key, clef, CwnBarEvent.TYPES[bar], tempo, factory);
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

    public int getBarIndex(int trackIndex, long barPosition) {
        return arrangement.getTrackBarBar(trackIndex, barPosition);
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

    public Arrangement createArrangement(String title, String subtitle, String composer, int tempo, int keySelection, int templateSelection) {
        Template template = Template.createTemplate(templateSelection);
        TimeSignature timeSignature = new SimpleTimeSignature("4/4");
        Arrangement arrangement = template.apply(keySelection, tempo, timeSignature, factory);
        arrangement.init(title, subtitle, composer);
        arrangement.setSelection(new AmbitusSelection());
        // updateWidth(width);
        return arrangement;
    }

    public void select(Location fromPosition, Location toPosition) {
        long fromTick = fromPosition.position;
        long toTick = toPosition.position;
        List<ModelElement> eventList = new ArrayList<>();
        int index = 0;
        int minStaffIndex = Math.min(fromPosition.staffIndex, toPosition.staffIndex);
        int maxStaffIndex = Math.max(fromPosition.staffIndex, toPosition.staffIndex);
        List<ModelElement> trackList = arrangement.getChildrenByType(MidiTrack.TYPE);
        for (ModelElement midiTrack : trackList) {
            if (minStaffIndex <= index && index <= maxStaffIndex) {
                for (ModelElement midiEvent : midiTrack.getChildrenByClass(Event.class)) {
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
        selection.set(eventList, staff, CwnSelection.SelectionType.NOTE);
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

    public void output() {
        System.out.println("=========================================");
        if (arrangement != null) {
            System.out.println("Arrangement: " + arrangement.getId());
            for (CwnTrack cwnTrack : arrangement.getActiveMidiTrackList()) {
                MidiTrack track = (MidiTrack) cwnTrack;
                System.out.println("# Track " + track.getName());
                for (Event event : track.getChildrenByClass(Event.class)) {
                    Trias trias = PositionTools.getTrias(track, event.getPosition());
                    System.out.println("  " + trias + ": " + event.getType() + ", " + event.getId());
                }
            }
            System.out.println("-----------------------------------------");
            if (!getClipboard().getElements().isEmpty()) {
                System.out.println("Clipboard:");
                for (Event event : getClipboard().getElements()) {
                    System.out.println("- " + event);
                }
                System.out.println("-----------------------------------------");
            }
            System.out.println("History:");
            History history = arrangement.getHistory();
            for (Operation op : history.getFuture()) {
                System.out.println("+ " + op.info());
            }
            for (Operation op : history.getHistory()) {
                System.out.println("- " + op.info());
            }
        } else {
            System.out.println("no arrangement found!");
        }
        System.out.println("=========================================");
    }

    public Arrangement getArrangement() {
        return arrangement;
    }

    public void setArrangement(Arrangement arrangement) {
        this.arrangement = arrangement;
        arrangement.setSelection(new AmbitusSelection());
        scoreBuilder.setContainer(arrangement);
    }

    public void updateScoreParameter() {
        if (arrangement != null) {
            Boolean t2 = arrangement.getAttributeValue(Arrangement.durationTuplet2);
            Boolean t3 = arrangement.getAttributeValue(Arrangement.durationTuplet3);
            Boolean t4 = arrangement.getAttributeValue(Arrangement.durationTuplet4);
            Boolean t5 = arrangement.getAttributeValue(Arrangement.durationTuplet5);
            Boolean t6 = arrangement.getAttributeValue(Arrangement.durationTuplet6);
            getScoreParameter().setTuplet(t2, t3, t4, t5, t6);
            int stretchFactor = arrangement.getAttributeValue(Arrangement.stretchFactor);
            int groupLevel = arrangement.getAttributeValue(Arrangement.groupLevel);
            getScoreParameter().setDisplayStretchFactor(stretchFactor);
            getScoreParameter().setMetricLevel(groupLevel);
        }
    }
}
