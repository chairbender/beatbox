/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import javafx.beans.value.ObservableValue;
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import KyleWrapper.*;
import java.lang.UnsupportedOperationException;
import beatbox.Granularity.GranularityType;
import beatbox.LooperStates.LooperState;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;

/** Node for displaying a loop and channel controls
 * (delete, volume, etc...). Note that deleteAction (what
 * occurs when the delete button is pressed) must be specified
 * by the creator.
 * @author Kyle
 */
public class LoopChannel extends Parent implements ClassificationListener {
    //These variables control how the loop is displayed

    private int loopWidth = 400;
    private int loopHeight = 100;
    private int loopLayoutX;
    private int loopLayoutY;
    private int bpm;
    private int loopChannelId;
    private static int loopChannelIdIncrementer = 0;
    private boolean alreadyRecorded;
    //TODO: redundant fields here and in loop
    private ObjectProperty<GranularityType> quantization = new SimpleObjectProperty<GranularityType>(GranularityType.QUARTER_NOTE);
    private final Rectangle rctBorder;

    public ObjectProperty<GranularityType> quantizationProperty() {
        return quantization;
    }
    private Loop loop;
    private LooperState state;
    //width and height of this LoopChannel
    private int width;
    private int height;
    private Button delBtn;
    private Slider volSlider;
    private final int CONTROL_WIDTH = 120;
    private DeleteButtonHandler deleteAction;
    private TimingReceivedHandler update;
    private PadPane parentPane;

