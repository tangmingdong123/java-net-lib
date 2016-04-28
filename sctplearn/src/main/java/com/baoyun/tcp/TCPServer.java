package com.baoyun.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class TCPServer {
	public static void main(String[] args) throws IOException, InterruptedException {
		new TCPServer().start();
	}
	
	Socket socket ;
	LinkedBlockingQueue<Packet> receivedMsgQueue = new LinkedBlockingQueue<Packet>(102400);
	MessageQueue receivedBytesQueue = new MessageQueue(2048);
	Counter counter = new Counter("TCPServer");
	
	public void start() throws IOException, InterruptedException {
		ServerSocket ss = new ServerSocket();
		ss.bind(new InetSocketAddress(10004));
		System.out.println("listend start");
		
		socket = ss.accept();
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
		
		System.out.println("connected");
		
		
		
		
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
//				System.out.println(new String(bs,0,len));
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
		OutputStream os = socket.getOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(os,204800);
		while(true){
			Packet packet = receivedMsgQueue.take();
//			System.out.println(new String(packet.bs));
//			MemoryPool.getInstance().returnMiddle(packet.bs);
			bos.write(ByteUtil.toByte(packet.bs.length),0,4);
			bos.write(packet.bs,0,packet.bs.length);
		}
	}

}
