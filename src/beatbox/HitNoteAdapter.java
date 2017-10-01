/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package beatbox;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.shape.*;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
/**class for displaying a HitNote
 * @author Kyle
 */

public class HitNoteAdapter extends Parent{
    private HitNoteAdapter me = this;
    
    //required parameters
    private HitNote hitNote;
    public HitNote getHitNote() {return hitNote;}
    public void setHitNote(HitNote hitNote) {this.hitNote = hitNote;}

    private PadPicker picker;
    //optional params
    private int size = 10;

    private Rectangle rect;

    public HitNoteAdapter(int size, HitNote hitNote, PadPane parentPane) {
        super();
        
        this.hitNote = hitNote;
        this.size = size;
        rect = new Rectangle();
        rect.setWidth(10);
        rect.setHeight(10);
        rect.setFill(hitNote.getSoundClass().getSound().colorProperty().get());
        hitNote.getSoundClass().getSound().colorProperty().addListener(new ChangeListener<Paint>() {
            public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                rect.setFill(newValue);
            }
        });
        rect.setStroke(Color.BLACK);
        getChildren().add(rect);
        
        picker = new PadPicker(parentPane);
        picker.setLayoutX(size);
        picker.setVisible(false);
        picker.selectedSoundProperty().addListener(new ChangeListener<SoundClass>() {
            public void changed(ObservableValue<? extends SoundClass> observable, SoundClass oldValue, SoundClass newValue) {
                me.hitNote.setSoundClass(newValue);
                rect.setFill(me.hitNote.getSoundClass().getSound().colorProperty().get());
                me.hitNote.getSoundClass().getSound().colorProperty().addListener(new ChangeListener<Paint>() {
                    public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                        rect.setFill(newValue);
                    }
                });
            }
        });
        
        getChildren().add(picker);
        
//        BeatBoxNew.addClickHandler(new EventHandler<ActionEvent>() {
//            public void handle(ActionEvent event) {
//                if (event.getSource() != me)
//                    setPickerVisible(false);
//            }
//        });
    }
    
    public void setPickerVisible(boolean isVisible) {
        if (isVisible) {
            picker.refresh();
            picker.setVisible(true);
        } else {
            picker.setVisible(false);
        }
    }

    public boolean isPickerVisible() {
        return picker.isVisible();
    }
}
