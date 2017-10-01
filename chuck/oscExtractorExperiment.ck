//THIS IS HOPEFULLY FIXED

//Extracts the following for different numDataPoints
//0 - centroidAvg
//1 - centroidStdDev
//2 - centroidMin
//3 - centroidMax
//4 - rmsAvg
//5 - rmsStdDev
//6 - rmsMin
//7 - rmsMax
//8 - fft bin with highest fval (highest over all windows)
//...
//whenever an amplitude > threshold is detected from input, computed over
//numDataPoints 128-sample ffts that overlap by 64-samples
//
9 => int NUM_FEATURES; //constant for how many features are extracted

"127.0.0.1" => string hostname;
OscSend xmit;
xmit.setHost( hostname, 6448 );
OscSend xmit2;
xmit2.setHost( hostname, 6448 );

//Custom objects
adc => FFT f =^ RMS rms => blackhole;
f =^ Centroid centroid => blackhole;
f =^ Flux flux => blackhole;
f =^ RollOff rolloff => blackhole;
UAnaBlob b;



//Set up bin stuff
128 => int FFT_SIZE;
FFT_SIZE => f.size;
Windowing.hamming(64) => f.window;

1::second / 1::samp => float SR;
SR/FFT_SIZE => float bin_width;

//constants
.25 => float threshold;
0 => int peakDetected; //flag if peak has been detected
now => time lastPeakTime;
100::samp => dur peakWindow;
10::samp => dur peakPollRate;
[10,20,30,40,50,60,70,80,90,100,110] @=> int numDataPoints[]; //list of numDataPoints values to try

//for storing the results from each window
float rmsArr[numDataPoints[numDataPoints.size()-1]];
float cent[numDataPoints[numDataPoints.size()-1]];
float highestFFTBin[numDataPoints[numDataPoints.size()-1]]; //bin with highest fft fval
float highestFFTVal[numDataPoints[numDataPoints.size()-1]]; //the actual value of the bin

1 => float rmsMultiplier; //so it isn't out of wek's range (this actually doesn't matter, leave it at 1)

0 => int currentlyAnalyzing; //flag if currently analyzing i.e. don't detect peak

//Run the peak detector in parallel to set the peakDetected flag
spork ~peakDetector();
spork ~sendOscFeatureNames();

//Extract features and send via osc when peak detected.
while (true) {
    if (peakDetected) {
        <<<"Peak detected! Analyzing">>>;
        analyzeAndSend();
    }
    .1::second => now;
}

