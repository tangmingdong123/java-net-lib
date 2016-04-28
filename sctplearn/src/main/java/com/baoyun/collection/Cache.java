package com.baoyun.collection;

import java.util.concurrent.atomic.AtomicLong;

public class Cache {
	Object[] ring = null;
	int size = 0;
	AtomicLong readerIndex = new AtomicLong(-1);
	AtomicLong writerIndex = new AtomicLong(-1);
	
	public Cache(){
		this(81920);
	}
	
	public Cache(int size){
		int temp = size;
		while(temp>2 && temp%2 == 0){
			temp = temp/2;
		}
		if(temp!=2){
			throw new IllegalArgumentException("size not 2^n");
		}
		this.size = size;
		ring = new Object[size];
	}
	
	public Object take(){
		long rindex = readerIndex.get()+1;
		int i=0;
		while(true){
			long windex = writerIndex.get();
			if(rindex<=windex){
				Object obj = ring[(int)(rindex & (size-1))];
				readerIndex.incrementAndGet();
				return obj;
				
			}
			
			i++;
			if(i==1000){
				i = 0;
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void put(Object t){
		long windex = writerIndex.get()+1;
		
		while(true){
			if(windex-readerIndex.get()<=size){
				ring[(int)(windex & (size-1))] = t;
				writerIndex.incrementAndGet();
				break;
			}
		}
	}
	
	public static void main(String[] args) {
		int h = 15;
		int length = 10;
		System.out.println(h%length);
		System.out.println(h & (length-1));
	}
}
