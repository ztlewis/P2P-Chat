package model;

/**
 * This class stores all information about a single peer. The model
 * class keeps peers in a list so they can be accessed as a group
 * or individually.
 * @author Alex
 * @version 0.2
 * @since 0.1
 */
public class Peer {

	private String username;
	private String address;
	private int port;
	
	/**
	 * Constructor for Peer class.
	 * @param username Username of new peer.
	 * @param address IP address of peer.
	 * @param port Port number of peer.
	 */
	public Peer(String username, String address, int port)
	{
		this.username = username;
		this.address = address;
		this.port = port;
	}
	
	/**
	 * Getter for peer username.
	 * @return String username of peer.
	 */
	public String getUsername()
	{
		return username;
	}
	
	/**
	 * Getter for IP address of peer.
	 * @return String IP address of peer.
	 */
	public String getAddress()
	{
		return address;
	}
	
	/**
	 * Getter for port number for peer.
	 * @return Port number of peer.
	 */
	public int getPort()
	{
		return port;
	}
}
