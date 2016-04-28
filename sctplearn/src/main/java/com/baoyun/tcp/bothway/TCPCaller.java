package com.baoyun.tcp.bothway;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;

import com.baoyun.tcp.Counter;
import com.baoyun.tcp.DataGener;

public class TCPCaller {
	public static void main(String[] args) throws IOException, InterruptedException {
		new TCPCaller().start();
	}
	
	Socket socket ;
	Random r = new Random();
	Counter counter =new Counter("tcpclient");
	
	public void start() throws IOException, InterruptedException{
		socket = new Socket();
		socket.connect(new InetSocketAddress(10004));
		
		System.out.println("caller connected server-");
		
		send();		
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
				Thread.sleep(1);
			}
			//System.out.println("caller send batch");
		}
	}
}
