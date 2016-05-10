import com.ev34j.core.utils.Delay;
import com.ev34j.mindstorm.leds.Ev3StatusLight;
import com.ev34j.mindstorm.leds.Ev3StatusLights;
import com.ev34j.mindstorm.sound.Ev3Sound;

public class StatusLights {

  public static void main(String[] args) {

    Ev3StatusLight left = Ev3StatusLights.left();
    Ev3StatusLight right = Ev3StatusLights.right();

    Ev3Sound.setVolume(100);
    Ev3Sound.sayAsEnglish("Red");
    left.red();
    right.red();
    Delay.delaySecs(2);

    Ev3Sound.sayAsEnglish("Green");
    left.green();
    right.green();
    Delay.delaySecs(2);

    Ev3Sound.sayAsEnglish("Orange");
    left.orange();
    right.orange();
    Delay.delaySecs(2);

    Ev3Sound.sayAsEnglish("Off");
    left.off();
    right.off();
    Delay.delaySecs(2);
  }
}
