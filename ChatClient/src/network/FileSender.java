package network;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

import javax.swing.JOptionPane;

import view.ClientView;

import dht.DHTNetwork;


/**
 * This class is designed to send a specified file. 
 * <p>
 * It works by initialising a socket and accepting a single connection
 * before sending.
 * <p>
 * It should be used by spawning a new thread to invoke its run 
 * function. 
 * @author Alex
 * @version 0.3
 * @since 0.3
 */
public class FileSender implements Runnable{
	
	private File newFile;
	private String fileName;
	private ServerSocket servSocket;
	private Socket newSocket;
	private DHTNetwork dht;
	private ClientView view;
	private String username; //The one who is receiving the file. 

	/**
	 * Constructor for FileSender class. A socket is initiated for sending.
	 * @param newFile File that is being sent. 
	 * @param dht DHTNetwork object that created this instance of FileSender. 
	 */
	public FileSender(File newFile, DHTNetwork dht, ClientView view, String username)
	{
		this.newFile = newFile;
		this.fileName = newFile.getName();
		this.dht = dht;
		this.view = view;
		this.username = username;
		
		try {
			servSocket = new ServerSocket(0);
		} catch (IOException e) {
			System.out.println("File could not be sent.");
		}
	}
	
	public void run()
	{
		if(!newFile.exists())
		{
			return;
		}
		try
		{
			servSocket.setSoTimeout(10000);
			Socket newSocket = servSocket.accept();
			byte[] byteArray = new byte[(int)newFile.length()];
			BufferedInputStream bufferedInput = 
					new BufferedInputStream(new FileInputStream(newFile));
			
			bufferedInput.read(byteArray, 0, byteArray.length);
			OutputStream output = newSocket.getOutputStream();
			output.write(byteArray, 0, byteArray.length);
			output.flush();
			newSocket.close();
			servSocket.close();
			if(dht != null)
			{
				System.out.println("Finishing sending file: " + fileName + " (" + 
				dht.hash(fileName) + ").");
				if(dht.isTransferring())
				{
					dht.removeFileName(fileName);
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Finishing sending file: " 
				+ fileName + "(" + dht.hash(fileName) + ").");
				}
			}
			if(view != null)
			{
				Date date = new Date();
		        SimpleDateFormat sdFormat = new SimpleDateFormat("hh:mm:ss a");
				String dateString = sdFormat.format(date);
				
				System.out.println("Finished sending file to " + username);
				view.recieveMessage(username, dateString, "Finished receiving file: " + 
						fileName + ".");
			}
		}
		catch(IOException ioException)
		{
			if(dht != null)
			{
				System.out.println("File: " + fileName + " (" + dht.hash(fileName) 
						+ ") could not be sent.");
				if(dht.isTransferring())
				{
					dht.removeFileName(fileName);
				}
				else
				{
					JOptionPane.showMessageDialog(null, "File: " + fileName + " (" 
				+ dht.hash(fileName) + ") could not be sent.");
				}
			}
			if(view != null)
			{
				Date date = new Date();
		        SimpleDateFormat sdFormat = new SimpleDateFormat("hh:mm:ss a");
				String dateString = sdFormat.format(date);
				
				view.recieveMessage(username, dateString, "File: " + 
						fileName + " could not be received.");
			}
		}
	}
	
	/**
	 * Getter for the port of the socket that was created by this class. 
	 * @return The transmission port. 
	 */
	public int getPort()
	{
		return servSocket.getLocalPort();
	}
}
