/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package se.goransson.mqtt;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.HashMap;

import processing.core.PApplet;

/**
 * This is a template class and can be used to start a new processing library or
 * tool. Make sure you rename this class as well as the name of the example
 * package 'template' to your own library or tool naming convention.
 * 
 * @example Hello
 * 
 *          (the tag @example followed by the name of an example included in
 *          folder 'examples' will automatically include the example in the
 *          javadoc.)
 * 
 */

public class MQTT {

	/** Print debug messages */
	public boolean DEBUG = false;

	public static final int DISCONNECTED = 0;
	public static final int CONNECTING = 1;
	public static final int CONNECTED = 2;

	/** MQTT Protocol version (modeled after 3.1) */
	protected static final byte MQTT_VERSION = (byte) 0x03;

	/** MQTT Protocol name (modeled after standard 3.1, which is called MQIsdp) */
	protected static final String MQTT_PROTOCOL = "MQIsdp";

	/** Reference to the parent sketch */
	private PApplet mPApplet;

	/** Library version */
	public final static String VERSION = "##library.prettyVersion##";

	private Connection mConnection;

	private MonitoringThread mMonitoringThread;
	private KeepaliveThread mKeepaliveThread;

	private int state = DISCONNECTED;

	private int message_id = 0;

	private HashMap<String, Method> subscriptions;

	// Ping Related variables
	/**
	 * Keep alive defines the interval (in seconds) at which the client should
	 * send pings to the broker to avoid disconnects.
	 */
	private long keepalive = 10;

	/**
	 * Defines at what time the last action was taken by the client (this is
	 * used to determine if a ping should be sent or not)
	 */
	private long last_action = 0;

	/**
	 * Defines the number of seconds that the client will wait for a ping
	 * response before disconnecting.
	 */
	private long ping_grace = 5;

	/** Storage for the last sent ping request message. */
	private long last_ping_request = 0;

	/** Defines if a ping request has been sent. */
	private boolean ping_sent = false;

	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param theParent
	 */
	public MQTT(PApplet theParent) {
		mPApplet = theParent;
		welcome();

		subscriptions = new HashMap<String, Method>();
	}

