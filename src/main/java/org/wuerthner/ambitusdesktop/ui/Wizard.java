package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.template.SingleViolin;
import org.wuerthner.ambitus.template.Template;
import org.wuerthner.ambitusdesktop.ScoreModel;
import org.wuerthner.ambitusdesktop.ScorePanel;

import java.awt.*;


public class Wizard {
    public static Arrangement createArrangement(ScoreModel scoreModel, Component content) {
        ParameterDialog pd = new ParameterDialog(new String[]{"Sheet Music Wizard"},
                new String[]{"Title", "Subtitle", "Composer", "Tempo", "Key", "Time Signature", "Template"},
                new Object[]{"Untitled", "", "", "120",
                        ParameterDialog.makeCombo(Arrangement.KEYS, Arrangement.DEFAULT_KEY),
                        "4/4",
                        ParameterDialog.makeCombo(Template.TEMPLATES, 0) },
                content);
        String[] parameters = pd.getParameters();
        if (parameters!=null) {
            String title = parameters[0];
            String subtitle = parameters[1];
            String composer = parameters[2];
            Integer tempo = Integer.valueOf(parameters[3]);
            int keySelection = ParameterDialog.get(Arrangement.KEYS, parameters[4]);
            String timeSignature = parameters[5];
            int templateSelection = ParameterDialog.get(Template.TEMPLATES, parameters[6]);
            if (keySelection >= 0 && templateSelection >= 0) {
                Template template = Template.createTemplate(templateSelection);
                return scoreModel.createArrangement(title, subtitle, composer, tempo, keySelection, timeSignature, templateSelection);
            }
        }
        return null;
    }
}
