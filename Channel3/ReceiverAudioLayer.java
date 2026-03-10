
import CMPC3M06.AudioPlayer;

public class ReceiverAudioLayer {
    public static final int BLOCK_SIZE_BYTES = 512;
    public static final int BLOCK_DURATION_MS = 32;
    public AudioPlayer player;

    public ReceiverAudioLayer() throws Exception{
        player   = new AudioPlayer();
    }
    
    public void playSilence() throws Exception {
            player.playBlock(new byte[BLOCK_SIZE_BYTES]);
        }

    public void playBlock(byte[] block) throws Exception {// if block does not arrive play silence so no crash 
        if (block == null || block.length != BLOCK_SIZE_BYTES) {
            player.playBlock(new byte[BLOCK_SIZE_BYTES]);
        } else {
            player.playBlock(block);
        }
    }

    public void close(){
        player.close();
    }
    
}
