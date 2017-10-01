/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import beatbox.ChuckBoard;
import javafx.beans.value.ObservableValue;
import javafx.scene.*;
import javafx.scene.shape.*;
import com.illposed.osc.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.paint.*;
import javafx.scene.input.*;


import javafx.scene.control.Slider;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

/**Monitors input, which it receives through OSC. Updted version of
 * inputMOnitor
 * @requires: inputMonitor is chucked
 * @author Kyle
 */
//TODO: scale up the maxAdc if it gets exceeded a lot
public class VolumeMeter extends Parent{
    VolumeMeter me = this;
    
    private static BooleanProperty muted = new SimpleBooleanProperty(false);
    private int numSteps = 20;
    
    private static DoubleProperty threshold = new SimpleDoubleProperty(0.0); 
    public static DoubleProperty thresholdProperty() { return threshold; }
    private static DoubleProperty maxAdc = new SimpleDoubleProperty(.0001);
    public static DoubleProperty maxAdcProperty() { return maxAdc; }
//    public static void setThreshold(double value) {
//        threshold.set(value);
//    }
//    public static double getThreshold() {
//        return threshold.get();
//    }
//    public static void setMaxAdc(double val) {
//        maxAdc.set(val);
//    }
//    public static double getMaxAdc() {
//        return maxAdc.get();
//    }
    private Group grpSteps;
            
    private ImageView arrow;
    private ImageView imgX;
    private Line line;
    private final Button btnSilence;

    public VolumeMeter(final double width, final double height) {
        super();

        this.numSteps = (int)((height)/5);
        Rectangle back = new Rectangle();
        back.setWidth(width);
        back.setHeight(height);
        Stop[] stops = new Stop[] { new Stop(0,Color.RED.brighter()), new Stop((3*height)/4, Color.GREEN.brighter())};
        back.setFill(new LinearGradient(0,0,0,height,false,CycleMethod.NO_CYCLE,stops));
        getChildren().add(back);
        
        grpSteps = new Group();
        for (int i = 0; i < numSteps; i++) {
            Rectangle r = new Rectangle();
            r.setFill(Color.TRANSPARENT);
            r.setStroke(Color.BLACK);
            r.setWidth(width);
            r.setHeight((height)/numSteps);
            r.setLayoutX((width/2) - (r.getWidth()/2));
            r.setLayoutY(numSteps*r.getHeight() - (i+1)*r.getHeight());
            grpSteps.getChildren().add(r);
        }
        getChildren().add(grpSteps);
        
        maxAdc.addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                arrow.setLayoutY(height - ((threshold.get()/maxAdc.get())*(height)) - (arrow.getBoundsInLocal().getHeight()/2));
                line.setStartY(height - ((threshold.get()) / maxAdc.get() * (height)));
            }
        });

        ChuckBoard.ADC.lastProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.doubleValue() > maxAdc.get()) {
                    maxAdc.set(newValue.doubleValue() + newValue.doubleValue()/2);
                    arrow.setLayoutY(height - ((threshold.get()/maxAdc.get())*(height)) - (arrow.getBoundsInLocal().getHeight()/2));
                    line.setStartY(height - ((threshold.get()) / maxAdc.get() * (height)));
                }
                int stepNumber = (int)Math.round(newValue.doubleValue()/maxAdc.get()*(numSteps));
                for (int i = 0; i < numSteps; i++) {
                    Rectangle r = ((Rectangle)grpSteps.getChildren().get(i));
                    if (i <= numSteps / 2) {
                        //r.setFill(Color.GREEN);
                    } else if (i < (numSteps - numSteps/5)) {
                        //r.setFill(Color.YELLOW);
                    } else {
                        //r.setFill(Color.RED);
                    }
                    
                    if (i < stepNumber && !muted.get()) {
                        r.setFill(Color.TRANSPARENT);
                    } else {
                        r.setFill(Color.BLACK.deriveColor(0, 0, 0, 0.95));
                    }
                }
            }
        });
        
        arrow = new ImageView();
        arrow.setImage(new Image(System.getProperty("user.dir") + "\\images\\arrow_right.png"));
        threshold.addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.doubleValue() > maxAdc.get())
                    threshold.set(maxAdc.get());
                else if (newValue.doubleValue() < 0)
                    threshold.set(0);
                arrow.setLayoutY(height - ((threshold.get()/maxAdc.get())*(height)) - (arrow.getBoundsInLocal().getHeight()/2));
                line.setStartY(height - ((threshold.get()) / maxAdc.get() * (height)));
                ChuckBoard.setThreshold(threshold.get());
            }
        });
        arrow.setFitWidth(2*height/numSteps);
        arrow.setPreserveRatio(true);
        arrow.setLayoutX(-arrow.getBoundsInLocal().getWidth()/2);

        arrow.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                arrow.setCursor(Cursor.HAND);
            }
        });
        arrow.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                threshold.set(maxAdc.get() - (((me.sceneToLocal(0, event.getSceneY()).getY())/(height)) * maxAdc.get()));
            }
        });

        line = new Line();
        line.setFill(Color.WHITE);
        line.setStroke(Color.WHITE);
        Bindings.bindBidirectional(line.startYProperty(), line.endYProperty());
        line.setEndX(width);
        getChildren().add(line);
        getChildren().add(arrow);
        
//        imgX = new ImageView(new Image(System.getProperty("user.dir") + "\\images\\mute.png"));
//        imgX.setLayoutY(height+8);
//        imgX.setPreserveRatio(true);
//        imgX.setFitWidth(width);
//        imgX.setCursor(Cursor.HAND);
//        imgX.setOnMouseClicked(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                VolumeMeter.muted = !VolumeMeter.muted;
//            }
//        });
//        getChildren().add(imgX);
        btnSilence = new Button();
        final MuteButtonSkin skn = new MuteButtonSkin(btnSilence,width*2);
        if (muted.get()) {
            skn.setOff();
        }
        btnSilence.setSkin(skn);
        btnSilence.setLayoutY(height-width*2);
        btnSilence.setLayoutX(width+5);
        btnSilence.setCursor(Cursor.HAND);
        btnSilence.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                VolumeMeter.muted.set(!VolumeMeter.muted.get());
            }
        });
        VolumeMeter.muted.addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (skn.isOn() == muted.get())
                    btnSilence.arm();
                    btnSilence.disarm();
            }
        });
        getChildren().add(btnSilence);
        
        if (threshold.get() > maxAdc.get())
                    threshold.set(maxAdc.get());
        else if (threshold.get() < 0)
            threshold.set(0);
        arrow.setLayoutY(height - ((threshold.get()/maxAdc.get())*(height)) - (arrow.getBoundsInLocal().getHeight()/2));
        line.setStartY(height - ((threshold.get()) / maxAdc.get() * (height)));
        ChuckBoard.setThreshold(threshold.get());
    }
    
    public static boolean isMuted() {
        return VolumeMeter.muted.get();
    }
}
