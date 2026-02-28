package NetworkingCourseWork1.Coursework.Channel2;
import java.net.SocketException;

import uk.ac.uea.cmp.voip.DatagramSocket2;
import java.io.IOException;
import java.net.*;

public class Channel2Sender implements Runnable{
    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public void run(){
        final int PORT = 55555;
        InetAddress clientIP = null;
        byte[] block = null;
        DatagramSocket2 sending_Socket = null;

        try{
            clientIP = InetAddress.getByName("localhost");
        } catch(UnknownHostException e){
            System.out.println("Sender Error: unable to get client IP address");
            System.exit(1);

        }

        try{
            sending_Socket = new DatagramSocket2();
        } catch(SocketException e){
            System.out.println("Sender Error: unable to create socket");
            System.exit(0);
        }

        System.out.println("Sending...");
        for(int i = 0; i < 1000; i++){
            try{
                String payload = Integer.toString(i);
                block = payload.getBytes();

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
