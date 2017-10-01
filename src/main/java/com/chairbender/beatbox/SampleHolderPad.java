/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.chairbender.beatbox.wekinator.ClassificationListener;
import com.chairbender.beatbox.wekinator.TrainingExampleListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.scene.paint.Color;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.image.*;
import javafx.geometry.*;
import java.lang.Math;
import java.lang.Exception;
import javafx.scene.text.Text;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.*;
import javafx.scene.effect.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * A container of training examples for a single sound class
 *
 * On construction, adds itself as a TrainingExampleListener to
 * the wekinator
 * @author Kyle
 */
public class SampleHolderPad extends Parent implements TrainingExampleListener, ClassificationListener, Serializable {
    /*Constructor fields*/

    private SampleHolderPad me = this;
    private SoundClass soundClass;
    private Rectangle rctSamplePad;
    private Rectangle rctSamplePadFlash;
    private ImageView imgIcon;
    private Text txtName;
    private double size;
    private Rectangle rctHighlight;
    private FlowPane flpExamples;
    private Rectangle clipRect;
    private Text txtEdit;
    private Group grpFocused;
    private SoundPicker snpPick;
    private boolean picking = false;
    private Timeline flashTimeline;
    private Double opacity = 0.0;
    private Text txtExample;
    private boolean boxFocused = false;
    private boolean examplesDeleteable;
    private Rectangle bg;
    private boolean selected;
    private PadPane parentPane;
    private Text txtRecord;
    private Text txtRecord2;
    private EventHandler<ActionEvent> evnDelete;
    private boolean muted;
    private Knob knbVolume;
    private CheckBox cbMute;
    private boolean emph = false;
    private Text txtError;
    private int padId;
    private static int padIdIncrementer;

    public SampleHolderPad(SoundClass sc, double size, PadPane parent) {
        super();
        parentPane = parent;
        this.soundClass = sc;
        this.size = size;
        padId = padIdIncrementer++;

        initialize();
    }

