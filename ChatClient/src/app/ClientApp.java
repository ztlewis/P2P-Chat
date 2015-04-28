package app;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import dht.*;
import network.*;
import view.*;
import model.*;
import file.*;

/**
 * The driver class that contains the main method and pulls all components together.
 * @author Alex
 * @version 0.3
 * @since 0.1
 */
public class ClientApp {
    
    public static void main(String[] args) {
       
    	//Initializing each component of the Client Application.
        ClientNetwork network = new ClientNetwork();
        ClientModel model = new ClientModel();
        ClientConsole console = new ClientConsole();
        FileManager file = new FileManager();
        ClientLogin login = new ClientLogin();
        ClientView view = new ClientView();
        DHTNetwork dht = new DHTNetwork();
        
        //Passing references to each other.
        model.initNetwork(network);
        model.initConsole(console);
        model.initView(view);
        model.initDHT(dht);
        model.initLogin(login);
        console.initModel(model);
        network.initModel(model);
        network.initConsole(console);
        network.initFile(file);
        network.initDHT(dht);
        login.initNetwork(network);
        login.initView(view);
        login.initModel(model);
        login.initDHT(dht);
        view.initModel(model);
        view.refreshPeerList(model.getPeerList());
        view.initDHT(dht);
        dht.initNetwork(network);
        file.initConsole(console);
    }

}
