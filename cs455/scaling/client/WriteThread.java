package cs455.scaling.client;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.locks.Lock;

public class WriteThread implements Runnable{
	private Lock IOLock;
	private SocketChannel channel;
	private Client client;
	int frequency;
	
	public WriteThread(Client client, SocketChannel channel, int frequency, Lock IOLock)
	{
		this.client = client;
		this.channel = channel;
		this.frequency = frequency;
		this.IOLock = IOLock;
	}
	
	@Override
	public void run() {
		while(channel.isConnected())
		{
			Random rand = new Random();
			byte[] randBytes = new byte[8000];
			rand.nextBytes(randBytes);
			try {
				String hash = SHA1FromBytes(randBytes);
				//System.out.println("HASH STRING " + hash.length()); TEST CODE
				//System.out.println("HASH SIZE " + hash.getBytes().length); TEST CODE
				client.addHash(hash);
				//System.out.println("ATTEMPTING TO LOCK"); //TEST CODE
				IOLock.lock();
				//System.out.println("WRITING"); // TEST CODE
				channel.write(ByteBuffer.wrap(randBytes));
				IOLock.unlock();
				client.incrementSentMessages();
				Thread.sleep(1000/4);
			} catch (IOException | InterruptedException | NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * computes a hash from a byte array.
	 * @param data - The byte array to be hashed
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA1");
		byte[] hash = digest.digest(data);
		BigInteger hashInt = new BigInteger(1, hash);
		
		return hashInt.toString(16);
	}

}