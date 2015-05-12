package file;

import java.io.*;
import java.util.*;

import view.*;

/**
 * This class manages the log csv file on the presentation server that 
 * stores all message information in the network.
 * @author Alex
 * @version 0.2
 * @since 0.2
 */
public class FileManager {
	
	private FileWriter fWriterLog, fWriterPasswords;
	private ServerConsole console;
	
	/**
	 * Constructor for FileManager class.
	 */
	public FileManager()
	{}
	
	public void initConsole(ServerConsole console)
	{
		this.console = console;
	}
	
	/**
	 * Open the file and check it exists. If not, create it and write
	 * the columns to the top of the file.
	 */
	public void initLogFile()
	{
		File f = new File("log.txt");
		if(!f.exists())
		{
			try
			{
				fWriterLog = new FileWriter("log.txt", true);
				fWriterLog.append("Time, Tag, Method, Encryption, ");
				fWriterLog.append("Source Username, Source Address, " +
						"Source Port, ");
				fWriterLog.append("Destination Username, Destination Address, " +
					" Destination Port, ");
				fWriterLog.append("Contents, Message Size, Transmission Time\r\n");
				fWriterLog.close();
			}
			catch(IOException existException)
			{
				console.printError("Log file could not be created.");
				
			}
		}
	}
	
	public void initPasswordsFile() {
		File f = new File("passwords.txt");
		if (!f.exists()) {
			try {
				fWriterPasswords = new FileWriter("passwords.txt", true);
			} catch (IOException e) {
				console.printError("Passwords file could not be created");
			}
		}
	}
	
	/**
	 * Write a message to the log file.
	 * @param time Time that the message was sent.
	 * @param tag The packet tag.
	 * @param method The method of transmission (UDP, etc).
	 * @param sUsername The username of sender.
	 * @param sAddress The address of sender.
	 * @param sPort The port of sender.
	 * @param dUsername The username of recipient.
	 * @param dAddress The address of recipient.
	 * @param dPort The port of recipient.
	 * @param contents The contents of message.
	 * @param size The size of the message.
	 * @param transTime The time taken to transmit.
	 */
	public void storeMessageToLogFile(String time, String tag, String method, 
			String encryption, String sUsername, String sAddress, String sPort,
			String dUsername,String dAddress, String dPort, 
			List<String> contents, String size, String transTime)
	{
		try
		{
			fWriterLog = new FileWriter("log.txt", true);
			fWriterLog.append(time + ", " + tag + ", " + method + ", "
					+ encryption + ", ");
			fWriterLog.append(sUsername + ", " + sAddress + ", " + sPort 
					+ ", ");
			fWriterLog.append(dUsername + ", " + dAddress + ", " + dPort 
					+ ", ");
			fWriterLog.append(contents + ", " + size + ", " + transTime +
					"\r\n");
			fWriterLog.close();
		}
		catch(IOException ioException)
		{
			console.printError("An IO error occured when logging a message.");
		}
	}
}

