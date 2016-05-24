import com.ev34j.mindstorm.sensor.NxtSoundSensor;

import static java.lang.String.format;

public class SoundPressure {

  public static void main(String[] args)
      throws InterruptedException {

    final NxtSoundSensor ss = new NxtSoundSensor(3);

    float currVal = -1F;
    for (int i = 0; i < 2000; i++) {
      final float val = ss.getSoundPressureWeightedPct();
      if (Math.abs(currVal - val) > 5) {
        System.out.println(format("Sound pressure: %s", val));
        currVal = val;
      }
      Thread.sleep(20);
    }
  }
}
