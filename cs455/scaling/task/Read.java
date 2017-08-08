package cs455.scaling.task;

import java.nio.channels.SelectionKey;

public class Read implements Task{
	private final int taskType = 1;
	SelectionKey key;
	
	public Read(SelectionKey key)
	{
		this.key = key;
	}
	
	@Override
	public int getTaskType() {
		return taskType;
	}
	
	public SelectionKey getKey()
	{
		return key;
	}

}