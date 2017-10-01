/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

/**
 * GUI element for selecting a sound from the library and adding sounds to the
 * library.
 * @author Kyle
 */
public class SoundPicker extends Parent{
    SoundPicker me = this;
    Rectangle rctBack;
    GridPane grpSounds;
    AddButton btnAdd;
    Text txtAdd;
    
    private ObjectProperty<Sound> selectedSound = new SimpleObjectProperty<Sound>();
    public ObjectProperty<Sound> selectedSoundProperty() {
        return selectedSound;
    };
    
    public SoundPicker() {
        SoundLibrary.addOnLibraryChangedListener(new ChangeListener<Sound>() {
            public void changed(ObservableValue<? extends Sound> observable, Sound oldValue, Sound newValue) {
                refresh();
            }
        });
        refresh();
    }
    
    public void refresh() {
       getChildren().clear();
        grpSounds = new GridPane();
        int numCols = (int)Math.round(Math.sqrt(SoundLibrary.getSounds().size()));
        int r = 0;
        int c = 0;
        int count = 0;
        for (final Sound s : SoundLibrary.getSounds()) {
            if (c == numCols) {
                r++;
                c = 0;
            }
            final Group g = new Group();
            
            Text txt = new Text();
            txt.setLayoutX(12);
            txt.setTextOrigin(VPos.TOP);
            try {
                txt.setFont(Font.loadFont(new FileInputStream(System.getProperty("user.dir") + "\\fonts\\virtue.ttf"), 20));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SoundPicker.class.getName()).log(Level.SEVERE, null, ex);
            }
            txt.setText(s.getName());
            txt.setFill(Color.BLACK);
            g.setUserData(s);
            txt.setCursor(Cursor.HAND);
            //txt.setOnMouseClicked(me.getOnMouseClicked());
            txt.setOnMouseEntered(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent event) {
                    selectedSound.set((Sound)g.getUserData());
                }
            });
            
            Rectangle rc = new Rectangle(10,10);
            rc.setCursor(Cursor.HAND);
            rc.setOnMousePressed(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent event) {
                   s.colorProperty().set(Color.rgb((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255)));
                   refresh();
                }
            });
            rc.setFill(s.getColor());

            g.getChildren().add(txt);
            g.getChildren().add(rc);
        
            getChildren().add(g);
            grpSounds.add(g, c, r);
            c++;
            count++;
        }
        grpSounds.setHgap(5);
        getChildren().add(grpSounds);
        grpSounds.layout();
        
        btnAdd = new AddButton(20,false);
        btnAdd.setLayoutY(this.getBoundsInLocal().getHeight() + 20);
        btnAdd.setCursor(Cursor.HAND);
        btnAdd.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                ObjectProperty<Sound> res = new SimpleObjectProperty<Sound>();
                res.addListener(new ChangeListener<Sound>() {
                    public void changed(ObservableValue<? extends Sound> observable, Sound oldValue, Sound newValue) {
                        SoundLibrary.addSound(newValue);
                        selectedSound.set(newValue);
                    }
                });
                BeatBoxNew.getSound(res);
            }
        });
        getChildren().add(btnAdd);
        
        txtAdd = new Text();
        txtAdd.setFont(new Font(15));
        txtAdd.setText("Add file to library");
        txtAdd.setLayoutY(btnAdd.getLayoutY()+txtAdd.getBoundsInLocal().getHeight());
        txtAdd.setLayoutX(btnAdd.getLayoutX() + 25);
        getChildren().add(txtAdd);
        
        rctBack = new Rectangle();
        rctBack.setWidth(this.getBoundsInLocal().getWidth());
        rctBack.setHeight(this.getBoundsInLocal().getHeight());
        rctBack.setFill(Color.WHITE);
        
        DropShadow f = new DropShadow();
        f.setOffsetX(3);
        f.setOffsetY(3);
        rctBack.setEffect(f);
        
        getChildren().add(0,rctBack);
    }
    
}
