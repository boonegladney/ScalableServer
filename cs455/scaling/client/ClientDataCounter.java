package cs455.scaling.client;

import java.sql.Timestamp;

public class ClientDataCounter implements Runnable{
	private Client client;
	
	public ClientDataCounter(Client client)
	{
		this.client = client;
	}

	@Override
	public void run() {
		while(true)
		{
			int sent = client.getSentMessages();
			int received = client.getReceivedMessages();
			
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			
			System.out.printf("%s   Total Sent Count: %d   Total Received Count: %d\n", timestamp.toString(), sent, received);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}