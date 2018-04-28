package org.camra.staffing.data.service;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.ByteBuffer;
import java.util.Base64;

@Service
public class PasswordHash {

    public static void main(String[] args) {
        System.out.println(new PasswordHash().hashPassword("letmein!"));
    }

    public String hashPassword(String password) {
        try {
            return tryHashPassword(password, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return password;
        }
    }

    public String hashPassword(String password, int salt) {
        try {
            return tryHashPassword(password, salt);
        } catch (Exception e) {
            e.printStackTrace();
            return password;
        }
    }

    private String tryHashPassword(String password, int salt) throws Exception {

        char[] passwordChars = password.toCharArray();
        byte[] saltBytes = ByteBuffer.allocate(4).putInt(salt).array();

        SecretKeyFactory skf = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA512" );
        PBEKeySpec spec = new PBEKeySpec( passwordChars, saltBytes, 1, 256 );
        SecretKey key = skf.generateSecret( spec );
        byte[] res = key.getEncoded();
        return Base64.getEncoder().encodeToString(res);
    }
}
