package com.baoyun.sctp.sctplearn;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.SctpChannel;
import io.netty.channel.sctp.SctpChannelOption;
import io.netty.channel.sctp.nio.NioSctpChannel;

public class SctpEchoClient {
	static String HOST = System.getProperty("host", "172.16.1.2");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "1024"));

    public static void main(String[] args) throws Exception {
    	HOST = args[0];
    	for(int i=0;i<10;i++){
	    	new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						one();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
    	}
    	
    	Thread.sleep(60*1000L);
    }
    public static void one() throws Exception {
        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSctpChannel.class)
             .option(SctpChannelOption.SCTP_NODELAY, true)
             .handler(new ChannelInitializer<SctpChannel>() {
                 @Override
                 public void initChannel(SctpChannel ch) throws Exception {
                     ch.pipeline().addLast(
                             //new LoggingHandler(LogLevel.INFO),
                             new SctpEchoClientHandler());
                 }
             });

            // Start the client.
            ChannelFuture f = b.connect(HOST, PORT).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
    }
}
