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
import javafx.application.Platform;

/**
 *
 * @author Kyle
 */
public class AddLoopListener implements OSCListener {
    private List<HitNote> notes;
    private double repeatTime;
    private OSCPortOut sender;
    private Loop loop;
    /**
     * @param notes not null, represents an ordered list of
     * notes in a measure
     * @param repeatTime the time to repeat the loop at in ms
     */
    public AddLoopListener(Loop loop, OSCPortOut sender) {
        this.notes = loop.getNotes();
        this.repeatTime = loop.getEndTime();
        this.sender = sender;
        this.loop = loop;
    }
    
    public void acceptMessage(Date date, final OSCMessage oscm) {
        Platform.runLater(new Runnable() {
            public void run() {
                //System.out.println(Arrays.toString(oscm.getArguments()));
                //System.out.println(loop.getLoopId() + " " + ((Integer)(oscm.getArguments()[0])).toString());
                int toSet = (Integer)(oscm.getArguments().get(0));
                //System.out.println("toSet " + toSet);
                loop.setLoopId(toSet);
                //System.out.println(loop.getLoopId() + " " + ((Integer)(oscm.getArguments()[0])).toString());
                Object args[] = new Object[(notes.size()+1)*2];
                int i = 0;
                for (HitNote note : notes) {
                    args[i++] = (int)note.getClassValue();
                    args[i++] = (int)(1000*note.getTimeOffsetInMilliseconds());
                }

                //add repeatTime
                args[i++] = (int)0;
                args[i++] = (int)(1000*repeatTime);
                
                try {
                    System.out.println("Sending ready message " + Arrays.toString(args));
                    sender.send(new OSCMessage("/loopNotes", Arrays.asList(args)));
                    //now tell it to start playing
                    //sender.send(new OSCMessage("/playAllLoops",null));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
