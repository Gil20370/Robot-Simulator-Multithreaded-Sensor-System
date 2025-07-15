package bgu.spl.mics.application.objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.*;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private static LiDarDataBase instance;
    private ArrayList<StampedCloudPoints> data;

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public synchronized static LiDarDataBase getInstance(String filePath) throws FileNotFoundException {
        if (instance == null) {
            try {
                // Parse the JSON file to initialize the database
                ArrayList<StampedCloudPoints> stamps = parseLiDarData(filePath);
                instance = new LiDarDataBase(stamps);
            } catch (IOException e) {
                throw new FileNotFoundException("Could not read the file at path: " + filePath);
            }
        }
        return instance;
    }


    private LiDarDataBase(ArrayList<StampedCloudPoints> data) {
        this.data = data;
    }

    private static ArrayList<StampedCloudPoints> parseLiDarData(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Read the JSON file into a list of maps
        List<Map<String, Object>> rawData = objectMapper.readValue(new File(filePath), new TypeReference<List<Map<String, Object>>>() {});
        ArrayList<StampedCloudPoints> result = new ArrayList<>();

        for (Map<String, Object> entry : rawData) {
            int time = (int) entry.get("time");
            String id = (String) entry.get("id");
            ArrayList<ArrayList<Double>> rawCloudPoints = (ArrayList<ArrayList<Double>>) entry.get("cloudPoints");
            result.add(new StampedCloudPoints(id, time,rawCloudPoints));
        }

        return result;
    }

    public synchronized ArrayList<StampedCloudPoints> getData() {
        return data;
    }
}
