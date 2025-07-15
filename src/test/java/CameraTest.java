import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

public class CameraTest {

    private Camera camera; // camera
    private ArrayList<StampedDetectedObjects> detectedObjects; // list of stamped detected objects

    @BeforeEach
    public void setUp() {
        // Initialize a list of stamped detected objects for testing
        detectedObjects = new ArrayList<>(); // init the list
        ArrayList<DetectedObject> objects1 = new ArrayList<>();
        objects1.add(new DetectedObject("1", "Object1"));
        ArrayList<DetectedObject> objects2 = new ArrayList<>();
        objects2.add(new DetectedObject("2", "Object2"));
        ArrayList<DetectedObject> objects3 = new ArrayList<>();
        objects3.add(new DetectedObject("3", "Object3"));

        detectedObjects.add(new StampedDetectedObjects(1, objects1)); // add a stampedObject
        detectedObjects.add(new StampedDetectedObjects(3, objects2)); // same
        detectedObjects.add(new StampedDetectedObjects(5, objects3)); // same

        // Initialize the camera with id = 1, frequency = 2, and the test list
        camera = new Camera(1, 2, detectedObjects);
    }

    /**
     * @pre detectedObjects is initialized with 3 elements
     * @pre camera frequency is 2
     * @post the returned object matches the time + frequency condition
     * @post the returned object is removed from the list
     * @post detectObjectsnsize is 2
     * @inv the list size decreases by 1 if a match is found and return correspond object
     */
    @Test
    public void testGetDetectedObjectsAtTime_ExactMatch() {
        StampedDetectedObjects result = camera.getDetectedObjectsAtTime(3);
        assertNotNull(result);
        assertEquals(1, result.getTime());
        assertEquals("Object1", result.getDetectedObjects().get(0).getDescription());
        assertEquals(2, camera.getDetectedObjectsList().size());
    }

    /**
     * @pre detectedObjects is initialized with 3 elements
     * @pre camera frquency is 2
     * @pre provided tick is 4
     * @post return null
     * @inv the list size remains the same
     */
    @Test
    public void testGetDetectedObjectsAtTime_NoMatch() {
        StampedDetectedObjects result = camera.getDetectedObjectsAtTime(4);
        assertNull(result);
        assertEquals(3, camera.getDetectedObjectsList().size());
    }
}