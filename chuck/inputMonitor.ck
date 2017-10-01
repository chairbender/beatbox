//monitors input and sends the level
//out to Osc port 6450

"127.0.0.1" => string hostname;
OscSend xmit;
xmit.setHost(hostname, 6448);

// SIMPLE ENVELOPE FOLLOWER, by P. Cook

adc => Gain g => OnePole p => blackhole;
adc => g;

3 => g.op;
0.9999 => p.pole; // to smooth, add more nines

while (1)       {
    0.1 :: second => now;
    xmit.startMsg("/monitorLevel", "f");
    xmit.addFloat(p.last());
}