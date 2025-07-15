package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.LinkedList;

public class DetectObjectsEvent implements Event<Boolean> {
    private StampedDetectedObjects stamps;

    public DetectObjectsEvent(StampedDetectedObjects stamps) {
        this.stamps = stamps;
    }
    public StampedDetectedObjects getStamps() {
        return stamps;
    }

}
