package brain.utils;

import java.security.SecureRandom;

public class CodeGenerator {
    
    static SecureRandom rnd = new SecureRandom();
    
    //<editor-fold defaultstate="collapsed" desc="Pin">
    public static String generatePIN(int len){
        String digits = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for(int i=0; i<len; i++){
           sb.append(digits.charAt(rnd.nextInt(digits.length())));
        }
        return sb.toString();
    }
    //</editor-fold>
       
    //<editor-fold defaultstate="collapsed" desc="Otp Code">
    public static String generateOtp(){
        String referenceCode = randomString(8);
        return generatePIN(6);
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Sales Code">
    public static String generateSalesCode(){
        String salesCode = randomString(10);
        return salesCode;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Random code generator">
    public static String randomString(int len) {
        String AB = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(len);
        for(int i=0; i<len; i++){
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }
    //</editor-fold>
}
