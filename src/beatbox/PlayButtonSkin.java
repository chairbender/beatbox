/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;

/**
 *
 * @author Kyle
 */
class PlayButtonSkin extends ToggleButtonSkin {
    public PlayButtonSkin(CheckBox cb, int prefHeight) {
        super(cb, prefHeight, new Image(System.getProperty("user.dir") + "\\images\\pause.png"), 
                new Image(System.getProperty("user.dir") + "\\images\\play.png"));
    }
    
}
