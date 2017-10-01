/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.shape.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import beatbox.Granularity.GranularityType;
import beatbox.LoopChannel;
import beatbox.LooperStates.LooperState;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

/** Loop Manager version 2, for all your loop managing needs
 *
 * @author Kyle
 */
public class LoopManager extends Parent{
    private LoopManager me = this;
    private static final int MENU_HEIGHT = 90;
    private PauseTransition countInTransition;
    private int height;
    private int width;
    private boolean isTesting; //if true, only can record one loop at a time
    //TODO: private onRecordFinished: function(); //what to do when a loop was recorded
    private String enteredCountIn;
    private IntegerProperty countIn = new SimpleIntegerProperty(4);
    private String enteredBpm;
    private IntegerProperty bpm = new SimpleIntegerProperty(100);
    private ChoiceBox choiceBox;
    private TextField tempoBox;
    protected VBox loops;
    private LooperState state = LooperState.IDLE;
    private CheckBox recBtn;
    private CheckBox playBtn;
    private TextField recordBox;
    //private ScrollPane s;
    private Rectangle r;
    private Rectangle r1;
    private Rectangle rctBackground;
    //private final Rectangle rctScrollBack;
    private HitNoteAdapter tmpNote;
    private int tmpInd;
    private Point2D tmpPt;
    private Rectangle rctDim;
    private boolean dimmed;
    private final FadeTransition ftOut;
    private final FadeTransition ftIn;
    private final ImageView imgMouseRight;

    public LoopManager(final int width, final int height) {
        super();
        this.width = width;
        this.height = height;

        countInTransition = new PauseTransition();
        bpm.addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                for (Node n : loops.getChildren()) {
                    LoopChannel lc = (LoopChannel)n;
                    lc.setBpm(bpm.get());
                }
                countInTransition.setDuration(Duration.millis(1.0 / bpm.get() * 60 * countIn.get() * 1000));
            }
        });
        countIn.addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                countInTransition.setDuration(Duration.millis(1.0 / bpm.get() * 60 * countIn.get() * 1000));
            }
        });
        countInTransition.setOnFinished(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (state == LooperState.RECORDING || state == LooperState.RECORDING_AND_PLAYING) {
                    ((LoopChannel)loops.getChildren().get(loops.getChildren().size() - 1)).startRecord();
                    if (state == LooperState.RECORDING) {
                        playAllLoops();
                    }
                    countInTransition.stop();
                }
            }
        });

        this.setFocusTraversable(true);
        
