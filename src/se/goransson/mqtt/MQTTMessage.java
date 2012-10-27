package se.goransson.mqtt;

import java.util.HashMap;
import java.util.Map;

public class MQTTMessage {

	public byte type;
	public boolean DUP;
	public int QoS;
	public boolean retain;
	public int remainingLength;
	
	public Map<String, Object> variableHeader = new HashMap<String, Object>();
	
	public byte[] payload;
}
