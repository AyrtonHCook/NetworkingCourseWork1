package NetworkingCourseWork1.Coursework.Channel2;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

import uk.ac.uea.cmp.voip.DatagramSocket2;


public class Channel2Receiver implements Runnable{
    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public void run(){
        // initialise variables
        final int PORT = 55555;
        DatagramSocket2 receiving_Socket = null;

        // Creates DatagramSocket
        try{
            receiving_Socket = new DatagramSocket2(PORT);
            receiving_Socket.setSoTimeout(3000);
        } catch(SocketException e){
            System.out.println("Receiver Error: unable to create socket");
            System.exit(0);
        }

        // Main loop
        boolean running = true;
        // ArrayList to store values of incoming packets
        ArrayList<Integer> packetArrayList = new ArrayList<Integer>();
        ArrayList<Float> delayArrayList = new ArrayList<Float>();
        while(running){
            try{
                // Create DatagramPacket to store incoming packets in a buffer
                byte[] buffer = new byte[12];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 10);
                receiving_Socket.receive(packet);

                // Put data into the ByteBuffer and seperate squence number and timestamp
                ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());
                packetArrayList.add(wrapped.getInt());
                
                // Get delay from timestamp
                float delay = (System.nanoTime() - wrapped.getLong()) / 1_000_000f;
                delayArrayList.add(delay);

            } catch(SocketTimeoutException e){
                System.out.println("Receiver time out, message ended");
                break;
            } catch(IOException e){
                System.out.println("Receiver Error: IOException occurred");
            } 
        }
        System.out.println("Receive over");
        receiving_Socket.close();

        // Calculations for analysis
        Iterator<Integer> it = packetArrayList.iterator();
        Integer temp = null;
        float loss_count = 0;
        Integer burst_length = 0;
        ArrayList<Integer> burstArrayList = new ArrayList<Integer>();

        // Check beginning of the array see if there are loss from the beginning
        if(packetArrayList.get(0) > 0){
            burstArrayList.add(packetArrayList.get(1));
        }

        // Check end of the array see if there are loss at the end
        if(packetArrayList.get(packetArrayList.size() - 1) < 9999){
            burstArrayList.add(9999 - packetArrayList.get(packetArrayList.size() - 1));
        }

        // Iterate through the arrayList to check if there are packet lost
        int now = it.next();
        while(it.hasNext()){ 
            int next = it.next();
            if(next - now != 1){
                burst_length = next - now - 1;
                burstArrayList.add(burst_length);
            }
            now = next;
        }

        // get average burst length
        Iterator<Integer> it1 = burstArrayList.iterator();
        int sum = 0;
        int max_burst_len = 0;
        while(it1.hasNext()){
            now = it1.next();
            sum += now;
            if (now > max_burst_len){
                max_burst_len = now;
            }
        }
        float average_burst_length = sum / burstArrayList.size();

        // get average delay
        Iterator<Float> it2 = delayArrayList.iterator();
        float sum1 = 0;
        float max = 0;
        float now1;
        while(it2.hasNext()){
            now1 = it2.next();
            sum1 += now1;
            if (now1 > max){
                max = now1;
            }
        }
        float average_delay = sum1 / delayArrayList.size();

        // Calculations of packet rate, maximum burst length and average burst length
        System.out.println("Amount of packets received: " + packetArrayList.size());
        System.out.println("Amount of packets lost: " + (10000 - packetArrayList.size()));
        float packet_rate = (10000 - packetArrayList.size())/10000f * 100;
        System.out.printf("Packet loss rate: %.2f%%%n", packet_rate);
        System.out.println("Maximum burst length: " + max_burst_len);
        System.out.println("Average burst length: " + average_burst_length);
        System.out.printf("Maximum delay: %.2fms %n", max);
        System.out.printf("Average delay: %.2fms %n", average_delay);
    }
}
