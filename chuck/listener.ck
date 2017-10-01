//Listens for onsets.  
//Control using onsetDetectorController

adc => blackhole;

//-------------------------------------
// initializing objects for OSC connection
OscRecv recv;
6449 => recv.port;
recv.listen();
// send object 
OscSend xmit; 
// aim the transmitter 
xmit.setHost( "127.0.0.1", 6450);
// specifying OSC address and type tag
recv.event( "/bbox, f" ) @=> OscEvent oe;
//--------------------------------------
.29 => float cutoff;

//for checking and getting vals
fun float getVals( OscEvent oe) {
    while ( true ) {
         oe => now;
         while (oe.nextMsg() != 0) {
            oe.getFloat() => cutoff;
         }
    }       
}

spork ~ getVals(oe);
while (true) {     
    //onset detected?
	if (adc.last() > cutoff) {
        <<<"ONSET DETECTED">>>;
		<<< adc.last() >>>;
        
    }
    
    //Send the amplitude back to Processing
    xmit.startMsg( "/amp", "f" );
    adc.last() => xmit.addFloat;
    
    //advance the time
	1::samp => now;
}