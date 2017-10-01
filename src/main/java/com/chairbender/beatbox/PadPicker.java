/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import java.util.ArrayList;
import java.util.List;
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
 * GUI element for picking a soundclass
 * @author Kyle
 */
public class PadPicker extends Parent {

    
    PadPicker me = this;
    Rectangle rctBack;
    GridPane grpSounds;
    AddButton btnAdd;
    Text txtAdd;
    
    private ObjectProperty<SoundClass> selectedSoundClass = new SimpleObjectProperty<SoundClass>();
    private final PadPane padPane;
    public ObjectProperty<SoundClass> selectedSoundProperty() {
        return selectedSoundClass;
    };
    
    public PadPicker(PadPane pads) {
        this.padPane = pads;
//        pads.addOnPadsChangedListener(new ChangeListener<Sound>() {
//            public void changed(ObservableValue<? extends Sound> observable, Sound oldValue, Sound newValue) {
//                refresh();
//            }
//        });
        refresh();
    }
    
    //call this to update the colors and labels
    public void refresh() {
       getChildren().clear();
        grpSounds = new GridPane();
        int numCols = (int)Math.round(Math.sqrt(padPane.getPads().size()));
        int r = 0;
        int c = 0;
        int count = 0;
        for (final SampleHolderPad s : padPane.getPads()) {
            if (c == numCols) {
                r++;
                c = 0;
            }
            final Group g = new Group();
            
            Text txt = new Text();
            txt.setLayoutX(12);
            txt.setTextOrigin(VPos.TOP);
            txt.setFont(new Font(20));
            txt.setText(s.getSoundClass().getSound().getName());
            txt.setFill(Color.BLACK);
            g.setUserData(s);
            txt.setCursor(Cursor.HAND);
            //txt.setOnMouseClicked(me.getOnMouseClicked());
            txt.setOnMouseEntered(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent event) {
                    selectedSoundClass.set(((SampleHolderPad)g.getUserData()).getSoundClass());
                    ChuckBoard.play(selectedSoundClass.get(), ((SampleHolderPad)g.getUserData()).getVolume());
                }
            });
            
            Rectangle rc = new Rectangle(10,10);
            rc.setCursor(Cursor.HAND);
            rc.setFill(s.getSoundClass().getSound().getColor());

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
