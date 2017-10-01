// Controller for listener.ck
import oscP5.*;
import netP5.*;
import controlP5.*;
OscP5 oscP5;
NetAddress myBroadcastLocation;  // IP address of destination
ControlP5 controlP5; 
int oscServerPortSend = 6449; 
int oscServerPortRecieve = 6450;
String oscServer = "127.0.0.1";

void setup() {
  size(200,200);
  
  // initializing oscP5 object
  oscP5 = new OscP5(this,oscServerPortRecieve);
  //address of osc reciever
  myBroadcastLocation = new NetAddress(oscServer,oscServerPortSend);
  // creating GUI instances with controlP5 object  
  controlP5 = new ControlP5(this);
  controlP5.addSlider("threshold",.001,.8,10,20,130,50);
  controlP5.addSlider("monitor",0.,1.,10,120,130,50);
  
}

void draw() {
  background(0);  // clearing the canvas every frame
}

void controlEvent(ControlEvent theEvent) {
  // build an OSC queue and send it to destination
  OscMessage myMessage = new OscMessage("/bbox");
  myMessage.add( controlP5.controller("threshold").value() );
  oscP5.send(myMessage, myBroadcastLocation);
}  
  
void oscEvent(OscMessage theOscMessage) {
   float result = theOscMessage.get(0).floatValue();
   controlP5.controller("monitor").setValue(result);

}
