import CMPC3M06.AudioPlayer;
import javax.sound.sampled.LineUnavailableException;
import java.io.*;
import java.net.*;
import java.util.*;


public class AudioReceiver {
    public static void main(String[] args) {

        //define receiving port
        int PORT = 55555;

        DatagramSocket receiving_socket = null;
        try{
            receiving_socket = new DatagramSocket(55555);
            receiving_socket.setSoTimeout(32);
        } catch (SocketException e) {
            System.out.println("Error: cannot open socket");
            e.printStackTrace();
            System.exit(0);
        }
        
        // create audio player
        AudioPlayer player = null;
        try{
            player = new AudioPlayer();
        } catch (LineUnavailableException e) {
            System.out.println("Error: cannot open audio player");
        }

        //main loop
        boolean running = true;
        while(running){
            //Receive a DatagramPacket (each packet will be a block)
            byte[] buffer = new byte[512];
            DatagramPacket packet = new DatagramPacket(buffer, 0, 512);

            // playing received block
            try{
                receiving_socket.receive(packet);
                player.playBlock(packet.getData());
            } catch (SocketTimeoutException e){
                // end loop if message ended
              System.out.println("End of message");
            } catch (IOException e) {
                System.out.println("Error: IOException occured");
                e.printStackTrace();
                System.exit(0);
            }

        }

        //Close audio output
        player.close();
    }
}
