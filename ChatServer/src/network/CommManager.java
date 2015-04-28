package network;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

public interface CommManager {
	
	public void initSocket() throws IOException;
	public void initSocket(String address, String port) throws IOException;
	public String getServerAddress() throws UnknownHostException;
	public int getServerPort();
	public List<String> receivePacket() throws IOException, ClassNotFoundException;
	public int sendPacket(String rcvAddress, int rcvPort, String header, 
			List<String> contents) throws IOException;
	public void clearMsgCache();
}
