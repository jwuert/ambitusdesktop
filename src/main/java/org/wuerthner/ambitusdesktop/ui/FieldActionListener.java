package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.attribute.PositionAttribute;
import org.wuerthner.ambitus.model.Event;
import org.wuerthner.ambitus.model.NoteEvent;
import org.wuerthner.ambitusdesktop.ScoreModel;
import org.wuerthner.ambitusdesktop.ScoreUpdater;
import org.wuerthner.cwn.api.CwnTrack;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.ScoreUpdate;
import org.wuerthner.sport.api.Attribute;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FieldActionListener<T> implements ActionListener {
    private final JTextField field;
    private final Attribute<T> attribute;
    private final ScoreModel scoreModel;
    private final ScoreUpdater scoreUpdater;

    public FieldActionListener(JTextField field, Attribute<T> attribute, ScoreModel scoreModel, ScoreUpdater scoreUpdater) {
        this.field = field;
        this.attribute = attribute;
        this.scoreModel = scoreModel;
        this.scoreUpdater = scoreUpdater;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (scoreModel.getSelection().hasSingleSelection()) {
            CwnTrack track = scoreModel.getTrackList().get(0);
            Event event = scoreModel.getSelection().getSingleSelection();
            String value = field.getText();
            if (attribute== NoteEvent.position) {
                long position = PositionTools.getPosition(track, value);
                scoreModel.getArrangement().setEventAttribute(event, (PositionAttribute) attribute, position);
            } else {
                scoreModel.getArrangement().setEventAttribute(event, attribute, attribute.getValue(value));
            }
            scoreUpdater.update(new ScoreUpdate(track, scoreModel.getSelection()).extendRangeByOneBar());
        }
        field.getParent().getParent().requestFocus();
    }
}
