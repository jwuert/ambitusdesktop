package org.wuerthner.ambitusdesktop.midi;

import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

public interface DeviceHandler {

    public boolean isRunning();

    public String[] getInputDevices();

    public String[] getOutputDevices();

    public void setInputDevice(String name);

    public void setOutputDevice(String name);

    public void start(long start, long end, int tempo, Sequence sequence, int sleep, boolean record);

    public void stop();

    public long getPosition();

    public Track getRecordTrack();

    public void playPitch(int pitch, int velocity, int channel, int duration);
}