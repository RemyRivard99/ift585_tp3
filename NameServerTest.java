import org.junit.Test;

import java.io.IOException;
import java.net.*;

public class NameServerTest {

    //on envoie une querry a une nameserver
    @Test
    public void test1() throws IOException {

        /**
         * "A" DNS records for manytools.org [Google DNS servers]:
         * manytools.org. 599 IN A 62.221.205.57
         */
        byte[] ipAddr = new byte[]{(byte)62, (byte)221, (byte)205, (byte)157};
        InetAddress address = InetAddress.getByAddress("manytools.org.", ipAddr );

        DatagramSocket socket = new DatagramSocket();

        socket.connect(address, 53);

        String test = "aegasv";
        DatagramPacket packet = new DatagramPacket(test.getBytes(),test.getBytes().length);
        socket.send(packet);

        DatagramPacket response = new DatagramPacket(new byte[2014], 2014);
        socket.receive(response);
    }
}
