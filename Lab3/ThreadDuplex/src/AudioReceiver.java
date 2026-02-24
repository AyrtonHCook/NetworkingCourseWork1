
import CMPC3M06.AudioPlayer;
/*
 * TextReceiver.java
 */

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class AudioReceiver implements Runnable{

    static DatagramSocket receiving_socket;

    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        //initialise variables
        int PORT = 55555;
        short authenticationKey = 10;
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
        int key = 15;
        while (running){

            try{



                byte[] buffer = new byte[514];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 514);
                receiving_socket.receive(packet);

                byte[] receivedPacket = packet.getData();
                // getting header and checking the authentication key
                short header = (short) (((receivedPacket[0] & 0xFF) << 8) | (receivedPacket[1] & 0xFF));
                if(header != authenticationKey){
                    System.out.println("Error: authentication failed");
                    System.exit(0);
                }
                else {
                    System.out.println("Sender varified");
                }
                // removing the header from the packet
                byte[] encrypted = Arrays.copyOfRange(receivedPacket, 2, receivedPacket.length);

                int len = encrypted.length;

                ByteBuffer unwrapDecrypt = ByteBuffer.allocate(len);
                ByteBuffer cipherText = ByteBuffer.wrap(encrypted);

                for(int j = 0; j < len/4; j++) {
                    int fourByte = cipherText.getInt();
                    fourByte = fourByte ^ key;
                    unwrapDecrypt.putInt(fourByte);
                }

                byte[] decryptedBlock = unwrapDecrypt.array();

                player.playBlock(decryptedBlock);

            } catch (IOException e){
                System.out.println("ERROR: TextReceiver: Some random IO error occured!");
                e.printStackTrace();

            }
        }
    }

}
