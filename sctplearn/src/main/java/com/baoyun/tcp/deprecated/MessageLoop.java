package com.baoyun.tcp.deprecated;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.baoyun.tcp.Packet;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

public class MessageLoop {
	Disruptor<Packet> msgDisruptor = new Disruptor<Packet>(new PacketFactory()
		,1024
		,Executors.newSingleThreadExecutor()
		,ProducerType.SINGLE
		//,new BlockingWaitStrategy()
		, new YieldingWaitStrategy()
	);
	
	AtomicLong counter = new AtomicLong(0);
	Translator translator = new Translator();
	
	@SuppressWarnings("unchecked")
	public MessageLoop(PacketHandler handler){
		msgDisruptor.handleEventsWith(new MsgEventHandler(handler));
		msgDisruptor.start();
	}
	
	public static interface PacketHandler{
		public void onMsg(Packet packet);
	}
	
	class MsgEventHandler implements EventHandler<Packet>{
		private PacketHandler packetHandler;
		public MsgEventHandler( PacketHandler packetHandler){
			this.packetHandler = packetHandler;
		}
		@Override
		public void onEvent(Packet packet,  long sequence, boolean endOfBatch)
				throws Exception {
			packetHandler.onMsg(packet);
			counter.incrementAndGet();
		}
	}
	
	class PacketFactory implements EventFactory<Packet>{
		@Override
		public Packet newInstance() {
			return new Packet(new byte[1024],1024);
		}
	}
	
	class Translator implements EventTranslatorOneArg<Packet, Packet>{
	    @Override
	    public void translateTo(Packet event, long sequence, Packet data) {
	        event.bs = data.bs;
	        event.len = data.len;
	    }    
	}
	
	public void publishEvent(Packet pc){
		msgDisruptor.getRingBuffer().publishEvent(translator, pc);
	}
	
	public void shutdown(){
		msgDisruptor.shutdown();
	}
	
	public static void main(String[] args) throws InterruptedException {
		MessageLoop mc = new MessageLoop(new PacketHandler() {
			@Override
			public void onMsg(Packet packet) {
				
			}
		});
		
		byte[] data = new byte[1024];
		//LinkedBlockingQueue<Packet> queue = new LinkedBlockingQueue<Packet>();
		long t1 = System.currentTimeMillis();
		for(int i=0;i<1000*1000*10;i++){
			mc.publishEvent(new Packet(data,data.length));
			//queue.put(new Packet(data,data.length));
		}
		long t2 = System.currentTimeMillis();
		System.out.println(t2-t1+",msg:"+mc.counter.get());
		mc.shutdown();
		System.exit(0);
	}
}
