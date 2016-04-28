package com.baoyun.tcp.bothway.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.baoyun.collection.Cache;
import com.baoyun.tcp.ByteUtil;
import com.baoyun.tcp.Counter;
import com.baoyun.tcp.bothway.TCPSender;
import com.baoyun.tcp.zerocopy.DataItem;

public class NIOServerParam {  
    //通道管理器  
    private Selector selector;
    Counter counter = new Counter("NIOClient");
    /** 
     * 处理读取服务端发来的信息 的事件 
     * @param key 
     * @throws IOException  
     */  
	Map<Channel,SyncReaderZeroCopy> readerMap = new ConcurrentHashMap<>();  
	
    //是否需要回复
    private boolean needReply =false;
    private int port = 0;
    private int replyPort = 0;
    public NIOServerParam(boolean needReply, int port) {
		super();
		this.needReply = needReply;
		this.port = port;
	}
    public NIOServerParam(boolean needReply, int port,int replyPort) {
		super();
		this.needReply = needReply;
		this.port = port;
		this.replyPort = replyPort;
	}
    
    /** 
     * 获得一个Socket通道，并对该通道做一些初始化的工作 
     * @param port  连接的服务器的端口号          
     * @throws IOException 
     */  
    public void initServer() throws IOException {  
    	// 获得一个通道管理器  
        this.selector = Selector.open();  
        
        // 获得一个Socket通道  
        ServerSocketChannel channel = ServerSocketChannel.open();  
        // 设置通道为非阻塞  
        channel.configureBlocking(false);  
        channel.bind(new InetSocketAddress(port));
        channel.setOption(StandardSocketOptions.SO_RCVBUF, 102400*5);
        //channel.setOption(StandardSocketOptions.SO_SNDBUF, 102400);
        //channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        
        //将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。  
        channel.register(selector, SelectionKey.OP_ACCEPT);  
    }  
  
    /** 
     * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理 
     * @throws IOException 
     */  
    @SuppressWarnings("rawtypes")
	public void listen() throws IOException {  
        // 轮询访问selector  
    	//boolean ready = false;
        while (true) {  
            selector.select();  
            // 获得selector中选中的项的迭代器  
            Iterator ite = this.selector.selectedKeys().iterator();  
            while (ite.hasNext()) {  
                SelectionKey key = (SelectionKey) ite.next();  
                // 删除已选的key,以防重复处理  
                ite.remove();  
                // 连接事件发生  
                if (key.isAcceptable()) {  
                	//if(!ready){
	                	ServerSocketChannel server = (ServerSocketChannel) key.channel();  
	                    // 如果正在连接，则完成连接  
	                	SocketChannel channel = server.accept();
	                	channel.configureBlocking(false);
	                    //在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读的权限。  
	                    channel.register(this.selector, SelectionKey.OP_READ);  
	                    
	                    startWriteThread(channel);
	                    
	                   // ready = true;
                	//}
                } else if (key.isReadable()) {  
                    read(key);  
                } 
            }  
  
        }  
    }
	private void startWriteThread(final SocketChannel channel) throws IOException {
		System.out.println("------------------start write thread-"+channel.hashCode());
		final SyncReaderZeroCopy reader = new SyncReaderZeroCopy(4096,new Cache(8192*16));
		readerMap.put(channel, reader);
		
		Set<SocketOption<?>> ops = channel.supportedOptions();
		for(SocketOption<?> op:ops){
			System.out.println(op.name()+"="+channel.getOption(op));
		}
		
		
		final TCPSender sender;
		if(needReply){
			sender = new TCPSender(replyPort++);
		}else{
			sender = null;
		}
		final Cache md = reader.getDispaptcher();
		final Cache md2 = new Cache(8192*16);
		//在这里可以给服务端发送信息哦  
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] data =new byte[512];
				try {
					while(true){
						DataItem p = (DataItem)md.take();
						counter.addMsg(p.getLen());
						
						if(needReply){
							sender.send(ByteUtil.toByte(data.length));
							sender.send(data);
//							md2.put(data);
						}
						
						p.clear();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.setName("handler-Thread");
		t.start();
//		
//		//在这里可以给服务端发送信息哦  
//				Thread t2 = new Thread(new Runnable() {
//					@Override
//					public void run() {
//						try {
//							while(true){
//								byte[] data = (byte[])md2.take();
//								
//								if(needReply){
//									sender.send(ByteUtil.toByte(data.length));
//									sender.send(data);
//								}
//								
//							}
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//				});
//				t2.setName("handler-Thread2");
//				t2.start();
	}  

	
	
	
    public void read(SelectionKey key) throws IOException{  
        //和服务端的read方法一样  
    	// 服务器可读取消息:得到事件发生的Socket通道  
    	SocketChannel channel = (SocketChannel) key.channel(); 
    	SyncReaderZeroCopy reader = readerMap.get(channel);
    			
    	reader.read(channel);
    }  
      
      
    /** 
     * 启动客户端测试 
     * @throws IOException  
     */  
    public static void main(String[] args) throws IOException {  
        NIOServerParam client = new NIOServerParam(true,10004);  
        client.initServer();  
        
        System.out.println("NIOServer started!");
        client.listen();  
       
    }  
  
}
