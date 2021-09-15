package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.template.SingleViolin;
import org.wuerthner.ambitus.template.Template;
import org.wuerthner.ambitusdesktop.ScoreModel;
import org.wuerthner.ambitusdesktop.ScorePanel;


public class Wizard {
    public Wizard(ScoreModel scoreModel, ScorePanel content) {
        System.out.println("Wizard!");

        ParameterDialog pd = new ParameterDialog(new String[]{"Sheet Music Wizard"},
                new String[]{"Title", "Subtitle", "Composer", "Tempo", "Key", "Template"},
                new Object[]{"Untitled", "", "", "120",
                        ParameterDialog.makeCombo(Arrangement.KEYS, Arrangement.DEFAULT_KEY),
                        ParameterDialog.makeCombo(Template.TEMPLATES, 0) },
                content);
        String[] parameters = pd.getParameters();
        if (parameters!=null) {
            String title = parameters[0];
            String subtitle = parameters[1];
            String composer = parameters[2];
            Integer tempo = Integer.valueOf(parameters[3]);
            int keySelection = ParameterDialog.get(Arrangement.KEYS, parameters[4]);
            int templateSelection = ParameterDialog.get(Template.TEMPLATES, parameters[5]);
            if (keySelection >= 0 && templateSelection >= 0) {
                Template template = Template.createTemplate(templateSelection);
                scoreModel.newArrangement(title, subtitle, composer, tempo, keySelection, templateSelection);
            }
        }
    }
}
