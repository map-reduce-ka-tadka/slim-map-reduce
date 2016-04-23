	package com.net;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.main.ClientMain;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

/**
 * This Class initializes the client and provides methods to start it.
 * @author Abhijeet Sharma
 * @version 2.0
 * @since April 8, 2016 
 */
public class SortClient {
	public static final Logger LOG = LoggerFactory.getLogger("client");
	/**
	 * Starts the server.
	 * @throws InterruptedException
	 */
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
			LOG.info("Client connecting to {}:{}", ClientMain.SERVER_ADDRESS, ClientMain.SERVER_PORT);
			// Start the client.
			ChannelFuture f = boot.connect(new InetSocketAddress(ClientMain.SERVER_ADDRESS, ClientMain.SERVER_PORT)).sync();			
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
}