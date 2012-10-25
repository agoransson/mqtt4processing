package se.goransson.mqtt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TCPConnection extends Connection {

	private Socket mSocket;
	
	public TCPConnection(InetAddress addr, int port) throws IOException {
		mSocket = new Socket(addr, port);
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		return mSocket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return mSocket.getOutputStream();
	}

	@Override
	public void close() throws IOException {
		mSocket.close();
	}

}
