/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/** A pane that holds sample pads
 *
 * @author Kyle
 */

public class PadPane extends Parent implements Serializable{
//    private static List<Sound> lst = new ArrayList<Sound>();
//    private static List<ChangeListener<? super Sound>> listeners;
    
    private PadPane me = this;
    private FlowPane fpPads;
    private int tmpInd;
    private double width, height;
    private SampleHolderPad tmpPad;
    private Point2D tmpPt;
    private boolean dimmed;
    private Rectangle rctDim;
    private Rectangle rctSelect;
    private AddButton addButton;
    private StringProperty name;
    private BooleanProperty dragging;
    private ImageView imgMouseRight;
    private FadeTransition ftOut;
    private FadeTransition ftIn;
    private Rectangle rctSamplePadHolder;
    public StringProperty nameProperty() {
        return name;
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public String getName() {
        return name.get();
    }
    
    public PadPane(double width, double height) {
        this.width = width;
        this.height = height;
        name = new SimpleStringProperty("Default");
        dragging = new SimpleBooleanProperty(false);
        
        initialize();
    }
    
    //initialize using current fields
    private void initialize() {

//        ScrollPane scp = new ScrollPane();
//        scp.setPrefWidth(width);
//        scp.setPrefHeight(height);
//        scp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//        scp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
                
        //mouse help icon
        imgMouseRight = new ImageView(new Image(System.getProperty("user.dir") + "\\images\\mouse_help_right.png"));
        imgMouseRight.setSmooth(true);
        imgMouseRight.setPreserveRatio(true);
        imgMouseRight.setLayoutX(width + 5);
        imgMouseRight.setOpacity(0.0);
        getChildren().add(imgMouseRight);

        fpPads = new FlowPane();
        
        me.setOnMouseDragged(new EventHandler<MouseEvent>() {
            private double dragOriginX;
            private double dragOriginY;

            public void handle(MouseEvent event) {
                if (event.getX() < 5 || event.getX() > width-5
                        || event.getY() < 5 || event.getY() > fpPads.getBoundsInLocal().getHeight()-10) {
                    dragging.set(false);
                    rctSelect.setVisible(false);
                    me.requestFocus();
                    return;
                }
                
                if (!dragging.get() && event.isPrimaryButtonDown()) {
                    for (Node n : fpPads.getChildren()) {
                        SampleHolderPad shp = (SampleHolderPad) n;
                        if (shp.localToScene(shp.getBoundsInLocal()).
                                contains(event.getSceneX(),event.getSceneY()))
                                return;
                    }

                    dragging.set(true);
                    rctSelect.setVisible(true);
                    dragOriginX = event.getX();
                    dragOriginY = event.getY();
                    rctSelect.setX(dragOriginX);
                    rctSelect.setY(dragOriginY);
                } else {
                    if (event.getX() > dragOriginX) {
                        rctSelect.setX(dragOriginX);
                        rctSelect.setWidth(event.getX() - dragOriginX);
                    } else {
                        rctSelect.setX(event.getX());
                        rctSelect.setWidth(dragOriginX - event.getX());
                    }
                    if (event.getY() > dragOriginY) {
                        rctSelect.setY(dragOriginY);
                        rctSelect.setHeight(event.getY() - dragOriginY);
                    } else {
                        rctSelect.setY(event.getY());
                        rctSelect.setHeight(dragOriginY - event.getY());
                    }
                }

                for (Node n : fpPads.getChildren()) {
                    SampleHolderPad shp = (SampleHolderPad) n;
                    if (rctSelect.localToScene(
                            rctSelect.getBoundsInLocal()).intersects(shp.localToScene(
                            shp.getBoundsInLocal()))) {
                        shp.setSelected(true);
                    } else {
                        shp.setSelected(false);
                    }
                } 
            } 
        });

        me.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                dragging.set(false);
                rctSelect.setVisible(false);
                me.requestFocus();
            }
        });
        me.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (event.isStillSincePress()) {
                    for (Node n : fpPads.getChildren()) {
                        if (n instanceof SampleHolderPad) {
                            if (((SampleHolderPad)n).contains(event.getX(),event.getY()))
                                return;
                        }
                    }
                    for (Node n : fpPads.getChildren()) {
                        if (n instanceof SampleHolderPad) {
                            ((SampleHolderPad)n).setSelected(false);
                        }
                    }
                }
            }
        });
        
        fpPads.setOrientation(Orientation.HORIZONTAL);
        fpPads.setPrefWrapLength(width);
        fpPads.setHgap(20);
        fpPads.setVgap(18);
        fpPads.setPadding(new Insets(8,8,8,8));

        me.getChildren().add(fpPads);
        
        addButton = new AddButton(20,false);
        getChildren().add(addButton);
        addButton.setCursor(Cursor.HAND);
        addButton.setLayoutX(5);
        addButton.setLayoutY(fpPads.getBoundsInLocal().getHeight());
        addButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (SoundLibrary.getSounds().isEmpty()) {
                    Sound s = new Sound("Kick",
                                new File(System.getProperty("user.dir") + "\\sounds\\kick.wav"),Color.RED);
                    SoundLibrary.addSound(s);
                    try {
                        addPad(new SampleHolderPad(SoundClass.getSoundClass(SoundClass.getNextSoundClassValue(),
                                SoundLibrary.randomSound()),80,me));
                    } catch (Exception ex) {
                        Logger.getLogger(PadPane.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        addPad(new SampleHolderPad(SoundClass.getSoundClass(SoundClass.getNextSoundClassValue(),
                                SoundLibrary.randomSound()),80,me));
                    } catch (Exception ex) {
                        Logger.getLogger(PadPane.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        
        fpPads.boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                addButton.setLayoutY(fpPads.getBoundsInLocal().getHeight());
            }
        });
        
        rctSelect = new Rectangle();
        rctSelect.setFill(Color.rgb(20, 60, 60, .2));
        rctSelect.setVisible(false);
        getChildren().add(rctSelect);
        
        ftOut = new FadeTransition(Duration.millis(500), imgMouseRight);
        ftOut.setFromValue(1.0);
        ftOut.setToValue(0.0);
        
        ftIn = new FadeTransition(Duration.millis(500), imgMouseRight);
        ftIn.setFromValue(0.0);
        ftIn.setToValue(1.0);
    }
    
    /**
     * Adds the pad to the PadPane
     * @param pad 
     */
    public void addPad(final SampleHolderPad pad) {
        fpPads.getChildren().add(pad);
        pad.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    fpPads.getChildren().remove(pad);
                    WekinatorSingleton.getWekinator().removeClassificationListener(pad);
                    //WekinatorSingleton.getWekinator().deleteTrainingExamples(pad.getExampleIds());
                    try {
                        BeatBoxNew.trainOnAllPads();
                        BeatBoxNew.logPads();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        pad.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                fadeInHelp();

            }
        });
        pad.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                fadeOutHelp();
            }
        });
        pad.setParentPane(me);
    }
    
    /**
     * Dim all pads but the passed one
     * @requires pad is in the PadPane
     * @param pad 
     * @param alpha alpha channel for the transparency overlay
     */
    public void dimAllButPad(SampleHolderPad pad,double d) {
        tmpPad = pad;
        tmpInd = fpPads.getChildren().indexOf(pad);
        tmpPt = fpPads.localToScene(pad.getLayoutX(),pad.getLayoutY());
        fpPads.getChildren().remove(pad);
        //placeholder to prevent layout movement
        rctSamplePadHolder = new Rectangle();
        rctSamplePadHolder.setWidth(pad.getSize());
        rctSamplePadHolder.setHeight(pad.getSize());
        rctSamplePadHolder.setArcHeight(40);
        rctSamplePadHolder.setArcWidth(40);
        rctSamplePadHolder.setFill(Color.rgb(97,97,97));
        rctSamplePadHolder.setVisible(false);
        DropShadow fx = new DropShadow();
        fx.setOffsetX(1);
        fx.setOffsetY(1);
        fx.setRadius(1);
        rctSamplePadHolder.setEffect(fx);
        fpPads.getChildren().add(tmpInd, rctSamplePadHolder);
        
        dimmed = true;
        
        //now give the parent the pad
        BeatBoxNew.dimAllButSamplePad(pad, tmpPt, d);

    }
    
    private void fadeInHelp() {
        ftIn.setFromValue(imgMouseRight.getOpacity());
        ftIn.playFromStart();
    }
    
    private void fadeOutHelp() {
        ftOut.setFromValue(imgMouseRight.getOpacity());
        ftOut.playFromStart();
    }
    
    /**
     * @return the collection of pads in this PadPane
     */
    public Collection<SampleHolderPad> getPads() {
        Set<SampleHolderPad> s = new HashSet<SampleHolderPad>();
        for (Node n : fpPads.getChildren()) {
            if (n instanceof SampleHolderPad)
                s.add((SampleHolderPad)n);
        }
        if (dimmed)
            s.add(tmpPad);
        return s;
    }

    void clearPads() {
        fpPads.getChildren().clear();
    }

    List<? extends SampleHolderPad> getSelectedPads() {
        List<SampleHolderPad> res = new ArrayList<SampleHolderPad>();
        
        for (Node n : fpPads.getChildren()) {
            if (n instanceof SampleHolderPad) {
                if (((SampleHolderPad)n).isSelected())
                    res.add(((SampleHolderPad)n).copyOf());
            }
        }
        return res;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeDouble(width);
        out.writeDouble(height);
        out.writeObject(name.get());
        
        //save all sampleholders
        out.writeInt(fpPads.getChildren().size());
        for (Node n : fpPads.getChildren()) {
            if (n instanceof SampleHolderPad) {
                SampleHolderPad p = (SampleHolderPad)n;
                out.writeObject(p);
            }
        }
        
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        width = in.readDouble();
        height = in.readDouble();
        name = new SimpleStringProperty((String)in.readObject());
        dragging = new SimpleBooleanProperty(false);
        me = this;
        
        initialize();
        
        //load sampleHolders
        int num = in.readInt();
        for (int i = 0; i < num; i++) {
            SampleHolderPad p = (SampleHolderPad)in.readObject();
            addPad(p);
        }
    }

    void finishDim(SampleHolderPad tmpPad) {
        //puts this pad back in its place
        fpPads.getChildren().add(fpPads.getChildren().indexOf(rctSamplePadHolder), 
                tmpPad);
        fpPads.getChildren().remove(rctSamplePadHolder);
        tmpPad.unfocus();
    }
    
}
