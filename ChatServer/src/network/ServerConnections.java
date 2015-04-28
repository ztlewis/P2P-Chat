package network;

import model.*;
import view.*;

import java.io.IOException;
import java.util.*;

/**
 * This class handles the constant communication to connected to peers to
 * verify that they are alive and haven't been terminated without 
 * deregistering.
 * @author Alex
 * @version 0.2
 * @since 0.2
 */
public class ServerConnections implements Runnable{
	
	private ServerModel model;
	private ServerConsole console;
	private ServerNetwork network;
	private Map<String, Boolean> checkList = new HashMap<String, Boolean>();
	private int lifeCheckTime = 60000; //The time in milliseconds that the server
	//before checking for life again.
	private int replyTime = 30000; //The time in milliseconds that the server allows
	//the peers to reply within to confirm life.
	
	/**
	 * The constructor for the ServerConnections class.
	 */
	public ServerConnections()
	{}
	
	/**
	 * Method to pass a reference to the network to the connections
	 * class.
	 * @param network The ServerNetwork Object that is being passed in.
	 */
	public void initNetwork(ServerNetwork network)
	{
		this.network = network;
	}
	
	/**
	 * Method to pass a reference to the console to the connections 
	 * class.
	 * @param console The ServerConsole Object that is being passed in.
	 */
	public void initConsole(ServerConsole console)
	{
		this.console = console;
	}
	
	/**
	 * Method to pass a reference to the model to the connections 
	 * class.
	 * @param console The ServerModel Object that is being passed in.
	 */
	public void initModel(ServerModel model)
	{
		this.model = model;
	}

	/**
	 * Confirm that a certain user is alive to the latest life check.
	 * @param username The username of the alive client.
	 */
	public void confirmLife(String username)
	{
		checkList.put(username, true);
	}
	
	//This method attempts to check peer life every 30 seconds.
	/**
	 * This run method constantly checks for peer life every 30 seconds.
	 */
	public void run()
	{
		Boolean tempCheck;
		boolean disconnections = false;
		
		//Allow network to print details without being interrupted.
		try
		{
			Thread.sleep(1000);
		}
		catch(InterruptedException iException)
		{
			console.printError("Connection delay interrupted.");
		}
		
		//Check life of every peer every 30 seconds.
		while(true)
		{
			checkList.clear();
		
			//Recreate check list.
			for(Peer peer : model.getPeers())
			{
				checkList.put(peer.getUsername(), false);
			}
			
			console.printMessage("ATTEMPTING LIFE CHECK.");
			
			//If there are no peers then don't bother.
			if(checkList.isEmpty())
			{
				try
				{
					console.printMessage("NO PEERS CONNECTED.");
					Thread.sleep(lifeCheckTime);
				}
				catch(InterruptedException iException)
				{
					console.printError("Check life routine interrupted.");
				}
				continue;
			}
			
			checkLife();
			
			try
			{
				//Sleep for 1 minute.
				console.printMessage("WAITING FOR LIFE VERIFICATIONS.");
				//Give some time for peers to actually send back their 
				//verifications.
				Thread.sleep(replyTime);
				console.printMessage("FINISHED WAITING FOR LIFE VERIFICATIONS");
				
				//Check for any entries that are still listed as false 
				//and remove them.
				Map<String, Peer> fullMap = new HashMap<String, Peer>();
				fullMap.putAll(model.getPeerList());
				for(Peer peer : fullMap.values())
				{
					tempCheck = checkList.get(peer.getUsername());
					//This means that the peer is new and has been added 
					//while asleep.
					if(tempCheck == null)
					{
						continue;
					}
					//Means that the peer is not new and has not responded. 
					//Dead peer.
					else if(tempCheck == false)
					{
						model.removePeer(peer.getUsername());
						console.lifeFail(peer.getUsername(), peer.getAddress(), 
								peer.getPort());
						List<String> contents = new ArrayList<String>();
						contents.add(peer.getUsername());
						network.notifyPresentationServer("PRES_DEATH", contents);
						disconnections = true;
					}
				}
				
				//If we've had any dead peers then we need to redistribute the
				//new peer list without that peer.
				if(disconnections)
				{
					network.distributePeerLists();
				}
				
				Thread.sleep(lifeCheckTime);
			}
			catch(InterruptedException iException)
			{
				console.printError("Check life routine interrupted.");
			}
		}
	}
	
	/**
	 * Method to send a life check packet to every user on the current peer
	 * list.
	 */
	public void checkLife()
	{
		String peerAddress;
		int peerPort;
		List<String> packetData;
		
		for(Peer peer : model.getPeers())
		{
			packetData = new ArrayList<String>();
			peerAddress = peer.getAddress();
			peerPort = peer.getPort();
			network.addData("LIFE_CHECK", packetData, 
					peerAddress, peerPort);
			console.lifeCheck(peer.getUsername(), peerAddress, 
					peerPort);
		}
	}

}
