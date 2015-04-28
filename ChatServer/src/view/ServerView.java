package view;

import javax.swing.*;

import java.awt.*;
import java.net.UnknownHostException;

import network.*;

public class ServerView {
	
	private JLabel serverDetails;
	private JTextArea log;
	private CommManager comm;
	private JFrame frame;
	private Container panel;
	private JScrollPane spLog;
	
	public ServerView()
	{
		frame = new JFrame("P2P Server");
		log = new JTextArea();
		log.setEditable(false);
		
		serverDetails = new JLabel();
		panel = frame.getContentPane();
		spLog = new JScrollPane(log);
	
		panel.setLayout(new BorderLayout());
		panel.add(spLog, BorderLayout.CENTER);
		panel.add(serverDetails, BorderLayout.NORTH);
		frame.setSize(500,400);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
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
	
	public void initialise()
	{
		try {
			serverDetails.setText("Server Address: " + comm.getServerAddress()
					+ ", Server Port: " + comm.getServerPort());
		} catch (UnknownHostException e) {
			
		}
		frame.setVisible(true);
	}
	
	public void logMessage(String message)
	{
		log.append(message + "\n");
	}
	
	public void initComm(CommManager comm)
	{
		this.comm = comm;
	}
}
