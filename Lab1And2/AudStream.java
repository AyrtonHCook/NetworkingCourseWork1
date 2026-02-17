import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;



public class AudStream {
    public static void main(String[] args) throws Exception{
        System.out.println("Start");
        
        AudioRecorder recorder = new AudioRecorder();
        AudioPlayer player = new AudioPlayer();

        int reverbTime = 10;

        for (int i = 0; i < Math.ceil(reverbTime / 0.032); i++){
            byte[] block = recorder.getBlock();
            player.playBlock(block);
        }
        recorder.close();
        player.close();
        
        System.out.println("End");

    }
}
