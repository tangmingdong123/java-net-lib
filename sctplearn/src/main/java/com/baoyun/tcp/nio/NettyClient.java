package com.baoyun.tcp.nio;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;

import com.baoyun.tcp.ByteUtil;
import com.baoyun.tcp.DataGener;

public class NettyClient {

    public static String host = "127.0.0.1";
    public static int port = 10004;

    /**
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException,
            IOException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
            	.channel(NioSocketChannel.class)
            	.option(ChannelOption.SO_KEEPALIVE,true)
                .handler(new FrameChannelInitializer(new InboundHandlerFactory() {
					
					@Override
					public SimpleChannelInboundHandler<ByteBuf> create() {
						return new ClientHandler();
					}
				}));

            // 连接服务端
            Channel ch = b.connect(host, port).sync().channel();

           
            while(true) {
                ch.writeAndFlush(Unpooled.copiedBuffer(DataGener.genData()));
            }
            
            //Thread.sleep(50000);
        } finally {
            // The connection is closed automatically on shutdown.
            group.shutdownGracefully();
        }
    }
}
