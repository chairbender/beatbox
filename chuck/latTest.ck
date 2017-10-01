time chrono;

Impulse i => dac;

adc => Gain g => OnePole p => blackhole;
adc => g;
3 => g.op;
.9999 => p.pole;

now => time start;

10::samp => dur peakPollRate;

//ADJUST THIS
.1 => float threshold;

spork ~makeImpulses();
while (1) {  
    if (adc.last() > threshold) {
       <<< "peak at", (now - chrono) /ms >>>;
        .2::second => now;
    }
    
    peakPollRate => now;
}

fun void makeImpulses() {
	while (true) {
		1 => i.next;
		.8::second => now;
		<<< "Impulse at ", (now - start) / ms >>>;
	}
}