    private void initialize() {
        //WekinatorBeatboxLogger.padCreated(soundClass.getClassValue(), "noname", soundClass.getSound().getName());
        this.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
//                BeatBoxNew.clickMade(me);
            }
        });

        WekinatorSingleton.getWekinator().addClassificationListener(me);
        WekinatorSingleton.getWekinator().addTrainingExampleListener(me);

        rctSamplePad = new Rectangle();
        rctSamplePad.setWidth(size);
        rctSamplePad.setHeight(size);
        rctSamplePad.setArcHeight(40);
        rctSamplePad.setArcWidth(40);
        rctSamplePad.setFill(Color.rgb(117,118,117));
        DropShadow fx = new DropShadow();
        fx.setOffsetX(1);
        fx.setOffsetY(1);
        fx.setRadius(1);
        rctSamplePad.setEffect(fx);
        rctSamplePad.setOnMousePressed(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (!muted) {
                    me.setCursor(Cursor.CLOSED_HAND);
                    ChuckBoard.play(soundClass, 1.0);
                    flash();
                }
            }
        });

        cbMute = new CheckBox();
        cbMute.setSkin(new MuteToggleButtonSkin(cbMute, 10));
        cbMute.setLayoutX(size / 2 - 5);
        cbMute.setLayoutY(size + 5);
        cbMute.setCursor(Cursor.HAND);
        cbMute.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                cbMute.setSelected(!muted);
                toggleMute(!muted);
            }
        });
        getChildren().add(0,cbMute);

        rctSamplePad.setOnDragDetected(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (!muted) {
                    Dragboard db = startDragAndDrop(TransferMode.COPY);
                    ClipboardContent c = new ClipboardContent();
                    c.putString("sample_pad");
                    db.setContent(c);

                    DragHelper.padsDragging = new ArrayList<SampleHolderPad>();
                    if (!me.selected) {
                        DragHelper.padsDragging.add(me.copyOf());
                    } else {
                        DragHelper.padsDragging.addAll(parentPane.getSelectedPads());
                    }

                    me.setCursor(Cursor.CLOSED_HAND);
                    event.consume();
                }
            }
        });

        rctSamplePad.setCursor(Cursor.HAND);


        rctSamplePadFlash = new Rectangle();
        rctSamplePadFlash.setWidth(size);
        rctSamplePadFlash.setHeight(size);
        rctSamplePadFlash.setArcHeight(40);
        rctSamplePadFlash.setArcWidth(40);
        rctSamplePadFlash.setFill(soundClass.getSound().getColor());
        DropShadow fx2 = new DropShadow();
        fx2.setOffsetX(1);
        fx2.setOffsetY(1);
        fx2.setRadius(1);
        rctSamplePadFlash.setEffect(fx2);
        
        final Rectangle rctIcon = new Rectangle();
        soundClass.soundProperty().addListener(new ChangeListener<Sound>() {

            public void changed(ObservableValue<? extends Sound> observable, Sound oldValue, Sound newValue) {
                rctSamplePadFlash.setFill(soundClass.getSound().getColor());
                BeatBoxNew.classifyAllExamples();
                newValue.colorProperty().addListener(new ChangeListener<Paint>() {
                    public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                        rctSamplePadFlash.setFill(soundClass.getSound().getColor());
                        rctIcon.setFill(soundClass.getSound().getColor());
                        BeatBoxNew.classifyAllExamples();
                    }
                });
            }
        });
        soundClass.getSound().colorProperty().addListener(new ChangeListener<Paint>() {
            public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                rctSamplePadFlash.setFill(soundClass.getSound().getColor());
                rctIcon.setFill(soundClass.getSound().getColor());
                BeatBoxNew.classifyAllExamples();
            }
        });
        getChildren().add(rctSamplePadFlash);
        getChildren().add(rctSamplePad);
        grpFocused = new Group();

        VolumeMeter vm = new VolumeMeter(15, size);
        vm.setLayoutX(size + 10);
        grpFocused.getChildren().add(vm);

        txtRecord = new Text();
        txtRecord.setText("Teach me what a " + soundClass.getSound().getName() + " sounds like");
        txtRecord.setFont(new Font(15));
        txtRecord.setFill(Color.WHITE);
        txtRecord.setLayoutY(-5);
        txtRecord.setLayoutX(size / 2 - txtRecord.getBoundsInLocal().getWidth() / 2);
        grpFocused.getChildren().add(txtRecord);

        txtRecord2 = new Text();
        txtRecord2.setText("Recording " + soundClass.getSound().getName() + " examples");
        txtRecord2.setFont(new Font(15));
        txtRecord2.setFill(Color.WHITE);
        txtRecord2.setLayoutY(size + size / 3);
        txtRecord2.setLayoutX(size / 2 - txtRecord2.getBoundsInLocal().getWidth() / 2);
        grpFocused.getChildren().add(txtRecord2);
        grpFocused.setVisible(false);
       

        getChildren().add(grpFocused);

        txtExample = new Text();
        txtExample.setFont(new Font(15));
        txtExample.setFill(Color.WHITE);
        txtExample.setLayoutY(txtRecord2.getLayoutY() + txtRecord2.getBoundsInLocal().getHeight() + 5);
        txtExample.textProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!muted) {
                    txtExample.setLayoutX(size / 2 - txtExample.getBoundsInLocal().getWidth() / 2);
                }
            }
        });
        txtExample.setVisible(false);
        grpFocused.getChildren().add(txtExample);
        
        txtError = new Text();
        txtError.setText("Your " + soundClass.getSound().getName() + " noise sounds like another pad's noise." +
                "\nTry deleting your examples and using " + "\na different noise for one of the pads.");
        txtError.setTextAlignment(TextAlignment.CENTER);
        txtError.setFont(new Font(15));
        txtError.setFill(Color.RED);
        txtError.setLayoutY(2*size);
        txtError.setLayoutX(size /2 - txtError.getBoundsInLocal().getWidth()/2);
        grpFocused.getChildren().add(txtError);
        txtError.setVisible(false);

        rctHighlight = new Rectangle();
        rctHighlight.setFill(Color.WHITE);
        rctHighlight.setLayoutX(size / 10);
        rctHighlight.setLayoutY(size / 10);
        rctHighlight.setWidth(this.size - size / 5);
        rctHighlight.setHeight(size / 5 + 5);
        rctHighlight.setVisible(false);
        getChildren().add(rctHighlight);

