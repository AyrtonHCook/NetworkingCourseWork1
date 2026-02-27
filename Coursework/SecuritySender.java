package NetworkingCourseWork1.Coursework;

import java.util.*;

public class SecuritySender{

    public int[] publicKey;
    private int[] privateKey;

    // getting greatest common dividor using euclidean algorithm
    // it should return 1 if there are no common divider
    public static int GCD(int num1, int num2){
        // check if remainder reaches 0 
        if (num2 == 0){
            // if true return the last non-zero remainder
            return num1;
        }
        return GCD(num2, num1 % num2);
    }

    private int[] getPrivateKey(){
        return privateKey;
    }

    public int[] genKey(){
        // choose two prime numbers p & q
        int p = 9001;
        int q = 5039;

        // compute n and z using p and q
        int n = p*q;
        int z = (p-1) * (q-1);

        // choose e where e<n and e has no common factor with z
        int e = 2;
        while (e < n){
            if (GCD(e, z) == 1){
                break;
            }
            e += 1;
        }
        // choose d where ed mod z == 1
        int d = 0;

        int[] key = {n, e, d};
        // public key = (n, e), private key = (n, d)
        return key;
    }

    public byte[] securitySender(byte[] outGoingPacket , int[] key){


        return null;
    }

    public static void main(String[] args) {
        SecuritySender secsender = new SecuritySender();
        System.out.println(Arrays.toString(secsender.genKey()));
    }
} 