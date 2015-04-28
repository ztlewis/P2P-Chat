package view;

import javax.swing.*;

import dht.DHTNetwork;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import network.*;
import model.*;

/**
 * This class is dedicated to the appearance and functionality of the login screen. 
 * @author Alex
 * @version 0.3
 * @since 0.3
 */
public class ClientLogin{
	
	private CommManager comm;
	private ClientLogin self;
	private JFrame frame;
	private Container panel;
	private JTextField username;
	private JTextField servAddress;
	private JTextField servPort;
	private JTextField presAddress;
	private JTextField presPort;
	private JLabel usernameLabel;
	private JLabel presAddressLabel;
	private JLabel presPortLabel;
	private JLabel servAddressLabel;
	private JLabel servPortLabel;
	private JButton loginButton;
	private JButton cancelButton;
	private JPanel buttonPanel;
	private JPanel infoPanel;
	private JPanel usernamePanel;
	private JPanel servAddressPanel;
	private JPanel servPortPanel;
	private JPanel presAddressPanel;
	private JPanel presPortPanel;
	private ClientView view;
	private ClientNetwork network;
	private ClientModel model;
	private DHTNetwork dht;
	private JLabel dhtInitLabel;
	private JCheckBox dhtCheckBox;
	private JLabel dhtAddressLabel;
	private JLabel dhtPortLabel;
	private JTextField dhtAddress;
	private JTextField dhtPort;
	private JPanel dhtCheckPanel;
	private JPanel dhtAddressPanel;
	private JPanel dhtPortPanel;
	private JPanel switchPanel;
	private JCheckBox switchCheckBox;
	private JLabel switchLabel;
	private JPanel methodPanel;
	private JComboBox<String> methodCBox;
	private JLabel methodLabel;
	private JLabel presCheckText;
	private JCheckBox presCheckBox;
	private JPanel presCheckPanel;
	
