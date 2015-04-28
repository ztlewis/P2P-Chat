package dht;


import java.util.ArrayList;
import java.util.List;

/**
 * This class is made to keep track of all finger table information.
 * Because we actually need to contact peers, we can't just have an 
 * id in the "target" field. This class uses DHT nodes to emulate 
 * a version of this "target" field where we can not only know the 
 * id of the node but also pull out contact information.
 * @author Alex
 * @version 0.3
 * @since 0.3
 */
public class FingerTable {
	
	private String ip; //Ip of this peer.
	private int port; //Port of this peer.
	private int id; //Id of THIS peer.
	private int tabSize; //Size of table.
	Object[][] tabData; //Actual finger table data.
	
	//Constructor for finger table. Specify id hash and size of table.
	/**
	 * Constructor for FingerTable. Initialises the id, ip, port and 
	 * table size values. An empty copy of the table data is initialised
	 * and filled with data of itself. 
	 * @param id
	 * @param ip
	 * @param port
	 * @param tabSize
	 */
	public FingerTable(int id, String ip, int port, int tabSize)
	{
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.tabSize = tabSize;
		tabData = new Object[tabSize][2]; //One field for target and another
		//for the link. The field "i" is the same as the actual index of this
		//structure.
		constructTable();
	}
	
	//Fill out the target fields.
	/**
	 * Create the initial finger table. 
	 */
	public void constructTable()
	{
		int counter;
		int maxIds;
		DHTNode self;
		
		//The entire ID space.
		maxIds = (int) Math.pow(2, tabSize-1);
		self = new DHTNode(id, ip, port);
		
		//Creating the target value for each field.
		for(counter=0; counter<tabSize; counter++)
		{
			tabData[counter][0] = (int)(id + Math.pow(2, counter))%maxIds;
			tabData[counter][1] = self;
		}
	}
	
	/**
	 * Add a node to the finger table if appropriate. 
	 * <p>
	 * This method cycles through the entire finger table and will replace
	 * and existing entries with the new set of details if it is a better
	 * fit for that slot in the table. 
	 * @param newId The id of the node being added .
	 * @param ip The address of the node being added. 
	 * @param port The port of the node being added. 
	 */
	public void addNode(int newId, String ip, int port)
	{
		int counter;
		int tempTarget;
		DHTNode tempNode;
		DHTNode newNode;
		
		//Make the new DHT node object.
		newNode = new DHTNode(newId, ip, port);
		
		//Checking each field in the table.
		for(counter=0; counter<tabSize; counter++)
		{
			//Get target and link information one row.
			tempTarget = (int)tabData[counter][0];
			tempNode = (DHTNode)tabData[counter][1];
			
			//Normal case where the new target is closer than the
			//old one. 
			if(tempNode.getId() >= tempTarget)
			{
				if((newId > tempTarget) && (newId < tempNode.getId()))
				{
					tabData[counter][1] = newNode;
				}
			}
			else
			{
				if((newId > tempTarget) || (newId < tempNode.getId()))
				{
					tabData[counter][1] = newNode;
				}
			}
		}
	}
	
	/**
	 * Add a node to the finger table if appropriate. 
	 * <p>
	 * This method cycles through the entire finger table and will replace
	 * and existing entries with the new set of details if it is a better
	 * fit for that slot in the table. 
	 * @param newNode The node being added. 
	 */
	public void addNode(DHTNode newNode)
	{
		int counter;
		int tempTarget;
		DHTNode tempNode;
		
		if(newNode == null)
		{
			return;
		}
		
		//Checking each field in the table.
		for(counter=0; counter<tabSize; counter++)
		{
			//Get target and link information one row.
			tempTarget = (int)tabData[counter][0];
			tempNode = (DHTNode)tabData[counter][1];
			
			//Normal case where the new target is closer than the
			//old one. 
			if(tempNode.getId() >= tempTarget)
			{
				if((newNode.getId() > tempTarget) && (newNode.getId() < tempNode.getId()))
				{
					tabData[counter][1] = newNode;
				}
			}
			else
			{
				if((newNode.getId() > tempTarget) || (newNode.getId() < tempNode.getId()))
				{
					tabData[counter][1] = newNode;
				}
			}
		}
	}
	
