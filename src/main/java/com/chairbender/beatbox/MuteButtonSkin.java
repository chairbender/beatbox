/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Kyle
 */
public class MuteButtonSkin implements Skin<Button>{
    private Button parent;
    private Node result;
    private boolean isOn = true;
    private ImageView imgv;
    private Image in = new Image(System.getProperty("user.dir") + "\\images\\silence_in.png");
    private Image on = new Image(System.getProperty("user.dir") + "\\images\\silence_on.png");
    private Image off = new Image(System.getProperty("user.dir") + "\\images\\silence_off.png");
    
    public MuteButtonSkin(Button par, double size) {
        parent = par;
        System.out.println("sz: " + size);

        
        imgv = new ImageView(on);
        imgv.setPreserveRatio(true);
        imgv.setFitHeight(size);
        imgv.setSmooth(true);
        imgv.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                parent.arm();
            }
        });
        imgv.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                parent.disarm();
                parent.fire();
            }
        });
        
        parent.armedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue)
                    imgv.setImage(in);
                else {
                    isOn = !isOn;
                    if (isOn) {
                        imgv.setImage(on);
                    } else {
                        imgv.setImage(off);
                    }
                }
                    
            }
        });
        result = imgv;
        
    }
    
    public boolean isOn() {
        return isOn;
    }

    public Button getSkinnable() {
        return parent;
    }

    public Node getNode() {
        return result;
    }

    public void dispose() {
        return;
    }

    void setOff() {
        imgv.setImage(off);
        isOn = false;
    }
    
}
