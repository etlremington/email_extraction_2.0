import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.*;
import java.time.LocalDate;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.MessageCollectionPage;

public class JavaMailClient {

	// Declare the properties file
	final static Properties oAuthProperties = new Properties();
	final static Logger logger = LogManager.getLogger(JavaMailClient.class.getName());
	static String subject = null;
	static String fileName = null;
	static String path = null;
	static Map<String,String> getProperty = new HashMap<String,String>();

	// main function
	public static void main(String[] args) {

		try {
			// Fetches the properties file from the resources folder
			oAuthProperties.load(JavaMailClient.class.getResourceAsStream("oAuth.properties"));
		} catch (IOException e) {
			logger.error("Unable to read OAuth configuration. Make sure you have a properly formatted oAuth.properties file. See README for details.");
			try {
				SendAuthMail.sendMail("akshitrastogi@remingtonhotels.com");
			} catch (Exception e1) {
				logger.error("Unable to send email.");
			}
			return;
		}

		// Method to initialize the graph
		initializeGraph(oAuthProperties);

		// Fetches the current user and displays the email and username
		greetUser(oAuthProperties);
		
		//Fetching the properties from the property file
		getProperty = getProperty();

		// using iterators
        Iterator<Map.Entry<String, String>> itr = getProperty.entrySet().iterator();
          
        while(itr.hasNext())
        {
             Map.Entry<String, String> entry = itr.next();
             if(entry.getKey().equalsIgnoreCase("path")) 
            	 path = entry.getValue();
             if(entry.getKey().equalsIgnoreCase("subject")) 
            	 subject = entry.getValue();
             if(entry.getKey().equalsIgnoreCase("fileName")) 
            	 fileName = entry.getValue();
        }
        
        logger.info("path is:" + path);
        logger.info("subject is:" + subject);
        logger.info("fileName is:" + fileName);
      
        // Get the attachment from the email 
 		getAttachments(oAuthProperties, subject, path, fileName);
		 
	}

	/*
	 * Method : initializeGraph(Properties properties) Return Type : void Purpose :
	 * The purpose of this method is to initialize the graph.
	 */
	private static void initializeGraph(Properties properties) {
		try {
			Graph.ensureGraphForAppOnlyAuth(properties);
		} catch (Exception e) {
			logger.error("Error initializing Graph for user auth");
		}
	}

	/*
	 * Method : listInbox() Return Type : void Purpose : The purpose of this method
	 * is list out all the messages for the current page.
	 */
	private static void listInbox(Properties properties, String subject) {
		try {

			final MessageCollectionPage messages = Graph.getInbox(properties);
			LocalDate currentDate = LocalDate.now();

			// The below statements gets the previous date in the format "dd/MM/yy"
			LocalDate previousDate = currentDate.minusDays(1);
			DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("dd/MM/yy");

			// Output each message's details
			for (Message message : messages.getCurrentPage()) {
				if (message.subject.contains(subject) && message.receivedDateTime.format(dateformatter).equals(dateformatter.format(previousDate))) {
					logger.info("  Message: " + message.subject);
					logger.info("  From: " + message.from.emailAddress.name);
					logger.info("  Status: " + (message.isRead ? "Read" : "Unread"));
					logger.info("  Received: " + message.receivedDateTime
							// Values are returned in UTC, convert to local time zone
							.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
							.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
				}
			}

			final Boolean moreMessagesAvailable = messages.getNextPage() != null;
			System.out.println("\nMore messages available? " + moreMessagesAvailable);
		} catch (Exception e) {
			logger.error("Error getting inbox");
		}
	}

	/*
	 * Method : getAttachments() Return Type : void Purpose : The purpose of this
	 * method is get all the attachments from the current page.
	 */
	private static void getAttachments(Properties properties, String subject, String path, String fileName) {

		try {

			final MessageCollectionPage messages = Graph.getInbox(properties);
			LocalDate currentDate = LocalDate.now();
			LocalDate previousDate = currentDate.minusDays(1);

			// The below statements gets the previous date in the format "dd/MM/yy"
			DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("dd/MM/yy");
			DateTimeFormatter timestamp = DateTimeFormatter.ofPattern("ddMMyyyy");

			for (Message message : messages.getCurrentPage()) {
				if (message.subject.contains(subject) && message.receivedDateTime.format(dateformatter).equals(dateformatter.format(previousDate))) {
					logger.info("Message: " + message.subject);
					logger.info("Message has attachment ?: " + message.hasAttachments);
						
					if (message.hasAttachments) {
						AttachmentCollectionPage attachmentPage = Graph.getAttachments(properties, message.id);
						for (Attachment attachment : attachmentPage.getCurrentPage()) {
							if (attachment instanceof FileAttachment) {
								// Specifies the target directory where the downloaded attachments needs to be
								// stored.
								if(fileName == null) {
									fileName = attachment.name;
								}
								if(attachment.name.substring(attachment.name.length() - 4, attachment.name.length()).equalsIgnoreCase(".csv")) {
									File fileDown = new File(path + fileName + currentDate.format(timestamp) + attachment.name.substring(attachment.name.length() - 4, attachment.name.length()));
									Files.write(fileDown.toPath(), ((FileAttachment) attachment).contentBytes);
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error("Error getting attachments");
		}
	}

	/*
	 * Method : greetUser() Return Type : void Purpose : The purpose of this method
	 * is greet the user before displaying all the options.
	 */
	private static void greetUser(Properties properties) {
		try {
			final User user = Graph.getUser(properties);
			// For Work/school accounts, email is in mail property
			// Personal accounts, email is in userPrincipalName
			final String email = user.mail == null ? user.userPrincipalName : user.mail;
			logger.info("Hello, " + user.displayName + "!");
			logger.info("Email: " + email);
		} catch (Exception e) {
			logger.error("Error getting user");
			logger.error("Error getting user");
		}
	}

	/*
	 * Method : getPropertySubject() 
	 * Return Type : int 
	 * Purpose : The purpose of this method is to give the count of the rows in the property file.
	 */
	public static Map<String, String> getProperty() {

		File file = new File("D:\\FILES\\properties.txt");
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		Map<String, String> fileproperty = new HashMap<String, String>();
		String line = null;

		try {

			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			
			line = bufferedReader.readLine();
			fileproperty.put("subject", line);
			
			line = bufferedReader.readLine();
			fileproperty.put("path", line);
			
			line = bufferedReader.readLine();
			fileproperty.put("filename", line);
			
		} catch (FileNotFoundException e) {
			logger.error("Unable to locate the file....!!!");
		} catch (IOException e) {
			logger.error("Error reading the file:" + e.getMessage());
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				logger.error("Unable to close file:" + file.getName());
			}
		}
		return fileproperty;
	}

}
