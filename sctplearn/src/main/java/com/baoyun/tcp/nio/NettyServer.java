package com.baoyun.tcp.nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
	public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.childOption(ChannelOption.SO_BACKLOG, 12800);
            b.childOption(ChannelOption.SO_RCVBUF, 12800);
            b.childOption(ChannelOption.SO_SNDBUF, 12800);
            b.option(ChannelOption.SO_BACKLOG, 12800);
            b.option(ChannelOption.SO_RCVBUF, 12800);
            b.option(ChannelOption.SO_SNDBUF, 12800);
            //通过NoDelay禁用Nagle,使消息立即发出去，不用等待到一定的数据量才发出去
            //b.option(ChannelOption.TCP_NODELAY, true);
            //保持长连接状态
            b.childOption(ChannelOption.SO_KEEPALIVE, true);
            b.childHandler(new ServerChannelInitializer());

            // 服务器绑定端口监听
            ChannelFuture f = b.bind(10004).sync();
            
            System.out.println("Netty Server start");
            // 监听服务器关闭监听
            f.channel().closeFuture().sync();

            // 可以简写为
            /* b.bind(portNumber).sync().channel().closeFuture().sync(); */
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
