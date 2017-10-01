/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.scene.*;
import javafx.scene.shape.*;
import com.illposed.osc.*;
import java.io.BufferedReader;
import java.io.Console;
import java.net.*;
import javafx.scene.paint.*;
import javafx.scene.input.*;
import java.util.*;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;

//TODO: hide the representation of the adapters more.

/**All the stuff for getting info from chuck
 *
 * @author Kyle
 */
public class ChuckBoard {

    private static final int receivePort = 6466;
    private static final int SEND_PORT = 6460;
    private static final int adcRecvPort = 6451;
    private static OSCPortIn receiver;
    private static OSCPortIn adcRecv;
    private static OSCPortOut sender;
    private static BooleanProperty savingOrLoading = new SimpleBooleanProperty(false);
    private static List<Integer> currentLoopIds;
    //for getting adc.last
    public static ADCListener ADC;
    //for getting timing data
    public static TimingListener timingListener;
    private static Process pChuck;
    private static LoggerThread errorThread;
    private static LoggerThread inputThread;

    //also starts running chuck
    public static void initialize() {
        try {
            //TODO: change when OSX support added
            if (OSValidator.isWindows()) {
                Runtime.getRuntime().exec(System.getProperty("user.dir") + "\\chuck\\Windows\\asio_chuck.exe --kill");
                Runtime.getRuntime().exec("taskkill /f /im chuck");
                Runtime.getRuntime().exec("taskkill /f /im chuck*32");
                Runtime.getRuntime().exec("taskkill /f /im asio_chuck");
                Runtime.getRuntime().exec("taskkill /f /im asio_chuck*32");
                Thread.sleep(5000);
                pChuck = Runtime.getRuntime().exec(System.getProperty("user.dir") + "\\chuck\\Windows\\asio_chuck.exe " + (System.getProperty("user.dir") + "\\chuck\\oscExtractorBBOX.ck").substring(2).replace("\\", "/"));
                BufferedReader input = new BufferedReader(new InputStreamReader(pChuck.getInputStream()));
                errorThread = new LoggerThread(pChuck.getErrorStream());
                inputThread = new LoggerThread(pChuck.getInputStream());
                String line = null;
                Thread.sleep(5000);
            }
            OSCPortIn rcv = new OSCPortIn(receivePort);
            receiver = rcv;
            adcRecv = new OSCPortIn(adcRecvPort);
            sender = new OSCPortOut(InetAddress.getByName("localhost"), SEND_PORT);
            timingListener = new TimingListener(receiver);
            ADC = new ADCListener(adcRecv);
            receiver.startListening();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //Plays back the recorded example, using the chuckId
    public static void playExample(Example e) {
        Object args[] = new Object[1];
        args[0] = e.getChuckId();
        OSCMessage msg = new OSCMessage("/playExample", Arrays.asList(args));
        try {
            sender.send(msg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     *Play the metronome at the specified bpm with the specified number of count in
     * beats, also marks
     * this time + countIn as the 0 for future /peakTime messages
     */
    public static void playMetronomeAndMark(int bpm, int countIn) {
        Object args[] = new Object[2];
        args[0] = bpm;
        args[1] = countIn;
        OSCMessage msg = new OSCMessage("/startMetroAndMark", Arrays.asList(args));
        try {
            sender.send(msg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Stops the metronome if it's playing
     */
    public static void stopMetronome() {
        Object args[] = new Object[0];
        OSCMessage msg = new OSCMessage("/stopMetro", Arrays.asList(args));
        try {
            sender.send(msg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //Play the file currently linked to the SoundClass
    public static void play(SoundClass sound, double volume) {
        Object args[] = new Object[2];
        args[0] = sound.getClassValue();
        args[1] = (int) (volume * 1000);
        try {
            sender.send(new OSCMessage("/playSound", Arrays.asList(args)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //Set the threshold for detecting a peak
    //db is in decibels
    public static void setThreshold(double db) {
        Object args[] = new Object[1];
        args[0] = (float) (db);
        try {
            sender.send(new OSCMessage("/setThreshold", Arrays.asList(args)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //puts chuck in "training mode", meaning
    //it waits longer between detected peaks (so
    //it doesn't detect a bunch of peaks for a single note).
    //Also, tells chuck to start actually recording the audio of
    //vocalizations to LiSa instances.
    //
    public static void setChuckTrainingMode(boolean train) {
        Object args[] = new Object[1];
        if (train) {
            args[0] = 1;
        } else {
            args[0] = 0;
        }
        try {
            sender.send(new OSCMessage("/trainingMode", Arrays.asList(args)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //save chuck's recorded example audio to the specified file
    //lst = all the examples to be saved
    public static void saveRecordedExamples(final List<Example> lst, String file) throws IOException {
        
        //new process:
        //get all examples in sorted order by chuckId
        //send them (the chuckIds) all to chuck, first with a size mssage and then the actual message
        //chuck records only those example ids to file
        //relabel all examples' chuckIds starting at 0 and incrementing by 1
        savingOrLoading.set(true);
        
        //send size and location message
        Object args[] = new Object[2];
        args[0] = lst.size();
        args[1] = file;
        
        //need to change examples now, in main thread
        final Object exs[] = new Object[lst.size()];
        int i = 0;
        for (Example e : lst) {
            exs[i++] = e.getChuckId();
            e.setChuckId(i-1);
        }
        System.out.println("exs: " + Arrays.toString(exs));
        
        receiver.addListener("/readyToSave", new OSCListener() {
            public void acceptMessage(Date date, OSCMessage oscm) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        System.out.println("fx: got readyToSave");
                        //now send the whole list
                        try {
                            System.out.println("fx: sent exampleList");
                            sender.send(new OSCMessage("/exampleList", Arrays.asList(exs)));
                        } catch (IOException ex) {
                            Logger.getLogger(ChuckBoard.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            //change this if the length of examples increases
                            Thread.sleep(lst.size()*100);
                            savingOrLoading.set(false);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ChuckBoard.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                
            }
        });
                
        //prepare listener
        receiver.addListener("/doneSaving", new OSCListener() {
            public void acceptMessage(Date date, OSCMessage oscm) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        System.out.println("fx: got doneSaving");
                        savingOrLoading.set(false);
                    }
                });
            }
        });
        
        sender.send(new OSCMessage("/saveRecordedExamples", Arrays.asList(args)));
        System.out.println("fx: sent saveRecordedExamples");
    }

    //tell chuck to load recorded example audio from the
    //specified file
    public static void loadRecordedExamples(String file) {

        //tell chuck to load
        savingOrLoading.set(true);
        receiver.addListener("/doneLoading", new OSCListener() {

            public void acceptMessage(Date date, OSCMessage oscm) {
                savingOrLoading.set(false);
            }
        });
        Object args[] = new Object[1];
        args[0] = file;
        try {
            sender.send(new OSCMessage("/loadRecordedExamples", Arrays.asList(args)));
            System.out.println("sent load message");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void removeLoop(int loopId) {
        Object args[] = new Object[1];
        args[0] = loopId;
        try {
            sender.send(new OSCMessage("/removeLoop", Arrays.asList(args)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**Associate the passed sound with the passed classValue in chuck.
     * loads the sound into chuck, linked to the passed classValue
     * @param absPath absolute path to the sound file (.wav)
     * @param classValue class value pertaining to this sound
     */
    public static void addSound(Sound snd, int classValue) {
        Object args[] = new Object[2];
        args[0] = snd.getPath();
        args[1] = classValue;
        try {
            sender.send(new OSCMessage("/addSound", Arrays.asList(args)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     *@param soundClass sound to set volume for
     *volume : volume to set sound to
     */
    public static void setClassVolume(SoundClass snd, double volume) {
        Object args[] = new Object[2];
        args[0] = (float) volume;
        args[1] = snd.getClassValue();
        try {
            sender.send(new OSCMessage("/setSoundGain", Arrays.asList(args)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //stops all osc activity and listening. Call when application is done
    //stops chuck, waits till saving/loading is done if it is currently happening
    //calls System.exit() when done
    public static void stop() {
        savingOrLoading.addListener(new ChangeListener<Boolean>() {

            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.out.println("changed!");
                if (OSValidator.isWindows()) {
                    try {
                        Runtime.getRuntime().exec(System.getProperty("user.dir") + "\\chuck\\Windows\\asio_chuck.exe --kill");
                        Runtime.getRuntime().exec("taskkill /f /im chuck");
                        Runtime.getRuntime().exec("taskkill /f /im chuck*32");
                        Runtime.getRuntime().exec("taskkill /f /im asio_chuck");
                        Runtime.getRuntime().exec("taskkill /f /im asio_chuck*32");
                        pChuck.destroy();
                        receiver.stopListening();
                        ChuckBoard.adcRecv.stopListening();
                        ChuckBoard.sender.close();
                        errorThread.stop();
                        inputThread.stop();                        
                        Runtime.getRuntime().exit(0);
                    } catch (IOException ex) {
                        Logger.getLogger(ChuckBoard.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        if (!savingOrLoading.get()) {
            System.out.println("changed!");
            if (OSValidator.isWindows()) {
                try {
                    Runtime.getRuntime().exec(System.getProperty("user.dir") + "\\chuck\\Windows\\asio_chuck.exe --kill");
                    Runtime.getRuntime().exec("taskkill /f /im chuck");
                    Runtime.getRuntime().exec("taskkill /f /im chuck*32");
                    Runtime.getRuntime().exec("taskkill /f /im asio_chuck");
                    Runtime.getRuntime().exec("taskkill /f /im asio_chuck*32");
                    pChuck.destroy();
                    receiver.stopListening();
                    ChuckBoard.adcRecv.stopListening();
                    ChuckBoard.sender.close();
                    errorThread.stop();
                    inputThread.stop();
                    Runtime.getRuntime().exit(0);
                } catch (IOException ex) {
                    Logger.getLogger(ChuckBoard.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    static void setVolume(double level) {
        Object ar[] = new Object[1];
        ar[0] = (int)(1000*level);
        try {
            System.out.println("setting master volume " + level);
            sender.send(new OSCMessage("/setMasterVolume", Arrays.asList(ar)));
        } catch (IOException ex) {
            Logger.getLogger(ChuckBoard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
class LoggerThread implements Runnable {

    Thread t;
    BufferedReader input;
    private boolean stop = false;

    LoggerThread(InputStream is) {
        input = new BufferedReader(new InputStreamReader(is));
        t = new Thread(this, "my thread");
        t.start();
    }

    public void stop() {
        stop = true;
    }

    public void run() {
        stop = false;
        while (!stop) {
            try {
                ///byte[] byteArray = new byte[2];
                int b = input.read();
                // input.read

                if (b == -1) {
                    stop = true;
                    // System.out.println("made it to end of stream");
                } else {
                    //TODO: send to console in reasonable way
                    System.out.print((char) b);
                    //String s = String.
                    //Console.getInstance().log(String.valueOf((char) b));
                }
            } catch (IOException ex) {
                Logger.getLogger(LoggerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
