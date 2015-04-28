package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

/**
 * This class is designed to handle all TCP related procedures for the network.
 * <p>
 * All technical details regarding sockets and packet transmission is explicitly
 * dealt with in this class.
 * @author Alex
 * @version 0.3
 * @since 0.3
 */
public class TCPManager implements CommManager{
	private ServerSocket servSocket;
	private Socket clientSocket;
	private int timeout = 10000;
	private int connectionNum = 0;
	
	public void initSocket() throws IOException
	{
		try {
			servSocket = new ServerSocket(0);
			servSocket.setSoTimeout(timeout);
		} catch (IOException e) {
			System.out.println("TCP socket could not be initialised.");
		}
	}
	
	public void initSocket(String address, String port) throws IOException
	{
		try {
			servSocket = new ServerSocket(Integer.parseInt(port));
			servSocket.setSoTimeout(timeout);
		} catch (NumberFormatException e) {
			System.out.println("Invalid port number.");
		} catch (IOException e) {
			System.out.println("TCP socket could not be initialised.");
		}
	}
	
	public String getClientAddress() throws UnknownHostException
	{
		InetAddress tempAddress;
		tempAddress = servSocket.getInetAddress();
		return tempAddress.getLocalHost().getHostAddress();
	}
	
	public int getClientPort()
	{
		return servSocket.getLocalPort();
	}
	
	public List<String> receivePacket() throws IOException, ClassNotFoundException
	{
		List<String> packetData;
		int length;
		DataInputStream is;
		byte[] recvBuffer = new byte[100000];
		
		//Accept a connection to receive from. 
		try
		{
			clientSocket = servSocket.accept();
		}
		catch(SocketTimeoutException stException)
		{
			return null;
		}
		is = new DataInputStream(clientSocket.getInputStream());
		is.read(recvBuffer);
		
		ObjectInputStream objIS = new ObjectInputStream(new 
				ByteArrayInputStream(recvBuffer));
		packetData = (List<String>) objIS.readObject();
		//Make sure to add size to the packet info.
		packetData.add(Integer.toString(sizeOf(packetData).length));
		return packetData;
	}
	
	public int sendPacket(String rcvAddress, int rcvPort, String header, 
			List<String> contents) throws IOException
	{
		//Connection begins so increment the counter.
		connectionNum++;
		//Add address, port.
		contents.add(getClientAddress());
		contents.add(Integer.toString(getClientPort()));
		
		try
		{
			DataOutputStream os;
			byte[] sendBuffer = new byte[100000];
			clientSocket = new Socket(rcvAddress, rcvPort);
			os = new DataOutputStream(clientSocket.getOutputStream());
			sendBuffer = listByteArray(header, contents);
			os.write(sendBuffer);
			//Connection ended so decrement. 
			connectionNum--;
			return sendBuffer.length;
		}
		catch(IOException ioException)
		{
			//We want IO Exception to still get thrown as usual but before this
			//block of code ends, we want to decrement the counter so that our
			//program knows that this connection isn't live anymore. 
			connectionNum--;
			throw new IOException();
		}
	}
	
	/**
	 * Add a String header to the list and convert from a list of Strings to a 
	 * byte array so that it can be transmitted via packets.
	 * @param message The message to be added.
	 * @param list The String list.
	 * @return
	 */
	public byte[] listByteArray(String message, List<String> list) {
		ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
		ObjectOutputStream    objectOS;
		
		list.add(0, message);

		try {
			objectOS = new ObjectOutputStream(byteArrayOS);
			objectOS.writeObject(list);
			objectOS.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

		return byteArrayOS.toByteArray();
	}
	
	public void clearMsgCache()
	{
		
	}
	
	/**
	 * Get the size of an object. 
	 * <p>
	 * This is needed in TCP as we don't have a DatagramPacket with a size
	 * parameter. 
	 * @param obj The object we're measuring. 
	 * @return A byte array of the object that we can measure. 
	 * @throws java.io.IOException
	 */
	public static byte[] sizeOf(Object obj) throws java.io.IOException
	{
		ByteArrayOutputStream byteObject = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteObject);
		objectOutputStream.writeObject(obj);
		objectOutputStream.flush();
		objectOutputStream.close();
		byteObject.close();
		 
		return byteObject.toByteArray();
	}
	
	public void connectionEstablished()
	{
		try {
			servSocket.setSoTimeout(0);
		} catch (SocketException e) {
		}
	}
	
	public int getCacheSize()
	{
		return connectionNum;
	}
}
