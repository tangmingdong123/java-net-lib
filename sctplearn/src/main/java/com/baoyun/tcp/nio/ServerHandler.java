package com.baoyun.tcp.nio;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.baoyun.tcp.ByteUtil;
import com.baoyun.tcp.Counter;
import com.baoyun.tcp.MessageQueue;
import com.baoyun.tcp.Packet;

public class ServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
	static Map<Channel,MessageQueue> channelMap = new ConcurrentHashMap<>();
	
	static Counter counter = new Counter("receive");
	static Counter wcounter = new Counter("bytes");
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, ByteBuf msg)
            throws Exception {

        final byte[] dst = new byte[msg.readableBytes()];
        msg.readBytes(dst);
        wcounter.addMsg(dst.length);
        
        MessageQueue mq = channelMap.get(ctx.channel());
        if(mq == null){
        	mq = new MessageQueue(Integer.MAX_VALUE);
        	channelMap.put(ctx.channel(), mq);
        	
        	final MessageQueue nmq = mq;
        	new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(true){
						Packet p;
						try {
							p = nmq.readMsg();
							//System.out.println(p.len);
							counter.addMsg(p.len);
							
							ctx.writeAndFlush(Unpooled.copiedBuffer(ByteUtil.toByte(p.bs.length),p.bs));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					}
				}
			}).start();
        }
        mq.put(new Packet(dst,dst.length));
        // 收到消息直接打印输出
//        System.out.println(ctx.channel().remoteAddress() + " Say : " + new String(dst));
        
//        counter.addMsg(dst);
        //System.out.println("Received your message "+new String(dst));
        // 返回客户端消息 - 我已经接收到了你的消息
//        ByteBuf buf = Unpooled.copiedBuffer(ByteUtil.toByte(dst.length),dst);
//        ChannelFuture ft = ctx.writeAndFlush(buf);
//        ft.addListener(new GenericFutureListener() {
//
//			@Override
//			public void operationComplete(Future future) throws Exception {
//				//wcounter.addMsg(dst);
//			}
//		});
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

//        ctx.writeAndFlush("Welcome to "
//                + InetAddress.getLocalHost().getHostName() + " service!\n");

        super.channelActive(ctx);
    }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
		cause.printStackTrace();
	}
    
    
}
