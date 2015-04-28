package network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

/**
 * The purpose of this class is to add a retrying mechanism to the 
 * UDPPacketManager class. 
 * <p>
 * This class is only used in UDPPacketManager and implements a retrying
 * mechanism for when UDP packets are being sent. 
 * <p>
 * UDP is a lossy communication protocol and packets can be dropped
 * randomly due to bad connection or high network traffic. 
 * <p>
 * This class attempts to negate that by retrying 5 times (allowing 5 seconds
 * for packet confirmation to return from the receiver between retries) before giving up. 
 * The message cache ensures that duplicate messages are not handled (because
 * packet confirmations could potentially be lost too). 
 * @author Alex
 * @version 0.3
 * @since 0.3
 */
public class UDPPacketTimer implements Runnable{
	private List<Integer> msgIdList;
	private int packetId;
	private byte[] sendBuffer;
	private DatagramPacket sendPacket;
	private DatagramSocket socket;
	private int timesRetried;
	private UDPPacketManager udp;
	
	/**
	 * A constructor for UDPPacketTimer. 
	 * @param msgIdList The list of message ids. 
	 * @param packetId The id of the current packet. 
	 * @param rcvAddress The address of the node receiving the packet. 
	 * @param rcvPort The port of the node receiving the packet. 
	 * @param header The packet tag. 
	 * @param contents The contents of the packet. 
	 * @param socket The UDP socket sent from UDPPacketManager class. 
	 * @param udp The UDPPacketManager object that created this. 
	 */
	public UDPPacketTimer(List<Integer> msgIdList, int packetId, InetAddress rcvAddress, 
			int rcvPort, String header, List<String> contents, DatagramSocket socket, 
			UDPPacketManager udp)
	{
		sendBuffer = new byte[100000];
		this.msgIdList = msgIdList;
		this.packetId = packetId;
		this.socket = socket;
		contents.add(0, Integer.toString(packetId));
		sendBuffer = listByteArray(header, contents);
		sendPacket = new DatagramPacket(sendBuffer, 
				sendBuffer.length, rcvAddress, rcvPort);
		this.udp = udp;
	}
	
	/**
	 * Return the packet length. 
	 * @return Packet length. 
	 */
	public int getPacketLength()
	{
		return sendPacket.getLength();
	}
	
	public void run()
	{
		timesRetried = 0;
		//Make sure that we haven't exceeded 5 times and also make sure that 
		//the packet ID hasn't already been confirmed. 
		while((idExists(packetId))&&(timesRetried <= 5))
		{
			try {
				System.out.println("Sending message " + packetId + " " + 
			timesRetried + " time");
				timesRetried++;
				socket.send(sendPacket);
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println("Resend procedure interrupted.");
			} catch (IOException e) {
				System.out.println("An IO error occured in the resend "
						+ "procedure.");
			}
		}
		//If we exited that loop because we retried too many times then that 
		//means we timed out. 
		if(timesRetried > 5)
		{
			udp.removeMsgId(packetId);
			System.out.println("Message " + packetId + " timed out.");
		}
	}
	
	/**
	 * Checks whether the ID list contains the provided ID. 
	 * <p>
	 * Returns true if it is contained and false if it is not. 
	 * @param id The id being checked. 
	 * @return Whether or not it is contained in the ID list. 
	 */
	public boolean idExists(int id)
	{
		int counter;
		
		for(counter=0;counter<msgIdList.size();counter++)
		{
			if(msgIdList.get(counter) == id)
			{
				return true;
			}
		}
		return false;
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
}
