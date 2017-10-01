/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import java.util.*;
import java.io.*;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Paint;

/** Class for abstracting the library of audio files
 *
 * @author Kyle
 */
public class SoundLibrary implements Serializable{
    private static List<Sound> lst = new ArrayList<Sound>();
    private static List<ChangeListener<? super Sound>> listeners;

    static void addOnLibraryChangedListener(ChangeListener<Sound> changeListener) {
        if (listeners == null)
            listeners = new ArrayList<ChangeListener<? super Sound>>();
        listeners.add(changeListener);
    }

    static void removeSound(Sound get) {
        if (!lst.contains(get))
            System.out.println("Sound not found in library, not removing anything.");
        lst.remove(get);
        for (ChangeListener<? super Sound> c : listeners)
            c.changed(null, null, get);
        get.notifyDeletedFromLibrary();
    }

    static Sound randomSound() {
        return lst.get((int)Math.random() * (lst.size()-1));
    }

    private SoundLibrary() {
        
    }
    
    /**
     * 
     * @param sound  if name is null or empty, doesn't add the sound
     */
    static void addSound(Sound sound) {
        if (sound != null) {
            if (listeners == null)
                listeners = new ArrayList<ChangeListener<? super Sound>>();
            if (sound.getName() == null || sound.getName().isEmpty()) {
                System.out.println("Didn't add sound");
                return;
            }
            lst.add(sound);
//            sound.colorProperty().addListener(new ChangeListener<Paint>() {
//                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
//                    System.out.println("changed sound color");
//                }
//            });
            for (ChangeListener<? super Sound> c : listeners)
                c.changed(null, null, sound);
        }
    }
    
    /**
     * @requires load has been called
     * @return the list of sounds in the library. The list is immutable.
     */
    public static List<Sound> getSounds() {
        return Collections.unmodifiableList(lst);
    }
    
    public static void save(ObjectOutputStream out) throws IOException {
        out.writeObject(lst.size());
        for (Sound s : lst) {
            out.writeObject(s);
        }
    }
    
    public static void load(ObjectInputStream in) throws IOException, ClassNotFoundException {
        lst = new ArrayList<Sound>();

        int num = (Integer)in.readObject();

        for (int i = 0; i < num; i++) {
            addSound((Sound)in.readObject());
            //lst.add((Sound)in.readObject());
        }
    }
}
