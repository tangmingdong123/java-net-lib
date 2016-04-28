package com.baoyun.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;

public class TCPClient {
	public static void main(String[] args) throws IOException {
		new TCPClient().start();
	}
	
	Socket socket ;
	Random r = new Random();
	Counter counter =new Counter("tcpclient");
	MessageQueue mq = new MessageQueue(1024);
	
	public void start() throws IOException{
		socket = new Socket();
		socket.connect(new InetSocketAddress(10004));
		
		System.out.println("connected");
		
		Thread sender = new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					send();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}});
		sender.setName("sender");
		sender.start();
		
		Thread reader = new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					read();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}});
		reader.setName("reader");
		reader.start();
		
		Thread handler = new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					handle();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}});
		handler.setName("handler");
		handler.start();
		
	}
	
	private void send() throws IOException, InterruptedException {
		OutputStream os = socket.getOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(os,102400);
		int i=0;
		byte[] ds = DataGener.genData();
		while(true){
			bos.write( ds,0,ds.length);
			bos.flush();
			i ++;
			if(i%1000==0){
				Thread.sleep(5);
			}
		}
	}
	
	private void read() throws IOException,
			InterruptedException {
		InputStream is = socket.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(is,102400);
		while(true){
			//System.out.println("read");
			byte[] bs = new byte[1024];//MemoryPool.getInstance().getBig();
			int i = bis.read(bs);
			//System.out.println(i);
			if(i>0){
				mq.put(new Packet(bs,i));
				//System.out.println(i);
			}
		}
	}
	
	private void handle() throws IOException,
		InterruptedException {
		while(true){
			Packet p = mq.readMsg();
			counter.addMsg(p.bs);
			//System.out.println("get");
		}
	}
}
