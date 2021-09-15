package org.wuerthner.ambitusdesktop.midi;


import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;


public class MidiDeviceHandler implements DeviceHandler {
    private Info[] infoArray;
    private Sequencer sequencer = null;
    private MidiDevice inputDevice = null;
    private MidiDevice synthesizer = null; // outputDevice!
    private Track metronomeTrack = null;
    private Track recordTrack = null;

    private void dumpInfo() {
        System.out.println("-----------------------------");
        System.out.println("Sequencer:                  " + (sequencer == null ? "-" : sequencer.getDeviceInfo().getName()));
        for (Transmitter t : sequencer.getTransmitters()) {
            System.out.println("* " + t.toString() + " - " + t.getReceiver().toString());
        }
        for (Receiver r : sequencer.getReceivers()) {
            System.out.println("+ " + r.toString());
        }
        System.out.println("Input:                      " + (inputDevice == null ? "-" : inputDevice.getDeviceInfo().getName()));
        System.out.println("Synthesizer (outputDevice): " + (synthesizer == null ? "-" : synthesizer.getDeviceInfo().getName()));
        System.out.println("RecordTrack:                " + recordTrack);
        if (synthesizer instanceof Synthesizer) {
            System.out.println("Latency: " + ((Synthesizer) synthesizer).getLatency());
        }
        System.out.println("-----------------------------");
    }

    public MidiDeviceHandler() {
        update();
        if (getInputDevices().length > 0) {
            setInputDevice(getInputDevices()[0]);
        }
        if (getOutputDevices().length > 0) {
            setOutputDevice(getOutputDevices()[0]);
        }
    }

    public void update() {
        System.out.println("update midi devices...");
        infoArray = MidiSystem.getMidiDeviceInfo();
    }

    @Override
    public boolean isRunning() {
        return sequencer != null && sequencer.isRunning();
    }

    @Override
    public void start(long start, long end, int tempo, Sequence sequence, int sleep, boolean record) {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.addMetaEventListener(new ServiceMetaEventListener());

            sequencer.open();
            if (record) {
                inputDevice.open();
                inputDevice.getTransmitter().setReceiver(sequencer.getReceiver());
                metronomeTrack = Metronome.createTrack(sequence);
                recordTrack = sequence.createTrack();
            }
            sequencer.setSequence(sequence);
            sequencer.setTickPosition(start);
            // sequencer.setTempoInBPM(tempo);

            if (synthesizer != null) {
                if (!synthesizer.isOpen())
                    synthesizer.open();
                sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());
            }
            if (record) {
                sequencer.recordDisable(metronomeTrack);
                sequencer.recordEnable(recordTrack, -1);
            }
            dumpInfo();
            Thread.sleep(sleep);
            if (record) {
                sequencer.startRecording();
            } else {
                sequencer.start();
            }
        } catch (MidiUnavailableException | InvalidMidiDataException | InterruptedException e) {
            e.printStackTrace();
            if (inputDevice != null) {
                inputDevice.close();
            }
        }
    }

    /**
     * Stops playing (or recording) a sequence, stops the presentations moving cursors.
     **/
    @Override
    public void stop() {
        if (sequencer != null) {
            try {
                if (sequencer.isRecording()) {
                    sequencer.stopRecording();
                    Sequence sequence = sequencer.getSequence();
                    sequence.deleteTrack(metronomeTrack);
                    // midiExtractor.extractInputSequence(this, sequence);
                }
                if (sequencer.isRunning()) {
                    sequencer.stop();
                    // sequencer.stopRecording();
                }
                if (sequencer.isOpen()) {
                    sequencer.close();
                }
                if (inputDevice != null && inputDevice.isOpen()) {
                    inputDevice.getTransmitter().close();
                    inputDevice.close();
                }
                if (synthesizer != null && synthesizer.isOpen()) {
                    synthesizer.close();
                    try {
                        synthesizer.getReceiver().close();
                    } catch (MidiUnavailableException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sequencer = null;
        }
    }

    @Override
    public void playPitch(int pitch, int velocity, int channel, int duration) {
        try {
            if (!synthesizer.isOpen()) {
                synthesizer.open();
            }
            // _synthesizer.getReceiver().send(new SingleMessage(SingleMessage.PROGRAM_CHANGE, channel, prg), 0);
            ShortMessage msg;
            msg = new ShortMessage();
            msg.setMessage(ShortMessage.NOTE_ON, channel, pitch, velocity);
            synthesizer.getReceiver().send(msg, 0);
            msg = new ShortMessage();
            msg.setMessage(ShortMessage.NOTE_OFF, channel, pitch, velocity);
            Thread.sleep(150);
            synthesizer.getReceiver().send(msg, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getPosition() {
        return sequencer == null ? 0 : sequencer.getTickPosition();
    }

    @Override
    public Track getRecordTrack() {
        return recordTrack;
    }

    @Override
    public final String[] getOutputDevices() {
        return getDevices(false);
    }

    @Override
    public final String[] getInputDevices() {
        return getDevices(true);
    }

    public boolean isRecording() {
        return recordTrack != null;
    }

    @Override
    public void setInputDevice(String name) {
        boolean done = false;
        try {
            for (Info info : infoArray) {
                if (info.getName().equals(name)) {
                    boolean allowsInput = (MidiSystem.getMidiDevice(info).getMaxTransmitters() != 0);
                    if (allowsInput) {
                        inputDevice = MidiSystem.getMidiDevice(info);
                        done = true;
                    }
                }
            }
            if (!done) {
                throw new MidiUnavailableException();
            }
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setOutputDevice(String name) {
        boolean done = false;
        try {
            for (Info info : infoArray) {
                if (info.getName().equals(name)) {
                    boolean allowsOutput = (MidiSystem.getMidiDevice(info).getMaxReceivers() != 0);
                    if (allowsOutput) {
                        synthesizer = MidiSystem.getMidiDevice(info);
                        done = true;
                    }
                }
            }
            if (!done) {
                throw new MidiUnavailableException();
            }
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private final String[] getDevices(boolean input) {
        List<String> deviceList = new ArrayList<String>();
        for (int i = 0; i < infoArray.length; i++)
            try {
                MidiDevice device = MidiSystem.getMidiDevice(infoArray[i]);
                boolean allowsInput = (device.getMaxTransmitters() != 0);
                boolean allowsOutput = (device.getMaxReceivers() != 0);
                if ((allowsOutput && !input) || (allowsInput && input))
                    deviceList.add(infoArray[i].getName());
            } catch (MidiUnavailableException mue) {
                System.err.println(mue);
            }
        return (String[]) deviceList.toArray(new String[] {});
    }

    public void dump() {
        for (int i = 0; i < infoArray.length; i++) {
            try {
                System.out.println(i + ": " + infoArray[i].getName() + " - " + infoArray[i].getDescription() + ", rec: " + MidiSystem.getMidiDevice(infoArray[i]).getMaxReceivers() + ", trans: "
                        + MidiSystem.getMidiDevice(infoArray[i]).getMaxTransmitters());
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    public class ServiceMetaEventListener implements MetaEventListener {

        @Override
        public void meta(MetaMessage event) {
            if (event.getType() == 47) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stop();
                // TODO: stop?
                // Registry registry = BaseRegistry.getInstance();
                // registry.getActiveDocument().getHandler().perform(Operation.NO_OPERATION);
            }
        }
    }
}