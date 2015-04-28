package network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class UDPPacketTimer implements Runnable{
	private List<Integer> msgIdList;
	private int packetId;
	private byte[] sendBuffer;
	private DatagramPacket sendPacket;
	private DatagramSocket socket;
	private int timesRetried;
	private UDPPacketManager udp;
	
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
	
	public int getPacketLength()
	{
		return sendPacket.getLength();
	}
	
	public void run()
	{
		timesRetried = 0;
		while((idExists(packetId))&&(timesRetried <= 5))
		{
			try {
				System.out.println("Sending message " + packetId + " " + timesRetried + " time");
				timesRetried++;
				socket.send(sendPacket);
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println("Resend procedure interrupted.");
			} catch (IOException e) {
				System.out.println("An IO error occured in the resend procedure.");
			}
		}
		if(timesRetried > 5)
		{
			udp.removeMsgId(packetId);
			System.out.println("Message " + packetId + " timed out.");
		}
	}
	
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
