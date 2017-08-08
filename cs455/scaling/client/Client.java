package cs455.scaling.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs455.scaling.task.*;

public class Client {
	private Lock hashLock = new ReentrantLock();
	private Lock IOLock = new ReentrantLock();
	private Selector selector;
	private SocketChannel channel;
	private ArrayList<String> hashes;
	private WriteThread writeThread;
	private ReadThread readThread;
	private final AtomicInteger numberOfReceivedMessages = new AtomicInteger();
	private final AtomicInteger numberOfSentMessages = new AtomicInteger();
	
	public Client() throws IOException, InterruptedException
	{
		hashes = new ArrayList<String>();
		//startClient(); why is this here?
	}
	
	// ------------THREAD SAFE ACCESSES AND MUTATIONS OF DATA TRACKING VARIABLES -----------------
	public void incrementReceivedMessages()
	{
		numberOfReceivedMessages.incrementAndGet();
	}
	
	public int getReceivedMessages()
	{
		return numberOfReceivedMessages.getAndSet(0);
	}
	
	public void incrementSentMessages()
	{
		numberOfSentMessages.incrementAndGet();
	}
	
	public int getSentMessages()
	{
		return numberOfSentMessages.getAndSet(0);
	}
	//-----------------------------------------------------------------------------------------------
	
	/**
	 * Connects to the server and gets a SocketChannel object. It then
	 * will start the write thread and read thread.
	 * @param key
	 * @throws IOException
	 */
	public void connect(SelectionKey key, int frequency) throws IOException
	{
		SocketChannel channel = (SocketChannel) key.channel();
		if(channel.isConnectionPending())
		{
			channel.finishConnect();
		}
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
		
		ClientDataCounter dataCounter = new ClientDataCounter(this);
		Thread dataCounterThread = new Thread(dataCounter);
		dataCounterThread.start();
		
		writeThread = new WriteThread(this, channel, frequency, IOLock);
		Thread threadOne = new Thread(writeThread);
		threadOne.start();
		
		readThread = new ReadThread(this, IOLock);
		Thread threadTwo = new Thread(readThread);
		threadTwo.start();
		
		selector.wakeup();
	}
	
	public void read(SelectionKey key)
	{
		key.interestOps(0);
		Read read = new Read(key);
		readThread.setDone(false, read);
	}
	
	public void startClient(String host, int port, int frequency) throws IOException, InterruptedException
	{
		selector = Selector.open();
		channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_CONNECT);
		channel.connect(new InetSocketAddress(host, port));
		
		while(true)
		{
			selector.select();
			
			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while(keys.hasNext())
			{
				SelectionKey key = keys.next();
				keys.remove();
				
				if(key.isConnectable())
				{
					connect(key, frequency);
				}
				if(key.isReadable())
				{
					read(key);
				}
			}
		}
	}
	
	/**
	 * Adds a computed hash to the hashes arrayList.
	 * @param hash - the hash string to be added.
	 */
	public void addHash(String hash)
	{
		synchronized(hashLock)
		{
			hashes.add(hash);
		}
	}
	
	/**
	 * Finds the matching hash in the hashes ArrayList and removes it.
	 * if the has is not found, then it prints an error message.
	 * @param hashBytes
	 */
	public void removeHash(byte[] hashBytes)
	{
		synchronized(hashLock)
		{
			String hash = new String(hashBytes);
			for(int i = 0; i < hashes.size(); i++)
			{
				if(hashes.get(i).equals(hash))
				{
					//System.out.println("HASH MATCHED"); // TEST CODE
					hashes.remove(i);
					return;
				}
			}
			System.out.println("ERROR! HASH NOT FOUND");
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException
	{
		Client client = new Client();
		client.startClient(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
	}
}