//        rctBackground = new Rectangle();
//        rctBackground.setFill(Color.rgb(97,97,97));
//        rctBackground.setWidth(width + 14);
//        rctBackground.setHeight(height - MENU_HEIGHT + 14);
//        rctBackground.setLayoutY(MENU_HEIGHT - 7 + 10);
//        rctBackground.setTranslateX(-7);
//        getChildren().add(rctBackground);
//
//        r = new Rectangle();
//        r.setWidth(width);
//        r.setHeight(height);
//        r.setFill(Color.TRANSPARENT);
//        r.setStrokeWidth(.5);
//        this.getChildren().add(r);
//
//        r1 = new Rectangle();
//        r1.setWidth(width);
//        r1.setHeight(MENU_HEIGHT);
//        r1.setFill(Color.TRANSPARENT);
//        r1.setStrokeWidth(.5);
//        getChildren().add(r1);

        final FlowPane f = new FlowPane();
        f.setHgap(10);
        f.setPadding(new Insets(5, 5, 5, 5));
        f.setPrefWrapLength(width);
        f.setAlignment(Pos.CENTER);
        
        final FlowPane fpBottom = new FlowPane();
        fpBottom.setHgap(10);
        fpBottom.setPadding(new Insets(5,5,5,5));
        fpBottom.setPrefWrapLength(width);
        fpBottom.setAlignment(Pos.TOP_LEFT);
        
        loops = new VBox();
        getChildren().add(loops);
        loops.setLayoutY(MENU_HEIGHT+10);
        
        fpBottom.setLayoutY(loops.getBoundsInLocal().getHeight() + MENU_HEIGHT+15);
        
        final Rectangle rctBack = new Rectangle();
        rctBack.setFill(Color.rgb(117,118,117));
        rctBack.setLayoutX(loops.getLayoutX()-5);
        rctBack.setLayoutY(loops.getLayoutY()-5);
        rctBack.setWidth(Math.max(me.width+10,loops.getBoundsInLocal().getWidth()+10));
        rctBack.setHeight(loops.getBoundsInLocal().getHeight()+10);
        getChildren().add(0,rctBack);
        
        loops.boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                fpBottom.setLayoutY(loops.getBoundsInLocal().getHeight() + MENU_HEIGHT+15);
                rctBack.setWidth(Math.max(me.width+10,loops.getBoundsInLocal().getWidth()+10));
                rctBack.setHeight(loops.getBoundsInLocal().getHeight()+10);
            }
        });
        
        getChildren().add(f);
        getChildren().add(fpBottom);
        
        recBtn = new CheckBox();
        recBtn.setSkin(new RecordButtonSkin(recBtn, 80));
        recBtn.setOnMousePressed(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                recordClickHandler();
            }
        });
        recBtn.setCursor(Cursor.HAND);
        //EffectHelper.glowOver(recBtn);
        f.getChildren().add(recBtn);

        playBtn = new CheckBox();
        playBtn.setSkin(new PlayButtonSkin(playBtn, 45));
        playBtn.setOnMousePressed(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                playClickHandler();
            }
        });
        playBtn.setCursor(Cursor.HAND);
        f.getChildren().add(playBtn);

        Text tx1 = new Text("Record Count-in");
        tx1.setFill(Color.WHITE);
        fpBottom.getChildren().add(tx1);

        recordBox = new TextField();
        recordBox.setFocusTraversable(true);
        recordBox.setText("4");
        recordBox.setPrefColumnCount(2);
        recordBox.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                me.requestFocus();
            }
        });
        recordBox.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                int res = 0;
                try {
                    res = Integer.parseInt(recordBox.getText());
                } catch (Exception ex) {
                    recordBox.setText(oldValue);
                }
                if (res != 0) {
                    countIn.set(res);
                }
            }
        });
        recordBox.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                System.out.println("focus");
                recordBox.requestFocus();
            }
        });
        recordBox.setPrefWidth(20);
        recordBox.setEditable(true);
        recordBox.setDisable(false);
        fpBottom.getChildren().add(recordBox);

        Text tx2 = new Text("Tempo (bpm):");
        tx2.setFill(Color.WHITE);
        fpBottom.getChildren().add(tx2);

        tempoBox = new TextField();
        tempoBox.setText("100");
        tempoBox.setFocusTraversable(true);
        tempoBox.setPrefColumnCount(3);
        tempoBox.setPrefWidth(50);
        tempoBox.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                me.requestFocus();
            }
        });
        tempoBox.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                int res = 0;
                try {
                    res = Integer.parseInt(tempoBox.getText());
                } catch (Exception ex) {
                    tempoBox.setText(oldValue);
                }
                if (res != 0) {
                    bpm.set(res);
                }
            }
        });
        tempoBox.setEditable(true);
        tempoBox.setDisable(false);
        fpBottom.getChildren().add(tempoBox);

        Text tx3 = new Text("Lock to:");
        tx3.setFill(Color.WHITE);
        fpBottom.getChildren().add(tx3);

        choiceBox = new ChoiceBox();
        choiceBox.setFocusTraversable(true);
        choiceBox.getItems().add(new Granularity(GranularityType.QUARTER_NOTE));
        choiceBox.getItems().add(new Granularity(GranularityType.EIGTH_NOTE));
        choiceBox.getItems().add(new Granularity(GranularityType.SIXTEENTH_NOTE));
        choiceBox.getSelectionModel().select(0);

        fpBottom.getChildren().add(choiceBox);
                
        this.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.R) {
                    recordClickHandler();
                } else if (event.getCode() == KeyCode.SPACE) {
                    playClickHandler();
                }
            }
        });
        
                
        this.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                me.requestFocus();
            }
        });
        
        bpm.set(100);
        countIn.set(3);
        countIn.set(4);
        
        //mouse help icon
        imgMouseRight = new ImageView(new Image(System.getProperty("user.dir") + "\\images\\mouse_help_right.png"));
        imgMouseRight.setSmooth(true);
        imgMouseRight.setPreserveRatio(true);
        imgMouseRight.setLayoutY(loops.getLayoutY()-33);
        imgMouseRight.setLayoutX(width - 80);
        imgMouseRight.setOpacity(0.0);
        getChildren().add(imgMouseRight);
        
        ftOut = new FadeTransition(Duration.millis(500), imgMouseRight);
        ftOut.setFromValue(1.0);
        ftOut.setToValue(0.0);
        
        ftIn = new FadeTransition(Duration.millis(500), imgMouseRight);
        ftIn.setFromValue(0.0);
        ftIn.setToValue(1.0);
    }

    //record a new loop in a new loopchannel
    private void recordNewLoop() {
        //always stop the loops when  recording a new loop.
        stopAllLoops();
        playBtn.setSelected(false);
        if (isTesting && (loops.getChildren().size() > 0)) {
            loops.getChildren().remove(loops.getChildren().get(0));
        }

        if (state == LooperState.PLAYING) {
            state = LooperState.RECORDING_AND_PLAYING;
        } else {
            state = LooperState.RECORDING;
        }

        final LoopChannel newLoop = new LoopChannel(BeatBoxNew.getActivePadPane(),me,width, 100);
        newLoop.setLayoutY(100);
        newLoop.setBpm(bpm.get());
        newLoop.setQuantization(((Granularity) (choiceBox.getSelectionModel().getSelectedItem())).getGranularityType());
        newLoop.setDeleteAction(new DeleteButtonHandler(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (state == LooperState.RECORDING || state == LooperState.RECORDING_AND_PLAYING) {
                    //just don't add it to chuck
                    if (newLoop == loops.getChildren().get(loops.getChildren().size() - 1)) {
                        newLoop.stopRecord();
                        loops.getChildren().remove(newLoop);
                    }
                } else {
                    newLoop.stopPlaying();
                    loops.getChildren().remove(newLoop);
                }

                if (loops.getChildren().size() == 0) {
                    recBtn.setSelected(false);
                    choiceBox.setDisable(false);
                    tempoBox.setEditable(true);
                    playBtn.setSelected(false);
                    state = LooperState.IDLE;
                }
            }
        }));
        //bind to the choicebox for quantization

        loops.getChildren().add(loops.getChildren().size(), newLoop);
        //start metronome
        ChuckBoard.playMetronomeAndMark(bpm.get(), countIn.get());
        countInTransition.playFromStart();
    }

    //Stop recording the new loop
    private void stopRecord() {
        countInTransition.stop();
        ChuckBoard.stopMetronome();
        ((LoopChannel)loops.getChildren().get(loops.getChildren().size() - 1)).finishRecord();
        if (!((LoopChannel)loops.getChildren().get(loops.getChildren().size() - 1)).hasNotes()) {
            ((LoopChannel)loops.getChildren().get(loops.getChildren().size() - 1)).delete();
        }
        if (state == LooperState.RECORDING_AND_PLAYING) {
            state = LooperState.PLAYING;
            //onRecordFinished();
            //restart playing the loops
            stopAllLoops();
            playAllLoops();
        } else {
            state = LooperState.IDLE;
        }
    }

    //event handler for recording
    private void recordClickHandler() {
        if (BeatBoxNew.isRecordReady()) {
            if ((!recBtn.isSelected()) && (state == LooperState.IDLE || state == LooperState.PLAYING)) {
                recBtn.setSelected(true);
                recordBox.setEditable(false);
                recordBox.setDisable(true);
                tempoBox.setEditable(false);
                choiceBox.setDisable(true);
                recordNewLoop();
            } else if (state == LooperState.RECORDING || state == LooperState.RECORDING_AND_PLAYING) {
                recBtn.setSelected(false);
                choiceBox.setDisable(false);
                tempoBox.setEditable(true);
                recordBox.setEditable(true);
                recordBox.setDisable(false);
                stopRecord();
            }
        }
    }

    //event handler for recording
    private void playClickHandler() {
        if (!playBtn.isSelected() && state == LooperState.IDLE && loops.getChildren().size() > 0) {
            playBtn.setSelected(true);
            tempoBox.setEditable(false);
            recordBox.setEditable(false);
            recordBox.setDisable(true);
            choiceBox.setDisable(true);
            playAllLoops();
        } else if (state == LooperState.PLAYING) {
            playBtn.setSelected(false);
            choiceBox.setDisable(false);
            tempoBox.setEditable(true);
            recordBox.setEditable(true);
            recordBox.setDisable(false);
            stopAllLoops();
        } else if (state == LooperState.RECORDING_AND_PLAYING) {
            recBtn.setSelected(false);
            choiceBox.setDisable(false);
            tempoBox.setEditable(true);
            recordBox.setEditable(true);
            recordBox.setDisable(false);
            stopRecord();
            playBtn.setSelected(false);
            choiceBox.setDisable(false);
            tempoBox.setEditable(true);
            recordBox.setEditable(true);
            recordBox.setDisable(false);
            stopAllLoops();
        }
    }

