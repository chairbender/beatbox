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
public class RecordButtonSkin extends ToggleButtonSkin{
    public RecordButtonSkin(CheckBox cb, int prefHeight) {
        super(cb, prefHeight, new Image(System.getProperty("user.dir") + "\\images\\record_in.png"), 
                new Image(System.getProperty("user.dir") + "\\images\\record_out.png"));
    }
}
