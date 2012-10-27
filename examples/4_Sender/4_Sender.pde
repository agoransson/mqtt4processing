import org.json.*;
import se.goransson.mqtt.*;

MQTT mqtt;

void setup() {
  mqtt = new MQTT( this );
  mqtt.connect( "195.178.234.111", 1883, "mqtt_sender" );
}

void draw() {
}

void mouseDragged(){
  JSONObject obj = new JSONObject();
  obj.put("x", mouseX);
  obj.put("y", mouseY);
  
  mqtt.publish("mouse", obj.toString());
}