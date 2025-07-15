package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;

public class TerminatedBroadcast implements Broadcast {

    private Class<? extends MicroService> microServiceClass;

    public TerminatedBroadcast(Class<? extends MicroService> microServiceClass) {
        System.out.println("The service sending the broadcast will terminate" +microServiceClass);
        this.microServiceClass = microServiceClass;


    }

    public Class<? extends MicroService> getMicroServiceClass() {
        return microServiceClass;
    }
}
