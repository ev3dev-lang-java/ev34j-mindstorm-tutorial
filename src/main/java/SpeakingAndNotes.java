import com.ev34j.mindstorm.sound.Ev3Sound;
import com.ev34j.mindstorm.sound.Note;
import com.ev34j.mindstorm.utils.Wait;

public class SpeakingAndNotes {

  public static void main(String[] args) {

    Ev3Sound.sayAsEnglish("I am a LEGO robot", 100);
    Wait.secs(1);
    Ev3Sound.sayAsSpanish("Soy un robot LEGO", 100);
    Wait.secs(1);

    for (Note note : Note.values())
      Ev3Sound.playNote(note, 1, 100);
  }
}