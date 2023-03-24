package mytest.com.ai.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class NioClient {
    public static void main(String[] args) throws Exception{
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        new Thread(new SingleReactorClient(socketChannel,selector)).start();
    }

    static class SingleReactorClient implements Runnable{

        private final Selector selector;
        private final SocketChannel socketChannel;

        public SingleReactorClient(SocketChannel socketChannel, Selector selector) {
            this.socketChannel = socketChannel;
            this.selector = selector;
        }

        @Override
        public void run() {
            //connect to server
            try {
                doConnect(socketChannel, selector);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            while (true) {
                try {
                    selector.select(1000);
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        processKey(selectionKey);
                        iterator.remove();
                    }
                } catch (Exception e) {

                }
            }
        }

        private void processKey(SelectionKey selectionKey) throws Exception{
            if (selectionKey.isValid()) {
                if (selectionKey.isConnectable()) {
                   SocketChannel sc = (SocketChannel) selectionKey.channel();
                    if (sc.finishConnect()) {
                        sc.register(selector, SelectionKey.OP_READ);
                        doService(sc);
                    } else {
                        // connect failed, exit
                        System.exit(1);
                    }
                }

                if (selectionKey.isReadable()) {
                    SocketChannel sc = (SocketChannel) selectionKey.channel();
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    int readBytes = sc.read(readBuffer);
                    // socketChanel non blocked, check bytes via return value
                    if (readBytes > 0) {
                        // switch read/write mode
                        readBuffer.flip();
                        byte[] bytes = new byte[readBuffer.remaining()];
                        readBuffer.get(bytes);
                        String msg = new String(bytes, Charset.defaultCharset());
                        doServiceAfterRead(msg);
                    } else if (readBytes < 0) {
                        // -1 : channel closed
                        selectionKey.cancel();
                        sc.close();
                    } else {
                        // not received data
                    }
                }
            }
        }

        private void doServiceAfterRead(String msg) {
            System.out.println("received data from server successfully: "+msg);
        }

        private void doConnect(SocketChannel socketChannel, Selector selector) throws Exception{
            System.out.println("client start successfully, start connect to server.");
            boolean connect = socketChannel.connect(new InetSocketAddress("127.0.0.1", 8888));
            System.out.println("connect = "+connect);
            if (connect) {
                //read data
                socketChannel.register(selector, SelectionKey.OP_READ);
                System.out.println("client connect to server successfully, start to send data");
                doService(socketChannel);
            }else {
                socketChannel.register(selector, SelectionKey.OP_CONNECT);
            }
        }

        private void doService(SocketChannel socketChannel) throws Exception{
            System.out.println("client start to send data to server.");
            byte[] bytes = "hello server, I am nio client!".getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            socketChannel.write(writeBuffer);
        }
    }
}
