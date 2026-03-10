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
    public final int INTERLEAVER_SQUARE = VoIPSender2.INTERLEAVER_SQUARE;
    public final int HEADER_SIZE = PACKET_SIZE -BLOCK_SIZE;
    boolean running = true;
    boolean started = false;
    boolean[] check = new boolean[INTERLEAVER_SQUARE*2];
    byte[][] packet_Array = new byte[INTERLEAVER_SQUARE*2][PACKET_SIZE];

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
        int start = 0;
        boolean readFirst = true;
        while(running){
            j = i%(INTERLEAVER_SQUARE);
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
            
            // get order by reading sequence number>
            ByteBuffer buffer1 = ByteBuffer.wrap(packet.getData());
            int seq = buffer1.getInt();
            int index = seq%packet_Array.length;
            packet_Array[index] = buffer1.array();
            check[index] = true;
            System.out.printf("Packet %d received%n", seq);

            int row = j/INTERLEAVER_SIZE;
            int col = j%INTERLEAVER_SIZE;
            int expectindex = col*INTERLEAVER_SIZE+row;

            if(i >= INTERLEAVER_SQUARE && expectindex%INTERLEAVER_SQUARE == 0){
                if(readFirst){
                    start = 0;
                }else{
                    start = 16;
                }
                for(int k = start; k < INTERLEAVER_SQUARE+start; k++){
                    if(check[k]){
                        ByteBuffer payload = ByteBuffer.wrap(packet_Array[k]);
                        int cseq = payload.getInt();
                        seq_ArrayList.add(cseq);
                        Long ctime = payload.getLong();
                        time_ArrayList.add(ctime);
                        byte[] block = new byte[BLOCK_SIZE];
                        payload.get(block);
                        try{
                            audio.playBlock(block);
                            System.out.printf("Packet %d played%n", cseq);
                        }catch(Exception e){
                            System.err.println("Receiver Side Error: cannot play block");
                        }                         
                    } else{
                        try{
                            audio.playSilence();
                        }catch(Exception e){
                            System.err.println("Receiver Side Error: cannot play block");
                        }
                    }
                }
                Arrays.fill(check, start, INTERLEAVER_SQUARE+start, false);
                readFirst = !readFirst;
            }
            				
            
            i++;
		}
        receiving_Socket.close();
    }
    

    public void playBlocks(){
        while(running){
            long now = System.nanoTime();
            if(started){

            }
            
       } 
    }
}
