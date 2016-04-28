package com.baoyun.tcp;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class Counter {
	String name;
	AtomicLong msgCounter = new AtomicLong(0);
	AtomicLong byteCounter = new AtomicLong(0);
	
	
	
	public Counter(String name) {
		super();
		this.name = name;
	}

	public void start(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					print();
				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void addMsg(byte[] msg){
		long started = msgCounter.incrementAndGet();
		byteCounter.addAndGet(msg.length);
		
		if(started==1){
			start();
		}
	}
	
	public void addMsg(int len){
		long started = msgCounter.incrementAndGet();
		byteCounter.addAndGet(len);
		
		if(started==1){
			start();
		}
	}
	
	private void print() throws InterruptedException, IOException{
		long begin = System.currentTimeMillis();
		long lastbs = 0;
		long lastmsg = 0;
		
		while(true){
			Thread.sleep(1000*2);
			long bs = byteCounter.get();
			long msg = msgCounter.get();
			System.out.println(name+"--bytes:"+(bs-lastbs)/1024/1024+"M,speed:"+((bs-lastbs)*1000/1024/1024/(System.currentTimeMillis()-begin))+"M");
			System.out.println(name+"--packet speed:"+((msg-lastmsg)*1000/(System.currentTimeMillis()-begin))+"个");

			begin = System.currentTimeMillis();
			lastbs = bs;
			lastmsg = msg;
		}
	}
}
