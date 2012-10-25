package se.goransson.mqtt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Messages {

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

	/** Message type: Disconnect from server */
	protected static final byte DISCONNECT = 0x0d;

	/**
	 * Create a CONNECT MQTT message.
	 * 
	 * @return The resulting MQTT package.
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] connect(String identifier)
			throws UnsupportedEncodingException, IOException {
		ByteArrayOutputStream payload = new ByteArrayOutputStream();
		payload.write(0);
		payload.write(identifier.length());
		payload.write(identifier.getBytes("UTF-8"));
		return encode(CONNECT, false, 0, false, payload.toByteArray(), "false",
				"false", "false", "false", "false");
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

			variableHeader.write(0x00); // LSB
			variableHeader.write(MQTT.MQTT_PROTOCOL.getBytes("UTF-8").length);
			variableHeader.write(MQTT.MQTT_PROTOCOL.getBytes("UTF-8"));
			variableHeader.write(MQTT.MQTT_VERSION);
			// Connect flags
			// variableHeader.write((cleansession ? 1 : 0) << 1
			// | (will ? 1 : 0) << 2 | (qos) << 3
			// | (will_retain ? 1 : 0) << 5 | (password ? 1 : 0) << 6
			// | (username ? 1 : 0) << 7);
			variableHeader.write((0) << 1 | (0) << 2 | (0) << 3 | (0) << 5
					| (0) << 6 | (0) << 7);
			variableHeader.write(0x00);
			variableHeader.write(0x0A);
			break;
		case PUBLISH:
			// Variable header, read the params.
			int message_id = Integer.parseInt(params[0]);
			String topic_name = params[1];

			variableHeader.write(0x00); // Topic MSB
			variableHeader.write(topic_name.getBytes("UTF-8").length); // Topic
																		// LSB
			variableHeader.write(topic_name.getBytes("UTF-8")); // Topic
			// variableHeader.write((message_id >> 8) & 0xFF); // Message ID MSB
			// variableHeader.write(message_id & 0xFF); // Message ID LSB
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
}
