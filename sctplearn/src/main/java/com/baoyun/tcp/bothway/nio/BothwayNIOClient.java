package com.baoyun.tcp.bothway.nio;

import java.io.IOException;

import com.baoyun.tcp.bothway.TCPCaller;

public class BothwayNIOClient {
	private int listenPort = 0;
	
	public BothwayNIOClient(int listenPort) {
		super();
		this.listenPort = listenPort;
	}

	public void start() throws IOException, InterruptedException{
		final NIOServerParam server= new NIOServerParam(false,listenPort);
		server.initServer();
		
		System.out.println("NIOClient started!");
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
		
		new TCPCaller().main(null);
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		new BothwayNIOClient(Integer.parseInt(args[0])).start();
	}
}
