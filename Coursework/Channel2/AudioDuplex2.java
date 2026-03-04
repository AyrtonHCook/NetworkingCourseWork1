package NetworkingCourseWork1.Coursework.Channel2;

public class AudioDuplex2 {
        public static void main(String[] args) {
        AudioSender2 sender = new AudioSender2();
        AudioReceiver2 receiver = new AudioReceiver2();
        sender.start();
        receiver.start();
    }
}
