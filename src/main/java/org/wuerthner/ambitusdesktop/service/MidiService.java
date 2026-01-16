package org.wuerthner.ambitusdesktop.service;

import org.wuerthner.ambitus.model.*;
import org.wuerthner.ambitusdesktop.midi.MidiDeviceHandler;
import org.wuerthner.ambitusdesktop.score.AmbitusSelection;
import org.wuerthner.cwn.api.CwnEvent;
import org.wuerthner.cwn.api.CwnSelection;
import org.wuerthner.sport.api.ModelElement;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

public class MidiService {
    public static final int initialSleep = 500;
//    private static final SelectionTools selectionTools = new SelectionTools();
    private static MidiDeviceHandler deviceHandler = new MidiDeviceHandler();

    private SequenceService sequenceService = new SequenceService();

    // private final MidiExtractor midiExtractor;


    // public MidiExtractor getMidiExtractor() {
    // return midiExtractor;
    // }

    public void play(Arrangement arrangement, AmbitusSelection selection, boolean fromCaret, boolean selectionOnly, boolean record) {
        play(arrangement, selection, fromCaret, selectionOnly, record, 100, -1, 0);
    }

    public void play(Arrangement arrangement, AmbitusSelection selection, boolean fromCaret, boolean selectionOnly, boolean record, int tempo, int exposedTrack, int exposedStrength) {
        if (!arrangement.getActiveMidiTrackList().isEmpty()) {

            long startPosition = fromCaret ? arrangement.getCaret() : arrangement.getBarOffsetPosition();

            long endPosition = 99999999999999L;
            if (selectionOnly && !selection.getSelection().isEmpty()) {
                startPosition = selection.getSelection().get(0).getPosition();
                endPosition = selection.getSelection().get(selection.getSelection().size()-1).getPosition()
                    +selection.getSelection().get(selection.getSelection().size()-1).getDuration();
            }
            Sequence sequence = sequenceService.createSequence(arrangement, startPosition, endPosition, selection, exposedTrack, exposedStrength, tempo);
            play(arrangement, startPosition, endPosition, sequence, initialSleep, record);
        }
    }

    private synchronized void play(Arrangement arrangement, long start, long end, Sequence sequence, int sleep, boolean record) {

        // if (synthesizer != null) {
        // logger.info("using: " + synthesizer.getDeviceInfo().getName() + " for output");
        // synthesizer.open();
        // sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
        // }

        int tempo = arrangement.getTempo(0);

        // DeviceHandler deviceHandler = BaseRegistry.getInstance().getAttachment(DeviceHandler.class);
        // // deviceHandler.stop();
        // sequenceService.dumpSequence(sequence, arrangement.getPPQ());
        deviceHandler.start(start, end, tempo, sequence, sleep, record);// midiExtractor);

        //
        // Thread.sleep(5000);
        // sequencer.stopRecording();
        // sequencer.close();
        // inputDevice.close();
        // for (Track track : sequence.getTracks()) {
        // System.out.println("> track: " + track.size() + ", " + track.toString() + ", " + recordTrack);
        // if (track == recordTrack) {
        // int trackSize = track.size();
        // for (int i = 0; i < trackSize; i++) {
        // MidiEvent midiEvent = track.get(i);
        // System.out.println(" +++ midiEvent: " + midiEvent.getTick() + ": " + midiEvent.getMessage().getMessage()[0] + ":" + midiEvent.getMessage().getMessage()[1]);
        // }
        // }
        // }
    }

    public static void stop() {
        // DeviceHandler deviceHandler = BaseRegistry.getInstance().getAttachment(DeviceHandler.class);
        deviceHandler.stop();
    }

    public static long getPosition() {
        return deviceHandler.getPosition();
    }

    public void playPitch(NoteEvent noteEvent) {
        // DeviceHandler deviceHandler = BaseRegistry.getInstance().getAttachment(DeviceHandler.class);
        deviceHandler.playPitch(noteEvent.getPitch(), noteEvent.getVelocity(), 0/* TODO */, (int) Math.min(384, noteEvent.getDuration()));
    }

//    public void playPitch_backup(NoteEvent noteEvent) {
//        Sequence sequence = null;
//        int program = 0; // TODO
//        int channel = 0; // TODO
//
//        try {
//            int ppq = arrangement.getPPQ();
//            sequence = new Sequence(Sequence.PPQ, ppq);
//            Track track = ((Sequence) sequence).createTrack();
//            track.add(createInstrumentEvent(program, channel));
//            long start = noteEvent.getPosition();
//            long end = noteEvent.getPosition() + (int) (0.5 * ppq);
//            int pitch = noteEvent.getPitch();
//            int velocity = noteEvent.getVelocity();
//            track.add(createNoteOnEvent(pitch, start, velocity, channel));
//            track.add(createNoteOffEvent(pitch, end, channel));
//            play(start, sequence, 10);
//        } catch (InvalidMidiDataException e) {
//            e.printStackTrace();
//        }
//    }


