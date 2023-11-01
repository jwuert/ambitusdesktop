package org.wuerthner.ambitusdesktop.service;

import org.wuerthner.ambitus.model.*;
import org.wuerthner.ambitusdesktop.score.AmbitusSelection;
import org.wuerthner.cwn.api.CwnTrack;
import org.wuerthner.cwn.api.Trias;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.sample.SampleTimeSignatureEvent;
import org.wuerthner.cwn.sample.SampleTrack;
import org.wuerthner.cwn.timesignature.SimpleTimeSignature;
import org.wuerthner.sport.api.ModelElement;

import javax.sound.midi.*;
import java.util.List;

public class SequenceService {
    public Sequence createSequence(Arrangement arrangement, long endPosition, AmbitusSelection selection, int exposedTrack, int exposedStrength, int tempo) {
        Sequence sequence = null;
        long startPosition = 0;
        boolean playAll = (endPosition == 0);
        boolean hasExposedTracks = exposedTrack >= 0;
        double exposeFactor = 1.0 + 0.2 * exposedStrength;
        double tempoFactor = arrangement.getTempo(0) * 0.01 * tempo * 0.01;
        try {
            sequence = new Sequence(Sequence.PPQ, arrangement.getPPQ());
            Track tempoTrack = sequence.createTrack();
            List<CwnTrack> trackList = arrangement.getTrackList();
            startPosition = PositionTools.getPosition(arrangement.getFirstActiveMidiTrack().get(), new Trias(arrangement.getBarOffset(),0,0));
            for (int trackNo = 0; trackNo < trackList.size(); trackNo++) {
                if (trackList.get(trackNo) instanceof MidiTrack) {
                    MidiTrack midiTrack = (MidiTrack) trackList.get(trackNo);
                    double volumeWeight = (hasExposedTracks ? trackNo == exposedTrack ? exposeFactor : 1.0 / exposeFactor : 1.0);
                    double volume = midiTrack.getVolume() * 0.1;
                    int program = midiTrack.getInstrument();
                    int channel = midiTrack.getChannel();
                    boolean mute = midiTrack.getMute();
                    if (!mute) {
                        Track track = sequence.createTrack();
                        track.add(createInstrumentEvent(program, channel));
                        for (Event event : midiTrack.getList(Event.class)) {
                            long end = event.getPosition() + event.getDuration() - 1;
                            if (endPosition == 0 || end <= endPosition) {
                                if (event instanceof TempoEvent) {
                                    TempoEvent tempoEvent = TempoEvent.class.cast(event);
                                    tempoTrack.add(createTempoEvent(tempoEvent.getPosition(), (int) (tempoEvent.getTempo() * tempoFactor)));
                                } else if (event instanceof NoteEvent) {
                                    NoteEvent noteEvent = NoteEvent.class.cast(event);
                                    long start = noteEvent.getPosition();
                                    // long end = noteEvent.getEnd() - 1;
                                    if (playAll || (start >= startPosition && end <= endPosition)) {
                                        int pitch = noteEvent.getPitch();
                                        int velocity = (int) (noteEvent.getVelocity() * volumeWeight * volume);
                                        if (velocity > 127) {
                                            velocity = 127;
                                        }
                                        track.add(createNoteOnEvent(pitch, start, velocity, channel));
                                        track.add(createNoteOffEvent(pitch, end, channel));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (InvalidMidiDataException imde) {
            System.err.println(imde);
        }
        return sequence;
    }

    public void dumpSequence(Sequence sequence, int ppq) {
        Track[] tracks = sequence.getTracks();
        int i = 0;
        CwnTrack defaultTrack = new SampleTrack(ppq);
        defaultTrack.addEvent(new SampleTimeSignatureEvent(0, new SimpleTimeSignature("4/4")));
        for (Track track : tracks) {
            System.out.println("Track " + i++ + ": " + track.ticks());
            for (int k = 0; k < track.size(); k++) {
                MidiEvent event = track.get(k);
                PositionTools.getTrias(defaultTrack, event.getTick());
                System.out.println(" Event: " + event.getTick() + ", " + formatEventMessage(event));
            }
        }
    }

    private String formatEventMessage(MidiEvent event) {
        MidiMessage message = event.getMessage();
        byte[] arr = message.getMessage();
        String value = "";
        for (byte b : arr) {
            int i = (int) b;
            if (i < 0) {
                i += 256;
            }
            value += i + " ";
        }
        return value;
    }

    private MidiEvent createNoteOnEvent(int nKey, long lTick, int velocity, int channel) {
        return createNoteEvent(ShortMessage.NOTE_ON, nKey, velocity, lTick, channel);
    }

    private MidiEvent createNoteOffEvent(int nKey, long lTick, int channel) {
        return createNoteEvent(ShortMessage.NOTE_OFF, nKey, 0, lTick, channel);
    }

    private MidiEvent createNoteEvent(int nCommand, int nKey, int nVelocity, long lTick, int channel) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(nCommand, channel, nKey, nVelocity);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        MidiEvent event = new MidiEvent(message, lTick);
        return event;
    }

    private MidiEvent createTempoEvent(long lTick, int tempo) {
        MetaMessage message = new MetaMessage();
        if (tempo==0) { tempo = 100; }
        int mpq = (int) (60000000 / tempo);
        try {
            message.setMessage(0x51, new byte[] { (byte) (mpq >> 16 & 0xff), (byte) (mpq >> 8 & 0xff), (byte) (mpq & 0xff) }, 3);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        MidiEvent event = new MidiEvent(message, lTick);
        return event;
    }

    private MidiEvent createInstrumentEvent(int program, int channel) {
        ShortMessage instrumentChange = new ShortMessage();
        try {
            instrumentChange.setMessage(ShortMessage.PROGRAM_CHANGE, channel, program, 0);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        // MidiEvent instrumentChange = new
        // MidiEvent(ShortMessage.PROGRAM_CHANGE,drumPatch.getBank(),drumPatch.getProgram());
        return new MidiEvent(instrumentChange, 0);
    }
}
