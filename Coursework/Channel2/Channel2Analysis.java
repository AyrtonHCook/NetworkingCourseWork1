package NetworkingCourseWork1.Coursework.Channel2;

public class Channel2Analysis{
    

    public static void main(String[] args) {
        Channel2Sender sender = new Channel2Sender();
        Channel2Receiver receiver = new Channel2Receiver();
        sender.start();
        receiver.start();
    }
}