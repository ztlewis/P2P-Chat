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
import java.net.UnknownHostException;
import java.util.List;

public class TCPManager implements CommManager{
	ServerSocket servSocket;
	Socket clientSocket;
	
	public void initSocket() throws IOException
	{
		try {
			servSocket = new ServerSocket(0);
		} catch (IOException e) {
			System.out.println("TCP socket could not be initialised.");
		}
	}
	
	public void initSocket(String address, String port) throws IOException
	{
		try {
			servSocket = new ServerSocket(Integer.parseInt(port));
		} catch (NumberFormatException e) {
			System.out.println("Invalid port number.");
		} catch (IOException e) {
			System.out.println("TCP socket could not be initialised.");
		}
	}
	
	public String getServerAddress() throws UnknownHostException
	{
		InetAddress tempAddress;
		tempAddress = servSocket.getInetAddress();
		return tempAddress.getLocalHost().getHostAddress();
	}
	
	public int getServerPort()
	{
		return servSocket.getLocalPort();
	}
	
	public List<String> receivePacket() throws IOException, ClassNotFoundException
	{
		List<String> packetData;
		int length;
		DataInputStream is;
		byte[] recvBuffer = new byte[100000];
		
		clientSocket = servSocket.accept();
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
		//Add address, port.
		contents.add(getServerAddress());
		contents.add(Integer.toString(getServerPort()));
		
		DataOutputStream os;
		byte[] sendBuffer = new byte[100000];
		clientSocket = new Socket(rcvAddress, rcvPort);
		
		os = new DataOutputStream(clientSocket.getOutputStream());
		sendBuffer = listByteArray(header, contents);
		os.write(sendBuffer);
		return sendBuffer.length;
	}
	
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
}
