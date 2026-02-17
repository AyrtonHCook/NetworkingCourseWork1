<<<<<<< HEAD:Lab3/ThreadDuplex/src/AudioReceiver.java

import CMPC3M06.AudioPlayer;
import javax.sound.sampled.*;
/*
 * TextReceiver.java
 */
=======
import CMPC3M06.AudioRecorder;
>>>>>>> 9deda5bc1f164545055eb0438d682cb4adc2d027:Lab3/AudioSender.java

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
<<<<<<< HEAD:Lab3/ThreadDuplex/src/AudioReceiver.java
import java.io.*;

public class AudioReceiver implements Runnable{
    
    static DatagramSocket receiving_socket;
    
=======

public class AudioSenderThread implements Runnable{

    public DatagramSocket sending_socket;

>>>>>>> 9deda5bc1f164545055eb0438d682cb4adc2d027:Lab3/AudioSender.java
    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        //initialise variables
        int PORT = 55555;
<<<<<<< HEAD:Lab3/ThreadDuplex/src/AudioReceiver.java
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

            } catch (IOException e){
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();
=======
        InetAddress clientIP = null;
        AudioRecorder recorder = null;
        byte[] block = null;
        try {
            // get client IP address
            clientIP = InetAddress.getByName("0000.0000.0000.0000");
        } catch (UnknownHostException e) {
            System.out.println("Errorr: Counld not find client IP.");
            System.exit(0);
        }

        try{
            // create socket
            sending_socket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Error: Unable to open socket.");
            System.exit(0);
        }

        try {
            // create audio recorder
            recorder = new AudioRecorder();
        } catch (LineUnavailableException e) {
            System.out.println("Error: Unable to open AudioRecorder.");
            System.exit(0);
        }

        System.out.println("Session start.");
        while(true){
            try{
                // get payload of the packet
                block = recorder.getBlock();
            } catch (IOException e) {
                System.out.println("Error: IO exception occurred.");
                System.exit(0);
            }

            try{
                // create and send packet
                DatagramPacket packet = new DatagramPacket(block, block.length, clientIP, PORT);
                sending_socket.send(packet);
            } catch (IOException e) {
                System.out.println("Error: IO exception occurred at packet sending.");
                System.exit(0);
>>>>>>> 9deda5bc1f164545055eb0438d682cb4adc2d027:Lab3/AudioSender.java
            }
        }
    }

}
..
