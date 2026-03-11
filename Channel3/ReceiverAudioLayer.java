import CMPC3M06.AudioPlayer;

public class ReceiverAudioLayer {
    public static final int BLOCK_SIZE_BYTES = 512;
    public static final int BLOCK_DURATION_MS = 32;
    public AudioPlayer player;

    public ReceiverAudioLayer() throws Exception {
        player = new AudioPlayer();
    }

    public void playSilence() throws Exception {
        player.playBlock(new byte[BLOCK_SIZE_BYTES]);
    }

    public void playBlock(byte[] audioBlock) throws Exception {
        if (audioBlock == null || audioBlock.length != BLOCK_SIZE_BYTES) {
            player.playBlock(new byte[BLOCK_SIZE_BYTES]);
        } else {
            player.playBlock(audioBlock);
        }
    }

    public void close() {
        player.close();
    }
}