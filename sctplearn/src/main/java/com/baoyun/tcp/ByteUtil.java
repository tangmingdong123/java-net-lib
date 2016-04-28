package com.baoyun.tcp;

public class ByteUtil {
	public static int parseInt(byte[] bs) {
		return ( (bs[0] & 0xff) <<24) | ( (bs[1] & 0xff) <<16) | ((bs[2] & 0xff) <<8) | (bs[3]& 0xff);
	}
	public static byte[] toByte(int s) {
		return new byte[]{
				(byte)(s >> 24),
				(byte)((s >> 16) & 0xff),
				(byte)((s >> 8) & 0xff),
				(byte)(s & 0xff)
		};
	}
	
	public static void main(String[] args) {
		byte[] bs = toByte(1028);
		System.out.println(bs[0]);
		System.out.println(bs[1]);
		System.out.println(bs[2]);
		System.out.println(bs[3]);
		
		for(int i=0;i<1024*1024;i++){
			if(i!=parseInt(toByte(i)))
				System.out.println(i);
		}
	}
}
