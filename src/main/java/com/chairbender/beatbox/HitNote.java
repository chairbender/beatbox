/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

import com.chairbender.beatbox.Granularity.GranularityType;
import javafx.scene.paint.Color;

/**Hit note represents a Hit in note form in a loop.
 *
 * @absfield position : the position of this note in units of the smallest
 * granularity from the start of the loop.
 * @author Kyle
 */
class HitNote {
//granularity of the representation. 1 = whole note
    private GranularityType quantization;
    //number of granularity lengths from start of this hit's loop
    private int offset;
    private TimedHit timedHit;
    private int bpm;

    public HitNote(int bpm, TimedHit timedHit, GranularityType quantization) {
        this.quantization = quantization;
        this.timedHit = timedHit;
        //convert the bpm to bpms (milliseconds)
        double bpms = bpm / 60.0 / 1000.0;
        //now get the amount of whole notes that the hit offset represents
        double hitOffsetQuarterNote = bpms * timedHit.getTiming().getOffset();
        double hitOffsetWholeNote = hitOffsetQuarterNote / 4;
        //convert it to the amount of <quantization> notes that the hit offset
        //represents
        double hitOffsetQuantization = (hitOffsetWholeNote /
                Granularity.granularityFraction(quantization));
        //round this offset
        offset = (int) Math.round(hitOffsetQuantization);
        this.bpm = bpm;
       // println("hitnote made: {offset}");
    }
    
    /**
     * @return the offset of this note from the start of its
     * measure in units of SMALLEST_GRANULARITY
     */
    public int getPosition() {
        return offset * Granularity.granInSmallest(quantization);
    }

    //change the bpm of this HitNote
    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    //TODO: Make playback actually play sound
    /**
     *
     * @return the color of this hitnote based on its soundClass
     */
    public Color getColor() {
        return timedHit.getHit().getSoundClass().getSound().getColor();
    }

    //return this note's class
    public int getClassValue() {
        return timedHit.getHit().getSoundClass().getClassValue();
    }

    //return the milliseconds after the start of the measure this hit note occurs
    public double getTimeOffsetInMilliseconds() {
        //TODO: this hardcoded conversion might be incorrect if the
        //SMALLEST_GRANULARITY is changed
        if (getPosition() == 0)
            return 0;
        else
            return (1.0 / (((bpm * 8)) / getPosition()) * 60000);
    }
    
    public SoundClass getSoundClass() {
        return this.timedHit.getHit().getSoundClass();
    }

    public void setSoundClass(SoundClass newValue) {
        this.timedHit.getHit().setSoundClass(newValue);
    }
}
