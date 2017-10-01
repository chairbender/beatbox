/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import javafx.beans.value.ObservableValue;
import javafx.scene.*;
import java.util.*;
import javafx.scene.Group;
import javafx.scene.shape.*;
import beatbox.Granularity.GranularityType;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
//TODO: Make the scrolling out of view work like before when b42 drops

/**Loop displays grid of hits
 * it only displays hits that have timing data,
 * so you can add the Hit and the TimingData separately.
 * There is a weird error when you try to record twice using the same loop.
 *It's better to just make a new Loop when you want to re-record
 *
 * @author Kyle
 */
public class Loop extends Parent {

    private Loop me = this;
    private final int DEFAULT_WIDTH = 400;
    private final int PIXELS_PER_32ND_NOTE = 10; //vertical distance represented
    //by a 32nd note
    private final int MAX_LOOP_WIDTH = 10000;
    //thickness to draw grid lines at for whole note, quarter note, and sixteenth
    //note
    private final double GRID_THICKNESS[] = {2.0, 1.0, 0.5};
    private final double GRID_HEIGHT[] = {50, 25, 10};
    private final Color GRID_COLOR[] = {Color.DARKBLUE, Color.BLUE, Color.BLUE};
    private int bpm;
    private ObjectProperty<GranularityType> quantization = new SimpleObjectProperty<GranularityType>(GranularityType.QUARTER_NOTE);

    public ObjectProperty<GranularityType> quantizationProperty() {
        return quantization;
    }
    private int width = DEFAULT_WIDTH;
    private int height = 100;
    private Line endLine; //line marking the end of the loop
    private double zoom;
    private Group notes;
    private Group gridLines;
    //private Group loopAreaGroup;
    //Map from integer to TimedHit
    private Map<Integer, TimedHit> incompleteHits = new HashMap<Integer, TimedHit>();
    private ScrollPane view;
    private Line playbackHead;
    //scroll along with headTransX
    private IntegerProperty headTransX = new SimpleIntegerProperty(0);
    private boolean inNote = false;
    private Timeline headTrans;
    private boolean scrolling = false; //whether head is scrolling
    private boolean recording = false;
    private PadPane padPane;
    private int countInBeats;
    private IntegerProperty loopId = new SimpleIntegerProperty(-1);
    private LoopManager lm;

    public int getLoopId() {
        // System.out.println("Getting loop id");
        return loopId.get();
    }
    private DoubleProperty volumeProperty = new SimpleDoubleProperty();

    public DoubleProperty volumeProperty() {
        return volumeProperty;
    }

