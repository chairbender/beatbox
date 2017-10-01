/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.VPos;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javax.swing.JOptionPane;
/* Node for managing a bunch of padpanes
 *
 * @author Kyle
 */

public class PadPaneLibraryBrowser extends Parent implements Serializable {

    private PadPaneLibraryBrowser me = this;
    private VBox tabs;
    private ObjectProperty<PadPane> activePadPaneProperty = new SimpleObjectProperty<PadPane>();
    private double height, width, paneWidth, paneHeight;
    private PadPane ppAll;
    private PadPaneThumbnail pptAll;
    private ImageView imgMouseRight;
    private FadeTransition ftOut;
    private FadeTransition ftIn;
    private Rectangle bg;
    private static final double OFFSET = 10;

    public ObjectProperty<PadPane> activePadPaneProperty() {
        return activePadPaneProperty;
    }

    public PadPaneLibraryBrowser(double width, double height, double paneWidth, double paneHeight) {

        this.height = height;
        this.width = width;
        this.paneHeight = paneHeight;
        this.paneWidth = paneWidth;

        initialize(false);
        
        //add the default tab
        PadPaneThumbnail pptn = new PadPaneThumbnail(new PadPane(paneWidth, paneHeight),width - 10, 20,OFFSET);
        addTab(pptn);
        setActivePad(pptn);
    }
    
    public void setHeight(double height) {
        bg.setHeight(height);
    }

    //sets up the scene graph using the current values of width, height, paneWidth, etc...
    //if defaultTab is true, creates an empty Untitled tab and selects it
    private void initialize(boolean defaultTab) {
        bg = new Rectangle();
        bg.setWidth(width);
        bg.setHeight(height);
        bg.setFill(Color.web("c2c6ca"));
        getChildren().add(bg);
        
        //mouse help icon
        imgMouseRight = new ImageView(new Image(System.getProperty("user.dir") + "\\images\\mouse_help_right.png"));
        imgMouseRight.setSmooth(true);
        imgMouseRight.setPreserveRatio(true);
        imgMouseRight.setLayoutY(-33);
        imgMouseRight.setLayoutX(width - 80);
        imgMouseRight.setOpacity(0.0);
        getChildren().add(imgMouseRight);

        Text txtTitle = new Text();
        txtTitle.setText("PAD LIBRARY");
        try {
            txtTitle.setFont(Font.loadFont(new FileInputStream(System.getProperty("user.dir") + "\\fonts\\LucidaGrande.ttf"), 13));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PadPaneLibraryBrowser.class.getName()).log(Level.SEVERE, null, ex);
        }
        txtTitle.setFill(Color.web("72808d"));
        txtTitle.setTextOrigin(VPos.TOP);
        txtTitle.setLayoutX(4);
        getChildren().add(txtTitle);


