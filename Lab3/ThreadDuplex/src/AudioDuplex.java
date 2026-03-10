/*
 * AudioDuplex.java
 */

/**
 *
 * @author  abj
 */


public class AudioDuplex {
    
    public static void main (String[] args){
        
        ReceiverAudioLayer receiver = new ReceiverAudioLayer();
        AudioSender sender = new AudioSender();
        
        receiver.start();
        sender.start();
        
    }
    
}
