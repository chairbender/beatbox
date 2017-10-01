/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import javafx.scene.Parent;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * A button used to add things
 * @author Kyle
 */
public class AddButton extends Parent{
    
    public AddButton(double size, boolean gray) {
        if (gray) {
            Rectangle bg = new Rectangle();
            bg.setWidth(size);
            bg.setHeight(size);
            bg.setArcHeight(size/4);
            bg.setArcWidth(size/4);
            bg.setFill(Color.TRANSPARENT);
            getChildren().add(bg);
            ImageView imgPlus = new ImageView(new Image(System.getProperty("user.dir") + "\\images\\add_gray.png"));
            imgPlus.setFitWidth(size);
            imgPlus.setFitHeight(size);
            getChildren().add(imgPlus);
        } else {
            Rectangle bg = new Rectangle();
            bg.setWidth(size);
            bg.setHeight(size);
            bg.setArcHeight(size/4);
            bg.setArcWidth(size/4);
            bg.setFill(Color.rgb(87,87,87));
            getChildren().add(bg);
            ImageView imgPlus = new ImageView(new Image(System.getProperty("user.dir") + "\\images\\add.png"));
            imgPlus.setFitWidth(size);
            imgPlus.setFitHeight(size);
            getChildren().add(imgPlus);

            DropShadow ds = new DropShadow();
            ds.setOffsetX(2);
            ds.setOffsetY(2);

            this.setEffect(ds);
        }
        
    }
}
