import org.json.*;
import se.goransson.mqtt.*;

MQTT mqtt;

int mx = -20, my = -20;

void setup() {
  mqtt = new MQTT( this );
  mqtt.connect( "127.0.0.1", 1883, "mqtt_receiver" );
  
}

void keyPressed(){
  mqtt.subscribe( "mouse" );
}

void draw() {
  background( 0 );
  fill( 255 );
  ellipse( mx, my, 20, 20 );
}

void mouse(byte[] payload){
  String json = new String(payload);
  JSONObject obj = new JSONObject( json );
  mx = obj.getInt( "x" );
  my = obj.getInt( "y" );
}