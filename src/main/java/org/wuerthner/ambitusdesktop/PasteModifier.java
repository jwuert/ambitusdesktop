package org.wuerthner.ambitusdesktop;

import org.wuerthner.ambitus.model.Event;
import org.wuerthner.sport.api.Modifier;

public class PasteModifier implements Modifier<Event> {
    private final long deltaPosition;

    public PasteModifier(long deltaPosition) {
        this.deltaPosition = deltaPosition;
    }

    @Override
    public void modify(Event event) {
        event.performTransientSetAttributeValueOperation(Event.position, event.getPosition()+deltaPosition);
    }
}
