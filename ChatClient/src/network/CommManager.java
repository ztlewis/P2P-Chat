package network;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

public interface CommManager {
	
	/**
	 * Uses default IP and any socket that is available.
	 * @throws IOException
	 */
	public void initSocket() throws IOException;
	/**
	 * Create socket with custom specified address and port.
	 * @param address String IP address.
	 * @param port Port number desired.
	 * @throws IOException
	 */
	public void initSocket(String address, String port) throws IOException;
	/**
	 * Produce String representation of IP address.
	 * @return The client's local IP address.
	 * @throws UnknownHostException
	 */
	public String getClientAddress() throws UnknownHostException;
	/**
	 * Return the client's port for the application.
	 * @return The client's port number.
	 */
	public int getClientPort();
	/**
	 * Attempt to receive a packet from the initialised socket.
	 * @return The information in the packet in this order: 
	 * header, message (if any), clientAddress, clientPort. 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public List<String> receivePacket() throws IOException, ClassNotFoundException;
	/**
	 * Send a UDP packet to a given address and socket number.
	 * @param rcvAddress IP address of the recipient.
	 * @param rcvPort The socket of the recipient.
	 * @param header The packet tag.
	 * @param contents The contents of the message.
	 * @throws IOException
	 */
	public int sendPacket(String rcvAddress, int rcvPort, String header, 
			List<String> contents) throws IOException;
	/**
	 * Dispose of any entries in the message cache that have been there for
	 * more than 30 seconds. 
	 */
	public void clearMsgCache();
	public void connectionEstablished();
	public int getCacheSize();
}
