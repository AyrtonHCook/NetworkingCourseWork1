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
    private byte[] toFixedLength(BigInteger value, int length) {
    byte[] raw = value.toByteArray();

    if (raw.length == length) {
        return raw;
    }

    // remove leading sign byte 
    if (raw.length == length + 1 && raw[0] == 0) {
        byte[] trimmed = new byte[length];
        System.arraycopy(raw, 1, trimmed, 0, length);
        return trimmed;
    }

    // pad with zeros if too short
    if (raw.length < length) {
        byte[] padded = new byte[length];
        System.arraycopy(raw, 0, padded, length - raw.length, raw.length);
        return padded;
    }

    throw new IllegalArgumentException(
        "Value length " + raw.length + " does not fit fixed length " + length
    );
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

    public byte[] encryption(byte[] packet, BigInteger[] keys) {
        BigInteger plaintext = new BigInteger(1, packet);
        BigInteger n = keys[0];
        BigInteger e = keys[1];

        if (plaintext.compareTo(n) >= 0) {
            throw new IllegalArgumentException("Plaintext too large for modulus");
        }

        BigInteger ciphertext = plaintext.modPow(e, n);

        int modulusBytes = (n.bitLength() + 7) / 8;
        return toFixedLength(ciphertext, modulusBytes);
    }

    public byte[] decryption(byte[] packet, BigInteger[] keys) {
        BigInteger ciphertext = new BigInteger(1, packet);
        BigInteger n = keys[0];
        BigInteger d = keys[1];

        BigInteger plaintext = ciphertext.modPow(d, n);

        return toFixedLength(plaintext, AudioLayer.BLOCK_SIZE_BYTES);
    }

    public static void main(String[] args) {
        SecurityLayer secsender = new SecurityLayer();
        
        BigInteger[] keys = secsender.genKey();
        int key_len = keys[0].bitLength();

        BigInteger[] public_key = new BigInteger[]{keys[0], keys[2]};
        BigInteger[] private_key = new BigInteger[]{keys[0], keys[1]};

        System.out.println(Arrays.toString(keys));

        BigInteger n = new BigInteger("703066526860816942287777156524360518055407827466697888592637417094017290394214927213238702630010019208354126500461658975560802975422333552604204487001201499996009317236258193985719873760718327113990756776696447523712591431252788272680262606911924803725011434820171777624632289386938774482173393778794164294349478880860555882094299434844339731682977360227972993762000396127013057766571910731286146349393646303118375768106973861677576645599805045495129650024373721100949266595114653633990009909378416213797303343051164477280326136547850605219675280215277744315462858943320292282133275247902301017030013814132450843977644732048254142916679187036926338177846683893046819725585536853897655915975768867490106352387440015328384824418344747410570621392546753216416350361125678904879442464799809963796057258725585452176954978699955236746443270354904129250763803886733480347743599216701368701353701879950783046970049431332965560876979487233517420773130930232792627171289405283946719162220636842867130488937138100119791607157664796569868900594871421936223771711984325463868242398692392480097406966398802847857737141999903467547199996345214749749271894271686894987246999553471529584746065712764451499400974385431615784099135827980724502090836917");
        BigInteger E = new BigInteger("65537");
        BigInteger[] keys1 = new BigInteger[]{n, E};
        BigInteger d = new BigInteger("122768715891712911600184960850584887447183837794358768894733396420707903493772916475095041166025827545057061258087541775124247818037645683751201857717651860261445147419804671742261291107582900277591440263553628415419791817435294703641499081030563917387567799259686067765327859373241487025252793359545301374559949895670662397038118356536897079350290567319970748441526657205510435831372338471532701509413932409064905203018389747364667090837910934901601594746157634073565518820124389218111626614018441417072742717211308516990342132027001576607625675676085852357388299094364365547350858323343972608433273999251289614393084113838640581358875629514108535339637114044179689271674467376958272554749901855682701414006519938220937389298868728150155869963981233629711377860023969377574809719347124253364036175522878900296879583802486457551695833035379983908461348826282777591895902218685437795130970925959619581622212246935173996151630865616269016308608587803876566399340261062545658526592678888324527947006701700186435195812769238944000122912712472508250013842872534004946892764966610857534291481073164801647645648865781044991039328624931859201146376864219892902383483654102725297947472705510447478772093288755504222290627309971672387409815665");
        BigInteger[] keys2 = new BigInteger[]{n,d};
        
        byte[] block = new byte[512];
        SecureRandom random = new SecureRandom();
        random.nextBytes(block);
        byte[] cyphertext = secsender.encryption(block, keys1);
        byte[] decryptedtext = secsender.decryption(cyphertext, keys2);

        System.out.println();
        System.out.println(Arrays.toString(block));
        System.out.println();
        System.out.println(Arrays.toString(decryptedtext));

        if(Arrays.equals(block, decryptedtext)){
            System.out.println("same!!!");
        }
    }
}