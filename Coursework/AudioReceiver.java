package NetworkingCourseWork1.Coursework;
import CMPC3M06.AudioPlayer;

public class AudioReceiver {
    public static final int BLOCK_SIZE_BYTES = 512;
    public AudioPlayer player;

    public AudioReceiver() throws Exception{
        AudioPlayer player   = new AudioPlayer();
    }
    
    public void playSilence() throws Exception {
            player.playBlock(new byte[BLOCK_SIZE_BYTES]);
        }

    public void playBlock(byte[] block) throws Exception {
        if (block == null || block.length != BLOCK_SIZE_BYTES) {
            // if something goes wrong play silence
            this.playSilence();
        } else {
            player.playBlock(block);
        }
    }

    public void close(){
        player.close();
    }
    
}
