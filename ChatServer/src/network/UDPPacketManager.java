package network;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import model.Peer;

/**
 * This class is designed to handle all UDP related procedures for the network.
 * <p>
 * All technical details regarding sockets and packet transmission is explicitly
 * dealt with in this class.
 * @author Alex
 * @version 0.2
 * @since 0.2
 */
public class UDPPacketManager implements CommManager {
	
	private DatagramSocket socket;
	private List<Integer> msgIdList;
	private Random random;
	private Map<Integer, Date> msgCache;
	
	/**
	 * Constructor for UDPPacketManager. 
	 */
	public UDPPacketManager()
	{
		msgIdList = new CopyOnWriteArrayList<Integer>();
		random = new Random();
		msgCache = new ConcurrentHashMap<Integer, Date>();
	}
	
	/**
	 * Create socket with custom specified address and port.
	 * @param address String IP address.
	 * @param port Port number desired.
	 * @throws IOException
	 */
	public void initSocket(String address, String port) throws IOException
	{
		//Case where address and port is specified via arguments.
		socket = new DatagramSocket(Integer.parseInt(port), 
				InetAddress.getByName(address));
	}
	
	/**
	 * Uses default IP and any socket that is available.
	 * @throws IOException
	 */
	public void initSocket() throws IOException
	{
		socket = new DatagramSocket(new InetSocketAddress(0));
	}
	
	/**
	 * Produce String representation of IP address.
	 * @return The client's local IP address.
	 * @throws UnknownHostException
	 */
	public String getServerAddress() throws UnknownHostException
	{
		InetAddress tempAddress;
		
		tempAddress = socket.getInetAddress();
		return tempAddress.getLocalHost().getHostAddress();
	}
	
	/**
	 * Return the client's port for the application.
	 * @return The client's port number.
	 */
	public int getServerPort()
	{
		return socket.getLocalPort();
	}
	
	/**
	 * Send a UDP packet to a given address and socket number.
	 * @param rcvAddress IP address of the recipient.
	 * @param rcvPort The socket of the recipient.
	 * @param header The packet tag.
	 * @param contents The contents of the message.
	 * @throws IOException
	 */
	public int sendPacket(String rcvAddress, int rcvPort, String header, 
			List<String> contents) throws IOException
	{
		int currentId;
		InetAddress convertAddress = InetAddress.getByName(rcvAddress);
		//Cycle back at 100 messages.
		currentId = random.nextInt();
		msgIdList.add(currentId);
		UDPPacketTimer r = new UDPPacketTimer(msgIdList, currentId, convertAddress
				,rcvPort, header, contents, socket, this);
		new Thread(r).start();
		return r.getPacketLength();
	}
	
	private void sendConfirmation(String rcvAddress, int rcvPort, int id) throws IOException
	{
		InetAddress convertAddress = InetAddress.getByName(rcvAddress);
		byte[] sendBuffer = new byte[100000];
		List<String> contents = new ArrayList<String>();
		contents.add(Integer.toString(id));
		
		sendBuffer = listByteArray("RCV_CONFIRM", contents);
		DatagramPacket sendPacket = new DatagramPacket(sendBuffer, 
				sendBuffer.length, convertAddress, rcvPort);
		socket.send(sendPacket);
	}
	
	/**
	 * Attempt to receive a packet from the initialised socket.
	 * @return The information in the packet in this order: 
	 * header, message (if any), clientAddress, clientPort. 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public List<String> receivePacket() throws IOException, 
	ClassNotFoundException
	{
		byte[] recvBuffer = new byte[100000];
		DatagramPacket recvPacket = new DatagramPacket(recvBuffer, 
				recvBuffer.length);
		List<String> packetData;
		String clientAddress;
		int clientPort;
		int size;
		int msgId;
		
		//Receive request
		socket.receive(recvPacket);
		size = recvPacket.getLength();
		
		//Add the address and port to the data so we can track it 
		//in other methods.
		clientAddress = recvPacket.getAddress().getHostAddress();
		clientPort = recvPacket.getPort();
		
		ObjectInputStream objIS = new ObjectInputStream(new 
				ByteArrayInputStream(recvPacket.getData()));
		
		packetData = (List<String>) objIS.readObject();
		msgId = Integer.parseInt(packetData.remove(1));
		if(msgCache.containsKey(msgId))
		{
			packetData.set(0, "DUPLICATE");
		}
		if(packetData.get(0).equalsIgnoreCase("RCV_CONFIRM"))
		{
			removeMsgId(msgId);
			System.out.println("Received confirmation " + msgId);
		}
		else
		{
			msgCache.put(msgId, new Date());
			sendConfirmation(clientAddress, clientPort, msgId);
			System.out.println("Got message " + msgId);
		}
	    
	    //Adds client address and port to the packet data.
		packetData.add(clientAddress);
		packetData.add(Integer.toString(clientPort));
		packetData.add(Integer.toString(size));
	    
		return packetData;
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
	
	public void removeMsgId(int id)
	{
		/*
		int counter;
		
		for(counter=0;counter<msgIdList.size();counter++)
		{
			if(msgIdList.get(counter) == id)
			{
				msgIdList.remove(counter);
				return;
			}
		}
		*/
		
		msgIdList.remove((Object)id);
	}
	
	public void clearMsgCache()
	{
		Date currentDate = new Date();
		Date recvDate;
		double time;
		Iterator<Map.Entry<Integer,Date>> iter = msgCache.entrySet().iterator();
		//Iterate through the map. If an entry has been there longer than 30 secs,
		//dispose of it.
		while (iter.hasNext()) {
		    Map.Entry<Integer,Date> entry = iter.next();
		    recvDate = entry.getValue();
		    time = currentDate.getTime() - recvDate.getTime();
		    if(time > 30000)
		    {
		    	System.out.println("Removed message " + entry.getKey() + "from the ID list.");
		    	iter.remove();
		    }
		}
	}
}