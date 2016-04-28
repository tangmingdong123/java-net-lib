package com.baoyun.tcp.zerocopy;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import com.baoyun.collection.Cache;
import com.baoyun.tcp.ByteUtil;

public class DataItem {
	//private Cache itemCache;
	private Cache bufferCache;
	
	private List<DataBlock> blocks = new ArrayList<DataBlock>();
	private int len = 0;
	
	public void addBlock(Cache itemCache,Cache bufferCache, RefByteBuffer source, int from, int to){
		if(from==to || source==null){
			return;
		}
		//this.itemCache = itemCache;
		this.bufferCache = bufferCache;
		
		blocks.add(new DataBlock(source, from, to));
		len += to-from;
		source.increaseRef();
	}
	
	static ByteBuffer buf = ByteBuffer.allocateDirect(102400);
	public void write(SocketChannel channel) throws IOException {
		buf.clear();
		ByteBuffer source = null;
		try{
			buf.put(ByteUtil.toByte(len));
			for(DataBlock b:blocks){
				source = b.source.getBuffer().asReadOnlyBuffer();
				source.position(b.from);
				source.limit(b.to);
				
				buf.put(source);
				buf.limit(b.to-b.from+4);
			}
		}catch(Exception e){
			//e.printStackTrace();
			//e.printStackTrace();
		}
		buf.flip();
		while(buf.hasRemaining()){
			channel.write(buf);
		}
	}
	public void write(OutputStream os) throws IOException {
		byte[] bs = new byte[len];
		int read = 0;
		
		ByteBuffer source = null;
		try{
			for(DataBlock b:blocks){
				source = b.source.getBuffer().asReadOnlyBuffer();
				source.position(b.from);
				
				source.get(bs, read, b.to-b.from);
				read += b.to-b.from;
			}
		}catch(Exception e){
			//e.printStackTrace();
			//e.printStackTrace();
		}
		os.write(bs);
	}
	
	public int getLen(){
		return len;
	}
	
	public void clear(){
		for(DataBlock block:blocks){
			int ref = block.source.decreaseRef();
			if(ref == 0 && bufferCache !=null){
				bufferCache.put(block.source);
			}
		}
		
		blocks.clear();
		len = 0;
		//itemCache.put(this);
	}
	
	public class DataBlock {
		private RefByteBuffer source;
		private int from;
		private int to;
		public DataBlock(RefByteBuffer source, int from, int to) {
			super();
			this.source = source;
			this.from = from;
			this.to = to;
		}
	}
}
