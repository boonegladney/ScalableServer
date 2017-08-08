package cs455.scaling.threadpool;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import cs455.scaling.task.*;
import cs455.scaling.util.*;

public class WorkerThread implements Runnable{
	
	private ThreadPool pool;
	private volatile Task task; //the task that the thread needs to work on.
	private TaskFactory taskFactory;
	private int threadID; // TEST CODE
	
	
	/**
	 * Constructor for the work thread.
	 * Sets task to null;
	 */
	public WorkerThread(ThreadPool pool)
	{
		this.pool = pool;
		this.task = null;
		taskFactory = TaskFactory.getTaskFactory();
		Random rand = new Random();
		threadID = rand.nextInt(1000); // TEST CODE
	}
	
	/**
	 * The run method execute a task when a task is available.
	 */
	public synchronized void run() {
		while(true)
		{
			if(task != null)
			{
				try {
					handleTask();
				} catch (IOException | NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Sends task to proper method for processing. The method
	 * that the task gets sent to depends on the type of task
	 * being processed.
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	private void handleTask() throws NoSuchAlgorithmException, IOException
	{
		if(task.getTaskType() == 0) // junk task
		{
			processJunk((Junk)taskFactory.getTask(task));
		}
		else if(task.getTaskType() == 1) // read task
		{
			Read read = (Read)task;
			readData(read);
		}
		else if(task.getTaskType() == 2) // write task
		{
			Write write = (Write)task;
			writeData(write);
		}
		else
		{
			System.out.println("Task Not Recognized By WorkerThread...");
		}
	}
	
	private void writeData(Write write) throws IOException
	{
		//get info from key
		SelectionKey key = write.getKey();
		SocketChannel inputChannel = (SocketChannel)key.channel();
		
		//get write order
		WriteOrder order = pool.getWriteOrder(inputChannel);
		
		//send hash size
		ByteBuffer sizeBuf = ByteBuffer.allocateDirect(4);
		sizeBuf.putInt(order.getData().length);
		sizeBuf.flip();
		inputChannel.write(sizeBuf);
		
		//send hash
		inputChannel.write(ByteBuffer.wrap(order.getData()));
		
		key.interestOps(SelectionKey.OP_READ);
		nullifyTask();
		pool.returnToPool(this);
		key.selector().wakeup();
	}
	
	/**
	 * Reads the data from the info given by the read task. It will produce
	 * a hash of the data received, add a WriteOrder the array in the ThreadPool
	 * and will register the channel with the selector with an OP_WRITE
	 * SelectionKey.
	 * @param read
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private void readData(Read read) throws IOException, NoSuchAlgorithmException
	{
		SelectionKey key = read.getKey();
		SocketChannel channel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(8000);
		
		int hasRead = 0;
		
		try {
			while(buffer.hasRemaining()) // && hasRead != 1)
			{
				hasRead = channel.read(buffer);
			}
		} catch(IOException e)
		{
			key.cancel(); // CHECK THIS
			channel.close();
			return;
		}
		if(hasRead == -1)
		{
			key.cancel(); // CHECK THIS
			channel.close();
			return;
		}
		
		// ---------- DO SOMETHING WITH BYTEBUFFER ------------------------------
		
		// read from buffer
		buffer.flip();
		byte[] randBytes = new byte[8000];
		buffer.get(randBytes);
		
		// hash data
		String hash = SHA1FromBytes(randBytes);
		
		// add write order
		WriteOrder writeOrder = new WriteOrder(channel, hash.getBytes());
		pool.registerWriteOrder(writeOrder);
		
		// HouseKeeping...
		pool.incrementReceived();
		nullifyTask();
		pool.returnToPool(this);
		key.interestOps(SelectionKey.OP_WRITE);
		key.selector().wakeup();
	}
	
	private String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA1");
		byte[] hash = digest.digest(data);
		BigInteger hashInt = new BigInteger(1, hash);
		
		return hashInt.toString(16);
	}
	
	/**
	 * processes a junk task. It simply prints the integer payload
	 * that is stored in the task object.
	 * @param junk
	 */
	private void processJunk(Junk junk)
	{
		System.out.println("Thread: " + threadID + " Processed junk payload of: " + junk.getPayload());
		nullifyTask();
		pool.returnToPool(this);
	}
	
	// MUTATORS
	
	/**
	 * Sets the worker threads task to null
	 */
	private void nullifyTask()
	{
		task = null;
	}
	
	/**
	 * Sets the task variable so the thread can begin processing it.
	 * @param task - the task to be processed by the thread.
	 */
	public synchronized void setTask(Task task)
	{
		this.task = task;
		notify();
	}
	
	// ACCESSORS

}
