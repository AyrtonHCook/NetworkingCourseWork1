import java.net.*;
import java.io.*;
import java.util.Vector;

import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;

public class AudioSender {
    static DatagramSocket sending_socket;

    public static void main (String[] args)throws Exception{

        //***************************************************
        //Port to send to
        int PORT = 55555;
        //IP ADDRESS to send to
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("000.000.000.000");
        } catch (UnknownHostException e) {
            System.out.println("ERROR: TextSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //***************************************************
        //Open a socket to send from
        //We dont need to know its port number as we never send anything to it.
        //We need the try and catch block to make sure no errors occur.

        //DatagramSocket sending_socket;
        try{
            sending_socket = new DatagramSocket();
        } catch (SocketException e){
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
        //***************************************************

        //***************************************************


        System.out.println("Sending Recording");

        AudioRecorder recorder = new AudioRecorder();

        //***************************************************

        //***************************************************
        //Main loop.
        int stoptime = 10;

        for (int i = 0; i < Math.ceil(stoptime / 0.032); i++){
            System.out.println("Start Recording");



            int reverbTime = 10;

            byte[] block = recorder.getBlock();

            try{
                //Make a DatagramPacket from it, with client address and port number
                DatagramPacket packet = new DatagramPacket(block, block.length, clientIP, PORT);

                //Send it
                sending_socket.send(packet);

            } catch (IOException e){
                System.out.println("ERROR: TextSender: Some random IO error occured!");
                e.printStackTrace();
            }
        }
        //Close the socket
        System.out.println("End Recording");
        sending_socket.close();
        //***************************************************
    }

}
