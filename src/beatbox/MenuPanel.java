/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Hyperlink;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.text.Font;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**Class that encapsulates all menu functionality and controls.
 * the Hide Tutorial will be checked the same across all instances
 * of this object.
 * @author Kyle
 */
public class MenuPanel extends Parent {

    private Hyperlink hp;
    private Hyperlink hp2;
    private Hyperlink hp3;
    private Hyperlink hp4;
    private Hyperlink hp5;
    private CheckBox cb;
    private Text txt;
    private EventHandler<ActionEvent> saveAllData;
    private EventHandler<ActionEvent> loadAllData;
    private EventHandler<ActionEvent> toggleAction;
    private boolean checked; //TODO: bind cb.selected;
    private boolean showCheckbox = true;

    /**
     * 
     * @param toggleText text to use for the switching action 
     */
    public MenuPanel(String toggleText) {
        super();
        HBox hb = new HBox();
        hb.setSpacing(5);
        getChildren().add(hb);
        
        hp = new Hyperlink();
        hp.setFont(new Font(25));
        hp.setText("Help");
        //hp.setLayoutX(getScene().getWidth() - hp.getBoundsInLocal().getWidth() - 15);
        hp.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                showHelp();
            }
        });
        //hb.getChildren().add(hp);
        
        hp2 = new Hyperlink();
        hp2.setFont(new Font(25));
        hp2.setText("Bug?");
        //hp2.setLayoutX(hp.getLayoutX() - hp2.getBoundsInLocal().getWidth());
        hp2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showBugReport();
            }
        });
        //hb.getChildren().add(hp2);
        
        hp3 = new Hyperlink();
        hp3.setFont(new Font(35));
        hp3.setText(toggleText);
        //hp3.setLayoutX(hp2.getLayoutX() - hp3.getBoundsInLocal().getWidth());
        hp3.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                toggleAction.handle(null);
            }
        });
        //hb.getChildren().add(hp3);
        
        hb.getChildren().add(hp3);
        hb.getChildren().add(hp2);
        hb.getChildren().add(hp);
        
    }
    
    /*hp4 = Hyperlink {
    layoutX: bind hp3.layoutX - hp4.boundsInLocal.width;
    font: Font { size: 25 };
    text: "Save"
    onMousePressed: function(me: MouseEvent) {
    saveAllData();
    },
    }
    hp5 = Hyperlink {
    layoutX: bind hp4.layoutX - hp5.boundsInLocal.width;
    font: Font { size: 25 };
    text: "Load"
    onMousePressed: function(me: MouseEvent) {
    loadAllData();
    }
    }]
    }*/
    
    //shows the help screen
    private void showHelp() {
        UrlOpener.openURL("http://code.google.com/p/beatbox/wiki/UserDocumentation");
    }

    private void showBugReport() {
        UrlOpener.openURL("http://code.google.com/p/beatbox/issues/entry");
    }

    void setToggleAction(EventHandler<ActionEvent> eventHandler) {
        toggleAction = eventHandler;
    }
    
    public int getWidth() {
        return 280;
    }
}
