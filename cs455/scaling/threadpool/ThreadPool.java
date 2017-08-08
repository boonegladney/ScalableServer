package cs455.scaling.threadpool;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;

import java.util.Random;

import cs455.scaling.task.*;
import cs455.scaling.server.*;
import cs455.scaling.util.*;

public class ThreadPool {
	private volatile ArrayList<WriteOrder> writeOrders = new ArrayList<WriteOrder>();
	private volatile LinkedList<WorkerThread> workerThreads; // the linked list of worker threads.
	private volatile boolean fullyInitialized = false;
	private Server server;
	
	/**
	 * This method starts the requested number of threads and adds
	 * each one to the workerThreads linked list.
	 * @param numOfThreads - The number of threads to create.
	 */
	public ThreadPool(int numOfThreads, Server server)
	{
		this.server = server;
		workerThreads = new LinkedList<WorkerThread>();
		//for the number of threads, create a new thread and add it to the workerThreads linked list
		for(int i = 0; i < numOfThreads; i++)
		{
			WorkerThread workerThread = new WorkerThread(this);
			Thread thread = new Thread(workerThread);
			thread.start();
			workerThreads.add(workerThread);
		}
		fullyInitialized = true;
	}
	
	/**
	 * This method takes a Task object and give the task to
	 * the first WorkerThread in the workerThreads linked list.
	 * The WorkerThread will be removed from the pool
	 * @param task - the Task to be given to a WorkerThread
	 */
	public synchronized void receiveTask(Task task)
	{
		WorkerThread nextThread = workerThreads.pollFirst();
		nextThread.setTask(task);
	}
	
	/**
	 * This method will return the given WorkerThread to the
	 * the pool by adding the given workerThread to the end
	 * of the workerThreads linked list.
	 * @param workerThread - the WorkerThread to be returned to the pool.
	 */
	public synchronized void returnToPool(WorkerThread workerThread)
	{
		workerThreads.addLast(workerThread);
	}
	
	/**
	 * This method checks if a WorkerThread is available to take
	 * a task.
	 * @return true if there is an available WorkerThread and false otherwise.
	 */
	public synchronized boolean workersAvailable()
	{
		return !(workerThreads.isEmpty());
	}
	
	public void registerWriteOrder(WriteOrder writeOrder)
	{
		synchronized(writeOrders)
		{
			writeOrders.add(writeOrder);
		}
	}
	
	/**
	 * Returns the first WriteOrder that is found in the writeorders
	 * ArrayList that contains the same channel as the argument. It will
	 * print an error if one is not found, and will return null.
	 * @param channel - the channel to be compared to.
	 * @return - A valid WriteOrder, or null if none were found.
	 */
	public WriteOrder getWriteOrder(SocketChannel channel)
	{
		synchronized(writeOrders)
		{
			for(int i = 0; i < writeOrders.size(); i++)
			{
				if(writeOrders.get(i).getChannel().equals(channel))
				{
					WriteOrder tmp = writeOrders.get(i);
					writeOrders.remove(i);
					return tmp;
				}
			}
			return null;
		}
	}
	
	/**
	 * Returns whether a write order exists in the writeOrders
	 * ArrayList that contains the same channel as the argument.
	 * @param channel - The channel to be compared to.
	 * @return - true if a valid WriteOrder was found, and false otherwise.
	 */
	public boolean writeOrderExists(SocketChannel channel)
	{
		synchronized(writeOrders)
		{
			for(int i = 0; i < writeOrders.size(); i++)
			{
				if(writeOrders.get(i).getChannel().equals(channel))
				{
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Used by ThreadPoolManager to ensure that the Thread Pool
	 * has been fully initialized before using it.
	 * @return - true if all threads have been initialized and started.
	 */
	public boolean isInitialized()
	{
		return fullyInitialized;
	}
	
	public void incrementReceived()
	{
		server.incrementReceived();
	}
}