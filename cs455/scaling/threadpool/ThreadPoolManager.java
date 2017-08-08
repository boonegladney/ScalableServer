package cs455.scaling.threadpool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs455.scaling.task.*;

public class ThreadPoolManager implements Runnable{
	private volatile LinkedList<Task> tasks; //linked list of tasks;
	private ThreadPool pool;
	private Lock taskLock = new ReentrantLock();
	
	public void run() {
		while(true)
		{
			taskLock.lock();
			if(pool.workersAvailable() && !tasks.isEmpty())
			{
				assignTask();
			}
			taskLock.unlock();
		}
		
	}
	
	/**
	 * Initalizes the ThreadPool and the LinkedList of tasks.
	 * @param sizeOfThreadPool - The number of Threads to be created in the thread pool.
	 */
	public ThreadPoolManager(ThreadPool pool)
	{
		this.tasks = new LinkedList<Task>();
		this.pool = pool;
	}
	
	/**
	 * Adds a task to the tasks linked list.
	 * @param task - the task to be added to the linked list.
	 */
	public void receiveTask(Task task)
	{
		taskLock.lock();
		tasks.add(task);
		taskLock.unlock();
	}
	
	/**
	 * Gives a task to the thread pool to be processed.
	 */
	private void assignTask()
	{
		Task task = tasks.removeFirst();
		pool.receiveTask(task);
	}
	
	/**
	 * Main function used for testing the ThreadPoolManager.
	 * @param args
	 */
	public static void main(String[] args)
	{	
		ThreadPool pool = new ThreadPool(10, null);
		ThreadPoolManager manager = new ThreadPoolManager(pool);
		Thread thread = new Thread(manager);
		while(!manager.pool.isInitialized()){continue;}
		thread.start();
		
		
		
		ArrayList<Junk> junk = new ArrayList<Junk>();
		
		junk.add(new Junk(1));
		junk.add(new Junk(2));
		junk.add(new Junk(3));
		junk.add(new Junk(4));
		junk.add(new Junk(5));
		junk.add(new Junk(6));
		junk.add(new Junk(7));
		junk.add(new Junk(8));
		junk.add(new Junk(9));
		junk.add(new Junk(10));
		junk.add(new Junk(11));
		junk.add(new Junk(12));
		junk.add(new Junk(13));
		junk.add(new Junk(14));
		junk.add(new Junk(15));
		junk.add(new Junk(16));
		junk.add(new Junk(17));
		junk.add(new Junk(18));
		junk.add(new Junk(19));
		junk.add(new Junk(20));
		
		for(int i = 0; i < junk.size(); i++)
		{
			manager.receiveTask(junk.get(i));
		}
	}
	
}
