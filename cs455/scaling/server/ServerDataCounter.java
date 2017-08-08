package cs455.scaling.server;

import java.sql.Timestamp;

public class ServerDataCounter implements Runnable{
	private Server server;
	
	public ServerDataCounter(Server server)
	{
		this.server = server;
	}

	@Override
	public void run() {
		while(true)
		{
			int received = server.getNumMessagesReceived();
			int clients = server.getNumberOfClients();
			double fraction = ((double) received)/5.0;
			
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.printf("%s  Current Server Throughput: %.2f/s  Active Client Connections: %d\n", timestamp.toString(), fraction, clients);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
}
