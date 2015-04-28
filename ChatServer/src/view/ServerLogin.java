package view;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import network.*;

public class ServerLogin {
	
	private CommManager comm;
	private JFrame frame;
	private Container panel;
	private JTextField servAddress;
	private JTextField servPort;
	private JTextField presAddress;
	private JTextField presPort;
	private JLabel servAddressLabel;
	private JLabel servPortLabel;
	private JLabel presAddressLabel;
	private JLabel presPortLabel;
	private JButton loginButton;
	private JButton cancelButton;
	private JPanel buttonPanel;
	private JPanel infoPanel;
	private JPanel servAddressPanel;
	private JPanel servPortPanel;
	private JPanel presAddressPanel;
	private JPanel presPortPanel;
	private ServerView view;
	private ServerNetwork network;
	private JPanel methodPanel;
	private JComboBox<String> methodCBox;
	private JLabel methodLabel;
	private JLabel details;
	private JCheckBox detailsCheckBox;
	private JPanel detailsPanel;
	private JLabel presCheckText;
	private JCheckBox presCheckBox;
	private JPanel presCheckPanel;
	
	public ServerLogin()
	{
		frame = new JFrame("P2P Server");
		panel = frame.getContentPane();
		servAddress = new JTextField("127.0.0.1", 12);
		servPort = new JTextField("4506", 6);
		presAddress = new JTextField("127.0.0.1", 12);
		presPort = new JTextField("4505", 6);
		loginButton = new JButton("Login");
		cancelButton = new JButton("Quit");
		buttonPanel = new JPanel();
		infoPanel = new JPanel();
		servAddressLabel = new JLabel("Server Address:");
		servPortLabel = new JLabel("Server Port:");
		presAddressLabel = new JLabel("Presentation Address:");
		presPortLabel = new JLabel("Presentation Port:");
		servAddressPanel = new JPanel();
		servPortPanel = new JPanel();
		presAddressPanel = new JPanel();
		presPortPanel = new JPanel();
		methodLabel = new JLabel("Select method:");
		methodPanel = new JPanel();
		methodCBox = new JComboBox<String>();
		detailsPanel = new JPanel();
		details = new JLabel("Choose custom address and port:");
		detailsCheckBox = new JCheckBox();
		presCheckText = new JLabel("Allow presentation server:");
		presCheckBox = new JCheckBox();
		presCheckPanel = new JPanel();
		
		methodCBox.addItem("UDP");
		methodCBox.addItem("TCP");
		methodCBox.setSelectedIndex(0);
		methodPanel.setLayout(new FlowLayout());
		methodPanel.add(methodLabel);
		methodPanel.add(methodCBox);
		
		panel.setLayout(new BorderLayout());
		infoPanel.setLayout(new GridLayout(7,1));
		presCheckPanel.setLayout(new FlowLayout());
		servAddressPanel.setLayout(new FlowLayout());
		servPortPanel.setLayout(new FlowLayout());
		presAddressPanel.setLayout(new FlowLayout());
		presPortPanel.setLayout(new FlowLayout());
		detailsPanel.setLayout(new FlowLayout());
		detailsPanel.add(details);
		detailsPanel.add(detailsCheckBox);
		servAddressPanel.add(servAddressLabel);
		servAddressPanel.add(servAddress);
		servPortPanel.add(servPortLabel);
		servPortPanel.add(servPort);
		servAddress.setEnabled(false);
		servPort.setEnabled(false);
		presAddress.setEnabled(false);
		presPort.setEnabled(false);
		presAddressPanel.add(presAddressLabel);
		presAddressPanel.add(presAddress);
		presPortPanel.add(presPortLabel);
		presPortPanel.add(presPort);
		presCheckPanel.add(presCheckText);
		presCheckPanel.add(presCheckBox);
		infoPanel.add(methodPanel);
		infoPanel.add(detailsPanel);
		infoPanel.add(servAddressPanel);
		infoPanel.add(servPortPanel);
		infoPanel.add(presCheckPanel);
		infoPanel.add(presAddressPanel);
		infoPanel.add(presPortPanel);
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(loginButton);
		buttonPanel.add(cancelButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		panel.add(infoPanel, BorderLayout.CENTER);
		frame.setSize(400,300);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setVisible(true);
		
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				System.exit(0);
			}
		});
		
		loginButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
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
				
				if(((String)methodCBox.getSelectedItem()).equalsIgnoreCase("UDP"))
				{
					comm = new UDPPacketManager();
					System.out.println("Starting with UDP...");
				}
				else
				{
					comm = new TCPManager();
					System.out.println("Starting with TCP...");
				}
				network.initComm(comm);
				view.initComm(comm);
				if(detailsCheckBox.isSelected())
				{
					network.initSocket(servAddress.getText(), servPort.getText());
				}
				else
				{
					network.initSocket();
				}
				if(presCheckBox.isSelected())
				{
					network.setPresDetails(presAddress.getText(), presPort.getText());
				}
				view.initialise();
				new Thread(network).start();
				frame.dispose();
			}
		});
		
		detailsCheckBox.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent evt) {
		        if(detailsCheckBox.isSelected())
		        {
		        	servAddress.setEnabled(true);
		        	servPort.setEnabled(true);
		        }
		        else
		        {
		        	servAddress.setEnabled(false);
		        	servPort.setEnabled(false);
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
	
	public void initView(ServerView view)
	{
		this.view = view;
	}
	
	public void initNetwork(ServerNetwork network)
	{
		this.network = network;
	}
}
