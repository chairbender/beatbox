/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import java.lang.Math;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.effect.*;
import javafx.scene.input.MouseEvent;
import java.io.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.*;
import javafx.util.Duration;

/**
 *A box representing a single recorded example
 *
 * @author Kyle
 */
public class Example extends Parent implements Serializable{
    /*Constructor fields*/
    Example me = this;
    private boolean outline = false; //if it should be a placeholder-looking box
    private int boxSize = 15;
    private boolean selected;  //if the node is currently selected, displays
    //differently
    private SoundClass soundClass; //soundClass this example is for
    private SoundClass classifiedSoundClass; //soundclass this example was classified as
    private int exampleId; //the id of this example (given by chuck) that persists in the wekinator instance
    private int chuckId; //the id of this example used to trigger the sound in chuck, can change
    private Color color; //color to display
    private Rectangle rect;
    private boolean deleteable;
    private ImageView imgX;
    private boolean muted = false;
    private Rectangle rctFlash;
    private Timeline tmlFlash;
    private ChangeListener<Sound> sndListen;
    private ChangeListener<Paint> pntListen;
    /**
     * 
     *
     * @param soundClass soundclass of this example
     * @param id id corresponding to this example in chuck
     * @param boxSize height and width of this
     */
    public Example(SoundClass soundClass, int exampleId, int boxSize) {
        super();
        this.soundClass = soundClass;
        this.classifiedSoundClass = soundClass;
        this.exampleId = exampleId;
        this.chuckId = exampleId;
        this.boxSize = boxSize;
        this.deleteable = true;
        
        init();
    }
    
    private void init() {
        
        this.setCursor(Cursor.HAND);
        rect = new Rectangle();
        rect.setArcWidth(boxSize/3);
        rect.setArcHeight(boxSize/3);
        rect.setWidth(boxSize);
        rect.setHeight(boxSize);
        soundClass.soundProperty().addListener(new ChangeListener<Sound>() {
            public void changed(ObservableValue<? extends Sound> observable, Sound oldValue, Sound newValue) {
                if (classifiedSoundClass == null && !muted) {
                    rect.setFill(newValue.getColor());
                    newValue.colorProperty().addListener(new ChangeListener<Paint>() {
                        public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                            rect.setFill(newValue);
                        }
                    });
                }
            }
        });
        soundClass.getSound().colorProperty().addListener(new ChangeListener<Paint>() {
            public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                rect.setFill(newValue);
            }
        });
        rect.setStroke(Color.BLACK);
        getChildren().add(rect);
        rect.setFill(soundClass.getSound().getColor());
        
        rctFlash = new Rectangle();
        rctFlash.setArcWidth(boxSize/3);
        rctFlash.setArcHeight(boxSize/3);
        rctFlash.setWidth(boxSize);
        rctFlash.setHeight(boxSize);
        rctFlash.setFill(Color.WHITE);
        rctFlash.setOpacity(0.0);
        rctFlash.setEffect(new Glow(1.0));
        getChildren().add(rctFlash);
        
        //setting up flashing
        tmlFlash = new Timeline();
        tmlFlash.setAutoReverse(false);

        tmlFlash.getKeyFrames().add(
                new KeyFrame(Duration.ZERO,
                new KeyValue(rctFlash.opacityProperty(), 0.0)));
        tmlFlash.getKeyFrames().add(
                new KeyFrame(Duration.millis(50),
                new KeyValue(rctFlash.opacityProperty(), 1.0)));
        tmlFlash.getKeyFrames().add(
                new KeyFrame(Duration.millis(400),
                new KeyValue(rctFlash.opacityProperty(), 0.0)));
        
        
        imgX = new ImageView(new Image(System.getProperty("user.dir") + "\\images\\x.png"));
        imgX.setPreserveRatio(true);
        imgX.setFitWidth(boxSize);
        imgX.setVisible(false);
        getChildren().add(imgX);
        rctFlash.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (deleteable)
                    imgX.setVisible(true);
            }
        });
        rctFlash.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (deleteable)    
                    imgX.setVisible(false);
            }
        });
    }
    
    /**
     * 
     * @effect starts displaying this example with the color of sc
     * @param sc soundclass to display this example as, can be different from
     * it's declared soundclass.
     */
    public void setClassifiedClass(SoundClass sc) {
        if (sndListen != null) {
            classifiedSoundClass.soundProperty().removeListener(sndListen);
            classifiedSoundClass.getSound().colorProperty().removeListener(pntListen);
        }
        classifiedSoundClass = sc;
        rect.setFill(sc.getSound().getColor());
        sndListen = new ChangeListener<Sound>() {
            public void changed(ObservableValue<? extends Sound> observable, Sound oldValue, Sound newValue) {
                oldValue.colorProperty().removeListener(pntListen);
                pntListen = new ChangeListener<Paint>() {
                        public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                            rect.setFill(newValue);
                        }
                };
                newValue.colorProperty().addListener(pntListen);
                rect.setFill(newValue.getColor());
            }
        };
        pntListen = new ChangeListener<Paint>() {
            public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                rect.setFill(newValue);
            }
        };
        classifiedSoundClass.soundProperty().addListener(sndListen);
        classifiedSoundClass.getSound().colorProperty().addListener(pntListen);
    }
    
    //returns the id specifically for using with chuck example playback
    public int getChuckId() {
        return chuckId;
    }
    
    public void setChuckId(int num) {
        chuckId = num;
    }
    
    /**
     * 
     * @param size resize
     */
    public void setSize(double size) {
        this.boxSize = (int) size;
        
        rect.setArcWidth(boxSize/3);
        rect.setArcHeight(boxSize/3);
        rect.setWidth(boxSize);
        rect.setHeight(boxSize);
        
        rctFlash.setArcWidth(boxSize/3);
        rctFlash.setArcHeight(boxSize/3);
        rctFlash.setWidth(boxSize);
        rctFlash.setHeight(boxSize);
        
        imgX.setFitWidth(boxSize);
    }
    
    //serialize the object
    private void writeObject(ObjectOutputStream oos) {
        try {
            oos.writeObject(exampleId);
            oos.writeObject(chuckId);
            oos.writeObject(soundClass.getClassValue());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    //make it flash
    public void flash() {
        tmlFlash.playFromStart();
    }
    
    /**
     * Make this box appear selected
     * @param isSelected 
     */
    public void setSelected(boolean isSelected) {
        this.selected = isSelected;
        if (isSelected) {
            rect.setFill(Color.WHITE);
        } else {
            rect.setFill(soundClass.getSound().getColor());
        }
    }
    
    public boolean isSelected() {
        return selected;
    }

    //deserialize the object
    private void readObject(ObjectInputStream ois){
        try {
            exampleId = ((Integer)ois.readObject());
            chuckId = (Integer)ois.readObject();
            soundClass = SoundClass.getFromClass((Integer)ois.readObject());
            classifiedSoundClass = soundClass;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        
        init();
    }

    int getExampleId() {
        return exampleId;
    }

    void setDeleteable(boolean deleteable) {
        this.deleteable = deleteable;
    }
    
    SoundClass getSoundClass() {
        return soundClass;
    }
    
    /**
     * 
     * @return return an exact copy of this example
     */
    Example copyOf() {
        return new Example(soundClass,exampleId,boxSize);
    }

    SoundClass getClassifiedClass() {
        return classifiedSoundClass;
    }

    //Determine whether the example should be displayed as muted
    void setMuted(boolean b) {
        muted = b;
        if (b) {
            rect.setFill(Color.GRAY);
        } else {
            rect.setFill(soundClass.getSound().getColor());
        }
    }
}
