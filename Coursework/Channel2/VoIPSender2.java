package NetworkingCourseWork1.Coursework.Channel2;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import NetworkingCourseWork1.Coursework.AudioLayer;
import uk.ac.uea.cmp.voip.DatagramSocket2;

public class VoIPSender2 implements Runnable {
    // intial variables or common varibales for sender and receiver
    private AudioLayer audio;
    public static final int PORT = 55555;
    public static final int BLOCK_SIZE = 512;
    public static final int PACKET_SIZE =524;
    public static final int INTERLEAVER_SIZE = 4;
    public static final long BLOCK_DURATION = 32_000_000;
    boolean running = true;

    // Constructer
    public VoIPSender2(AudioLayer audioLayer){
        audio = audioLayer;
    }

    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

    public void stop(){
        running = false;
    }

    public void run(){
        // create DatagramSocket2 object
        DatagramSocket2 sending_Socket = null;
        try{
            sending_Socket = new DatagramSocket2();
        } catch(SocketException e){
            System.err.println("Sender Side Error: cannot create socket");
            e.printStackTrace();
            System.exit(0);
        }

        // get client address
        InetAddress client_Address = null;
        try{
            client_Address = InetAddress.getByName("localhost");
        } catch(UnknownHostException e){
            System.err.println("Sender Side Error: cannot get client address");
            e.printStackTrace();
            System.exit(0);
        }

        // main loop
        // variables for packet and interleaver
        int i = 0; // packet squence number
        int j = 0;
        byte[][] buffer = new byte[INTERLEAVER_SIZE*INTERLEAVER_SIZE][BLOCK_SIZE];
        long nextSendTime = System.nanoTime(); // for schedule sending
        while(running){
            // creaate packet
            byte[] block = null;
            try{
                block = audio.getBlock(); // record audio
            } catch(Exception e){
                System.err.println("Sender Side Error: cannot get audio block");
                e.printStackTrace();
                System.exit(0);
            }

            // interleaving process
            j = i % (INTERLEAVER_SIZE*INTERLEAVER_SIZE);
            int row = j/INTERLEAVER_SIZE;
            int col = j%INTERLEAVER_SIZE;
            int index = col * INTERLEAVER_SIZE + row;

            // adding header, seq number + timestamp + block = 4 + 8 + 512 = 524 bytes
            ByteBuffer packet_Buffer = ByteBuffer.allocate(PACKET_SIZE);
            packet_Buffer.putInt(i); // add sequence number
            packet_Buffer.putLong(System.nanoTime()); // add timestamp
            packet_Buffer.put(block); // add audio block
            byte[] payload = packet_Buffer.array();
            buffer[index] = payload;

            //send packet once buffer is full
            if(i >= (INTERLEAVER_SIZE*INTERLEAVER_SIZE)){
                //wait until scheduled time
                long now = System.nanoTime();
                long sleepTime = nextSendTime - now;
                if(sleepTime > 0){
                    try{
                        Thread.sleep(sleepTime / 1_000_000, (int)(sleepTime % 1_000_000));
                    } catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
                try{
                    DatagramPacket packet = new DatagramPacket(buffer[j], PACKET_SIZE, client_Address, PORT);
                    sending_Socket.send(packet);
                    System.out.printf("Packet %d sent%n", i-(INTERLEAVER_SIZE*INTERLEAVER_SIZE));
                } catch(IOException e){
                    System.err.println("Sender Side Error: IOException occurred");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            nextSendTime += BLOCK_DURATION;
            i++;
        }
        sending_Socket.close();
    }
}
