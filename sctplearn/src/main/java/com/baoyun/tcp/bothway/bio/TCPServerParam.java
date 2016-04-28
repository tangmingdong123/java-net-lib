package com.baoyun.tcp.bothway.bio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.baoyun.collection.Cache;
import com.baoyun.tcp.ByteUtil;
import com.baoyun.tcp.Counter;
import com.baoyun.tcp.MessageQueue;
import com.baoyun.tcp.Packet;
import com.baoyun.tcp.bothway.TCPSender;

public class TCPServerParam {
	//是否需要回复
    private boolean needReply =false;
    private int port = 0;
    private int replyPort = 0;
    public TCPServerParam(boolean needReply, int port) {
		super();
		this.needReply = needReply;
		this.port = port;
	}
    public TCPServerParam(boolean needReply, int port,int replyPort) {
		super();
		this.needReply = needReply;
		this.port = port;
		this.replyPort = replyPort;
	}
    
	Socket socket ;
	Cache receivedMsgQueue = new Cache(1024*8);
	MessageQueue receivedBytesQueue = new MessageQueue(2048);
	Counter counter = new Counter("TCPServer");
	
	public void start() throws IOException, InterruptedException {
		ServerSocket ss = new ServerSocket();
		ss.bind(new InetSocketAddress(port));
		System.out.println("listend start");
		
		socket = ss.accept();
		System.out.println("connected"+port);
		
		new Thread(new Runnable(){
			
			@Override
			public void run() {
				try {
					send();
				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}}).start();
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					processReceive();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}}).start();
		
		
		
		
		
		
		try{
			read();
		}catch(Exception e){
			e.printStackTrace();
		}
		ss.close();
	}



	private void read() throws IOException,
			InterruptedException {
		
		InputStream is = socket.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is,102400);
		while(true){
			byte[] bs = new byte[102400];//MemoryPool.getInstance().getBig();
			int len = bis.read(bs);
			
			if(len>0){
				Packet recv = new Packet(bs,len);
				receivedBytesQueue.put(recv);
			}
		}
	}

	
	
	private void processReceive() throws InterruptedException {
		while(true){
			Packet p = receivedBytesQueue.readMsg();
			receivedMsgQueue.put(p);
			counter.addMsg(p.len);
		}
	}
	
	private void send() throws InterruptedException, IOException{
			TCPSender sender;
			if(needReply){
				System.out.println("to connect reply server");
				sender = new TCPSender(replyPort);
			}else{
				sender = null;
			}
			
			int i=0;
			while(true){
				Packet packet = (Packet)receivedMsgQueue.take();
				if(needReply){
					sender.send(ByteUtil.toByte(packet.len));
					sender.send(packet.bs,0,packet.len);
				}
				receivedBytesQueue.returnBlock(packet.bs);
				
				i ++;
				if(i==10000){
					Thread.sleep(2);
					i = 0;
				}
			}
	}

}
