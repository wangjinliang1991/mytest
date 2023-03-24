package mytest.com.ai.bio;

import java.io.*;
import java.net.Socket;

public class BioClient {
    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader in = null;
        BufferedWriter out = null;

        try {
            socket = new Socket("127.0.0.1", 8888);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("ready to send data to server");

            out.write("hello server, I am client \n");
            out.flush();

            //receive data from server
            String line = in.readLine();
            System.out.println("received data from server: " + line);
        }  catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
