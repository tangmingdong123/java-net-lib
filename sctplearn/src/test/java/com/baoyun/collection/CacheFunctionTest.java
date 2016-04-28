package com.baoyun.collection;

import java.util.concurrent.atomic.AtomicInteger;


public class CacheFunctionTest {

	public static void main(String[] args) throws InterruptedException {
		int size = 81920;// 1024*128;
		final AtomicInteger index = new AtomicInteger(0);
		final Cache source = new Cache(size);
		
		for(int i=0;i<size;i++){
			source.put(index.incrementAndGet());
		}
		
		
		long begin = System.currentTimeMillis();
		Thread reader = new Thread(new Runnable() {
			
			@Override
			public void run() {
				int start = 0;
				for(int i=0;i<2000*1000;i++){
					//source.take();
					Integer g = (Integer)source.take();
					if(start+1!=g.intValue()){
						System.out.println("err:"+start+","+g);
					}
					//System.out.println(""+start+","+g);
					
					start = g;
				}
			}
		});
		reader.start();
		
		Thread writer = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=0;i<2000*1000;i++){
					source.put(index.incrementAndGet());
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
