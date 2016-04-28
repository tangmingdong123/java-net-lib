package com.baoyun.tcp.deprecated;

import com.baoyun.tcp.ByteUtil;
import com.baoyun.tcp.MessageQueue;
import com.baoyun.tcp.Packet;



public class BufTest {
	static MessageQueue mq = new MessageQueue(1000*1000*3);
	static int SIZE = 1000*1000;
	static int PACKET_SIZE = 1024;
	
	public static void main(String[] args) throws InterruptedException {
		byte[] data = new byte[PACKET_SIZE];
		for(int i=0;i<data.length;i++){
			data[i] = (byte)i;
		}
//		byte[] data = "hello".getBytes();
		
		
		for(int i=0;i<SIZE;i++){
			mq.put(new Packet(ByteUtil.toByte(data.length+4),4));
			mq.put(new Packet(data,data.length));
		}
		System.out.println("data ok");
		
//		MyByteBuf buf = new MyByteBuf(1024*1024);
//		long t1 = System.currentTimeMillis();
//		for(int i=0;i<100000;i++){
//			buf.put(data);
//			if(buf.size()-buf.position()<data.length){
//				buf.trim(buf.position());
//				buf.clear();
//				
//			}
//		}
//		long t2 = System.currentTimeMillis();
//		System.out.println(t2-t1);
		
		long t1 = System.currentTimeMillis();
		processReceive();
		long t2 = System.currentTimeMillis();
		System.out.println(t2-t1);
	}
	
	private static void processReceive() throws InterruptedException {
		for(int i=0;i<SIZE;i++){
			byte[] bs = mq.readMsg().bs;
//			System.out.println(new String(bs));
//			System.out.println(bs.length);
//			MemoryPool.getInstance().returnMiddle(bs);
		}
	}

}
