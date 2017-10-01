/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Kyle
 */
//TODO: fix once Oracle adds a legit input dialog or the ability to pause the main thread
public class InputDialog {
    
    public static boolean showing = false;
    
    /**
     * @effects displays a popup dialog box prompting the user to enter text. Blocks mouse
     * and key events on the active scene of the parent stage, but doesn't stop the thread. Hides
     * the current scene of the parent.
     * @param prompt what to ask
     * @param processing what to do with the entered data when it's entered
     */
    public static void showDialog(final Stage parent, String prompt, final InputHandler processing) {
        showing = true;
        final Scene scnCurrent = parent.getScene();
        final EventHandler<WindowEvent> close = parent.getOnCloseRequest();
        final double w = parent.getWidth();
        final double h = parent.getHeight();
        
        //Create the new scene
        Group grpRoot = new Group();
        Scene scnDialog = new Scene(grpRoot,200,100,Color.WHITE);
        
//        Text txtPrompt = new Text();
//        //TODO: pick a good font
//        txtPrompt.setFont(new Font(15));
//        txtPrompt.setText(prompt);
//        txtPrompt.setTextOrigin(VPos.TOP);
//        txtPrompt.setLayoutX(scnDialog.getWidth()/2 - txtPrompt.getBoundsInLocal().getWidth()/2);
//        txtPrompt.setLayoutY(scnDialog.getHeight()/3);
//        grpRoot.getChildren().add(txtPrompt);
        
        final TextField txfResponse = new TextField();
        txfResponse.setPromptText(prompt);
        txfResponse.setLayoutX(scnDialog.getWidth()/2 - 50);
        txfResponse.setLayoutY(scnDialog.getHeight()/3);
        grpRoot.getChildren().add(txfResponse);
        
        Text txt = new Text();
        txt.setText(prompt);
        txt.setFont(new Font(15));
        txt.setLayoutX(txfResponse.getLayoutX());
        txt.setLayoutY(30);
        grpRoot.getChildren().add(txt);
        
        Button btnOk = new Button();
        btnOk.setText("Ok");
        btnOk.setLayoutX(scnDialog.getWidth()/2 - btnOk.getWidth()/2);
        btnOk.setLayoutY(2*scnDialog.getHeight()/3);
        btnOk.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                processing.onInputSent(txfResponse.getText());
                parent.setWidth(w);
                parent.setHeight(h);
//                parent.setX(parent.getOwner().getWidth()/2 - parent.getWidth()/2);
//                parent.setY(2*parent.getOwner().getHeight()/3);
                parent.setOnCloseRequest(close);
                parent.setScene(scnCurrent);
                showing = false;
            }
        });
        grpRoot.getChildren().add(btnOk);
        txfResponse.setOnAction(btnOk.getOnAction());
        
        parent.setWidth(scnDialog.getWidth()+50);
        parent.setHeight(scnDialog.getHeight()+50);
//        parent.setX(parent.getOwner().getWidth()/2 - parent.getWidth()/2);
//        parent.setY(parent.getOwner().getHeight()/2 - parent.getHeight()/2);
        parent.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent event) {
                processing.onInputSent(txfResponse.getText());
                parent.setWidth(w);
                parent.setHeight(h);
//                parent.setX(parent.getOwner().getWidth()/2 - parent.getWidth()/2);
//                parent.setY(2*parent.getOwner().getHeight()/3);
                parent.setOnCloseRequest(close);
                parent.setScene(scnCurrent);
                event.consume();
            }
        });
        parent.setScene(scnDialog);
    }
    
}
