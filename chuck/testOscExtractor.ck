"localhost" => string hostname;
OscSend xmit;
xmit.setHost( hostname, 6448 );


adc => blackhole;
.001 => float threshold;
0 => int peakDetected; //flag if peak has been detected
now => time lastPeakTime;
100::samp => dur peakWindow;
10::samp => dur peakPollRate;

//Run the peak detector in parallel to set the peakDetected flag
spork ~peakDetector();

//Extract features and send via osc when peak detected.
while (true) {
if (peakDetected) {
	xmit.startMsg( "/oscCustomFeatures", "f");
	Std.rand2f(0.0, 1.0) => xmit.addFloat; //dummy feature: just a random number
}
.1::second=> now;

}

//Pretty inefficient but does the trick
//Looks for a peak in the last peakWindow 
fun void peakDetector() {
	while (true){
		if (adc.last() > threshold || adc.last() < -1 * threshold) {
			1 => peakDetected;
			now => lastPeakTime;	
		
		} else if (now > lastPeakTime + peakWindow) {
			0 => peakDetected;
		}
		peakPollRate => now; //This is silly; would be better do do a low-pass filter envelope and look for peaks there. But don't worry about it right now.
	}

}