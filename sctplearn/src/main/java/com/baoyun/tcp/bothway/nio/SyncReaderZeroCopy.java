package com.baoyun.tcp.bothway.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.baoyun.collection.Cache;
import com.baoyun.tcp.ByteUtil;
import com.baoyun.tcp.zerocopy.DataItem;
import com.baoyun.tcp.zerocopy.RefByteBuffer;

public class SyncReaderZeroCopy {
	
	Cache bufferCache = null;
	//Cache itemCache = null;
	Cache dispaptcher = null;
	
	public Cache getDispaptcher() {
		return dispaptcher;
	}

	public SyncReaderZeroCopy(int size,Cache md){
		this.dispaptcher = md;
		bufferCache  = new Cache(size);
		
		for(int i=0;i<size;i++){
			bufferCache.put(new RefByteBuffer(ByteBuffer.allocateDirect(102400)));
		}
	}
	
	
	
	public void read(SocketChannel channel) throws IOException{
		//System.out.println("read-0");
		RefByteBuffer refbuf = (RefByteBuffer)bufferCache.take();
		refbuf.clear();
		// 创建读取的缓冲区  
    	channel.read(refbuf.getBuffer());  
    	refbuf.getBuffer().flip();
        
        handle(refbuf);
        //System.out.println("read-1");
	}
	
	State state = new State();
	public void handle(RefByteBuffer refbuf) {
		while(refbuf.getBuffer().hasRemaining()){
			if(state.state() == State.HEAD){
				readHead(refbuf);
			}
			
			if(state.state() == State.BODY){
				readBody(refbuf);
			}
			
			if(state.state == State.END){
				dispaptcher.put(state.body);
				state.begin();
			}
		}
	}

	
	private void readHead(RefByteBuffer refbuf) {
		ByteBuffer readBuffer = refbuf.getBuffer();
		
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
	
	private void readBody(RefByteBuffer refbuf) {
		ByteBuffer readBuffer = refbuf.getBuffer();
		
		int needread = state.msglength-state.bodyBytes;
		if(readBuffer.remaining()<needread){
			needread = readBuffer.remaining();
		}
		state.body.addBlock(null,bufferCache,refbuf, readBuffer.position(), readBuffer.position()+needread);
		state.bodyBytes += needread;
		readBuffer.position(readBuffer.position()+needread);
		
		if(state.bodyBytes == state.msglength){
			state.end();
		}
	}
	
	class State{
		final static int HEAD = 0;
		final static int BODY = 1;
		final static int END = 2;
		
		public byte[] head= new byte[4];
		public int headBytes = 0;
		
		public int msglength = 0;
		
		public DataItem body;
		public int bodyBytes = 0;
		
		public int state = 0;//0 正在读head, 1正在读body, 2读取完毕
		
		public int state(){
			return state;
		}
		
		public void begin(){
			state = HEAD;
			
			headBytes = 0;
			head[0] = 0;
			head[1] = 0;
			head[2] = 0;
			head[3] = 0;
			msglength = 0;
			
			bodyBytes = 0;
		}
		
		public void beginBody(){
			state = BODY;
			
			msglength = ByteUtil.parseInt(head);
			//System.out.println("msglen:"+msglength);
			
			bodyBytes = 0;
			body = new DataItem();//(DataItem)itemCache.get();
		}
		
		public void end(){
			state = END;
		}
	}
}
