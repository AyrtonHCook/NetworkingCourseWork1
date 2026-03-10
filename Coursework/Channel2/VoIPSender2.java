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
    public static final int INTERLEAVER_SQUARE = INTERLEAVER_SIZE*INTERLEAVER_SIZE;
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
        byte[][] buffer = new byte[INTERLEAVER_SQUARE*2][BLOCK_SIZE]; // first buffer index 0-15, second 16-31
        int start = 0;
        long nextSendTime = System.nanoTime(); // for schedule sending
        boolean readFirst = true;
        while(running){
            if(i >= INTERLEAVER_SQUARE*2 && i%INTERLEAVER_SQUARE == 0){
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
				if(readFirst){
                    start = 0;
                } else{
                    start = 16;
                }
                System.out.print(start);
                for(int k = start; k < (start+INTERLEAVER_SQUARE); k++){
                    try{
                        DatagramPacket packet = new DatagramPacket(buffer[k], buffer[k].length, client_Address, PORT);
                        sending_Socket.send(packet);
                        int sending_seq = ByteBuffer.wrap(packet.getData()).getInt();
                        System.out.printf("Packet %d sent%n", sending_seq);
                    } catch(IOException e){
                        System.err.println("Sender Side Error: IOException occurred");
                        e.printStackTrace();
                        System.exit(0);
                    }
                }
			    nextSendTime += BLOCK_DURATION;
                readFirst = !readFirst;
			}
            j = i%INTERLEAVER_SQUARE;
            int row = j/INTERLEAVER_SIZE;
            int col = j%INTERLEAVER_SIZE;
            int index = col*INTERLEAVER_SIZE+row;

            // check if the first part is full
            if((i/INTERLEAVER_SQUARE)%2 == 1){ // if true first part not free
                index += INTERLEAVER_SQUARE; // offset to read second part
            } 
            //System.out.printf("INDEX: %d, %d%n", index, i);
            
            // record audio
            byte[] block = null;
			try{
				block = audio.getBlock(); 
			} catch(Exception e){
				System.err.println("Sender Side Error: cannot get audio block");
				e.printStackTrace();
				System.exit(0);
			}
            
            // adding header, seq number + timestamp + block = 4 + 8 + 512 = 524 bytes
			ByteBuffer packet_Buffer = ByteBuffer.allocate(PACKET_SIZE);
			packet_Buffer.putInt(i); // add sequence number
			packet_Buffer.putLong(System.nanoTime()); // add timestamp
			packet_Buffer.put(block); // add audio block
			byte[] payload = packet_Buffer.array();
			buffer[index] = payload;

            i++;
        }
        sending_Socket.close();
    }    
}

