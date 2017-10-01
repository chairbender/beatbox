package beatbox;

///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package beatbox;
//
//import beatbox.ChuckBoard;
//import javafx.beans.value.ObservableValue;
//import javafx.scene.*;
//import javafx.scene.shape.*;
//import com.illposed.osc.*;
//import javafx.beans.binding.Bindings;
//import javafx.beans.property.*;
//import javafx.beans.value.ChangeListener;
//import javafx.event.EventHandler;
//import javafx.scene.paint.*;
//import javafx.scene.input.*;
//
//
//import javafx.scene.control.Slider;
//import javafx.scene.image.ImageView;
//import javafx.scene.image.Image;
//
///**Monitors input, which it receives through OSC
// * @requires: inputMonitor is chucked
// * @author Kyle
// */
////TODO: scale up the maxAdc if it gets exceeded a lot
//public class InputMonitor extends Parent{
//    InputMonitor me = this;
//    private int preferredWidth = 100;
//    private int preferredHeight = 20;
//    private DoubleProperty threshold = new SimpleDoubleProperty(0.0); 
//    public DoubleProperty thresholdProperty() { return threshold; }
//    private DoubleProperty maxAdc = new SimpleDoubleProperty(.15);
//            
//    private ImageView arrow;
//    
//    private Line line; 
//
//    public InputMonitor(int preferredWidth) {
//        super();
//        this.preferredWidth = preferredWidth;
//        Rectangle r = new Rectangle();
//        r.setWidth(preferredWidth);
//        r.setHeight(preferredHeight);
//        r.setFill(Color.TRANSPARENT);
//        r.setStroke(Color.BLACK);
//        getChildren().add(r);
//        
//        final Rectangle r2 = new Rectangle();
//        final int prefW = preferredWidth;
//        ChuckBoard.ADC.lastProperty().addListener(new ChangeListener<Number>() {
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                if (newValue.doubleValue() > maxAdc.get()) {
//                    maxAdc.set(newValue.doubleValue()+.025);
//                    threshold.set(threshold.get());
//                }
//                r2.setWidth((int)(newValue.doubleValue()/maxAdc.get()*prefW));
//            }
//        });
//        r2.setFill(Color.RED);
//        r2.setHeight(preferredHeight);
//        getChildren().add(r2);
//        
//        arrow = new ImageView();
//        arrow.setImage(new Image(System.getProperty("user.dir") + "\\images\\arrow.png"));
//        threshold.addListener(new ChangeListener<Number>() {
//            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                if (newValue.doubleValue() > maxAdc.get())
//                    threshold.set(maxAdc.get());
//                else if (newValue.doubleValue() < 0)
//                    threshold.set(0);
//                arrow.setLayoutX((threshold.get()/maxAdc.get())*me.preferredWidth - (arrow.getBoundsInLocal().getWidth()/2));
//                line.setStartX(threshold.get() / maxAdc.get() * me.preferredWidth);
//            }
//        });
//
//        arrow.setLayoutY(-arrow.getBoundsInLocal().getHeight()/2);
//        arrow.setFitWidth(50);
//        arrow.setPreserveRatio(true);
//        arrow.setOnMouseEntered(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                arrow.setCursor(Cursor.HAND);
//            }
//        });
//        arrow.setOnMouseDragged(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                threshold.set((me.sceneToLocal(event.getSceneX(), 0).getX()/me.preferredWidth) * maxAdc.get());
//            }
//        });
//        getChildren().add(arrow);
//        
//        line = new Line();
//        Bindings.bindBidirectional(line.startXProperty(), line.endXProperty());
//        line.setEndY(preferredHeight);
//        getChildren().add(line);
//    }
//
//    double getPreferredWidth() {
//        return preferredWidth;
//    }
//}
