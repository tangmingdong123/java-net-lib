package com.baoyun.tcp.bothway.bio;

import java.io.IOException;

import com.baoyun.tcp.bothway.TCPCaller;

public class BothwayTCPClient {
	public void start() throws IOException, InterruptedException{
		final TCPServerParam server= new TCPServerParam(false,10005);
		
		new Thread(new  Runnable() {
			
			@Override
			public void run() {
				try {
					server.start();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}).start();
		
		
		System.out.println("NIOClient started!");
		
		new TCPCaller().main(null);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		new BothwayTCPClient().start();
	}
}
