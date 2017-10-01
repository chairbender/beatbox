/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

/**Class to carry timing data for hits
 *
 * @author Kyle
 */
class HitTiming {
private int id;
    private double time;

    /**
     *
     * @param id
     * @param time in milliseconds
     */
    public HitTiming(int id, double time) {
        this.id = id;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    //get the time offset this hit ocurred, in ms
    public double getOffset() {
        return time;
    }

    //set the offset of this HitTiming from the beginning of the loop.
    //time is in ms
    public void setOffset(double time) {
        this.time = time;
    }

    @Override
    //equal if their ids are equal
    public boolean equals(Object o) {
        return (o instanceof HitTiming) &&
                ((HitTiming)o).id == this.id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.id;
        hash = 37 * hash + Float.floatToIntBits((float)this.time);
        return hash;
    }

    @Override
    public String toString() {
        return "id: " + id + ", time: " + time;
    }
    
}
