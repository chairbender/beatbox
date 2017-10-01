/* SUMMARY OF OSC MESSAGES LISTENED FOR

/startMetroAndMark, i(bpm) i(countin) - start the metronome at tempo i, mark time message was received + count in as zero for peakTime offset calculations
/stopMetro - stop the metronome

/addLoop, i - listens for a /loopNotes message with i serializes HitNotes
/loopNotes, i,f,i,f,... - plays sounds of class i at time f (expects this in milliseconds) from when the message
                           was received
/stopPlayback - stop looping and playing a noteList

/addSound, s(absolutePath) i(classValue) - adds a sound to the list stored at arg0 with classValue arg1

/setGain, i(gainValue*100) i(loopId) - set the gain of the loop

/playSound, i(classValue) - play the sound with that classValue immediately

/setThreshold, f(threshold) - set the threshold for peak detection

/trainingMode, i(on) - (1 means on, 0 means off) wait longer after a peak is detected to avoid detecting multiple peaks per vocalization

/saveRecordedExamples, s(file path) saves the recorded examples the file.
                        expects .wav file. sends /doneSaving to bboxSend when done.

/loadRecordedExamples, s(file path) load recorded examples from the file to the recorded examples array.
                            expects .wav file that was previously created by saveRecordedExamples. sends /doneLoading when done
                            
/recordQuantization, f(fraction of quarter note it's quantized to (i.e. if this was .5, would quantize to eight notes))
                        0 means no quantization
                        
/setSoundGain, i (gain * 100) i (soundId) gain * 100 instead of gain because osc only works with ints in this environment

*/

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
//
//whenever an amplitude > threshold is detected from input, computed over
//numDataPoints 128-sample ffts that overlap by 64-samples
//
//outputs chuckId for every example at the end of th
//
//When it recieves a /playExample and an int, it plays the
//example with that chuckId once
//

Gain masterGain => dac;

"127.0.0.1" => string hostname;
OscSend wekinatorSend;
wekinatorSend.setHost( hostname, 6448 );
OscSend bboxSend;
bboxSend.setHost( hostname, 6466 );

OscRecv orec;
6460 => orec.port;
orec.listen();

//Custom objects
adc => FFT f =^ RMS rms => blackhole;
f =^ Centroid centroid => blackhole;
f =^ Flux flux => blackhole;
f =^ RollOff rolloff => blackhole;
UAnaBlob b;

//metronome
Impulse pulse => BiQuad filt => masterGain;
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
Sound sounds[20];

/*
Class representing a sound that will be played back
can adjust volume
*/
class Sound {
/*---PRIVATE STUFF---*/
    SndBuf p_snd;
    Envelope p_env;
    1::ms => p_env.duration;
    Gain p_gain;
/*---END PRIVATE STUFF---*/    
    

    UGen m_audioOut; //audio output for this class
    .5 => p_gain.gain;
    1 => p_snd.rate;
    p_snd => p_gain => p_env => m_audioOut => masterGain;
    
    //plays this sound out m_audioOut
    fun void playFromStart() {
        p_env.keyOff(1);
        1::ms => now;
        0 => p_snd.pos;
        p_env.value(0);
        p_env.keyOn(1);
    }
    
    fun void setGain(float gain) {
        gain => p_gain.gain;
    }
    
    //load a new sound from the file specified by absPath
    fun static Sound createSound(string absPath) {
        Sound snd; 
        absPath => snd.p_snd.read;
        return snd;
    }
    
}
Gain gFinal;
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

//INITIALIZATION-----------------------------

//Run the peak detector in parallel to set the peakDetected flag
//spork ~peakDetector();
spork ~examplePlayer();
spork ~startMetroAndMark();
spork ~soundAdderListener();
spork ~setSoundGainListener();
spork ~playSoundListener();
spork ~thresholdListener();
spork ~trainingModeListener();
spork ~adcSender();
spork ~saveExampleListener();
spork ~loadExampleListener();
spork ~masterVolumeListener();

//monitors input and sends adc on port 6451
//also monitors for peaks
OscSend xmit3;
xmit3.setHost(hostname, 6451);
adc => Gain g => OnePole p => blackhole;
adc => g;

