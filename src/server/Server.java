package server;

//  Hello World server in Java
//  Binds REP socket to tcp://*:port
//  Expects "Hello" from client, replies with "World"

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import misc.CommandWrapper;

import org.msgpack.MessagePack;
import org.zeromq.ZMQ;

public class Server implements Runnable {

	ZMQ.Context context = null;
	ZMQ.Socket socket = null;

	public void bind(String address) {
		this.context = ZMQ.context(1);
		this.socket = context.socket(ZMQ.REP);

		this.socket.bind(address);
	}

	public String executeMethod(CommandWrapper command)
	{
		String methodName = command.getMethodName();
		String reply = null;
		if(methodName.equalsIgnoreCase("sayhellotoserver"))
		{
			reply = sayHelloToServer();
		}
		return reply;
	}
	
	public String sayHelloToServer()
	{
		//System.out.println("Received Hello");
		return "Hello World";
	}
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				// Wait for next request from the client
				byte[] request = socket.recv(0);
				//extract methodname from request
				final MessagePack packer = new MessagePack();
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final CommandWrapper command = packer.createUnpacker(new ByteArrayInputStream(request)).read(new CommandWrapper());

                packer.write(out, executeMethod(command));
                this.socket.send(out.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
				//

				// Do some 'work'
				//this.wait(1000);

				// Send reply back to client
				//String reply = "World";
		}

		socket.close();
		context.term();
	}
	
}
