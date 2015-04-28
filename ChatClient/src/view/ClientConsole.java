package view;

import java.io.*;
import java.util.*;

import model.*;
import network.*;

/**
 * This class deals directly with input and output via the console.
 * <p>
 * Will eventually be modified to cater for GUI.
 * @author Alex
 * @version 0.2
 * @since 0.2
 */
public class ClientConsole implements Runnable{
    
    private ClientModel model;
    private boolean isRunning = true;
    private String option;
    private Map<String, Peer> peerList;
    private String recipientName;
    private String message;
    private String userName;
    private String serverIp;
    private String serverPortNo;
    private String presAddress;
    private String presPort;
    private Scanner scan = new Scanner(System.in);
    
    /**
     * Retrieve user details in order to request a connection to the server.
     * @param userName Client's user name.
     * @param serverIp IP address of the server.
     * @param serverPortNo Port number of the server.
     */
    public void request()
    {   
       	System.out.print("Enter server IP: ");
       	serverIp = scan.nextLine();
       	System.out.print("Enter port number: ");
       	serverPortNo = scan.nextLine();
        System.out.print("Please enter presentation server address: ");
       	presAddress = scan.nextLine();
       	System.out.print("Please enter presentation server port: ");
       	presPort = scan.nextLine();
       	model.setPresDetails(presAddress, presPort);
       	System.out.print("Enter user name: ");
       	userName = scan.nextLine();
       	model.initSession(userName, serverIp, serverPortNo);
    }
    
    /**
     * Prompt the user for username input via console. 
     */
    public void obtainUsername()
    {
       	System.out.print("Enter user name: ");
       	userName = scan.nextLine();
       	model.setUsername(userName);
    }
    
    /**
     * The console's run function. Deals with printing the menu 
     * and receiving input.
     */
    public void run()
    {
        while(isRunning)
        {
        	System.out.println("MENU:");
        	System.out.println("1. Send message.");
        	System.out.println("2. Display peer list.");
        	System.out.println("3. Quit.");
        	System.out.print("What would you like to do?: ");
        	option = scan.nextLine();
            	
        	if(option.equalsIgnoreCase("1"))
        	{
        		peerList = model.getPeerList();
        		System.out.println("PEER LIST:");
        		printPeerList();
        		System.out.print("Who would you like to talk to?: ");
        		recipientName = scan.nextLine();
        		System.out.print("Enter your message: ");
        		message = scan.nextLine();
        		model.sendMessage(recipientName, message);
        	}
        	else if(option.equalsIgnoreCase("2"))
        	{
        		System.out.println("PEER LIST:");
        		printPeerList();
        	}
        	else if(option.equalsIgnoreCase("3"))
        	{
        		isRunning = false;
        		model.deregisterClient();
        		System.out.println("Exiting...");
        		scan.close();
        		System.exit(0);
        	}
        
        }
    }
    
    /**
     * Passes a P2PClientModel object to the console and sets it as the model.
     * @param model The P2PClientModel Object that we wish to set to console.
     */
    public void initModel(ClientModel model)
    {
        this.model = model;
    }
    
    /**
     * Prints an appropriate String to the console when a peer message
     * is received by the system.
     * @param username The username of the sender. 
     * @param time The time that it was received.
     * @param message The actual message contents.
     */
    public void receiveMessage(String username, String time, String message)
    {
        System.out.println("\n" + time + " | " + username + ": " + message);
    }
    
    /**
     * Method to print a customised error String to the console.
     * @param message The error message.
     */
    public void printError(String message)
    {
    	System.out.println("ERROR: " + message);
    }
    
    /**
     * Prints the peer list CURRENTLY stored in the console.
     */
    public void printPeerList()
    {
    	for(Peer peer : peerList.values())
        {
            System.out.println(peer.getUsername());
        }
    }
    
    /**
     * Retrieves the new peer list from the model and prints to console.
     */
    public void newPeerList()
    {
    	System.out.println("\nNEW PEER LIST RECIEVED:");
    	peerList = model.getPeerList();
    	printPeerList();
    }
}

