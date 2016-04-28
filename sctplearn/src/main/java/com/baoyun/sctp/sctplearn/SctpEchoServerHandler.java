package com.baoyun.sctp.sctplearn;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicLong;

@Sharable
public class SctpEchoServerHandler extends ChannelInboundHandlerAdapter {
	static AtomicLong counter = new AtomicLong(0);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
    	long i = counter.incrementAndGet();
    	if(i==1){
    		startPrint();
    	}
        ctx.write(msg);
    }

    private void startPrint() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();
				while(true){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					long end = System.currentTimeMillis();
					
					long speed = counter.get() *1000/(end-start);
					System.out.println("count:"+counter.get()+",time:"+((end-start)/100)+",speed:"+speed);
				}
			}
		}).start();
	}

	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
