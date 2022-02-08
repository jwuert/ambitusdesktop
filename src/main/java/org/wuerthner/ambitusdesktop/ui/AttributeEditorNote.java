package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.attribute.PositionAttribute;
import org.wuerthner.ambitus.model.Accent;
import org.wuerthner.ambitus.model.Event;
import org.wuerthner.ambitus.model.MidiTrack;
import org.wuerthner.ambitus.model.NoteEvent;
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
import java.util.stream.Collectors;

public class AttributeEditorNote {
    private final NoteEvent event;
    private final ScoreModel scoreModel;
    private final ScoreUpdater scoreUpdater;

    public AttributeEditorNote(NoteEvent selectedEvent, ScoreModel scoreModel, ScoreUpdater scoreUpdater) {
        this.event = selectedEvent;
        this.scoreModel = scoreModel;
        this.scoreUpdater = scoreUpdater;
    }

    public void init(JPanel content) {
        CwnTrack track = scoreModel.getTrackList().get(0);
        Trias trias = PositionTools.getTrias(track, event.getPosition());
        String position = trias.toFormattedString();
        int ehs = ((CwnNoteEvent) event).getEnharmonicShift();
        List<String> enhList = new ArrayList<>();
        enhList.addAll(Arrays.asList(NoteEvent.SHIFT));
        enhList.add(NoteEvent.SHIFT[ehs+2]);
        List<String> voiceList = new ArrayList<>();
        voiceList.add("1");
        voiceList.add("2");
        voiceList.add(""+(event.getVoice()+1));
//        List<String> accentList = new ArrayList<>();
//        accentList.addAll(event.getAccents().stream().map(ac -> ac.getName()).collect(Collectors.toList()));

        String pitch = Score.getCPitch(((CwnNoteEvent) event).getPitch(), ehs);
        ParameterDialog pd = new ParameterDialog(new String[]{"Note Configuration"},
                new String[]{"Position", "Duration", "Pitch", "Enharmonic Shift", "Velocity", "Voice", "Lyrics" /*, "Accents"*/},
                new Object[]{
                        position,
                        "" + event.getDuration(),
                        pitch,
                        enhList.toArray(new String[]{}), // NoteEvent.SHIFT[ehs+2],
                        "" + event.getVelocity(),
                        voiceList.toArray(new String[]{}),
                        "" + event.getLyrics()
                        // accentList
                },
                content);
        String[] parameters = pd.getParameters();
        if (parameters != null) {
            long newPosition = PositionTools.getPosition(track, parameters[0]);
            long newDuration = Long.valueOf(parameters[1]);
            int newPitch = Score.getPitch(parameters[2]);
            int newPitchEnh = Score.getEnharmonicShift(parameters[2]);
            int newDirectEnh = Arrays.asList(NoteEvent.SHIFT).indexOf(parameters[3]) - 2;
            int newEhs = newDirectEnh == ehs ? newPitchEnh : newDirectEnh;
            int newVelocity = Integer.valueOf(parameters[4]);
            int newVoice = Integer.valueOf(parameters[5])-1;
            String newLyrics = parameters[6];

            scoreModel.getArrangement().setNoteEventAttributes(event, newPosition, newDuration, newPitch, newEhs, newVelocity, newVoice, newLyrics);

            scoreUpdater.update(new ScoreUpdate(track, scoreModel.getSelection()).extendRangeByOneBar());
        }
    }
}
