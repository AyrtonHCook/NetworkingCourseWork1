package NetworkingCourseWork1.Coursework;
import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;


public class AudioLayer {

    // Audio block constants
    public static final int BLOCK_SIZE_BYTES = 512;   // bytes per block
    public static final int SAMPLE_RATE      = 8000;  // samples per second (for calculating bit rates later on)
    public static final int BLOCK_DURATION_MS = 32;   

    private AudioRecorder recorder;
    private AudioPlayer   player;

    //constructor
    public AudioLayer() throws Exception {
        recorder = new AudioRecorder();
        player   = new AudioPlayer();
    }

    //record audio
    public byte[] getBlock() throws Exception {
        return recorder.getBlock();
    }

    // Plays the given block of audio
    public void playBlock(byte[] block) throws Exception {
        if (block == null || block.length != BLOCK_SIZE_BYTES) {
            // if something goes wrong play silence
            System.out.println("packet error");
            player.playBlock(new byte[BLOCK_SIZE_BYTES]);
        } else {
            player.playBlock(block);
        }
    }

    // Plays a block of silence
    public void playSilence() throws Exception {
        player.playBlock(new byte[BLOCK_SIZE_BYTES]);
    }

    // Closes the audio recorder and player.
    public void close() {
        try { recorder.close(); } catch (Exception e) {}
        try { player.close();   } catch (Exception e) {}
    }
}