//Start playing all the loops in the LoopManager
    private void playAllLoops() {
        if (state == LooperState.RECORDING) {
            playBtn.setSelected(true);
            state = LooperState.RECORDING_AND_PLAYING;
            for (Node n : loops.getChildren()) {
                LoopChannel loop = (LoopChannel)n;
                if (loop.getState() != LooperState.RECORDING) {
                    loop.startPlaying();
                }
            }
        } else {
            state = LooperState.PLAYING;
            for (Node n : loops.getChildren()) {
                LoopChannel loop = (LoopChannel)n;
                if (loop.getState() == LooperState.IDLE) {
                    loop.startPlaying();
                }
            }
        }
    }

//Stop playing all the loops in the LoopManager
    private void stopAllLoops() {
        if (state == LooperState.RECORDING_AND_PLAYING) {
            for (Node n : loops.getChildren()) {
                LoopChannel loop = (LoopChannel)n;
                if (loop.getState() == LooperState.PLAYING) {
                    loop.stopPlaying();
                }
            }
            state = LooperState.RECORDING;
            recBtn.setSelected(false);
            stopRecord();
        } else {
            state = LooperState.IDLE;
        }

        for (Node n : loops.getChildren()) {
            LoopChannel loop = (LoopChannel)n;
            if (loop.getState() == LooperState.PLAYING) {
                loop.stopPlaying();
            }
        }
    }
    
        
    public void fadeInHelp() {
        ftIn.setFromValue(imgMouseRight.getOpacity());
        ftIn.playFromStart();
    }
    
    public void fadeOutHelp() {
        ftOut.setFromValue(imgMouseRight.getOpacity());
        ftOut.playFromStart();
    }

    void setWidth(int width) {
        this.width = width;
        resizeAll();
    }
    
    private void resizeAll() {
        r.setWidth(width);
        r.setHeight(height);

        r1.setWidth(width);
        r1.setHeight(MENU_HEIGHT);
    }
    
    void setHeight(int height) {
        this.height = height;
        resizeAll();
    }
    
    //invisible lightbox the hitnote and its PadPicker
    public void focusHitNote(final HitNoteAdapter hna, final Loop l) {
//        
//        
//        tmpNote = hna;
//        tmpPt = hna.getParent().localToScene(hna.getLayoutX(),hna.getLayoutY());
//        final EventHandler<? super MouseEvent> ev = hna.getOnMouseClicked();
//        hna.setOnMouseClicked(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                ev.handle(event);
//                tmpNote.setPickerVisible(false);
//                getChildren().remove(rctDim);
//                getChildren().remove(tmpNote);
//                //readd the note
//                l.addNote(hna.getHitNote());
//                dimmed = false;
//            }
//        });
//        
//        rctDim = new Rectangle();
//        rctDim.setLayoutX(-this.getLayoutX());
//        rctDim.setLayoutY(-this.getLayoutY());
//        rctDim.setCursor(Cursor.HAND);
//        rctDim.setWidth(getScene().getWidth());
//        rctDim.setHeight(getScene().getHeight());
//        rctDim.setFill(Color.rgb(0, 0, 0,0));
//        rctDim.setOnMouseClicked(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                tmpNote.setPickerVisible(false);
//                getChildren().remove(rctDim);
//                getChildren().remove(tmpNote);
//                //readd the note
//                l.addNote(hna.getHitNote());
//                dimmed = false;
//            }
//        });
//        getChildren().add(rctDim);
//        getChildren().add(tmpNote);
//        tmpNote.setLayoutX(me.sceneToLocal(tmpPt).getX());
//        tmpNote.setLayoutY(me.sceneToLocal(tmpPt).getY());
//        dimmed = true;
    }
    
    public boolean isHitNoteFocused() {
        return dimmed;
    }
}