	private void welcome() {
		System.out
				.println("##library.name## ##library.prettyVersion## by ##author##");
	}

	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}

	/**
	 * Set the keep alive time out for the client in seconds; this defines at
	 * what interval the client should send pings to the broker so that it
	 * doesn't disconnect.
	 * 
	 * Default is set at 10 seconds.
	 * 
	 * @param seconds
	 */
	public void setKeepalive(int seconds) {
		keepalive = seconds;
	}

	/**
	 * Connect to a MQTT server. This also sends the required connect message.
	 * 
	 * @param host
	 * @param port
	 * @param id
	 */
	public void connect(String host, int port, String id) {
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			PApplet.println("Ohno! Something went wrong... Unknown host error, I didn't understand the host name.");
			return;
		}

		try {
			mConnection = new TCPConnection(addr, port);
		} catch (IOException e) {
			PApplet.println("Ohno! Something went wrong... IO Error, failed to establish a connection to host.");
			return;
		}

		mMonitoringThread = new MonitoringThread(mConnection);
		Thread thread1 = new Thread(null, mMonitoringThread, "MonitoringThread");
		thread1.start();

		mKeepaliveThread = new KeepaliveThread();
		Thread thread2 = new Thread(null, mKeepaliveThread, "KeepaliveThread");
		thread2.start();

		connect(id);
	}

	/**
	 * Used to send the connect message, shouldn't be used outside the MQTT
	 * class.
	 * 
	 * @param id
	 */
	private void connect(String id) {
		if (state == DISCONNECTED) {
			try {
				mConnection.getOutputStream().write(Messages.connect(id));
				last_action = System.currentTimeMillis();
			} catch (IOException e) {
				if (DEBUG)
					PApplet.println("Ohno! Something went wrong... IO Error, failed to send CONNECT message.");
				return;
			}
		} else {
			if (DEBUG)
				PApplet.println("Ohno! Something went wrong... MQTT Error, you gots to be disconnected dude!");
		}
	}

	/**
	 * Send the disconnect message.
	 */
	public void disconnect() {
		if (state == CONNECTED) {
			try {
				mConnection.getOutputStream().write(Messages.disconnect());
				// last_action = System.currentTimeMillis();
			} catch (IOException e) {
				if (DEBUG)
					PApplet.println("Ohno! Something went wrong... IO Error, failed to send DISCONNECT message.");
			}

			try {
				mConnection.close();
			} catch (IOException e) {
				if (DEBUG)
					PApplet.println("Ohno! Something went wrong... IO Error, failed to close the connection.");
			}

			mMonitoringThread.stop();
			mKeepaliveThread.stop();

			state = DISCONNECTED;
		} else {
			if (DEBUG)
				PApplet.println("Ohno! Something went wrong... MQTT Error, you gots to be connected dude!");
		}
	}

	public void publish(String topic, String message) {
		if (state == CONNECTED) {
			try {
				mConnection.getOutputStream().write(
						Messages.publish(topic, message.getBytes()));
				last_action = System.currentTimeMillis();
			} catch (IOException e) {
				if (DEBUG)
					PApplet.println("Ohno! Something went wrong... IO Error, failed to send PUBLISH message.");
			}
		} else {
			if (DEBUG)
				PApplet.println("Ohno! Something went wrong... MQTT Error, you gots to be connected dude!");
		}
	}

	public void subscribe(String topic) {
		if (state == CONNECTED) {
			try {
				mConnection.getOutputStream().write(
						Messages.subscribe(getMessageId(), topic,
								Messages.EXACTLY_ONCE));
				last_action = System.currentTimeMillis();

				registerSubscription(topic);
			} catch (IOException e) {
				if (DEBUG)
					PApplet.println("Ohno! Something went wrong... IO Error, failed to send SUBSCRIBE message.");
			}
		} else {
			if (DEBUG)
				PApplet.println("Ohno! Something went wrong... MQTT Error, you gots to be connected dude!");
		}
	}

	/**
	 * 
	 * @param topic
	 * @return
	 */
	private boolean registerSubscription(String topic) {
		Method m = null;
		try {
			m = mPApplet.getClass().getMethod(topic, byte[].class);
		} catch (Exception e) {
			if (DEBUG)
				PApplet.println("Ohno! Something went wrong... MQTT Error, you forgot to add the subscription method! 1 "
						+ topic);
			return false;
		}

		// Add the callback
		if (m != null) {
			subscriptions.put(topic, m);
		} else {
			if (DEBUG)
				PApplet.println("Ohno! Something went wrong... MQTT Error, you forgot to add the subscription method! 2 "
						+ topic);
			return false;
		}
		return true;
	}

	private int getMessageId() {
		message_id++;

		if (message_id == 65536) {
			message_id = 0;
		}

		return message_id;
	}

	private class KeepaliveThread implements Runnable {

		private volatile boolean finished = false;

		@Override
		public void run() {
			while (!finished) {

				if (ping_sent
						&& System.currentTimeMillis() - last_ping_request > (ping_grace * 1000)) {
					if (DEBUG)
						PApplet.println("Ohno! Something went wrong... MQTT Error, didn't get a ping response - maybe the connection died.");
					disconnect();
					ping_sent = false;
				}

				if (System.currentTimeMillis() - last_action > (keepalive * 1000)) {
					if (state == CONNECTED) {
						synchronized (mConnection) {
							try {
								mConnection.getOutputStream().write(
										Messages.ping());
								ping_sent = true;
								last_action = System.currentTimeMillis();
							} catch (IOException e) {
								if (DEBUG)
									PApplet.println("Ohno! Something went wrong... IO Error, failed to send PING message.");
							}
						}
					}
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}

		public void stop() {
			finished = true;
		}
	}

	private class MonitoringThread implements Runnable {

		Connection mConnection;

		private volatile boolean finished;

		public MonitoringThread(Connection connection) {
			mConnection = connection;
		}

		public void stop() {
			finished = true;
		}

		public void run() {
			int ret = 0;
			byte[] buffer = new byte[16384];

			while (!finished || ret >= 0) {
				try {
					ret = mConnection.getInputStream().read(buffer);
				} catch (IOException e) {
					if (DEBUG)
						PApplet.println("Ohno! Something went wrong... IO Error, failed to read messages.");
					break;
				}

				if (ret > 0) {
					MQTTMessage msg = Messages.decode(buffer);

					switch (msg.type) {
					case Messages.CONNECT:
						if (DEBUG)
							PApplet.println("CONNECT");
						state = CONNECTING;
						break;
					case Messages.CONNACK:
						if (DEBUG)
							PApplet.println("CONNACK");
						state = CONNECTED;
						break;
					case Messages.PUBLISH:
						if (DEBUG)
							PApplet.println("PUBLISH");

						Method eventMethod = subscriptions
								.get(msg.variableHeader.get("topic_name"));
						if (eventMethod != null) {
							try {
								eventMethod.invoke(mPApplet, msg.payload);
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							}
						}
						break;
					case Messages.PUBACK:
						if (DEBUG)
							PApplet.println("PUBACK");
						break;
					case Messages.PUBREC:
						if (DEBUG)
							PApplet.println("PUBREC");
						break;
					case Messages.PUBREL:
						if (DEBUG)
							PApplet.println("PUBREL");
						break;
					case Messages.PUBCOMP:
						if (DEBUG)
							PApplet.println("PUBCOMP");
						break;
					case Messages.SUBSCRIBE:
						if (DEBUG)
							PApplet.println("SUBSCRIBE");
						break;
					case Messages.SUBACK:
						if (DEBUG)
							PApplet.println("SUBACK");
						break;
					case Messages.UNSUBSCRIBE:
						if (DEBUG)
							PApplet.println("UNSUBSCRIBE");
						break;
					case Messages.UNSUBACK:
						if (DEBUG)
							PApplet.println("UNSUBACK");
						break;
					case Messages.PINGREQ:
						if (DEBUG)
							PApplet.println("PINGREQ");
						last_ping_request = System.currentTimeMillis();
						break;
					case Messages.PINGRESP:
						if (DEBUG)
							PApplet.println("PINGRESP");
						ping_sent = false;
						break;
					}
				}
			}
		}
	}
}
