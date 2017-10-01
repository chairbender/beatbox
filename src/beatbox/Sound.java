/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *Represents an audio file with a name in the library. Also a GUI element
 * @author Kyle
 */
public class Sound implements Serializable{
    private File file;
    private String name;
    private ObjectProperty<Paint> color = new SimpleObjectProperty<Paint>(null);
    private List<EventHandler<ActionEvent>> onDeletedFromLibrary;
    public void addOnDeletedFromLibrary(EventHandler<ActionEvent> ev) {
        if (onDeletedFromLibrary == null)
            onDeletedFromLibrary = new ArrayList<EventHandler<ActionEvent>>();
        onDeletedFromLibrary.add(ev);
    }
    public void removeOnDeletedFromLibrary(EventHandler<ActionEvent> ev) {
        onDeletedFromLibrary.remove(ev);
    }
    
    //signals this sound that it was deleted from library, calling all
    //and removing all handlers
    public void notifyDeletedFromLibrary() {
        for (EventHandler<ActionEvent> e : onDeletedFromLibrary) {
            e.handle(null);
        }
        onDeletedFromLibrary.clear();
    }
    
    public ObjectProperty<Paint> colorProperty() {
        return color;
    }
    
    
    public Sound(String name, File file, Color color) {
        this.file = file;
        this.name = name;
        this.color.set(color);
    }

    String getName() {
        return name;
    }
    
    String getPath() {
        return file.getAbsolutePath();
    }
    
    Color getColor() {
        return (Color)color.get();
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof Sound) && ((Sound)o).name.equals(this.name) &&
                ((Sound)o).file.equals(this.file);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.file != null ? this.file.hashCode() : 0);
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
    
     private void writeObject(java.io.ObjectOutputStream out)
     throws IOException {
         out.writeObject(ResourceUtils.getRelativePath(file.getAbsolutePath(), System.getProperty("user.dir"), "\\"));
         out.writeObject(name);
         out.writeObject(((Color)color.get()).getRed());
         out.writeObject(((Color)color.get()).getGreen());
         out.writeObject(((Color)color.get()).getBlue());
     }
     
     private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException {
        file = new File((String)in.readObject());
        name = (String)in.readObject();
        int r = (int)(255 * (Double)in.readObject());
        int g = (int)(255 *(Double)in.readObject());
        int b = (int)(255 * (Double)in.readObject());
        color = new SimpleObjectProperty<Paint>(null);
        color.set(Color.rgb(r, g, b));
     }

}
