package dht;


/**
 * The purpose of this class is to keep track of ONE individual DHT node.
 * This includes the id, address and port of the node. These objects are
 * kept in the finger table.
 * @author Alex
 * @version 0.3
 * @since 0.3
 */
public class DHTNode {
	
	//Information for each DHT node.
	private int id;
	private String address;
	private int port;
	
	//Constructor for a DHT node.
	/**
	 * Constructor that specifies id, address and port. 
	 * @param id The id of the new DHT node. 
	 * @param address The address of the new DHT node. 
	 * @param port The port of the new DHT node. 
	 */
	public DHTNode(int id, String address, int port)
	{
		this.id = id;
		this.address = address;
		this.port = port;
	}
	
	//Getters for instance variables.
	/**
	 * Getter for the node id. 
	 * @return This node's id. 
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * Getter for the node address. 
	 * @return This node's address. 
	 */
	public String getAddress()
	{
		return address;
	}
	
	/**
	 * Getter for the node port. 
	 * @return This node's port. 
	 */
	public int getPort()
	{
		return port;
	}
}
