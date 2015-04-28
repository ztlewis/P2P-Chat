 package view;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import model.ClientModel;
import model.Peer;
import network.*;
import dht.*;

/**
 * This class is dedicated to the functionality and the appearance of the 
 * Centralised P2P and Structured DHT interface.
 * <p>
 * The interface is specified (whether or not we want DHT) by toggling the tabs
 * that are shown on the screen.
 * @author Alex
 * @version 0.3
 * @since 0.3
 */
public class ClientView {
	
	private DHTNetwork dht;
	private CommManager comm;
	private JFrame frame;
	private Container chatPanel;
	private Container upPanel;
	private Container downPanel;
	private Container panel;
	private JList<String> peerList;
	private JTable downFTable;
	private JTable downLists;
	private JTable downFiles;
	private JTable upFTable;
	private JTable upLists;
	private JTable upFiles;
	private DefaultTableModel fingerModel;
	private DefaultTableModel listModel;
	private DefaultTableModel fileModel;
	private JScrollPane downSpFinger;
	private JScrollPane downSpLists;
	private JScrollPane downSpFiles;
	private JScrollPane upSpFinger;
	private JScrollPane upSpLists;
	private JScrollPane upSpFiles;
	private JButton send;
	private JButton file;
	private JTextField message;
	private JTabbedPane tabs;
	private JPanel msgPanel;
	private ClientModel model;
	private JTabbedPane funcTabs;
	private JPanel upDirPanel;
	private JPanel downDirPanel;
	private JButton upButton;
	private JButton downButton;
	private JTextField upDir;
	private JTextField downDir;
	private JLabel upLabel;
	private JLabel downLabel;
	private JButton choose;
	private File selectedUpload;
	private JPanel upDhtPanel;
	private JPanel downDhtPanel;
	private JPanel upConnectionDetailsPanel;
	private JPanel downConnectionDetailsPanel;
	private JLabel upConnectionDetails;
	private JLabel downConnectionDetails;
	private String lastIdDownload;
	private String lastIdUpload;
	