    public Loop(PadPane p, LoopManager lm, int width, int height) {
        super();
        this.lm = lm;
        padPane = p;
        loopId.addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                // System.out.println("Changed loopId" + newValue);
            }
        });

        gridLines = new Group();
        notes = new Group();
        this.width = width;
        this.height = height;
        initialize();

        view = new ScrollPane();
        view.setPrefViewportWidth(width);
        view.setPrefViewportHeight(height);
        view.setVbarPolicy(ScrollBarPolicy.NEVER);
        view.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        //getChildren().add(view);

        Group loopAreaGroup = new Group();
        getChildren().add(loopAreaGroup);
        //view.setContent(loopAreaGroup);

        playbackHead = new Line();
        playbackHead.setEndY(height);
        Bindings.bindBidirectional(playbackHead.startXProperty(), headTransX);
        Bindings.bindBidirectional(playbackHead.endXProperty(), playbackHead.startXProperty());
        loopAreaGroup.getChildren().add(playbackHead);
        loopAreaGroup.getChildren().add(gridLines);
        loopAreaGroup.getChildren().add(notes);

        endLine = new Line();
        endLine.setEndY(height);
        endLine.setStrokeWidth(3);
        endLine.setOnMouseEntered(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (!scrolling) {
                    setCursor(Cursor.HAND);
                }
            }
        });
        endLine.setOnMouseExited(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (!scrolling) {
                    setCursor(Cursor.DEFAULT);
                }
            }
        });
        endLine.setOnMouseDragged(new EventHandler<MouseEvent>() {

            public void handle(MouseEvent event) {
                if (!scrolling) {
                    int closestX;
                    if (event.getX() % (PIXELS_PER_32ND_NOTE * 2) < (PIXELS_PER_32ND_NOTE * 2) / 2) {
                        closestX = (int) (event.getX()
                                - (event.getX() % (PIXELS_PER_32ND_NOTE * 2)));
                    } else {
                        closestX = (int) (event.getX() + ((PIXELS_PER_32ND_NOTE * 2)
                                - (event.getX() % (PIXELS_PER_32ND_NOTE * 2))));
                    }

                    endLine.setStartX(closestX);
                    endLine.setEndX(closestX);
                    Set<Line> delLines = new HashSet<Line>();

                    //if it shrunk, remove trailing gridLines
                    for (Node n : gridLines.getChildren()) {
                        Line l = (Line) n;
                        if (l.getEndX() > closestX) {
                            delLines.add(l);
                        }
                    }

                    for (Line l : delLines) {
                        gridLines.getChildren().remove(l);
                    }

                    for (int i = 1; i < notes.getChildren().size() + 1; i++) {
                        HitNoteAdapter note = (HitNoteAdapter) notes.getChildren().get(notes.getChildren().size() - i);
                        if (note.getLayoutX() >= closestX) {
                            notes.getChildren().remove(note);
                        }
                    }

                    drawGridBetween((int) (((Line) gridLines.getChildren().get(gridLines.getChildren().size() - 1)).getStartX()
                            + getPixelsBetweenLines()),
                            closestX + getPixelsBetweenLines());
                }
            }
        });
        loopAreaGroup.getChildren().add(endLine);

        headTransX.addListener(new ChangeListener<Number>() {

            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //System.out.println("x: " + playbackHead.getStartX());
                if (scrolling) {
                    if (headTransX.get() > ((Line) gridLines.getChildren().get(gridLines.getChildren().size() - 1)).getStartX() - 10) {
                        if (recording) {
                            addGridLines();
                            view.hvalueProperty().set(1);
                        }
                    }
                    if (headTransX.get() > view.getPrefViewportWidth() - 10) {
                        view.hvalueProperty().set(1);
                    }
                    if (headTransX.get() == 0) {
                        view.hvalueProperty().set(0);
                    }

                    //check for collision
                    boolean done = false;
                    for (Node n : notes.getChildren()) {
                        if (n.localToScene(
                                n.getBoundsInLocal()).intersects(playbackHead.localToScene(
                                playbackHead.getBoundsInLocal()))) {
                            if (!inNote && !recording) {
                                ChuckBoard.play(((HitNoteAdapter) n).getHitNote().getSoundClass(), volumeProperty.get());
                                inNote = true;
                            }
                            done = true;
                        }
                    }
                    if (!done) {
                        inNote = false;
                    }
                }
            }
        });

    }

    private void initialize() {
        //fill with gridlines every quantization length
        drawGridBetween(0, width - 1);
    }

    //add gridlines (call this when the scrollview grows past its specified
    //width)
    private void addGridLines() {
        int lastLineX = (int) (((Line) gridLines.getChildren().get(gridLines.getChildren().size() - 1)).getStartX());

        drawGridBetween(lastLineX + getPixelsBetweenLines(),
                lastLineX + getPixelsBetweenLines() + width);
    }

    private int getPixelsBetweenLines() {
        int gn = Granularity.granInSmallest(GranularityType.SIXTEENTH_NOTE);
        int ppn = PIXELS_PER_32ND_NOTE;
        return Granularity.granInSmallest(GranularityType.SIXTEENTH_NOTE) * PIXELS_PER_32ND_NOTE;
    }

    //TODO: this implementation is not so efficient.
    /**Adds the hit data to the loop. If
     * timing data for the same id exists in this loop, will render
     * the hit. Otherwise, waits until timing data with hit's id is received to
     * render. If hit overlaps another hit, doesn't add it
     *
    //    */
    public void addHit(Hit hit) {
        //println("addhit: {hit.id}");
        if (incompleteHits.containsKey(hit.getId())) {
            incompleteHits.get(hit.getId()).setHit(hit);
        } else {
            incompleteHits.put(hit.getId(), new TimedHit(hit, null));
        }
        generateHitNotes();
    }

    /**Adds the timing data to the loop. If
     * hit data for the same id exists in this loop, will render
     * the hit. Otherwise, waits until hit data with time's id is received to
     * render.
     */
    public void addTiming(HitTiming time) {
        // println("addtiming: {time.getId()}");
        //convert the time based on the count in offset
        // println("before: {time.getOffset()}");
        time.setOffset(time.getOffset() - ((1.0 / bpm) * countInBeats * 60000));
        //println("after: {time.getOffset()}");
        if (incompleteHits.containsKey(time.getId())) {
            incompleteHits.get(time.getId()).setTiming(time);
        } else {
            incompleteHits.put(time.getId(), new TimedHit(null, time));
        }
        generateHitNotes();
    }

    //checks to see if any TimedHits are complete. If so, removes them from
    //the incomplete map and displays them. Doesn't add overlapping hits.
    private void generateHitNotes() {
        //loop through the map
        Set<Integer> toRemove = new HashSet<Integer>();
        for (int key : incompleteHits.keySet()) {
            TimedHit th = incompleteHits.get(key);
            //if it's completed
            if (th != null && th.getHit() != null && th.getTiming() != null) {
                toRemove.add(key);
            }
        }

        for (int key : toRemove) {
            TimedHit th = incompleteHits.remove(key);
            HitNote hn = new HitNote(bpm, th, quantization.get());
            //check for duplicate
            boolean dupe = false;
            //println("position: {hn.getPosition()}");
            for (Node n : notes.getChildren()) {
                HitNoteAdapter note = (HitNoteAdapter) n;
                if (note.getHitNote().getPosition() == hn.getPosition()) {
                    dupe = true;
                }
            }

            if (dupe == false) {
                //Check if it falls before the measure
                if (hn.getPosition() >= 0) {
                    final HitNoteAdapter hna = new HitNoteAdapter(PIXELS_PER_32ND_NOTE, hn, padPane);
                    hna.setLayoutX(hn.getPosition() * PIXELS_PER_32ND_NOTE);
                    hna.setLayoutY(height / 2 - PIXELS_PER_32ND_NOTE / 2);
                    hna.setCursor(Cursor.HAND);
                    hna.setOnMouseClicked(new EventHandler<MouseEvent>() {

                        public void handle(MouseEvent event) {
                            if (event.getButton() == MouseButton.PRIMARY) {
                                if (!lm.isHitNoteFocused()) {
                                    hna.setPickerVisible(!hna.isPickerVisible());
                                    lm.focusHitNote(hna, me);
                                }
                            }
                            if (event.getButton() == MouseButton.SECONDARY) {
                                notes.getChildren().remove(hna);
                            }
                        }
                    });
                                
                    notes.getChildren().add(notes.getChildren().size(),hna);
                }
            }
        }
    }

    //Set the number of beats to offset the received timing information from,
    //I.e., if you had a 4 beat count in, pass 4 to this before recording.
    private void setCountInRecordOffset(Integer beats) {
        countInBeats = 4;
    }

    private void drawGridBetween(int startX, int endX) {
        int curX = startX;
        while (curX < endX) {
            int gheight = getGridHeight();
            Line l = new Line();
            l.setStartX(curX);
            l.setStartY(height / 2 - gheight / 2);
            l.setEndX(curX);
            l.setEndY(height / 2 + gheight / 2);
            l.setStroke(getGridLineColor());
            l.setStrokeWidth(getGridLineThickness());

            gridLines.getChildren().add(l);

            curX += getPixelsBetweenLines();
        }
    }

    //returns the thickness to draw the
    //next gridline with
    private double getGridLineThickness() {
        int numLines = gridLines.getChildren().size();
        if (numLines % 16 == 0) {
            return GRID_THICKNESS[0];
        } else if (numLines % 4 == 0) {
            return GRID_THICKNESS[1];
        } else {
            return GRID_THICKNESS[2];
        }
    }

    //returns the thickness to draw the
    //next gridline with
    private Color getGridLineColor() {
        int numLines = gridLines.getChildren().size();
        if (numLines % 16 == 0) {
            return GRID_COLOR[0];
        } else if (numLines % 4 == 0) {
            return GRID_COLOR[1];
        } else {
            return GRID_COLOR[2];
        }
    }

    private int getGridHeight() {
        int numLines = gridLines.getChildren().size();
        if (numLines % 16 == 0) {
            return (int) GRID_HEIGHT[0];
        } else if (numLines % 4 == 0) {
            return (int) GRID_HEIGHT[1];
        } else {
            return (int) GRID_HEIGHT[2];
        }
    }

    //scroll the head across indefinitely,
    //starting from position 0
    //recording indicates if it should loop or add new bars when it reaches
    //the end of the loop
    public void startScrolling(boolean recording) {
        scrolling = true;
        headTransX.set(0);
        this.recording = recording;
        //calculate scroll rate.
        int pixelsPerSecond = (bpm * PIXELS_PER_32ND_NOTE * 8) / 60;
        //animate
        if (recording) {
            headTrans = new Timeline();
            KeyValue kv = new KeyValue(headTransX, 0);
            KeyFrame kf = new KeyFrame(Duration.ZERO, kv);
            headTrans.getKeyFrames().add(kf);
            KeyValue kv2 = new KeyValue(headTransX, MAX_LOOP_WIDTH);
            KeyFrame kf2 = new KeyFrame(
                    Duration.millis((MAX_LOOP_WIDTH / pixelsPerSecond) * 1000),
                    kv2);
            headTrans.getKeyFrames().add(kf2);
        } else {
            headTrans = new Timeline();
            KeyValue kv = new KeyValue(headTransX, 0);
            KeyFrame kf = new KeyFrame(Duration.ZERO, kv);
            headTrans.getKeyFrames().add(kf);
            KeyValue kv2 = new KeyValue(headTransX, ((Line) gridLines.getChildren().get(gridLines.getChildren().size() - 1)).getStartX());
            KeyFrame kf2 = new KeyFrame(
                    Duration.millis((((Line) gridLines.getChildren().get(gridLines.getChildren().size() - 1)).getStartX()
                    / pixelsPerSecond) * 1000),
                    kv2);
            headTrans.getKeyFrames().add(kf2);
            headTrans.setCycleCount(Timeline.INDEFINITE);
        }

        view.setHvalue(0.0);
        headTrans.playFromStart();
    }

    //stops scrolling
    //if recording is true, trims the grid so that only lines up to
    //the last 16th note where the play head was will be kept
    public void stopScrolling() {
        if (recording) {
            trimGrid();
            endLine.setStartX(((Line) gridLines.getChildren().get(gridLines.getChildren().size() - 1)).getStartX());
            endLine.setEndX(endLine.getStartX());
        }

        scrolling = false;
        view.setHvalue(0);
        if (headTrans != null) {
            headTrans.stop();
        }

    }

    //trims the grid so that only lines up to
    //the last 16th note where the play head was will be kept
    private void trimGrid() {
        int lastX = headTransX.get();
        while (((Line) gridLines.getChildren().get(gridLines.getChildren().size() - 1)).getStartX() > headTransX.get()) {
            //delete gridLines[gridLines.size() - 1] from gridLines;
            gridLines.getChildren().remove(gridLines.getChildren().get(gridLines.getChildren().size() - 1));
        }
    }

    //reset loop to initial state
    public void reset() {
        gridLines.getChildren().clear();
        notes.getChildren().clear();
        incompleteHits.clear();
        initialize();
    }

    public List<HitNote> getNotes() {
        List<HitNote> res = new ArrayList<HitNote>();
        for (Node hna : notes.getChildren()) {
            res.add(res.size(), ((HitNoteAdapter) hna).getHitNote());
        }
        return res;
    }

    //returns the time (in ms) that the loop ends
    public double getEndTime() {
        return (((Line) gridLines.getChildren().get(gridLines.getChildren().size() - 1)).getStartX() / PIXELS_PER_32ND_NOTE) / 8 / bpm * 60000;
    }

    //change the bpm this loop plays at
    public void setBpm(int bpm) {
        this.bpm = bpm;
        for (Node hn : notes.getChildren()) {
            ((HitNoteAdapter) hn).getHitNote().setBpm(bpm);
        }

    }

    //sets the id of this loop.
    public void setLoopId(int id) {
        //System.out.println("Setting loop id");
        loopId.set(id);
    }

    void addNote(HitNote hn) {
        final HitNoteAdapter hna = new HitNoteAdapter(PIXELS_PER_32ND_NOTE, hn, padPane);
        hna.setLayoutX(hn.getPosition() * PIXELS_PER_32ND_NOTE);
        hna.setLayoutY(height / 2 - PIXELS_PER_32ND_NOTE / 2);
        hna.setCursor(Cursor.HAND);
        hna.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
//                BeatBoxNew.clickMade(hna);
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (!lm.isHitNoteFocused()) {
                        hna.setPickerVisible(!hna.isPickerVisible());
                        lm.focusHitNote(hna, me);
                    }
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    notes.getChildren().remove(hna);
                }
            }
        });
        hna.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                lm.fadeInHelp();

            }
        });
        hna.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                lm.fadeOutHelp();
            }
        });        

        //figure out where it goes
        int ind = -1;
        for (int i = 0; i < notes.getChildren().size(); i++) {
            if (((HitNoteAdapter) notes.getChildren().get(i)).getHitNote().getPosition() < hn.getPosition()) {
                ind = i;
                break;
            }
        }

        notes.getChildren().add(ind + 1, hna);
    }
}
