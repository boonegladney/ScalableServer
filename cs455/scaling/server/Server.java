package cs455.scaling.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.InetAddress;

import cs455.scaling.threadpool.*;
import cs455.scaling.task.*;

public class Server {
	// global variables
	private ThreadPoolManager TPM;
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	private final AtomicInteger numberOfMessagesReceived = new AtomicInteger();
	private final AtomicInteger numberOfClients = new AtomicInteger();
	
	public Server(int poolSize)
	{
		ThreadPool pool = new ThreadPool(poolSize, this);
		TPM = new ThreadPoolManager(pool);
	}
	
	// ------------THREAD SAFE ACCESSES AND MUTATIONS OF DATA TRACKING VARIABLES -----------------
	public void incrementReceived()
	{
		numberOfMessagesReceived.incrementAndGet();
	}
	
	public int getNumMessagesReceived()
	{
		return numberOfMessagesReceived.getAndSet(0);
	}
	
	public int getNumberOfClients()
	{
		return numberOfClients.get();
	}
	//---------------------------------------------------------------------------------------------
	
	public void read(SelectionKey key)
	{
		key.interestOps(0); // DEREGISTER KEY OPS
		Read read = new Read(key);
		TPM.receiveTask(read);
	}
	
	public void write(SelectionKey key)
	{
		key.interestOps(0); // DEREGISTER KEY OPS
		Write write = new Write(key);
		TPM.receiveTask(write);
	}
	
	public void accept(SelectionKey key) throws IOException
	{
		ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
		SocketChannel channel = serverSocket.accept();
		
		System.out.println("A client is connecting to the Server...");
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
		
		numberOfClients.incrementAndGet();
	}
	
	public void start(int port) throws IOException
	{	
		// create thread pool
		Thread TPMThread = new Thread(TPM);
		TPMThread.start();
		
		// create data counter
		ServerDataCounter dataCounter = new ServerDataCounter(this);
		Thread dataCounterThread = new Thread(dataCounter);
		dataCounterThread.start();
		
		//initialize the serverSocketChannel and selector
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), port));;
		selector = Selector.open();
		serverSocketChannel.configureBlocking(false);
		
		// register the channel with the selector
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		while(true)
		{
			int trigger = this.selector.select();
			if(trigger == 0) continue; // saves some empty computations. Not much I dont think, but i may increase performance.
			Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
			while(keys.hasNext())
			{
				SelectionKey key = keys.next();
				keys.remove();
				
				if(key.isAcceptable())
				{
					accept(key);
				}
				
				if(key.isReadable())
				{
					read(key);
				}
				
				if(key.isWritable())
				{
					write(key);
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException
	{	
		// check to see if the correct number of arguments are available
		if(args.length != 2)
		{
			System.out.println("FAILED TO SETUP SERVER");
			System.out.println("Arg Usage: <port number> <thread pool size>");
			System.exit(1);
		}
		
		Server server = new Server(Integer.parseInt(args[1]));
		server.start(Integer.parseInt(args[0]));

	}
}
