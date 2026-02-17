/*
 * TextReceiver.java
 */

/**
 *
 * @author  abj
 */
import java.net.*;
import java.io.*;
import CMPC3M06.AudioPlayer;

public class AudioSender implements Runnable{
    
    static DatagramSocket receiving_socket;
    
    public void start(){
        Thread thread = new Thread(this);
	thread.start();
    }
    
    public void run (){
     
        //***************************************************
        //Port to open socket on
        int PORT = 55555;
        //***************************************************
        
        //***************************************************
        //Open a socket to receive from on port PORT
        
        //DatagramSocket receiving_socket;
        try{
		receiving_socket = new DatagramSocket(PORT);
	} catch (SocketException e){
                System.out.println("ERROR: TextReceiver: Could not open UDP socket to receive from.");
		e.printStackTrace();
                System.exit(0);
	}
        //***************************************************
        
        //***************************************************
        //Main loop.
        
        // create audio player
        AudioPlayer player = null;
        try{
            player = new AudioPlayer();
        } catch (LineUnavailableException e) {
            System.out.println("Error: cannot open audio player");
        }

        boolean running = true;
        
        while (running){
            byte[] buffer = new byte[512];
            DatagramPacket packet = new DatagramPacket(buffer, 0, 512);

            try{

                receiving_socket.receive(packet);
                player.playback(packet.getData());
                
                //The user can type EXIT to quit
                if (str.substring(0,4).equals("EXIT")){
                     running=false;
                }
            } catch (IOException e){
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        receiving_socket.close();
        //***************************************************
    }
}
