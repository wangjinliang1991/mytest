package mytest.com.ai.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NioServer {
    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8888));
        serverSocketChannel.configureBlocking(false);

        //open selector
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // create single reactor thread
        new Thread(new SingleReactor(selector)).start();
    }

    static class SingleReactor implements Runnable {

        private final Selector selector;

        public SingleReactor(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select(1000);
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        processSelectionKey(selectionKey);
                        iterator.remove();
                    }
                } catch (Exception ignored) {

                }
            }
        }

        private void processSelectionKey(SelectionKey selectionKey) throws Exception {
            if (selectionKey.isValid()) {
                // connection is ready
                if (selectionKey.isAcceptable()) {
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                    // accept a connection
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    // non blocking
                    socketChannel.configureBlocking(false);
                    // register on selector of read
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }

                if (selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    // bytebuffer
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    socketChannel.read(byteBuffer);

                    // flip
                    byteBuffer.flip();
                    //print
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);

                    String msg = new String(bytes, "UTF-8");
                    System.out.println("received data from client: " + msg);

                    //write to client
                    byteBuffer.clear();
                    byteBuffer.put("hello client, I am server!".getBytes(StandardCharsets.UTF_8));
                    byteBuffer.flip();
                    socketChannel.write(byteBuffer);
                }
            }
        }
    }
}
