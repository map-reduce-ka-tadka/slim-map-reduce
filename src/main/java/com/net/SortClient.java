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
import java.net.InetSocketAddress;

import com.main.ConfigParams;

public class SortClient {

	public static void start() {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap boot = new Bootstrap()
					.group(group)
					.channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new SortClientInitializer());
						}
					});

			// Start the client.
			ChannelFuture f = boot.connect(new InetSocketAddress(ConfigParams.MASTER_ADDRESS, ConfigParams.PORT)).sync();

			// Wait until the connection is closed.
			f.channel().closeFuture().sync();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// The connection is closed automatically on shutdown.
			group.shutdownGracefully();
		}
	}
	public static void main(String[] args) throws Exception {
		new SortClient().start();
	}
}