package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;

public class PoseEvent implements Event<Boolean> {
    private Pose pose;

    public PoseEvent(Pose pose) {
        this.pose = pose;
    }
    public Pose getCurrPose() {
        return pose;
    }
}