	/**
	 * Constructor for login. 
	 * <p>
	 * The entire frame is put together in this method and action listeners
	 * are added. 
	 */
	public ClientLogin ()
	{
		//Instantiate all components. 
		self = this;
		frame = new JFrame("P2P Client");
		panel = frame.getContentPane();
		username = new JTextField(12);
		servAddress = new JTextField("127.0.0.1", 12);
		servPort = new JTextField("4506", 6);
		presAddress = new JTextField("127.0.0.1", 12);
		presPort = new JTextField("4505", 6);
		loginButton = new JButton("Login");
		cancelButton = new JButton("Quit");
		buttonPanel = new JPanel();
		infoPanel = new JPanel();
		usernameLabel = new JLabel("Username:");
		servAddressLabel = new JLabel("Server Address:");
		servPortLabel = new JLabel("Server Port");
		presAddressLabel = new JLabel("Presentation Address:");
		presPortLabel = new JLabel("Presentation Port:");
		usernamePanel = new JPanel();
		servAddressPanel = new JPanel();
		servPortPanel = new JPanel();
		presAddressPanel = new JPanel();
		presPortPanel = new JPanel();
		dhtInitLabel = new JLabel("Join an existing DHT setup?:");
		dhtCheckBox = new JCheckBox();
		dhtAddressLabel = new JLabel("DHT Node Address:");
		dhtPortLabel = new JLabel("DHT Node Port:");
		dhtAddress = new JTextField(12);
		dhtPort = new JTextField(6);
		dhtCheckPanel = new JPanel();
		dhtAddressPanel = new JPanel();
		dhtPortPanel = new JPanel();
		switchPanel = new JPanel();
		switchLabel = new JLabel("Run in DHT mode? "
				+ "(instead of Centralised P2P)");
		switchCheckBox = new JCheckBox();
		methodLabel = new JLabel("Select method:");
		methodPanel = new JPanel();
		methodCBox = new JComboBox<String>();
		methodCBox.addItem("UDP");
		methodCBox.addItem("TCP");
		methodCBox.setSelectedIndex(0);
		presCheckText = new JLabel("Allow presentation server:");
		presCheckBox = new JCheckBox();
		presCheckPanel = new JPanel();
		
		//Set the layouts for each panel. 
		panel.setLayout(new BorderLayout());
		infoPanel.setLayout(new GridLayout(11,1));
		usernamePanel.setLayout(new FlowLayout());
		servAddressPanel.setLayout(new FlowLayout());
		servPortPanel.setLayout(new FlowLayout());
		presCheckPanel.setLayout(new FlowLayout());
		presAddressPanel.setLayout(new FlowLayout());
		presPortPanel.setLayout(new FlowLayout());
		dhtCheckPanel.setLayout(new FlowLayout());
		dhtAddressPanel.setLayout(new FlowLayout());
		dhtPortPanel.setLayout(new FlowLayout());
		switchPanel.setLayout(new FlowLayout());
		methodPanel.setLayout(new FlowLayout());
		
		//Add components to each panel. 
		methodPanel.add(methodLabel);
		methodPanel.add(methodCBox);
		usernamePanel.add(usernameLabel);
		usernamePanel.add(username);
		servAddressPanel.add(servAddressLabel);
		servAddressPanel.add(servAddress);
		servPortPanel.add(servPortLabel);
		servPortPanel.add(servPort);
		presCheckPanel.add(presCheckText);
		presCheckPanel.add(presCheckBox);
		presAddressPanel.add(presAddressLabel);
		presAddressPanel.add(presAddress);
		presPortPanel.add(presPortLabel);
		presPortPanel.add(presPort);
		dhtCheckPanel.add(dhtInitLabel);
		dhtCheckPanel.add(dhtCheckBox);
		dhtAddressPanel.add(dhtAddressLabel);
		dhtAddressPanel.add(dhtAddress);
		dhtPortPanel.add(dhtPortLabel);
		dhtPortPanel.add(dhtPort);
		
		//Add each subpanel to the info panel. 
		infoPanel.add(methodPanel);
		infoPanel.add(switchPanel);
		infoPanel.add(usernamePanel);
		infoPanel.add(servAddressPanel);
		infoPanel.add(servPortPanel);
		infoPanel.add(presCheckPanel);
		infoPanel.add(presAddressPanel);
		infoPanel.add(presPortPanel);
		infoPanel.add(dhtCheckPanel);
		infoPanel.add(dhtAddressPanel);
		infoPanel.add(dhtPortPanel);
		switchPanel.add(switchLabel);
		switchPanel.add(switchCheckBox);
		
		dhtCheckBox.setEnabled(false);
		dhtAddress.setEnabled(false);
		presAddress.setEnabled(false);
		presPort.setEnabled(false);
		dhtPort.setEnabled(false);
		presAddress.setSize(12, 1);
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(loginButton);
		buttonPanel.add(cancelButton);
		
		//Add to the content pane. 
		panel.add(buttonPanel, BorderLayout.SOUTH);
		panel.add(infoPanel, BorderLayout.CENTER);
		frame.setSize(400,500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				System.exit(0);
			}
		});
		
		loginButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				if(((String)methodCBox.getSelectedItem()).
						equalsIgnoreCase("UDP"))
				{
					comm = new UDPPacketManager();
					System.out.println("Starting with UDP...");
					network.setMethod("UDP");
				}
				else
				{
					comm = new TCPManager();
					System.out.println("Starting with TCP...");
					network.setMethod("TCP");
				}
				network.initComm(comm);
				view.initComm(comm);
				network.initSocket();
				
				//Check presentation detail validity.
				try
				{
					InetAddress.getByName(presAddress.getText());
					Integer.parseInt(presPort.getText());
				}
				catch(UnknownHostException uhException)
				{
					JOptionPane.showMessageDialog(null, 
							"Presentation address invalid.");
					return;
				}
				catch(NumberFormatException nfException)
				{
					JOptionPane.showMessageDialog(null, 
							"Presentation port invalid.");
					return;
				}
				
				if(presCheckBox.isSelected())
				{
					network.setPresDetails(presAddress.getText(), presPort.getText());
				}
				if(switchCheckBox.isSelected())
				{
					model.enableDHT(true);
					if(dhtCheckBox.isSelected())
					{
						try {
							dht.initDHT(network.getClientAddress(), 
									network.getClientPort(), false);
							dht.printDetails();
						} catch (UnknownHostException e) {
							return;
						}
						dht.joinNetwork(dhtAddress.getText(), 
								Integer.parseInt(dhtPort.getText()));
					}
					else
					{
						try {
							dht.initDHT(network.getClientAddress(), 
									network.getClientPort(), true);
							dht.printDetails();
							view.initialise(false);
							dht.setDhtRegistered(true);
							frame.dispose();
						} catch (UnknownHostException e) {
							return;
						}
					}
					loginButton.setEnabled(false);
				}
				else
				{
					model.enableDHT(false);
					try {
						dht.initDHT(network.getClientAddress(), 
								network.getClientPort(), false);
						dht.printDetails();
					} catch (UnknownHostException e) {
						return;
					}
					
					//Check server detail validity.
					try
					{
						InetAddress.getByName(servAddress.getText());
						Integer.parseInt(servPort.getText());
					}
					catch(UnknownHostException uhException)
					{
						JOptionPane.showMessageDialog(null, 
								"Server address invalid.");
						return;
					}
					catch(NumberFormatException nfException)
					{
						JOptionPane.showMessageDialog(null, 
								"Server port invalid.");
						return;
					}
					if(((username.getText()).trim()).isEmpty())
					{
						JOptionPane.showMessageDialog(null, 
								"Username empty.");
						return;
					}
					
					model.initSession(username.getText(), servAddress.getText(), 
							servPort.getText());
					loginButton.setEnabled(false);
				}
				new Thread(network).start();
			}
		});
		
		dhtCheckBox.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {
		        if(dhtCheckBox.isSelected())
		        {
		        	dhtAddress.setEnabled(true);
		        	dhtPort.setEnabled(true);
		        }
		        else
		        {
		        	dhtAddress.setEnabled(false);
		        	dhtPort.setEnabled(false);
		        }
		      }
		    });
		
		presCheckBox.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {
		        if(presCheckBox.isSelected())
		        {
		        	presAddress.setEnabled(true);
		        	presPort.setEnabled(true);
		        }
		        else
		        {
		        	presAddress.setEnabled(false);
		        	presPort.setEnabled(false);
		        }
		      }
		    });
		
		switchCheckBox.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {
		        if(switchCheckBox.isSelected())
		        {
		        	dhtCheckBox.setEnabled(true);
		        	if(dhtCheckBox.isSelected())
			        {
			        	dhtAddress.setEnabled(true);
			        	dhtPort.setEnabled(true);
			        }
			        else
			        {
			        	dhtAddress.setEnabled(false);
			        	dhtPort.setEnabled(false);
			        }
		        	servAddress.setEnabled(false);
		        	servPort.setEnabled(false);
		        	username.setEnabled(false);
		        }
		        else
		        {
		        	dhtCheckBox.setEnabled(false);
		        	dhtAddress.setEnabled(false);
		        	dhtPort.setEnabled(false);
		        	servAddress.setEnabled(true);
		        	servPort.setEnabled(true);
		        	username.setEnabled(true);
		        }
		      }
		    });
		
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        if (JOptionPane.showConfirmDialog(frame, 
		            "Are you sure you want to exit?", "EXIT SESSION", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		            System.exit(0);
		        }
		    }
		});
	}
	
	/**
	 * Passes a reference of the view to the login. 
	 * @param view The ClientView object we are passing in.
	 */
	public void initView(ClientView view)
	{
		this.view = view;
	}
	
	/**
	 * Passes a reference of the network to the login. 
	 * @param network The ClientNetwork object we are passing in. 
	 */
	public void initNetwork(ClientNetwork network)
	{
		this.network = network;
	}
	
	/**
	 * Passes a reference of the model to the login.
	 * @param model The ClientModel object we are passing in. 
	 */
	public void initModel(ClientModel model)
	{
		this.model = model;
	}
	
	/**
	 * Passes a reference of the dht network to the login.
	 * @param dht The DHTNetwork object we are passing in. 
	 */
	public void initDHT(DHTNetwork dht)
	{
		this.dht = dht;
	}
	
	public void allowReLogin()
	{
		if(model.dhtEnabled())
		{
			if(!dht.isDhtRegistered())
			{
				JOptionPane.showMessageDialog(null, "DHT connection could not be established.");
			}
		}
		else
		{
			if(!model.getServerRegistered())
			{
				if(model.getDuplicateStatus())
				{
					JOptionPane.showMessageDialog(null, "Username already taken.");
					model.setDuplicateStatus(false);
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Server connection could not be established.");
				}
			}
		}
		loginButton.setEnabled(true);
	}
	
	/**
	 * Remove this frame from view. 
	 */
	public void disposeLogin()
	{
		frame.dispose();
	}
}

