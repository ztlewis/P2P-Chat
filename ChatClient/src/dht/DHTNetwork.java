package dht;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import static java.nio.file.StandardCopyOption.*;

import network.*;

import java.io.File;
import java.io.IOException;
import java.lang.*;
import java.net.*;


/**
 * The DHTNetwork class is designed to control all aspects of the DHT 
 * implementation. This means maintaining the successor and predecessor 
 * lists, the finger table, etc. 
 * @author Alex
 * @version 0.3
 * @since 0.3
 */
/**
 * @author Alex
 * @version 0.3
 * @since 0.3
 */

public class DHTNetwork implements Runnable{
	
	private String ip;
	private int port;
	private int id;
	private int storeNo= 3;
	private int tabSize = 9;
	private DHTNode[] successorList;
	private DHTNode[] predecessorList;
	private FingerTable fTable;
	private ClientNetwork network;
	private List<String> fileNames;
	private boolean transferring = false;
	private Map<String, File> upDirTable; //Keeps track of all the 
	//directories of files that we send upload requests for.
	private long dhtReplyTime = 30000;
	private long dhtCheckTime = 60000;
	private boolean dhtConfirmed = false;
	private boolean dhtRegistered = false;
	
	/**
	 * Simple constructor for the DHTNetwork class.
	 */
	public DHTNetwork()
	{}
	
	/**
	 * Pass in a reference to a ClientNetwork object.
	 * @param network The ClientNetwork object that we wish
	 * to pass in.
	 */
	public void initNetwork(ClientNetwork network)
	{
		this.network = network;
	}
	/**
	 * This method is designed to initialise an instance of this class for
	 * a particular IP/port configuration. The hash is created whilst
	 * the finger table, lists and other essential data structures 
	 * are generated. This must be called before using the DHTNetwork class
	 * for anything. 
	 * @param ip
	 * @param port
	 * @param firstNode
	 */
	public void initDHT(String ip, int port, boolean firstNode)
	{
		this.ip = ip;
		this.port = port;
		id = hash(ip + port);
		fTable = new FingerTable(id, ip, port, tabSize);
		successorList = new DHTNode[storeNo];
		predecessorList = new DHTNode[storeNo];
		fileNames = new CopyOnWriteArrayList<String>();
		upDirTable = new Hashtable<String, File>();
		new Thread(this).start();
		//If this is the first node in the DHTNetwork, we should send a notice
		//to the presentation server so that it can show us. Normally, there is
		//no data transmission when the first node creates it's own network so 
		//this is the only way the presentation server can know of us. 
		if(firstNode)
		{
			List<String> contents = new ArrayList<String>();
			network.notifyPresentationServer("PRES_DHT_ADD", contents);
		}
	}
	
	/**
	 * Getter for the id value.
	 * @return The id value as an int.
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * Getter for the finger table size value.
	 * @return The table index as an int. 
	 */
	public int getTabSize()
	{
		return tabSize;
	}

	/**
	 * The hash function to generate an id value for a new node. 
	 * <p>
	 * This function allows a string input. This input is hashed with
	 * SHA-1 which is then converted to an integer and taken an appropriate 
	 * modulus of giving us a value between 0 and the max value of 
	 * our chord ring (255 in this case). 
	 * <p>
	 * The way that we are using this function to determine the node id 
	 * is by concatenating the address string and the port int together
	 * and hashing it. 
	 * <p>
	 * Example: hash(127.0.0.14505) for address 127.0.0.1 and port 4505.
	 * @param input The string that we are hashing.
	 * @return The resulting id value.
	 */
	public int hash(String input)
	{
		MessageDigest hash;
		byte[] digestBytes;
		BigInteger hashVal = null;
		int id;
		int idSpace;
		
		try
		{
			//Hash the input string.
			hash = MessageDigest.getInstance("SHA-1");
			hash.reset();
			hash.update(input.getBytes("utf8"));
			digestBytes = hash.digest();
			//Get the numerical value of the digest.
			hashVal = new BigInteger(digestBytes);
		}
		catch(Exception e)
		{}
		
		//Get possible values in id space.
		idSpace = (int)Math.pow(2, tabSize-1);
		//Use mod operator to get something in
		//between 0 and idSpace.
		id = hashVal.intValue()%idSpace;
		id = Math.abs(id);
		return id;
	}
	
	/**
	 * The query function is used to determine the responsible node for a
	 * particular id. 
	 * <p>
	 * This is generally used to either find what node
	 * a request (for a node's new successor or for an upload/download)
	 * should be forwarded to.
	 * <p>
	 * The node first checks if IT is the node responsible (in which case
	 * it returns null), if not, it searches the finger table and returns
	 * the appropriate DHTNode object. 
	 * @param itemId
	 * @return
	 */
	public DHTNode query(int itemId)
	{
		DHTNode searchNode = null;
		//Case where the current node is responsible for this
		//item id.
		if(predecessorList[0] == null)
		{
			return searchNode;
		}
		if((itemId > predecessorList[0].getId()))
		{
			if(itemId <= id)
			{
				return searchNode;
			}
			if(id < predecessorList[0].getId())
			{
				return searchNode;
			}
		}
		if((itemId < predecessorList[0].getId()))
		{
			if((id < predecessorList[0].getId())&&(id >= itemId))
			{
				return searchNode;
			}
		}

		searchNode = fTable.findSuccessor(itemId);
			
		if(searchNode.getId() == id)
		{
			searchNode = null;
		}
		
		return searchNode;
	}
	
	/**
	 * Prints the details of the DHTNetwork into the console.
	 * <p>
	 * Helpful for debugging. 
	 */
	public void printDetails()
	{
		System.out.println("ID: " + id);
		System.out.println("Address: " + ip);
		System.out.println("Port: " + port);
		
		int counter;
		
		for(counter=0; counter<storeNo; counter++)
		{
			if(successorList[counter] != null)
			{
				System.out.println("Successor " + (counter+1) + ": " + 
					successorList[counter].getId());
			}
			else
			{
				System.out.println("Successor " + (counter+1) + ": Not set");
			}
		}
		
		for(counter=0; counter<storeNo; counter++)
		{
			if(predecessorList[counter] != null)
			{
				System.out.println("Predecessor " + (counter+1) + ": " + 
					predecessorList[counter].getId());
			}
			else
			{
				System.out.println("Predecessor " + (counter+1) + ": Not set");
			}
		}
		
		fTable.printTable();
		printFileNames();
	}
	
