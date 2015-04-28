package view;

import java.util.*;
import java.text.*;
import network.*;

public class ServerConsole {
	
	private Date date;
	private SimpleDateFormat dateFormat;
	private ServerNetwork network;
	private String presAddress;
	private String presPort;
	private ServerView view;
	
	/**
	 * This class is made specifically to create messages to print to 
	 * console and eventually to trigger events in the view.
	 * <p>
	 * It creates messages for registration and deregistration of 
	 * peers, requests and sending of peer lists.
	 * @author Alex
	 * @version 0.2
	 * @since 0.2
	 */
	public ServerConsole()
	{
		date = new Date();
		dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	}
	
	public void initView(ServerView view)
	{
		this.view = view;
	}
	
	 /**
     * Gets a string representing the current date and time.
     * <p>
     * Used for the other logging methods.
     * @return Date time String.
     */
	private String getCurrentDateTime()
	{
		return dateFormat.format(date);
	}
	
	/**
	 * Print the IP address and port to console.
	 * @param address The IP address of the server.
	 * @param port The port number of the server.
	 */
	public void printConnectionDetails(String address, int port)
	{
		System.out.println("Server Details:");
		System.out.println("IP Address: " + address);
		System.out.println("Port: " + port);
		System.out.println("");
	}
	
	public void initNetwork(ServerNetwork network)
	{
		this.network = network;
	}
	
	public void retrievePresDetails()
	{
		Scanner scan = new Scanner(System.in);
		System.out.print("Please enter presentation server address: ");
		presAddress = scan.nextLine();
		System.out.print("Please enter presentation server port: ");
		presPort = scan.nextLine();
		scan.close();
		network.setPresDetails(presAddress, presPort);
	}
	
	/**
     * Construct a message signifying that a peer has requested to the 
     * server and display it in console.
     * @param username String representing user name.
     * @param address String representing IP address.
     * @param port The port number.
     */
    public void request(String username, String address, int port)
    {
        date = new Date();
        String message;
        
        //Construct string and send to console.
        message = "REQUEST: Peer '" + username + "' @ " + address + ":" 
        + port + " has sent a request.";
        
        printMessage(message);
    }
    
    /**
     * Construct a message signifying that a peer has registered to the
     * server and display it in console.
     * @param username String representing user name.
     * @param address String representing IP address.
     * @param port The port number.
     */
    public void registerPeer(String username, String address, int port)
    {
        date = new Date();
        String message;
        
        //Construct string and send to console.
        message = "CONNECTED: Peer '" + username + "' @ " + address 
        		+ ":" + port + " has been registered.";
        
        printMessage(message);
    }
    
    /**
     * Construct a message signifying that a peer has deregistered 
     * to the server and display it in console.
     * @param username String representing user name.
     * @param address String representing IP address.
     * @param port The port number.
     */
    public void deregisterPeer(String username, String address, int port)
    {
        date = new Date();
        String message;
        
        //Construct string and send to console.
        message = "DISCONNECTED: Peer '" + username + "' @ " + address 
        		+ ":" + port + " has been deregistered.";
        
        printMessage(message);
    }
    
    /**
     * Construct a message signifying that a peer has been sent a peer list 
     * to the server and display it in console.
     * @param username String representing user name.
     * @param address String representing IP address.
     * @param port The port number.
     */
    public void peerList(String username, String address, int port)
    {
        date = new Date();
        String message;
        
        message = "PEERLIST: Peer '" + username + "' @ " + address 
        		+ ":" + port + " has been sent an updated Peer List.";
        
        printMessage(message);
    }
    
    /**
     * Method printing messages to with a timestamp added.
     * @param message String to print.
     */
    public void printMessage(String message)
    {
        System.out.println(getCurrentDateTime() + " | " + message);
        view.logMessage(getCurrentDateTime() + " | " + message);
    }
    
    /**
     * A method to print a customised error message to console.
     * @param message The contents of the message.
     */
    public void printError(String message)
    {
    	System.out.println("ERROR: " + message);
    	view.logMessage("ERROR: " + message);
    }
    
    /**
     * Construct a message signifying that a peer is being checked
     * for life.
     * @param username String representing user name.
     * @param address String representing IP address.
     * @param port The port number.
     */
    public void lifeCheck(String username, String address, int port)
    {
    	date = new Date();
        String message;
        
        message = "LIFE CHECK: Peer '" + username + "' @ " + address 
        		+ ":" + port + " has been checked for life.";
        
        printMessage(message);
    }
	
    /**
     * Construct a message signifying that a peer has responded to the
     * life check.
     * @param username String representing user name.
     * @param address String representing IP address.
     * @param port The port number.
     */
    public void lifeVerified(String username, String address, int port)
    {
        date = new Date();
        String message;
        
        //Construct string and send to console.
        message = "LIFE VERIFIED: Peer '" + username + "' @ " + address 
        		+ ":" + port + " has verified life.";
        
        printMessage(message);
    }
    
    /**
     * Construct a message signifying that a peer has not responded to
     * the life check within the required time.
     * @param username String representing user name.
     * @param address String representing IP address.
     * @param port The port number.
     */
    public void lifeFail(String username, String address, int port)
    {
        date = new Date();
        String message;
        
        //Construct string and send to console.
        message = "LIFE FAILED: Peer '" + username + "' @ " + address 
        		+ ":" + port + " has failed to verify life.";
        
        printMessage(message);
    }
}
