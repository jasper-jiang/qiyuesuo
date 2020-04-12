package com.qiyuesuo.utils;

import org.junit.Test;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateKeySpec;

public class RSAUtil {

    /**
     * 使用模和指数生成RSA私钥
     * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA
     * /None/NoPadding】
     *
     * @param modulus
     *            模
     * @param exponent
     *            指数
     * @return
     */
    public static RSAPrivateKey getPrivateKey(String modulus, String exponent) {
        try {
            BigInteger b1 = new BigInteger(modulus);
            BigInteger b2 = new BigInteger(exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static RSAPrivateKey getPrivateKey() throws IOException {
        File key = new File("key");
        if(!key.exists()){
            key.mkdirs();
        }
        System.out.println(key.getAbsolutePath());
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("key\\private.key")));
        String modulus = bufferedReader.readLine();
        String exponent = bufferedReader.readLine();
        return getPrivateKey(modulus,exponent);
    }

    /**
     * 私钥解密
     *
     * @param data
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String decryptByPrivateKey(String data, RSAPrivateKey privateKey)
            throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        //模长
        int key_len = privateKey.getModulus().bitLength() / 8;
        byte[] bytes = data.getBytes();
        byte[] bcd = ASCII_To_BCD(bytes, bytes.length);
        System.err.println(bcd.length);
        //如果密文长度大于模长则要分组解密
        String ming = "";
        byte[][] arrays = splitArray(bcd, key_len);
        for(byte[] arr : arrays){
            ming += new String(cipher.doFinal(arr));
        }
        return ming;
    }
    /**
     * ASCII码转BCD码
     *
     */
    public static byte[] ASCII_To_BCD(byte[] ascii, int asc_len) {
        byte[] bcd = new byte[asc_len / 2];
        int j = 0;
        for (int i = 0; i < (asc_len + 1) / 2; i++) {
            bcd[i] = asc_to_bcd(ascii[j++]);
            bcd[i] = (byte) (((j >= asc_len) ? 0x00 : asc_to_bcd(ascii[j++])) + (bcd[i] << 4));
        }
        return bcd;
    }
    public static byte asc_to_bcd(byte asc) {
        byte bcd;

        if ((asc >= '0') && (asc <= '9'))
            bcd = (byte) (asc - '0');
        else if ((asc >= 'A') && (asc <= 'F'))
            bcd = (byte) (asc - 'A' + 10);
        else if ((asc >= 'a') && (asc <= 'f'))
            bcd = (byte) (asc - 'a' + 10);
        else
            bcd = (byte) (asc - 48);
        return bcd;
    }
    /**
     * BCD转字符串
     */
    public static String bcd2Str(byte[] bytes) {
        char temp[] = new char[bytes.length * 2], val;

        for (int i = 0; i < bytes.length; i++) {
            val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
            temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');

            val = (char) (bytes[i] & 0x0f);
            temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
        }
        return new String(temp);
    }
    /**
     * 拆分字符串
     */
    public static String[] splitString(String string, int len) {
        int x = string.length() / len;
        int y = string.length() % len;
        int z = 0;
        if (y != 0) {
            z = 1;
        }
        String[] strings = new String[x + z];
        String str = "";
        for (int i=0; i<x+z; i++) {
            if (i==x+z-1 && y!=0) {
                str = string.substring(i*len, i*len+y);
            }else{
                str = string.substring(i*len, i*len+len);
            }
            strings[i] = str;
        }
        return strings;
    }
    /**
     *拆分数组
     */
    public static byte[][] splitArray(byte[] data,int len){
        int x = data.length / len;
        int y = data.length % len;
        int z = 0;
        if(y!=0){
            z = 1;
        }
        byte[][] arrays = new byte[x+z][];
        byte[] arr;
        for(int i=0; i<x+z; i++){
            arr = new byte[len];
            if(i==x+z-1 && y!=0){
                System.arraycopy(data, i*len, arr, 0, y);
            }else{
                System.arraycopy(data, i*len, arr, 0, len);
            }
            arrays[i] = arr;
        }
        return arrays;
    }


    /**
     * 签名
     * @param sid
     * @return
     * @throws Exception
     */
    public static String getSignByPrivateKey(String sid) throws Exception{
        RSAPrivateKey privateKey = getPrivateKey();
        Signature sign = Signature.getInstance("SHA1WithRSA");
        sign.initSign(privateKey);
        sign.update(sid.getBytes("UTF-8"));
        byte[] signatureBytes = sign.sign();
        return new BASE64Encoder().encode(signatureBytes);
    }



//    /**
//     * 验签
//     * @param sid
//     * @param signData
//     * @return
//     * @throws Exception
//     */
//    public static boolean checkSign(String sid,String signData) throws Exception{
//        Signature sign = Signature.getInstance("SHA1WithRSA");
//        sign.initVerify(getPublicKey());
//        sign.update(sid.getBytes("UTF-8"));
//        return sign.verify(new BASE64Decoder().decodeBuffer(signData));
//    }

//    public static void main(String[] args) throws Exception {
//
//
//        String ming = "123213213213213";
//        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("key/public.key")));
//        BufferedReader bufferedReader2 = new BufferedReader(new FileReader(new File("key/private.key")));
//        bufferedReader.readLine();
//        String modulus = bufferedReader.readLine().substring(11);
//        String public_exponent = bufferedReader.readLine().substring(19);
//        String private_exponent = bufferedReader2.readLine();
//        RSAPublicKey pubKey = RSAUtils.getPublicKey(modulus, public_exponent);
//        RSAPrivateKey priKey = RSAUtils.getPrivateKey(modulus,private_exponent);
//
//        FileDao fileDao = new FileDao();
//        model.File file = new model.File();
//
//        file = fileDao.findById("6ce0d3b4b4644ad29703b14089ca4fff");
//
//        System.out.println(file.getEnvelope());
//        ming = RSAUtils.decryptByPrivateKey(file.getEnvelope(), priKey);
//        System.err.println(ming);
//
//        System.out.println(ming);
//
//        AESCrpyt.decrypt(file.getUrl(),file.getUrl()+".de",ming);
//
//        //生成公钥和私钥
////        RSAPublicKey publicKey = (RSAPublicKey) map.get("public");
////        RSAPrivateKey privateKey = (RSAPrivateKey) map.get("private");
////        System.err.println("public-------------\n"+map.get("public"));
////        System.out.println("----------------------------------------------");
////        System.out.println("private-------------\n"+((RSAPrivateKey) map.get("private")));
////        System.out.println("----------------------------------------------");
//        //模
//
////        System.out.println(bufferedReader.readLine());
////        fileReader.
////        String modulus = publicKey.getModulus().toString();
////        System.out.println(modulus);
//        //公钥指数
////        String public_exponent = publicKey.getPublicExponent().toString();
////        System.out.println("public_exponent:" + public_exponent);
//        //私钥指数
////        String private_exponent = privateKey.getPrivateExponent().toString();
////        System.out.println("private_exponent:" + private_exponent);
//        //明文
//
//        //使用模和指数生成公钥和私钥
////        RSAPublicKey pubKey = RSAUtils.getPublicKey(modulus, public_exponent);
//
////        System.out.println(pubKey.getPublicExponent().toString());
////        RSAPrivateKey priKey = RSAUtils.getPrivateKey(modulus, private_exponent);
//        //加密后的密文
////        String mi = RSAUtils.encryptByPublicKey(ming, pubKey);
////        System.err.println(mi);
//        //解密后的明文
////        ming = RSAUtils.decryptByPrivateKey(mi, priKey);
////        System.err.println(ming);
//    }

//    public static void main(String[] args) throws Exception {
//
////        String signatureBytes = getSignByPrivateKey("uuiduuiduuiduuiduuiduuiduuiduuid","54559086965365935217456028354922218842464628304198145477037595796447543406119410183792673859694528503631523871295679915738882911595746781022670663552823483729562786537726891685923738420124772161915111310000715197276151093109552659798057662151002713540978230462215251590777117993291400122131383224559401279745");
////
////
////        System.out.println(signatureBytes);
//        String s = "BILhvaF7rdCcBhtDZYEHlOkV9C/PxvpUdUicgXzQEJHrA7jjad/fH3l03OVLhccNKd9UemTzo9Nkl2ow0ulCxMWkS9Xn8PoRUB+znLYWXC65ejSiW5pUHoqYDoqanevSUwahuxe9vqZtUdg4ul+KP7T3 VREfOkUYjYjECP+uNb8=";
//        System.out.println(checkSign("uuiduuiduuiduuiduuiduuiduuiduuid", s));
//
//    }
}