package cs455.scaling.task;

import java.nio.channels.SelectionKey;

public class Write implements Task{
	private int taskType = 2;
	private SelectionKey key;
	
	public Write(SelectionKey key)
	{
		this.key = key;
	}
	
	/**
	 * Returns the taskType.
	 */
	@Override
	public int getTaskType() {
		return taskType;
	}
	
	/**
	 * This method returns the key variable stored.
	 * @return the SelectionKey stored.
	 */
	public SelectionKey getKey()
	{
		return key;
	}
}
