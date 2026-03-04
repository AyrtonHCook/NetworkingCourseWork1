package NetworkingCourseWork1.Coursework.Channel2;

import java.io.IOException;
import java.net.*;
import java.util.*;
import javax.sound.sampled.LineUnavailableException;
import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket2;

public class AudioReceiver2 implements Runnable{
    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run(){

    }
    
}
