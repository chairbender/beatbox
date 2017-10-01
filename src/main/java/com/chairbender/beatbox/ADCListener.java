/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import com.illposed.osc.*;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.property.*;

/**
 *
 * @author Kyle
 */
class ADCListener {
    
    private DoubleProperty last;
    public DoubleProperty lastProperty() { return last; }
    
    public ADCListener(OSCPortIn in) {
        last = new SimpleDoubleProperty(0.0);
        in.addListener("/monitorLevel", new OSCListener() {
            public void acceptMessage(Date date, final OSCMessage oscm) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        last.set((Float) oscm.getArguments().get(0));
                    }
                });
            }
        });
        in.startListening();   
    }
}
