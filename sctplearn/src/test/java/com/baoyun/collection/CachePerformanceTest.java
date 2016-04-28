package com.baoyun.collection;


public class CachePerformanceTest {

	public static void main(String[] args) throws InterruptedException {
		int size = 81920;
		final Cache source = new Cache(size);
		final Cache target = new Cache(size);
		
		for(int i=0;i<size;i++){
			source.put(new byte[1024]);
		}
		
		
		long begin = System.currentTimeMillis();
		Thread reader = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=0;i<1000*5000;i++){
					byte[] bs = (byte[])source.take();
//					if(bs==null){
//						System.out.println("null");
//					}
					target.put(bs);
				}
			}
		});
		reader.start();
		
		Thread writer = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=0;i<1000*5000;i++){
					byte[] bs = (byte[])target.take();
					if(bs==null){
						System.out.println("null");
					}
					source.put(bs);
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
