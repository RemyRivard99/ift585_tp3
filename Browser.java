import java.io.*;
import java.net.*;

class Browser {
    /**
     * Wikipedia:
     *      " Google Public DNS est un service de Google qui consiste à offrir des serveurs DNS récursifs aux
     *      utilisateurs d'Internet. Il a été annoncé le 9 décembre 2001.
     *
     *      Les adresses IP anycast des serveurs sont les suivantes :
     *
     *      IPv4 : 8.8.8.8 et 8.8.4.4
     *      IPv6 : 2001:4860:4860::8888 et 2001:4860:4860::88442 "
     */
    private static final int DNS_PORT = 53;
    private static final int HTTP_PORT = 80;

    public static void httpGET(String domain, String page) throws IOException {
        System.out.println();
        System.out.println("$$$$----DNS QUERY SECTION-----$$$$");
        System.out.println("    ----QUERY SECTION-----");

        InetAddress ip = InetAddress.getByAddress(new byte[]{8, 8, 8, 8});      //multicast DNS server address

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        /**
         *  {DNS header (query)}
         *  MessageId
         *  Flags - QR, OPCODE, AA, TC, RD, RA, res1, res2, res3, RCODE
         *  QDCOUNT - items dans section "question"
         *  ANCOUNT - items dans section "authority"
         *  NSCOUNT - items dans section "authority"
         *  ARCOUNT - items dans section "additional"
         */
        dos.writeShort(0x1234);     //MessageID
        dos.writeShort(0x0100);     // Flags
        dos.writeShort(0x0001);     //QDCOUNT
        dos.writeShort(0x0000);     //ANCOUNT - toujours 0 pour une requete, 1 pour une reponse du serveur.
        dos.writeShort(0x0000);     //NSCOUNT
        dos.writeShort(0x0000);     //ARCOUNT

        /**
         * {DNS message (query)}
         * QNAME - "The name being queried", le contenu de QNAME depend de QTYPE
         * QTYPE - Le type de ressource que l'on cherche
         * QCLASS - une categorie parent de QTYPE (de ce que je comprends)
         */
        //QNAME
        String[] domainParts = domain.split("\\.");
        System.out.println(domain + " has " + domainParts.length + " parts");
        for (int i = 0; i<domainParts.length; i++) {
            System.out.println("Writing: " + domainParts[i]);
            byte[] domainBytes = domainParts[i].getBytes("UTF-8");
            dos.writeByte(domainBytes.length);
            dos.write(domainBytes);
        }
        dos.writeByte(0x00);        //END of QNAME FIELD separator
        dos.writeShort(0x0001);     //QTYPE - A (un domaine de type IPV4), X0002 pour les nameservers
        dos.writeShort(0x0001);     //QCLASS - 01 = Internet. Pas beaucoup de chances qu'on utilise autre chose

        byte[] dnsQueryByteArray = baos.toByteArray();

        /** Montre a l'ecran les bytes envoye */
        System.out.println("Sending: " + dnsQueryByteArray.length + " bytes");
        for (int i =0; i< dnsQueryByteArray.length; i++) {
            System.out.print("0x" + String.format("%x", dnsQueryByteArray[i]) + " " );
        }

        /** Envoie la requete DNS a l'adresse 8.8.8.8, port 53 */
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsQuery = new DatagramPacket(dnsQueryByteArray, dnsQueryByteArray.length, ip, DNS_PORT);
        socket.send(dnsQuery);

        /** Attends une reponse du serveur DNS  */
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        /** Imprime le contenu du paquet recu */
        System.out.println();
        System.out.println("Received: " + packet.getLength() + " bytes");
        for (int i = 0; i < packet.getLength(); i++) {
            System.out.print(" 0x" + String.format("%x", buf[i]) + " " );
        }
        System.out.println();
        System.out.println();
        System.out.println("    ----ANSWER SECTION-----");
        /**
         *  {DNS header (answer)}
         *  MessageId
         *  Flags - QR, OPCODE, AA, TC, RD, RA, res1, res2, res3, RCODE
         *  QDCOUNT - items dans section "question"
         *  ANCOUNT - items dans section "authority"
         *  NSCOUNT - items dans section "authority"
         *  ARCOUNT - items dans section "additional"
         */
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(buf));
        System.out.println("MessageId: 0x" +    String.format("%x", din.readShort()));
        System.out.println("Flags: 0x" +        String.format("%x", din.readShort()));
        System.out.println("QDCOUNT: 0x" +      String.format("%x", din.readShort()));
        System.out.println("ANCOUNT: 0x" +      String.format("%x", din.readShort()));
        System.out.println("NSCOUNT: 0x" +      String.format("%x", din.readShort()));
        System.out.println("ARCOUNT: 0x" +      String.format("%x", din.readShort()));

