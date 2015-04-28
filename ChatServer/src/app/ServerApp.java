package app;

import network.*;
import view.*;
import model.*;
import file.*;

/**
 * The driver class that contains the main method and pulls the view, 
 * model and network components together.
 * @author Alex
 * @version 0.2
 * @since 0.1
 */
public class ServerApp {

	public static void main(String[] args) {

		ServerNetwork network = new ServerNetwork();
		ServerModel model = new ServerModel();
		ServerConsole console  = new ServerConsole();
		//CommManager comm = new UDPPacketManager();
		ServerConnections connection = new ServerConnections();
		FileManager file = new FileManager();
		ServerLogin login = new ServerLogin();
		ServerView view = new ServerView();
		
		network.initModel(model);
		network.initConsole(console);
		//network.initComm(comm);
		network.initConnections(connection);
		network.initFile(file);
		connection.initModel(model);
		connection.initConsole(console);
		connection.initNetwork(network);
		//view.initComm(comm);
		login.initNetwork(network);
		login.initView(view);
		console.initNetwork(network);
		console.initView(view);
		file.initConsole(console);
		/*
		if(args.length == 4)
		{
			network.initSocket(args[0], args[1]);
			network.setPresDetails(args[2], args[3]);
		}
		else
		{
			network.initSocket();
		}
		*/
	}

}
