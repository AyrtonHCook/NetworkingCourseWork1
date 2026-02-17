import java.util.Vector;
import java.util.Iterator;

import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;

public class AudRandLoss {

   public static void main(String[] args) throws Exception{
        System.out.println("Recording");

        Vector<byte[]> voiceBuffer = new Vector<byte[]>();

        AudioRecorder recorder = new AudioRecorder(); 
        AudioPlayer player = new AudioPlayer();


        int recordTime = 10;
        double lossProb = 0.10;

        byte[] silentBlock = new byte[512];

        for (int i = 0; i < Math.ceil(recordTime/0.032); i++){
            double p = (double) (Math.random());
            byte[] block = recorder.getBlock();
            if (p < lossProb){
                block = silentBlock;
            }
            voiceBuffer.add(block);
        }

        recorder.close();

        System.out.println("Audio outputting");
        
        Iterator<byte[]> voiceItr = voiceBuffer.iterator();

        while (voiceItr.hasNext()){
            player.playBlock(voiceItr.next());
        }

        player.close();
        System.out.println("End");
    }

}
