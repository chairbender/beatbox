/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import KyleWrapper.BeatboxWekinatorWrapper;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import wekinator.ChuckConfiguration;
import wekinator.ChuckRunner;

/**This uses the Singleton pattern on an instance of
 * BeatboxWekinatorWrapper.
 *
 * @specfield wekinator: a BeatboxWekinatorWrapper
 * @author Kyle
 */
class WekinatorSingleton {
    
    private static BeatboxWekinatorWrapper wekinator;
    private static final int NUM_FEATURES = 9;
    private static final int MAX_CLASSES = 100;
    private static int numClasses;
    private static boolean init = false;

    /**
     * @effects sets the number of classes in the wekinator instance
     * to num. This cannot be changed after wekinator has been instantiated.
     */
//    static void setNumClasses(int num) {
//        numClasses = num;
//    }

    /**
     *
     * @return the instance of BeatboxWekinatorWrapper with
     * 10 features and 100 classes (i.e. you can make as many as you like)
     */
    public static BeatboxWekinatorWrapper getWekinator() {
        
        if (wekinator == null) {
            System.out.println("made a wekinator instance");
            init = true;
            String[] featNames = new String[NUM_FEATURES];
            featNames[0] = "centroidAvg";
            featNames[1] = "centroidStdDev";
            featNames[2] = "centroidMin";
            featNames[3] = "centroidMax";
            featNames[4] = "rmsAvg";
            featNames[5] = "rmsStdDev";
            featNames[6] = "rmsMin";
            featNames[7] = "rmsMax";
            featNames[8] = "fftHighest";
            String rootDir = new File(".").getAbsolutePath().replace(".", "");
            System.out.println("The root directory is: " + rootDir);
            try {
                if (OSValidator.isWindows()) {
//                    ChuckRunner.exportConfigurationToChuckFileKyle(new ChuckConfiguration(),
//                            "/" + rootDir.replace("\\","/").substring(3) + "chuck",
//                            new File("/" + rootDir.replace("\\","/").substring(3) + "chuck/core_chuck/config.ck"));
                    wekinator = new BeatboxWekinatorWrapper(NUM_FEATURES, MAX_CLASSES);
                }  else if (OSValidator.isMac()) {
                    wekinator = new BeatboxWekinatorWrapper(NUM_FEATURES, MAX_CLASSES);
                } else {
                    System.err.println("ERROR: Operating system not supported.");
                }
            } catch (Exception ex) {
                Logger.getLogger(WekinatorSingleton.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return wekinator;
    }

    static void loadFromFile(File file) throws Exception {
        if (wekinator == null)
            wekinator = BeatboxWekinatorWrapper.loadFromFile(file);
    }

    static void stopWekinator() {
        wekinator = null;
    }
}
