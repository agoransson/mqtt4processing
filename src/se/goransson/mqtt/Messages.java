package se.goransson.mqtt;

/*
 * Copyright (C) 2012 Andreas Goransson, David Cuartielles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Messages {

	// Quality of Service

	/** Fire and Forget */
	protected static final byte AT_MOST_ONCE = 0x00;

	/** Acknowledged deliver */
	protected static final byte AT_LEAST_ONCE = 0x01;

	/** Assured Delivery */
	protected static final byte EXACTLY_ONCE = 0x02;

	// Message Types

	/** Message type: connect */
	protected static final byte CONNECT = 0x01;

	/** Message type: connection acknowledgement */
	protected static final byte CONNACK = 0x02;

	/** Message type: Publish message */
	protected static final byte PUBLISH = 0x03;

	/** Message type: Publish acknowledgement */
	protected static final byte PUBACK = 0x04;

	/** Message type: Publish received */
	protected static final byte PUBREC = 0x05;

	/** Message type: Publish release */
	protected static final byte PUBREL = 0x06;

	/** Message type: Publish complete */
	protected static final byte PUBCOMP = 0x07;

	/** Message type: Subscribe to topic */
	protected static final byte SUBSCRIBE = 0x08;

	/** Message type: Subscription acknowledgement */
	protected static final byte SUBACK = 0x09;

	/** Message type: Unsubscribe from topic */
	protected static final byte UNSUBSCRIBE = 0x0a;

	/** Message type: Unsubscribe acknowledgement */
	protected static final byte UNSUBACK = 0x0b;

	/** Message type: Ping request */
	protected static final byte PINGREQ = 0x0c;

	/** Message type: Ping response. */
	protected static final byte PINGRESP = 0x0d;

	/** Message type: Disconnect from server */
	protected static final byte DISCONNECT = 0x0e;

	/**
	 * Create a CONNECT MQTT message.
	 * 
	 * @return The resulting MQTT package.
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] connect(String identifier) throws IOException {
		ByteArrayOutputStream payload = new ByteArrayOutputStream();
		payload.write(0);
		payload.write(identifier.length());
		payload.write(identifier.getBytes("UTF-8"));
		return encode(CONNECT, false, 0, false, payload.toByteArray(), "false",
				"false", "false", "false", "false");
	}

	/**
	 * Create the DISCONNECT MQTT message, it has no variable header or payload
	 * and it doesn't use any other attribute of the fixed header than the type.
	 * 
	 * @return The resulting MQTT package.
	 * @throws IOException
	 */
	public static byte[] disconnect() throws IOException {
		return encode(DISCONNECT, false, 0, false, new byte[0]);
	}

	/**
	 * Create a PUBLISH MQTT message with QoS {@link #AT_MOST_ONCE} and message
	 * ID 0.
	 * 
	 * @param topic
	 *            Which topic to subscribe to.
	 * @param message
	 *            The message to send.
	 * @return The resulting MQTT package.
	 * @throws IOException
	 */
	public static byte[] publish(String topic, byte[] message)
			throws IOException {
		return encode(PUBLISH, false, 0, false, message, Integer.toString(0),
				topic);
	}

	/**
	 * Create a SUBSCRIBE message, it has a QoS of {@link #AT_LEAST_ONCE}.
	 * 
	 * @param message_id
	 *            The message id of the subscribe, handled by the client.
	 * @param subscribe_topic
	 *            The topic to which the client wants to subscribe.
	 * @param subscribed_qos
	 *            The wanted QoS for the subscription, can be
	 *            {@link #AT_MOST_ONCE}, {@link #AT_LEAST_ONCE}, or
	 *            {@link #EXACTLY_ONCE}.
	 * @return The MQTT package.
	 * @throws IOException
	 */
	public static byte[] subscribe(int message_id, String subscribe_topic,
			int subscribed_qos) throws IOException {
		ByteArrayOutputStream payload = new ByteArrayOutputStream();

		payload.write((byte) ((subscribe_topic.length() >> 8) & 0xFF));
		payload.write((byte) (subscribe_topic.length() & 0xFF));
		payload.write(subscribe_topic.getBytes("UTF-8"));
		payload.write(subscribed_qos);

		return encode(SUBSCRIBE, false, AT_LEAST_ONCE, false,
				payload.toByteArray(), Integer.toString(message_id));
	}

	public static byte[] unsubscribe(int message_id, String... subscribe_topic) throws IOException {
		ByteArrayOutputStream payload = new ByteArrayOutputStream();

		for (String topic : subscribe_topic) {
			payload.write((byte) ((topic.length() >> 8) & 0xFF));
			payload.write((byte) (topic.length() & 0xFF));
			payload.write(topic.getBytes("UTF-8"));
		}
		
		return encode(UNSUBSCRIBE, false, AT_LEAST_ONCE, false,
				payload.toByteArray(), Integer.toString(message_id));
	}
	
	public static byte[] ping() throws IOException {
		return encode(PINGREQ, false, 0, false, new byte[0]);
	}

	/**
	 * Low level compilation of an MQTT package.
	 * 
	 * @param type
	 *            Message type, can be {@link #CONNACK}, {@link #PUBLISH},
	 *            {@link #PUBACK}, {@link #PUBREC}, {@link #PUBREL},
	 *            {@link #PUBCOMP}, {@link #SUBSCRIBE}, {@link #SUBACK},
	 *            {@link #UNSUBSCRIBE}, {@link #UNSUBACK}, {@link #PINGREQ},
	 *            {@link #PINGRESP} or {@link #DISCONNECT}
	 * @param retain
	 *            Only used on publish messages. The server holds on to the last
	 *            message for each topic.
	 * @param qos
	 *            Quality of Service, can be {@link #AT_MOST_ONCE},
	 *            {@link #AT_LEAST_ONCE}, or {@link EXACTLY_ONCE}
	 * @param dup
	 *            Should be set to true when a message is being re-delivered.
	 *            Only when QoS is {@link #AT_LEAST_ONCE} or
	 *            {@link #EXACTLY_ONCE}
	 * @param payload
	 *            The message payload, varies depending on message type.
	 * @param params
	 *            The parameters for the Variable Header.
	 * @return The MQTT message as a byte array.
	 * @throws IOException
	 */
	protected static byte[] encode(int type, boolean retain, int qos,
			boolean dup, byte[] payload, String... params) throws IOException {
		// Create the container for the message
		ByteArrayOutputStream message = new ByteArrayOutputStream();

		// Fixed Header
		message.write((byte) ((retain ? 1 : 0) | qos << 1 | (dup ? 1 : 0) << 3 | type << 4));

		// Variable Header (create another ByteArrayOutputStream)
		ByteArrayOutputStream variableHeader = new ByteArrayOutputStream();

		switch (type) {
		case CONNECT:
			// Variable Header, read the params.
			// boolean username = Boolean.parseBoolean(params[0]);
			// boolean password = Boolean.parseBoolean(params[1]);
			// boolean will = Boolean.parseBoolean(params[2]);
			// boolean will_retain = Boolean.parseBoolean(params[3]);
			// boolean cleansession = Boolean.parseBoolean(params[4]);

			variableHeader.write((MQTT.MQTT_PROTOCOL.getBytes("UTF-8").length >> 8) & 0xFF); // MSB
			variableHeader.write(MQTT.MQTT_PROTOCOL.getBytes("UTF-8").length); // LSB
			variableHeader.write(MQTT.MQTT_PROTOCOL.getBytes("UTF-8"));
			variableHeader.write(MQTT.MQTT_VERSION);
			// Connect flags
			// variableHeader.write((cleansession ? 1 : 0) << 1
			// | (will ? 1 : 0) << 2 | (qos) << 3
			// | (will_retain ? 1 : 0) << 5 | (password ? 1 : 0) << 6
			// | (username ? 1 : 0) << 7);
			variableHeader.write((1) << 1 | (0) << 2 | (0) << 3 | (0) << 5
					| (0) << 6 | (0) << 7);
			variableHeader.write(0x00);
			variableHeader.write(0x0A);
			break;
		case PUBLISH:
			// Variable header, read the params.
			int message_id = Integer.parseInt(params[0]);
			String topic_name = params[1];

			variableHeader
					.write((topic_name.getBytes("UTF-8").length >> 8) & 0xFF); // MSB
			variableHeader.write(topic_name.getBytes("UTF-8").length); // LSB
			variableHeader.write(topic_name.getBytes("UTF-8")); // Topic
			if (qos > AT_MOST_ONCE) {
				variableHeader.write((message_id >> 8) & 0xFF); // Message ID MSB
				variableHeader.write(message_id & 0xFF); // Message ID LSB
			}
			break;
		case DISCONNECT:
			// Has no variable header
			break;
		case SUBSCRIBE:
			// Variable header, read the params.
			message_id = Integer.parseInt(params[0]);

			variableHeader.write((message_id >> 8) & 0xFF); // Message ID MSB
			variableHeader.write(message_id & 0xFF); // Message ID LSB
			break;
		case UNSUBSCRIBE:
			// Variable header, read the params.
			message_id = Integer.parseInt(params[0]);

			variableHeader.write((message_id >> 8) & 0xFF); // Message ID MSB
			variableHeader.write(message_id & 0xFF); // Message ID LSB
			break;
		}

		// Remaining length
		int length = payload.length + variableHeader.size();
		do {
			byte digit = (byte) (length % 128);
			length /= 128;
			if (length > 0)
				digit = (byte) (digit | 0x80);
			message.write(digit);
		} while (length > 0);

		// Variable header
		message.write(variableHeader.toByteArray());

		// Payload
		message.write(payload);

		// Return the finished message
		return message.toByteArray();
	}

	public static MQTTMessage decode(byte[] message) {
		int i = 0;
		MQTTMessage mqtt = new MQTTMessage();
		mqtt.type = (byte) ((message[i] >> 4) & 0x0F);
		mqtt.DUP = ((message[i] >> 3) & 0x01) == 0 ? false : true;
		mqtt.QoS = (message[i] >> 1) & 0x03;
		mqtt.retain = (message[i++] & 0x01) == 0 ? false : true;

		int multiplier = 1;
		int len = 0;
		byte digit = 0;
		int headerOffset = 1;
		do {
			headerOffset++;
			
			digit = message[i++];
			len += (digit & 127) * multiplier;
			multiplier *= 128;
		} while ((digit & 128) != 0);
		mqtt.remainingLength = len;

		
		switch (mqtt.type) {
		case CONNECT:
			// The client should never receive a CONNECT message.
			break;
			
		case CONNACK:
			// TODO
			byte reserved = message[i++];
			mqtt.variableHeader.put("return_code", message[i++]);
			
			break;
			
		case PUBLISH:
			int topic_name_len = (message[i++] * 256 + message[i++]);
			mqtt.variableHeader.put("topic_name_len", topic_name_len);
			
			String topic_name = new String(message, i, topic_name_len);
			mqtt.variableHeader.put("topic_name", topic_name);
			i += topic_name_len;

			// Only read the ID if the Quality of Service is above AT_MOST_ONCE!
			if( mqtt.QoS > AT_MOST_ONCE ){
				int message_id = (message[i++] << 8 & 0xFF00 | message[i++] & 0xFF);
				mqtt.variableHeader.put("message_id", Integer.toString(message_id));
			}
			break;
			
		case SUBSCRIBE:
			mqtt.variableHeader.put("message_id",
					(message[i++] << 8 | message[i++]));
			break;
		case PINGREQ:
			break;
		}

//		StringBuilder sb = new StringBuilder();
		
		ByteArrayOutputStream payload = new ByteArrayOutputStream();
		for (int b = i; b < headerOffset + mqtt.remainingLength; b++){
//			sb.append("'").append((char)message[b]).append("'").append(" ");
			payload.write(message[b]);
		}
//		System.out.println("Received");
//		System.out.println(sb.toString());
		
		mqtt.payload = payload.toByteArray();

		return mqtt;
	}

}