	/**
	 * Adds a node with this IP and port combination to the DHTNetwork
	 * object.
	 * <p>
	 * This function will not force the new node into any list or finger
	 * table if it does not belong there. It simply checks for places
	 * in the successor list/predecessor list and finger table where it
	 * belongs and places it there. 
	 * @param ip The address of the new node.
	 * @param port The port of the new node.
	 */
	public void addNode(String ip, int port)
	{
		int newId;
		
		newId = hash(ip + port);
		fTable.addNode(newId, ip, port);
		
		int counter;
		
		for(counter=0; counter<storeNo; counter++)
		{
			if(attemptSetSuccessor(ip, port, counter))
			{
				break;
			}
		}
		
		for(counter=0; counter<storeNo ; counter++)
		{
			if(attemptSetPredecessor(ip, port, counter))
			{
				break;
			}
		}
	}
	
	/**
	 * Sends a join request to an existing node.
	 * @param destAddress The address of the existing node.
	 * @param destPort The port of the existing node.
	 */
	public void joinNetwork(String destAddress, int destPort)
	{
		//Send a join request to the provided node. That node will then
		//locate the correct successor. 
		List<String> packetData = new ArrayList<String>();
		
		packetData.add(ip);
		packetData.add(Integer.toString(port));
		network.addData("DHT_JOIN", packetData,
				destAddress, destPort);
	}
	
	/**
	 * This method handles a join request and decides whether it is responsible
	 * for handling the request.
	 * <b>
	 * If not, it forwards the request to the appropriate node. 
	 * @param packetContents The contents of the join request. 
	 */
	public void forwardJoinReq(List<String> packetContents)
	{
		int destId;
		DHTNode fwdNode;
		List<String> packetData = new ArrayList<String>();
		String destAddress;
		int destPort;
		DHTNode orgNode;
		
		//Retrieve new node information from packet.
		orgNode = removeDHTInfo(packetContents);
		destAddress = orgNode.getAddress();
		destPort = orgNode.getPort();
		
		//Get the closest successor in the finger table.
		destId = hash(destAddress + destPort);
		fwdNode = query(destId);
		
		//Forward the request to them if it's not us and if it is us,
		//then reply to the request and tell them what we know.
		//This means it's us.
		if(fwdNode == null)
		{
			//Push all the predecessors back and have this new node as the immediate
			//predecessor.
			if(destId == id)
			{
				return;
			}
			returnDetails(destAddress, destPort);
			addNode(destAddress, destPort);
			System.out.println("Served join request.");
			printDetails();
		}
		//This means it's not us. Reconstruct this packet and send it
		//to the node we think it belongs to.
		else
		{
			packetData.add(destAddress);
			packetData.add(Integer.toString(destPort));
			network.addData("DHT_JOIN", packetData,
					fwdNode.getAddress(), fwdNode.getPort());
		}
	}
	
	/**
	 * Sends an addition packet for this node to its predecessor.
	 * <p>
	 * This starts a ring of notifications through the entire network
	 * until it comes back to this node.
	 */
	public void notifySelfAddition()
	{
		//Communicate your information to your first predecessor.
		//This information will be passed through the entire 
		//Chord ring until it returns to this node. 
		try
		{
			commAddition(predecessorList[0], network.getClientAddress(), network.getClientPort());
		}
		catch(UnknownHostException uhException)
		{
			System.out.println("Could not send addition packet.");
		}
	}
	
	/**
	 * Sends an addition packet to a particular node for a new address
	 * port combination. 
	 * @param node The node that this packet is being sent to.
	 * @param address The address of the new node that is being added.
	 * @param port The port of the new node that is being added. 
	 */
	public void commAddition(DHTNode node, String address, int port)
	{
		List<String> packetData = new ArrayList<String>();
		String destAddress;
		int destPort;
	
		if(node == null)
		{
			return;
		}
			
		destAddress = node.getAddress();
		destPort = node.getPort();
			
		packetData.add(address);
		packetData.add(Integer.toString(port));
			
		network.addData("DHT_ADD", packetData,
					destAddress, destPort);	
	}
	
	/**
	 * This method handles an addition packet and forwards it if necessary.
	 * <p>
	 * If a node receives an addition packet of itself, then it stops.
	 * This means that the addition packet has been forwarded through
	 * the entire chord ring and thus, does not need to be propogated further.
	 * @param packetContents The contents of the addition packet.
	 */
	public void forwardAddition(List<String> packetContents)
	{
		String newAddress;
		int newPort;
		DHTNode newNode;
		
		newNode = removeDHTInfo(packetContents);
		
		if(newNode != null)
		{
			//Stop forwarding if it has come back all the way to you.
			if(newNode.getId() == id)
			{
				return;
			}
			newAddress = newNode.getAddress();
			newPort = newNode.getPort();
			//Add the node for ourselves.
			addNode(newAddress, newPort);
		
			//Forward the addition notice backwards in the chord circle.
			commAddition(predecessorList[0], newAddress, newPort);
		}
	}
	
