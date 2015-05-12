package file;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	private Map<String, String> users = new HashMap<String, String>();
	
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
	
	public void initPasswordsFiles() {
		List<String> fileContents = new ArrayList<String>();
		String username, password;
		
		File f = new File("passwords.txt");
		if (!f.exists()) {
			try {
				fWriterPasswords = new FileWriter("passwords.txt", true);
			} catch (IOException e) {
				console.printError("Passwords file could not be created");
			}
		}
		try {
			fileContents = Files.readAllLines(Paths.get(f.getAbsolutePath()), Charset.defaultCharset());
		} catch (IOException e) {
			console.printError("Passwords file could not be read from");
			return;
		}
		// Store the usernames and passwords in the Map.
		for (String s : fileContents) {
			String[] ids = s.split("");
			username = ids[0];
			password = ids[1];
			users.put(username, password);
		}
		console.printMessage("The passwords file was loaded.");
	}
	
	/* Get the password, in hashed form, for a particular user. */
	public String getPassword(String username) {
		return users.get(username);
	}
	
	/* Stores a username and hashed password into the Map. */
	public void storeUserAndPassword(String username, String password) {
		users.put(username, password);
		try {
			fWriterPasswords.write(username + ":" + password);
		} catch (IOException e) {
			System.out.println("The username and password could not be stored in the passwords file.");
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

