Descriptions of individual files:

client package:

	- Client.java -> This class is the main hub for the client. It sets up 
	the inital connection to the server and starts all the threads necessary 
	for the client operations.
	
	- ReadThread.java -> This is a Thread that starts when the client connects 
	and will be given work by the client to read data messages from the channel
	connected to the server.
	
	- WriteThread.java -> This is a thread that creates random byte arrays and
	sends them to the server at a constant rate.
	
	- ClientDataCount.java -> This is a thread that will periodically gather and
	reset data from the client about its tasks and will print this inforation to
	the console.
	
server package:

	- Server.java -> This is the main server program that creates the thread pool
	and will send tasks to the pool to be processed. It contains the selector and
	handles the channel selection process.
	
	- ServerDataCount.java -> This is a thread that will periodically gather and
	reset data from the server about its tasks and will print this information to
	the console.
	
task package:

	- Junk.java -> This is a test task used to test the thread pool.
	
	- Read.java -> This is a task that is meant to instruct a worker
	thread to read data from a particular channel.
	
	- Write.java -> This is a task that is meant to instruct a worker
	thread to write data to a particular channel.
	
	- TaskFactor.java -> This is a factory class that will convert a
	Task object to the correct corresponding object of type Read, Write,
	or Junk.
	
	- Task.java -> This is an interface for all tasks to inherhit.
	
threadpool package:

	- ThreadPool.java -> This is my actual thread pool implementation. It handles
	direct interactions with worker threads.
	
	- ThreadPoolManager.java -> This is a manager for my thread pool, which is what
	the server uses to interact with the pool.
	
	- WorkerThread.java -> This is the thread that the threadpool stores multiple of.
	It is capable of handling all task types that my program uses. Additional function-
	ality could very easily be added.
	
util package:

	- WriteOrder.java -> This is a data structure that holds information about a
	particular write that needs to happen so that when a write task is received
	a corresponding WriteOrder can be found with the necessary info for the write.
	
	
	
ADDITIONAL NOTES:

-- server arguments are <server port>  <number of pool threads>
-- client arguments are <server host>  <server port>  <frequency of message sending>

-- IMPORTANT: If a client receives a hash that it does not recognize, it will print
	an error to the console to that effect. If no errors are printed, then all hashes
	are being validated as they come in.
	
-- Some code is commented out, and some code might be obsolute (I think I removed such
	code segments though).
