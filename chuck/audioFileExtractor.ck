
"127.0.0.1" => string hostname;
OscSend wekinatorSend;
wekinatorSend.setHost( hostname, 6448 );
OscSend bboxSend;
bboxSend.setHost( hostname, 6466 );

OscRecv orec;
6460 => orec.port;
orec.listen();

//Custom objects
SndBuf sndb => FFT f =^ RMS rms => blackhole;
f =^ Centroid centroid => blackhole;
f =^ Flux flux => blackhole;
f =^ RollOff rolloff => blackhole;
UAnaBlob b;

//metronome
Impulse pulse => BiQuad filt => dac;
330 => filt.pfreq;
0.99 => filt.prad;
0 => int playMetronome;

20 => int MAX_LOOP_COUNT;

//time offset
time chrono;


//The audio examples
LiSa examples[500];
0 => int examplesRecorded; //size of the examples array
0 => int exampleIdIncrementer; //used for assigning example ids
0 => int recordAudio; //whether chuck should record detected peaks to lisa instances

//The sounds (i.e. drum sounds), indexed by their
//classValue


//Set up bin stuff
128 => int FFT_SIZE;
FFT_SIZE => f.size;
Windowing.hamming(64) => f.window;

1::second / 1::samp => float SR;
SR/FFT_SIZE => float bin_width;


0 => int peakDetected; //flag if peak has been detected
now => time lastPeakTime;
100::samp => dur peakWindow;
80 => int numDataPoints; //number of windows to use for calculating statistics
1 => float rmsMultiplier; //so it isn't out of wek's range (this actually doesn't matter, leave it at 1)

0 => int currentlyAnalyzing; //flag if currently analyzing i.e. don't detect peak
0 => int num;

//whether the program is playing loops
0 => int looping;

Event addLoopWait;
0 => int addingLoop;

Event stoppingWait;
0 => int stopping;

//INITIALIZATION-----------------------------

//Run the peak detector in parallel to set the peakDetected flag
//spork ~peakDetector();
spork ~playListener();

//monitors input and sends adc on port 6451
//also monitors for peaks
OscSend xmit3;
xmit3.setHost(hostname, 6451);
sndb => Gain g => OnePole p => blackhole;
sndb => g;
sndb => dac;

0 => sndb.rate;
sndb.read("C:\\testsounds\\j\\Sound05.wma.wav");

3 => g.op;
0.9999 => p.pole; // to smooth, add more nines
10::samp => dur peakPollRate;

3264::samp => dur exampleLength;

.01 => float threshold;
0 => int inPeak; //variable that tracks whether the audio is still in a single peak
.7::second => dur waitTime;
0 => float lastPeak;
.001 => float peakDropThreshold; //how far does audio need to drop below peak to start finding new peaks
0.0 => float lastP;
1.0 => float spacedPLast;
float lowestP;
while (1) {  
    if (p.last() > threshold && !inPeak && p.last() > (lowestP + threshold/8)) {
        now => lastPeakTime;
       <<< "peak at", (now - chrono) /ms >>>;
        1 => inPeak;
        p.last() => lowestP;
        spork ~analyzeAndSend();
        waitTime => now;
    }
    if (inPeak) {
        Math.max(p.last(),lastPeak) => lastPeak;
        //<<<"last peak:",lastPeak>>>;
    }
    if (((lastPeak - p.last()) > peakDropThreshold) || (p.last() < threshold) && inPeak) {
        //<<<"out of peak">>>;
        0 => inPeak;
        0 => lastPeak;
    }
    spacedPLast => lastP;
    Math.min(lowestP,p.last()) => lowestP;
    
    peakPollRate => now;
}
/*while (true) {
if (p.last() > threshold) {
    <<< "peak at", (now - chrono) /ms >>>;
    .1::second => now;
}
peakPollRate => now;
}*/

//listens for /recordQuantization to
//help it avoid redundant calls to analyzePeak during loop recording
/*
fun void recordQuantizationListener() {
    orec.event("/recordQuantization,f") @=> OscEvent setEvent;
    
    while (true) {
        setEvent => now;
        while (setEvent.nextMsg() != 0) {
            setEvent.getInt() => quantizationFactor;
        }
    }
}*/



