package com.baoyun.tcp.bothway.bio;

import java.io.IOException;

public class BothwayTCPServer {
	public void start() throws IOException, InterruptedException{
		final TCPServerParam server= new TCPServerParam(true,10004,10005);
		server.start();
		
		System.out.println("TCPServer started!");
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		new BothwayTCPServer().start();
	}
}
