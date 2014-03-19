package client;

import misc.CommandWrapper;

import org.msgpack.MessagePack;
import org.zeromq.ZMQ;

/**
 * Hello World client in Java Connects REQ socket to tcp://serverip:port Sends
 * "CommandMethodName" to server, expects "Hello World" back
 * 
 * @author Dhananjay
 */
public class Client {
	ZMQ.Context context = null;
	ZMQ.Socket socket = null;

	public void connect(String address) {
		this.context = ZMQ.context(1);
		this.socket = context.socket(ZMQ.REQ);
		// Socket to talk to server
		this.socket.connect(address);
	}

	/**
	 * This method is a generalized function which creates command an sets the
	 * methodName
	 * 
	 * @param methodName
	 */
	public void executeFunction(String methodName) {
		try {
			MessagePack msgpack = new MessagePack();
			CommandWrapper command = new CommandWrapper();
			command.setMethodName(methodName);

			byte raw[] = msgpack.write(command);
			socket.send(raw, 0);

			byte[] reply = socket.recv(0);
			System.out.println("Received from server " + new String(reply));
		} catch (Exception e) {
			e.printStackTrace();
		}

		 /*for (int requestNbr = 0; requestNbr != 10; requestNbr++) { try {
		 String request = "Hello"; System.out.println("Sending Hello " +
		 requestNbr);
		  
		 byte raw[] = msgpack.write(request); socket.send(raw, 0);
		  
		 byte[] reply = socket.recv(0); System.out.println("Received " + new
		 String(reply) + " " + requestNbr); } catch (Exception e) {
		 e.printStackTrace(); break; } }*/
	}
}