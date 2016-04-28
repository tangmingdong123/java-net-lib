package com.baoyun.collection;

import java.util.LinkedList;

public class LinkedListTest {

	public static void main(String[] args) throws InterruptedException {
		int size = 8192;
		final LinkedList<byte[]> source = new LinkedList<byte[]>();
		final LinkedList<byte[]> target = new LinkedList<byte[]>();
		for(int i=0;i<size;i++){
			source.add(new byte[1024]);
		}
		
		
		long begin = System.currentTimeMillis();
		Thread reader = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=0;i<1000*5000;){
					byte[] bs = null;
					synchronized (source) {
						if(!source.isEmpty()){
							bs = source.removeFirst();
						}
					}
					if(bs!=null){
						synchronized (target) {
							target.add(bs);
						}
						i++;
					}
				}
			}
		});
		reader.start();
		
		Thread writer = new Thread(new Runnable() {
			
			@Override
			public void run() {
				byte[] bs = null;
				for(int i=0;i<1000*5000;){
					synchronized (target) {
						if(!target.isEmpty()){
							bs = target.removeFirst();
						}
					}
					if(bs!=null){
						synchronized (source) {
							source.add(bs);
						}
						i++;
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
