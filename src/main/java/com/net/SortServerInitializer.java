package com.net;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * This class initializes the pipeline for the server.
 * @author Abhijeet Sharma
 * @version 1.0  
 * @since April 8, 2016 
 */
public class SortServerInitializer extends ChannelInitializer<SocketChannel> {
	/**
	 * Initialized the channel pipeline.
	 */
	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();
		pipeline.addLast("server_decoder", new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)));  
		pipeline.addLast("server_encoder", new ObjectEncoder());	
		pipeline.addLast("server_handler", new SortServerHandler());
	}
}