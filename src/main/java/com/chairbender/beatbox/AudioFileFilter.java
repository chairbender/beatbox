package com.chairbender.beatbox;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import javax.swing.filechooser.*;

/**
 *
 * @author Kylea
 */
public class AudioFileFilter extends FileFilter{

    @Override
    public boolean accept(File f) {
        return f.isDirectory() || f.getName().toLowerCase().endsWith(".wav");
    }

    @Override
    public String getDescription() {
        return "WAV audio file";
    }

}
