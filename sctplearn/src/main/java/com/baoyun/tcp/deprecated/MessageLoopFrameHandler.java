package com.baoyun.tcp.deprecated;

import com.baoyun.tcp.ByteUtil;
import com.baoyun.tcp.Packet;
import com.baoyun.tcp.deprecated.MessageLoop.PacketHandler;

/**
 * 分帧
 * @author MorningSheep
 *
 */
public class MessageLoopFrameHandler implements PacketHandler{
	MessageLoop reuse = null;
	MessageLoop dispatcher = null;
	
	byte[] headerbytes = new byte[4];
	boolean readHeader = true;
	int headerLen = 0;
	
	byte[] messagebytes = null;
	boolean readBody = false;
	int bodyLen = 0;
	
	public MessageLoopFrameHandler(MessageLoop reuse,MessageLoop dispatcher){
		this.reuse = reuse;
		this.dispatcher = dispatcher;
	}
	
	@Override
	public synchronized void onMsg(Packet packet) {
		//System.out.println("onFrame:"+Thread.currentThread().getName());
		while(true){
			if(readHeader){
				int len = packet.read(headerbytes, headerLen, 4-headerLen);
				headerLen += len;
				if(headerLen == 4){
					//读了消息头
					beginBody();
				}else{
					//packet 读取完毕
					if(reuse!=null)
						reuse.publishEvent(packet);
					break;
				}
			}
			int msgLen = ByteUtil.parseInt(headerbytes);
			if(readBody){
				int len = packet.read(messagebytes, bodyLen, msgLen-bodyLen-4);
				bodyLen += len;
				if(msgLen-4 == bodyLen){
					endBody();
					
					//读了消息体 
					if(dispatcher !=null){
						dispatcher.publishEvent(new Packet(messagebytes,msgLen));
					}
				}else{
					break;
				}
			}
		}
	}
	
	public void endBody(){
		//System.out.println(new String(messagebytes,0,ByteUtil.parseInt(headerbytes)));
		readHeader = true;
		headerLen = 0;
		
		readBody = false;
		bodyLen = 0;
	}
	
	public void beginBody(){
		//System.out.println("len:"+ByteUtil.parseInt(headerbytes));
		
		readHeader = false;
		headerLen = 4;
		
		readBody = true;
		bodyLen = 0;
		messagebytes = new byte[2048];
	}
	
	public static void main(String[] args) {
//		MessageLoop reuse = new MessageLoop(new PacketHandler() {
//			@Override
//			public void onMsg(Packet packet) {
////				System.out.println("onnew");
////				//新产生一个
////				packet.len = 5;
////				packet.bs = "hello".getBytes();
//			}
//		});
//		
		MessageLoop dispacther = new MessageLoop(new PacketHandler() {
			@Override
			public void onMsg(Packet packet) {
				System.out.println("msg:"+new String(packet.bs,0,packet.len));
			}
		});
//		
		MessageLoop frame = new MessageLoop(new MessageLoopFrameHandler(null,dispacther));
		
		frame.publishEvent(new Packet(new byte[]{0},1));
		frame.publishEvent(new Packet(new byte[]{0},1));
		frame.publishEvent(new Packet(new byte[]{0},1));
		frame.publishEvent(new Packet(new byte[]{9},1));
		frame.publishEvent(new Packet("hello".getBytes(),5));
		
		MessageLoopFrameHandler handler = new MessageLoopFrameHandler(null,null);
		handler.onMsg(new Packet(new byte[]{0},1));
		handler.onMsg(new Packet(new byte[]{0},1));
		handler.onMsg(new Packet(new byte[]{0},1));
		handler.onMsg(new Packet(new byte[]{9},1));
		handler.onMsg(new Packet("hello".getBytes(),5));
		handler.onMsg(new Packet(new byte[]{0},1));
		handler.onMsg(new Packet(new byte[]{0},1));
		handler.onMsg(new Packet(new byte[]{0},1));
		handler.onMsg(new Packet(new byte[]{9},1));
		handler.onMsg(new Packet("hello".getBytes(),5));
	}
}
