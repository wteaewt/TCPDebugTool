package com.svs.wt.tcpdebugtool.helper;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


public class CharHelper {
    private static final char[] Charts= "0123456789ABCDEF".toCharArray();
    private final static Map<Character,Integer> map;

    static {
        map = new HashMap<Character, Integer>() {{
            put('0',0);
            put('1',1);
            put('2',2);
            put('3',3);
            put('4',4);
            put('5',5);
            put('6',6);
            put('7',7);
            put('8',8);
            put('9',9);
            put('A',10);
            put('B',11);
            put('C',12);
            put('D',13);
            put('E',14);
            put('F',15);
        }};
    }

    public static String CharsToHexString(char[] cArray,int size ,boolean isSplit) {
        StringBuilder sb = new StringBuilder("");
        int bit;
        for (int i = 0; i < size; i++) {
            bit = (cArray[i] & 0x0f0) >> 4;
            sb.append(Charts[bit]);
            bit = cArray[i] & 0x0f;
            sb.append(Charts[bit]);
            if(isSplit&&i<size-1)
                sb.append(' ');
        }
        return sb.toString();
    }

    public static char[] HexStringToChars(String hex) {
        char[] org=hex.replaceAll(" ","").toUpperCase().toCharArray();
        char[] dis=new char[org.length/2];
        for(int i=0;i<org.length-1;i+=2){
            dis[i/2]=(char)(map.get(org[i+1])+(map.get(org[i])<<4));
        }
        return dis;
    }

    public static String C2H(char[] cArray) {
        int size=cArray.length;
        StringBuilder sb = new StringBuilder("");
        int bit;
        for (int i = 0; i < size; i++) {
            bit = (cArray[i] & 0x0f0) >> 4;
            sb.append(Charts[bit]);
            bit = cArray[i] & 0x0f;
            sb.append(Charts[bit]);
        }
        return sb.toString();
    }

    public static char[] H2C(String hex) {
        char[] org=hex.toCharArray();
        char[] dis=new char[org.length/2];
        for(int i=0;i<org.length;i+=2){
            dis[i/2]=(char)(map.get(org[i+1])+(map.get(org[i])<<4));
        }
        return dis;
    }

}
