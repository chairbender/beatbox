package com.chairbender.beatbox;

///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package beatbox;
//
//import java.lang.Math;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javafx.animation.*;
//import javafx.beans.value.ObservableValue;
//import javafx.scene.*;
//import javafx.scene.paint.*;
//import javafx.scene.shape.*;
//import javafx.scene.paint.Color;
//import javafx.scene.paint.RadialGradient;
//import javafx.scene.effect.*;
//import javafx.scene.input.MouseEvent;
//import java.io.*;
//import javafx.beans.binding.Bindings;
//import javafx.beans.value.ChangeListener;
//import javafx.event.EventHandler;
//import javafx.geometry.Rectangle2D;
//import javafx.scene.image.*;
//
///**
// *A ui element representing a single recorded example. Is the new version of SampleBox
// *
// * @author Kyle
// */
//public class SampleBox extends Parent{
//    /*Constructor fields*/
//    SampleBox me = this;
//    private boolean outline = false; //if it should be a placeholder-looking box
//    private int boxSize = 15;
//    private boolean selected;  //if the node is currently selected, displays
//    //differently
//    private SoundClass soundClass; //soundClass this example is for
//    private int exampleId; //the id of this example (given by chuck)
//    private Color color; //color to display
//    private Rectangle rect;
//    private ImageView imgX;
//    /**
//     * 
//     * @param soundClass soundclass of this example
//     * @param id id corresponding to this example in chuck
//     * @param size height and width of this
//     */
//    public SampleBox(SoundClass soundClass, int exampleId, int size) {
//        super();
//        this.soundClass = soundClass;
//        this.exampleId = exampleId;
//        this.boxSize = size;
//        this.setOnMouseEntered(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                ChuckBoard.playExample(me.exampleId);
//            }
//        });
//        
//        rect = new Rectangle();
//        rect.setArcWidth(size/3);
//        rect.setArcHeight(size/3);
//        rect.setWidth(size);
//        rect.setHeight(size);
//        soundClass.colorProperty().addListener(new ChangeListener<Paint>() {
//            public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
//                rect.setFill(newValue);
//            }
//            
//        });
//        rect.setStroke(Color.BLACK);
//        getChildren().add(rect);
//        rect.setFill(soundClass.getColor());
//        
//        imgX = new ImageView(new Image(System.getProperty("user.dir") + "\\images\\x.png"));
//        imgX.setPreserveRatio(true);
//        imgX.setFitWidth(size);
//        imgX.setVisible(false);
//        getChildren().add(imgX);
//        
//        me.setOnMouseEntered(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                imgX.setVisible(true);
//            }
//        });
//        me.setOnMouseExited(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent event) {
//                imgX.setVisible(false);
//            }
//        });
//    }
//    //serialize the object
//    public void writeObject(ObjectOutputStream oos) {
//        try {
//            oos.writeObject(exampleId);
//            oos.writeObject(soundClass.getClassValue());
//        } catch (IOException ex) {
//            Logger.getLogger(SampleBox.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//    
//    /**
//     * Make this box appear selected
//     * @param isSelected 
//     */
//    public void setSelected(boolean isSelected) {
//        this.selected = isSelected;
//        if (isSelected) {
//            rect.setFill(Color.WHITE);
//        } else {
//            rect.setFill(soundClass.getColor());
//        }
//    }
//    
//    public boolean isSelected() {
//        return selected;
//    }
//
//    //deserialize the object
//    public void readObject(ObjectInputStream ois){
//        try {
//            exampleId = ((Integer)ois.readObject());
//            soundClass = SoundClass.getFromClass((Integer)ois.readObject());
//        } catch (IOException ex) {
//            Logger.getLogger(SampleBox.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(SampleBox.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    int getExampleId() {
//        return exampleId;
//    }
//}