	/**
	 * Constructor for view. 
	 * <p>
	 * The entire frame is put together in this method and action listeners
	 * are added. 
	 */
	public ClientView()
	{
		//Instantiate everything. 
		frame = new JFrame("P2P Client");
		panel = frame.getContentPane();
		funcTabs = new JTabbedPane();
		panel.add(funcTabs);
		chatPanel = new JPanel();
		chatPanel.setLayout(new BorderLayout());
		msgPanel = new JPanel();
		msgPanel.setLayout(new FlowLayout());
		peerList = new JList<String>();
		peerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		peerList.setFixedCellWidth(50);
		upLabel = new JLabel("Filename: ");
		downLabel = new JLabel("Filename: ");
		send = new JButton("Send Message");
		file = new JButton("Send File");
		upButton = new JButton("Upload");
		downButton = new JButton("Download");
		choose = new JButton("Select File");
		upConnectionDetails = new JLabel();
		downConnectionDetails = new JLabel();
		upConnectionDetailsPanel = new JPanel();
		downConnectionDetailsPanel = new JPanel();
		message = new JTextField(20);
		tabs = new JTabbedPane();
		fingerModel = new DefaultTableModel();
		listModel = new DefaultTableModel();
		fileModel = new DefaultTableModel();
		downFTable = new JTable(fingerModel);
		downLists = new JTable(listModel);
		downFiles = new JTable(fileModel);
		upFTable = new JTable(fingerModel);
		upLists = new JTable(listModel);
		upFiles = new JTable(fileModel);
		downFTable.setEnabled(false);
		downLists.setEnabled(false);
		downFiles.setEnabled(false);
		upFTable.setEnabled(false);
		upLists.setEnabled(false);
		upFiles.setEnabled(false);
		downSpFinger = new JScrollPane(downFTable);
		downSpLists = new JScrollPane(downLists);
		downSpFiles = new JScrollPane(downFiles);
		upSpFinger = new JScrollPane(upFTable);
		upSpLists = new JScrollPane(upLists);
		upSpFiles = new JScrollPane(upFiles);
		msgPanel.add(message);
		msgPanel.add(send);
		msgPanel.add(file);
		chatPanel.add(msgPanel, BorderLayout.SOUTH);
		upPanel = new JPanel();
		downPanel = new JPanel();
		upDhtPanel = new JPanel();
		downDhtPanel = new JPanel();
		upDir = new JTextField(15);
		upDir.setEnabled(false);
		downDir = new JTextField(15);
		
		//Choose panel layouts. 
		upPanel.setLayout(new GridLayout(3,1));
		downPanel.setLayout(new GridLayout(2,1));
		upDhtPanel.setLayout(new GridLayout(4,1));
		downDhtPanel.setLayout(new GridLayout(4,1));
		upDirPanel = new JPanel();
		downDirPanel = new JPanel();
		upDirPanel.setLayout(new FlowLayout());
		downDirPanel.setLayout(new FlowLayout());
		
		//Add columns to each of the table models. 
		fingerModel.addColumn("No");
		fingerModel.addColumn("Target");
		fingerModel.addColumn("ID");
		fingerModel.addColumn("Address");
		fingerModel.addColumn("Port");
		
		listModel.addColumn("ID");
		listModel.addColumn("Address");
		listModel.addColumn("Port");
		
		fileModel.addColumn("Filename");
		fileModel.addColumn("ID");
		
		//Add each component to each subpanel. 
		upDirPanel.add(upLabel);
		upDirPanel.add(upDir);
		downDirPanel.add(downLabel);
		downDirPanel.add(downDir);
		upPanel.add(upDirPanel);
		upPanel.add(choose);
		upPanel.add(upButton);
		downPanel.add(downDirPanel);
		downPanel.add(downButton);
		upDhtPanel.add(upPanel);
		upDhtPanel.add(upSpLists);
		upDhtPanel.add(upSpFinger);
		upDhtPanel.add(upSpFiles);
		downDhtPanel.add(downPanel);
		downDhtPanel.add(downSpLists);
		downDhtPanel.add(downSpFinger);
		downDhtPanel.add(downSpFiles);
		
		//Add subpanels to the panel on the Upload Tab and the Download
		//Tab. 
		upConnectionDetailsPanel.setLayout(new BorderLayout());
		downConnectionDetailsPanel.setLayout(new BorderLayout());
		upConnectionDetailsPanel.add(upDhtPanel, BorderLayout.CENTER);
		downConnectionDetailsPanel.add(downDhtPanel, BorderLayout.CENTER);
		upConnectionDetailsPanel.add(upConnectionDetails, BorderLayout.NORTH);
		downConnectionDetailsPanel.add(downConnectionDetails, BorderLayout.NORTH);
		
		peerList.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent evt) {
		        JList peerList = (JList)evt.getSource();
		        if (evt.getClickCount() == 2) {
		            String username = (String) peerList.getSelectedValue();
		            if(!tabExists(username))
		            {
		            	addConversation(username);
		            }
		            else
		            {
		            	removeConversation(username);
		            }
		        }
		    }
		});
		
		send.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				sendMessage();
			}
		});
		
		file.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				 JFileChooser chooser = new JFileChooser();
				 int returnVal = chooser.showOpenDialog(frame);
				 if(returnVal == JFileChooser.APPROVE_OPTION) {
				      
					 File transferFile = chooser.getSelectedFile();
					 int index = tabs.getSelectedIndex();
					 if(index >= 0)
					 {
						 model.sendFileRequest(tabs.getTitleAt(index),
				    		 transferFile);
					 }
				 }
			}
		});
		
		chatPanel.add(tabs, BorderLayout.CENTER);
		chatPanel.add(peerList, BorderLayout.EAST);
		funcTabs.add("Chat", chatPanel);
		funcTabs.add("Upload", upConnectionDetailsPanel);
		funcTabs.add("Download", downConnectionDetailsPanel);
		send.setEnabled(false);
		file.setEnabled(false);
		
		lastIdUpload = "";
		lastIdDownload = "";
		
		frame.setSize(600,600);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        if (JOptionPane.showConfirmDialog(frame, 
		            "Are you sure you want to exit?", "EXIT SESSION", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		        	//If we're signed into the P2P chat, then let's deregister
		        	//from that.
		        	if(model.getServerRegistered() == true)
		        	{
		        		model.deregisterClient();
		        	}
		        	dht.notifySelfRemoval();
		        	while(comm.getCacheSize() > 0)
		        	{
		        		
		        	}
		            System.exit(0);
		        }
		    }
		});
	}
	
	/**
	 * Triggers the message send method in the model and in turn invokes
	 * the sending of the message to the recipient. 
	 */
	public void sendMessage()
	{
		int index;
		String sendString;
		String sendUser;
		JTextArea chatArea;
		String dateString;
		
		index = tabs.getSelectedIndex();
		if(index >= 0)
		{
			sendString = message.getText();
			sendUser = tabs.getTitleAt(index);
			
			model.sendMessage(sendUser, sendString);
			chatArea = (JTextArea)tabs.getComponentAt(index);
			
			Date date = new Date();
	        SimpleDateFormat sdFormat = new SimpleDateFormat("hh:mm:ss a");
			dateString = sdFormat.format(date);
			
			chatArea.append(dateString + " - " + model.getUsername() 
					+ ": " + sendString + "\n");
			message.setText("");
		}
	}
	
	/**
	 * Displays the message in the message log. 
	 * @param username The user that the message was received from. 
	 * @param time The time the message was received at. 
	 * @param message The message that was sent. 
	 */
	public void recieveMessage(String username, String time, String message)
	{
		int counter;
		JTextArea chatArea;
		
		if(!tabExists(username))
		{
			addConversation(username);
		}
		
		for(counter=0; counter<tabs.getComponentCount(); counter++)
		{
			if(tabs.getTitleAt(counter).equalsIgnoreCase(username))
			{
				break;
			}
		}
		
		chatArea = (JTextArea)tabs.getComponentAt(counter);
		chatArea.append(time + " - " + username + ": " + message + "\n");
	}
	
	/**
	 * Opens up another tab for a specified user. 
	 * @param username The user that the conversation is being requested for. 
	 */
	public void addConversation(String username)
	{
		JTextArea chat = new JTextArea();
		chat.setEditable(false);
		tabs.add(username, chat);
		
		send.setEnabled(true);
		file.setEnabled(true);
	}
	
	public void removeConversation(String username)
	{
		int counter;
		
		for(counter=0;counter<tabs.getTabCount();counter++)
		{
			if(tabs.getTitleAt(counter).equalsIgnoreCase(username))
			{
				tabs.removeTabAt(counter);
			}
		}
		
		if(tabs.getTabCount() == 0)
		{
			send.setEnabled(false);
			file.setEnabled(false);
		}
	}
	
	/**
	 * Initialises aspects of the view. 
	 * <p>
	 * Firstly, the connection details are updated as required. The tabs
	 * modified depending on what is needed (we only need chat OR dht not 
	 * both). 
	 * @param mode True for DHT, false for chat. 
	 */
	public void initialise(boolean mode) //true for centralised, false for DHT
	{
		frame.setTitle("P2P Client: " + model.getUsername());
		try {
			upConnectionDetails.setText("DHT ID: " + dht.getId() + ", Peer Address: " + comm.getClientAddress()
					+ ", Peer Port: " + comm.getClientPort());
			downConnectionDetails.setText("DHT ID: " + dht.getId() + ", Peer Address: " + comm.getClientAddress()
					+ ", Peer Port: " + comm.getClientPort());
		} catch (UnknownHostException e) {
			
		}
		if(mode)
		{
			funcTabs.remove(upConnectionDetailsPanel);
			funcTabs.remove(downConnectionDetailsPanel);
		}
		else
		{
			funcTabs.remove(chatPanel);
		}
		
		model.refreshDHTInfo();
		downFTable.setDefaultRenderer(Object.class, new DHTDownCellRenderer());
		upFTable.setDefaultRenderer(Object.class, new DHTUpCellRenderer());
		frame.setVisible(true);
	}
	
	/**
	 * Passing a reference of the model to the view. 
	 * @param model The ClientModel object we are passing in. 
	 */
	public void initModel(ClientModel model)
	{
		this.model = model;
	}
	
	/**
	 * Passing a reference to a CommManager to the view. 
	 * <p>
	 * Exactly what this reference is depends on whether we've chosen a
	 * UDP or TCP method of transmission. 
	 * @param comm The CommManager we are passing in. 
	 */
	public void initComm(CommManager comm)
	{
		this.comm = comm;
	}
	
	/**
	 * Passing a reference to the dht network to the view. 
	 * <p>
	 * At this point, DHT dependent buttons are also initialised. 
	 * @param dht The DHTNetwork object that we are passing in. 
	 */
	public void initDHT(DHTNetwork dht)
	{
		this.dht = dht;
		initDHTButtons();
	}
	
	/**
	 * After the DHT reference is passed in, we can invoke methods in 
	 * the DHTNetwork object through the Upload and Download buttons. 
	 */
	public void initDHTButtons()
	{
		upButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				if(selectedUpload != null)
				{
					try {
						dht.uploadCheck(upDir.getText(),comm.getClientAddress(), 
								comm.getClientPort(), selectedUpload);
						upDir.setText("");
						selectedUpload = null;
					} catch (UnknownHostException e) {}
				}
				else
				{
					JOptionPane.showMessageDialog(null ,"No file selected.");
				}
			}
		});
		
		downButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				try {
					dht.forwardFileDownRequest(downDir.getText(),comm.getClientAddress(), 
							comm.getClientPort(), true);
					downDir.setText("");
				} catch (UnknownHostException e) {}
			}
		});
		
		choose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				 JFileChooser chooser = new JFileChooser();
				 int returnVal = chooser.showOpenDialog(upPanel);
				 if(returnVal == JFileChooser.APPROVE_OPTION) {
				      
					 selectedUpload = chooser.getSelectedFile();
				     upDir.setText(chooser.getSelectedFile().getName());
				 }
			}
		});
	}
	
	/**
	 * Refresh the GUI peer list. 
	 * <p>
	 * Reflect changes to the peer list in the view by passing in a new 
	 * Map containing the peer list. 
	 * @param map The peer list hash map containing <username, peer> pairs. 
	 */
	public void refreshPeerList(Map<String, Peer> map)
	{
		int listSize = map.size()-1;
		if(listSize >=0)
		{
			String[] array = new String[listSize];
			int counter = 0;
			
			for(Peer peer : map.values())
			{
				if(!model.getUsername().equalsIgnoreCase(peer.getUsername()))
				{
					array[counter] = peer.getUsername();
					counter++;
				}
			}
			
			peerList.setListData(array);
		}
	}
	
	/**
	 * A check to see if a chat tab for a particular user is already open. 
	 * @param username The user that is being checked. 
	 * @return The existence status. 
	 */
	public boolean tabExists(String username)
	{
		int counter;
		
		for(counter=0; counter<tabs.getComponentCount(); counter++)
		{
			if(tabs.getTitleAt(counter).equalsIgnoreCase(username))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Trigger a refresh of the GUI finger table to reflect the changes in the
	 * DHTNetwork's finger table. 
	 * <p>
	 * The finger table information is gathered from the DHTNetwork object
	 * in the form of a two dimensional string array and is then placed
	 * into the table. 
	 */
	public void refreshFingerTable()
	{
		String[][] tableList;
		fingerModel.setRowCount(0);
		
		tableList = dht.getTableInfo();
		int counter;
		
		for(counter=0;counter<dht.getTabSize();counter++)
		{
			Vector<Object> row = new Vector<Object>();
			row.add(tableList[counter][0]);
			row.add(tableList[counter][1]);
			row.add(tableList[counter][2]);
			row.add(tableList[counter][3]);
			row.add(tableList[counter][4]);
			
			fingerModel.addRow(row);
		}
	}
	
	/**
	 * Trigger a refresh of the GUI file list to reflect the changes in the
	 * DHTNetwork's file list. 
	 * <p>
	 * The file list information is gathered from the DHTNetwork object
	 * in the form of a two dimensional string array and is then placed
	 * into the table. 
	 */
	public void refreshFileNames()
	{
		String[][] fileNames;
		fileModel.setRowCount(0);
		int counter;
		
		fileNames = dht.getFileNames();
		for(counter=0;counter<fileNames[0].length;counter++)
		{
			Vector<Object> row = new Vector<Object>();
			row.add(fileNames[0][counter]);
			row.add(fileNames[1][counter]);
			fileModel.addRow(row);
		}
	}
	
	/**
	 * Trigger a refresh of the GUI pred/succ lists to reflect the changes 
	 * in the DHTNetwork's lists. 
	 * <p>
	 * The list is gathered from the DHTNetwork object
	 * in the form of a two dimensional string array and is then placed
	 * into the table. 
	 */
	public void refreshLists()
	{
		String[][] tableList;
		listModel.setRowCount(0);
		
		tableList = dht.getListInfo();
		int counter;
		
		for(counter=0;counter<(dht.getStoreNo()*2);counter++)
		{
			Vector<Object> row = new Vector<Object>();
			row.add(tableList[counter][0]);
			row.add(tableList[counter][1]);
			row.add(tableList[counter][2]);
			
			listModel.addRow(row);
		}
	}
	
	/**
	 * Set the last download id. 
	 * @param id The last download id. 
	 */
	public void setLastDownloadId(int id)
	{
		lastIdDownload = Integer.toString(id);
	}
	
	/**
	 * Set the last upload id. 
	 * @param id The last upload id. 
	 */
	public void setLastUploadId(int id)
	{
		lastIdUpload = Integer.toString(id);
	}
	
	public class DHTDownCellRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table, 
				Object value, boolean isSelected, boolean hasFocus,
                int row, int column)
		{
	
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if(((String)table.getValueAt(row, 2)).equalsIgnoreCase(lastIdDownload))
			{
				c.setBackground(Color.YELLOW);
			}
			else
			{
				c.setBackground(table.getBackground());
			}
			return c;
		}
	}
	
	public class DHTUpCellRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table, 
				Object value, boolean isSelected, boolean hasFocus,
                int row, int column)
		{
	
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if(((String)table.getValueAt(row, 2)).equalsIgnoreCase(lastIdUpload))
			{
				c.setBackground(Color.PINK);
			}
			else
			{
				c.setBackground(table.getBackground());
			}
			return c;
		}
	}
}