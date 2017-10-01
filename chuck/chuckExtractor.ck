Test Chuck feature extractor (extracting 5 features) is below:
/* Test beat box feature extractor */

public class CustomFeatureExtractor {
	0 => int isExtracting;
	1 => int isOK;

	//Features: RMS, centroid, flux, rolloff
	//Sent for window following peak > threshold

	5 => int numFeats; //change this to your # of features
	new float[numFeats] @=> float features[]; //store computed features in this array
	50::ms => dur defaultRate => dur rate; //optionally change
	0.0 => float thisAmp;
	.001 => float threshold; //Amplitude threshold (absolute) for triggering

	//Custom objects
	adc => FFT f =^ RMS rms => blackhole;
	f =^ Centroid centroid => blackhole;
	f =^ Flux flux => blackhole;
	f =^ RollOff rolloff => blackhole;
	UAnaBlob b;

	//Set up bin stuff
	2048 => int FFT_SIZE;
	FFT_SIZE => f.size;
	Windowing.hamming(1024) => f.window;

	1::second / 1::samp => float SR;
	SR/FFT_SIZE => float bin_width;


	//TODO: Try to set up; set 0=>isOK if any problems happen
	fun void setup() {
		0 => isExtracting;
		1 => isOK;
		new float[numFeats] @=> float features[];
		defaultRate => rate;
	}

	//Setup and specify # features Java wants
	fun void setup(int n) {
		setup();
		if (n != numFeats) {
			0 => isOK;
			<<< "Error: we don't agree on the number of features!">>>;
		}
	}

	//TODO: Fill in function for computing features
	fun void computeFeatures() {
		
		if (thisAmp > threshold || thisAmp < (-1 * threshold)) {
			<<< "Over threshold!">>>;
			thisAmp => features[0];
			rms.upchuck();
			rms.fval(0) => features[1];
			centroid.upchuck();
			centroid.fval(0) => features[2];
			flux.upchuck();
			flux.fval(0) => features[3];
			rolloff.upchuck();
			rolloff.fval(0) => features[4];
		} else {
			for (0 => int i; i < numFeats; i++) {
				0.0 => features[i];
			}
		}
		Math.fabs(adc.last()) => thisAmp;
	}
	
/*** Shouldn't have to edit anything beyond this point **/
	fun float[] getFeatures() {
		return features;
	}

	fun int numFeatures() {
		return numFeats;
	}

	fun void extract() {
		if (! isExtracting) {
		  1 => isExtracting;
			while (isExtracting) {
				computeFeatures();			
				rate => now;
		 }
	   }
	}			

	fun void stop() {
		0 => isExtracting;
	}
	fun string[] getFeatureNamesArray() {
		string s[numFeats];
		for (0 => int i; i < numFeats; i++) {
			"Bin_" + i => s[i];
		}
		return s;
	}

} //end class