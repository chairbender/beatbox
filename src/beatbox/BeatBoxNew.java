/*
 * NOTES from mtg:
 * Use envelope instead of gain object for smooth transitions
 * Need master volume when sound playback is frequent
 * 
 * Example level warnings: useful for bad examples, 
 * classes that are too similar
 * 
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;


import KyleWrapper.WekinatorBeatboxLogger;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Kyle
 */
public class BeatBoxNew extends Application {

    private static BeatBoxNew app;
    private static Rectangle rctDim;
    private static File retVal;
    
//    private static Set<EventHandler<ActionEvent>> clickHandlers;
    
    VBox sampleHolders;
    Scene introScene;
    Scene trainScene;
    static Group trainRoot;
    Scene performScene;
    Group performRoot;
    static Stage stg;
    
    static final boolean ONLY_FALSE_POSITIVES = false;
   
    
    final int TOP_BAR_HEIGHT = 60;
    final int LOOPER_HEIGHT = 200;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(BeatBoxNew.class, args);
    }
    private int curSoundClass;
    private MenuPanel menpPerform;
    private MenuPanel menpTrain;
    private ScrollPane scv;
    private Rectangle rect;
    private Text trnTxt;
    private Text trnSmallTxt;
    private FlowPane fp;
    private static PadPaneLibraryBrowser ppm;
    private static LoopManager lmLoops;
    private static VBox vbMain;
    private SampleHolderPad tmpPad;
    private Rectangle rctBack;
    private ScrollPane scpAll;
    private Knob knbMaster;
    private double vol = .015;
    
    @Override
    public void start(Stage primaryStage) throws Exception {      
//        clickHandlers = new HashSet<EventHandler<ActionEvent>>();
        ChuckBoard.initialize();
        
        if (!loadStuffFromFile()) {
            ppm = new PadPaneLibraryBrowser(100, 450,450,150);
        }
        
        //initialize logging
//        WekinatorBeatboxLogger.setup(System.getProperty("user.dir"), WekinatorSingleton.getWekinator());
//        WekinatorBeatboxLogger.startLogging();
        

        stg = primaryStage;
        
        rctBack = new Rectangle();
        rctBack.setFill(Color.rgb(42,41,42));
        rctBack.setWidth(800);
        rctBack.setHeight(700);
                      
        trainRoot = new Group();
        scpAll = new ScrollPane();
        scpAll.setPrefViewportWidth(800);
        scpAll.setPrefViewportHeight(700);
        scpAll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scpAll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        scpAll.setContent(trainRoot);
        trainScene = new Scene(scpAll,800,700,Color.rgb(77,77,77));
        stg.setScene(trainScene);
        
        trainRoot.getChildren().add(rctBack);
        
        vbMain = new VBox();
        vbMain.setPrefWidth(800 - 200);
        vbMain.setSpacing(65);
        vbMain.setLayoutX(110);
        vbMain.setLayoutY(150);
        trainRoot.getChildren().add(vbMain);
        
        ImageView title = new ImageView(new Image(System.getProperty("user.dir") + "\\images\\title.png"));
        title.setPreserveRatio(true);
        title.setSmooth(true);
        title.setFitWidth(400);
        title.setLayoutX(30);
        title.setLayoutY(10);
        trainRoot.getChildren().add(title);
        
        VolumeMeter vm = new VolumeMeter(20,title.getBoundsInLocal().getHeight());
        vm.setLayoutX(title.getBoundsInLocal().getWidth() + title.getLayoutX() + 15);
        vm.setLayoutY(title.getLayoutY());
        trainRoot.getChildren().add(vm);
        
//        activePadPane = new PadPane(400,400);
//        Sound kick = new Sound("Kick",
//                    new File(System.getProperty("user.dir") + "\\sounds\\kick.wav"),Color.RED);
//        Sound snare = new Sound("Snare",
//                    new File(System.getProperty("user.dir") + "\\sounds\\snare.wav"),Color.GREEN);
//        Sound hat = new Sound("Hihat",
//                    new File(System.getProperty("user.dir") + "\\sounds\\hihat.wav"),Color.BLUE);
//        
//        activePadPane.addPad(new SampleHolderPad(
//                SoundClass.getSoundClass(0, 
//                kick),
//                100));
//        activePadPane.addPad(new SampleHolderPad(
//                SoundClass.getSoundClass(1, 
//                snare),
//                100));
//        activePadPane.addPad(new SampleHolderPad(
//                SoundClass.getSoundClass(3, 
//                hat),
//                100));
//        
//        trainRoot.getChildren().add(activePadPane);
//        activePadPane.setLayoutX(title.getLayoutX());
//        activePadPane.setLayoutY(120);
        ppm.setLayoutY(150);
        trainRoot.getChildren().add(1,ppm);
        PadPane newValue = ppm.activePadPaneProperty().get();
        newValue.setLayoutX(200);
        newValue.setLayoutY(150);
        vbMain.getChildren().add(newValue);
        ppm.activePadPaneProperty().addListener(new ChangeListener<PadPane>() {
            public void changed(ObservableValue<? extends PadPane> observable, PadPane oldValue, PadPane newValue) {
                vbMain.getChildren().remove(oldValue);
                vbMain.getChildren().add(0,newValue);
                newValue.setFocusTraversable(false);
                try {
                    trainOnAllPads(newValue);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        
        fp = new FlowPane();
        fp.setPadding(new Insets(0,50,5,0));
        fp.setLayoutY(title.getBoundsInLocal().getHeight());
        fp.setLayoutX(title.getLayoutX());
        fp.setOrientation(Orientation.HORIZONTAL);
        fp.setPrefWrapLength(600);
        fp.setHgap(20);
        fp.setVgap(10);
        
        lmLoops = new LoopManager(500,300);
        lmLoops.setLayoutX(200);
        lmLoops.setLayoutY(ppm.getLayoutY() + 150 + 10);
        vbMain.getChildren().add(lmLoops);
        
        //master volume
        Text txtMaster = new Text();
        txtMaster.setFont(new Font(20));
        txtMaster.setText("Master Volume");
        txtMaster.setLayoutX(trainScene.getWidth() - txtMaster.getBoundsInLocal().getWidth()-140);
        txtMaster.setTextOrigin(VPos.TOP);
        txtMaster.setFill(Color.WHITE);
        trainRoot.getChildren().add(txtMaster);
        
        knbMaster = new Knob(20,4.0);
        knbMaster.setLayoutX(txtMaster.getLayoutX() + txtMaster.getBoundsInLocal().getWidth()/2 - 10);
        knbMaster.setLayoutY(txtMaster.getLayoutY() + txtMaster.getBoundsInLocal().getHeight());
        knbMaster.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                ChuckBoard.setVolume(newValue.doubleValue());
            }
        });
        knbMaster.valueProperty().set(vol);
        ChuckBoard.setVolume(knbMaster.valueProperty().doubleValue());
        trainRoot.getChildren().add(knbMaster);
        
        trainRoot.getChildren().add(fp);
        

        stg.show();
        app = this;
        ppm.setFocusTraversable(false);
        ppm.activePadPaneProperty().get().setFocusTraversable(false);
        ppm.setLayoutX(0);
        
        WekinatorSingleton.getWekinator().startRunning();
        
        trainRoot.boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                if (!InputDialog.showing && (newValue.getHeight() > oldValue.getHeight() ||
                        newValue.getWidth() > oldValue.getWidth())) {
                    rctBack.setWidth(Math.max(newValue.getWidth(),stg.getWidth())-2);
                    rctBack.setHeight(Math.max(newValue.getHeight(),stg.getHeight())-2);
                    if (rctDim != null) {
                        rctDim.setWidth(rctBack.getWidth()-2);
                        rctDim.setHeight(rctBack.getHeight()-2);
                    }
                }
                rctBack.setLayoutX(0);
                rctBack.setLayoutY(0);
                ppm.setHeight(Math.max(newValue.getHeight()-2, stg.getHeight()-2)- 150);
            }
        });
        ppm.setHeight(Math.max(trainRoot.getBoundsInLocal().getHeight()-2, stg.getHeight()-2)- 150);
        trainOnAllPads(ppm.activePadPaneProperty().get());
        classifyAllExamples();
        
        primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                scpAll.setPrefViewportWidth(newValue.doubleValue());
                rctBack.setWidth(Math.max(newValue.doubleValue()-2,trainRoot.getBoundsInLocal().getWidth()-2));
            }
        });
        primaryStage.heightProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                scpAll.setPrefViewportHeight(newValue.doubleValue());
                rctBack.setHeight(Math.max(newValue.doubleValue()-2,trainRoot.getBoundsInLocal().getHeight()-2));
                ppm.setHeight(Math.max(trainRoot.getBoundsInLocal().getHeight()-2, stg.getHeight()-2)- 150);
            }
        });
    }   
    
    @Override
    public void stop() {
        try {
            FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + "\\data\\bbox.lib");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            ChuckBoard.saveRecordedExamples(ppm.getAllExamples(), System.getProperty("user.dir") + "\\data\\bbox.wav");
            WekinatorSingleton.getWekinator().saveWekinatorToFile(new File(System.getProperty("user.dir") + "\\data\\wekinator.data"));
            SoundLibrary.save(oos);
            oos.writeObject(ppm);
            oos.writeDouble(VolumeMeter.maxAdcProperty().get());
            oos.writeDouble(VolumeMeter.thresholdProperty().get());
            oos.writeDouble(knbMaster.valueProperty().get());
            oos.close();
            fos.close();
            //WekinatorSingleton.getWekinator().disconnectOSC();
            ChuckBoard.stop();
            WekinatorBeatboxLogger.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        WekinatorSingleton.stopWekinator();
        ConfigurationHelper.saveProperties();
    }
    
    private boolean loadStuffFromFile() {
        try {
            ConfigurationHelper.loadProperties();
            FileInputStream fis = new FileInputStream(System.getProperty("user.dir") + "\\data\\bbox.lib");
            ObjectInputStream ois = new ObjectInputStream(fis);
            ChuckBoard.loadRecordedExamples(System.getProperty("user.dir") + "\\data\\bbox.wav");
            WekinatorSingleton.loadFromFile(new File(System.getProperty("user.dir") + "\\data\\wekinator.data"));
            SoundLibrary.load(ois);
            ppm = (PadPaneLibraryBrowser) ois.readObject();
            VolumeMeter.maxAdcProperty().set(ois.readDouble());
            VolumeMeter.thresholdProperty().set(ois.readDouble());
            vol = ois.readDouble();
            fis.close();
            ois.close();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    //ONLY CALL THIS FROM SampleHolderPad, never by itself
    public static void dimAllButSamplePad(SampleHolderPad n, Point2D scenePt,double d) {
        app.dimAllButSamplePadMe(n, scenePt,d);
    }
    
    public static void undim() {
        app.undimMe();
    }
    
    /**
     * Open a dialog for a user to pick a sound
     * @effects sets result to the chosen sound when the user is done with the input dialog
     * i.e., it doesn't pause the main thread to wait for the user to finish
     */
    public static void getSound(final ObjectProperty<Sound> result) {
        FileChooser fc = new FileChooser();
        String file;
        if (ConfigurationHelper.defaultSoundPath.isDirectory())
            fc.setInitialDirectory(ConfigurationHelper.defaultSoundPath);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("WAV audio files","*.wav"));
        fc.setTitle("Select Sound");
        try {
            retVal = fc.showOpenDialog(stg);
        } catch (Exception ex) {
            fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("WAV audio files","*.wav"));
            fc.setTitle("Select Sound");
            retVal = fc.showOpenDialog(stg);
        }

        if (retVal != null) {
            file = retVal.getAbsolutePath();
        } else {
            return;
        }
        
        ConfigurationHelper.defaultSoundPath = new File(retVal.getParentFile().getAbsolutePath());
        
        //get name
//        String name = JOptionPane.showInputDialog("Enter sound name");
//        if (name == null) 
//            return null;
//
//        return new Sound(name,retVal,Color.rgb((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255)));
        
        //get name using not awt stuff
        InputDialog.showDialog(stg, "Enter sound name", new InputHandler() {
            public void onInputSent(String input) {
                result.set(new Sound(input,retVal,Color.rgb((int)(Math.random()*255), (int)(Math.random()*255), (int)(Math.random()*255))));
            }
        });
        
    }
    
    private void undimMe() {
       //remove the dimming rectangle
        trainRoot.getChildren().remove(rctDim);
        
        //give back the pad to the active pad pane
        trainRoot.getChildren().remove(tmpPad);
        ppm.activePadPaneProperty().get().finishDim(tmpPad);
    }
    
    /**
     * Trains wekinator on the current examples in all the pads. Also colors in the examples the
     * class that they are classified as using the built classifier
     */
    public static void trainOnAllPads(PadPane toTrain) {       
        int[] exIds = new int[0];
        int[] classIds = new int[0];
        
        for (SampleHolderPad p : toTrain.getPads()) {
            exIds = concat(exIds, p.getExampleIds());
            classIds = concat(classIds, p.getClassIds());
            
        }
        
        if (exIds.length > 0) {
            try {
                WekinatorSingleton.getWekinator().setSelectedExamplesAndClasses(exIds,classIds);
                classifyAllExamples();
            } catch (Exception ex) {
                Logger.getLogger(BeatBoxNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void trainOnAllPads() {
        if (ppm != null) 
        trainOnAllPads(ppm.activePadPaneProperty().get());
    }

    /**
     * @effect runs current classifier on all examples in the active pad pane and gives them
     * their classified sound class, which changes their color. Also outlines confused example holders
     */
    public static void classifyAllExamples() {
        System.out.println("------------------------Coloring----------------------");
        if (ppm == null)
            return;
        
        int confuse[][] = 
            new int[100][101];
        Set<Integer> cvs = new HashSet<Integer>();
        
        for (SampleHolderPad p : ppm.activePadPaneProperty().get().getPads()) {
            if (!p.getExamples().isEmpty()) {
                confuse[p.getSoundClass().getClassValue()][100] = p.getExamples().size();
                cvs.add(p.getSoundClass().getClassValue());
                for (Example e : p.getExamples()) {
                    //Get the vector
                    int[] res = WekinatorSingleton.getWekinator().getNearestNeighborClassesForInstance(e.getExampleId());
                    System.out.println(Arrays.toString(res));
                    Map<Integer,Integer> classCounts = new HashMap<Integer,Integer>();
                    for (int i = 0; i < res.length; i++) {
                        if (classCounts.get(res[i]) == null)
                            classCounts.put(res[i], 0);
                        classCounts.put(res[i], classCounts.get(res[i])+1);
                    }
                    //leave one out if it exists
                    if (classCounts.containsKey(e.getSoundClass()))
                        classCounts.put(e.getSoundClass().getClassValue(),classCounts.get(e.getSoundClass().getClassValue()) - 1);

                    //find max
                    int maxClass = -1;
                    int maxValue = -1;
                    for (Integer i : classCounts.keySet()) {
                        if (classCounts.get(i) > maxValue) {
                            maxValue = classCounts.get(i);
                            maxClass = i;
                        }
                    }

                    if (SoundClass.getFromClass(maxClass).getClassValue() != e.getClassifiedClass().getClassValue()) {
                        e.flash();
                    }
                    e.setClassifiedClass(SoundClass.getFromClass(maxClass));
                    confuse[p.getSoundClass().getClassValue()][e.getClassifiedClass().getClassValue()]++;
                }
            }
        }
        
        //now the array is filled, we can outline example holders
        for (SampleHolderPad p : ppm.activePadPaneProperty().get().getPads()) {
            p.setEmphasize(false);
            if (!p.getExamples().isEmpty()) {
                int tot = p.getExamples().size();
                if (confuse[p.getSoundClass().getClassValue()][p.getSoundClass().getClassValue()] < (int)tot*(2.0/3)) {
                    p.setEmphasize(true);
                } 
                if (!ONLY_FALSE_POSITIVES) {
                    //search for other
                    for (int i : cvs) {
                        //if this pad makes up more than 1/3 of the other pad's examples
                        if (i != p.getSoundClass().getClassValue() && 
                                confuse[i][p.getSoundClass().getClassValue()] >= Math.round((2.0/3)*confuse[i][100])) {
                            p.setEmphasize(true);
                        }
                    }
                }
            }
        } 
    }
    
    /**
     * @effect runs current classifier on all examples in the active pad pane and gives them
     * their classified sound class, which changes their color
     */
//    public static void classifyAllExamples() throws Exception {
//        if (ppm == null)
//            return;
//        for (SampleHolderPad p : ppm.activePadPaneProperty().get().getPads()) {
//            if (!p.getExamples().isEmpty()) {
//                for (Example e : p.getExamples()) {
//                    //Get the vector
//                    int[] res = WekinatorSingleton.getWekinator().getNearestNeighborClassesForInstance(e.getExampleId());
//                    System.out.println(Arrays.toString(res));
//                    Map<Integer,Integer> classCounts = new HashMap<Integer,Integer>();
//                    for (int i = 0; i < res.length; i++) {
//                        if (classCounts.get(res[i]) == null)
//                            classCounts.put(res[i], 0);
//                        classCounts.put(res[i], classCounts.get(res[i])+1);
//                    }
//                    //leave one out if it exists
//                    if (classCounts.containsKey(e.getSoundClass()))
//                        classCounts.put(e.getSoundClass().getClassValue(),classCounts.get(e.getSoundClass().getClassValue()) - 1);
//
//                    //find max
//                    int maxClass = -1;
//                    int maxValue = -1;
//                    for (Integer i : classCounts.keySet()) {
//                        if (classCounts.get(i) > maxValue) {
//                            maxValue = classCounts.get(i);
//                            maxClass = i;
//                        }
//                    }
//
//                    if (SoundClass.getFromClass(maxClass).getClassValue() != e.getClassifiedClass().getClassValue()) {
//                        e.flash();
//                    }
//                    e.setClassifiedClass(SoundClass.getFromClass(maxClass));
//                }
//            }
//        }
//    }
    
    //scenePt is the scene coordinates to place the pad at
    private void dimAllButSamplePadMe(SampleHolderPad n, Point2D scenePt, double d) {
        rctDim = new Rectangle();
        rctDim.setCursor(Cursor.HAND);
        rctDim.setWidth(rctBack.getWidth());
        rctDim.setHeight(rctBack.getHeight());
        rctDim.setLayoutX(0);
        rctDim.setLayoutY(0);
        rctDim.setFill(Color.rgb(0, 0, 0,0.5));
        rctDim.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                undim();
            }
        });
        rctDim.setOpacity(d);
        trainRoot.getChildren().add(trainRoot.getChildren().size(), rctDim);
        n.setLayoutX(trainRoot.sceneToLocal(scenePt).getX());
        n.setLayoutY(trainRoot.sceneToLocal(scenePt).getY());
        trainRoot.getChildren().add(n);
        tmpPad = n;
    }
    
    public static void focusSamplePad(SampleHolderPad n,double d) {
        app.focusSamplePadMe(n,d);
    }
    
    static boolean isRecordReady() {
        return ppm.activePadPaneProperty().get() != null && ppm.activePadPaneProperty().get().getPads().size() > 0;
    }
    
    /**
     * like the dimAllBut function, but doesn't lightbox
     * @param n 
     */
    private void focusSamplePadMe(SampleHolderPad n,double d) {
        ppm.activePadPaneProperty().get().dimAllButPad(n,d);
    }
    
    private static int[] concat(int[] first, int[] second) {
        int[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
    
    
    static Collection<SampleHolderPad> getAllPads() {
        Set<SampleHolderPad> res = new HashSet<SampleHolderPad>();
        
        for (PadPane p : ppm.getAllPadPanes()) {
            res.addAll(p.getPads());
        }
        
        return res;
    }
    
    public static PadPane getActivePadPane() {
        return ppm.activePadPaneProperty().get();
    }
    
    static PadPaneLibraryBrowser getPadPaneLibraryBrowser() {
        return ppm;
    }
    
    static void logPads() {
//        if (ppm != null) {
//            int num = ppm.activePadPaneProperty().get().getPads().size();
//            int padIds[] = new int[num];
//            boolean isPadEnabled[] = new boolean[num];
//            int numExamplesInPad[] = new int[num];
//            int exampleInPadClassifiedAsPad[][] = new int[num][];
//            int i = 0;
//            for (SampleHolderPad p : ppm.activePadPaneProperty().get().getPads()) {
//                padIds[i] = p.getSoundClass().getClassValue();
//                isPadEnabled[i] = !p.isMuted();
//                numExamplesInPad[i] = p.getExampleIds().length;
//
//                exampleInPadClassifiedAsPad[i] = new int[p.getExampleIds().length];
//                int j = 0;
//                for (Example e : p.getExamples()) {
//                    exampleInPadClassifiedAsPad[i][j++] = e.getClassifiedClass().getClassValue();
//                }
//
//                i++;
//            }
//            WekinatorBeatboxLogger.logPadSummary(padIds, isPadEnabled, numExamplesInPad, exampleInPadClassifiedAsPad);
//        }
    }
    
//    static void addClickHandler(EventHandler<ActionEvent> handle) {
//        clickHandlers.add(handle);
//    }
//    
//    //tell bbox to alert all objects that a click has been made and they
//    //should close their windows
//    static void clickMade(Object source) {
//        for (EventHandler<ActionEvent> e : clickHandlers)
//            e.handle(new ActionEvent(source,null));
//    }
    
    
    
}
