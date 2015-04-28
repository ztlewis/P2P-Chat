package network;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.*;
import java.text.*;

import javax.swing.SwingUtilities;

import model.*;
import view.*;
import file.*;
import dht.*;

/**
 * This class is primarily designed to coordinate the network and deal with 
 * message sending and receiving.
 * <p>
 * The UDPPacketManager class is utilised to carry out these duties.
 * @author Alex
 * @version 0.2
 * @since 0.1
 */
public class ClientNetwork implements Runnable{

	private ClientModel model;
	private ClientConsole console;
	private CommManager comm;
	private FileManager file;
	private DHTNetwork dht;
	
	private Thread runningThread;
	private boolean isRunning = false;
	private String presAddress;
	private String presPort;
	private String method = "N/A";
	private String encryption = "N/A";
	private boolean presSet = false;
	
	/**
	 * Constructor for ClientNetwork.
	 */
	public ClientNetwork()
	{
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
		}
		catch(IOException ioException)
		{
			console.printError("Socket could not be created.");
		}
	}
	
	/**
	 * Set the method string for all packets forwarded
	 * to the presentation server. 
	 * @param method The method string.
	 * (e.g. UDP or TCP)
	 */
	public void setMethod(String method)
	{
		this.method = method;
	}
	
	/**
	 * Pass the model reference to the network.
	 * @param model The ClientModel object we want to include.
	 */
	public void initModel(ClientModel model)
	{
		this.model = model;
	}
	
	/**
	 * Pass the console reference to the network.
	 * @param console The ClientConsole object we want to include.
	 */
	public void initConsole(ClientConsole console)
	{
		this.console = console;
	}
	
	/**
	 * Pass the comm reference to the network.
	 * @param comm The UDPPacketManager object we want to include.
	 */
	public void initComm(CommManager comm)
	{
		this.comm = comm;
	}
	
	/**
	 * Pass the file manager reference to the network.
	 * @param file The FileManager object we want to include. 
	 */
	public void initFile(FileManager file)
	{
		this.file = file;
	}
	
	/**
	 * Pass the dht network referenec to the network.
	 * @param dht The DHTNetwork object we want to include. 
	 */
	public void initDHT(DHTNetwork dht)
	{
		this.dht = dht;
	}
	
	/**
	 * Retrieves the address of the client machine.
	 * @return The String representation of the IP address.
	 * @throws UnknownHostException
	 */
	public String getClientAddress() throws UnknownHostException
	{
		return comm.getClientAddress();
	}
	
	/**
	 * Retrieves the port of the client machine.
	 * @return The client port.
	 */
	public int getClientPort()
	{
		return comm.getClientPort();
	}
	
	/**
	 * Set the presState parameter to determine
	 * whether or not the presentation details
	 * have been set for this client.
	 * @param state
	 */
	public void setPresState(boolean state)
	{
		presSet = state;
	}
	
	/**
	 * Get the presState parameter. This determines
	 * whether or not the presentation details
	 * have been set for this client.
	 * @return
	 */
	public boolean getPresState()
	{
		return presSet;
	}
	
	/**
	 * This is the network's run method that attempts to retrieve a 
	 * packet and determine what course of action it should take.
	 * <p>
	 * This is done by checking the packet tag.
	 * <p>
	 * Registration success will cause the console to initiate.
	 * <p>
	 * Peer list will update the peer list with the new information.
	 * <p>
	 * Message will display message.
	 * <p>
	 * Life check will cause the network to confirm life to the server.
	 * <p>
	 * Error will cause exit.
	 */
	public void run()
	{	
		file.initFile();
		
		/*
		if(!getPresState())
		{
			console.request();
		}
		else
		{
			console.obtainUsername();
		}
		*/
		
		synchronized(this)
		{
			runningThread = Thread.currentThread();
		}
		
		while(isRunning())
		{
			if((!runningThread.isInterrupted())||(dht.isDhtRegistered())||
					(model.getServerRegistered()))
			{
				try {
					netRoutine();
				} catch (InterruptedException e) {
					if((!dht.isDhtRegistered())&&(!model.getServerRegistered()))
					{
						break;
					}
				}
			}
		}
		model.allowReLogin();
		System.out.println("Network thread ended.");
	}
	
	/**
	 * Method for receiving packet and determining what course
	 * of action to take depending on the packet tag. 
	 * @throws InterruptedException This means that the socket
	 * has timed out. It returns null in this case and throws
	 * an InterruptedException.
	 */
	private void netRoutine() throws InterruptedException
	{
		List<String> packetContents;
		List<String> fileDupe;
		List<String> forwardDupe;
		String packetTag;
		String sentAddress;
		int sentPort;
		String message ="";
		int size;
		String time;
		String clientAddress;
		int clientPort;
		String transTime = null;
		
		try
		{
			comm.clearMsgCache();
			packetContents = comm.receivePacket();
			if(packetContents == null)
			{
				throw new InterruptedException();
			}
			System.out.println(packetContents);
			packetTag = packetContents.remove(0);
			if(packetTag.equalsIgnoreCase("DUPLICATE"))
			{
				return;
			}
			if(packetTag.equalsIgnoreCase("RCV_CONFIRM"))
			{
				return;
			}
			size = Integer.parseInt(packetContents.remove(packetContents.size()-1));
			sentPort = Integer.parseInt(packetContents.remove(packetContents.size()-1));
			sentAddress = packetContents.remove(packetContents.size()-1);
			encryption = packetContents.remove(packetContents.size()-1);
			method = packetContents.remove(packetContents.size()-1);
			time = packetContents.remove(packetContents.size()-1);
			fileDupe = new ArrayList<String>();
			fileDupe.addAll(packetContents);
			forwardDupe = new ArrayList<String>();
			forwardDupe.addAll(packetContents);
			//Now packet contents will contain either the actual message,
			//peerlist information
			//or username (i.e. the contents of the message) or nothing.
			
			if(sentAddress.equalsIgnoreCase("127.0.0.1"))
			{
				try {
					sentAddress = comm.getClientAddress();
				} catch (UnknownHostException e) {
				}
			}
			
			if(packetTag.equalsIgnoreCase("ERROR"))
			{
				console.printError(message);
				model.setDuplicateStatus(true);
				throw new InterruptedException();
			}
			else if(packetTag.equalsIgnoreCase("REGISTRATION_SUCCESS"))
			{
				model.setServerRegistered(true);
				initialiseView(true);
				new Thread(console).start();
			}
			else if(packetTag.equalsIgnoreCase("PEER_LIST"))
			{
				model.updatePeerList(packetContents);
			}
			else if(packetTag.equalsIgnoreCase("MESSAGE"))
			{
				model.receiveMessage(sentAddress, sentPort, 
						packetContents.get(0), packetContents.get(1));
			}
			else if(packetTag.equalsIgnoreCase("LIFE_CHECK"))
			{
				//Send a response to the server to confirm that you
				//active.
				List<String> packetData = new ArrayList<String>();
				int length;
				packetData.add(model.getUsername());
				length = addData("LIFE_CONFIRM", packetData, sentAddress,
						sentPort);
			}
			else if(packetTag.equalsIgnoreCase("DHT_JOIN"))
			{
				dht.forwardJoinReq(packetContents);
				dht.printDetails();
			}
			else if(packetTag.equalsIgnoreCase("DHT_SETUP"))
			{
				dht.receiveDetails(packetContents, sentAddress, sentPort);
				dht.printDetails();
				initialiseView(false);
			}
			else if(packetTag.equalsIgnoreCase("DHT_ADD"))
			{
				dht.forwardAddition(packetContents);
				dht.printDetails();
			}
			else if(packetTag.equalsIgnoreCase("DHT_REMOVAL"))
			{
				dht.forwardRemoval(packetContents);
			}
			else if(packetTag.equalsIgnoreCase("DHT_UP"))
			{
				dht.receiveUpRequest(packetContents);
			}
			else if(packetTag.equalsIgnoreCase("DHT_UP_CONFIRM"))
			{
				
			}
			else if(packetTag.equalsIgnoreCase("DHT_DOWN"))
			{
				dht.receiveDownRequest(packetContents);
			}
			else if(packetTag.equalsIgnoreCase("DHT_DOWN_CONFIRM"))
			{
				String fileName = packetContents.remove(0);
				int dhtPort = Integer.parseInt(packetContents.remove(0));
				dht.receiveFile(fileName, sentAddress, dhtPort);
			}
			else if(packetTag.equalsIgnoreCase("DHT_TRANSFER"))
			{
				String fileName = packetContents.remove(0);
				int dhtPort = Integer.parseInt(packetContents.remove(0));
				dht.addFileName(fileName);
				dht.receiveFile(fileName, sentAddress, dhtPort);
			}
			else if(packetTag.equalsIgnoreCase("DHT_DOWN"))
			{
				dht.receiveDownRequest(packetContents);
			}
			else if(packetTag.equalsIgnoreCase("DHT_PRED_CHECK"))
			{
				List<String> packetData = new ArrayList<String>();
				addData("DHT_PRED_CONFIRM", packetData, sentAddress,
						sentPort);
			}
			else if(packetTag.equalsIgnoreCase("DHT_PRED_CONFIRM"))
			{
				dht.setDHTConfirmation(true);
			}
			else if(packetTag.equalsIgnoreCase("DHT_DEATH"))
			{
				dht.forwardDeath(packetContents);
			}
			else if(packetTag.equalsIgnoreCase("DHT_FIX"))
			{
				DHTNode newNode;
				
				newNode = dht.removeDHTInfo(packetContents);
				if(newNode != null)
				{
					dht.addNode(newNode.getAddress(), newNode.getPort());
				}
			}
			else if(packetTag.equalsIgnoreCase("FILE_SEND"))
			{
				String fileName = packetContents.remove(0);
				int filePort = Integer.parseInt(packetContents.remove(0));
				receiveFile(fileName, sentAddress, filePort, sentPort);
			}
			
			clientAddress = comm.getClientAddress();
			clientPort = comm.getClientPort();
			
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
					model.findUsername(sentAddress, sentPort), 
					sentAddress, Integer.toString(sentPort)
					, model.getUsername(), clientAddress, 
					Integer.toString(clientPort), 
					fileDupe, Integer.toString(size), transTime);
			
			forward(time, packetTag, method, encryption, 
					model.findUsername(sentAddress, sentPort), sentAddress, Integer.toString(sentPort),
					forwardDupe, Integer.toString(size), transTime);
			try {
				SwingUtilities.invokeAndWait(model);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch(ClassNotFoundException cnfException)
		{
			console.printError("An erroneous packet was recieved.");
		}
		catch(IOException ioException)
		{
			console.printError("An IO error occured while " +
					"recieving a packet.");
		}
	}
	
	private synchronized boolean isRunning()
	{
		return this.isRunning;
	}
	
	/**
	 * Send a registration request for the username with server and login.
	 * @param username The requested username.
	 * @param serverAddress The address of the chat server.
	 * @param serverPort The port of the chat server.
	 */
	public void registerClient(String username, String serverAddress, 
			String serverPort)
	{
		List<String> packetData = new ArrayList<String>();
		int length;
		
		packetData.add(username);
		length = addData("REGISTER", packetData,
				serverAddress, Integer.parseInt(serverPort));
				
	}
	
	/**
	 * Send a deregistration notice from the user to the currently connected
	 * server.
	 * @param username The username of the client.
	 * @param serverAddress The IP address of the current server.
	 * @param serverPort The 
	 */
	public void deregisterClient(String username, String serverAddress, 
			String serverPort)
	{
		List<String> packetData = new ArrayList<String>();
		int length;
		
		packetData.add(username);
		length = addData("DEREGISTER", packetData,
				serverAddress, Integer.parseInt(serverPort));
	}
	
	/**
	 * Send message to another peer.
	 * @param message Message contents.
	 * @param destAddress String address of destination peer.
	 * @param destPort Port of destination peer.
	 */
	public void sendMessage(String message, String destAddress, int destPort)
	{
		List<String> packetData = new ArrayList<String>();
		String destUsername;
		int length;
		
		packetData.add(generateTimeString());
		packetData.add(message);
		destUsername = model.findUsername(destAddress, destPort);
		
		length = addData("MESSAGE", packetData,
				destAddress, destPort);
	}
	
    /**
     * Generate time string used for time stamps.
     * @return String representing time.
     */
    public String generateTimeString() 
    {
        Date dateNow = new Date();
        SimpleDateFormat sdFormat = new SimpleDateFormat("hh:mm:ss a");

        return sdFormat.format(dateNow);
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
			String encryption, String destUsername, String destAddress, String destPort,
			List<String> contents, String size, String transTime)
	{
    	if(!getPresState())
    	{
    		return;
    	}
		String dateString;
		List<String> packetData = new ArrayList<String>();
		
		int counter;
		
		for(counter=0; counter<contents.size(); counter++)
		{
			packetData.add(contents.get(counter));
		}
		
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
		packetData.add(destUsername);
		packetData.add(destAddress);
		packetData.add(destPort);
		packetData.add(size);
		
		//Add server as "username".
		packetData.add(model.getUsername());
		System.out.println(packetData + " is being forwarded.");
		
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
	
	/**
	 * Function to add the extra information to packet so data can be 
	 * tracked.
	 * @param packetTag
	 * @param packetData
	 * @param clientAddress
	 * @param clientPort
	 */
	public int addData(String packetTag, List<String> packetData, String destAddress, 
			int destPort)
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
		System.out.println("Sending " + packetTag + packetData +
				" to " + destAddress + ":" + destPort);
		try
		{
			returnVal = comm.sendPacket(destAddress, destPort, 
					packetTag, packetData);
		}
		catch(IOException ioException)
		{
			console.printError("An error occured while creating the " +
					"packet.");
		}
		try {
			file.storeMessage(dateString, packetTag, method, encryption,
					model.getUsername(), comm.getClientAddress(),
					Integer.toString(comm.getClientPort()),
					model.findUsername(destAddress, destPort), destAddress,
					Integer.toString(destPort), packetDupe, Integer.toString(returnVal), "N/A");
		} catch (UnknownHostException e) {
			console.printError("Failed to store sent message.");
		}
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
	 * Call for a refresh in the GUI in order to display and changes
	 * that have been made to reflect new DHT information.
	 */
	public void refreshDHTGUI()
	{
		model.refreshDHTInfo();
	}
	
	/**
	 * Set the ID of the last node that a DHT_DOWN packet has been
	 * sent to.
	 * @param lastId ID of the receiving node.
	 */
	public void setLastDownloadId(int lastId)
	{
		model.setLastDownloadId(lastId);
	}
	
	/**
	 * Set the ID of the last node that a DHT_UP packet has been
	 * sent to.
	 * @param lastId ID of the receiving node. 
	 */
	public void setLastUploadId(int lastId)
	{
		model.setLastUploadId(lastId);
	}
	
	/**
	 * Initialises the view. 
	 * <p>
	 * The appearance of the GUI is dependent on the dhtStatus.
	 * @param dhtStatus True will make the DHT interface appear whilst
	 * false will make the centralised interface appear. 
	 */
	public void initialiseView(boolean dhtStatus)
	{
		model.initialiseView(dhtStatus);
	}
	
	/**
	 * Begin a new thread to receive a file. 
	 * @param fileName Name of the new file. 
	 * @param address Address of the sending node. 
	 * @param port Port of the sending node. 
	 */
	public void receiveFile(String fileName, String address, int filePort, int port)
	{
		Runnable r = new FileReceiver(fileName, address, filePort, 
				null, model.getView(), model.findUsername(address, port));
		new Thread(r).start();
	}
	
	/**
	 * Begin a new thread to send a file. 
	 * @param newFile File object corresponding to the file we want to send.
	 * @return The port that we are sending the file from. 
	 */
	public int sendFile(File newFile, String destAddress, int destPort)
	{
		FileSender r = new FileSender(newFile, null, model.getView(), 
				model.findUsername(destAddress, destPort));
		int portNo = r.getPort();
		new Thread(r).start();
		return portNo;
	}
	
	/**
	 * Send a request to another node to send a file to them.
	 * <p>
	 * This doesn't require a confirmation packet in return.
	 * @param destAddress Address of the receiving peer. 
	 * @param destPort Address of the receiving peer 
	 * @param selectedFile The file being transferred. 
	 */
	public void sendFileRequest(String destAddress, int destPort, File selectedFile)
	{
		List<String> packetData = new ArrayList<String>();
		int filePort = sendFile(selectedFile, destAddress, destPort);
		packetData.add(selectedFile.getName());
		packetData.add(Integer.toString(filePort));
		
		addData("FILE_SEND", packetData,
				destAddress, destPort);
	}
	
	/**
	 * Method that should be called whenever contact has been made with
	 * the server or DHT node that this peer is joining to.
	 * <p>
	 * A peer is considered to have made contact when it receives a 
	 * REGISTRATION_SUCCESS packet or a DHT_SETUP packet.
	 */
	public void connectionEstablished()
	{
		comm.connectionEstablished();
	}
}
