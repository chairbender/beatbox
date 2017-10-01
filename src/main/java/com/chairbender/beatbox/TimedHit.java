/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chairbender.beatbox;

/**
 *Contains a hit together with timing information
 * @author Kyle
 */
class TimedHit {
    //the hit pertaining to this TimedHit
    private Hit hit;
    public Hit getHit() {return hit;}
    public void setHit(Hit hit) {this.hit = hit;}
    
    //the time it occurred from the beginning of the loop
    private HitTiming timing;
    public HitTiming getTiming() {return timing;}
    public void setTiming(HitTiming timing) {this.timing = timing;}
    
    public TimedHit(Hit hit, HitTiming timing) {
        this.hit = hit;
        this.timing = timing;
    }
}
