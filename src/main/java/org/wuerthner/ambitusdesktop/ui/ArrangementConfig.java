package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.template.Template;
import org.wuerthner.ambitusdesktop.ScoreModel;
import org.wuerthner.ambitusdesktop.ScorePanel;


import java.awt.*;

public class ArrangementConfig {
    public ArrangementConfig(ScoreModel scoreModel, Component content) {
        Arrangement arr = scoreModel.getArrangement();
        ParameterDialog pd = new ParameterDialog(new String[]{"Arrangement Configuration"},
                new String[]{"Title", "Subtitle", "Composer", "PPQ", "Group Level", "Resolution",
                        "Spacing", "Allow Dotted Rests", "Merge Rests in Empty Bars", "Double Dotted Notes"},
                new Object[]{
                        arr.getAttributeValue(Arrangement.name, ""),
                        arr.getAttributeValue(Arrangement.subtitle, ""),
                        arr.getAttributeValue(Arrangement.composer, ""),
                        ""+arr.getAttributeValue(Arrangement.pulsePerQuarter, Arrangement.DEFAULT_PPQ),
                        ParameterDialog.makeCombo(Arrangement.LEVELS, arr.getAttributeValue(Arrangement.groupLevel)),
                        ParameterDialog.makeCombo(Arrangement.GRIDS, arr.getAttributeValue(Arrangement.resolution)),
                        // ParameterDialog.makeCombo(Arrangement.TUPLET_PRESENTATION, arr.getAttributeValue(Arrangement.tupletPresentation)),
                        ParameterDialog.makeCombo(Arrangement.STRETCH_FACTORS, arr.getAttributeValue(Arrangement.stretchFactor)),
                        arr.getAttributeValue(Arrangement.flagAllowDottedRests),
                        arr.getAttributeValue(Arrangement.flagMergeRestsInEmptyBars),
                        arr.getAttributeValue(Arrangement.durationBiDotted)
                },
                content);
        String[] parameters = pd.getParameters();
        if (parameters!=null) {
            String title = parameters[0];
            String subtitle = parameters[1];
            String composer = parameters[2];
            Integer ppq = Integer.valueOf(parameters[3]);
            int level = ParameterDialog.get(Arrangement.LEVELS, parameters[4]);
            int resolution = ParameterDialog.get(Arrangement.GRIDS, parameters[5]);
            int stretchFac = ParameterDialog.get(Arrangement.STRETCH_FACTORS, parameters[6]);
            boolean dottedRests = Boolean.valueOf(parameters[7]);
            boolean mergeRests = Boolean.valueOf(parameters[8]);
            boolean biDotted = Boolean.valueOf(parameters[9]);
            arr.setConfiguration(title, subtitle, composer, ppq, level, resolution, stretchFac, dottedRests, mergeRests, biDotted);
            scoreModel.updateScoreParameter();
        }
    }
}
