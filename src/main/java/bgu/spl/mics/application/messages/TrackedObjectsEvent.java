package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.ArrayList;
import java.util.LinkedList;

public class TrackedObjectsEvent implements Event<Boolean> {

    private ArrayList<TrackedObject> trackedObjects;

    public TrackedObjectsEvent(ArrayList<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }

    public ArrayList<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }


}
