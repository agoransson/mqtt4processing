package se.goransson.mqtt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Connection {

	public static final int STATUS_CLOSED = 1;
	public static final int STATUS_OPENED = 0;

	public abstract InputStream getInputStream() throws IOException;

	public abstract OutputStream getOutputStream() throws IOException;

	public abstract void close() throws IOException;
}
