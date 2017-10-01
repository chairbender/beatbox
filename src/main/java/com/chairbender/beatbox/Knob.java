/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
/**Node that looks like a fader knob and acts basically like a slider but circular
 *
 * @author Kyle
 */
public class Knob extends Parent{
    private Knob me = this;
    
    private SimpleDoubleProperty valueProperty = new SimpleDoubleProperty(0.5);
    public DoubleProperty valueProperty() {
        return valueProperty;
    }
    private double maxValue = 1;
    private Line lnPoint;
    private boolean dragging;
    private double dragOriginY;
    private double width;
    private double valueOrigin;
    
    public Knob(double size, double maxValue) {
        super();
        this.width = size;
        this.maxValue = maxValue;
        Circle c = new Circle();
        c.setRadius(width/2);
        c.setFill(Color.rgb(129, 129, 129));
        c.setLayoutX(width/2);
        c.setLayoutY(width/2);
        getChildren().add(c);
        this.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                dragging = true;
                dragOriginY = event.getY();
                valueOrigin = valueProperty.get();
            }
        });
        this.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (dragging) {
                    valueProperty.set(Math.max(0,Math.min(me.maxValue, valueOrigin - (event.getY() - dragOriginY)*.01)));
                }
            }
        });
        this.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (dragging)
                    dragging = false;
            }
        });
        
        lnPoint = new Line();
        lnPoint.setStartX(width/2);
        lnPoint.setStartY(width/2);
        lnPoint.setStroke(Color.rgb(192, 192, 192));
        lnPoint.setStrokeWidth(2);
        valueProperty.addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double theta = (270*(newValue.doubleValue()/me.maxValue)) - 225;
                double sin = Math.sin(theta);
               
                double cos = Math.cos(theta);
              
                lnPoint.setEndY(width/2 + Math.sin(Math.toRadians(theta))*width/2);
                lnPoint.setEndX(width/2 + Math.cos(Math.toRadians(theta))*width/2);
            }
        });
        getChildren().add(lnPoint);
        valueProperty.set(0.15);
    }
}
