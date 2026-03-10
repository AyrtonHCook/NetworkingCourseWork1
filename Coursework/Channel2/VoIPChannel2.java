package NetworkingCourseWork1.Coursework.Channel2;

import java.io.IOException;

import NetworkingCourseWork1.Coursework.AudioLayer;

public class VoIPChannel2 {
        public static void main(String[] args) {
        
        AudioLayer audioLayer = null;
        try{
            audioLayer = new AudioLayer();
        } catch(Exception e){
            System.err.println("Error: Cannot open audio layer");
            System.exit(0);
        }

        VoIPSender2 sender = new VoIPSender2(audioLayer);
        VoIPReceiver2 receiver = new VoIPReceiver2(audioLayer);
        
        System.out.println("Seesion started, press ENTER to end");
        sender.start();
        receiver.start();
        
        // this if statement is just for testing
        boolean y = true;
        if(y == true){
            try{
                System.in.read();
            } catch(IOException e){
                System.err.println("Duplex error: IOException");
                System.exit(0);
            }
        } else{
            try{
                Thread.sleep(3000);
            } catch(Exception e){
                System.err.println("Duplex error");
                System.exit(0);
            }
        }
        
        
        sender.stop();
        receiver.stop();
        try{
            Thread.sleep(1000);
        } catch(Exception e){
            System.err.println("Duplex error");
            System.exit(0);
        }
        System.out.println("Session ended");
        audioLayer.close();
        System.exit(1);
    }
}
