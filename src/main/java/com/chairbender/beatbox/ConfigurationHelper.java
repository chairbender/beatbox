/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import java.util.*;
import java.io.*;
import java.lang.System;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Class for saving and getting configuration information
 * @author Kyle
 */

class ConfigurationHelper {
    //default path to open the file browser to
    public static File defaultSoundPath;
    public static String defaultDataPath;
    public static double threshold;
    private static Properties p;
    
    //Populate all static fields with values from file
    public static void loadProperties() throws FileNotFoundException, IOException {
        p = new Properties();
        try {
            p.load(new FileInputStream(System.getProperty("user.dir") + "\\data\\user.props"));
        } catch (Exception e) {
            e.printStackTrace();
            FileOutputStream o = new FileOutputStream(System.getProperty("user.dir") + "\\data\\user.props");
            o.close();
            p.load(new FileInputStream(System.getProperty("user.dir") + "\\data\\user.props"));
        }
        defaultSoundPath = new File(p.getProperty("default_path_relative"));
        defaultDataPath = p.getProperty("default_data_path");
        threshold = Float.parseFloat(p.getProperty("threshold"));
    }

    //save all current static fields to file
    public static void saveProperties() {
        p.setProperty("default_path_relative", ResourceUtils.getRelativePath(defaultSoundPath.getAbsolutePath(), System.getProperty("user.dir"), "\\"));
        p.setProperty("default_data_path", defaultDataPath);
        p.setProperty("threshold","" + threshold);

        try {
            p.store(new FileOutputStream(System.getProperty("user.dir") + "\\data\\user.props"),"");
        } catch (IOException ex) {
            Logger.getLogger(ConfigurationHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}