        /**
         * {DNS message (answer)}
         * QNAME - "The name being queried", le contenu de QNAME depend de QTYPE
         * QTYPE - Le type de ressource que l'on cherche
         * QCLASS - une categorie parent de QTYPE (de ce que je comprends)
         */
        int recordsLength = 0;
        while ((recordsLength = din.readByte()) > 0) {     //Tant que l'on n'atteint pas un separateur (0x00)
            byte[] record = new byte[recordsLength];
            for (int i = 0; i < recordsLength; i++) record[i] = din.readByte();
            System.out.println("QNAME: " + new String(record, "UTF-8"));
        }
        System.out.println("QTYPE: 0x" +    String.format("%x", din.readShort()));
        System.out.println("QCLASS: 0x" +   String.format("%x", din.readShort()));

        /**
         * {DNS answer (answer only)}
         * NAME - Il s'agit d'un pointeur de quelque sorte ou bien il reflete QNAME.
         * TYPE - Le type de RECORD qui a ete retourne par le serveur DNS
         * CLASS - La classe du RECORD qui a ete routourne, proobablement "Internet" dans ce cas-ci
         * TTL - "caching time limit"
         * RLENGTH - longueur de RDATA en octets.
         * RDATA - Contenu de la reponse, envoye par le serveur
         */
        System.out.println("Name: 0x" +     String.format("%x", din.readShort()));
        System.out.println("Type: 0x" +     String.format("%x", din.readShort()));
        System.out.println("Class: 0x" +    String.format("%x", din.readShort()));
        System.out.println("TTL: 0x" +      String.format("%x", din.readInt()));

        short rdataLength = din.readShort();
        System.out.println("RLength: 0x" +  String.format("%x", rdataLength));

        System.out.print("RData: ");
        String rdata = "";
        for (int i = 0; i < rdataLength; i++ ) {
            rdata += (din.readByte() & 0xFF);
            if(i != rdataLength - 1) rdata += ".";
        }

        System.out.println(rdata);          //RDATA contient l'adresse IP du serveur que l'on veut contacter
        socket.close();

        System.out.println();
        System.out.println("$$$$----HTTP REQUEST SECTION-----$$$$");
        System.out.println();

        /** Envoie la requete HTTP a l'adresse retourne par le serveur DNS, port 80 */
        String[] ipParts = rdata.split("\\.");
        InetAddress httpIp = InetAddress.getByAddress(new byte[]{ (byte)((int)Integer.valueOf(ipParts[0])), (byte)((int)Integer.valueOf(ipParts[1])), (byte)((int)Integer.valueOf(ipParts[2])), (byte)((int)Integer.valueOf(ipParts[3]))});

        try (Socket sock = new Socket(httpIp, HTTP_PORT)){
            OutputStream out = sock.getOutputStream();
            PrintWriter writer = new PrintWriter(out, true);
            System.out.println("HTTP REQUEST: ");

            System.out.println("GET /" + page + " HTTP/1.1");
            writer.println("GET /" + page + " HTTP/1.1");

            System.out.println("Accept: text/html, image/gif, image/jpeg, image/tiff");
            writer.println("Accept: text/html, image/gif, image/jpeg, image/tiff");

            System.out.println("Host: www." + domain + ":" + HTTP_PORT);
            writer.println("Host: www." + domain + ":" + HTTP_PORT);

            System.out.println();
            writer.println();

            InputStream input = sock.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));


            System.out.println("--PRINT ANSWER CONTENT--: ");
            System.out.println("STATUS LINE: ");
            System.out.println(reader.readLine());
            System.out.println();

            System.out.println("HEADER SECTION: ");
            String line;
            while ((line = reader.readLine()) != null) {     //Tant que l'on n'atteint pas un separateur (0x00)
                if(line.equals("")) break;
                System.out.println(line);
            }
            System.out.println();

            System.out.println("CONTENT SECTION: ");
            //TODO: GET IMAGES
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (UnknownHostException e){
            System.out.println("Server not found: " + e.getMessage());
        }


    }
}