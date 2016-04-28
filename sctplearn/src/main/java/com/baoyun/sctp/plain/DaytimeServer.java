package com.baoyun.sctp.plain;



import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

@SuppressWarnings("restriction")
public class DaytimeServer {
    static int SERVER_PORT = 3456;
    static int US_STREAM = 0;
    static int FR_STREAM = 1;

    static SimpleDateFormat USformatter = new SimpleDateFormat(
                                "h:mm:ss a EEE d MMM yy, zzzz", Locale.US);
    static SimpleDateFormat FRformatter = new SimpleDateFormat(
                                "h:mm:ss a EEE d MMM yy, zzzz", Locale.FRENCH);

    public static void main(String[] args) throws IOException {
        SctpServerChannel ssc = SctpServerChannel.open();
        InetSocketAddress serverAddr = new InetSocketAddress(SERVER_PORT);
        ssc.bind(serverAddr);

        ByteBuffer buf = ByteBuffer.allocateDirect(60);
        CharBuffer cbuf = CharBuffer.allocate(60);
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetEncoder encoder = charset.newEncoder();

        while (true) {
            SctpChannel sc = ssc.accept();
            for(int i=0;i<100;i++){
	            /* get the current date */
	            Date today = new Date();
	            cbuf.put(USformatter.format(today)).flip();
	            encoder.encode(cbuf, buf, true);
	            buf.flip();
	
	            /* send the message on the US stream */
	            MessageInfo messageInfo = MessageInfo.createOutgoing(null,
	                                                                 US_STREAM);
	            sc.send(buf, messageInfo);
	
	            /* update the buffer with French format */
	            cbuf.clear();
	            cbuf.put(FRformatter.format(today)).flip();
	            buf.clear();
	            encoder.encode(cbuf, buf, true);
	            buf.flip();
	
	            /* send the message on the French stream */
	            messageInfo.streamNumber(FR_STREAM);
	            sc.send(buf, messageInfo);
	
	            cbuf.clear();
	            buf.clear();
	            
	            try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            
            
            
            sc.close();
        }
    }
}
