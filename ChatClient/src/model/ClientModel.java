package model;

import java.io.File;
import java.util.*;

import dht.DHTNetwork;
import network.*;
import view.*;


/**
 * This class is designed to deal with the client's registration with the 
 * server well as identify incoming packets from other peers.
 * <p>
 * When receiving messages from other peers, this class also triggers 
 * events in the view to ensure that the GUI changes and opens tabs 
 * appropriately (and also the console).
 * @author Alex
 * @version 0.2
 * @since 0.1
 */
public class ClientModel implements Runnable{
	
	private ClientNetwork network;
	private ClientConsole console;
	private DHTNetwork dht;
	private ClientView view;
	private ClientLogin login;
	private boolean serverRegistered;
	private String username;
	private String serverIP;
	private String serverPort;
	private Map<String, Peer> peerList;
	private boolean dhtStatus = false;
	private boolean duplicateName = false;
	
	/**
	 * Constructor for ClientModel class.
	 */
	public ClientModel()
	{
		serverRegistered = false;
		peerList = new HashMap<String, Peer>();
		username = "N/A";
	}
	
	/**
	 * Set the DHT status. 
	 * <p>
	 * Should be set to true when DHT is enabled and false when it is not. 
	 * @param status New DHT status. 
	 */
	public void enableDHT(boolean status)
	{
		dhtStatus = status;
	}
	
	/**
	 * Getter for the DHT status. 
	 * @return The DHT status. 
	 */
	public boolean dhtEnabled()
	{
		return dhtStatus;
	}
	
	/**
	 * Pass network reference to model class.
	 * @param network The ClientNetwork object being passed to model.
	 */
	public void initNetwork(ClientNetwork network)
	{
		this.network = network;
	}
	
	/**
	 * Pass console reference to model class.
	 * @param console The ClientConsole object being passed to model.
	 */
	public void initConsole(ClientConsole console)
	{
		this.console = console;
	}
	
	/**
	 * Pass view reference to the model class. 
	 * @param view The ClientView object being passed to model. 
	 */
	public void initView(ClientView view)
	{
		this.view = view;
	}
	
	/**
	 * Pass DHT reference to the model class. 
	 * @param dht The DHTNetwork object being passed to model. 
	 */
	public void initDHT(DHTNetwork dht)
	{
		this.dht = dht;
	}
	
	//Segmented this into two functions in the case where server is provided
	//as a commandline argument.
	/**
	 * Set the server connection details. 
	 * @param serverIP The server IP address. 
	 * @param serverPort The server port. 
	 */
	public void setServer(String serverIP, String serverPort)
	{
		this.serverIP = serverIP;
		this.serverPort = serverPort;
	}
	
	/**
	 * Set the username. Also calls the registerClent() method which will
	 * send a registration packet to the server. 
	 * @param username The desired username. 
	 */
	public void setUsername(String username)
	{
		this.username = username;
		registerClient();
	}
	
	/**
	 * Initializes session in the model for a given client.
	 * <p>
	 * Stores the connection information at this point.
	 * @param username A String representing the client's username.
	 * @param serverIP A String representing the server's IP address.
	 * @param serverPort The port number of the server.
	 */
	public void initSession(String username, String serverIP, 
			String serverPort)
	{
		this.username = username;
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		
		registerClient();
	}
	
	/**
	 * Checks client is registered with the server.
	 * @return Returns T for registration success and F for registration 
	 * failure.
	 */
	public boolean getServerRegistered()
	{
		return serverRegistered;
	}
	
	/**
	 * Sets the serverRegistered boolean to either T or F depending on 
	 * parameter.
	 * @param status The boolean value that the function will set the 
	 * serverRegistered value to.
	 */
	public void setServerRegistered(boolean status)
	{
		serverRegistered = status;
		if(status)
		{
			network.connectionEstablished();
		}
	}
	
	/**
	 * Getter for the username variable.
	 * @return The username of the client.
	 */
	public String getUsername()
	{
		if(dhtStatus)
		{
			return Integer.toString(dht.getId());
		}
		else
		{
			return username;
		}
	}
	
	/**
	 * Getter for the serverIP variable.
	 * @return The IP address of the server.
	 */
	public String getServerIP()
	{
		return serverIP;
	}
	
	/**
	 * Getter for the serverPort variable.
	 * @return The port number of the server.
	 */
	public String getServerPort()
	{
		return serverPort;
	}
	
	/**
	 * Getter for the peer list.
	 * @return A Map structure containing the listed peer information.
	 */
	public Map<String, Peer> getPeerList()
	{
		return peerList;
	}
	
	/**
	 * Attempt to register the client by causing the ClientNetwork Object to 
	 * contact the server.
	 */
	public void registerClient()
	{
		network.registerClient(username, serverIP, serverPort);
	}
	
	/**
	 * Deregister the client by causing the ClientNetwork Object to 
	 * contact the server.
	 */
	public void deregisterClient()
	{
		network.deregisterClient(username, serverIP, serverPort);
	}
	
	/**
	 * Receive a message.
	 * <p>
	 * This involves cycling through the list of peers and checking the 
	 * incoming IP address with those listed to determine who is the sender.
	 * @param srcAddress The address where the packet came from.
	 * @param srcPort The port where the packet came from on the peer's side.
	 * @param time The time that the packet was sent.
	 * @param message The message contents.
	 */
	public void receiveMessage(String srcAddress, int srcPort, 
			String time, String message)
	{
		//Determine which peer sent the message.
		for(Peer peer : peerList.values())
		{
			if((peer.getAddress().equalsIgnoreCase(srcAddress)) && 
					(peer.getPort() == srcPort))
			{
				console.receiveMessage(peer.getUsername(), time, message);
				view.recieveMessage(peer.getUsername(), time, message);
			}
		}
	}
	
