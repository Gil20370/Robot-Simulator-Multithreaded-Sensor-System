package bgu.spl.mics.application.objects;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;
    private static StatisticalFolder singleton = new StatisticalFolder();

    public synchronized static StatisticalFolder getInstance() {
		if(singleton == null){
			singleton = new StatisticalFolder();
		}
		return singleton;
	}
    
    private StatisticalFolder() {
        this.systemRuntime = 0;
        this.numTrackedObjects = 0;
        this.numLandmarks = 0;
        this.numDetectedObjects = 0;
    }
    public synchronized int getSystemRuntime() {
        return systemRuntime;
    }
    public synchronized void setSystemRuntime(int systemRuntime) {
        this.systemRuntime = systemRuntime;
    }
    public synchronized int getNumDetectedObjects() {
        return numDetectedObjects;
    }
    public synchronized void addNumDetectedObjects(int numDetectedObjects) {
        this.numDetectedObjects = this.numDetectedObjects+numDetectedObjects;
    }
    public synchronized int getNumTrackedObjects() {
        return numTrackedObjects;
    }
    public synchronized void addNumTrackedObjects(int numTrackedObjects) {
        this.numTrackedObjects = this.numTrackedObjects+numTrackedObjects;
    }
    public synchronized int getNumLandmarks() {
        return numLandmarks;
    }
    public synchronized void incNumLandmarks() {
        this.numLandmarks++;
    }
}
