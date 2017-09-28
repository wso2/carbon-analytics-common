package org.wso2.carbon.databridge.commons.binary.test;

import junit.framework.Assert;
import org.junit.Test;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConverterUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public class BinaryMessageConvertUtilTest {

    @Test
    public void testEventBufferSize() {
        int stringSize = BinaryMessageConverterUtil.getSize("i♥apim)");
        int intSize = BinaryMessageConverterUtil.getSize(1);
        int longSize = BinaryMessageConverterUtil.getSize(1L);
        int doubleSize = BinaryMessageConverterUtil.getSize(1D);
        int floatSize = BinaryMessageConverterUtil.getSize(1F);
        int booleanSize = BinaryMessageConverterUtil.getSize(true);
        int intArraySize = BinaryMessageConverterUtil.getSize(new int[]{1, 2, 3});

        Assert.assertEquals("Expected byte length of String is 13", 13, stringSize);
        Assert.assertEquals("Expected byte length of Int is 4", 4, intSize);
        Assert.assertEquals("Expected byte length of Float is 4", 4, floatSize);
        Assert.assertEquals("Expected byte length of Long is 8", 8, longSize);
        Assert.assertEquals("Expected byte length of Double is 8", 8, doubleSize);
        Assert.assertEquals("Expected byte length of Boolean is 4", 1, booleanSize);
        Assert.assertEquals("Expected byte length of Int-Array  is 4", 4, intArraySize);
    }

    @Test
    public void testGetString() {
        Charset charset = Charset.forName("UTF-8");
        CharsetEncoder encoder = charset.newEncoder();
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = encoder.encode(CharBuffer.wrap("i♥apim)"));
        } catch (CharacterCodingException e) {

        }
        String returnString = BinaryMessageConverterUtil.getString(byteBuffer, 4);
        Assert.assertEquals("Expected return string is \'i♥\'", "i♥", returnString);
    }

    @Test
    public void loadData() {
        String exampleString = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. ";
        InputStream stream = null;
        try {
            stream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] dataArray = null;

        try {
            BinaryMessageConverterUtil.loadData(stream, dataArray);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
