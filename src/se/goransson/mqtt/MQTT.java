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
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
	
	/**
	 * a Constructor, usually called in the setup() method in your sketch to
	 * initialize and start the library.
	 * 
	 * @example Hello
	 * @param theParent
	 */
	public MQTT(PApplet theParent, String host, int port) {
		mPApplet = theParent;
		welcome();
		
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		try {
			mConnection = new TCPConnection(addr, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mMonitoringThread = new MonitoringThread(mConnection);
		Thread thread = new Thread(null, mMonitoringThread, "MonitoringThread");
		thread.start();
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

	public void connect(String id){
		try {
			mConnection.getOutputStream().write(Messages.connect(id));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void publish(String topic, String message){
		try {
			mConnection.getOutputStream().write(Messages.publish(topic, message.getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private class MonitoringThread implements Runnable {

		Connection mConnection;

		public MonitoringThread(Connection connection) {
			mConnection = connection;
		}

		public void run() {
			int ret = 0;
			byte[] buffer = new byte[16384];

			while (ret >= 0) {
				try {
					ret = mConnection.getInputStream().read(buffer);
				} catch (IOException e) {
					break;
				}

				if (ret > 0) {
					
				}
			}
		}
	}
}
