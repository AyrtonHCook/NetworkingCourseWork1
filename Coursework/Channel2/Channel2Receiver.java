package NetworkingCourseWork1.Coursework.Channel2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;

import uk.ac.uea.cmp.voip.DatagramSocket2;


public class Channel2Receiver implements Runnable{
    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }
    
    public void run(){
        final int PORT = 55555;
        DatagramSocket2 receiving_Socket = null;

        try{
            receiving_Socket = new DatagramSocket2(PORT);
            receiving_Socket.setSoTimeout(3000);
        } catch(SocketException e){
            System.out.println("Receiver Error: unable to create socket");
            System.exit(0);
        }
        boolean running = true;
        ArrayList<Integer> packetArrayList = new ArrayList<Integer>();
        while(running){
            try{
                byte[] buffer = new byte[80];
                DatagramPacket packet = new DatagramPacket(buffer, 0, 80);
                receiving_Socket.receive(packet);

                //Get a string from the byte buffer
                String str = new String(buffer);
                //Display it
                //System.out.println(str);
                packetArrayList.add(Integer.valueOf(str.trim()));
                
            } catch(SocketTimeoutException e){
                System.out.println("Receiver time out, message ended");
                break;
            } catch(IOException e){
                System.out.println("Receiver Error: IOException occurred");
            } 
        }
        System.out.println("Receive over");
        receiving_Socket.close();
                Iterator<Integer> it = packetArrayList.iterator();
        Integer temp = null;
        float loss_count = 0;
        Integer burst_length = 0;
        ArrayList<Integer> burstArrayList = new ArrayList<Integer>();
        while(it.hasNext()){
            int now = it.next();
            System.out.println(now);
            if (temp == null){
                temp = it.next();
            }
            if (now - temp != 1){
                loss_count++;
                burst_length++;
            } 
            else{
                if(burst_length > 0){
                    burstArrayList.add(burst_length);
                    burst_length = 0;
                }
            }
            temp = now;
        }
        System.out.println("Amount of packets lost: " + loss_count);
        float packet_rate = loss_count/1000;
        System.out.printf("Packet rate: %.3f%n", packet_rate);
        Iterator<Integer> it1 = burstArrayList.iterator();
        int sum = 0;
        while(it1.hasNext()){
            sum += it1.next();
        }
        float average_burst_length = sum/burstArrayList.size();
        System.out.println("Average burst length: " + average_burst_length);
    }
}
