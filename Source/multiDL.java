import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class multiDL implements Runnable {
    private URL url;
    private int port;
    private multiDL(String url_, File filename){
        try {
            url = new URL(url_);
            if (url.getPort() == -1){
                port = 80;
            }else {
                port = url.getPort();
            }
        }catch (MalformedURLException ex){
            ex.printStackTrace();
        }
    }
    private Selector selector;
    public void run(){
        SocketChannel channel;
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(new InetSocketAddress(url.getHost(), port));

            while (!Thread.interrupted()){
                selector.select(1024);
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()){
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;
                    if (key.isConnectable()){
                        System.out.println("Thread connected");
                        connect(key);
                    }
                    if (key.isWritable()){
                        write(key);
                    }
                    if (key.isReadable()){
                        read(key);
                    }
                }
            }

        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void close(){
        try {
            selector.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer readerBuffer = ByteBuffer.allocate(1024);
        readerBuffer.clear();
        int length;
        try{
            length = channel.read(readerBuffer);
        } catch (IOException ex){
            System.out.println("Problem with read, close conn");
            key.cancel();
            channel.close();
            return;
        }
        if (length == -1){
            System.out.println("Nothing was read from server");
            channel.close();
            key.cancel();
        }
        readerBuffer.flip();
        byte[] buff = new byte[1024];
        readerBuffer.get(buff, 0, length);
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(ByteBuffer.wrap(HelperFX.getDLRequest(url.getHost(), url.getPath()).getBytes()));
        key.interestOps(SelectionKey.OP_READ);
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()){
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_WRITE);
    }

}