package com.baoyun.tcp.nio;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import com.baoyun.tcp.Counter;

@Sharable
public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
	static Counter counter = new Counter("client");
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg)
            throws Exception {
        byte[] dst = new byte[msg.readableBytes()];
        msg.readBytes(dst);
        //System.out.println(new String(dst));
        
        counter.addMsg(dst);
    }

    /*
     * 
     * 覆盖 channelActive 方法 在channel被启用的时候触发 (在建立连接的时候)
     * 
     * channelActive 和 channelInActive 在后面的内容中讲述，这里先不做详细的描述
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        System.out.println("RamoteAddress : " + ctx.channel().remoteAddress()
                + " active !");


        super.channelActive(ctx);
    }
}