//        imgIcon = new ImageView(new Image(System.getProperty("user.dir") + "\\images\\diamond.png"));
//        imgIcon.setPreserveRatio(true);
//        imgIcon.setFitWidth(size/5);
//        imgIcon.setLayoutX(size/10);
//        imgIcon.setLayoutY(size/10);
//        ColorAdjust fx2 = new ColorAdjust();
//        fx2.setHue(-sc.getColor().getHue()/360.0); //out of 360
//        fx2.setSaturation(sc.getColor().getSaturation()); //out of 1.0
//        fx2.setBrightness(1-sc.getColor().getBrightness()); //out of 1.0, 1.0 = white, 0.0 = black
//        System.out.println("hsv: " + sc.getColor().getHue()/360.0 + " " + sc.getColor().getSaturation() + " " + (1-sc.getColor().getBrightness()));


        rctIcon.setLayoutX(size / 10);
        rctIcon.setLayoutY(size / 10);
        rctIcon.setWidth(size / 5);
        rctIcon.setHeight(size / 5);
        rctIcon.setCursor(Cursor.HAND);
        rctIcon.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                soundClass.getSound().colorProperty().set(Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
                BeatBoxNew.classifyAllExamples();
            }
        });
        rctIcon.setFill(soundClass.getSound().getColor());
        getChildren().add(rctIcon);

        BoxBlur fx3 = new BoxBlur();
        fx3.setHeight(2);
        //fx3.setInput(fx2);
        fx3.setWidth(2);
        rctIcon.setEffect(fx3);

        txtName = new Text();
        txtName.setText(soundClass.getSound().getName());
        Font f = null;
        try {
            f = Font.loadFont(new FileInputStream(System.getProperty("user.dir") + "\\fonts\\virtue.ttf"), 20);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SampleHolderPad.class.getName()).log(Level.SEVERE, null, ex);
        }
        txtName.setFont(f);
        txtName.setFill(Color.WHITE);
        txtName.setLayoutX(size / 7 + rctIcon.getWidth());
        txtName.setTextOrigin(VPos.CENTER);
        txtName.setLayoutY(rctIcon.getLayoutY() + rctIcon.getHeight()/2);
        txtName.setCursor(Cursor.HAND);

        snpPick = new SoundPicker();
        snpPick.setLayoutX(rctIcon.getLayoutX());
        snpPick.setLayoutY(txtName.getLayoutY());
        snpPick.setDisable(true);
        snpPick.setVisible(false);


        txtName.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (!muted) {
                    if (!picking) {
                        picking = true;
                        snpPick.setDisable(false);
                        snpPick.setVisible(true);
                        txtName.setFill(Color.BLACK);
                        snpPick.refresh();
                        rctHighlight.setVisible(true);
                        BeatBoxNew.focusSamplePad(me,0.0);
                    } else {
                        unfocus();
                    }
                }
            }
        });
        txtName.setOnMouseEntered(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (!picking && !muted) {
                    txtName.setFill(Color.BLACK);
                    rctHighlight.setVisible(true);
                }
            }
        });
        txtName.setOnMouseExited(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (!picking && !muted) {
                    txtName.setFill(Color.WHITE);
                    rctHighlight.setVisible(false);
                }
            }
        });
        while (txtName.getBoundsInLocal().getWidth() > (me.size / 2)) {
            try {
                txtName.setFont(Font.loadFont(new FileInputStream(System.getProperty("user.dir") + "\\fonts\\virtue.ttf"), txtName.getFont().getSize() - .5));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SampleHolderPad.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        getChildren().add(txtName);



        knbVolume = new Knob(size / 6.0,1.0);
        knbVolume.setLayoutX(size - size / 15 - size / 6);
        knbVolume.setLayoutY(size - size / 15 - size / 6);
        knbVolume.valueProperty().addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (!muted) {
                    ChuckBoard.setClassVolume(soundClass, newValue.doubleValue());
                }
            }
        });
        knbVolume.setOnMouseReleased(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (!muted) {
                    ChuckBoard.play(soundClass, 1.0);
                    me.flash();
                }
            }
        });
        ChuckBoard.setClassVolume(soundClass, knbVolume.valueProperty().doubleValue());
        getChildren().add(knbVolume);

        clipRect = new Rectangle();

        flpExamples = new FlowPane();
        flpExamples.setPrefWrapLength(size - size / 5);
        flpExamples.setClip(clipRect);
        flpExamples.setPadding(new Insets(5, 5, 5, 5));
        flpExamples.setLayoutX(size / 10);
        flpExamples.setLayoutY(2 * size / 5);
        flpExamples.setHgap(4);
        flpExamples.setVgap(4);
        bg = new Rectangle();
        bg.setFill(Color.rgb(120, 120, 120));
        InnerShadow is = new InnerShadow();
        bg.setEffect(is);
        bg.setLayoutX(flpExamples.getLayoutX());
        bg.setLayoutY(flpExamples.getLayoutY());

        Bindings.bindBidirectional(bg.widthProperty(), clipRect.widthProperty());
        Bindings.bindBidirectional(bg.heightProperty(), clipRect.heightProperty());
        //flpExamples.layout();
        flpExamples.heightProperty().addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (me.boxFocused && !muted) {
                    bg.setHeight(Math.max(bg.getHeight(), newValue.doubleValue()));
                }
                txtRecord2.setLayoutY(Math.max(txtRecord2.getLayoutY(), flpExamples.getLayoutY() + flpExamples.getHeight() + me.size / 5));
            }
        });

        //clipRect.setLayoutX(flpExamples.getLayoutX());
        //clipRect.setLayoutY(flpExamples.getLayoutY());
        clipRect.setWidth(size - size / 5);
        clipRect.setHeight(size / 6 + 10);
        bg.setCursor(Cursor.HAND);
        flpExamples.setCursor(Cursor.HAND);
        bg.setOnMouseEntered(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (!boxFocused & !muted) {
                    txtEdit.setVisible(true);
                }
            }
        });
        bg.setOnMouseExited(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (!boxFocused & !muted) {
                    txtEdit.setVisible(false);
                }
            }
        });
        bg.setOnMouseClicked(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (!boxFocused && !muted) {
                    //resize example
                    for (Node n : flpExamples.getChildren()) {
                        ((Example) n).setSize(10);
                    }
                    flpExamples.setHgap(4);
                    flpExamples.setVgap(4);
                    boxFocused = true;
                    cbMute.setDisable(true);
                    cbMute.setOpacity(0.5);
                    setExamplesDeletable(true);
                    me.parentPane.dimAllButPad(me,0.9);
                    clipRect.setHeight(Math.max(flpExamples.getHeight(), me.size - (2 * me.size) / 5));
                    grpFocused.setVisible(true);
                    grpFocused.setDisable(false);
                    flpExamples.setCursor(Cursor.DEFAULT);
                    bg.setCursor(Cursor.DEFAULT);
                    txtEdit.setVisible(false);
                    txtName.setDisable(true);
                    startRecord();
                }
            }
        });
        getChildren().add(bg);
        flpExamples.setOnMouseEntered(bg.getOnMouseEntered());
        flpExamples.setOnMouseExited(bg.getOnMouseExited());
        flpExamples.setOnMouseClicked(bg.getOnMouseClicked());


        //TODO: fix once font is fixed
        txtEdit = new Text();
        txtEdit.setFont(new Font(size / 8));
        txtEdit.setText("edit");
        txtEdit.setStroke(Color.rgb(235, 233, 77));
        txtEdit.setLayoutX(size / 2 - txtEdit.getBoundsInLocal().getWidth() / 2);
        txtEdit.setLayoutY(bg.getLayoutY() + bg.getHeight());
        txtEdit.setTextOrigin(VPos.TOP);
        txtEdit.setVisible(false);

        Rectangle rctEdit = new Rectangle();
        rctEdit.setLayoutX(txtEdit.getLayoutX());
        rctEdit.setLayoutY(txtEdit.getLayoutY());
        rctEdit.setWidth(txtEdit.getBoundsInLocal().getWidth());
        rctEdit.setHeight(txtEdit.getBoundsInLocal().getHeight());
        rctEdit.setFill(Color.rgb(67, 67, 67));
        Bindings.bindBidirectional(rctEdit.visibleProperty(), txtEdit.visibleProperty());
        getChildren().add(rctEdit);
        getChildren().add(txtEdit);
        grpFocused.setDisable(true);

        getChildren().add(flpExamples);
        getChildren().add(snpPick);

        evnDelete = new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
                snpPick.selectedSoundProperty().set(SoundLibrary.randomSound());
            }
        };

        snpPick.setOnMouseClicked(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (!muted) {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        BeatBoxNew.undim();
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        if (SoundLibrary.getSounds().size() > 1) {
                            SoundLibrary.removeSound(snpPick.selectedSoundProperty().get());
                        }
                    }
                }
            }
        });

        snpPick.selectedSoundProperty().addListener(new ChangeListener<Sound>() {
            public void changed(ObservableValue<? extends Sound> observable, Sound oldValue, Sound newValue) {
                //WekinatorBeatboxLogger.padFileChanged(soundClass.getClassValue(), "noname", newValue.getName());
                soundClass.setSound(newValue);

                txtName.setText(newValue.getName());
                Font f = null;
                try {
                    f = Font.loadFont(new FileInputStream(System.getProperty("user.dir") + "\\fonts\\virtue.ttf"), 20);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(SampleHolderPad.class.getName()).log(Level.SEVERE, null, ex);
                }
                txtName.setFont(f);

                txtRecord.setText("Teach me what a " + soundClass.getSound().getName() + " sounds like");
                txtRecord2.setText("Recording " + soundClass.getSound().getName() + " examples");
                txtError.setText("Your " + soundClass.getSound().getName() + " noise sounds like another pad's noise." +
                "\nTry deleting your examples and using " + "\na different noise for one of the pads.");

                while (txtName.getBoundsInLocal().getWidth() > (me.size / 2)) {
                    try {
                        txtName.setFont(Font.loadFont(new FileInputStream(System.getProperty("user.dir") + "\\fonts\\virtue.ttf"), txtName.getFont().getSize() - .5));
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(SampleHolderPad.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                rctIcon.setFill(newValue.getColor());
                newValue.addOnDeletedFromLibrary(evnDelete);
            }
        });

        //setting up flashing
        flashTimeline = new Timeline();
        flashTimeline.setAutoReverse(false);

        flashTimeline.getKeyFrames().add(
                new KeyFrame(Duration.ZERO,
                new KeyValue(rctSamplePad.opacityProperty(), 1.0)));
        flashTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(50),
                new KeyValue(rctSamplePad.opacityProperty(), 0.0)));
        flashTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(400),
                new KeyValue(rctSamplePad.opacityProperty(), 1.0)));
        
        flpExamples.setHgap(2);
        flpExamples.setVgap(2);
    }

    /**
     * Gets out of the focused in record mode and sound picking
     */
    public void unfocus() {
        //resize examples
        for (Node n : flpExamples.getChildren()) {
            ((Example) n).setSize(5);
        }
        flpExamples.setHgap(2);
        flpExamples.setVgap(2);
        boxFocused = false;
        cbMute.setDisable(false);
        cbMute.setOpacity(1.0);
        setExamplesDeletable(false);
        clipRect.setHeight(me.size / 6 + 10);
        grpFocused.setVisible(false);
        grpFocused.setDisable(true);
        flpExamples.setCursor(Cursor.HAND);
        bg.setCursor(Cursor.HAND);
        txtEdit.setVisible(false);
        picking = false;
        snpPick.setDisable(true);
        snpPick.setVisible(false);
        rctHighlight.setVisible(false);
        txtName.setDisable(false);
        txtName.setFill(Color.WHITE);
        stopRecord();
        
        BeatBoxNew.logPads();
    }

    //requires: no other SampleHolder is in training mode
    //effects: begins recording and recieving osc messages from chuck.
    //Adds a sample every time wekinator recieves an osc feature vector
    private void startRecord() {
        WekinatorSingleton.getWekinator().stopRunning();
        try {
            WekinatorSingleton.getWekinator().startRecordingExamples();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ChuckBoard.setChuckTrainingMode(true);
    }

    //effects: stops recording and trains the model
    public void stopRecord() {
        WekinatorSingleton.getWekinator().stopRecordingExamples();
        if (flpExamples.getChildren().size()> 0) {
            BeatBoxNew.trainOnAllPads();
        }
//SHOULDNT NEED TO DO THIS BEVAUSE OF ADD EXAMPLE
        ChuckBoard.setChuckTrainingMode(false);
        WekinatorSingleton.getWekinator().startRunning();
    }

    public SoundClass getSoundClass() {
        return soundClass;
    }

    public double getSize() {
        return size;
    }

    /**
     * toggles whether examples can be clicked on to be deleted
     * @param deleteable 
     */
    private void setExamplesDeletable(boolean deleteable) {
        for (Node n : flpExamples.getChildren()) {
            Example e = (Example) n;
            e.setDeleteable(deleteable);
        }
        this.examplesDeleteable = deleteable;
    }

    @Override
    public void fireTrainingExampleRecorded(final int id, int classValue) {
        Platform.runLater(new Runnable() {

            public void run() {
                if (!VolumeMeter.isMuted() && boxFocused && !muted) {
                    //WekinatorBeatboxLogger.logNewExampleRecorded(id, soundClass.getClassValue(), flpExamples.getChildren().size()+1);
                    addExample(id, 10);
                }
            }
        });
    }

    private void addExample(final Example e) {
        e.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (examplesDeleteable) {
                    flpExamples.getChildren().remove(e);
                    //WekinatorSingleton.getWekinator().deleteTrainingExample(e.getExampleId());
                    BeatBoxNew.trainOnAllPads();
                    BeatBoxNew.classifyAllExamples();
                    //WekinatorBeatboxLogger.logExampleDeleted(e.getExampleId(), soundClass.getClassValue(), flpExamples.getChildren().size());
                    BeatBoxNew.logPads();
                }
            }
        });
        e.setOnMouseEntered(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (e.getClassifiedClass() != null && e.getSoundClass().getClassValue() != e.getClassifiedClass().getClassValue()) {
                    txtExample.setText("This " + e.getSoundClass().getSound().getName()
                            + " sounds like a " + e.getClassifiedClass().getSound().getName());
                    txtExample.setVisible(true);
                }
                //WekinatorBeatboxLogger.logMouseOverExample(e.getExampleId(), soundClass.getClassValue());
                ChuckBoard.playExample(e);
            }
        });
        e.setOnMouseExited(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                txtExample.setVisible(false);
            }
        });
        flpExamples.getChildren().add(e);
        try {
            //doesn't work during loading unless wek has been loaded
            //TODO: Fix this so it works / find a replacement
            // WekinatorSingleton.getWekinator().addTrainingExample(soundClass.getClassValue(),e);
            BeatBoxNew.trainOnAllPads();
            BeatBoxNew.classifyAllExamples();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        BeatBoxNew.logPads();
    }
    
    private void addExample(final int id, double size) {
        addExample(new Example(soundClass, id, (int) size));
    }

    /**
     * 
     * @return the list of example ids in this pad
     */
    int[] getExampleIds() {
        if (!muted) {
            int[] res = new int[flpExamples.getChildren().size()];
            int i = 0;
            for (Node n : flpExamples.getChildren()) {
                res[i++] = ((Example) n).getExampleId();
            }
            return res;
        }
        return new int[0];
    }

    /**
     * 
     * @return return the class ids for each example Id. This will be
     * parallel with getExampleIds, assuming no examples have been added between calls
     */
    int[] getClassIds() {
        if (!muted) {
            int[] res = new int[flpExamples.getChildren().size()];
            int i = 0;
            for (Node n : flpExamples.getChildren()) {
                res[i++] = soundClass.getClassValue();
            }
            return res;
        }
        return new int[0];
    }

    @Override
    public double prefWidth(double height) {
        return size;
    }

    @Override
    public double prefHeight(double width) {
        return size;
    }

    public void fireClassificationResult(int id, final int classValue) {
        Platform.runLater(new Runnable() {

            public void run() {
                if (classValue == soundClass.getClassValue() && !muted && !VolumeMeter.isMuted()) {
                    flash();
                    ChuckBoard.play(soundClass, knbVolume.valueProperty().get());
                }
            }
        });
    }

    //Flash the pad the same color as its soundClass.
    private void flash() {
        flashTimeline.playFromStart();
    }

    /**
     * 
     * @return a copy of this, with the same soundClass but a new classValue
     */
    SampleHolderPad copyOf() {
        SoundClass s = null;
        try {
            s = SoundClass.getSoundClass(SoundClass.getNextSoundClassValue(), soundClass.getSound());
        } catch (Exception ex) {
            Logger.getLogger(SampleHolderPad.class.getName()).log(Level.SEVERE, null, ex);
        }
        SampleHolderPad res = new SampleHolderPad(s, (int) size, parentPane);
        res.setExamples(flpExamples.getChildren());

        res.clipRect.setHeight(res.size / 6);
        return res;
    }

    //The parent must be set to the padpane the sampleHolder is in for
    //the multiple drag to work
    public void setParentPane(PadPane newParent) {
        parentPane = newParent;
    }

    //Set current examples to examples
    private void setExamples(Collection<Node> examples) {
        for (Node n : examples) {
            if (n instanceof Example) {
                addExample(((Example) n).copyOf());
            }
        }
        boxFocused = false;
    }

    //sets whether to display the pad as selected
    void setSelected(boolean b) {
        selected = b;
        if (b) {
            rctSamplePad.setFill(Color.rgb(50, 50, 50));
        } else {
            rctSamplePad.setFill(Color.rgb(97, 97, 97));
        }
    }

    boolean isSelected() {
        return selected;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        //write the soundclass
        //TODO: if soundclass isn't in the library, add it
        out.writeInt(soundClass.getClassValue());
        out.writeObject(soundClass.getSound());
        out.writeInt(padId);

        //fields
        out.writeDouble(size);
        out.writeBoolean(muted);
        //write the examples
        out.writeInt(flpExamples.getChildren().size());
        for (Node n : flpExamples.getChildren()) {
            if (n instanceof Example) {
                Example e = (Example) n;
                out.writeObject(e);
            }
        }
    }
    
    public int getPadId() {
        return padId;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        soundClass = SoundClass.getSoundClass(in.readInt(), (Sound) in.readObject());
        padId = in.readInt();
        padIdIncrementer = Math.max(padId,padIdIncrementer) + 1;
        ChuckBoard.addSound(soundClass.getSound(), soundClass.getClassValue());
        size = in.readDouble();
        me = this;
        toggleMute(in.readBoolean());
        initialize();

        //add all examples
        int num = in.readInt();
        for (int i = 0; i < num; i++) {
            Example e = (Example)in.readObject();
            addExample(e);
            e.setSize(5);
        }

    }

    Set<Example> getExamples() {
        if (!muted) {
            Set<Example> res = new HashSet<Example>();
            for (Node n : flpExamples.getChildren()) {
                if (n instanceof Example) {
                    res.add((Example) n);
                }
            }
            return res;
        }
        return new HashSet<Example>();
    }

    private void toggleMute(boolean isMuted) {
        muted = isMuted;
        if (muted) {
            //WekinatorBeatboxLogger.logPadDisabled(soundClass.getClassValue(), "noname", soundClass.getSound().getName());
            me.setOpacity(.3);
        } else {
            //WekinatorBeatboxLogger.logPadEnabled(soundClass.getClassValue(), "noname", soundClass.getSound().getName());
            me.setOpacity(1);
        }
        if (flpExamples != null) {
            for (Node n : flpExamples.getChildren()) {
                if (n instanceof Example)
                    ((Example) n).setMuted(muted);
            }
        }
        BeatBoxNew.trainOnAllPads();
        BeatBoxNew.logPads();
    }
    
    public boolean isMuted() {
        return muted;
    }
    
    public double getVolume() {
        return knbVolume.valueProperty().doubleValue();
    }

    //put a red line around the pad, show
    //help text when focused
    void setEmphasize(boolean emphasized) {
        emph = emphasized;
        if (emph) {
            bg.setStroke(Color.RED);
            bg.setStrokeWidth(2.0);
            txtError.setVisible(true);
        } else {
            bg.setStroke(Color.BLACK);
            bg.setStrokeWidth(1.0);
            txtError.setVisible(false);
        }
    }
    
    boolean getEmphasize() {
        return emph;
    }
}
