# mqtt4processing
## A basic MQTT client library for Processing

# About MQTT
MQTT stands for MQ Telemetry Transport. It is a publish/subscribe, extremely simple and lightweight messaging protocol, designed for constrained devices and low-bandwidth, high-latency or unreliable networks. The design principles are to minimise network bandwidth and device resource requirements whilst also attempting to ensure reliability and some degree of assurance of delivery. These principles also turn out to make the protocol ideal of the emerging “machine-to-machine” (M2M) or “Internet of Things” world of connected devices, and for mobile applications where bandwidth and battery power are at a premium.

*source: [MQTT.org](http://www.mqtt.org)*

# Installation

1. Download the latest version [here](http://www.santiclaws.se/mqtt4processing/mqtt4Processing.zip)
2. Extract the zip-file into your /sketchbook/libraries/ folder.
3. Restart Processing IDE

# Getting started with MQTT in Processing.

**Create the library object**

``` java
MQTT mqtt = new MQTT(this);
```

**Subscribing to a topic**

``` java
mqtt.subscribe( "mytopic" );

void mytopic(byte[] payload){
  String message = new String(payload);
}
```

**Publish message to a topic**

``` java
String message = "msg" + random(0,1000);
mqtt.publish( "mytopic", message );
```
