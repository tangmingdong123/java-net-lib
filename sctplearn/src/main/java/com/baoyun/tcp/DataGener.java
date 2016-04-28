package com.baoyun.tcp;

public class DataGener {
	static int batchSize = 100;
	static byte[] little = null;
	public static byte[] genData(){
		if(little==null){
			MyByteBuf buf = new MyByteBuf(204800);
			
			byte[] ds = new byte[512];
			for(int d=0;d<ds.length;d++){
				ds[d] = (byte)d;
			}
			
			for(int i=0;i<batchSize;i++){
				buf.put(ByteUtil.toByte(ds.length));
				buf.put(ds);
			}
			little = buf.available();
		}
		return little;
	}
}
