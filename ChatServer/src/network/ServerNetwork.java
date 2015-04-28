package network;

import model.*;
import view.*;
import file.*;
import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is primarily designed to coordinate the network and deal with 
 * message sending and receiving.
 * <p>
 * The UDPPacketManager class is utilised to carry out these duties.
 * @author Alex
 * @version 0.2
 * @since 0.1
 */
public class ServerNetwork extends Thread{

	private ServerConsole console;
	private ServerModel model;
	private CommManager comm;
	private ServerConnections connection;
	private FileManager file;
	
	private static String threadName = "P2P Server";
	private String serverAddress;
	private int serverPort;
	private String presAddress;
	private String presPort;
	private boolean isRunning = false;
	private String method = "UDP";
	private String encryption = "N/A";
	private boolean presSet = false;
	
	/**
	 * The constructor for the ServerNetwork class.
	 * @throws IOException
	 */
	public ServerNetwork()
	{
		super(threadName);
		isRunning = true;
	}
	
	/**
	 * Use the UDP class to initialise the socket.
	 */
	public void initSocket()
	{
		try
		{
			comm.initSocket();
			serverAddress = comm.getServerAddress();
			serverPort = comm.getServerPort();
		}
		catch(IOException ioException)
		{
			console.printError("Socket could not be created.");
		}
	}
	
	/**
	 * Use the UDP class to intialise the socket. The address
	 * and port can be specified in this version of the method.
	 * @param address The String representation of the requested IP address.
	 * @param port The requested port number.
	 */
	public void initSocket(String address, String port)
	{
		try
		{
			comm.initSocket(address, port);
			serverAddress = comm.getServerAddress();
			serverPort = comm.getServerPort();
		}
		catch(IOException ioException)
		{
			console.printError("Socket could not be created.");
		}
	}
	
	public void initFile(FileManager file)
	{
		this.file = file;
	}
	
	/**
	 * Returns the IP address of the local host. If host is unknown, 
	 * UnknownHostException will be thrown.
	 * @return A String containing the IP address of the local host.
	 */
	public String getServerAddress() throws UnknownHostException
	{
		return comm.getServerAddress();
	}
	
	/**
	 * Returns the port that the server application is using.
	 * @return An int giving the port number that the server application 
	 * is using on the local host.
	 */
	public int getServerPort()
	{
		return comm.getServerPort();
	}
	
	/**
	 * Method to pass a reference to the model to the network
	 * class.
	 * @param network The ServerModel Object that is being passed in.
	 */
	public void initModel(ServerModel model)
	{
		this.model = model;
	}
	
	/**
	 * Method to pass a reference to the console to the network
	 * class.
	 * @param network The ServerConsole Object that is being passed in.
	 */
	public void initConsole(ServerConsole console)
	{
		this.console = console;
	}
	
	/**
	 * Method to pass a reference to the comm to the network
	 * class.
	 * @param network The UDPPacketManager Object that is being passed in.
	 */
	public void initComm(CommManager comm)
	{
		this.comm = comm;
	}
	
	/**
	 * Method to pass a reference to the connections to the network
	 * class.
	 * @param network The ServerConnections Object that is being passed in.
	 */
	public void initConnections(ServerConnections connection)
	{
		this.connection = connection;
	}
	
	/**
	 * This thread's run method. Loops constantly attempting to receive packets 
	 * and either register or deregister users with this information. 
	 * <p>
	 * Obtains the client address and port from this information. 
	 * After removing the packet tag, this method deduces what kind of 
	 * request the packet is for.
	 * <p>
	 * Whether the packet is telling the server about a registration or 
	 * deregistration, the server will then go through the peer list and 
	 * send an updated peer list to every peer.
	 */
	public void run()
	{	
		try
		{
			console.printConnectionDetails(getServerAddress(), 
					getServerPort());
		}
		catch(UnknownHostException uhException)
		{
			console.printError("Host address could not be identified.");
			System.exit(0);
		}
		
		file.initFile();
		
		/*
		if(!getPresState())
		{
			console.retrievePresDetails();
		}
		*/
		new Thread(connection).start();
		
		while(isRunning)
		{
			netRoutine();
		}
	}
	
