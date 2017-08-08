package cs455.scaling.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.Lock;

import cs455.scaling.task.*;

public class ReadThread implements Runnable{
	private Lock IOLock;
	private volatile boolean done;
	private volatile Read readTask = null;
	private Client client;
	
	public ReadThread(Client client, Lock IOLock)
	{
		this.client = client;
		done = true;
		this.IOLock = IOLock;
	}
	
	@Override
	public synchronized void run() {
		while(true)
		{
			if(done)
			{
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				// do the read
				int hasRead = 0;
				SelectionKey key = readTask.getKey();
				SocketChannel channel = (SocketChannel)key.channel();
				ByteBuffer buf;
				int hashSize = 0;
				try {	
					IOLock.lock();
					
					// read hash size
					ByteBuffer sizeBuf = ByteBuffer.allocateDirect(4);
					int temp = 0;
					while(sizeBuf.hasRemaining())
					{
						temp = channel.read(sizeBuf);
					}
					//System.out.println("READ SIZE ELEMENTS " + temp); // TEST CODE
					sizeBuf.flip();
					hashSize = sizeBuf.getInt();
					//System.out.println("READ HASHSIZE " + hashSize); // TEST CODE
					
					// read hash
					buf = ByteBuffer.allocateDirect(hashSize);
					//System.out.println(buf.hasRemaining()); // TEST CODE
					while(buf.hasRemaining())
					{
						hasRead = channel.read(buf);
					}
					//System.out.println("DONE READING " + hasRead + " " + buf.hasRemaining()); // TEST CODE
				} catch(IOException e) //CATCH ERRORS -----------------------------------------
				{
					e.printStackTrace();
					key.cancel();
					try {
						channel.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					return;
				} finally
				{
					IOLock.unlock();
				}
				if(hasRead == -1)
				{
					key.cancel();
					try {
						channel.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				//-------------------------------------------------------------------------------
				
				buf.flip();
				byte[] hash = new byte[hashSize];
				buf.get(hash);
				client.removeHash(hash);
				done = true;
				client.incrementReceivedMessages();
				key.interestOps(SelectionKey.OP_READ);
				key.selector().wakeup();
			}
		}
		
	}
	
	public synchronized void setDone(boolean isDone, Read read)
	{
		done = isDone;
		if(!done)
		{
			readTask = read;
			notify();
		}
	}

}