//When peak detected, compute min, max, avg, and std. dev for centroid and rms
//and send
fun void analyzeAndSend() {  
    1 => currentlyAnalyzing;
    xmit.startMsg( "/oscCustomFeatures", "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
    //Get all float and rms values for the maximum numDataPoints
    getRmsCentroidAndFFT();   
    
    for (0 => int i; i < numDataPoints.size(); i++) {
        analyzeNumDataPoints(numDataPoints[i]) @=> float result[];
        for (0 => int j; j < NUM_FEATURES; j++) {
            result[j] => xmit.addFloat;
        }       
    }
    <<<"done">>>;
    0 => currentlyAnalyzing;
}

//populates rms, cent, and fft arrays
fun void getRmsCentroidAndFFT() {
    numDataPoints[numDataPoints.size()-1] => int maxDataPoints;
   
    for (0 => int i; i < maxDataPoints; 1 +=> i) {
        f.upchuck();
        centroid.upchuck();
        rms.upchuck(); 
        rms.fval(0) => rmsArr[i];

        centroid.fval(0) => cent[i];
        
        0 => float highestBinMagnitude;
        0 => int highestBin;

         //find highest bin
        for (1 => int j; j < FFT_SIZE; j++) {
            if (f.fval(j) > highestBinMagnitude) {
                f.fval(j) => highestBinMagnitude;
                j => highestBin;
            }
        }
        highestBin => highestFFTBin[i];
        highestBinMagnitude => highestFFTVal[i];
        
        64::samp => now;
    }
}

//Analyze data using the specified number of data points, return the float array corresponding to
//the osc message format described at top
//requires that the arrays have been populated
fun float[] analyzeNumDataPoints(int numDataPoints) {
    float centroidTotal;
    float centroidMin;
    float centroidMax;
    float centroidStdDev;
    float rmsTotal;
    float rmsMin;
    float rmsMax;
    float rmsStdDev;
    0 => float highestBin;
    0 => float highestBinMagnitude;
    new float[numDataPoints] @=> float centroidData[];
    new float[numDataPoints] @=> float rmsData[];
    
    //get 1st centroid and rms

    rmsArr[0] +=> rmsTotal;
    rmsArr[0] => rmsMin => rmsMax => rmsData[0];
    cent[0] +=> centroidTotal;
    cent[0] => centroidMin => centroidMax => centroidData[0];
    //do rest
    for (1 => int i; i < numDataPoints; i++) {
        rmsArr[i] +=> rmsTotal;
        Math.max( rmsArr[i], rmsMax ) => rmsMax;
        Math.min( rmsArr[i], rmsMin ) => rmsMin;
        rmsArr[i] => rmsData[i];
        cent[i] +=> centroidTotal;
        Math.max( cent[i], centroidMax ) => centroidMax;
        Math.min( cent[i], centroidMin ) => centroidMin;
        cent[i] => centroidData[i];
        
        //compute current highestBin
        if (highestFFTVal[i] > highestBinMagnitude) {
            highestFFTVal[i] => highestBinMagnitude;
            highestFFTBin[i] => highestBin;
        }
    }
    //calculate
    rmsTotal / numDataPoints => float rmsAvg;
    centroidTotal / numDataPoints => float centroidAvg;
    //get std dev
    float rmsDevSquaredTotal;
    float centroidDevSquaredTotal;
    for (0 => int i; i < numDataPoints; i++) {
        (rmsData[i] - rmsAvg)*(rmsData[i] - rmsAvg) +=> rmsDevSquaredTotal; 
        (centroidData[i] - centroidAvg)*(centroidData[i] - centroidAvg) +=> centroidDevSquaredTotal;
    }
    Math.sqrt(rmsDevSquaredTotal / numDataPoints) => rmsStdDev;
    Math.sqrt(centroidDevSquaredTotal / numDataPoints) => centroidStdDev;
    
    float result[NUM_FEATURES];
    
	centroidAvg => result[0]; 
    centroidStdDev => result[1];
    centroidMin => result[2];
    centroidMax => result[3];
    rmsAvg * rmsMultiplier => result[4];
    rmsStdDev * rmsMultiplier => result[5];
    rmsMin * rmsMultiplier => result[6];
    rmsMax * rmsMultiplier => result[7];
    highestBin => result[8];
    
    return result;
}

//Pretty inefficient but does the trick
//Looks for a peak in the last peakWindow 
fun void peakDetector() {
	while (true){
		if (adc.last() > threshold || adc.last() < -1 * threshold) {
			1 => peakDetected; // analyze
			now => lastPeakTime;	
		
		} else if (now > lastPeakTime + peakWindow) {
			0 => peakDetected;
		}

        peakPollRate => now; //This is silly; would be better do do a low-pass filter envelope and look for peaks there. But don't worry about it right now.

	}

}

//run in parallel to send osc feature names
fun void sendOscFeatureNames() {    
    while (true) {
        xmit2.startMsg( "/oscCustomFeaturesNames sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");
        xmit2.addString("10cAvg");
        xmit2.addString("10cSD");
        xmit2.addString("10cMin");
        xmit2.addString("10cMax");
        xmit2.addString("10rAvg");
        xmit2.addString("10rSD");
        xmit2.addString("10rMin");
        xmit2.addString("10rMax");
        xmit2.addString("10highestFFT");
        xmit2.addString("20cAvg");
        xmit2.addString("20cSD");
        xmit2.addString("20cMin");
        xmit2.addString("20cMax");
        xmit2.addString("20rAvg");
        xmit2.addString("20rSD");
        xmit2.addString("20rMin");
        xmit2.addString("20rMax");
        xmit2.addString("20highestFFT");
        xmit2.addString("30cAvg");
        xmit2.addString("30cSD");
        xmit2.addString("30cMin");
        xmit2.addString("30cMax");
        xmit2.addString("30rAvg");
        xmit2.addString("30rSD");
        xmit2.addString("30rMin");
        xmit2.addString("30rMax");
        xmit2.addString("30highestFFT"); 
        xmit2.addString("40cAvg");
        xmit2.addString("40cSD");
        xmit2.addString("40cMin");
        xmit2.addString("40cMax");
        xmit2.addString("40rAvg");
        xmit2.addString("40rSD");
        xmit2.addString("40rMin");
        xmit2.addString("40rMax");
        xmit2.addString("40highestFFT"); 
        xmit2.addString("50cAvg");
        xmit2.addString("50cSD");
        xmit2.addString("50cMin");
        xmit2.addString("50cMax");
        xmit2.addString("50rAvg");
        xmit2.addString("50rSD");
        xmit2.addString("50rMin");
        xmit2.addString("50rMax");
        xmit2.addString("50highestFFT"); 
        xmit2.addString("60cAvg");
        xmit2.addString("60cSD");
        xmit2.addString("60cMin");
        xmit2.addString("60cMax");
        xmit2.addString("60rAvg");
        xmit2.addString("60rSD");
        xmit2.addString("60rMin");
        xmit2.addString("60rMax");
        xmit2.addString("60highestFFT"); 
        xmit2.addString("70cAvg");
        xmit2.addString("70cSD");
        xmit2.addString("70cMin");
        xmit2.addString("70cMax");
        xmit2.addString("70rAvg");
        xmit2.addString("70rSD");
        xmit2.addString("70rMin");
        xmit2.addString("70rMax");
        xmit2.addString("70highestFFT"); 
        xmit2.addString("80cAvg");
        xmit2.addString("80cSD");
        xmit2.addString("80cMin");
        xmit2.addString("80cMax");
        xmit2.addString("80rAvg");
        xmit2.addString("80rSD");
        xmit2.addString("80rMin");
        xmit2.addString("80rMax");
        xmit2.addString("80highestFFT"); 
        xmit2.addString("90cAvg");
        xmit2.addString("90cSD");
        xmit2.addString("90cMin");
        xmit2.addString("90cMax");
        xmit2.addString("90rAvg");
        xmit2.addString("90rSD");
        xmit2.addString("90rMin");
        xmit2.addString("90rMax");
        xmit2.addString("90highestFFT"); 
        xmit2.addString("100cAvg");
        xmit2.addString("100cSD");
        xmit2.addString("100cMin");
        xmit2.addString("100cMax");
        xmit2.addString("100rAvg");
        xmit2.addString("100rSD");
        xmit2.addString("100rMin");
        xmit2.addString("100rMax");
        xmit2.addString("100highestFFT");
        xmit2.addString("110cAvg");
        xmit2.addString("110cSD");
        xmit2.addString("110cMin");
        xmit2.addString("110cMax");
        xmit2.addString("110rAvg");
        xmit2.addString("110rSD");
        xmit2.addString("110rMin");
        xmit2.addString("110rMax");
        xmit2.addString("110highestFFT");  
        
        
        .1::second => now;
    }
}