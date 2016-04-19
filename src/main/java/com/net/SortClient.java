/*
 * @author Abhijeet Sharma
 * @version 1.0  
 * @since April 8, 2016 
 */

package com.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.main.ConfigParams;

public class SortClient {
	public static final Logger LOG = LoggerFactory.getLogger("client");
	
	public static void start() {
		
		InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap boot = new Bootstrap();
			boot.group(group)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new SortClientInitializer());
					}
				});
				//.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
				//.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);
			LOG.info("Client connecting to {}:{}", ConfigParams.MASTER_ADDRESS, ConfigParams.PORT);
			// Start the client.
			ChannelFuture f = boot.connect(new InetSocketAddress(ConfigParams.MASTER_ADDRESS, ConfigParams.PORT)).sync();			
			// Wait until the connection is closed.
			f.channel().closeFuture().sync();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// The connection is closed automatically on shutdown.
			group.shutdownGracefully();
			LOG.info("Client Exit.");
		}
	}
	/*public static void main(String[] args) throws Exception {		
		SortClient.start();
	}*/
}