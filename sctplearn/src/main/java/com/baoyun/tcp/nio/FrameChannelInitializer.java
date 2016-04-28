package com.baoyun.tcp.nio;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class FrameChannelInitializer extends ChannelInitializer<SocketChannel> {
	private InboundHandlerFactory handlerFactory;
	public FrameChannelInitializer(InboundHandlerFactory handlerFactory){
		this.handlerFactory = handlerFactory;
	}
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 以("\n")为结尾分割的 解码器
        pipeline.addLast("framer", new LengthFieldBasedFrameDecoder(1024*10
        		, 0//lengthFieldOffset
        		, 4//lengthFieldLength
        		, 0//lengthAdjustment
        		, 4//initialBytesToStrip
        		, true//failFast
        		));

        // 自己的逻辑Handler
        pipeline.addLast("handler", handlerFactory.create());
    }
}
