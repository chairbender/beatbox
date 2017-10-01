/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package beatbox;

import java.util.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**Represents a sound that is a Wekinator class (i.e. the
 * sound that the user wants to train the program to play back given
 * a mic input). Can never have two soundclasses with the same id. Uses the
 * object pool pattern to achieve this.
 *
 * @specfield : color Color to use when representing this sound
 * @specfield sound : wav file that this sound represents
 * @specfield classValue : the classValue that wekinator refers to this by
 *
 * @author Kyle
 */
public class SoundClass {

    static int numSoundClasses() {
        if (absValues == null)
            return 0;
        return absValues.size();
    }
    private int classValue;
    private ObjectProperty<Sound> sound;
    public ObjectProperty<Sound> soundProperty() {
        return sound;
    }

    private static Map<Integer,SoundClass> absValues;

    private SoundClass(int classValue, Sound snd) {
        this.classValue = classValue;
        sound = new SimpleObjectProperty<Sound>(snd);
        absValues.put(classValue, this);
    }
    
     /**
     * @requires color and sound != null. classValue represents a valid
     * Wekinator classvalue for the current Wekinator instance.
     * @param color color that hits of this sound should be represented with
     * @param sound audio file (absolute path) to play when hits of this sound are triggered
     * @param classValue integer representing the class id of this class in
     * Wekinator
     * @returns a new SoundClass if classValue != any other instantiated classValue,
     * otherwise, returns the already instantiated SoundClass with the same classValue
     */
    public static SoundClass getSoundClass(int classValue, Sound snd) {
        //check if absValue is instantiated
        if (absValues == null) {
            absValues = new HashMap();
        }

        //check if same abstract value already exists
        if (absValues.containsKey(classValue)) {
            return absValues.get(classValue);
        } else {
            SoundClass sc = new SoundClass(classValue, snd);
            absValues.put(classValue, sc);
            ChuckBoard.addSound(snd,classValue);
            return sc;
        }
    }

    /**
    *
    * @param classValue the classValue of the SoundClass to get
    * @return the SoundClass with the passed classValue, otherwise null
    */
    public static SoundClass getFromClass(int classValue) {
        if (absValues == null)
            return null;
        return absValues.get(classValue);
    }

    public Sound getSound() {
        return sound.get();
    }

    public int getClassValue() {
        return classValue;
    }
    
    public void setSound(Sound snd) {
        this.sound.set(snd);
        ChuckBoard.addSound(snd, classValue);
    }

    /**
     *
     * @param o object to test equality for
     * @return true if o is a SoundClass and if the
     * classValue == this.classValue
     */

    @Override
    public boolean equals(Object o) {
        return (o instanceof SoundClass) &&
                (((SoundClass)o).getClassValue() ==
                classValue);
    }

    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.classValue;
        return hash;
    }
    
    public static int getNextSoundClassValue() throws Exception {
        if (absValues == null) {
            absValues = new HashMap<Integer,SoundClass>();
        }
        int i = 0;
        for (i = 0; i < 100; i++) {
            if (!absValues.containsKey(i))
                break;
        }
        if (i == 100)
            throw new Exception("Too many sound classes");
        return i;
    }

}
