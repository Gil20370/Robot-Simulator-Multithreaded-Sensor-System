import bgu.spl.mics.application.objects.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

public class FusionSlamTest {

    /**
     * compute the place of the detectedObject relative to starting point
     * @pre robot is found in x=10 and y=20 relative to starting point
     * @pre Pose has zero yaw,time=0 and local coordinates should translate directly without rotation.
     * @pre local coordinates size=2
     * @post locl coordinates size=2
     * @post Coordinates are translated only; rotation is not applied.
     * @post yaw=0
     * @post local coordinates un changed:(10,20)
     * @post retrieve global coordinates (11,21),(12,23)
     * @INV Local coordinates are unchanged; global coordinates reflect translation only.
     */
    @Test
    public void testConvertToGlobalCoordinates_NoRotation() {
        Pose pose = new Pose(10, 20, 0, 0); // X=10, Y=20, Yaw=0
        ArrayList<CloudPoint> localCoordinates = new ArrayList<>();
        localCoordinates.add(new CloudPoint(1, 1));
        localCoordinates.add(new CloudPoint(2, 3));
        FusionSlam fusionSlam = FusionSlam.getInstance();
        ArrayList<CloudPoint> globalCoordinates = fusionSlam.convertToGlobalCoordinates(localCoordinates, pose);

        assertEquals(2, localCoordinates.size(), 0.001);
        assertEquals(0,pose.getYaw(), 0.001);
        assertEquals(10,pose.getX(), 0.001);
        assertEquals(20,pose.getY(), 0.001);
        assertEquals(11, globalCoordinates.get(0).getX());
        assertEquals(21, globalCoordinates.get(0).getY(), 0.001);
        assertEquals(12, globalCoordinates.get(1).getX(), 0.001);
        assertEquals(23, globalCoordinates.get(1).getY(), 0.001);
    }

    /**
     * compute the place of the detectedObject relative to starting point
     * @pre robot is found in x=0 and y=0 relative to starting point (at starting point)
     * @pre Pose has 90 yaw,time=0 and local coordinates should translate directly without rotation.
     * @pre local coordinates size=2
     * @post locl coordinates size=2
     * @post Coordinates are translated only; rotation is not applied.
     * @post yaw=90
     * @post local coordinates un changed:(0,00)
     * @post retrieve global coordinates (0,1),(-1,0)
     * @INV Local coordinates are unchanged; global coordinates reflect translation only.
     */
    @Test
    public void testConvertToGlobalCoordinates_WithRotation() {
        Pose pose = new Pose(0, 0, 90, 0); // X=0, Y=0, Yaw=90 degrees
        ArrayList<CloudPoint> localCoordinates = new ArrayList<>();
        localCoordinates.add(new CloudPoint(1, 0)); // 1 unit along the X-axis
        localCoordinates.add(new CloudPoint(0, 1)); // 1 unit along the Y-axis

        FusionSlam fusionSlam = FusionSlam.getInstance();
        ArrayList<CloudPoint> globalCoordinates = fusionSlam.convertToGlobalCoordinates(localCoordinates, pose);

        assertEquals(2, localCoordinates.size(), 0.001);
        assertEquals(90,pose.getYaw(), 0.001);
        assertEquals(0,pose.getX(), 0.001);
        assertEquals(0,pose.getY(), 0.001);
        assertEquals(0, globalCoordinates.get(0).getX(), 0.001);
        assertEquals(1, globalCoordinates.get(0).getY(), 0.001);
        assertEquals(-1, globalCoordinates.get(1).getX(), 0.001);
        assertEquals(0, globalCoordinates.get(1).getY(), 0.001);
    }
}
