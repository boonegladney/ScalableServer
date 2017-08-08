package cs455.scaling.task;

public class TaskFactory {
	private static TaskFactory taskFactory = new TaskFactory();
	
	public static TaskFactory getTaskFactory()
	{
		return taskFactory;
	}
	
	/**
	 * This method will retrive the proper object type of the Task
	 * @param task - the task to be converted.
	 * @return The actual object of the task
	 */
	public Task getTask(Task task)
	{
		int taskType = task.getTaskType();
		switch(taskType)
		{
		case 0:
			Junk junk = new Junk((Junk)task);
			return junk;
		default: 
			return null;
		}
	}
}