/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**a shrunk version of a padpane, to be used in a menu
 *
 * @author Kyle
 */
public class PadPaneThumbnail extends Parent implements Serializable {

    private double width, height;
    private PadPaneThumbnail me = this;
    private PadPane pane;
    private Rectangle rect;
    private Text text;
    private boolean active;
    private double offset;
    private TextField txtName;
    private Group grpNotText;

    //offset how far from left edge of pplb
    public PadPaneThumbnail(PadPane p, double width, double height, double offset) {
        this.width = width;
        this.height = height;
        this.pane = p;
        this.offset = offset;

        initialize();
    }

    //initialize the nodes and stuff using the current width and height
    private void initialize() {
        grpNotText = new Group();
        
        me.setCursor(Cursor.HAND);
        text = new Text();
        text.setLayoutX(offset);
        text.setFill(Color.web("4b4b4b"));
        text.setTextOrigin(VPos.TOP);
        try {
            text.setFont(Font.loadFont(new FileInputStream(System.getProperty("user.dir") + "\\fonts\\LucidaGrande.ttf"), 13.5));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PadPaneLibraryBrowser.class.getName()).log(Level.SEVERE, null, ex);
        }

        //if text too big, cutoff
        if (pane.getName().equals("")) {
            text.setText("Untitled");
        } else {
            text.setText(pane.getName());
        }

        pane.nameProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                text.setText(newValue);
                try {
                    text.setFont(Font.loadFont(new FileInputStream(System.getProperty("user.dir") + "\\fonts\\LucidaGrande.ttf"), 13.5));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(PadPaneLibraryBrowser.class.getName()).log(Level.SEVERE, null, ex);
                }


                while (text.getBoundsInLocal().getWidth() > (width)) {
                    try {
                        text.setFont(Font.loadFont(new FileInputStream(System.getProperty("user.dir") + "\\fonts\\LucidaGrande.ttf"), text.getFont().getSize() - .5));
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        while (text.getBoundsInLocal().getWidth() > (width)) {
            try {
                text.setFont(Font.loadFont(new FileInputStream(System.getProperty("user.dir") + "\\fonts\\LucidaGrande.ttf"), text.getFont().getSize() - .5));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        rect = new Rectangle();
        rect.setWidth(width+offset);
        rect.setHeight(text.getBoundsInLocal().getHeight());
        Stop[] stops = new Stop[] { new Stop(0, Color.web("abb4c2")), new Stop(1,Color.web("7d869b")) };
        
        LinearGradient g = new LinearGradient(0,0,0,text.getBoundsInLocal().getHeight(),true,CycleMethod.NO_CYCLE,stops);
        
        rect.setFill(Color.TRANSPARENT);
        grpNotText.getChildren().add(rect);

        me.setOnDragEntered(new EventHandler<DragEvent>() {

            public void handle(DragEvent event) {
                System.out.println("drag entered");
                if (!me.active) {
                    BeatBoxNew.getPadPaneLibraryBrowser().setActivePad(me);
                    event.consume();
                Stop[] stops = new Stop[] { new Stop(0, Color.web("abb4c2")), new Stop(1,Color.web("7d869b")) };

                LinearGradient g = new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,stops);

                rect.setFill(g);
                text.setFill(Color.WHITE);
                }
            }
        });
        me.setOnDragDropped(new EventHandler<DragEvent>() {

            public void handle(DragEvent event) {
                System.out.println("drag dropped");
                for (SampleHolderPad p : DragHelper.padsDragging) {
                    pane.addPad(p);
                }
                event.setDropCompleted(true);
                event.consume();

            }
        });
        me.setOnDragExited(new EventHandler<DragEvent>() {

            public void handle(DragEvent event) {
                if (!me.active) {
                    rect.setFill(Color.TRANSPARENT);
                    text.setFill(Color.web("454545"));
                }
            }
        });
        me.setOnDragOver(new EventHandler<DragEvent>() {

            public void handle(DragEvent event) {
                if (!me.getPadPane().nameProperty().get().equals("All Pads")) {
                    event.acceptTransferModes(TransferMode.COPY);
                }
            }
        });
        rect.setLayoutX(0);
        
        txtName = new TextField();
        txtName.setPrefColumnCount(5);
        txtName.setVisible(false);
        
        getChildren().add(grpNotText);
        getChildren().add(text);
        getChildren().add(txtName);
        

    }

    /**
     * If active, the borders are the same color as the background
     * @param active 
     */
    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            Stop[] stops = new Stop[] { new Stop(0, Color.web("abb4c2")), new Stop(1,Color.web("7d869b")) };
        
            LinearGradient g = new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,stops);
   
            rect.setFill(g);
            text.setFill(Color.WHITE);
        } else {
            rect.setFill(Color.TRANSPARENT);
            text.setFill(Color.web("454545"));
            txtName.setVisible(false);
            grpNotText.setVisible(true);
            text.setVisible(true);
        }
    }

    PadPane getPadPane() {
        return pane;
    }

    boolean isActive() {
        return active;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeDouble(width);
        out.writeDouble(height);
        out.writeObject(pane);
        out.writeDouble(offset);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        width = in.readDouble();
        height = in.readDouble();
        pane = (PadPane) in.readObject();
        offset = in.readDouble();
        me = this;

        initialize();
    }
    
    //changes to textbox to prompt for name input
    public void promptName() {
        txtName.setText(pane.getName());
        txtName.setVisible(true);
        txtName.requestFocus();
        text.setVisible(false);
        grpNotText.setVisible(false);
        
        txtName.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                pane.setName(txtName.getText());
                txtName.setVisible(false);
                grpNotText.setVisible(true);
                text.setVisible(true);
            }
        });
    }
}
