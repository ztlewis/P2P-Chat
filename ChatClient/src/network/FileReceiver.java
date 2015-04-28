package network;

import java.net.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

import javax.swing.JOptionPane;

import view.ClientView;

import dht.DHTNetwork;


/**
 * This class is designed to receive a file that is being sent by another node.
 * <p>
 * It works by creating an instance of this class (by specifying the address
 * and port of the sender so it knows where to connect. Then another thread
 * should be spawned to cycle through the run(...) method. 
 * @author Alex
 * @version 0.3
 * @since 0.3
 */
public class FileReceiver implements Runnable{

	private String fileName;
	private String address;
	private int port;
	private DHTNetwork dht;
	private ClientView view;
	private String username; //The one who is sending the file.
	
	/**
	 * Basic constructor for the FileReceiver class. 
	 * @param fileName The name of the file being received. 
	 * @param address The address of the node sending. 
	 * @param port The port of the node sending. 
	 * @param dht The DHTNetwork object that created this object. 
	 */
	public FileReceiver(String fileName, String address, int port, 
			DHTNetwork dht, ClientView view, String username)
	{
		this.fileName = fileName;
		this.address = address;
		this.port = port;
		this.dht = dht;
		this.view = view;
		this.username = username;
	}
	
	@Override
	public void run() {
		try
		{
			//Important that we use stream socket so that we can actually
			//receive a file. DatagramSockets were only useful for 
			//packets because information order and reliability was not 
			//as important.
			Socket recvSocket = new Socket(address, port);
			byte[] byteArray = new byte[1000000];
			int bytesRead;
			int offset = 0;
					
			InputStream input = recvSocket.getInputStream();
			FileOutputStream fileOutput = new FileOutputStream(fileName);
			BufferedOutputStream bufferedOutput = 
					new BufferedOutputStream(fileOutput);
			recvSocket.setSoTimeout(10000);
					
			while((bytesRead = input.read(byteArray, offset, (byteArray.length-offset)))!=-1)
			{
				offset += bytesRead;
			}
			
			bufferedOutput.write(byteArray, 0, offset);

			bufferedOutput.close();
			fileOutput.close();
			recvSocket.close();
			if(dht != null)
			{
				JOptionPane.showMessageDialog(null, "Finished receiving file: " + 
				fileName + "(" + dht.hash(fileName) + ").");
				System.out.println("Finished receiving file: " + fileName + " (" + 
				dht.hash(fileName) + ").");
			}
			if(view != null)
			{
				Date date = new Date();
		        SimpleDateFormat sdFormat = new SimpleDateFormat("hh:mm:ss a");
				String dateString = sdFormat.format(date);
				
				System.out.println("Finished receiving file from " + username);
				view.recieveMessage(username, dateString, "Finished sending file: " + 
						fileName + ".");
			}
		}
		catch(IOException ioException)
		{
			System.gc();
			if(dht != null)
			{
				JOptionPane.showMessageDialog(null, "File: " + fileName + " (" + 
				dht.hash(fileName) + ") could not be received.");
				System.out.println("File: " + fileName + " (" + dht.hash(fileName)
						+ ") could not be received.");
			}
			if(view != null)
			{
				Date date = new Date();
		        SimpleDateFormat sdFormat = new SimpleDateFormat("hh:mm:ss a");
				String dateString = sdFormat.format(date);
				
				view.recieveMessage(username, dateString, "File: " + 
						fileName + " could not be sent.");
			}
			File delFile = new File(fileName);
			delFile.delete();
		}
	}
}
