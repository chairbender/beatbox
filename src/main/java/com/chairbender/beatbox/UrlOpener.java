package com.chairbender.beatbox;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UrlOpener{
    public static void openURL(String url){
        try {
            java.awt.Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            Logger.getLogger(UrlOpener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}