package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.model.NoteEvent;
import org.wuerthner.ambitus.model.SymbolEvent;
import org.wuerthner.ambitusdesktop.ScoreModel;
import org.wuerthner.ambitusdesktop.ScoreUpdater;
import org.wuerthner.cwn.api.CwnNoteEvent;
import org.wuerthner.cwn.api.CwnTrack;
import org.wuerthner.cwn.api.Trias;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.Score;
import org.wuerthner.cwn.score.ScoreUpdate;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AttributeEditorSymbol {
    private final SymbolEvent event;
    private final ScoreModel scoreModel;
    private final ScoreUpdater scoreUpdater;

    public AttributeEditorSymbol(SymbolEvent selectedEvent, ScoreModel scoreModel, ScoreUpdater scoreUpdater) {
        this.event = selectedEvent;
        this.scoreModel = scoreModel;
        this.scoreUpdater = scoreUpdater;
    }

    public void init(JPanel content) {
        CwnTrack track = scoreModel.getTrackList().get(0);
        Trias trias = PositionTools.getTrias(track, event.getPosition());
        String position = trias.toFormattedString();
        String name = event.getSymbolName();

        List<String> voiceList = new ArrayList<>();
        voiceList.add("1");
        voiceList.add("2");
        voiceList.add(""+(event.getVoice()+1));

        ParameterDialog pd = new ParameterDialog(new String[]{name + " Configuration"},
                new String[]{"Position", "Duration", "Offset", "Parameter", "Voice"},
                new Object[]{
                        position,
                        "" + event.getDuration(),
                        "" + event.getVerticalOffset(),
                        "" + event.getParameter(),
                        voiceList.toArray(new String[]{})
                },
                content);
        String[] parameters = pd.getParameters();
        if (parameters != null) {
            long newPosition = PositionTools.getPosition(track, parameters[0]);
            long newDuration = Long.parseLong(parameters[1]);
            int newOffset = Integer.parseInt(parameters[2]);
            int newParameter = Integer.parseInt(parameters[3]);
            int newVoice = Integer.valueOf(parameters[4])-1;

            scoreModel.getArrangement().setSymbolEventAttributes(event, newPosition, newDuration, newOffset, newParameter, newVoice);

            scoreUpdater.update(new ScoreUpdate(track, scoreModel.getSelection()).extendRangeByOneBar());
        }
    }
}