	/**
	 * Send a message.
	 * <p>
	 * This involves cycling through the list of peers and checking for the IP 
	 * address of the one that we wish to send a message to. The ClientNetwork
	 * Object is then contacted to deal with the specifics of sending the
	 * actual packet.
	 * @param peerID The String representation of the username of the peer.
	 * @param message A String containing the message.
	 */
	public void sendMessage(String username, String message)
	{
	    for(Peer peer : peerList.values())
        {
            if(peer.getUsername().equalsIgnoreCase(username))
            {
                String destAddress = peer.getAddress();
                int destPort = peer.getPort();
                
                network.sendMessage(message, destAddress, destPort);
            }
        }
	}
	
	/**
	 * Update the list of peers.
	 * <p>
	 * The peer information is taken out of the list and stored before a method
	 * is called from the ClientView to reflect these changes in the GUI.
	 * @param list A List of Strings containing peer information.
	 */
	public void updatePeerList(List<String> list)
	{
        peerList.clear();
		for(String peerString : list) 
		{
            String[] peerDetails = peerString.split(";");
            String peerUsername = peerDetails[0];
            String peerAddress  = peerDetails[1];
            int peerPort     = Integer.parseInt(peerDetails[2]);
            
                peerList.put(peerAddress + ":" + peerPort, 
                		new Peer(peerUsername, peerAddress, peerPort));
		}
		console.newPeerList();
		view.refreshPeerList(peerList);
	}
	
	/**
	 * Search the peer list for the username that corresponds to the provided
	 * username and port. 
	 * <p>
	 * If the address/port combination is not found in the peer list, this 
	 * method will return the string "SERVER". 
	 * @param address
	 * @param port
	 * @return
	 */
	public String findUsername(String address, int port)
	{
		if(dhtStatus)
		{
			String id;
			id = Integer.toString(dht.hash(address + port));
			return id;
		}
		for(Peer peer : peerList.values())
		{
			if(peer.getAddress().equalsIgnoreCase(address) && 
					peer.getPort() == port)
			{
				return peer.getUsername();
			}
		}
		return "SERVER";
	}
	
	/**
	 * Set the connection details of the Presentation Server. 
	 * @param address The address of the Presentation Server. 
	 * @param port The port of the Presentation Server. 
	 */
	public void setPresDetails(String address, String port)
	{
		network.setPresDetails(address, port);
	}
	
	/**
	 * Trigger changes in the view to reflect changes to filenames, lists
	 * or the finger table. 
	 */
	public void refreshDHTInfo()
	{
		view.refreshFileNames();
		view.refreshFingerTable();
		view.refreshLists();
	}
	
	public void run()
	{
		refreshDHTInfo();
	}
	
	/**
	 * Save the last download id. 
	 * @param id The last download id. 
	 */
	public void setLastDownloadId(int id)
	{
		view.setLastDownloadId(id);
	}
	
	/**
	 * Save the last upload id. 
	 * @param id The last upload id. 
	 */
	public void setLastUploadId(int id)
	{
		view.setLastUploadId(id);
	}
	
	/**
	 * Trigger the GUI to dispose of the login screen and to show the chat/dht
	 * interface. 
	 * <p>
	 * This depends what DHT status is given. If it is true, the DHT interface
	 * should appear and if not, the chat interface should appear. 
	 * @param dhtStatus
	 */
	public void initialiseView(boolean dhtStatus)
	{
		view.initialise(dhtStatus);
		login.disposeLogin();
	}
	
	/**
	 * Pass login reference to model class. 
	 * @param login The ClientLogin object being passed to the model. 
	 */
	public void initLogin(ClientLogin login)
	{
		this.login = login;
	}
	
	/**
	 * Sends a file transfer request to a particular peer.
	 * @param username The username of the designated peer.
	 * @param selectedFile The file object of the desired local
	 * file to be transferred. 
	 */
	public void sendFileRequest(String username, File selectedFile)
	{
		String destAddress;
		int destPort;
		
		for(Peer peer : peerList.values())
		{
			if((peer.getUsername()).equalsIgnoreCase(username))
			{
				destAddress = peer.getAddress();
				destPort = peer.getPort();
				
				network.sendFileRequest(destAddress, destPort, selectedFile);
			}
		}
	}
	
	/**
	 * Retrieve the ClientView object for the model.
	 * @return The model's ClientView object. 
	 */
	public ClientView getView()
	{
		return view;
	}
	
	/**
	 * Allow the user the login to the model's initialised
	 * ClientLogin object. 
	 */
	public void allowReLogin()
	{
		login.allowReLogin();
	}
	
	/**
	 * Set the boolean that keeps track of whether a duplicate
	 * username has been provided. Receiving an "ERROR" packet
	 * fro the server when logging in will trigger this method.
	 * @param status True for duplicate, false for unique or
	 * not logged in.
	 */
	public void setDuplicateStatus(boolean status)
	{
		duplicateName = status;
	}
	
	/**
	 * Retrieve the duplicate status of this model to determine
	 * whether or not. 
	 * @return True for duplicate, false for unique or not logged
	 * in.
	 */
	public boolean getDuplicateStatus()
	{
		return duplicateName;
	}
}
