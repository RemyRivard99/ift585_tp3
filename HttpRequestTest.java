import org.junit.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class HttpRequestTest {

    @Test
    public void httpRequestByName() throws IOException {
        InetAddress ip2 = InetAddress.getByName("google.com");
        int HTTP_PORT = 80;

        try (Socket sock = new Socket(ip2, HTTP_PORT)){
            OutputStream out = sock.getOutputStream();
            PrintWriter writer = new PrintWriter(out, true);
            writer.println("GET / HTTP/1.1");
            writer.println("Host: www.google.com:80");
            writer.println("Connection: Close");
            writer.println();

            InputStream input = sock.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = reader.readLine()) != null)
                System.out.println(line);

        } catch (UnknownHostException e){
            System.out.println("Server not found: " + e.getMessage());
        }
    }

    @Test
    public void httpRequestByIpAddress() throws IOException {
        InetAddress ip2 = InetAddress.getByAddress(new byte[]{(byte)172, (byte)217, (byte)13, (byte)206});
        int HTTP_PORT = 80;

        try (Socket sock = new Socket(ip2, HTTP_PORT)){
            OutputStream out = sock.getOutputStream();
            PrintWriter writer = new PrintWriter(out, true);
            writer.println("GET / HTTP/1.1");
            writer.println("Host: www.google.com:80");
            writer.println("Connection: Close");
            writer.println();

            InputStream input = sock.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = reader.readLine()) != null)
                System.out.println(line);

        } catch (UnknownHostException e){
            System.out.println("Server not found: " + e.getMessage());
        }
    }
}
