package cs455.scaling.util;

import java.nio.channels.SocketChannel;

public class WriteOrder {
	private SocketChannel channel;
	private byte[] data;
	
	/**
	 * constructs the WriteOrder
	 * @param channel
	 * @param data
	 */
	public WriteOrder(SocketChannel channel, byte[] data)
	{
		this.channel = channel;
		this.data = data;
	}
	
	/**
	 * Getter method for the SocketChannel
	 * @return - the SocketChannel
	 */
	public SocketChannel getChannel()
	{
		return channel;
	}
	
	/**
	 * Getter method for the byte[] data
	 * @return - the data byte array.
	 */
	public byte[] getData()
	{
		return data;
	}
}