	public void netRoutine()
	{
		List<String> packetContents;
		String packetTag;
		String clientAddress;
		int clientPort;
		String clientUsername;
		String time;
		int size;
		String transTime = null;
		
		try
		{
			packetContents = comm.receivePacket();
		}
		catch(IOException ioException)
		{
			console.printError("An IO error occured while " +
					"recieving a packet.");
			return;
		}
		catch(ClassNotFoundException cnfException)
		{
			console.printError("An erroneous packet was received.");
			return;
		}
		
		System.out.println(packetContents);
		packetTag = packetContents.remove(0);
		if(packetTag.equalsIgnoreCase("RCV_CONFIRM"))
		{
			return;
		}
		size = Integer.parseInt(packetContents.remove(packetContents.size()-1));
		clientPort = Integer.parseInt(packetContents.remove(packetContents.size()-1));
		clientAddress = packetContents.remove(packetContents.size()-1);
		encryption = packetContents.remove(packetContents.size()-1);
		method = packetContents.remove(packetContents.size()-1);
		time = packetContents.remove(packetContents.size()-1);
		
		if(clientAddress.equalsIgnoreCase("127.0.0.1"))
		{
			try {
				clientAddress = comm.getServerAddress();
			} catch (UnknownHostException e) {
			}
		}
		
		clientUsername = model.findUsername(clientAddress, clientPort);
		
		if(packetTag.equalsIgnoreCase("REGISTER"))
		{
			clientUsername = packetContents.get(0);
			register(clientUsername, clientAddress, clientPort);
		}
		else if(packetTag.equalsIgnoreCase("DEREGISTER"))
		{
			deregister(clientUsername, clientAddress, clientPort);
		}
		else if(packetTag.equalsIgnoreCase("LIFE_CONFIRM"))
		{
			connection.confirmLife(clientUsername);
			console.lifeVerified(clientUsername, clientAddress, 
					clientPort);
		}
		
		//Calculating transmission time.
		//Add date string to packet.
		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat
						("yyyy-MM-dd HH:mm:ss.SSS");
		try {
			Date sendDate = dateFormat.parse(time);
			dateFormat.format(currentDate);
			transTime = Long.toString(currentDate.getTime() - sendDate.getTime());
		} catch (ParseException e) {
			transTime = "N/A";
		}
		
		
		file.storeMessage(time, packetTag, method, encryption, 
				clientUsername, clientAddress, Integer.toString(clientPort)
				, "SERVER", serverAddress, 
				Integer.toString(serverPort), 
				packetContents, Integer.toString(size), transTime);
		forward(time, packetTag, method, encryption, 
				clientUsername, clientAddress, Integer.toString(clientPort),
				packetContents, Integer.toString(size), transTime);
	}
	
	public void setPresState(boolean state)
	{
		presSet = state;
	}
	
	public boolean getPresState()
	{
		return presSet;
	}
	
	/**
	 * Cycle through the list of peers and send an updated version of the 
	 * peer list to each one.
	 */
	public void distributePeerLists()
	{
		String peerAddress;
		int peerPort;
		List<String> packetData;
		int length=0;
		
		for(Peer peer : model.getPeers())
		{
			packetData = model.getPeersList();
			peerAddress = peer.getAddress();
			peerPort = peer.getPort();
			length = addData("PEER_LIST", packetData,
					peerAddress, peerPort);
			console.peerList(peer.getUsername(), peerAddress, peerPort);
		}
	}
	
	/**
	 * Attempt to register a user that has sent a request.
	 * @param clientUsername The username of the client.
	 * @param clientAddress The IP address of the client.
	 * @param clientPort The port of the client.
	 * @return
	 */
	public boolean register(String clientUsername, String clientAddress, 
			int clientPort)
	{
		Peer newPeer;
		List<String> packetData = new ArrayList<String>();
		int length;
		
		console.request(clientUsername, clientAddress, clientPort);

			if(model.usernameAvailable(clientUsername))
			{
				length = addData("REGISTRATION_SUCCESS", 
						packetData, clientAddress, clientPort);
				newPeer = new Peer(clientUsername, clientAddress, clientPort);
				model.registerPeer(newPeer);
				console.registerPeer(clientUsername, clientAddress, 
						clientPort);
				distributePeerLists();
			}
			else
			{
				packetData.add("Username already in use.");
				length = addData("ERROR", packetData,
						clientAddress, clientPort);
			}
			return true;
	}
	