        tabs = new VBox();
        tabs.setSpacing(2);
        //tabs.setLayoutX(OFFSET);
        tabs.setLayoutY(txtTitle.getBoundsInLocal().getHeight() + 2);
        activePadPaneProperty = new SimpleObjectProperty(null);
        activePadPaneProperty.set(new PadPane(paneWidth, paneHeight));
        final PadPaneThumbnail newp = new PadPaneThumbnail(activePadPaneProperty.get(), width - 10, 20, OFFSET);
        newp.setOnMouseClicked(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (newp.isActive()) {
                        InputDialog.showDialog(BeatBoxNew.stg, "Enter pad name", new InputHandler() {

                            public void onInputSent(String input) {
                                if (input != null) {
                                    newp.getPadPane().setName(input);
                                }
                            }
                        });
                    } else {
                        for (Node n : tabs.getChildren()) {
                            if (n instanceof PadPaneThumbnail) {
                                ((PadPaneThumbnail) n).setActive(false);
                            }
                        }
                        newp.setActive(true);
                        activePadPaneProperty.set(newp.getPadPane());
                    }
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    tabs.getChildren().remove(newp);
                }
            }
        });
        if (defaultTab) {
            tabs.getChildren().add(newp);
            newp.setActive(true);
        }

        getChildren().add(tabs);

        AddButton btnAdd = new AddButton(12,true);
        btnAdd.setTranslateX(OFFSET);
        btnAdd.setOnMousePressed(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                PadPane newp = new PadPane(me.paneWidth, me.paneHeight);
                for (Node n : tabs.getChildren()) {
                    if (n instanceof PadPaneThumbnail) {
                        ((PadPaneThumbnail) n).setActive(false);
                    }
                }
                PadPaneThumbnail newt = new PadPaneThumbnail(newp, me.width - 10, 20, OFFSET);
                addTab(newt);
                newt.setActive(true);
                activePadPaneProperty.set(newp);
            }
        });
        btnAdd.setCursor(Cursor.HAND);
        tabs.getChildren().add(btnAdd);

        ppAll = new PadPane(paneWidth, paneHeight);
        ppAll.setName("All Pads");
        pptAll = new PadPaneThumbnail(ppAll, width - 10, 20, OFFSET);
        pptAll.setOnMouseClicked(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (!pptAll.isActive()) {
                        for (Node n : tabs.getChildren()) {
                            if (n instanceof PadPaneThumbnail) {
                                ((PadPaneThumbnail) n).setActive(false);
                            }
                        }
                        pptAll.setActive(true);
                        activePadPaneProperty.set(pptAll.getPadPane());
                        ppAll.clearPads();
                        //Populate pptAll with all of the pads in the other 
                        for (SampleHolderPad p : BeatBoxNew.getAllPads()) {
                            ppAll.addPad(p.copyOf());
                        }
                    }
                }
            }
        });

        tabs.getChildren().add(0, pptAll);
        
        ftOut = new FadeTransition(Duration.millis(500), imgMouseRight);
        ftOut.setFromValue(1.0);
        ftOut.setToValue(0.0);
        
        ftIn = new FadeTransition(Duration.millis(500), imgMouseRight);
        ftIn.setFromValue(0.0);
        ftIn.setToValue(1.0);
    }

    private void addTab(final PadPaneThumbnail tab) {
        tab.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (tab.isActive()) {
//                        //InputDialog.showDialog(BeatBoxNew.stg, "Enter pad name", new InputHandler() {
//               
//                            public void onInputSent(String input) {
//                                if (input != null) {
//                                    tab.getPadPane().setName(input);
//                                }
//                            }
//                        });
                        tab.promptName();
                    } else {
                        for (Node n : tabs.getChildren()) {
                            if (n instanceof PadPaneThumbnail) {
                                ((PadPaneThumbnail) n).setActive(false);
                            }
                        }
                        tab.setActive(true);
                        activePadPaneProperty.set(tab.getPadPane());
                    }
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    if (tabs.getChildren().size() > 3) {
                        tabs.getChildren().remove(tab);
                        if (tab.isActive()) {
                            setActivePad((PadPaneThumbnail) tabs.getChildren().get(tabs.getChildren().size() - 2));
                        }
                    }
                }
            }
        });
        tab.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                fadeInHelp();

            }
        });
        tab.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                fadeOutHelp();
            }
        });
        tabs.getChildren().add(tabs.getChildren().size() - 1, tab);
    }

    /**
     * 
     * @return  all of the pad panes in this browser
     */
    public Collection<PadPane> getAllPadPanes() {
        Set<PadPane> res = new HashSet<PadPane>();
        for (Node n : tabs.getChildren()) {
            if (n instanceof PadPaneThumbnail && n != ppAll) {
                res.add(((PadPaneThumbnail) n).getPadPane());
            }
        }
        return res;
    }
    
    private void fadeInHelp() {
        ftIn.setFromValue(imgMouseRight.getOpacity());
        ftIn.playFromStart();
    }
    
    private void fadeOutHelp() {
        ftOut.setFromValue(imgMouseRight.getOpacity());
        ftOut.playFromStart();
    }
    

    //gets all of the examples in the app, ordered by chuckId
    public List<Example> getAllExamples() {
        List<Example> res = new ArrayList<Example>();
        for (PadPane p : getAllPadPanes()) {
            for (SampleHolderPad shp : p.getPads()) {
                for (Example e : shp.getExamples()) {
                    res.add(e);
                }
            }
        }

        Collections.sort(res, new Comparator<Example>() {

            public int compare(Example t, Example t1) {
                return t.getChuckId() - t1.getChuckId();
            }
        });

        return res;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeDouble(height);
        out.writeDouble(width);
        out.writeDouble(paneHeight);
        out.writeDouble(paneWidth);

        out.writeInt(tabs.getChildren().size() - 2);
        for (Node n : tabs.getChildren()) {
            if (n instanceof PadPaneThumbnail && n != pptAll) {
                PadPaneThumbnail p = (PadPaneThumbnail) n;
                out.writeObject(p);
            }
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        //read height and width
        height = in.readDouble();
        width = in.readDouble();
        paneHeight = in.readDouble();
        paneWidth = in.readDouble();
        me = this;
        //read and add PadPaneThumbnails
        int num = in.readInt();

        //initialize
        initialize(num == 0);

        for (int i = 0; i < num; i++) {
            PadPaneThumbnail n = (PadPaneThumbnail) in.readObject();
            addTab(n);
        }
        if (num > 0)
            setActivePad((PadPaneThumbnail)tabs.getChildren().get(1));

    }

    void setActivePad(PadPaneThumbnail ppt) {
        if (!ppt.isActive()) {
            for (Node n : tabs.getChildren()) {
                if (n instanceof PadPaneThumbnail) {
                    ((PadPaneThumbnail) n).setActive(false);
                }
            }
            ppt.setActive(true);
            activePadPaneProperty.set(ppt.getPadPane());
        }
    }
}

