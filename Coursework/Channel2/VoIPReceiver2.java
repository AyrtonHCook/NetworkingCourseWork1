package NetworkingCourseWork1.Coursework.Channel2;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import NetworkingCourseWork1.Coursework.AudioLayer;
import uk.ac.uea.cmp.voip.DatagramSocket2;

public class VoIPReceiver2 implements Runnable{
    // constants and other variables
    private AudioLayer audio = null;
    public final int PORT = VoIPSender2.PORT;
    public final int BLOCK_SIZE = VoIPSender2.BLOCK_SIZE;
    public final int PACKET_SIZE = VoIPSender2.PACKET_SIZE;
    public final int INTERLEAVER_SIZE = VoIPSender2.INTERLEAVER_SIZE;
    public final int HEADER_SIZE = PACKET_SIZE -BLOCK_SIZE;
    boolean running = true;

    // ArrayLists for analysis
    private ArrayList<Integer> seq_ArrayList = new ArrayList<Integer>();
    private ArrayList<Long> time_ArrayList = new ArrayList<Long>(); 
    
    // constructor
    public VoIPReceiver2(AudioLayer audioLayer){
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
        DatagramSocket2 receiving_Socket = null;

        // create datagramsocket
        try{
            receiving_Socket = new DatagramSocket2(PORT);
        } catch(SocketException e){
            System.err.println("Receiving Side Error: cannot create socket");
            e.printStackTrace();
            System.exit(0);
        }

        // main loop
        int i = 0;
        int j = 0;
        // buffer for incoming packets
        ByteBuffer packet_Buffer;
        boolean[] check = new boolean[INTERLEAVER_SIZE*INTERLEAVER_SIZE];
        byte[][] packet_Array = new byte[INTERLEAVER_SIZE*INTERLEAVER_SIZE][PACKET_SIZE];
        while(running){
            j = i%(INTERLEAVER_SIZE*INTERLEAVER_SIZE);
            // create packet object for incoming packet
            byte[] buffer = new byte[PACKET_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, PACKET_SIZE);
            try{
                receiving_Socket.receive(packet); // receiving incoming packet assign them to packet
            } catch(IOException e){
                System.err.println("Receiving Side Error: cannot receive packet");
                e.printStackTrace();
                System.exit(0);
            }
            
            packet_Buffer = ByteBuffer.wrap(packet.getData());
            int seq = packet_Buffer.getInt();
            Long time = packet_Buffer.getLong();
            time_ArrayList.add(time);
            byte[] block = new byte[BLOCK_SIZE];
            packet_Buffer.get(block);

            int index = seq%(INTERLEAVER_SIZE*INTERLEAVER_SIZE);
            packet_Array[index] = packet.getData();
            check[index] = true; 
            
            if(index == (INTERLEAVER_SIZE*INTERLEAVER_SIZE)-1){
                for(int k = 0; k < (INTERLEAVER_SIZE*INTERLEAVER_SIZE); k++){
                    if(check[k] == false){
                        try{
                            audio.playSilence();
                        } catch(Exception e){
                            System.err.print("Receiver side error: cannot play silence");
                        }
                    }else {
                        // int row = k / INTERLEAVER_SIZE;
                        // int col = k % INTERLEAVER_SIZE;
                        // int index1 = col * INTERLEAVER_SIZE + row;
                        ByteBuffer playBuffer = ByteBuffer.wrap(packet_Array[k]);
                        int seq1 = playBuffer.getInt();
                        Long time1 = playBuffer.getLong();
                        byte[] block1 = new byte[BLOCK_SIZE];
                        playBuffer.get(block1);
                        try{
                            audio.playBlock(block1);
                            System.out.printf("play packet %d%n", seq1);
                        } catch(Exception e){
                            System.err.print("Receiver side error: cannot play audio");
                        }
                    }
                }
                Arrays.fill(check, false);
            }
        }
        receiving_Socket.close();
    }
}