Gain gExample => masterGain;
3 => g.op;
0.9999 => p.pole; // to smooth, add more nines
10::samp => dur peakPollRate;

3264::samp => dur exampleLength;

.01 => float threshold;
0 => int inPeak; //variable that tracks whether the audio is still in a single peak
.1::second => dur waitTime;
0 => float lastPeak;
.001 => float peakDropThreshold; //how far does audio need to drop below peak to start finding new peaks
float lastP;
float spacedPLast;
float lowestP;

while (1) {  
    if (false) {
        if (p.last() <= threshold && inPeak) {
            0 => inPeak;
        } else if (p.last() > threshold && !inPeak) {
            now => lastPeakTime;
            //<<< "peak at", (now - chrono) /ms >>>;
            1 => inPeak;
            spork ~analyzeAndSend();
            waitTime => now;
        }
    } else {
        if (p.last() > threshold && !inPeak && spacedPLast > lastP && p.last() > (lowestP + threshold/8)) {
            now => lastPeakTime;
           //<<< "peak at", (now - chrono) /ms >>>;
            1 => inPeak;
            //<<<"in peak">>>;
            p.last() => lowestP;
            spork ~analyzeAndSend();
            //<<<"PEAK">>>;
            //waitTime => now;
        }
        if (inPeak) {
            Math.max(p.last(),lastPeak) => lastPeak;
            //<<<"last peak:",lastPeak>>>;
        }
        if (((lastPeak - p.last()) > peakDropThreshold) || (p.last() < threshold) && inPeak) {
            //<<<"out of peak">>>;
            0 => inPeak;
            0 => lastPeak;
            .01::second => now;
        }
        spacedPLast => lastP;
        Math.min(lowestP,p.last()) => lowestP;
    }
    
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

//sends the monitor level
fun void adcSender() {
    while (true) {
        xmit3.startMsg("/monitorLevel", "f");
        xmit3.addFloat(p.last());
        p.last() => spacedPLast;
        (1.0/27)::second => now;
        //<<<"adc",p.last()>>>;
    }
}

//When peak detected, compute min, max, avg, and std. dev for centroid and rms
//and send, along with peakTime message
int numAnalyzeShreds;
fun void analyzeAndSend() {
    if (recordAudio) {
        exampleIdIncrementer => int id;
        exampleIdIncrementer++;
        <<<"example",exampleIdIncrementer>>>;
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
        
        wekinatorSend.startMsg( "/oscCustomFeaturesWithId", "ifffffffff"); 
        //wekinatorSend.startMsg( "/oscCustomFeatures", "fffffffff"); 
        id => wekinatorSend.addInt;
        
        for (0 => int j; j < 9; j++) {
            result[j] => wekinatorSend.addFloat;
        }
        //.5::second => now;

        0 => examples[exampleArrayIndex].record;
        adc =< examples[exampleArrayIndex]; //remove the lisa

        //route it to masterGain
        //examples[exampleArrayIndex] => masterGain;

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
        
        wekinatorSend.startMsg( "/oscCustomFeaturesWithId", "ifffffffff"); 
        //wekinatorSend.startMsg( "/oscCustomFeatures", "fffffffff"); 
        0 => wekinatorSend.addInt;
        
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

//Plays back an example when it receives a /playExample
//message and an int specifying the id to play
fun void examplePlayer() {
    orec.event("/playExample,i") @=> OscEvent playEvent;
    
    while (true) {
        playEvent => now;
        while (playEvent.nextMsg() != 0) {
            playEvent.getInt() => int id;
            <<<"playExample",id>>>;
            spork ~playit(id);
        }
    }
}

//called by examplePlayer to play a single example
//plays the example with the specified id
fun void playit(int id) {
    gExample.gain(2);
    examples[id] =< gExample;
    examples[id] => gExample;
    examples[id].voiceGain(0,.2);
    examples[id].playPos(0::samp); // set to beginning
    examples[id].rampUp(50::ms);
    exampleLength - 50::samp => now;
    examples[id].rampDown(50::samp);
    50::samp => now;
    examples[id] =< gExample;
}

fun void sendOscFeatureNames() {    
    while (true) {
        bboxSend.startMsg( "/oscCustomFeaturesNames", "sssssssss");
        for (0 => int i; i < 1; 1 +=> i) {
            bboxSend.addString(5 + "centroidAvg");
            bboxSend.addString(5 + "centroidStdDev");
            bboxSend.addString(5 + "centroidMin");
            bboxSend.addString(5 + "centroidMax");
            bboxSend.addString(5 + "rmsAvg");
            bboxSend.addString(5 + "rmsStdDev");
            bboxSend.addString(5 + "rmsMin");
            bboxSend.addString(5 + "rmsMax");
            bboxSend.addString(5 + "highestFFT");       
        }
        .1::second => now;
    }
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

//Looks for /startMetroAndMark, ii message
//starts playing a metronome at the tempo of
//the first integer with a count in of the second integer, and returns all /peakTime
//messages relative to the end of the count in
fun void startMetroAndMark() {
    orec.event("/startMetroAndMark,ii") @=> OscEvent playEvent;
    while (true) {
        playEvent => now;
        while (playEvent.nextMsg() != 0) {
            if (!playMetronome) {
                playEvent.getInt() => int bpm;
                playEvent.getInt() => int countin;
                now + ((1.0/bpm)*countin)::minute => chrono;
                spork ~metronome(bpm);
                spork ~stopMetronome(); //only listen when necessary
            }
        }
    }
}

//play a metronome
int globalBpm;
time metroStart;
fun void metronome(int bpm) {
    bpm => globalBpm;
    660 => filt.pfreq;
    .5 => pulse.next;
    1 => playMetronome;
    60.0 / (bpm) => float spb;
    now => metroStart;
    while (playMetronome) {
        //<<<"metronome at", (now - chrono) / ms>>>;
        spb::second=>now;
        if (!playMetronome) {
            me.exit();
        }
        .5 => pulse.next;
    }
}



//stop the metronome
fun void stopMetronome() {
    orec.event("/stopMetro") @=> OscEvent playEvent;
    while (true) {
        playEvent => now;
        while (playEvent.nextMsg() != 0) {
            0 => playMetronome;
            return;
        }   
    }
}

//listens for /addSound messages to add
//drum sounds to the list
fun void soundAdderListener() {
    orec.event("/addSound,si") @=> OscEvent addEvent;
    while (true) {
        addEvent => now;
        while (addEvent.nextMsg() != 0) {
            addEvent.getString() => string absolutePath;
            addEvent.getInt() => int classValue;
            <<<"Adding sound", classValue>>>;
            Sound.createSound(absolutePath) @=> sounds[classValue];
        }
    }
}

//listens for /setSoundGain, i i
fun void setSoundGainListener() {
    orec.event("/setSoundGain, f i") @=> OscEvent setEvent;
    while (true) {
        setEvent => now;
        while (setEvent.nextMsg() != 0) {
            setEvent.getFloat()=> float gainValue;
            setEvent.getInt() => int sndId;
            //<<<"Sound gain set">>>;
            sounds[sndId].setGain(gainValue);
        }
    }
}

//Listens for /playSound
fun void playSoundListener() {
    orec.event("/playSound,ii") @=> OscEvent playEvent;
    while (true) {
        playEvent => now;
        while (playEvent.nextMsg() != 0) {
            spork ~playSound(playEvent.getInt(),.5::second,masterGain,playEvent.getInt()/1000.0);
        }
    }
}
//plays the sound out the specified ugen,
//handles multiple instances of sound being played simultaniously
fun void playSound(int classValue, dur waitTime, UGen out, float volume) {
    gFinal =< out;
    gFinal => out;
    gFinal.gain(volume);
    sounds[classValue].m_audioOut =< gFinal;
    sounds[classValue].m_audioOut => gFinal;
    sounds[classValue].playFromStart();
    waitTime => now;
    sounds[classValue].m_audioOut =< gFinal;
}

//listens for /setThreshold
fun void thresholdListener() {
    orec.event("/setThreshold","f") @=> OscEvent setEvent;
    while (true) {
        setEvent => now;
        while (setEvent.nextMsg() != 0) {
            setEvent.getFloat() => threshold;
        }
    }
}

//listens for /trainingMode,i
fun void trainingModeListener() {
    orec.event("/trainingMode,i") @=> OscEvent modeEvent;
    while (true) {
        modeEvent => now;
        while (modeEvent.nextMsg() != 0) {
            if (modeEvent.getInt()) {
                .5::second => waitTime;
                1 => recordAudio;
            } else {
                .1::second => waitTime;
                0 => recordAudio;
            }
        }
    }
}

//set te master volume
fun void masterVolumeListener() {
    orec.event("/setMasterVolume","i") @=> OscEvent volEvent;
    while (true) {
        volEvent => now;
        while (volEvent.nextMsg() != 0) {
            volEvent.getInt()/1000.0 => masterGain.gain;
        }
    }
}

//listens for /saveRecordedExamples,
//then writes them (the whole occupied lisa array) to the specified file
//sends /doneSaving when done
fun void saveExampleListener() {
    orec.event("/saveRecordedExamples", "i s") @=> OscEvent saveEvent;
    while (true) {
        saveEvent => now;
        <<<"ck: got saveRecordedExamples">>>;
        while (saveEvent.nextMsg() != 0) {
            saveEvent.getInt() => int numExamples;
            saveEvent.getString() => string outputFile;
            
            "" => string result;
            //construct string for notes
            for (0 => int i; i < numExamples; i++) {
                result + "i " => result;
            }
            
            orec.event("/exampleList",result) @=> OscEvent exEvent;
            bboxSend.startMsg("/readyToSave","");
            <<<"ck: sent readyToSave", numExamples>>>;
            exEvent => now;
            <<<"ck: got exampleList">>>;
            
            WvOut wv;
            wv.wavFilename(outputFile);
            wv => blackhole;
            //plug each lisa instance into wvout and
            //play it
            int i;
            while (exEvent.nextMsg() != 0) {
                for (0 => int j; j < numExamples; j++) {
                    exEvent.getInt() => i;
                    examples[i] =< masterGain;
                    examples[i] => wv;
                    examples[i].playPos(0::samp); // set to beginning
                    examples[i].rampUp(1::ms);
                    examples[i].voiceGain(0,1);
                    exampleLength => now;
                    examples[i].rampDown(1::ms);
                    examples[i].voiceGain(0,.2);
                    examples[i] =< wv;
                }
            }

            wv.closeFile(outputFile);
            wv =< blackhole;
            <<<"ck: done saving">>>;
            bboxSend.startMsg("/doneSaving");
            bboxSend.startMsg("/doneSaving");
            bboxSend.startMsg("/doneSaving");
            bboxSend.startMsg("/doneSaving");
            bboxSend.startMsg("/doneSaving");
            bboxSend.startMsg("/doneSaving");
            bboxSend.startMsg("/doneSaving");
            bboxSend.startMsg("/doneSaving");
            bboxSend.startMsg("/doneSaving");
        }
    }
}

//listens for /loadRecordedExamples,
//then loads them from the file to the LiSa examples array
//sends /doneLoading when done
fun void loadExampleListener() {
    orec.event("/loadRecordedExamples,s") @=> OscEvent loadEvent;
    while (true) {
        loadEvent => now;
        //<<<"LOADING">>>;
        while (loadEvent.nextMsg() != 0) {
            loadEvent.getString() => string inputFile;

            SndBuf bf;
            bf.read(inputFile);
            //plug bf into each lisa in the example array
            //and play it
            //<<<"bf.length",bf.length()>>>;
            int i;
            1 => bf.rate;
            0 => bf.pos;
            Math.round(bf.length()/3264::samp) $ int => exampleIdIncrementer;
            for (0 => i; i < (bf.length()/3264::samp); i++) {
                examples[i] =< masterGain;
                bf => examples[i] => blackhole;
                exampleLength => examples[i].duration;
                examples[i].voiceGain(0,1);
                examples[i].record(1);
                exampleLength => now;
                0 => examples[i].record;
                examples[i].voiceGain(0,.2);
                bf =< examples[i];
                examples[i] =< blackhole;
                <<<"read example",i>>>;
            }           
            bboxSend.startMsg("/doneLoading");
        }
    }
}