	//This method deals with the compilation of all the successor, predecessor
	//and finger table information when a new predecessor is added
	//to the network.
	/**
	 * Compiles all information required by a new node (pred) for startup.
	 * <p>
	 * Creates a packet named "DHT_SETUP" which includes 2 successors and
	 * 3 predecessors (including itself which is already one more successor)
	 * which will fill the appropriate lists.
	 * <p>
	 * The contents of the finger table is also sent.
	 * @param destAddress The address of the new node.
	 * @param destPort The port of the new node.
	 */
	public void returnDetails(String destAddress, int destPort)
	{
		List<String> packetData = new ArrayList<String>();
		List<DHTNode> list;
		
		list = fTable.getNodes();
		
		//Add all of the unique nodes in the finger table. We'll know
		//when finger table information begins because there's
		//always going to be ONLY 5 entries before this.
		while(list.size() > 0)
		{
			addDHTInfo(list.remove(0), packetData);
		}
		//The last successor doesn't need to be sent because
		//this node will be one of them.
		
		int counter;
		
		//Add the successor information to the packet data.
		for(counter=(storeNo-1); counter>=0; counter--)
		{
			addDHTInfo(successorList[counter], packetData);
		}
		
		//Add all the predecessor information to the packet data.
		for(counter=(storeNo-1); counter>=0; counter--)
		{
			addDHTInfo(predecessorList[counter], packetData);
		}
		
		network.addData("DHT_SETUP", packetData,
				destAddress, destPort);
	}
	
	/**
	 * Used to receive and process a startup packet.
	 * <p>
	 * All of the information in the "DHT_SETUP" packet is removed
	 * and converted into DHTNode structures which are then
	 * added to the DHTNetwork information as needed. 
	 * @param packetData The contents of the setup packet.
	 * @param senderAddress The address of the sender.
	 * @param senderPort The port of the sender. 
	 */
	public void receiveDetails(List<String> packetData, String senderAddress, int senderPort)
	{
		int senderId;
		
		//Make the first successor from the address and port that the packet
		//originated from.
		senderId = hash(senderAddress + senderPort);
		
		//Proceed to recreate the nodes on this side.
		System.out.println("Adding predecessors.");
		int counter;
		
		for(counter=0; counter<storeNo; counter++)
		{
			predecessorList[counter] = removeDHTInfo(packetData);
		}
		System.out.println("Adding successors.");
		successorList[0] = new DHTNode(senderId, senderAddress, senderPort);
		for(counter=1; counter<storeNo; counter++)
		{
			successorList[counter] = removeDHTInfo(packetData);
		}
		//Of course our immediate successor needs to be added also.
		addNode(senderAddress, senderPort);
		
		System.out.println("Adding finger table successors.");
		while(packetData.size() > 0)
		{
			//After this point, it's only finger table information.
			fTable.addNode(removeDHTInfo(packetData));
		}
		notifySelfAddition();
		dhtRegistered = true;
		network.connectionEstablished();
	}
	
	/**
	 * Check whether the node is correctly registered.
	 * <p>
	 * If the node is starting it's own ring, it is automatically registered
	 * however, if it is joining a ring, it is only registered once it
	 * has received a "DHT_SETUP" packet that confirms its existence. 
	 * @return The boolean value. True for registered and false for unreg.
	 */
	public boolean isDhtRegistered()
	{
		return dhtRegistered;
	}
	
	/**
	 * Modifies the registration status of the DHTNetwork class.
	 * @param status The new reg status. 
	 */
	public void setDhtRegistered(boolean status)
	{
		dhtRegistered = status;
	}
	
	//Adds node address and port to packet data to prepare for transmission.
	/**
	 * Adds a node's address and port to existing packet data.
	 * @param tempNode The node whose information we want to add to the 
	 * string list. 
	 * @param packetData The packet data that we want to add the 
	 * information to. 
	 */
	public void addDHTInfo(DHTNode tempNode, List<String> packetData)
	{
		if(tempNode == null)
		{
			packetData.add("null");
			packetData.add("null");
		}
		else
		{
			packetData.add(tempNode.getAddress());
			packetData.add(Integer.toString(tempNode.getPort()));
		}
	}
	
	//Removes node address and port from packet data when received.
	/**
	 * Takes a node's address and port from existing packet data
	 * and returns a new DHTNode object with this information.
	 * @param packetData The packet data containing this info.
	 * @return The new DHT node.
	 */
	public DHTNode removeDHTInfo(List<String> packetData)
	{
		DHTNode returnNode;
		int returnId;
		String returnAddress;
		int returnPort;
		String strPort;
		
		//Remove the two last fields for port and address.
		strPort = packetData.remove(packetData.size()-1);
		
		if(strPort.equalsIgnoreCase("null"))
		{
			packetData.remove(packetData.size()-1);
			return null;
		}
		
		returnPort = Integer.parseInt(strPort);
		returnAddress = packetData.remove(packetData.size()-1);
		returnId = hash(returnAddress + returnPort);
		
		//Turn into new node and return that object.
		returnNode = new DHTNode(returnId, returnAddress, returnPort); 
		return returnNode;
	}
	
