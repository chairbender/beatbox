//sends play
OscSend xmit;
xmit.setHost("127.0.0.1", 6460);

xmit.startMsg("/play");