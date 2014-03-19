package test;

import client.Client;
import server.Server;

public class TestZeroRPC {

	public static void main (String args[]) {
		//instead of new objects try object-pool
		Server service = new Server();
	    service.bind("tcp://*:4242");
	    //service.run();
	    new Thread(service).start();

	    Client client = new Client();
	    client.connect("tcp://localhost:4242");
	    client.executeFunction("sayHelloToServer");
	}
	
}
