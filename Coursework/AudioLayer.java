import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;


public class AudioLayer {

    // Audio block constants
    public static final int BLOCK_SIZE_BYTES = 512;   // bytes per block
    public static final int SAMPLE_RATE      = 8000;  // samples per second (for calculating bit rates later on)
    public static final int BLOCK_DURATION_MS = 32;   // milliseconds per block

    private AudioRecorder recorder;
    private AudioPlayer   player;

    // Creates a new AudioLayer, initialises both the recorder and player,
    // and the recorder begins capturing audio immediately.
    // throws Exception if audio hardware cannot be opened
    public AudioLayer() throws Exception {
        recorder = new AudioRecorder();
        player   = new AudioPlayer();
    }

    // Records and returns one 32ms block of audio data (512 bytes)
    // If the audio data is already buffered this returns immediately
    // otherwise it blocks it until a full block is available.
    public byte[] getBlock() throws Exception {
        return recorder.getBlock();
    }

    // Plays the given block of audio data through the connected speakers.
    public void playBlock(byte[] block) throws Exception {
        if (block == null || block.length != BLOCK_SIZE_BYTES) {
            // if something goes wrong play silence
            player.playBlock(new byte[BLOCK_SIZE_BYTES]);
        } else {
            player.playBlock(block);
        }
    }

    // Plays a block of silence (512 zero bytes) through the speakers.
    public void playSilence() throws Exception {
        player.playBlock(new byte[BLOCK_SIZE_BYTES]);
    }

    // Closes the audio recorder and player.
    public void close() {
        try { recorder.close(); } catch (Exception e) { /* ignore */ }
        try { player.close();   } catch (Exception e) { /* ignore */ }
    }
}
