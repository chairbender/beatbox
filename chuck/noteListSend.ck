"127.0.0.1" => string hostname;
OscSend xmit;
xmit.setHost( hostname, 6450 );

//xmit.startMsg("/listenForNoteList","i");
//xmit.addInt(1);

xmit.startMsg("/noteList","if");
xmit.addInt(1);
xmit.addFloat(300.0);