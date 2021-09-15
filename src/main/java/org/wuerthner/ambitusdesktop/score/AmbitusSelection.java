package org.wuerthner.ambitusdesktop.score;

import org.wuerthner.ambitus.model.Event;
import org.wuerthner.ambitus.tool.AbstractSelection;
import org.wuerthner.ambitusdesktop.service.MidiService;
import org.wuerthner.cwn.api.CwnEvent;
import org.wuerthner.cwn.api.CwnPointer;
import org.wuerthner.cwn.api.CwnSelection;
import org.wuerthner.cwn.score.Location;
import org.wuerthner.sport.api.ModelElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AmbitusSelection extends AbstractSelection implements CwnSelection<Event> {
    @Override
    public boolean hasCursor() {
        return MidiService.isRunning();
    }

    @Override
    public long getCursorPosition() {
        return MidiService.getPosition();
    }
}
