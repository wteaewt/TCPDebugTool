package com.svs.wt.tcpdebugtool.helper;

import java.util.HashMap;
import java.util.Map;



public class ByteHelper {
    private static final char[] Charts= "0123456789ABCDEF".toCharArray();
    private final static Map<Byte,Integer> map;

    static {
        map = new HashMap<Byte, Integer>() {{
            put((byte)'0',0);
            put((byte)'1',1);
            put((byte)'2',2);
            put((byte)'3',3);
            put((byte)'4',4);
            put((byte)'5',5);
            put((byte)'6',6);
            put((byte)'7',7);
            put((byte)'8',8);
            put((byte)'9',9);
            put((byte)'A',10);
            put((byte)'B',11);
            put((byte)'C',12);
            put((byte)'D',13);
            put((byte)'E',14);
            put((byte)'F',15);
        }};
    }

    public static String BytesToHexString(byte[] bArray,int size ,boolean isSplit) {
        StringBuilder sb = new StringBuilder("");
        int bit;
        for (int i = 0; i < size; i++) {
            bit = (bArray[i] & 0x0f0) >> 4;
            sb.append(Charts[bit]);
            bit = bArray[i] & 0x0f;
            sb.append(Charts[bit]);
            if(isSplit&&i<size-1)
                sb.append(' ');
        }
        return sb.toString();
    }

    public static byte[] HexStringToBytes(String hex) throws NullPointerException {
        byte[] org=hex.replaceAll(" ","").toUpperCase().getBytes();
        byte[] dis=new byte[org.length/2];
        for(int i=0;i<org.length-1;i+=2){
            dis[i/2]=(byte)(map.get(org[i+1])+(map.get(org[i])<<4));
        }
        return dis;
    }
}
