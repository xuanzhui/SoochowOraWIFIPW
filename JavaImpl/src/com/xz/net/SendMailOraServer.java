package com.xz.net;

import com.xz.qr.GenerateQRCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMailOraServer {
	private int port = 465;
	private String host = "stbeehive.oracle.com";
	private String mailFrom = "WIFIPW Notification";
	private InternetAddress[] mailTo;
	private String subject = "Clear Guest Wifi Password Notification";
	private String username;
	private String password;

	private String qrcodePath = GenerateQRCode.outputQRPath;

	//wifi password sent
	private String wifiPassword;
	
	private boolean debug = false;
	//Create a Properties object to contain settings for the SMTP protocol provider
	private Properties props;
	private Authenticator authenticator;
	
	public SendMailOraServer(String username, String password){
		this.username=username;
		this.password=password;
	}
	
	public SendMailOraServer(String username, String password, String mailToStr) throws AddressException{
		this(username, password);
		this.setMailTo(mailToStr);
	}
	
	public void setWifiPassword(String wifiPassword) {
		this.wifiPassword = wifiPassword;
	}

	//multiple receiver should be separated with ','
	public void setMailTo(String mailToStr) throws AddressException{
		String[] mailarr = mailToStr.split(",");
		List<String> totmp = new ArrayList<String>(mailarr.length);
		for (String str : mailarr){
			if (str.trim().length()!=0)
				totmp.add(str.trim());
		}
		mailTo = new InternetAddress[totmp.size()];
		for (int i=0;i<totmp.size();i++)
			mailTo[i]=new InternetAddress(totmp.get(i));
	}
	
	private void init(){
		props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		//props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.ssl.enable", true);
		
		//If SMTP authentication is required you must set the mail.smtp.auth property 
		//and construct a Authenticator instance that returns a PasswordAuthentication instance with your username and password.
		props.put("mail.smtp.auth", true);
		authenticator = new Authenticator() {
	        private PasswordAuthentication pa = 
	        		new PasswordAuthentication(SendMailOraServer.this.username, SendMailOraServer.this.password);
	        @Override
	        public PasswordAuthentication getPasswordAuthentication() {
	            return pa;
	        }
	    };
	}
	
	private void formatMessage(MimeMessage message) throws MessagingException, IOException{
		//tell the mail client that the text part and the image part are related 
		//and should be shown as a single item, not as separate pieces of the message. 
		//We do so by changing how we create the message content
		Multipart multipart = new MimeMultipart("related");
		
		//Create a MimeBodyPart instance to contain the HTML body part. 
		//Order is important, the preferred format of an alternative multi-part message should be added last.
		MimeBodyPart htmlPart = new MimeBodyPart();
		String htmlContent = "<html><p>Password: <b>"+this.wifiPassword+
				"</b></p><p><img src='cid:qrcode'></p><p>Enjoy!</p></html>";
		htmlPart.setContent(htmlContent, "text/html");
		
		//add the image part
		MimeBodyPart imagePart = new MimeBodyPart();		
		imagePart.attachFile(this.qrcodePath);
		imagePart.setContentID("<qrcode>");	
		//Notice how we tell mail clients that the image is to be displayed inline (not as an attachment) with getDisposition().
		imagePart.setDisposition(MimeBodyPart.INLINE);

		//Add both MimeBodyPart instances to the MimeMultipart instance 
		//and set the MimeMultipart instance as the MimeMessage.
		
		//multipart.addBodyPart(textPart);
		multipart.addBodyPart(htmlPart);
		multipart.addBodyPart(imagePart);
		message.setContent(multipart);
	}
	
	public void sendMessage() throws MessagingException, IOException{
		this.init();
		
		//Create a Session instance using the Properties object and the Authenticator object. 
	    //If SMTP authentication in not needed a null value can be supplied for the Authenticator.
	    Session session = Session.getInstance(props, authenticator);
	    session.setDebug(debug);
	    
	    MimeMessage message = new MimeMessage(session);
	    
        message.setFrom(new InternetAddress(mailFrom+"<"+username+">"));
        message.setRecipients(Message.RecipientType.TO, mailTo);
        message.setSubject(subject);
        message.setSentDate(new Date());

        this.formatMessage(message);
        
        Transport.send(message);
	}
}
