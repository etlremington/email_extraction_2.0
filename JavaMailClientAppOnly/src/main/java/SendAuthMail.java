import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendAuthMail{

public static void sendMail(String recepient) throws Exception{
		
		System.out.println("Preparing to send email");
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.ssl.trust", "*");
		
		final String myAccountEmail = "remingtonhotelserror@gmail.com";
		final String password = "jimqvzljdhkxkhxr";
		
		Session session = Session.getInstance(properties, new Authenticator() {
			@Override 
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(myAccountEmail, password);
			}
		});
		
		Message message = prepareMessage(session, myAccountEmail, recepient);
		Transport.send(message);
		System.out.println("Message sent successfully");
	}
	
	private static Message prepareMessage(Session session, String myAccountEmail, String recepient) {
		
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(myAccountEmail));
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
			message.setSubject("App Only Authentication Failed for Email Extraction");
			message.setText("Hey there, \n The app only authentication for email extraction failed. Kindly check the credentials and try again...!!!");
			return message;
		} catch (Exception ex) {
			Logger.getLogger(SendAuthMail.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
}