	/**
	 * Prints the finger table to console.
	 * <p>
	 * Helpful for debugging purposes. 
	 */
	public void printTable()
	{
		int counter;
		DHTNode tempNode;
		
		//Cycle through and print everything.
		for(counter=0; counter<tabSize; counter++)
		{
			tempNode = (DHTNode)tabData[counter][1];
			System.out.println("Number: " + counter + ", Target: " + 
		tabData[counter][0] + ", Link: " + tempNode.getId() + ", Address: "
		+ tempNode.getAddress() + ", Port: " + tempNode.getPort());
		}
	}
	
	/**
	 * Find the closest successor in the finger table to the input id. 
	 * @param The id being searched. 
	 * @return The closest successor node. 
	 */
	public DHTNode findSuccessor(int itemId)
	{
		int counter;
		boolean smallestId = true;
		DHTNode targetNode = null;
		Integer targetId = null;
		DHTNode tempNode;
		Integer tempId = null;
		
		for(counter=0; counter<tabSize; counter++)
		{
			tempNode = (DHTNode)tabData[counter][1];
			tempId = (Integer)tabData[counter][0];
			//Check for the largest node that is smaller than the item id.
			if((tempId <= itemId) && ((targetId == null) || 
					(tempId > targetId)))
			{
				smallestId = false;
				targetId = tempId;
				targetNode = tempNode;
			}
		}
		
		//If we actually found one, return it.
		if(!smallestId)
		{
			return targetNode;
		}
		else
		{
			targetId = null;
			//Otherwise, it means they're all larger than the item id. That means 
			//we need to look for the  node with the largest id.
			for(counter=0; counter<tabSize; counter++)
			{
				tempId = (Integer)tabData[counter][0];
				tempNode = (DHTNode)tabData[counter][1];
				if((targetId == null) || (tempId > targetId))
				{
					targetId = tempId;
					targetNode = tempNode;
				}
			}
			
			return targetNode;
		}
	}
	
	//Get a list of all unique nodes in the finger table.
	/**
	 * Get an array list of all unique nodes in the finger table. 
	 * <p>
	 * Obviously duplicates are not included in the array. You can make the 
	 * assumption that each one is different. 
	 * @return List of nodes. 
	 */
	public List<DHTNode> getNodes()
	{
		List<DHTNode> list = new ArrayList<DHTNode>();
		int prevId = -1;
		int counter = 0;
		DHTNode tempNode;
		
		for(counter=0; counter<tabSize; counter++)
		{
			tempNode = (DHTNode)tabData[counter][1];
			if(tempNode.getId() != prevId)
			{
				list.add(tempNode);
				prevId = tempNode.getId();
			}
		}
		
		return list;
	}
	
	/**
	 * Replace any entries in the finger table that correspond to the old node
	 * with the new node. 
	 * @param oldNode The node being replaced. 
	 * @param newNode The new node being inserted. 
	 */
	public void replaceNode(DHTNode oldNode, DHTNode newNode)
	{
		int counter;
		DHTNode tempNode;
		
		for(counter=0; counter<tabSize; counter++)
		{
			tempNode = (DHTNode)tabData[counter][1];
			if(tempNode.getId() == oldNode.getId())
			{
				tabData[counter][1] = newNode;
			}
		}
	}
	
	/**
	 * Convert the finger table information into a two dimensional array. 
	 * @return The finger table information. 
	 */
	public String[][] getTableInfo()
	{
		String[][] stringTable = new String[tabSize][5];
		int counter;
		DHTNode tempNode;
		
		for(counter=0;counter<tabSize;counter++)
		{
			stringTable[counter][0] = Integer.toString(counter);
			stringTable[counter][1] = Integer.toString((int)tabData[counter][0]);
			
			tempNode = (DHTNode)tabData[counter][1];
			
			stringTable[counter][2] = Integer.toString(tempNode.getId());
			stringTable[counter][3] = tempNode.getAddress();
			stringTable[counter][4] = Integer.toString(tempNode.getPort());
		}
		
		return stringTable;
	}
}
