package cs455.scaling.task;

/**
 * This is a class specifically used for testing
 * @author superbooneman
 *
 */

public class Junk implements Task{
	private final int taskType = 0;
	private int payload;
	
	/**
	 * Constructor
	 * @param payload
	 */
	public Junk(int payload)
	{
		this.payload = payload;
	}
	
	/**
	 * Copy constructor
	 * @param payload - the payload to be stored in junk.
	 */
	public Junk(Junk junk)
	{
		this.payload = junk.payload;
	}
	
	/**
	 * @return - the int representing the task Junk.
	 */
	public int getTaskType() {
		// TODO Auto-generated method stub
		return taskType;
	}
	
	/**
	 * 
	 * @return - the junk payload
	 */
	public int getPayload()
	{
		return payload;
	}

}
