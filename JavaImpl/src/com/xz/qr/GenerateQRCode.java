package com.xz.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuanzhui on 15/7/15.
 */
public class GenerateQRCode {
    
    private String content;
    private boolean whetherSetLogo = true;
    public static String logoPath = "resources/oracle.png";
    public static String outputQRPath = "qrcode.png";
    
    private static final String CHARSET = "UTF-8";
    private static final String FORMAT_NAME = "PNG";
    // 二维码尺寸
    private static final int QRCODE_SIZE = 300;
    // LOGO宽度
    private static final int LOGO_WIDTH = 60;
    // LOGO高度
    private static final int LOGO_HEIGHT = 60;
    
    public GenerateQRCode(String content){
        this.content=content;
    }

    public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public boolean getWhetherSetLogo() {
		return whetherSetLogo;
	}

	public void setWhetherSetLogo(boolean whetherSetLogo) {
		this.whetherSetLogo = whetherSetLogo;
	}  

	public String getLogoPath() {
		return logoPath;
	}
	
	public void setLogoPath(String logoPath) {
		this.setWhetherSetLogo(true);
		GenerateQRCode.logoPath = logoPath;
	}

	public String getOutputQRPath() {
		return outputQRPath;
	}

	public void setOutputQRPath(String outputQRPath) {
		GenerateQRCode.outputQRPath = outputQRPath;
	}

	public void generateQRCode() throws Exception {
        // 用于设置QR二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        // 设置QR二维码的纠错级别（H为最高级别）具体级别信息
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 设置编码方式
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = new MultiFormatWriter().encode(this.content,
                BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, hints);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();

        //convert BitMatrix to BufferedImage
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000
                        : 0xFFFFFFFF);
            }
        }

        if (this.getWhetherSetLogo()==true)
        	this.insertLOGOImage(image, GenerateQRCode.logoPath);

        //write into file
        ImageIO.write(image, FORMAT_NAME, new FileOutputStream(new File(GenerateQRCode.outputQRPath)));
    }
    
	/**
	 * 插入logo
	 * @param source 原图
	 * @param logoPath logo路径
	 * @throws Exception
	 */
    private void insertLOGOImage(BufferedImage source, String logoPath) throws Exception {  
        File file = new File(logoPath);  
        if (!file.exists()) {  
            return;  
        }  
        Image logo_Img = ImageIO.read(new File(logoPath));  
        int width = logo_Img.getWidth(null);  
        int height = logo_Img.getHeight(null);  
         
        if (width > LOGO_WIDTH) {  
            width = LOGO_WIDTH;  
        }  
        if (height > LOGO_HEIGHT) {  
            height = LOGO_HEIGHT;  
        }  
        // 获取压缩后的LOGO 
        Image image = logo_Img.getScaledInstance(width, height,  
                Image.SCALE_SMOOTH);  
        logo_Img = image;  
 
        // 插入LOGO  
        //BufferedImage.createGraphic2D()，获取图形绘制的上下文，或者说是画笔
        //你可以用得到的画笔，在图像上绘制各种图形和文本
        Graphics2D graph = source.createGraphics();  
        int x = (QRCODE_SIZE - width) / 2;  
        int y = (QRCODE_SIZE - height) / 2;  
        graph.drawImage(logo_Img, x, y, width, height, null);  
        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);  
        graph.setStroke(new BasicStroke(3f));  
        graph.draw(shape);  
        graph.dispose();  
    }
}
