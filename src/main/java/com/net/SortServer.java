/*
 * @author Abhijeet Sharma
 * @version 1.0  
 * @since April 8, 2016 
 */

package com.net;

import java.net.InetSocketAddress;

import com.main.ConfigParams;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class SortServer {

	public static void start() throws InterruptedException {
		// configure the server
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap boot = new ServerBootstrap();
			boot.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel sChannel) throws Exception {
						sChannel.pipeline().addLast(new SortServerInitializer());
					}
				})
				.option(ChannelOption.SO_BACKLOG, 128)
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				.childOption(ChannelOption.TCP_NODELAY, true);
			
			// Start the server.
			ChannelFuture f = boot.bind(new InetSocketAddress(ConfigParams.MASTER_ADDRESS, ConfigParams.PORT)).sync();
			System.out.println("Server started at " + ConfigParams.MASTER_ADDRESS + ":" + ConfigParams.PORT);
			
			// Wait until the server socket is closed.
			f.channel().closeFuture().sync();
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		finally {			
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			
			// Wait until all threads are terminated.
			bossGroup.terminationFuture().sync();
			workerGroup.terminationFuture().sync();
		}
	}
	
	public static void main(String[] args) throws Exception {
		SortServer.start();
	}
}