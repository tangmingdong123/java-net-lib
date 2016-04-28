package com.baoyun.tcp.zerocopy;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class RefByteBuffer {
	private ByteBuffer buffer;
	private AtomicInteger ref;//引用计数器
	
	public RefByteBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
		ref = new AtomicInteger(0);
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}
	
	public int increaseRef(){
		return ref.incrementAndGet();
	}
	public int decreaseRef(){
		return ref.decrementAndGet();
	}
	
	public void clear(){
		buffer.clear();
		ref.set(0);
	}
}
