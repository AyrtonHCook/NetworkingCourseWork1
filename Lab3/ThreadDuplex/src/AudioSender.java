import CMPC3M06.AudioRecorder;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;

public class AudioSender implements Runnable{

    public DatagramSocket sending_socket;

    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        //initialise variables
        int PORT = 55555;
        InetAddress clientIP = null;
        AudioRecorder recorder = null;
        byte[] block = null;
        try {
            // get client IP address
            clientIP = InetAddress.getByName("localhost");
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
            }
        }
    }

}