    public class ServiceControllerEventListener implements ControllerEventListener {

        @Override
        public void controlChange(ShortMessage event) {
            // System.out.println(event);
        }

    }

	/*
	* @formatter:off
	public class MidiExtractor implements Extractor {
		public void extractInputSequence(DeviceHandler midiStatus, Sequence sequence) {
			try {
				MasterInformationProvider masterInformationProvider = arrangement.getHandler().getAttachment(MasterInformationProvider.class);
				int latencyTicks = 230; // TODO: arrangement attribute
				// Sequence sequence = sequencer.getSequence();
				Selection selection = BaseRegistry.getInstance().getSelection();
				MidiTrack midiTrack = null;
				if (selection.hasSingleModelElementSelected() && selection.getSelectedModelElement() instanceof MidiTrack) {
					midiTrack = (MidiTrack) selection.getSelectedModelElement();
					for (Track track : sequence.getTracks()) {
						if (track == midiStatus.getRecordTrack()) {
							int trackSize = track.size();
							for (int i = 0; i < trackSize; i++) {
								MidiEvent midiEvent = track.get(i);
								midiEvent.setTick(midiEvent.getTick() - latencyTicks);
								MidiFileService.handleMidiEvent(track, midiTrack, i, midiEvent, masterInformationProvider);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	* @formatter:on
	*/

//    private static void passThrough(Arrangement arrangement) {
//        System.out.println("Enter Passthrough!");
//        Info[] infos = MidiSystem.getMidiDeviceInfo();
//        for (int i = 0; i < infos.length; i++) {
//            System.out.println(i + ": " + infos[i].getName() + " - " + infos[i].getDescription() + ", " + infos[i].getVendor());
//        }
//
//        try {
//            // Metronome metronome = new Metronome();
//
//            // Open a connection to your input device
//            MidiDevice inputDevice = MidiSystem.getMidiDevice(infos[1]);
//            inputDevice.open();
//
//            // pass through:
//            // Synthesizer synth = MidiSystem.getSynthesizer();
//            // synth.open();
//            // System.out.println("* " + synth.getVoiceStatus().length);
//            // Receiver r = synth.getReceiver();
//            // Transmitter t = inputDevice.getTransmitter();
//            // t.setReceiver(r);
//
//            // Open a connection to the default sequencer (as specified by MidiSystem)
//            Sequencer sequencer = MidiSystem.getSequencer();
//            sequencer.open();
//            // Get the transmitter class from your input device
//            Transmitter transmitter = inputDevice.getTransmitter();
//            // Get the receiver class from your sequencer
//            Receiver receiver = sequencer.getReceiver();
//            // Bind the transmitter to the receiver so the receiver gets input from the transmitter
//            transmitter.setReceiver(receiver);
//
//            // Create a new sequence
//            Sequence seq = new Sequence(Sequence.PPQ, 384);
//            // And of course a track to record the input on
//            Track metronomeTrack = Metronome.createTrack(seq);
//            Track currentTrack = seq.createTrack();
//
//            // Do some sequencer settings
//            sequencer.setSequence(seq);
//            sequencer.setTickPosition(0);
//            sequencer.recordEnable(currentTrack, -1);
//            // And start recording
//            Thread.sleep(300);
//            sequencer.startRecording();
//            // metronome.startMetronome();
//            Thread.sleep(4000);
//
//            sequencer.stopRecording();
//            // metronome.stopMetronome();
//            sequencer.close();
//
//            // synth.close();
//            inputDevice.close();
//
//            // seq.deleteTrack(metronomeTrack);
//            MidiService midiService = new MidiService(arrangement);
//            midiService.play(0, seq, 200, false);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println("Leave Passthrough!");
//    }

    public static boolean isRunning() {
        return deviceHandler.isRunning();
    }
}
