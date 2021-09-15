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
        String tempo = "" + scoreModel.getTempo(location.staffIndex, location.position);
        int bar = scoreModel.getBarIndex(location.staffIndex, location.position);
        ParameterDialog pd = new ParameterDialog(new String[]{"Bar Configuration"},
                new String[]{"Metric", "Tempo", "Key", "Clef", "Bar"},
                new Object[]{metric,
                        tempo,
                        keys.toArray(new String[]{}),
                        clefs.toArray(new String[]{}),
                        ParameterDialog.makeCombo(CwnBarEvent.TYPES, 0)},
                content);
        String[] parameters = pd.getParameters();
        if (parameters!=null) {
            String newMetric = parameters[0];
            Integer newTempo = Integer.valueOf(parameters[1]);
            int keySelection = Arrays.asList(Arrangement.KEYS).indexOf(parameters[2]);
            int clefSelection = Arrays.asList(Arrangement.CLEFS).indexOf(parameters[3]);
            int barSelection = ParameterDialog.get(CwnBarEvent.TYPES, parameters[4]);
            String trackId = ((MidiTrack)scoreModel.getTrackList().get(location.staffIndex)).getId();
            scoreModel.setBarProperties(trackId, location.position, newMetric, keySelection, clefSelection, barSelection, newTempo);
        }
    }
}
