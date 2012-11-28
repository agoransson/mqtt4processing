/*
 * mqtt4processing
 * Example: Subscribe to COSM feed
 * 
 * NOTE:
 * This example requires the use of a JSON library, the one I use can be found at
 * https://github.com/agoransson/JSON-processing However, there are other JSON
 * libraries you can use if you feel like it!
 * 
 * You can also change to use another feed, such as CSV!
 *
 * Change <your_api_key> to your private API key!
 * 
 * author: Andreas Göransson, 2012
 */
import org.json.*;

import se.goransson.mqtt.*;

MQTT mqtt;

void setup() {
  mqtt = new MQTT( this );
  
  mqtt.connect( "api.cosm.com", 1883, "processing" + random(0, 10000) );
}

void keyPressed(){
  mqtt.subscribe( "<your_api_key>/v2/feeds/40093.json", "mysub" );
}

void draw() {
}

void mysub( byte[] payload ) {
  String json = new String(payload);

  JSONObject root = new JSONObject( json );
  
  JSONArray streams = root.getJSONArray( "datastreams" );
  
  JSONObject inside_humidity = streams.getJSONObject( 2 );
  
  println( inside_humidity.getJSONArray("tags").get(0) );
  println( inside_humidity.getString("current_value") );
}

