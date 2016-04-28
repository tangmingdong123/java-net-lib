package com.baoyun.tcp.deprecated;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

import com.baoyun.tcp.Packet;

public class TCPLoopServer {
	public static void main(String[] args) throws IOException, InterruptedException {
		new TCPLoopServer().start();
	}
	
	Socket socket ;
	AtomicLong counter = new AtomicLong(0);
	AtomicLong bytecounter = new AtomicLong(0);
	
	public void start() throws IOException, InterruptedException {
		ServerSocket ss = new ServerSocket();
		ss.bind(new InetSocketAddress(10004));
		System.out.println("listend start");
		
		socket = ss.accept();
		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					print();
				} catch (InterruptedException | IOException e) {
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
		final BufferedInputStream bis = new BufferedInputStream(is,102400);
		
		//处理消息
		MessageLoop consumer = new MessageLoop(new MessageLoop.PacketHandler() {
			@Override
			public void onMsg(Packet packet) {
//				System.out.println("----onMsg");
				counter.incrementAndGet();
			}
			
		});
		//分帧
		MessageLoop frameLoop = new MessageLoop(new MessageLoopFrameHandler(null, consumer));
		
		//接收
//		byteLoop = new MessageLoop(new MessageLoop.PacketHandler() {
//			@Override
//			public void onMsg(Packet packet) {
//				try {
//					int len = bis.read(packet.bs);
//					
//					if(len>0){
//						packet.len = len;
//						receivedBytesQueue.put(packet);
//					}
//				} catch (IOException | InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		//初始化一个byte[]列表，循环利用
//		for(int i=0;i<1024;i++){
//			byteLoop.publishEvent(new Packet(new byte[102400],102400));
//		}
		while(true){
			byte[] bs = new byte[204800];//MemoryPool.getInstance().getBig();
			int len = bis.read(bs);
			
			if(len>0){
				Packet recv = new Packet(bs,len);
				//receivedBytesQueue.put(recv);
				frameLoop.publishEvent(recv);
				bytecounter.addAndGet(recv.len);
			}
		}
	}

	
	
	private void print() throws InterruptedException, IOException{
		long begin = System.currentTimeMillis();
		
		while(true){
			Thread.sleep(1000);
			System.out.println("");
			System.out.println("total:"+counter.get()+",speed:"+(counter.get()*1000/(System.currentTimeMillis()-begin)));
			System.out.println("bytes:"+bytecounter.get()/1024/1024+"M,speed:"+(bytecounter.get()*1000/1024/1024/(System.currentTimeMillis()-begin))+"M");
		}
	}

}
