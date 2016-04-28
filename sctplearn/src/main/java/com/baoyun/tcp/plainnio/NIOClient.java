package com.baoyun.tcp.plainnio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import com.baoyun.tcp.Counter;
import com.baoyun.tcp.DataGener;
import com.baoyun.tcp.MessageQueue;
import com.baoyun.tcp.Packet;

public class NIOClient {  
    //通道管理器  
    private Selector selector;  
  
    /** 
     * 获得一个Socket通道，并对该通道做一些初始化的工作 
     * @param ip 连接的服务器的ip 
     * @param port  连接的服务器的端口号          
     * @throws IOException 
     */  
    public void initClient(String ip,int port) throws IOException {  
        // 获得一个Socket通道  
        SocketChannel channel = SocketChannel.open();  
        // 设置通道为非阻塞  
        channel.configureBlocking(false);  
          
        // 客户端连接服务器,其实方法执行并没有实现连接，需要在listen（）方法中调  
        //用channel.finishConnect();才能完成连接  
        channel.connect(new InetSocketAddress(ip,port)); 
        channel.setOption(StandardSocketOptions.SO_RCVBUF, 102400*5);
        //channel.setOption(StandardSocketOptions.SO_SNDBUF, 102400);
        channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        
        
        // 获得一个通道管理器  
        this.selector = Selector.open();  
        //将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。  
        channel.register(selector, SelectionKey.OP_CONNECT);  
    }  
  
    /** 
     * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理 
     * @throws IOException 
     */  
    @SuppressWarnings("rawtypes")
	public void listen() throws IOException {  
        // 轮询访问selector  
        while (true) {  
            selector.select();  
            // 获得selector中选中的项的迭代器  
            Iterator ite = this.selector.selectedKeys().iterator();  
            while (ite.hasNext()) {  
                SelectionKey key = (SelectionKey) ite.next();  
                // 删除已选的key,以防重复处理  
                ite.remove();  
                // 连接事件发生  
                if (key.isConnectable()) {  
                    SocketChannel channel = (SocketChannel) key  
                            .channel();  
                    // 如果正在连接，则完成连接  
                    if(channel.isConnectionPending()){  
                        channel.finishConnect();  
                          
                    }  
                    // 设置成非阻塞  
                    channel.configureBlocking(false);  
                   
                    //在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。  
                    channel.register(this.selector, SelectionKey.OP_READ);  
                    
                    //开启写
                    startWriteThread(channel);  
                } else if (key.isReadable()) {  
                    read(key);  
                } 
            }  
  
        }  
    }

	private void startWriteThread(final SocketChannel channel) throws IOException {
		//在这里可以给服务端发送信息哦  
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					while(true){
						ByteBuffer bb = buildMsg();
						while(bb.hasRemaining()){
							channel.write(bb);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					int i=0;
					while(true){
						Packet p = mq.readMsg();
						counter.addMsg(p.len);
						i++;
//						if(i%1000==0){
//							System.out.println("msg:"+i+","+mq.size());
//						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
	}  
    
    private ByteBuffer buildMsg() {
		byte[] data = DataGener.genData();
		ByteBuffer bb = ByteBuffer.allocate(data.length);
		bb.put(data);
		bb.flip();
		return bb;
	}

	MessageQueue mq = new MessageQueue(2048);
	Counter counter = new Counter("NIOClient");
    /** 
     * 处理读取服务端发来的信息 的事件 
     * @param key 
     * @throws IOException  
     */  
	ByteBuffer buffer = ByteBuffer.allocate(12800);  
	AtomicLong read = new AtomicLong(0);
	AtomicLong bytes = new AtomicLong(0);
    public void read(SelectionKey key) throws IOException{  
        //和服务端的read方法一样  
    	// 服务器可读取消息:得到事件发生的Socket通道  
        SocketChannel channel = (SocketChannel) key.channel();  
        // 创建读取的缓冲区  
        channel.read(buffer);  
        int p = buffer.position();
        byte[] data = new byte[p];
        buffer.flip();
        buffer.get(data); 
        mq.put(new Packet(data, data.length));
        buffer.clear();
        
        bytes.addAndGet(data.length);
        if(read.incrementAndGet()%10000 == 0){
//        	System.out.println(read.incrementAndGet()+","+(data.length));
//        	System.out.println(new String(data));
        }
//        System.out.println(new String(data));
    }  
      
      
    /** 
     * 启动客户端测试 
     * @throws IOException  
     */  
    public static void main(String[] args) throws IOException {  
        NIOClient client = new NIOClient();  
        client.initClient("127.0.0.1",10004);  
        client.listen();  
    }  
  
}