    public LoopChannel(PadPane parentPane, LoopManager lm, int width, int height) {
        super();
        this.parentPane = parentPane;
        this.width = width;
        this.height = height;
        update = new TimingReceivedHandler() {

            public void onReceived(final HitTiming ht) {
                Platform.runLater(new Runnable() {

                    public void run() {
                        if (state == LooperState.RECORDING) {
                            loop.addTiming(ht);
                        }
                    }
                });
            }
        };
        loopWidth = width - CONTROL_WIDTH;
        loopHeight = height;
        loopLayoutX = CONTROL_WIDTH;
        loopChannelId = loopChannelIdIncrementer++;

        rctBorder = new Rectangle();
        rctBorder.setWidth(width);
        rctBorder.setHeight(height);
        rctBorder.setFill(Color.TRANSPARENT);
        rctBorder.setStroke(Color.BLACK);
        getChildren().add(rctBorder);
        
        Rectangle rctControlBorder = new Rectangle();
        rctControlBorder.setFill(Color.WHITE);
        rctControlBorder.setStroke(Color.BLACK);
        rctControlBorder.setWidth(CONTROL_WIDTH);
        rctControlBorder.setHeight(height);
        getChildren().add(rctControlBorder);

        delBtn = new Button();
        delBtn.setText("X");
        delBtn.setOnAction(new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
                WekinatorBeatboxLogger.logTrackDeleted(loopChannelId);
                deleteAction.handle(event);
            }
        });
        delBtn.setLayoutX(CONTROL_WIDTH / 2 - 15);
        delBtn.setLayoutY(height / 2 - 10);
        getChildren().add(delBtn);

        volSlider = new Slider();
        volSlider.setMin(0);
        volSlider.setMax(1);
        volSlider.setValue(.25);
        volSlider.setScaleX(.75);
        volSlider.setLayoutX(CONTROL_WIDTH / 2 - 70);
        volSlider.setLayoutY(height / 2 + delBtn.getHeight() + 10);

        getChildren().add(volSlider);

        loop = new Loop(parentPane, lm, loopWidth, loopHeight);
        loop.setLayoutX(loopLayoutX);
        loop.setLayoutY(loopLayoutY);
        loop.setBpm(bpm);
        Bindings.bindBidirectional(quantization, loop.quantizationProperty());
        Bindings.bindBidirectional(loop.volumeProperty(), volSlider.valueProperty());
        loop.volumeProperty().set(volSlider.valueProperty().get());
        getChildren().add(loop);
        
        loop.boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                rctBorder.setWidth(newValue.getWidth() + CONTROL_WIDTH);
            }
        });

        state = LooperState.IDLE;
    }

    //start scrolling the underlying loop.
    //Does NOT start playing audio in chuck
    public void startPlaying() {
        state = LooperState.PLAYING;
        WekinatorBeatboxLogger.logTrackPlayed(loopChannelId);
        loop.startScrolling(false);
    }

    //stop scrolling the loop
    public void stopPlaying() {
        state = LooperState.IDLE;
        loop.stopScrolling();
    }

    public Loop getLoop() {
        return loop;
    }

    //return true if this has any notes
    public boolean hasNotes() {
        return loop.getNotes().size() > 0;
    }

    //Start recording classified hits to the loop and scrolling the loop
    //at the specified bpm and playing the metronome;
    public void startRecord() {
        WekinatorBeatboxLogger.logTrackRecordingStarted(loopChannelId);
        
        loop.reset();
        //subscribe to timingListener
        ChuckBoard.timingListener.addTimingReceivedEventHandler(update);

        //subscribe to wekinator classification
        WekinatorSingleton.getWekinator().addClassificationListener(this);
        state = LooperState.RECORDING;

        //ChuckBoard.playMetronomeAndMark(bpm);
        loop.startScrolling(true);
    }

    //Stop recording and scrolling and clicking, add the result of the recording to
    //chuck.
    public void finishRecord() {
        //log stuff
        int num = loop.getNotes().size();
        double beatPositions[] = new double[num];
        int beatClassesDetected[] = new int[num];
        int i = 0;
        for (HitNote hn : loop.getNotes()) {
            beatPositions[i] = hn.getPosition();
            beatClassesDetected[i] = hn.getClassValue();
            i++;
        }
        WekinatorBeatboxLogger.logTrackRecordingStopped(loopChannelId, num, beatPositions, beatClassesDetected);
        state = LooperState.IDLE;
        //signal wekinator to stop classifying
        ChuckBoard.stopMetronome();
        loop.stopScrolling();
        WekinatorSingleton.getWekinator().removeClassificationListener(this);
        ChuckBoard.timingListener.removeTimingReceivedEventHandler(update);
        alreadyRecorded = true;
    }

    //Stop recording and scrolling and clicking, don't add the result to chuck
    public void stopRecord() {
        //log stuff
        int num = loop.getNotes().size();
        double beatPositions[] = new double[num];
        int beatClassesDetected[] = new int[num];
        int i = 0;
        for (HitNote hn : loop.getNotes()) {
            beatPositions[i] = hn.getPosition();
            beatClassesDetected[i] = hn.getClassValue();
            i++;
        }
        WekinatorBeatboxLogger.logTrackRecordingStopped(loopChannelId, num, beatPositions, beatClassesDetected);
        state = LooperState.IDLE;
        //signal wekinator to stop classifying
        ChuckBoard.stopMetronome();
        loop.stopScrolling();
    }

    @Override
    public void fireClassificationResult(final int arg0, final int arg1) {
        Platform.runLater(new Runnable() {
            public void run() {
                if (state == LooperState.RECORDING) {
                    if (SoundClass.getFromClass(arg1) != null)
                        loop.addHit(new Hit(arg0, SoundClass.getFromClass(arg1)));
                }
            }
        });
    }

    //change the bpm this loop plays at, but doesn't update
    //itself in chuck (call refreshAllLoops to do that)
    public void setBpm(int bpm) {
        loop.setBpm(bpm);
    }

    /**
     * 
     * @param quant quantization to use for this loopchannel
     */
    void setQuantization(GranularityType quant) {
        this.quantization.set(quant);
    }

    /**
     * 
     * @param deleteButtonHandler code to execute when delete button is pressed
     */
    void setDeleteAction(DeleteButtonHandler deleteButtonHandler) {
        this.deleteAction = deleteButtonHandler;
    }

    /**
     * runs the delete action for this LoopChannel (specified by
     * setDeleteAction)
     */
    void delete() {
        deleteAction.handle(null);
    }

    /**
     * @return the LooperState of this loopChannel
     */
    LooperState getState() {
        return state;
    }
}
