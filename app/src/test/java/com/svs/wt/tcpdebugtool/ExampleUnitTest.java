package com.svs.wt.tcpdebugtool;

import com.svs.wt.tcpdebugtool.helper.ByteHelper;
import com.svs.wt.tcpdebugtool.helper.CharHelper;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void charsToHexString_isCorrect() throws Exception {
        char[] temps=new char[]{1,2,3,4,5};
        String str=CharHelper.CharsToHexString(temps,temps.length,true);
        assertEquals("01 02 03 04 05", str);
        str=CharHelper.CharsToHexString(temps,temps.length,false);
        assertEquals("0102030405", str);
    }
    @Test
    public void HexStringToChars_isCorrect() throws Exception {
        char[] temps=new char[]{1,2,3,4,5,1,2,3,4,5,1,2,3,4,5,1,2,3,4,5};
        String str="0102030405010203040501020304050102030405";
        char[] res=CharHelper.HexStringToChars(str);
        assertEquals(String.valueOf(temps), String.valueOf(res));

    }

    @Test
    public void bytesToHexString_isCorrect() throws Exception {
        byte[] temps=new byte[]{1,2,3,4,5,1,2,3,4,5,1,2,3,4,5,1,2,3,4,5};
        String str= ByteHelper.BytesToHexString(temps,temps.length,true);
        assertEquals("01 02 03 04 05 01 02 03 04 05 01 02 03 04 05 01 02 03 04 05", str);
        str=ByteHelper.BytesToHexString(temps,temps.length,false);
        assertEquals("0102030405010203040501020304050102030405", str);
    }
    @Test
    public void HexStringToBytes_isCorrect() throws Exception {
        byte[] temps=new byte[]{1,2,3,4,5,1,2,3,4,5,1,2,3,4,5,1,2,3,4,5,1,2,3,4,5,1,2,3,4,5,1,2,3,4,5,1,2,3,4,5};
        String str="01020304050102030405010203040501020304050102030405010203040501020304050102030405";
        byte[] res=ByteHelper.HexStringToBytes(str);
        assertEquals(new String(temps), new String(res));

    }

}