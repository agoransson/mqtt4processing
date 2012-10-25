import se.goransson.mqtt.*;

MQTT mqtt;

void setup(){
  mqtt = new MQTT( this, "195.178.234.111", 1883 );
  mqtt.connect( "processing" );
  
}

void draw(){
}

void keyPressed(){
  mqtt.publish("test", "message");
}