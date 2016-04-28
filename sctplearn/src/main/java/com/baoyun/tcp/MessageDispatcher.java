package com.baoyun.tcp;

import java.util.concurrent.LinkedBlockingQueue;

public class MessageDispatcher {
	LinkedBlockingQueue<Packet> receivedMsgQueue = new LinkedBlockingQueue<Packet>(102400);
	
	public void add(Packet p){
		while(true){
			try {
				receivedMsgQueue.put(p);
				break;
			} catch (InterruptedException e) {
			}
		}
	}
	
	public Packet take(){
		while(true){
			try {
				Packet p = receivedMsgQueue.take();
				if(p!=null){
					return p;
				}
			} catch (InterruptedException e) {
			}
		}
	}
}
