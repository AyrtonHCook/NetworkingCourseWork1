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

    // extended euclidean algorithm
    public static int[] extendedGCD(int num1, int num2){
        int[] result = {num1, 1, 0};
        // check if remainder reaches 0 
        if (num2 == 0){
            // if true return the last non-zero remainder
            return result;
        }
        result =  extendedGCD(num2, num1 % num2);
        int gcd = result[0];
        int x1 = result[1];
        int y1 = result[2];
        int x = y1;
        int y = x1 - (num1 / num2) * y1;

        return result = new int[]{gcd, x, y};
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
        // choose d where ed mod z == 1 using exdented euclidean algorithm
        int d = extendedGCD(e, z)[1];

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