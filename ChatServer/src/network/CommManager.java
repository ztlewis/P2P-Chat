package network;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

public interface CommManager {
	void initSocket() throws IOException;
	void initSocket(String address, int port) throws IOException;
	String getServerAddress() throws UnknownHostException;
	int getServerPort();
	List<String> receivePacket() throws IOException, ClassNotFoundException;
	int sendPacket(String rcvAddress, int rcvPort, String header, List<String> contents) throws IOException;
	void clearMsgCache();
}
