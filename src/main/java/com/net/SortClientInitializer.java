package com.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * This class initializes the pipeline for the Client.
 * @author Abhijeet Sharma
 * @version 1.0  
 * @since April 8, 2016 
 */
public class SortClientInitializer extends ChannelInitializer<SocketChannel> {
	/**
	 * Initialized the channel pipeline.
	 */
	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();
		pipeline.addLast("client_decoder", new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));  
		pipeline.addLast("client_encoder", new ObjectEncoder());
		pipeline.addLast("client_handler", new SortClientHandler());
	}
}