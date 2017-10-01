public class MyDemoExtractor {
	0 => int isExtracting;
	1 => int isOK;
	1 => int numFeats;
	0 => int id;

	new float[numFeats] @=> float features[]; //store computed features in this array
	
	100::ms => dur defaultRate => dur rate;

	OscSend xmit;
	"localhost" => string hostname;
	6448 => int port;
	xmit.setHost( hostname, port );

	//create any custom objects here:
	adc => FFT fft => blackhole;
	fft =^ Centroid c;
	UAnaBlob b;

	fun void computeFeatures() {
		c.upchuck() @=> b;
		b.fval(0) => features[0];
	}

	fun void setup() {
		0 => isExtracting;
		1 => isOK;
		new float[numFeats] @=> float features[];
		defaultRate => rate;
	}

	fun string[] getFeatureNamesArray() {
		string s[1];
		"Centroid" => s[0];
		return s;
	}
	
	//Calls setup, also checks that we agree on # featuress
	fun void setup(int n) {
		setup();
		if (n != numFeats) {
			0 => isOK;
			<<< "Error: we don't agree on the number of features!">>>;
		}
	}

	//Return the features
	fun float[] getFeatures() {
		<<< "Getting features">>>;
		return features;
	}

	fun int numFeatures() {
		return numFeats;
	}

	//Extraction loop, given user-specified functions above
	fun void extract() {
		if (! isExtracting) {
		  1 => isExtracting;
			while (isExtracting) {
				computeFeatures();
				sendFeatures();			
				rate => now;
		 }
	   }
	}

	fun void sendFeatures() {
		xmit.startMsg("/oscCustomFeaturesWithId, if");
		xmit.addInt(id);
		id++;
		xmit.addFloat(features[0]);
		//<<< "Sent features">>>;
	}			

	//Stop extracting
	fun void stop() {
		0 => isExtracting;
	}

	fun void sendNames() {
		xmit.startMsg("/oscCustomFeaturesNames, s");
		xmit.addString("Centroid");
	}
}


MyDemoExtractor m;

m.setup();
m.sendNames();

spork ~m.extract();


while (true) {
	1::hour => now;
}