/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import com.illposed.osc.*;
import java.util.Observer;
import java.util.Observable;
import java.lang.UnsupportedOperationException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *Class for getting timing data from
 * chuck.
 * expects osc messages in the form
 * /peakTime if, where
 * i = id of the hit
 * f = time offset of the hit in ms
 *
 *@specfield updates: function(:HitTiming)[] list of functions to be called when
 * hit timing is received
 * @author Kyle
 */
public class TimingListener implements OSCListener {
    //the OSCPort to check for /peakTime ff messages

    private OSCPortIn receiver;
    // List of Observers
    // Note that function(:String):Void[] is not a valid syntax !
    private Set<TimingReceivedHandler> updates;

    /*TODO: var onUpdate:function(:HitTiming):Void on replace {
        if ( onUpdate != null ) {
            insert onUpdate into updates;
            onUpdate = null;
        }
     };*/
    
    public TimingListener(OSCPortIn receiver) {
        super();
        this.receiver = receiver;
        this.receiver.addListener("/peakTime",this);
        this.receiver.startListening();
        updates = new HashSet<TimingReceivedHandler>();
    }

    /**
    * @effects adds the passed function to the list of functions to be called
    * whenever timing information is recieved from chuck and the adapter is listening.
    */
    public void addTimingReceivedEventHandler(TimingReceivedHandler handler) {
        updates.add(handler);
    }

    /**
    * @effects removes the function from the list of functions to be called
    * whenever timing info is received from chuck.
    */
    public void removeTimingReceivedEventHandler(TimingReceivedHandler handler) {
        updates.remove(handler);
    }

    //what to do when message received
    @Override
    public void acceptMessage (Date arg0, OSCMessage oscm) {
        for ( TimingReceivedHandler update : updates ) {
            update.onReceived(
                new HitTiming((Integer) oscm.getArguments().get(0),
                    (Float) oscm.getArguments().get(1))
            );
        }
    }
}
