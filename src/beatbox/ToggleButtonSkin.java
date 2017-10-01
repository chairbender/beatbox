/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package beatbox;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.Bloom;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;

/** A class that represents a skin for a button that can be pushed in and out
 * To use this class, extend it and override inImage and outImage with the
 * images you want to display (see RecordButtonSkin for an example)
 * @author Kyle
 */
public class ToggleButtonSkin implements Skin {
    private ToggleButtonSkin me = this;
    //image displayed when pressed in and out
    protected Image inImage;
    protected Image outImage;
    private Bloom blm;
    private Timeline bloomOn;
    private Timeline bloomOff;
    private ImageView node;
    
    private CheckBox checkBoxControl;
    private Integer prefHeight = 50;


    public Skinnable getSkinnable() {
        return checkBoxControl;
    }

    public Node getNode() {
        return node;
    }

    public void dispose() {
        return;
    }

    /*
    *@param parent the CheckBox for this skin
    */
    public ToggleButtonSkin(CheckBox cb, int prefHeight, Image in, Image out, boolean glowOver) {
        this.inImage = in;
        this.prefHeight = prefHeight;
        outImage = out;
        checkBoxControl = cb;
        node = new ImageView();  
        cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    node.setImage(inImage);
                } else {
                    node.setImage(outImage);
                }
            }
        });
        node.setPreserveRatio(true);
        node.setSmooth(true);
        node.setFitHeight(prefHeight);
        node.setImage(outImage);
        node.setSmooth(true);
    }
    
    public ToggleButtonSkin(CheckBox cb, int prefHeight, Image in, Image out) {
        this(cb,prefHeight,in,out,false);
    }

    private void glowOn() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void glowOff() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
