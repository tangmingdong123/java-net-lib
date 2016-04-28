package com.baoyun.tcp.bothway;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPSender {
	Socket socket ;
	OutputStream os;
	BufferedOutputStream bos;
	
	public TCPSender(int port) throws IOException{
		socket = new Socket();
		socket.connect(new InetSocketAddress(port));
		
		os = socket.getOutputStream();
		bos = new BufferedOutputStream(os,102400);
	}
	
	int i=0;
	
	public void send(byte[] ds) throws IOException, InterruptedException {
		send(ds,0,ds.length);
	}

	public void send(byte[] bs, int j, int len) throws IOException, InterruptedException{
		bos.write(bs,j, len);
		i ++;
		
		if(i==100){
			bos.flush();
			
			i = 0;
		}
	}

	public OutputStream getOutputStream() {
		return bos;
	}
}
