/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kyle
 */
public class StartPlaybackListener implements OSCListener{
    private List<HitNote> notes;
    private float repeatTime;
    private OSCPortOut sender;
    
    /**
     * @param notes not null, represents an ordered list of
     * notes in a measure
     * @param repeatTime the time to repeat the loop at in ms
     */
    public StartPlaybackListener(List<HitNote> notes, float repeatTime, OSCPortOut sender) {
        this.notes = notes;
        this.repeatTime = repeatTime;
    }
    
    public void acceptMessage(Date date, OSCMessage oscm) {
        Object args[] = new Object[(notes.size()+1)*2];
        int i = 0;
        for (HitNote note : notes) {
            args[i++] = note.getClassValue();
            args[i++] = note.getTimeOffsetInMilliseconds();
        }

        //add repeatTime
        args[i++] = 0;
        args[i++] = repeatTime;
        try {
            sender.send(new OSCMessage("/loopNotes", Arrays.asList(args)));
            //now tell it to start playing
            sender.send(new OSCMessage("/playAllLoops",null));
        } catch (IOException ex) {
            Logger.getLogger(StartPlaybackListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
