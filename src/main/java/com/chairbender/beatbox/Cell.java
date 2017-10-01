package com.chairbender.beatbox;

import javafx.event.*;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

/**
 * @author Kyle Hipke
 */

public class Cell extends Parent {

    private String color;
    void setColor(String c) {color = c;}
    
    private Integer row;
    void setRow(int r) {row = r;}
    
    private Integer col;
    void setCol(int c) {col = c;}
    
    private ColorChangeEvent updateColor;
    void setUpdateColor(ColorChangeEvent e) {updateColor = e;}
    
    private ColorChangeEvent selectColor;
    void setSelectColor(ColorChangeEvent e) {selectColor = e;}

    private final int size = 12;

    private Rectangle rectangle; 
    
    /*
     * 
     */
    public Cell(String colour, int col, int row) {
        super();
        this.setFocusTraversable(false);
        color = colour;
        this.col = col;
        this.row = row;
        
        rectangle = new Rectangle();
        rectangle.setId("#" + color);
        rectangle.setX(col*size);
        rectangle.setY(row * size);
        rectangle.setWidth(size);
        rectangle.setHeight(size);
        rectangle.setStrokeWidth(1.0);
        rectangle.setStroke(Color.BLACK);
        rectangle.setFill(Color.web("#" + color));
        this.getChildren().add(rectangle);
        
        this.setOnMouseReleased(new EventHandler<MouseEvent> () {
            public void handle(MouseEvent event) {
                selectColor.onChange(rectangle.getId(), (Color)rectangle.getFill());
            }
        });
        
        this.setOnMouseMoved(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                updateColor.onChange(rectangle.getId(), (Color)rectangle.getFill());
            }
        });
        
        this.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                rectangle.setStroke(Color.WHITE);
                toFront();
            }
        });
        
        this.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                rectangle.setStroke(Color.BLACK);
            }
        });
        
    }
}
