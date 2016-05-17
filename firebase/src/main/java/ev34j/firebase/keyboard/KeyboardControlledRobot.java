package ev34j.firebase.keyboard;

import com.ev34j.core.common.Platform;
import com.ev34j.mindstorm.motor.SteeringMotors;
import com.ev34j.mindstorm.sound.Ev3Sound;
import com.ev34j.mindstorm.utils.Wait;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static ev34j.firebase.keyboard.Constants.DEFAULT_ROBOT;
import static ev34j.firebase.keyboard.Constants.DEFAULT_USER;
import static ev34j.firebase.keyboard.Constants.LAST_KEYSTROKE;
import static ev34j.firebase.keyboard.Constants.METRICS;
import static ev34j.firebase.keyboard.Constants.POSITION1;
import static ev34j.firebase.keyboard.Constants.POSITION2;
import static ev34j.firebase.keyboard.Constants.POWER1;
import static ev34j.firebase.keyboard.Constants.POWER2;
import static ev34j.firebase.keyboard.Constants.STEERING;
import static java.lang.String.format;

public class KeyboardControlledRobot {

  public static void main(final String[] args) {
    final KeyboardControlledRobot robot = new KeyboardControlledRobot();
    Wait.forSecs(Integer.MAX_VALUE);
  }

  private final long           startTime = System.currentTimeMillis();
  private final SteeringMotors motors    = new SteeringMotors("A", "B");
  private final Executor       executor  = Executors.newSingleThreadExecutor();
  private final AtomicBoolean  firstTime = new AtomicBoolean(true);

  private int steering      = 0;
  private int power         = 0;
  private int exitCommand   = 0;
  private int lastSteering  = Integer.MIN_VALUE;
  private int lastPower1    = Integer.MIN_VALUE;
  private int lastPower2    = Integer.MIN_VALUE;
  private int lastPosition1 = Integer.MIN_VALUE;
  private int lastPosition2 = Integer.MIN_VALUE;

  private final Firebase firebase;

  public KeyboardControlledRobot() {
    this.firebase = new Firebase(Constants.FIREBASE_URL);
    this.firebase.getRoot()
                 .child(DEFAULT_USER)
                 .child(LAST_KEYSTROKE)
                 .addValueEventListener(
                     new ValueEventListener() {
                       @Override
                       public void onDataChange(final DataSnapshot dataSnapshot) {
                         final KeyboardData data = dataSnapshot.getValue(KeyboardData.class);
                         if (firstTime.get()) {
                           firstTime.set(false);
                           if (Platform.isEv3Brick())
                             Ev3Sound.say("Ready", 100);
                           System.out.println("Ready");
                         }

                         processKeyStroke(data);
                       }

                       @Override
                       public void onCancelled(final FirebaseError error) {
                         System.out.println(String.format("ValueEventListener.onCancelled() : %s", error.getMessage()));
                       }
                     });

    final Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            while (true) {
              // Do not send duplicate  values
              final int steering = getMotors().getSteering();
              if (steering != lastSteering) {
                reportMetricToFirebase(STEERING, steering);
                lastSteering = steering;
              }

              final int power1 = getMotors().getPower1();
              if (power1 != lastPower1) {
                reportMetricToFirebase(POWER1, power1);
                lastPower1 = power1;
              }

              final int power2 = getMotors().getPower2();
              if (power2 != lastPower2) {
                reportMetricToFirebase(POWER2, power2);
                lastPower2 = power2;
              }

              final int position1 = getMotors().getPosition1();
              if (position1 != lastPosition1) {
                reportMetricToFirebase(POSITION1, position1);
                lastPosition1 = position1;
              }

              final int position2 = getMotors().getPosition1();
              if (position2 != lastPosition2) {
                reportMetricToFirebase(POSITION2, position2);
                lastPosition2 = position2;
              }

              Wait.forMillis(500);
            }
          }
        };

    this.executor.execute(runnable);

    if (Platform.isEv3Brick())
      Ev3Sound.say("Initiated", 100);
    System.out.println("Initiated");
  }

  private Firebase getFirebase() { return this.firebase; }

  private SteeringMotors getMotors() { return this.motors; }

  private void reportMetricToFirebase(final String metric, final int value) {
    // System.out.println(format("Writing data for %s: %d", metric, value));
    final CountDownLatch latch = new CountDownLatch(1);
    final Firebase.CompletionListener listener =
        new Firebase.CompletionListener() {
          @Override
          public void onComplete(final FirebaseError error, final Firebase firebase) {
            if (error != null)
              System.err.println(format("Data not writter: %s", error.getMessage()));
            latch.countDown();
          }
        };

    this.getFirebase()
        .getRoot()
        .child(DEFAULT_ROBOT)
        .child(METRICS)
        .child(metric)
        .setValue(new RobotData(metric, value), listener);
  }

  private void processKeyStroke(final KeyboardData data) {
    // System.out.println("Type: " + data.getKeyType());
    // Prevent acting on keystrokes that occur before startup
    if (!Platform.isUnknown() || data.getTimeStamp() < startTime)
      return;

    switch (data.getKeyType()) {
      case LOWER_S:
      case UPPER_S:
        this.power = 0;
        this.updateMotors();
        break;
      case UP_ARROW:
        this.power = Math.min(this.power + 10, 100);
        this.updateMotors();
        break;
      case DOWN_ARROW:
        this.power = Math.max(this.power - 10, -100);
        this.updateMotors();
        break;
      case LEFT_ARROW:
        this.steering = Math.max(this.steering - 10, -100);
        this.updateMotors();
        break;
      case RIGHT_ARROW:
        this.steering = Math.min(this.steering + 10, 100);
        this.updateMotors();
        break;
      case SHIFT_UP_ARROW:
        this.power = Math.min(this.power + 20, 100);
        this.updateMotors();
        break;
      case SHIFT_DOWN_ARROW:
        this.power = Math.max(this.power - 20, -100);
        this.updateMotors();
        break;
      case SHIFT_LEFT_ARROW:
        this.steering = Math.max(this.steering - 20, -100);
        this.updateMotors();
        break;
      case SHIFT_RIGHT_ARROW:
        this.steering = Math.min(this.steering + 20, 100);
        this.updateMotors();
        break;
      case LOWER_X:
      case UPPER_X:
        this.exitCommand++;
        if (this.exitCommand >= 2) {
          this.motors.off();
          System.exit(0);
        }
        break;
      default:
        // Ignore other keys
    }

  }

  private void updateMotors() {
    if (this.power == 0)
      this.motors.off();
    else
      this.motors.on(this.steering, this.power);
  }
}
