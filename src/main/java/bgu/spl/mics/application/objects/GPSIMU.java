package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.PoseEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    private int currentTick;
    private STATUS status;
    private ArrayList<Pose> poseList;
    private int lastIndex;

    public GPSIMU(int currentTick, ArrayList<Pose> poseList) {
        this.currentTick = currentTick;
        this.status = STATUS.UP;
        this.poseList = poseList;
        this.lastIndex = 0;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public ArrayList<Pose> getPoseList() {
        return poseList;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public Pose getCurrentPose(int tick) {
        setCurrentTick(tick);
        Pose currPose = null;
        for (; lastIndex < poseList.size() && poseList.get(lastIndex).getTime() <= tick; lastIndex++) {
            if (poseList.get(lastIndex).getTime() == tick) {
                currPose= poseList.get(lastIndex);
            }
        }
        if(lastIndex==poseList.size())
            setStatus(STATUS.DOWN);
        return currPose;

    }
    public void getPosesUpToTime(int errorTime) {
        ArrayList<Pose> posesUpToErrorTime = new ArrayList<>();
        for (Pose pose : poseList) {
            if (pose.getTime() <= errorTime)
                posesUpToErrorTime.add(pose);
            else
                break;
        }
        ErrorGenerator.getInstance().setLastPoses(posesUpToErrorTime);
    }
}