package com.baoyun.tcp;

import java.util.concurrent.LinkedBlockingQueue;

import com.baoyun.collection.Cache;

public class MessageQueue {
	private final int block_size = 1028;
	Cache blocks = null;
	Cache queue ;
	Packet cur = null;
	
	public byte[] borrowBlock(){
//		return new byte[block_size];
		return (byte[])blocks.take();
//		try {
//			byte[] block = cache.poll(100, TimeUnit.MICROSECONDS);
//			if(block==null){
//				System.out.println("----------");
//				block = new byte[block_size];
//			}
//			return block;
//		} catch (InterruptedException e) {
//			System.out.println("----------");
//			return new byte[block_size];
//		}//0.1ms
	}
	
	
	public void returnBlock(byte[] block){
		blocks.put(block);
	}
	
	public MessageQueue(int size){
//		queue = new Cache(size);
		queue = new Cache(size);
		
		blocks = new Cache(size*4);
		for(int i=0;i<size*4;i++){
			blocks.put(new byte[block_size]);
		}
	}
	
	
	public void put(Packet p){
		queue.put(p);
	}
	
	public int size(){
		return 0;
		//return queue.size();
	}
	
	public Packet readMsg() throws InterruptedException{
		byte[] len4 = readLength(4,new byte[4]);
		int msglen = ByteUtil.parseInt(len4);
		
		if(msglen!=512){
			System.out.println("eerrr len:"+msglen);
		}
		byte[] msg = readLength(msglen,borrowBlock());
		return new Packet(msg,msglen);
	}

	private byte[] readLength(int size,byte[] target) throws InterruptedException {
		int len = 0;
		while(true){
			if(cur == null){
				cur = (Packet)queue.take();
			}
			if(cur.end()){
				cur = (Packet)queue.take();
				
			}
			int read = cur.read(target, len, size-len);
			len = read + len;
			if(len==size){
				break;
			}
		}
		return target;
	}
	
	public static void main(String[] args) throws InterruptedException {
		MessageQueue queue = new MessageQueue(1024);
		byte[] data = "hello".getBytes();
		
		// 1
		queue.put(new Packet(ByteUtil.toByte(data.length),4));
		queue.put(new Packet(data,data.length));
		System.out.println(new String(queue.readMsg().bs));
		
		// 2
		queue.put(new Packet(ByteUtil.toByte(data.length),4));
		queue.put(new Packet("hexxxx".getBytes(),2));
		queue.put(new Packet("llollll".getBytes(),3));
		System.out.println(new String(queue.readMsg().bs));
		
		// 3
		queue.put(new Packet(ByteUtil.toByte(data.length),4));
		queue.put(new Packet("hexxxx".getBytes(),2));
		queue.put(new Packet("llollll".getBytes(),3));
		System.out.println(new String(queue.readMsg().bs));
		
		// 4
		queue.put(new Packet(new byte[]{0},1));
		queue.put(new Packet(new byte[]{0},1));
		queue.put(new Packet(new byte[]{0},1));
		queue.put(new Packet(new byte[]{5},1));
		queue.put(new Packet("he".getBytes(),2));
		queue.put(new Packet("lloxxx".getBytes(),3));
		System.out.println(new String(queue.readMsg().bs));
		
		// 5
		queue.put(new Packet(new byte[]{0},1));
		queue.put(new Packet(new byte[]{0,1,3},1));
		queue.put(new Packet(new byte[]{0,5},2));
		queue.put(new Packet(new byte[]{5},0));
		queue.put(new Packet("hexxx".getBytes(),2));
		queue.put(new Packet("lloxxx".getBytes(),3));
		System.out.println(new String(queue.readMsg().bs));
		
		// 6
		MyByteBuf buf = new MyByteBuf(100);
		queue.put(new Packet(new byte[]{0},1));
		queue.put(new Packet(new byte[]{0,1,3},1));
		buf.put(new byte[]{0,5});
		buf.put("he".getBytes());
		
		
		byte[] bs = buf.available();
		queue.put(new Packet(bs,bs.length));
		queue.put(new Packet("lloxxx".getBytes(),3));
		System.out.println(new String(queue.readMsg().bs));
		
		// 7
		queue.put(new Packet(new byte[]{0},1));
		queue.put(new Packet(new byte[]{0},1));
		queue.put(new Packet(new byte[]{0,1,3,3},1));
		queue.put(new Packet(new byte[]{5,3,2},1));
		queue.put(new Packet("hexxx".getBytes(),2));
		queue.put(new Packet("lloxxx".getBytes(),3));
		System.out.println(new String(queue.readMsg().bs));
	}

}
