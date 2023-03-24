package mytest.com.ai.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BioServer {
    public static void main(String[] args) throws Exception{
        // socket server

        ServerSocket serverSocket = new ServerSocket(8888);
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ServerHandler(socket)).start();
        }
    }

    static class ServerHandler implements Runnable {
        private final Socket socket;

        public ServerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

                while (true) {
                    String line = reader.readLine();
                    System.out.println("server received data from client: "+line);

                    writer.write("send client data \n");
                    writer.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

}