	//Method to set one of the successors to a new node.
	/**
	 * Attempt to set the successor of a given index. 
	 * <p>
	 * Returns false if the successor was not set for this index.
	 * Returns true if the successor either WAS set for this index
	 * or some condition exists that it should not be added for 
	 * any other index and any subsequent set attempts should 
	 * stop (the node already exists in a list, etc). 
	 * @param newIp Address of the new node.
	 * @param newPort Port of the new node. 
	 * @param index Index of the successor spot that we want to attempt
	 * to set for. 
	 * @return The set status. 
	 */
	public boolean attemptSetSuccessor(String newIp, int newPort, int index)
	{
		int newId;
		DHTNode tempNode;
		
		newId = hash(newIp + newPort);
		tempNode = new DHTNode(newId, newIp, newPort);
		
		if(newId == id)
		{
			return true;
		}
		
		if(successorList[index] == null)
		{
			setSuccessor(tempNode, index);
			return true;
		}
		else if(newId == successorList[index].getId())
		{
			return true;
		}
		else if(newId > id)
		{
			if(successorList[index].getId() < id)
			{
				setSuccessor(tempNode, index);
				return true;
			}
			if(successorList[index].getId() > newId)
			{
				setSuccessor(tempNode, index);
				return true;
			}
		}
		else
		{
			if(successorList[index].getId() > id)
			{
				return false;
			}
			if(successorList[index].getId() > newId)
			{
				setSuccessor(tempNode, index);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sets the successor of a given index to the input DHTNode.
	 * <p>
	 * The entire list will be pushed back in this case. 
	 * @param newNode The new successor.
	 * @param index The index of the new successor. 
	 */
	public void setSuccessor(DHTNode newNode, int index)
	{
		int counter;
		
		//Shift back every successor after the one we're 
		//inserting.
		for(counter=(storeNo-1); counter>index; counter--)
		{
			successorList[counter] = successorList[counter-1];
		}
		
		successorList[index] = newNode;
	}
	
	/**
	 * Attempt to set the predecessor of a given index. 
	 * <p>
	 * Returns false if the predecessor was not set for this index.
	 * Returns true if the predecessor either WAS set for this index
	 * or some condition exists that it should not be added for 
	 * any other index and any subsequent set attempts should 
	 * stop (the node already exists in a list, etc). 
	 * @param newIp Address of the new node.
	 * @param newPort Port of the new node. 
	 * @param index Index of the predecessor spot that we want to attempt
	 * to set for. 
	 * @return The set status. 
	 */
	//Method to set one of the predecessors to a new node.
	public boolean attemptSetPredecessor(String newIp, int newPort, int index)
	{
		int newId;
		DHTNode tempNode;
		
		newId = hash(newIp + newPort);
		tempNode = new DHTNode(newId, newIp, newPort);
		
		if(newId == id)
		{
			return true;
		}
		
		if(predecessorList[index] == null)
		{
			setPredecessor(tempNode, index);
			return true;
		}
		else if(newId == predecessorList[index].getId())
		{
			return true;
		}
		else if(newId > id)
		{
			if(predecessorList[index].getId() < id)
			{
				return false;
			}
			if(predecessorList[index].getId() < newId)
			{
				setPredecessor(tempNode, index);
				return true;
			}
		}
		else
		{
			if(predecessorList[index].getId() > id)
			{
				setPredecessor(tempNode, index);
				return true;
			}
			if(predecessorList[index].getId() < newId)
			{
				setPredecessor(tempNode, index);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sets the predecessor of a given index to the input DHTNode.
	 * <p>
	 * The entire list will be pushed back in this case. 
	 * @param newNode The new predecessor.
	 * @param index The index of the new predecessor. 
	 */
	public void setPredecessor(DHTNode newNode, int index)
	{
		int counter;
		
		//Shift back every successor after the one we're 
		//inserting.
		for(counter=(storeNo-1); counter>index; counter--)
		{
			predecessorList[counter] = predecessorList[counter-1];
		}
		
		predecessorList[index] = newNode;
		
		if(index == 0)
		{
			checkFiles();
		}
	}
	
	/**
	 * Method called when a file is uploaded from a node. Checks that the
	 * current node is not responsible for the upload.
	 * <p>
	 * If it is, the file is transferred the "src" folder and the presentation
	 * server is notified accordingly. 
	 * <p>
	 * If not, the forward function runs and an appropriate node is chosen 
	 * to forward to. 
	 * @param fileName Name of the file to be uploaded. 
	 * @param reqAddress Address of the requesting node.
	 * @param reqPort Port of the requesting node. 
	 * @param selectedUpload The file (including the filepath) of the file. 
	 */
	public void uploadCheck(String fileName, String reqAddress, int reqPort, 
			File selectedUpload)
	{
		DHTNode checkNode;
		int fileId;
		int filePort;
		
		fileId = hash(fileName);
		checkNode = query(fileId);
		
		if(checkNode == null)
		{
			List<String> packetData = new ArrayList<String>();
			packetData.add(fileName);
			System.out.println("Received upload req for ID " + fileId);
			network.setLastUploadId(id);
			transferFile(selectedUpload);
			network.notifyPresentationServer("PRES_FILE", packetData);
			return;
		}
		
		filePort = sendFile(selectedUpload);
		forwardFileUpRequest(fileName, reqAddress, reqPort, filePort);
	}
	
	/**
	 * Handles an upload request. 
	 * <p>
	 * Firstly, determines whether or not this node is responsible for the
	 * upload. If so, the file is added to the file list.
	 * <p>
	 * Otherwise, the packet is reconstructed and forwarded on.
	 * @param fileName Name of the file.
	 * @param reqAddress Address of the requesting node.
	 * @param reqPort Port of the requesting node. 
	 * @param filePort Port that the requesting node has opened for transfer. 
	 * @return True if this node is responsible for the upload. Oterwise,
	 * false. 
	 */
	public boolean forwardFileUpRequest(String fileName, String reqAddress, 
			int reqPort, int filePort)
	{
		DHTNode fwdNode;
		List<String> packetData = new ArrayList<String>();
		int fileId;
		
		fileId = hash(fileName);
		
		fwdNode = query(fileId);
		
		//Forward the request to them if it's not us and if it is us,
		//then reply to the request and tell them what we know.
		//This means it's us.
		if(fwdNode == null)
		{
			System.out.println("Received upload req for ID " + fileId);
			addFileName(fileName);
			return true;
		}
		//This means it's not us. Reconstruct this packet and send it
		//to the node we think it belongs to.
		else
		{
			packetData.add(fileName);
			packetData.add(reqAddress);
			packetData.add(Integer.toString(reqPort));
			packetData.add(Integer.toString(filePort));
			network.addData("DHT_UP", packetData,
					fwdNode.getAddress(), fwdNode.getPort());
			network.setLastUploadId(fwdNode.getId());
			return false;
		}
	}
	
	/**
	 * Return a packet to the upload requesting node to notify them that this 
	 * node is the one responsible for their upload. 
	 * @param fileName Name of the file. 
	 * @param reqAddress Requesting node address.
	 * @param reqPort Requesting node port. 
	 * @param filePort The port that the requesting node opened for file 
	 * transmission. 
	 */
	public void returnFileUpConfirmation(String fileName, String reqAddress, 
			int reqPort, int filePort)
	{
		List<String> packetData = new ArrayList<String>();
		
		packetData.add(fileName);
		network.addData("DHT_UP_CONFIRM", packetData,
				reqAddress, reqPort);
		receiveFile(fileName, reqAddress, filePort);
	}
	
	/**
	 * Method to be called when an upload request is received by this node. 
	 * <p>
	 * Attempts forward the request. If it is responsible for the upload, 
	 * it will return an upload confirmation to the requesting node. 
	 * @param packetContents The contents of the upload request packet. 
	 */
	public void receiveUpRequest(List<String> packetContents)
	{
		String fileName;
		String reqAddress;
		int reqPort;
		int filePort;
		
		fileName = packetContents.remove(0);
		reqAddress = packetContents.remove(0);
		reqPort = Integer.parseInt(packetContents.remove(0));
		filePort = Integer.parseInt(packetContents.remove(0));
		
		if(forwardFileUpRequest(fileName, reqAddress, reqPort, filePort))
		{
			returnFileUpConfirmation(fileName, reqAddress, reqPort, filePort);
		}
	}
	
	/**
	 * Begin a new thread to receive a file. 
	 * @param fileName Name of the new file. 
	 * @param address Address of the sending node. 
	 * @param port Port of the sending node. 
	 */
	public void receiveFile(String fileName, String address, int port)
	{
		Runnable r = new FileReceiver(fileName, address, port, 
				this, null, null);
		new Thread(r).start();
	}
	
	/**
	 * Begin a new thread to send a file. 
	 * @param newFile File object corresponding to the file we want to send.
	 * @return The port that we are sending the file from. 
	 */
	public int sendFile(File newFile)
	{
		FileSender r = new FileSender(newFile, this, null, null);
		int portNo = r.getPort();
		new Thread(r).start();
		return portNo;
	}
	
	/**
	 * Handles a download request. 
	 * <p>
	 * Firstly, determines whether or not this node is responsible for the
	 * download.
	 * <p>
	 * Otherwise, the packet is reconstructed and forwarded on.
	 * @param fileName Name of the file.
	 * @param reqAddress Address of the requesting node.
	 * @param reqPort Port of the requesting node. 
	 * @param initRequest Whether or not the request originated from this
	 * node.  
	 * @return True if this node is responsible for the download. Otherwise,
	 * false. 
	 */
	public boolean forwardFileDownRequest(String fileName, String reqAddress, int reqPort, boolean initRequest)
	{
		DHTNode fwdNode;
		List<String> packetData = new ArrayList<String>();
		int fileId;
		
		fileId = hash(fileName);
		
		fwdNode = query(fileId);
		
		//Forward the request to them if it's not us and if it is us,
		//then reply to the request and tell them what we know.
		//This means it's us.
		if(fwdNode == null)
		{
			System.out.println("Received download req for ID " + fileId);
			if(initRequest)
			{
				network.setLastDownloadId(id);
			}
			return true;
		}
		//This means it's not us. Reconstruct this packet and send it
		//to the node we think it belongs to.
		else
		{
			packetData.add(fileName);
			packetData.add(reqAddress);
			packetData.add(Integer.toString(reqPort));
			network.addData("DHT_DOWN", packetData,
					fwdNode.getAddress(), fwdNode.getPort());
			if(initRequest)
			{
				network.setLastDownloadId(fwdNode.getId());
			}
			return false;
		}
	}
	
	/**
	 * Return a packet to the download requesting node to notify them that 
	 * this node is the one responsible for their download. 
	 * @param fileName The name of the file. 
	 * @param reqAddress The requesting node's address. 
	 * @param reqPort The requesting node's port. 
	 */
	public void returnFileDownConfirmation(String fileName, String reqAddress, int reqPort)
	{
		List<String> packetData = new ArrayList<String>();
		int filePort;
		File targetFile;
		
		targetFile = new File(fileName);
		
		filePort = sendFile(targetFile);
		packetData.add(fileName);
		packetData.add(Integer.toString(filePort));
		network.addData("DHT_DOWN_CONFIRM", packetData,
				reqAddress, reqPort);
	}
	
	/**
	 * Send a transfer notification to a particular node to force a transfer
	 * to happen immediately in the case that they are now the rightful owners
	 * of a certain file. 
	 * @param fileName The name of the file. 
	 * @param reqAddress The receiving node's address. 
	 * @param reqPort The receiving node's port. 
	 */
	public void returnFileTransfer(String fileName, String reqAddress,
			int reqPort)
	{
		List<String> packetData = new ArrayList<String>();
		int filePort;
		File targetFile;
		
		targetFile = new File(fileName);
		filePort = sendFile(targetFile);
		packetData.add(fileName);
		packetData.add(Integer.toString(filePort));
		network.addData("DHT_TRANSFER", packetData,
				reqAddress, reqPort);
	}
	
	/**
	 * Method to be called when an download request is received by this node. 
	 * <p>
	 * Attempts forward the request. If it is responsible for the download, 
	 * it will return a download confirmation to the requesting node. 
	 * @param packetContents The contents of the upload request packet. 
	 */
	public void receiveDownRequest(List<String> packetContents)
	{
		String fileName;
		String reqAddress;
		int reqPort;
		
		fileName = packetContents.remove(0);
		reqAddress = packetContents.remove(0);
		reqPort = Integer.parseInt(packetContents.remove(0));
		
		if(forwardFileDownRequest(fileName, reqAddress, reqPort, false))
		{
			returnFileDownConfirmation(fileName, reqAddress, reqPort);
		}
	}
	
	/**
	 * Called when a node wants to remove itself from the ring. 
	 * <p>
	 * It sends a notification of removal to its predecessor and
	 * includes all of it's immediate predecessors and successors to allow
	 * all of the nodes in the ring to fill in the gaps that this node 
	 * has created by leaving. 
	 * <p>
	 * It also sends a number of transfer requests out to the successor so that
	 * none of the files that the node is currently holding are lost. 
	 * <p>
	 * It then loops forever until those files finish transferring. 
	 */
	public void notifySelfRemoval()
	{
		int fileNo;
		transferring = true;
		List<String> dupeList = new ArrayList<String>();
		List<DHTNode> predecessors = new ArrayList<DHTNode>();
		List<DHTNode> successors = new ArrayList<DHTNode>();
		if(successorList[0] != null)
		{
			int counter;
			for(counter=0; counter<storeNo; counter++)
			{
				predecessors.add(predecessorList[counter]);
				successors.add(successorList[counter]);
			}
			try
			{
				commRemoval(predecessorList[0], network.getClientAddress(), 
						network.getClientPort(), predecessors, successors);
			}
			catch(UnknownHostException uhException)
			{
				System.out.println("Could not send removal packet.");
			}
		}
		
		int counter;
		
		//Send a file transfer requests for each file.
		if(predecessorList[0] != null)
		{
			fileNo = fileNames.size();
			dupeList.addAll(fileNames);
			for(counter=0;counter<fileNo;counter++)
			{
				returnFileTransfer(dupeList.get(counter),
						successorList[0].getAddress(), 
						successorList[0].getPort());
			}
			
			
			//DON'T close until we've finished shifting all our stuff.
			while(fileNames.size() > 0)
			{
			}
			transferring = false;
		}
	}
	
	//Sends an addition notice to a node for the input address
	//and port combination.
	/**
	 * Sends a removal packet of a particular node to a given address/port
	 * combination. 
	 * @param node The removed node. 
	 * @param address The address of the receiver. 
	 * @param port The port of the receiver. 
	 * @param predecessors The DHTNode predecessors of the removed node.
	 * @param successors The DHTNode successors of the removed node. 
	 */
	public void commRemoval(DHTNode node, String address, int port, 
			List<DHTNode> predecessors,
			List<DHTNode> successors)
	{
		List<String> packetData = new ArrayList<String>();
		String destAddress;
		int destPort;
			
		destAddress = node.getAddress();
		destPort = node.getPort();
			
		packetData.add(address);
		packetData.add(Integer.toString(port));
		
		int counter;
		for(counter=0; counter<storeNo; counter++)
		{
			addDHTInfo(predecessors.get(counter), packetData);
		}
		
		for(counter=0; counter<storeNo; counter++)
		{
			addDHTInfo(successors.get(counter), packetData);
		}
			
		network.addData("DHT_REMOVAL", packetData,
					destAddress, destPort);	
	}
	
	/**
	 * Handles a removal packet.
	 * <p>
	 * Firstly, the removed node is cleared from the finger table and the 
	 * lists. 
	 * <p>
	 * Then, provided that the removal packet has not been received by the 
	 * successor of the removed node (the other side of the circle), the 
	 * removal notice is forwarded. 
	 * @param packetContents The packet contents of the node removal. 
	 */
	public void forwardRemoval(List<String> packetContents)
	{
		String delAddress;
		int delPort;
		DHTNode delNode;
		List<DHTNode> predecessors = new ArrayList<DHTNode>();
		List<DHTNode> successors = new ArrayList<DHTNode>();
		
		int counter;
		for(counter=0; counter<storeNo; counter++)
		{
			successors.add(removeDHTInfo(packetContents));
		}
		for(counter=0; counter<storeNo; counter++)
		{
			predecessors.add(removeDHTInfo(packetContents));
		}
		delNode = removeDHTInfo(packetContents);

		delAddress = delNode.getAddress();
		delPort = delNode.getPort();

		//Forward the addition notice backwards in the chord circle.
		//Only if we've reached the other side of the chord circle
		//can we stop.
		if(predecessorList[0].getId() != delNode.getId())
		{
			commRemoval(predecessorList[0], delAddress, delPort, predecessors,
					successors);
		}
		
		//Clear the node.
		clearNode(delNode);
		printDetails();
		//Add all of the nodes in case one of them will patch the hole the
		//removed node left.
		DHTNode tempNode;
		for(counter=0; counter<storeNo; counter++)
		{
			tempNode = successors.get(counter);
			if(tempNode != null)
			{
				addNode(tempNode.getAddress(), tempNode.getPort());
			}
			tempNode = predecessors.get(counter);
			if(tempNode != null)
			{
				addNode(tempNode.getAddress(), tempNode.getPort());
			}
		}
		printDetails();
	}
	
	/**
	 * Clears a node out of the DHTNetwork predecessor list, successor
	 * list and finger table. 
	 * @param oldNode Node that is being removed. 
	 */
	public void clearNode(DHTNode oldNode)
	{
		//Firstly replace the deleted node with ourself (the least optimal
		//node before trying to add the rest). Typically, the successor 0
		//should take it's place but we don't need to dictate that in code.
		//Our add function should take appropriate action.
		fTable.replaceNode(oldNode, new DHTNode(id, ip, port));
		
		int counter;
		for(counter=0; counter<storeNo; counter++)
		{
			attemptRemovePredecessor(oldNode, counter);
			attemptRemoveSuccessor(oldNode, counter);
		}
	}
	
	/**
	 * Removes a predecessor at a given index if its ID is the same as the 
	 * input node. 
	 * @param oldNode Node that is being removed. 
	 * @param index Index that is being attempted. 
	 */
	public void attemptRemovePredecessor(DHTNode oldNode, int index)
	{
		int counter;
		
		if(predecessorList[index] == null)
		{
			return;
		}
		
		if(oldNode.getId() != predecessorList[index].getId())
		{
			return;
		}
		
		//Push everything forwards.
		for(counter=index; counter<(storeNo-1); counter++)
		{
			predecessorList[counter] = predecessorList[counter+1];
		}
		
		predecessorList[storeNo-1] = null;
	}
	
	/**
	 * Removes a successor at a given index if its ID is the same as the input
	 * node. 
	 * @param oldNode Node that is being removed. 
	 * @param index Index that is being attempted. 
	 */
	public void attemptRemoveSuccessor(DHTNode oldNode, int index)
	{
		int counter;
		
		if(successorList[index] == null)
		{
			return;
		}
		
		if(oldNode.getId() != successorList[index].getId())
		{
			return;
		}
		
		//Shift back every successor after the one we're 
		//inserting.
		for(counter=index; counter<(storeNo-1); counter++)
		{
			successorList[counter] = successorList[counter+1];
		}
		
		successorList[storeNo-1] = null;
	}
	
	/**
	 * Adds a file name to the list of files and refreshes the DHT GUI
	 * to reflect these changes. 
	 * @param newName The new file to be added. 
	 */
	public void addFileName(String newName)
	{
		fileNames.add(newName);
		network.refreshDHTGUI();
	}
	
	/**
	 * Removes a file name from the list of files and refreshes the DHT GUI
	 * to reflect these changes. 
	 * @param newName The file to be removed. 
	 */
	public void removeFileName(String newName)
	{
		int counter;
		
		for(counter=0; counter<fileNames.size(); counter++)
		{
			if(fileNames.get(counter).equalsIgnoreCase(newName))
			{
				fileNames.remove(counter);
				network.refreshDHTGUI();
				return;
			}
		}
	}
	
	/**
	 * Prints the file list to console. 
	 * <p>
	 * Helpful for debugging purposes. 
	 */
	public void printFileNames()
	{
		int counter;
		int fileId;
		
		for(counter=0; counter<fileNames.size(); counter++)
		{
			fileId = hash(fileNames.get(counter));
			System.out.println(fileNames.get(counter) + " (" + fileId + ")");
		}
	}
	
	/**
	 * Check that this node is still responsible for all of the files that it
	 * owns. 
	 * <p>
	 * This should be called when it's possible that a new predecessor may 
	 * have an ID that becomes responsible for some of this node's files. 
	 * <p>
	 * If this is the case, this node will trigger a transfer to the
	 * predecessor. 
	 */
	public void checkFiles()
	{
		String predAddress;
		int predPort;
		int fileId;
		int counter;
		int fileNo;
		List<String> dupeList = new ArrayList<String>();
		
		predAddress = predecessorList[0].getAddress();
		predPort = predecessorList[0].getPort();
		fileNo = fileNames.size();
		dupeList.addAll(fileNames);
		
		for(counter=0;counter<fileNo;counter++)
		{
			fileId = hash(dupeList.get(counter));
			if(query(fileId) != null)
			{
				returnFileTransfer(dupeList.get(counter),
						predAddress, predPort);
				removeFileName(dupeList.get(counter));
			}
		}
	}
	
	/**
	 * Checks whether this node is currently transferring files. 
	 * @return Transfer status. 
	 */
	public boolean isTransferring()
	{
		return transferring;
	}
	
	/**
	 * Add an upload entry (name file pair). 
	 * <p>
	 * This is so the node can remember where the file is when it receives
	 * its upload confirmation. 
	 * @param fileName The name of the file. 
	 * @param newFile The file object (including path). 
	 */
	public void addUpEntry(String fileName, File newFile)
	{
		upDirTable.put(fileName, newFile);
	}
	
	//Method is used to send check packets to the predecessor to make sure
	//it's alive.
	public void run()
	{
		String predAddress;
		int predPort;
		List<String> packetData;
		DHTNode delNode;
		List<DHTNode> successors = new ArrayList<DHTNode>();
		List<DHTNode> predecessors = new ArrayList<DHTNode>();
		
		while(true)
		{
			dhtConfirmed = false;
			System.out.println("Loop begins...");
			if(predecessorList[0] != null)
			{
				predAddress = predecessorList[0].getAddress();
				predPort = predecessorList[0].getPort();
				
				System.out.println("Checking predecessor.");
				packetData = new ArrayList<String>();
				network.addData("DHT_PRED_CHECK", packetData, 
						predAddress, predPort);
				try
				{
					Thread.sleep(dhtReplyTime);
				}
				catch(InterruptedException iException)
				{
					System.out.println("DHT predecessor check interrupted.");
					continue;
				}
				
				if(dhtConfirmed == false)
				{
					System.out.println("Predecessor failed to respond to life check.");
					delNode = new DHTNode(predecessorList[0].getId(),
							predecessorList[0].getAddress(),
							predecessorList[0].getPort());
					
					if(successorList[0]!= null)
					{
						if(successorList[0].getId() == delNode.getId())
						{
							int counter;
							for(counter=0; counter<storeNo; counter++)
							{
								if(counter != 0)
								{
									predecessors.add(predecessorList[counter]);
								}
								successors.add(successorList[counter]);
							}
							commDeath(successorList[0], delNode.getAddress(),
									delNode.getPort(), predecessors, successors);
						}
					}
					clearNode(delNode);
					printDetails();
				}
			}
			try
			{
				Thread.sleep(dhtCheckTime);
			}
			catch(InterruptedException iException)
			{
				System.out.println("DHT predecessor check interrupted.");
			}
		}
	}
	
	/**
	 * Checks whether this node has been confirmed.
	 * @param dhtConfirmed The DHT status. 
	 */
	public void setDHTConfirmation(boolean dhtConfirmed)
	{
		this.dhtConfirmed = dhtConfirmed;
	}
	
	/**
	 * Sends a death notification of a particular address/port 
	 * combination to a particular node. 
	 * <p>
	 * Also forwards predecessor and successor list information
	 * that fills the gaps in the other node lists that this
	 * dead node would have created. 
	 * @param node Node that this notification is being sent to. 
	 * @param address The address of the dead node. 
	 * @param port The port of the dead node. 
	 * @param pred0 Predecessor 0. 
	 * @param pred1 Predecessor 1. 
	 * @param pred2 Predecessor 2. 
	 * @param succ0 Successor 0.
	 * @param succ1 Successor 1. 
	 * @param succ2 Successor 2. 
	 */
	public void commDeath(DHTNode node, String address, int port, 
			List<DHTNode> predecessors, List<DHTNode> successors)
	{
		List<String> packetData = new ArrayList<String>();
		String destAddress;
		int destPort;
			
		destAddress = node.getAddress();
		destPort = node.getPort();
			
		packetData.add(address);
		packetData.add(Integer.toString(port));
		int counter;
		for(counter=0; counter<storeNo; counter++)
		{
			addDHTInfo(predecessors.get(counter), packetData);
		}
		
		for(counter=0; counter<storeNo; counter++)
		{
			addDHTInfo(successors.get(counter), packetData);
		}
			
		network.addData("DHT_DEATH", packetData,
					destAddress, destPort);	
	}
	
	/**
	 * Handles a death notification.
	 * <p>
	 * The dead node is removed from all of the lists and the finger table of
	 * this DHTNetwork class. Additionally, a check will be made to see whether
	 * or not this notification has made its way all the way to the other side
	 * of the chord circle. If not, it is forwarded on. 
	 * @param packetContents
	 */
	public void forwardDeath(List<String> packetContents)
	{
		String delAddress;
		int delPort;
		List<DHTNode> successors = new ArrayList<DHTNode>();
		List<DHTNode> predecessors = new ArrayList<DHTNode>();
		DHTNode delNode;
		
		int counter;
		for(counter=0; counter<storeNo; counter++)
		{
			successors.add(removeDHTInfo(packetContents));
		}
		for(counter=0; counter<storeNo; counter++)
		{
			predecessors.add(removeDHTInfo(packetContents));
		}
		
		delNode = removeDHTInfo(packetContents);

		delAddress = delNode.getAddress();
		delPort = delNode.getPort();

		//Forward the addition notice backwards in the chord circle.
		//Only if we've reached the other side of the chord circle
		//can we stop.
		if(successorList[0].getId() != delNode.getId())
		{
			commDeath(successorList[0], delAddress, delPort, predecessors,
					successors);
		}
		else
		{
			sendFix(successorList[1], predecessorList[storeNo-1]);
		}
		
		//Clear the node.
		clearNode(delNode);
		printDetails();
		//Add all of the nodes in case one of them will patch the hole the
		//removed node left.
		DHTNode tempNode;
		for(counter=0; counter<storeNo; counter++)
		{
			tempNode = successors.get(counter);
			addNode(tempNode.getAddress(), tempNode.getPort());
			tempNode = predecessors.get(counter);
			addNode(tempNode.getAddress(), tempNode.getPort());
		}
		printDetails();
	}
	
	//When a recipient node needs to know about a new 
	//successor/predecessor to fill it's lists or finger table,
	//we use this.
	/**
	 * Method used to send a fix packet to another node. This just tells a node
	 * to add another particular node to its lists and finger table. 
	 * @param recipient Receiving node. 
	 * @param addition Node that is being added in this fix. 
	 */
	public void sendFix(DHTNode recipient, DHTNode addition)
	{
		List<String> packetData = new ArrayList<String>();
		
		addDHTInfo(addition, packetData);
		network.addData("DHT_FIX", packetData,
				recipient.getAddress(), recipient.getPort());	
	}
	
	/**
	 * Check that this file name exists in the file list. 
	 * @param fileName Name of the file. 
	 * @return Exist status. 
	 */
	public boolean fileNameExists(String fileName)
	{
		int counter;
		
		for(counter=0;counter<fileNames.size();counter++)
		{
			if(fileName.equalsIgnoreCase(fileNames.get(counter)))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns a two dimensional array of strings with the finger table 
	 * information. 
	 * @return Table info. 
	 */
	public String[][] getTableInfo()
	{
		return fTable.getTableInfo();
	}
	
	/**
	 * Returns a two dimensional array of strings with the pred/succ
	 * list information. 
	 * @return List info. 
	 */
	public String[][] getListInfo()
	{
		String[][] stringList = new String[storeNo*2][3];
		
		int counter;
		int index = 0;
		
		for(counter=0; counter<storeNo; counter++)
		{
			addListInfo(stringList, predecessorList[counter], index);
			index++;
		}
		for(counter=0; counter<storeNo; counter++)
		{
			addListInfo(stringList, successorList[counter], index);
			index++;
		}
		
		return stringList;
	}
	
	/**
	 * Add a particular DHTNode's information to a two dimensional array. 
	 * @param stringList The two dimensional array that this information
	 * is going to be stored in. 
	 * @param addNode The DHTNode that is being added to the array. 
	 * @param index The index to which we are adding. 
	 */
	public void addListInfo(String[][] stringList, DHTNode addNode, int index)
	{
		if(addNode != null)
		{
			stringList[index][0] = Integer.toString(addNode.getId());
			stringList[index][1] = addNode.getAddress();
			stringList[index][2] = Integer.toString(addNode.getPort());
		}
		else
		{
			stringList[index][0] = "Not set";
			stringList[index][1] = "Not set";
			stringList[index][2] = "Not set";
		}
	}
	
	/**
	 * Returns a two dimensional array of strings containing filenames
	 * and their respective IDs. 
	 * @return File info. 
	 */
	public String[][] getFileNames()
	{
		String[][] fileList;
		fileList = new String[2][fileNames.size()];
		int counter;
		
		for(counter=0;counter<fileNames.size();counter++)
		{
			fileList[0][counter] = fileNames.get(counter);
			fileList[1][counter] = Integer.toString(hash(fileNames.get(counter)));
		}
		
		return fileList;
	}
	
	/**
	 * Copy a file to the "src" folder if it belongs to that node.
	 * @param transFile The file being transferred. 
	 */
	public void transferFile(File transFile)
	{
		Path currentPath = transFile.toPath();
		File newFile = new File(transFile.getName());
		Path newPath = newFile.toPath();
		
		try {
			Files.copy(currentPath, newPath, REPLACE_EXISTING);
		} catch (IOException e) {
			
		}
		
		addFileName(transFile.getName());
	}
	
	/**
	 * Returns the number of predecessor and successors stored.
	 * @return The store value.
	 */
	public int getStoreNo()
	{
		return storeNo;
	}
}
