package com.baoyun.tcp;

public class Packet{
	public byte[] bs;
	public int len;
	int position;
	public Packet(byte[] bs, int len) {
		super();
		this.bs = bs;
		this.len = len;
		position = 0;
	}
	
	public int read(byte[] target,int offset,int length){
		
		if(len - position <length){
			length = len - position;
		}
		System.arraycopy(bs, position, target, offset, length);
		position += length;
		
		return length;
	}
	
	public boolean end(){
		return len == position;
	}
}