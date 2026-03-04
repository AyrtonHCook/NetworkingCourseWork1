package NetworkingCourseWork1.Coursework.Channel2;

import java.net.*;
import java.util.*;
import uk.ac.uea.cmp.voip.DatagramSocket2;

public class VoIPLayer2 {
    final int  PORT = 55555;
    DatagramSocket socket;
    InetAddress clientIP;

    // For the sender side create InetAddress and DatagramSocket
    public void send_init(String address){
        try {
            clientIP = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            System.out.println("ERROR: TextSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }
        try{
            socket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void receive_init(){
        try{
            socket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
    }
}
