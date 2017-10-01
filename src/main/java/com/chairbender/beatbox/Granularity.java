/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

/**
 *
 * @author Kyle
 */
class Granularity {
    public static final GranularityType SMALLEST_GRANULARITY = GranularityType.THIRTYSECOND_NOTE;

    private GranularityType gType;

    public Granularity(GranularityType g) {
        this.gType = g;
    }
    
    public GranularityType getGranularityType() {
        return gType;
    }

    public static enum GranularityType {
        WHOLE_NOTE,
        QUARTER_NOTE,
        EIGTH_NOTE,
        SIXTEENTH_NOTE,
        THIRTYSECOND_NOTE
    }

    //returns the fraction of a measure the granularity represents
    //i.e. quarter note is 1/4, whole note is 1,
    public static double granularityFraction(GranularityType g) {
        switch (g) {
            case WHOLE_NOTE: return 1.0;
            case QUARTER_NOTE: return 1.0/4;
            case EIGTH_NOTE: return 1.0/8;
            case SIXTEENTH_NOTE: return 1.0/16;
            case THIRTYSECOND_NOTE: return 1.0/32;
            default: return 0;
        }
    }

    /**
     *
     * @param g granularity
     * @return the number of SMALLEST_GRANULARITY's that fit
     * in g. (i.e. if g == QUARTER_NOTE, returns 8. if g ==
     * WHOLE_NOTE, returns 32)
     */
    public static Integer granInSmallest(GranularityType g) {
        return (int) Math.round(granularityFraction(g) *
                (1/granularityFraction(SMALLEST_GRANULARITY)));
    }

    /**
     *
     * @return string representation of the granularity (text description)
     */
    @Override
    public String toString() {
         switch (gType) {
            case WHOLE_NOTE: return "Whole Note";
            case QUARTER_NOTE: return "Quarter Note";
            case EIGTH_NOTE: return "Eigth Note";
            case SIXTEENTH_NOTE: return "16th Note";
            case THIRTYSECOND_NOTE: return "32nd Note";
            default: return "Undefined Granularity";
        }
    }
}
