/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.chairbender.beatbox;

/**
 * Encapsulates a single recorded vocalization without
 * timing information
 * @author Kyle
 */
public class Hit {
    private SoundClass soundClass;
    private int id;
    
    public Hit(int id, SoundClass soundClass) {
        this.id = id;
        this.soundClass = soundClass;
    }
    
    public int getId() {
        return id;
    }
    
    public SoundClass getSoundClass() {
        return soundClass;
    }

    void setSoundClass(SoundClass newValue) {
        soundClass = newValue;
    }
}