package com.xz.test;

import com.xz.qr.GenerateQRCode;

import org.junit.Test;

/**
 * Created by xuanzhui on 15/7/15.
 */
public class GenerateQRCodeTest {
	@Test
    public void testQRGeneration() throws Exception {
        GenerateQRCode qr = new GenerateQRCode("bless me");
        qr.generateQRCode();
    }
}
