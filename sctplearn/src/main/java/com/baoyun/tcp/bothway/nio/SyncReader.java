package com.baoyun.tcp.bothway.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.baoyun.collection.Cache;
import com.baoyun.tcp.ByteUtil;
import com.baoyun.tcp.Packet;

public class SyncReader {
	private final int block_size = 1028;
	Cache cache = null;
	
	ByteBuffer readBuffer = ByteBuffer.allocate(102400);
	Cache dispaptcher = null;
	public Cache getDispaptcher() {
		return dispaptcher;
	}



	public SyncReader(int size,Cache md){
		this.dispaptcher = md;
		this.cache = new Cache(size*4) ;
		for(int i=0;i<size*4;i++){
			cache.put(new byte[block_size]);
		}
	}
	
	
	
	public void read(SocketChannel channel) throws IOException{
		// 创建读取的缓冲区  
    	channel.read(readBuffer);  
        readBuffer.flip();
        
        handle();
        
        readBuffer.clear();
	}
	
	public byte[] borrowBlock(){
		//return new byte[block_size];
		byte[] block = (byte[])cache.take();
		return block;
	}
	
	
	public void returnBlock(byte[] block){
		cache.put(block);
	}
	
	State state = new State();
	public void handle() {
		while(readBuffer.hasRemaining()){
			if(state.state() == State.HEAD){
				readHead();
			}
			
			if(state.state() == State.BODY){
				readBody();
			}
			
			if(state.state == State.END){
				Packet p = new Packet(state.body, state.msglength);
				dispaptcher.put(p);
				state.begin();
			}
		}
	}

	
	private void readHead() {
		int needread = 4-state.headBytes;
		if(readBuffer.remaining()<needread){
			needread = readBuffer.remaining();
		}
		readBuffer.get(state.head,state.headBytes,needread);
		state.headBytes += needread;
		
		if(state.headBytes == 4){
			state.beginBody();
		}
	}
	
	private void readBody() {
		int needread = state.msglength-state.bodyBytes;
		if(readBuffer.remaining()<needread){
			needread = readBuffer.remaining();
		}
		readBuffer.get(state.body,state.bodyBytes,needread);
		state.bodyBytes += needread;
		
		if(state.bodyBytes == state.msglength){
			state.end();
		}
	}
	
	class State{
		final static int HEAD = 0;
		final static int BODY = 1;
		final static int END = 2;
		
		public byte[] head = new byte[4];
		public int headBytes = 0;
		
		public int msglength = 0;
		
		public byte[] body = null;
		public int bodyBytes = 0;
		
		public int state = 0;//0 正在读head, 1正在读body, 2读取完毕
		
		public int state(){
			return state;
		}
		
		public void begin(){
			state = HEAD;
			headBytes = 0;
			msglength = 0;
			bodyBytes = 0;
		}
		
		public void beginBody(){
			state = BODY;
			msglength = ByteUtil.parseInt(head);
			bodyBytes = 0;
			body = borrowBlock();
		}
		
		public void end(){
			state = END;
		}
	}
}
