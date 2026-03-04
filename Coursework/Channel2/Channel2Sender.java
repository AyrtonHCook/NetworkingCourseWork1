package NetworkingCourseWork1.Coursework.Channel2;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class Channel2Sender implements Runnable{
    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public void run(){
        // Initialise variables
        final int PORT = 55555;
        InetAddress clientIP = null;
        DatagramSocket2 sending_Socket = null;

        // Get IP address
        try{
            clientIP = InetAddress.getByName("localhost");
        } catch(UnknownHostException e){
            System.out.println("Sender Error: unable to get client IP address");
            System.exit(1);

        }

        // Create DatagramSocket
        try{
            sending_Socket = new DatagramSocket2();
        } catch(SocketException e){
            System.out.println("Sender Error: unable to create socket");
            System.exit(0);
        }

        // Main loop sending 1000 packet with value 0 - 999
        System.out.println("Sending...");
        for(int i = 0; i < 10000; i++){
            try{
                // add sequence number
                ByteBuffer buffer = ByteBuffer.allocate(12);
                buffer.putInt(i);
                Long timestamp = System.nanoTime();
                buffer.putLong(timestamp);
                byte[] block = buffer.array();

                DatagramPacket sending_Packet = new DatagramPacket(block, block.length, clientIP, PORT);

                sending_Socket.send(sending_Packet);
            } catch(IOException e){
                System.out.println("Sender Error: IOException occurred");
                System.exit(0);
            }
        }
        System.out.println("End of message.");
        sending_Socket.close();
    }
}
