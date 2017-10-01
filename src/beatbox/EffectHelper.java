/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import javafx.animation.*;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.Bloom;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 *
 * @author Kyle
 */
public class EffectHelper {
    
    //makes the node glow on mouseover - eliminates any effects
    //that were placed on the node other than this glow
    //unspecified if the mouseEntered and mouseExited events
    //are set after calling this
    public static void glowOver(Node node) {
            final Bloom blm = new Bloom();
            node.setEffect(blm);
            blm.setThreshold(1.0);
            
            final Timeline tmlOn = new Timeline();
            KeyValue kv = new KeyValue(blm.thresholdProperty(),0.0);
            KeyFrame kf = new KeyFrame(Duration.millis(0),kv);
            KeyValue kv2 = new KeyValue(blm.thresholdProperty(),1.0);
            KeyFrame kf2 = new KeyFrame(Duration.millis(100),kv2);
            tmlOn.getKeyFrames().add(kf);
            tmlOn.getKeyFrames().add(kf2);
            
            final Timeline tmlOff = new Timeline();
            KeyValue kv3 = new KeyValue(blm.thresholdProperty(),1.0);
            KeyFrame kf3 = new KeyFrame(Duration.millis(0),kv3);
            KeyValue kv4 = new KeyValue(blm.thresholdProperty(),0.0);
            KeyFrame kf4 = new KeyFrame(Duration.millis(200),kv4);
            tmlOff.getKeyFrames().add(kf3);
            tmlOff.getKeyFrames().add(kf4);
            
            final EventHandler<? super MouseEvent> prevEn = node.getOnMouseEntered();
            final EventHandler<? super MouseEvent> prevEx = node.getOnMouseExited();
            
            node.setOnMouseEntered(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent event) {
                    if (prevEn != null)
                        prevEn.handle(event);
                    tmlOff.playFromStart();
                }
            });
            node.setOnMouseExited(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent event) {
                    if (prevEx != null)
                        prevEx.handle(event);
                    tmlOn.playFromStart();
                }
            });
    }
}

