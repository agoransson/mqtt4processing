import se.goransson.mqtt.*;

MQTT mqtt;

void setup() {
  mqtt = new MQTT( this );
  mqtt.connect( "127.0.0.1", 1883, "processing" );
}

void draw() {
}

void keyPressed() {
  if (key == 'p' ){
    String s = "message" + random(0, 2000);
    println( "Sent: " + s);
    mqtt.publish("test", s);
  }else if( key == 'd' )
    mqtt.disconnect();
  else if( key == 's' )
    mqtt.subscribe("test");
}

void test(byte[] payload){
  print( "Got: " );
  String s = new String(payload, 2, payload.length-2);
  println( s );
}