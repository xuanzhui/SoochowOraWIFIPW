package com.xz.test;

import com.google.zxing.WriterException;
import com.xz.qr.GenerateQRCode;

import java.io.IOException;

/**
 * Created by xuanzhui on 15/7/15.
 */
public class GenerateQRCodeTest {
    public static void main(String[] args) throws IOException, WriterException {
        GenerateQRCode qr = new GenerateQRCode("you know what hehe");
        qr.generateQRCode();
    }
}
