package beatbox;

///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package beatbox;
//
//import KyleWrapper.BeatboxWekinatorWrapper.RecordingState;
//import KyleWrapper.BeatboxWekinatorWrapper.TrainingState;
//import javafx.beans.value.ObservableValue;
//import javafx.scene.Group;
//import javafx.scene.text.Text;
//import javafx.scene.control.CheckBox;
//import KyleWrapper.ClassificationListener;
//import java.lang.UnsupportedOperationException;
//import javafx.beans.value.ChangeListener;
//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
//import javafx.scene.Parent;
//import javafx.scene.text.Font;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.control.Tooltip;
//
///**
// * Audition button that plays back the sound whenever a classification occurs
// * @author Kyle
// */
//public class ClassifierTester extends Parent implements ClassificationListener {
//    //private EventHandler<ActionEvent> onRecordFinished; //what to do when record finished
//    private CheckHandler checkBeforeRecord; //function to call before record is initiated. if true, starts, otherwise, doesn't
//    private boolean testing = false;
//    private CheckBox cb;
//    private Text txtText;
//    private boolean fx; 
//    
//    public ClassifierTester() {
//        WekinatorSingleton.getWekinator().addClassificationListener(this);
//        
//        this.focusedProperty().addListener(new ChangeListener<Boolean>() {
//            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                if (!newValue) {
//                    txtText.setText("Push to begin testing beatbox's sound detection");
//                    WekinatorSingleton.getWekinator().stopRunning();
//                    cb.setSelected(false);
//                    //onRecordFinished.handle(null);
//                }
//            }
//        });
//        
//        cb = new CheckBox();
//        cb.setSkin(new RecordButtonSkin(cb,40));
//        cb.setOnMousePressed(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                requestFocus();
//                if ((checkBeforeRecord == null || checkBeforeRecord.check()) && 
//                        !(WekinatorSingleton.getWekinator().getRecordingState() == RecordingState.RECORDING)) {
//                    if (cb.isSelected()) {
//                        txtText.setText("Push to befin testing beatbox's sound detection");
//                        WekinatorSingleton.getWekinator().stopRunning();
//                        cb.setSelected(false);
//                        //onRecordFinished.handle(null);
//                    } else {
//                        cb.setSelected(true);
//                        txtText.setText("Sound detected:");
//                        WekinatorSingleton.getWekinator().startRunning();
//                    }
//                }
//            }
//        });
//        getChildren().add(cb);
//        
//        txtText = new Text();
//        txtText.setLayoutX(cb.getBoundsInLocal().getWidth() + cb.getLayoutX() + 5);
//        txtText.setLayoutY(cb.getLayoutY() + cb.getBoundsInLocal().getHeight() / 2 + txtText.getBoundsInLocal().getHeight()/2);
//        txtText.setFont(new Font(18));
//        txtText.setText("Push to begin testing beatbox's sound detection");
//        getChildren().add(txtText);
//        
//    }
//    
//    @Override
//    public void fireClassificationResult(int id, int classValue) {
//        if (cb.isSelected()) {
//            txtText.setText("Sound detected: " + SoundClass.getFromClass(classValue).getName());
//            ChuckBoard.play(SoundClass.getFromClass(classValue));
//        }
//    }
//
//}
