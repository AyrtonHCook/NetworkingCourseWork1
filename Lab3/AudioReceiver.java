/*
 * TextReceiver.java
 */

/**
 *
 * @author  abj
 */
import java.net.*;
import java.io.*;

public class AudioReceiver implements Runnable{
    
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
        
        AudioPlayer player = null;
        try{
            player = new AudioPlayer();
        } catch (LineUnavailableException e) {
            System.out.println("Error: cannot open audio player");
        }


        boolean running = true;
        
        while (running){
         
            try{

                byte[] buffer = new byte[512];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 512);

                receiving_socket.receive(packet);
                
                player.playBlock(packet.getData());

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
