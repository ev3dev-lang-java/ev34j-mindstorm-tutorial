import com.ev34j.mindstorms.motor.SteeringMotors;
import com.ev34j.mindstorms.time.Wait;

public class SimpleSteeringMotors {

  public static void main(String[] args) {

    System.out.println("Trying out SteeringMotors");

    // Declare motors
    SteeringMotors motors = new SteeringMotors("A", "B");

    // Go forward 2 rotations at 25% power
    motors.onForRotations(0, 25, 2);
    motors.waitUntilStopped();

    // Pause
    Wait.forSecs(1);

    // Go back 2 rotations at 25% power
    motors.onForRotations(0, -25, 2);
    motors.waitUntilStopped();
  }
}
