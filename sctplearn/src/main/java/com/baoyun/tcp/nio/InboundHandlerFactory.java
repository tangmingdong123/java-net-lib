package com.baoyun.tcp.nio;

import io.netty.buffer.ByteBuf;
import io.netty.channel.SimpleChannelInboundHandler;

public interface InboundHandlerFactory {
	SimpleChannelInboundHandler<ByteBuf> create();
}
