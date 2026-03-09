package NetworkingCourseWork1.Coursework;

import java.util.*;
import java.math.BigInteger;
import java.security.SecureRandom;

public class SecurityLayer{
    public final int key_Bytes = 512;
    public final int key_Bits = key_Bytes * 8;

    // getting greatest common dividor using euclidean algorithm
    // it should return 1 if there are no common divider
    // public static BigInteger GCD(BigInteger num1, BigInteger num2){
    //     // check if remainder reaches 0 
    //     if (num2 == BigInteger.ZERO){
    //         // if true return the last non-zero remainder
    //         return num1;
    //     }
    //     return GCD(num2, num1.remainder(num2));
    // }

    // extended euclidean algorithm
    public static BigInteger[] extendedGCD(BigInteger num1, BigInteger num2){
        BigInteger[] result = {num1, BigInteger.ONE, BigInteger.ZERO};
        // check if remainder reaches 0 
        if (num2.compareTo(BigInteger.ZERO) == 0){
            // if true return the last non-zero remainder
            return result;
        }
        result =  extendedGCD(num2, num1.remainder(num2));
        BigInteger gcd = result[0];
        BigInteger x1 = result[1];
        BigInteger y1 = result[2];
        BigInteger x = y1;
        BigInteger y = x1.subtract((num1.divide(num2)).multiply(y1));

        return result = new BigInteger[]{gcd, x, y};
    }

    public BigInteger[] genKey(){
        // choose two prime numbers p & q
        SecureRandom random = new SecureRandom();

        BigInteger p = BigInteger.probablePrime(key_Bits/2, random); // get prime number
        BigInteger q = BigInteger.probablePrime(key_Bits/2, random); // get prime number
        while (p.equals(q)) {
            q = BigInteger.probablePrime(key_Bits/2, random);
        }

        // compute n and z using p and q
        BigInteger n = p.multiply(q);
        BigInteger z = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // choose e where e<n and e has no common factor with z
        BigInteger e = new BigInteger("65537");
        
        // choose d where ed mod z == 1 using exdented euclidean algorithm
        BigInteger d = extendedGCD(e, z)[1];
        if (d.compareTo(BigInteger.ZERO) < 0) {
            d = d.add(z);
        }

        BigInteger[] key = {n, d, e};
        // public key = (n, e), private key = (n, d)
        return key;
    }

    public byte[] encryption(byte[] packet, BigInteger[] keys){
        BigInteger plaintext = new BigInteger(1, packet);
        BigInteger n = keys[0];
        BigInteger e = keys[1];
        BigInteger cyphertext = plaintext.modPow(e, n);
        byte[] cyphertextbyte = cyphertext.toByteArray();

        return cyphertextbyte;
    }   

    public byte[] decryption(byte[] packet, BigInteger[] keys){
        BigInteger cyphertext = new BigInteger(1, packet);
        BigInteger n = keys[0];
        BigInteger d = keys[1];
        BigInteger plaintext = cyphertext.modPow(d, n);
        byte[] plaintextbyte = plaintext.toByteArray();
        
        return plaintextbyte;
    }

    public static void main(String[] args) {
        SecurityLayer secsender = new SecurityLayer();
        
        BigInteger[] keys = secsender.genKey();
        int key_len = keys[0].bitLength();

        BigInteger[] public_key = new BigInteger[]{keys[0], keys[2]};
        BigInteger[] private_key = new BigInteger[]{keys[0], keys[1]};

        System.out.println(Arrays.toString(keys));
        
        byte[] block = new byte[512];
        SecureRandom random = new SecureRandom();
        random.nextBytes(block);
        byte[] cyphertext = secsender.encryption(block, public_key);
        byte[] decryptedtext = secsender.decryption(cyphertext, private_key);

        System.out.println();
        System.out.println(block.toString());
        System.out.println();
        System.out.println(decryptedtext.toString());

        if(Arrays.equals(block, decryptedtext)){
            System.out.println("same!!!");
        }
    }
} 