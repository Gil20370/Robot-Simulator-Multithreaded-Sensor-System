package bgu.spl.mics.application;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static final Object lock = new Object();
    public static int camerasNum;
    public static int LiDarNum;
    public static boolean isFinished=false;



    public static void main(String args[]) throws FileNotFoundException {
        System.out.println(args[0]+"hehe");
        ///if (args.length < 1)
            ///throw new RuntimeException("there isnt a file path");
        String configFilePath = args[0];
        File configFile = new File(configFilePath);
        String outputDirectory = configFile.getParent();

        ArrayList<Camera> cameras = cameraInitializer(args[0]);
        ArrayList<LiDarWorkerTracker> LiDars = LiDarsInitializer(args[0]);

        ArrayList<Pose> poses = posesInitializer(args[0]);
        ArrayList<Integer> timeServiceParameters = getTimeServiceParameters(args[0]);
        GPSIMU gpsimu = new GPSIMU(0, poses);
        PoseService poseService = new PoseService(gpsimu);

        Thread poseThread = new Thread(poseService);
        ArrayList<CameraService> camerasService = new ArrayList<>();
        ArrayList<LiDarService> LiDarService = new ArrayList<>();
        ArrayList<Thread> cameraThreads = new ArrayList<>();
        ArrayList<Thread> liDarThreads = new ArrayList<>();

        poseThread.start();

        for (Camera camera : cameras) {
            camerasService.add(new CameraService(camera));
            cameraThreads.add(new Thread(camerasService.get(camerasService.size() - 1)));
            cameraThreads.get(cameraThreads.size() - 1).start();
            camerasNum++;
        }
        for (LiDarWorkerTracker liDar : LiDars) {
            LiDarService.add(new LiDarService(liDar));
            liDarThreads.add(new Thread(LiDarService.get(LiDarService.size() - 1)));
            liDarThreads.get(liDarThreads.size() - 1).start();
            LiDarNum++;

        }
        FusionSlam fs = FusionSlam.getInstance();
        FusionSlamService fusionSlamService = new FusionSlamService(fs);
        Thread fusionSlamThread = new Thread(fusionSlamService);
        fusionSlamThread.start();
        TimeService timeService = new TimeService(timeServiceParameters.get(0), timeServiceParameters.get(1));
        Thread timeServiceThread = new Thread(timeService);
        timeServiceThread.start();
        while (!fusionSlamService.isTerminated()) {
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException ignored) {
            }
            isFinished=true;
        }
        while (!timeService.isTerminated()) {
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException ignored) {
            }
            isFinished=true;
        }
        generateOutput(outputDirectory);

    }


    public static ArrayList<Camera> cameraInitializer(String filePath) {
        ArrayList<Camera> cameras = new ArrayList<>();
        try (FileReader reader = new FileReader(filePath)) {

            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject(); /// Parse JSON file
            /// Extract Cameras section
            JsonObject camerasObject = jsonObject.getAsJsonObject("Cameras");
            JsonArray camerasConfig = camerasObject.getAsJsonArray("CamerasConfigurations");
            String cameraDataPath = camerasObject.get("camera_datas_path").getAsString();

            File configFile = new File(filePath);
            String configDirectory = configFile.getParent();
            File cameraDataFile = new File(configDirectory, cameraDataPath);

            /// Load camera data file
            JsonObject cameraDataObject;
            try (FileReader dataReader = new FileReader(cameraDataFile)) {
                cameraDataObject = JsonParser.parseReader(dataReader).getAsJsonObject();
            }
            /// Parse each Camera
            for (JsonElement cameraElement : camerasConfig) {
                JsonObject cameraObj = cameraElement.getAsJsonObject();
                /// Read id, frequency, and camera_key
                int id = cameraObj.get("id").getAsInt();
                int frequency = cameraObj.get("frequency").getAsInt();
                String cameraKey = cameraObj.get("camera_key").getAsString();

                /// Initialize detectedStampedObjectsList
                ArrayList<StampedDetectedObjects> detectedStampedObjectsList = new ArrayList<>();
                /// Filling the detectedStampedObjectsList from the camera data
                JsonArray cameraDataArray = cameraDataObject.getAsJsonArray(cameraKey);
                for (JsonElement stampedObjectElement : cameraDataArray) {
                    JsonObject stampedObject = stampedObjectElement.getAsJsonObject();

                    int time = stampedObject.get("time").getAsInt();
                    JsonArray detectedObjectsArray = stampedObject.getAsJsonArray("detectedObjects");

                    ArrayList<DetectedObject> detectedObjects = new ArrayList<>();
                    for (JsonElement detectedObjectElement : detectedObjectsArray) {
                        JsonObject detectedObject = detectedObjectElement.getAsJsonObject();

                        String objectId = detectedObject.get("id").getAsString();
                        String description = detectedObject.get("description").getAsString();

                        detectedObjects.add(new DetectedObject(objectId, description));
                    }

                    detectedStampedObjectsList.add(new StampedDetectedObjects(time, detectedObjects));
                }

                // Create Camera object
                Camera camera = new Camera(id, frequency, detectedStampedObjectsList);

                cameras.add(camera);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cameras;
    }


    public static ArrayList<LiDarWorkerTracker> LiDarsInitializer(String filePath) {
        ArrayList<LiDarWorkerTracker> lidarWorkers = new ArrayList<>();

        try (FileReader reader = new FileReader(filePath)) {
            JsonObject configObject = JsonParser.parseReader(reader).getAsJsonObject();   /// Parse the configuration JSON

            /// Extract LiDarWorkers section
            JsonObject lidarObject = configObject.getAsJsonObject("LiDarWorkers");
            JsonArray lidarConfigurations = lidarObject.getAsJsonArray("LidarConfigurations");
            String lidarDataPath = lidarObject.get("lidars_data_path").getAsString();

            // Resolve the full path for lidar data file
            File configFile = new File(filePath);
            String configDirectory = configFile.getParent();
            File lidarDataFile = new File(configDirectory, lidarDataPath);

            LiDarDataBase db = LiDarDataBase.getInstance(lidarDataFile.getAbsolutePath());
            /// Parse each LiDarWorkerTracker
            for (JsonElement lidarConfigElement : lidarConfigurations) {
                JsonObject lidarConfig = lidarConfigElement.getAsJsonObject();
                /// Extract id and frequency
                int id = lidarConfig.get("id").getAsInt();
                int frequency = lidarConfig.get("frequency").getAsInt();
                // Create LiDarWorkerTracker instance
                LiDarWorkerTracker lidarWorker = new LiDarWorkerTracker(id, frequency, new ArrayList<TrackedObject>(), db);
                lidarWorkers.add(lidarWorker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lidarWorkers;
    }

    public static ArrayList<Pose> posesInitializer(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Read the root JSON object
            Map<String, Object> rootData = objectMapper.readValue(new File(filePath), Map.class);

            // Extract the path to the pose data file
            String poseDataFilePath = (String) rootData.get("poseJsonFile");

            /// Resolve the full path for pose data file
            File configFile = new File(filePath);
            String configDirectory = configFile.getParent();
            File poseDataFullPath = new File(configDirectory, poseDataFilePath);

            //  read the pose data file
            File poseFile = new File(poseDataFullPath.getAbsolutePath());
            List<Map<String, Object>> rawData = objectMapper.readValue(poseFile, new TypeReference<List<Map<String, Object>>>() {});

            ArrayList<Pose> result = new ArrayList<>();
            for (Map<String, Object> entry : rawData) {
                int time = (int) entry.get("time");
                float x = ((Double) entry.get("x")).floatValue();
                float y = ((Double) entry.get("y")).floatValue();
                float yaw = ((Double) entry.get("yaw")).floatValue();
                result.add(new Pose(x, y, yaw, time));
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<Pose>();
    }

    public static ArrayList<Integer> getTimeServiceParameters(String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Read the JSON file into a map
            Map<String, Object> configData = objectMapper.readValue(new File(filePath), Map.class);

            // Extract TickTime and Duration
            int tickTime = (int) configData.get("TickTime");
            int duration = (int) configData.get("Duration");

            // Store both in a list and return
            ArrayList<Integer> parameters = new ArrayList<>();
            parameters.add(tickTime);
            parameters.add(duration);

            return parameters;
        } catch (Exception e) {
            e.printStackTrace();
            ;
        }
        return new ArrayList<Integer>();
    }
    private static void generateOutput(String outputDirectory) {
        StatisticalFolder stats = StatisticalFolder.getInstance();
        ErrorGenerator errorGenerator = ErrorGenerator.getInstance();
        FusionSlam fusionSlam = FusionSlam.getInstance();


        ObjectMapper objectMapper = new ObjectMapper();
        File outputFile = new File(outputDirectory, "output_file.json");

        try {
            Map<String, Object> output = new HashMap<>();

            // Check for errors
            if (!errorGenerator.getCrashReason().equals("all_good")) {
                // Error case
                output.put("error", errorGenerator.getCrashReason());
                output.put("faultySensor", errorGenerator.getFaultySensor());
                output.put("last_Frames_camera", errorGenerator.getLastFramesCamera());
                output.put("last_Frames_Lidar", errorGenerator.getLastFramesLidar());
                output.put("poses", errorGenerator.getLastPoses());
                output.put("WorldMap", Map.of(
                        "landmarks", errorGenerator.getCoordinates()));
                output.put("Statistics", Map.of(
                        "systemRuntime", stats.getSystemRuntime(),
                        "numDetectedObjects", stats.getNumDetectedObjects(),
                        "numTrackedObjects", stats.getNumTrackedObjects(),
                        "numLandmarks", stats.getNumLandmarks() ));
            } else {
                // Success case
                output.put("Statistics", Map.of(
                        "systemRuntime", stats.getSystemRuntime(),
                        "numDetectedObjects", stats.getNumDetectedObjects(),
                        "numTrackedObjects", stats.getNumTrackedObjects(),
                        "numLandmarks", stats.getNumLandmarks()
                ));

                output.put("WorldMap", Map.of(
                        "landmarks", fusionSlam.getLandmarks()
                ));
            }

            // Write to JSON file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, output);
            System.out.println("Output written to: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Failed to write output file: " + e.getMessage());
        }
    }

}