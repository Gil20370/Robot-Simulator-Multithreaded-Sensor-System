package bgu.spl.mics.application;

import com.google.gson.Gson;
import java.io.FileReader;

public class ConfigLoader {
    public static Configuration loadConfig(String filePath) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(new FileReader(filePath), Configuration.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}