package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast
{

    private int errorTime;


    public CrashedBroadcast(int errorTime){
        this.errorTime=errorTime;
    }

    public int getErrorTime() {
        return errorTime;
    }


}
