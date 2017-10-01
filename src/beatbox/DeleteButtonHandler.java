/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 *
 * @author Kyle
 */
public class DeleteButtonHandler implements EventHandler<ActionEvent>{
    
    private EventHandler<ActionEvent> onDelete;

    /**
     * 
     * @param onDelete
     * @param selfReference a reference to the object that you want to delete
     */
    public DeleteButtonHandler(EventHandler<ActionEvent> onDelete) {
        this.onDelete = onDelete;
        
    }
    
    public void handle(ActionEvent event) {
        onDelete.handle(event);
    }
    
}
