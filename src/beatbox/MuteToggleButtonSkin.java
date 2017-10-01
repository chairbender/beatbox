/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;

/**
 *
 * @author Kyle
 */
public class MuteToggleButtonSkin extends ToggleButtonSkin {
    public MuteToggleButtonSkin(CheckBox cb, int prefHeight) {
        super(cb, prefHeight, new Image(System.getProperty("user.dir") + "\\images\\mute_off.png"), 
                new Image(System.getProperty("user.dir") + "\\images\\mute_on.png"));
    }
}