//When peak detected, compute min, max, avg, and std. dev for centroid and rms
//and send, along with peakTime message
int numAnalyzeShreds;
fun void analyzeAndSend() {
    if (recordAudio) {
        exampleIdIncrementer => int id;
        exampleIdIncrementer++;
        id => int exampleArrayIndex;
        examplesRecorded++;
        if (numAnalyzeShreds == 0) {
            1 => currentlyAnalyzing;
        }
        numAnalyzeShreds++;

        now => time peakTime;
        adc => examples[exampleArrayIndex] => blackhole; //stick a lisa in there
        1::second => examples[exampleArrayIndex].duration;
        examples[exampleArrayIndex].voiceGain(0, .2);
        examples[exampleArrayIndex].record(1);
        analyzeNumDataPoints(numDataPoints) @=> float result[];
        
        //wekinatorSend.startMsg( "/oscCustomFeatures", "ifffffffff"); 
        wekinatorSend.startMsg( "/oscCustomFeatures", "fffffffff"); 
        //id => wekinatorSend.addInt;
        
        for (0 => int j; j < 9; j++) {
            result[j] => wekinatorSend.addFloat;
        }
        //.5::second => now;

        0 => examples[exampleArrayIndex].record;
        adc =< examples[exampleArrayIndex]; //remove the lisa

        //route it to dac
        examples[exampleArrayIndex] => dac;

        bboxSend.startMsg("/peakTime","if");
        id => bboxSend.addInt;

        ((peakTime - chrono) / ms) => bboxSend.addFloat;
        numAnalyzeShreds--;
        if (numAnalyzeShreds == 0) {
            0 => currentlyAnalyzing;
        }
    } else {
        if (numAnalyzeShreds == 0) {
            1 => currentlyAnalyzing;
        }
        numAnalyzeShreds++;

        now => time peakTime;
        analyzeNumDataPoints(numDataPoints) @=> float result[];
        
        //wekinatorSend.startMsg( "/oscCustomFeaturesWithId", "ifffffffff"); 
        wekinatorSend.startMsg( "/oscCustomFeatures", "fffffffff"); 
        //0 => wekinatorSend.addInt;
        
        for (0 => int j; j < 9; j++) {
            result[j] => wekinatorSend.addFloat;
        }
        //.5::second => now;

        bboxSend.startMsg("/peakTime","if");
        0 => bboxSend.addInt;
        ((peakTime - chrono) / ms) => bboxSend.addFloat;
        numAnalyzeShreds--;
        if (numAnalyzeShreds == 0) {
            0 => currentlyAnalyzing;
        }
    }
}

//Analyze data using the specified number of data points, return the float array corresponding to
//the osc message format described at top
fun float[] analyzeNumDataPoints(int numDataPoints) {
    float centroidTotal;
    float centroidMin;
    float centroidMax;
    float centroidStdDev;
    float rmsTotal;
    float rmsMin;
    float rmsMax;
    float rmsStdDev;
    float highestBin;
    float highestBinMagnitude;
    new float[numDataPoints] @=> float centroidData[];
    new float[numDataPoints] @=> float rmsData[];

    

    //get 1st centroid and rms
    rms.upchuck();
    centroid.upchuck();
    f.upchuck();
    
    rms.fval(0) +=> rmsTotal;
    rms.fval(0) => rmsMin => rmsMax => rmsData[0];
    centroid.fval(0) +=> centroidTotal;
    centroid.fval(0) => centroidMin => centroidMax => centroidData[0];
    //find highest bin
    //TODO: uncomment if this is really necessary
    /*for (1 => int j; j < FFT_SIZE; j++) {
        if (f.fval(j) > highestBinMagnitude) {
            f.fval(j) => highestBinMagnitude;
            j => highestBin;
        }
    }*/
        
    //iterate
    for (1 => int i; i < numDataPoints; i++) {
        64::samp => now;
        rms.upchuck();
        centroid.upchuck();
        f.upchuck();
        rms.fval(0) +=> rmsTotal;
        Math.max( rms.fval(0), rmsMax ) => rmsMax;
        Math.min( rms.fval(0), rmsMin ) => rmsMin;
        rms.fval(0) => rmsData[i];
        centroid.fval(0) +=> centroidTotal;
        Math.max( centroid.fval(0), centroidMax ) => centroidMax;
        Math.min( centroid.fval(0), centroidMin ) => centroidMin;
        centroid.fval(0) => centroidData[i];                    
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
    
    float result[9];
    
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

//waits to play sndbuf
fun void playListener() {
    orec.event("/play") @=> OscEvent e;
    while (true) {
        <<<"playing">>>;
        e => now;
        while (e.nextMsg() != 0) {
            0 => sndb.pos;
            1 => sndb.rate;
            1 => sndb.gain;
        }
    }
}