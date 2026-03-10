import CMPC3M06.AudioRecorder;

public class SenderAudioLayer {

    public static final int BLOCK_SIZE_BYTES = 512;
    public static final int BLOCK_DURATION_MS = 32;

    private AudioRecorder recorder;

    public SenderAudioLayer() throws Exception {
        recorder = new AudioRecorder();
    }

    public byte[] getBlock() throws Exception {
        return recorder.getBlock();
    }

    public void close() {
        try { recorder.close(); } catch (Exception e) {}
    }
}