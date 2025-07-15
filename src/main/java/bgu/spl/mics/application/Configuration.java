package bgu.spl.mics.application;

import java.util.List;

class Configuration {
    CamerasConfig Cameras;
    LidarConfig LidarWorkers;
    String poseJsonFile;
    int TickTime;
    int Duration;

    static class CamerasConfig {
        List<CameraConfig> CamerasConfigurations;
        String camera_datas_path;
    }

    static class CameraConfig {
        int id;
        int frequency;
        String camera_key;
    }

    static class LidarConfig {
        List<LidarConfigEntry> LidarConfigurations;
        String lidars_data_path;
    }

    static class LidarConfigEntry {
        int id;
        int frequency;
    }
}