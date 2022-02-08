package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.model.MidiTrack;
import org.wuerthner.ambitus.template.Template;
import org.wuerthner.ambitusdesktop.ScoreModel;
import org.wuerthner.ambitusdesktop.ScorePanel;
import org.wuerthner.ambitusdesktop.ui.ParameterDialog;
import org.wuerthner.cwn.api.CwnBarEvent;
import org.wuerthner.cwn.score.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BarConfig {
    public BarConfig(ScoreModel scoreModel, ScorePanel content, Location location) {
        List<String> keys = new ArrayList<>();
        keys.addAll(Arrays.asList(Arrangement.KEYS));
        keys.add(Arrangement.KEYS[scoreModel.getKey(location.staffIndex, location.position)+7]);
        List<String> clefs = new ArrayList<>();
        clefs.addAll(Arrays.asList(Arrangement.CLEFS));
        clefs.add(Arrangement.CLEFS[scoreModel.getClef(location.staffIndex, location.position)]);
        String metric = scoreModel.getMetric(location.staffIndex, location.position);
        String tempo = "" + scoreModel.getTempo(0, location.position);
        int bar = scoreModel.getBarIndex(location.staffIndex, location.position);
        if (location.position==0) {
            //
            // Track Configuration
            //
            String name = ((MidiTrack)scoreModel.getArrangement().getTrackList().get(location.staffIndex)).getAttributeValue(MidiTrack.name);
            boolean mute = scoreModel.getMute(location.staffIndex);
            List<String> volumes = new ArrayList<>();
            volumes.addAll(Arrays.asList(MidiTrack.VOLUMES));
            int volume = scoreModel.getVolume(location.staffIndex);
            if (volume>10) { volume = 10; }
            volumes.add(MidiTrack.VOLUMES[volume]);
            // String volume = "" + scoreModel.getVolume(location.staffIndex);
            List<String> channels = new ArrayList<>();
            channels.addAll(Arrays.asList(MidiTrack.CHANNELS));
            channels.add(MidiTrack.CHANNELS[scoreModel.getChannel(location.staffIndex)]);
            List<String> instruments = new ArrayList<>();
            instruments.addAll(Arrays.asList(MidiTrack.MIDI_INSTRUMENTS));
            instruments.add(MidiTrack.MIDI_INSTRUMENTS[scoreModel.getInstrument(location.staffIndex)]);
            ParameterDialog pd = new ParameterDialog(new String[]{"Bar Configuration"},
                    new String[]{"Name", "Mute", "Volume", "Channel", "Instrument", "Metric", "Tempo", "Key", "Clef"},
                    new Object[]{
                            name,
                            mute,
                            volumes.toArray(new String[]{}),
                            channels.toArray(new String[]{}),
                            instruments.toArray(new String[]{}),
                            metric,
                            tempo,
                            keys.toArray(new String[]{}),
                            clefs.toArray(new String[]{}),
                            ParameterDialog.makeCombo(CwnBarEvent.TYPES, 0)},
                    content);
            String[] parameters = pd.getParameters();
            if (parameters != null) {
                String newName = parameters[0];
                boolean newMute = (Boolean.valueOf(parameters[1]));
                int newVolume = Arrays.asList(MidiTrack.VOLUMES).indexOf(parameters[2]);
                int channelSelection = Arrays.asList(MidiTrack.CHANNELS).indexOf(parameters[3]);
                int instrumentSelection = Arrays.asList(MidiTrack.MIDI_INSTRUMENTS).indexOf(parameters[4]);
                String newMetric = parameters[5];
                Integer newTempo = Integer.valueOf(parameters[6]);
                int keySelection = Arrays.asList(Arrangement.KEYS).indexOf(parameters[7]);
                int clefSelection = Arrays.asList(Arrangement.CLEFS).indexOf(parameters[8]);
                String trackId = ((MidiTrack) scoreModel.getTrackList().get(location.staffIndex)).getId();
                scoreModel.setTrackProperties(trackId, newMute, newVolume, newName, newMetric, keySelection, clefSelection, newTempo, instrumentSelection, channelSelection);
            }
        } else {
            //
            // Bar Configuration
            //
            ParameterDialog pd = new ParameterDialog(new String[]{"Bar Configuration"},
                    new String[]{"Metric", "Tempo", "Key", "Clef", "Bar (end)"},
                    new Object[]{
                            metric,
                            tempo,
                            keys.toArray(new String[]{}),
                            clefs.toArray(new String[]{}),
                            ParameterDialog.makeCombo(CwnBarEvent.TYPES, 0)},
                    content);
            String[] parameters = pd.getParameters();
            if (parameters != null) {
                String newMetric = parameters[0];
                Integer newTempo = Integer.valueOf(parameters[1]);
                int keySelection = Arrays.asList(Arrangement.KEYS).indexOf(parameters[2]);
                int clefSelection = Arrays.asList(Arrangement.CLEFS).indexOf(parameters[3]);
                int barSelection = ParameterDialog.get(CwnBarEvent.TYPES, parameters[4]);
                String trackId = ((MidiTrack) scoreModel.getTrackList().get(location.staffIndex)).getId();
                scoreModel.setBarProperties(trackId, location.position, newMetric, keySelection, clefSelection, barSelection, newTempo);
            }
        }
    }
}
