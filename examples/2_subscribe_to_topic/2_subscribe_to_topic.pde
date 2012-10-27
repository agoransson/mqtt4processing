/*
 * mqtt4processing
 * Example 2: Subscribe to a topic
 * 
 * author: Andreas Göransson, 2012
 */
import se.goransson.mqtt.*;

MQTT mqtt;

void setup() {
  // 1. Initialize the library object
  mqtt = new MQTT( this );
  
  // 2. Request a connection to a broker. The identification
  //    string at the end must be unique for that broker!
  mqtt.connect( "127.0.0.1", 1883, "mqtt_sender" );
  
  // 3. Subscribe to a simple topic
  mqtt.subscribe( "mytopic" );
}

void draw() {
}
