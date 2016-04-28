package com.baoyun.tcp.bothway.nio;

import java.io.IOException;

public class BothwayNIOServer {
	public void start() throws IOException{
		final NIOServerParam server= new NIOServerParam(true,10004,10005);
		server.initServer();
		
		System.out.println("NIOServer started!");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					server.listen();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		}).start();
		 
	}
	
	public static void main(String[] args) throws IOException {
		new BothwayNIOServer().start();
	}
}
