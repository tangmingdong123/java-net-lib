package com.baoyun.collection;

import java.util.concurrent.LinkedBlockingQueue;

public class BlockingQueueTest {

	public static void main(String[] args) throws InterruptedException {
		int size = 8192;
		final LinkedBlockingQueue<byte[]> source = new LinkedBlockingQueue<byte[]>(size);
		final LinkedBlockingQueue<byte[]> target = new LinkedBlockingQueue<byte[]>(size);
		for(int i=0;i<size;i++){
			source.add(new byte[1024]);
		}
		
		
		long begin = System.currentTimeMillis();
		Thread reader = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=0;i<1000*5000;i++){
					try {
						byte[] bs = source.take();
						target.put(bs);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		reader.start();
		
		Thread writer = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=0;i<1000*5000;i++){
					try {
						byte[] bs = target.take();
						source.put(bs);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		writer.start();
		
		reader.join();
		writer.join();
		long end1 = System.currentTimeMillis();
		System.out.println("time:"+(end1-begin));
	}

}
