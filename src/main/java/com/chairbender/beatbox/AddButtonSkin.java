/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;

/**
 *
 * @author Kyle
 */
public class AddButtonSkin extends ToggleButtonSkin{
     public AddButtonSkin(CheckBox cb, int prefHeight) {
        super(cb, prefHeight, new Image(System.getProperty("user.dir") + "\\images\\add_in.png"), 
                new Image(System.getProperty("user.dir") + "\\images\\add_out.png"));
    }
}
