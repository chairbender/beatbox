package beatbox;

///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package beatbox;
//
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javafx.beans.value.ObservableValue;
//import javafx.scene.*;
//import javafx.scene.shape.*;
//import javafx.scene.paint.Color;
//import javafx.scene.input.*;
//import javafx.scene.layout.*;
//import javafx.animation.*;
//import javafx.scene.control.*;
//import javafx.scene.text.Font;
//import javafx.scene.image.*;
//import javafx.geometry.*;
//import java.lang.Math;
//import KyleWrapper.*;
//import KyleWrapper.BeatboxWekinatorWrapper.RecordingState;
//import KyleWrapper.BeatboxWekinatorWrapper.RunningState;
//import KyleWrapper.BeatboxWekinatorWrapper.TrainingState;
//import java.lang.Exception;
//import javafx.scene.transform.Transform;
//import javafx.scene.text.Text;
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//import javafx.application.Platform;
//import javafx.beans.binding.Bindings;
//import javafx.beans.binding.BooleanBinding;
//import javafx.beans.property.*;
//import javafx.beans.value.ChangeListener;
//import javafx.event.*;
//import javafx.scene.paint.Paint;
//
///**
// * A container of training examples for a single sound class
// *
// * On construction, adds itself as a TrainingExampleListener to
// * the wekinator
// * @author Kyle
// */
//public class SampleHolder extends Parent implements TrainingExampleListener {
//    /*Constructor fields*/
//
//    SampleHolder me = this;
//    private int boxSize = 15; //size of the sampleBoxes
//    private int prefWidth = 100; //width you want it to be
//    private int prefHeight = 80; //height you want it to be
//    private SoundClass soundClass; //soundClass this sampleHolder is for,
//    //cannot be null
//    private String name; //name of this sample
//    private Tooltip tooltip;
//    private static final int MENU_HEIGHT = 30;
//    private static final int SCROLL_BAR_SPACE = 10;
//    private static final int ICON_SPACE = 10;
//    //private FlowPane samples; //the current samples in the holder
//    private Rectangle rctSelectBox;
//    private BooleanProperty dragging = new SimpleBooleanProperty(false);
//    private Rectangle boundRect;
//    private Set<SampleBox> selectedSamples;
//    private int colorOscillator = 0;
//    private int exampleIdInc = 0;
//    private BooleanProperty record = new SimpleBooleanProperty(false);
//    private Text txtName;
//    private Text txtTrain;
//    private CheckBox btnRecord;
//    private Button btnDelete;
//    private FlowPane samplePane;
//    private float vol;
//    private Text txtGain;
//    private Slider gain;
//    private ColorPicker colorPicker;
//
//    public SampleHolder(SoundClass sc, String name, int width, int height) {
//        super();
//        tooltip = new Tooltip("Click here to start/stop teaching beatbox a noise to trigger the sound with.\n"
//                + "An example will be detected whenever the input crosses the threshold."); //tooltip for the record button
//        this.prefWidth = width;
//        this.prefHeight = height;
//        this.name = name;
//        this.soundClass = sc;
//
//        //bind the rectangle's visibility to the dragging flag
//        Rectangle r = new Rectangle();
//        r.setFill(Color.YELLOW);
//        r.setStroke(Color.BLACK);
//        r.setOpacity(.5);
//
//
//        btnDelete = new Button();
//        btnDelete.setText("X");
//        btnDelete.setTooltip(new Tooltip("Click here to remove this sound."));
//        btnDelete.setLayoutX(prefWidth - 30);
//        btnDelete.setLayoutY(prefHeight - 23);
//
//        btnRecord = new CheckBox();
//        btnRecord.setSkin(new AddButtonSkin(btnRecord, MENU_HEIGHT));
//        btnRecord.setLayoutY(prefHeight - MENU_HEIGHT);
//        btnRecord.onMousePressedProperty().set(new EventHandler<MouseEvent>() {
//
//            public void handle(MouseEvent event) {
//                if (!(WekinatorSingleton.getWekinator().getRunningState() == RunningState.RUNNING)) {
//                    if (!btnRecord.isSelected()) {
//                        if (!(WekinatorSingleton.getWekinator().getRecordingState() == RecordingState.RECORDING)) {
//                            btnRecord.setSelected(true);
//                            startRecord();
//                        }
//                    } else {
//                        btnRecord.setSelected(false);
//                        stopRecord();
//                    }
//                }
//            }
//        });
//
//
//        txtTrain = new Text();
//        txtTrain.setLayoutY(btnRecord.getLayoutY() + MENU_HEIGHT);
//        txtTrain.setFont(new Font(MENU_HEIGHT));
//        txtTrain.setLayoutX(btnRecord.getLayoutX() + MENU_HEIGHT + 5);
//        txtTrain.setTextOrigin(VPos.BOTTOM);
//        txtTrain.setText("Make a " + name + " noise...");
//        Bindings.bindBidirectional(txtTrain.visibleProperty(), record);
//
//        txtName = new Text();
//        txtName.setFont(new Font(30));
//        txtName.setText(name);
//        txtName.setLayoutY((prefHeight + MENU_HEIGHT) / 2 - txtName.getBoundsInLocal().getHeight() / 2);
//        txtName.setLayoutX(prefWidth);
//        txtName.setOnMouseEntered(new EventHandler<MouseEvent>() {
//
//            public void handle(MouseEvent event) {
//                ChuckBoard.play(soundClass);
//                getScene().setCursor(Cursor.HAND);
//            }
//        });
//        txtName.setOnMouseExited(new EventHandler<MouseEvent>() {
//
//            public void handle(MouseEvent event) {
//                getScene().setCursor(Cursor.DEFAULT);
//            }
//        });
//        txtName.setOnMousePressed(new EventHandler<MouseEvent>() {
//
//            public void handle(MouseEvent event) {
//                ChuckBoard.play(soundClass);
//            }
//        });
//
//        txtGain = new Text();
//        txtGain.setText("Volume:");
//        txtGain.fontProperty().set(new Font(15));
//        txtGain.layoutXProperty().set(txtName.layoutXProperty().get() + 3);
//        txtGain.layoutYProperty().set(txtName.layoutYProperty().get()
//                + txtName.boundsInLocalProperty().get().getHeight() - 5);
//
//
//        gain = new Slider();
//        gain.setLayoutX(txtName.getLayoutX() + 3);
//        gain.setLayoutY(txtGain.layoutYProperty().get() + 10);
//        gain.setMin(0);
//        gain.setMax(1);
//        gain.setValue(.5);
//        gain.onMouseReleasedProperty().set(new EventHandler<MouseEvent>() {
//
//            public void handle(MouseEvent event) {
//                ChuckBoard.setClassVolume(soundClass.getClassValue(), gain.getValue());
//                ChuckBoard.play(soundClass);
//            }
//        });
//
//        colorPicker = new ColorPicker(null);
//        colorPicker.setLayoutX(txtName.getLayoutX() + txtName.getBoundsInLocal().getWidth() + 5);
//        colorPicker.setLayoutY(txtName.getLayoutY() - 15);
//
//        Group g1 = new Group();
//        g1.setFocusTraversable(true);
//
//        Group g2 = new Group();
//        Rectangle rTemp = new Rectangle();
//        rTemp.setHeight(height);
//        rTemp.setWidth(width);
//        rTemp.setFill(Color.WHITE);
//
//        g2.getChildren().add(rTemp);
//        g2.getChildren().add(btnDelete);
//        g2.getChildren().add(btnRecord);
//        g2.getChildren().add(txtName);
//        g2.getChildren().add(txtTrain);
//
//        g1.getChildren().add(g2);
//        g1.getChildren().add(colorPicker);
//        g1.getChildren().add(gain);
//        g1.getChildren().add(txtGain);
//
//        samplePane = new FlowPane();
//        samplePane.setPrefWrapLength(width - SCROLL_BAR_SPACE);
//        samplePane.setVgap(6);
//        samplePane.setHgap(8);
//        samplePane.setPadding(new Insets(5, 5, 5, 5));
//
//
//        ScrollPane scv = new ScrollPane();
//        scv.setPrefWidth(width);
//        scv.setPrefHeight(height - MENU_HEIGHT);
//        scv.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//        scv.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//
//        scv.setContent(samplePane);
//        g1.getChildren().add(scv);
//
//        getChildren().add(g1);
//
//        rctSelectBox = new Rectangle();
//        rctSelectBox.setFill(Color.color(.8, .8, .5, .2));
//        rctSelectBox.setStroke(Color.BLACK);
//        rctSelectBox.setVisible(false);
//        g1.getChildren().add(rctSelectBox);
//        scv.setOnMouseDragged(new EventHandler<MouseEvent>() {
//
//            private double dragOriginX;
//            private double dragOriginY;
//
//            public void handle(MouseEvent event) {
//                if (!dragging.get() && event.isPrimaryButtonDown()) {
//                    dragging.set(true);
//                    rctSelectBox.setVisible(true);
//                    dragOriginX = event.getX();
//                    dragOriginY = event.getY();
//                    rctSelectBox.setX(dragOriginX);
//                    rctSelectBox.setY(dragOriginY);
//                } else {
//                    if (event.getX() > dragOriginX) {
//                        rctSelectBox.setX(dragOriginX);
//                        rctSelectBox.setWidth(event.getX() - dragOriginX);
//                    } else {
//                        rctSelectBox.setX(event.getX());
//                        rctSelectBox.setWidth(dragOriginX - event.getX());
//                    }
//                    if (event.getY() > dragOriginY) {
//                        rctSelectBox.setY(dragOriginY);
//                        rctSelectBox.setHeight(event.getY() - dragOriginY);
//                    } else {
//                        rctSelectBox.setY(event.getY());
//                        rctSelectBox.setHeight(dragOriginY - event.getY());
//                    }
//                }
//
//                for (Node n : samplePane.getChildren()) {
//                    SampleBox sb = (SampleBox) n;
//                    if (rctSelectBox.localToScene(
//                            rctSelectBox.getBoundsInLocal()).intersects(sb.localToScene(
//                            sb.getBoundsInLocal()))) {
//                        sb.setSelected(true);
//                    } else {
//                        sb.setSelected(false);
//                    }
//                }
//            }
//        });
//
//        scv.setOnMouseReleased(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                dragging.set(false);
//                rctSelectBox.setVisible(false);
//                me.requestFocus();
//            }
//        });
//
//        this.setOnKeyPressed(new EventHandler<KeyEvent>() {
//            public void handle(KeyEvent event) {
//                if (event.getCode() == KeyCode.DELETE) {
//                    List<SampleBox> delList = new ArrayList<SampleBox>();
//                    for (Node n : samplePane.getChildren()) {
//                        SampleBox sb = (SampleBox) n;
//                        if (sb.isSelected()) {
//                            delList.add(sb);
//                        }
//                    }
//                    for (SampleBox sb : delList) {
//                        samplePane.getChildren().remove(sb);
//                        if (!samplePane.getChildren().isEmpty()) {
//                            WekinatorSingleton.getWekinator().deleteTrainingExample(sb.getExampleId());
//                            WekinatorSingleton.getWekinator().startTraining();
//                        }
//                        
//                    }
//                }
//            }
//        });
//
//        colorPicker.getSelectedColorProperty().addListener(new ChangeListener<Paint>() {
//
//            public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
//                soundClass.setColor((Color) newValue);
//            }
//        });
//
//        soundClass.setName(name);
//        soundClass.setColor(Color.RED);
//
//        ChuckBoard.addSound(soundClass.getSoundPath(), soundClass.getClassValue());
//    }
//
//    //requires: no other SampleHolder is in training mode
//    //effects: begins recording and recieving osc messages from chuck.
//    //Adds a sample every time wekinator recieves an osc feature vector
//    private void startRecord() {
//        WekinatorSingleton.getWekinator().stopRunning();
//        WekinatorSingleton.getWekinator().addTrainingExampleListener(this);
//        try {
//            WekinatorSingleton.getWekinator().startRecordingExamples();
//        } catch (Exception ex) {
//            Logger.getLogger(SampleHolder.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        WekinatorSingleton.getWekinator().setTrainingClassValue(soundClass.getClassValue());
//        ChuckBoard.setChuckTrainingMode(true);
//        record.set(true);
//    }
//
//    //effects: stops recording and trains the model
//    public void stopRecord() {
//        WekinatorSingleton.getWekinator().removeTrainingExampleListener(this);
//        WekinatorSingleton.getWekinator().stopRecordingExamples();
//        if (samplePane.getChildren().size() - 1 > 0) {
//            WekinatorSingleton.getWekinator().startTraining();
//        }
//
//        ChuckBoard.setChuckTrainingMode(false);
//        record.set(false);
//    }
//
//    public boolean isRecording() {
//        return record.get();
//    }
//
//    /**
//     * Adds a SampleBox to the end of the list whenever wekinator
//     * gets a feature vector
//     */
//    public void fireTrainingExampleRecorded(final int id, final int classValue) {
//        Platform.runLater(new Runnable() {
//
//            public void run() {
//                if (classValue == soundClass.getClassValue()) {
//                    final SampleBox s = new SampleBox(soundClass, id, boxSize);
//                    s.setOnMouseClicked(new EventHandler<MouseEvent>() {
//
//                        public void handle(MouseEvent event) {
//                            samplePane.getChildren().remove(s);
//                            if (!samplePane.getChildren().isEmpty()) {
//                                WekinatorSingleton.getWekinator().deleteTrainingExample(s.getExampleId());
//                                WekinatorSingleton.getWekinator().startTraining();
//                            }
//                        }
//                    });
//                    samplePane.getChildren().add(s);
//                }
//            }
//        });
//    }
//
//    //had to expose this for testing,
//    //don't use it unless you're testing
//    public List<SampleBox> getSamples() {
//        List<SampleBox> res = new ArrayList<SampleBox>();
//        for (Node n : samplePane.getChildren()) {
//            res.add((SampleBox) n);
//        }
//        return res;
//    }
//
//    //clears all this SampleHolder's samples and adds all the examples in
//    //the passed arraylist
//    public void setSamplesFromArrayList(ArrayList lst) {
//        samplePane.getChildren().setAll(lst);
//    }
//
//    //serialize the object
//    public void writeObject(ObjectOutputStream oos) {
//        try {
//            oos.writeObject(name);
//            oos.writeObject(soundClass.getClassValue());
//            oos.writeObject(soundClass.getColor().getRed());
//            oos.writeObject(soundClass.getColor().getGreen());
//            oos.writeObject(soundClass.getColor().getBlue());
//            oos.writeObject(soundClass.getSoundPath());
//            oos.writeObject(samplePane.getChildren().size() - 1);
//            for (Node smp : samplePane.getChildren()) {
//                ((SampleBox) smp).writeObject(oos);
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    //deserialize the object
//    public void readObject(ObjectInputStream ois) {
//        try {
//            String name = ((String) ois.readObject());
//            int cv = (Integer) ois.readObject();
//            int r = ((Integer) ois.readObject()) * 255;
//            int g = ((Integer) ois.readObject()) * 255;
//            int b = ((Integer) ois.readObject()) * 255;
//            Color c = Color.rgb(r, g, b);
//            /*TODO: colorPicker = ColorPicker {
//            layoutX: bind nameText.layoutX + nameText.boundsInLocal.width + 5;
//            layoutY: bind nameText.layoutY - 15;
//            color: c;
//            blocksMouse: true;
//            }*/
//
//            String p = (String) ois.readObject();
//            soundClass = SoundClass.getSoundClass(cv, c, p);
//            int size = (Integer) ois.readObject();
//            for (int i = 0; i < size; i++) {
//                /*TODO: var smp = SampleBox { boxSize: boxSize; color: bind colorPicker.color};
//                smp.readObject(ois);
//                insert smp into samples;*/
//            }
//        } //show the tooltip
//        /*TODO: public void showTooltips() {
//        tooltip.activate();
//        }*/ catch (IOException ex) {
//            Logger.getLogger(SampleHolder.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(SampleHolder.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    void setOnDelete(EventHandler<ActionEvent> onDelete) {
//        btnDelete.setOnAction(new DeleteButtonHandler(onDelete));
//    }
//}
