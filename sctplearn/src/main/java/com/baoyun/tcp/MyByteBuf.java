package com.baoyun.tcp;

public class MyByteBuf {
	private byte[] content;
	private int position;
	private int size;
	
	public MyByteBuf(int size){
		content = new byte[size];
		this.size = size;
	}
	
	public void put(byte[] bs,int offset,int length){
		if(size-position<length){
			throw new IllegalArgumentException("length");
		}
		System.arraycopy(bs, offset, content, position, length);
		position += length;
	}
	
	public void put(byte[] bs){
		put(bs,0,bs.length);
	}
	
	public byte[] get(int from,int length){
		byte[] bs = new byte[length];
		System.arraycopy(content,from,bs,0,length);
		return bs;
	}
	
	public byte[] available(){
		return get(0,position);
	}
	
	public byte[] originalData(){
		return this.content;
	}
	
	public void trim(int length){
		int left = position - length;
		System.arraycopy(content, length, content, 0, left);
		position = left;
	}
	
	public byte[] readMsg(int from){
		if(position-from>4){
			int len = readInt(from);
			//System.out.println(len);
			if(position-from>len){
				return get(from+4, len-4);
				
			}else{
				return null;
			}
		}else{
			return null;
		}
	}
	
	public int readMsg2(int from){
		if(position-from>4){
			int len = readInt(from);
			//System.out.println(len);
			if(position-from>len){
				return len-4;
			}else{
				return -1;
			}
		}else{
			return -1;
		}
	}
	
	
	public void clear(){
		position = 0;
	}
	
	public int position(){
		return position;
	}
	
	public int size(){
		return size;
	}
	
	public int readInt(int pos){
		return parseInt(get(pos,4));
	}
	
	private static int parseInt(byte[] bs) {
		return bs[0]<<24 | bs[1]<<16 | bs[2] <<8 | bs[3];
	}
	private static byte[] toByte(int s) {
		return new byte[]{
				(byte)(s >> 24),
				(byte)((s >> 16) & 0xff),
				(byte)((s >> 8) & 0xff),
				(byte)(s & 0xff)
		};
	}
	
	public static void main(String[] args) {
		MyByteBuf buf = new MyByteBuf(1024);
		byte[] source = "helloworld".getBytes();
		buf.put(source);
		System.out.println(source.length == buf.position);
		buf.put(source,0,source.length);
		System.out.println(source.length*2 == buf.position);
		System.out.println("hello".equals(new String(buf.get(0, 5))));
		System.out.println("hello".equals(new String(buf.get(10, 5))));
		buf.trim(10);
		System.out.println("hello".equals(new String(buf.get(0, 5))));
		System.out.println(buf.position == 10);
	}
}