	/**
	 * Deregister a peer.
	 * @param clientUsername The username of the client.
	 * @param clientAddress The IP address of the client.
	 * @param clientPort The port of the client.
	 */
	public void deregister(String clientUsername, String clientAddress, 
			int clientPort)
	{
		model.removePeer(clientUsername);
		console.deregisterPeer(clientUsername, clientAddress, clientPort);
		distributePeerLists();
	}
	
	/**
	 * Forward sent packet to presentation server.
	 * @param packetData Data in packet.
	 * @param clientUsername Recipient username.
	 * @param clientAddress Recipient address.
	 * @param clientPort Recipient port.
	 * @param length Length of original packet.
	 */
	public void forward(String time, String tag, String method, 
			String encryption, String clientUsername, String clientAddress, String clientPort,
			List<String> contents, String size, String transTime)
	{
		if(!getPresState())
    	{
    		return;
    	}
		String dateString;
		List<String> packetData = new ArrayList<String>();
		
		//Add date string to packet.
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat
				("yyyy-MM-dd HH:mm:ss.SSS");
		dateString = dateFormat.format(date);
		packetData.add(dateString);
		
		//Add method.
		packetData.add(method);
		
		//Add encryption.
		packetData.add(encryption);
		
		//Add receiver details.
		packetData.add(clientUsername);
		packetData.add(clientAddress);
		packetData.add(clientPort);
		packetData.add(size);
		
		//Add server as "username".
		packetData.add("SERVER");
		try
		{
			comm.sendPacket(presAddress, Integer.parseInt(presPort), 
					tag, packetData);
		}
		catch(IOException ioException)
		{
			console.printError("An error occured while creating the " +
					"presentation packet.");
		}
	}
	
	/**
	 * Function to add the extra information to packet so data can be 
	 * tracked.
	 * @param packetTag
	 * @param packetData
	 * @param clientAddress
	 * @param clientPort
	 */
	public int addData(String packetTag, List<String> packetData, String clientAddress, 
			int clientPort)
	{
		int returnVal = 0;
		String dateString;
		List<String> packetDupe = new ArrayList<String>();
		
		packetDupe.addAll(packetData);
		
		//Add date string to packet.
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat
				("yyyy-MM-dd HH:mm:ss.SSS");
		dateString = dateFormat.format(date);
		packetData.add(dateString);
		
		//Add method.
		packetData.add(method);
		
		//Add encryption.
		packetData.add(encryption);
		try
		{
			returnVal = comm.sendPacket(clientAddress, clientPort, 
					packetTag, packetData);
		}
		catch(IOException ioException)
		{
			console.printError("An error occured while creating the " +
					"packet.");
		}
		file.storeMessage(dateString, packetTag, method, encryption,
				"SERVER", serverAddress, Integer.toString(serverPort),
				model.findUsername(clientAddress, clientPort), clientAddress,
				Integer.toString(clientPort), packetDupe, Integer.toString(returnVal), "N/A");
		return returnVal;
	}
	
	/**
	 * Set presentation server details.
	 * @param presAddress String address of presentation server.
	 * @param presPort Port of presentation server.
	 */
	public void setPresDetails(String presAddress , String presPort)
	{
		this.presAddress = presAddress;
		this.presPort = presPort;
		setPresState(true);
	}
	
	/**
	 * Getter for presentation server address String.
	 * @return String representation of address.
	 */
	public String getPresAddress()
	{
		return presAddress;
	}
	
	/**
	 * Getter for presentaton server port.
	 * @return String of port.
	 */
	public String getPresPort()
	{
		return presPort;
	}
	
	 /**
     * Send a packet to the presentation server.
     * @param header The packet tag of the packet.
     * @param contents The contents of the packet.
     */
    public void notifyPresentationServer(String header, List<String> contents)
    {
    	if(!getPresState())
    	{
    		return;
    	}
    	try
    	{
    		System.out.println("Sending " + header + " to " + presAddress + ":" + presPort);
    		comm.sendPacket(presAddress, Integer.parseInt(presPort), 
    				header, contents);
    	}
    	catch(IOException ioException)
    	{
    		console.printError("An error occured while creating the " +
					"presentation packet.");
    	}
    }
}
