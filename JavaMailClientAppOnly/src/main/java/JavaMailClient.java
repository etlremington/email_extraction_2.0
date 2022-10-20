import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.InputMismatchException;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate; 
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.MessageCollectionPage;

public class JavaMailClient {
	
	//Declare the properties file
	final static Properties oAuthProperties = new Properties();
	//final static Logger logger = Logger.getLogger(JavaMailClient.class.getName());
	final static Logger logger = LoggerFactory.getLogger(JavaMailClient.class);
	
	//main function
	public static void main(String[] args) {
   
	    try {
	        //Fetches the properties file from the resources folder
	    	oAuthProperties.load(JavaMailClient.class.getResourceAsStream("oAuth.properties"));
	    } catch (IOException e) {
	        logger.info("Unable to read OAuth configuration. Make sure you have a properly formatted oAuth.properties file. See README for details.");
	        //e.printStackTrace();
	        return;
	    }
	    
	    //Method to initialize the graph
	    initializeGraph(oAuthProperties);
	    
	    //Fetches the current user and displays the email and username 
	    greetUser(oAuthProperties);

	    Scanner input = new Scanner(System.in);
	    int choice = -1;

	    while (choice != 0) {
	        System.out.println("Please choose one of the following options:");
	        System.out.println("0. Exit");
	        System.out.println("1. List my inbox");
	        System.out.println("2. Get Attachments");

	        try {
	            choice = input.nextInt();
	        } catch (InputMismatchException ex) {
	            // Skip over non-integer input
	        }

	        input.nextLine();

	        // Process user choice
	        switch(choice) {
	            case 0:
	                // Exit the program
	                System.out.println("Goodbye...");
	                break;
	            case 1:
	            	// List emails from user's inbox
	                listInbox(oAuthProperties);
	                break;
	           case 2:
	                // List emails from user's inbox
	        	    getAttachments(oAuthProperties);
	            	break;
	            default:
	                System.out.println("Invalid choice");
	        }
	    }

	    input.close();
	}
	
	/* Method : initializeGraph(Properties properties) 
	 * Return Type : void
	 * Purpose : The purpose of this method is to initialize the graph.
	 */
	private static void initializeGraph(Properties properties) {
	    try {
	        Graph.ensureGraphForAppOnlyAuth(properties);
	    } catch (Exception e)
	    {
	    	logger.info("Error initializing Graph for user auth");
	        //e.printStackTrace();
	    }
	}
		
	/* Method : listInbox() 
	 * Return Type : void
	 * Purpose : The purpose of this method is list out all the messages for the current page.
	 */
	private static void listInbox(Properties properties) {
	    try {
	        
	    	final MessageCollectionPage messages = Graph.getInbox(properties);
	        LocalDate currentDate = LocalDate.now();
	        
	        //The below statements gets the previous date in the format "dd/MM/yy"
	        LocalDate previousDate = currentDate.minusDays(1);
	        DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("dd/MM/yy");
	        
	        // Output each message's details
	        for (Message message: messages.getCurrentPage()) {
	        	if(message.subject.contains("Rate360") && message.receivedDateTime.format(dateformatter).equals(dateformatter.format(previousDate))) {
	            System.out.println("  Message: " + message.subject);
	            System.out.println("  From: " + message.from.emailAddress.name);
	            System.out.println("  Status: " + (message.isRead ? "Read" : "Unread"));
	            System.out.println("  Received: " + message.receivedDateTime
	                // Values are returned in UTC, convert to local time zone
	                .atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
	                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
	        	}
	        }

	        final Boolean moreMessagesAvailable = messages.getNextPage() != null;
	        System.out.println("\nMore messages available? " + moreMessagesAvailable);
	    } catch (Exception e) {
	    	logger.info("Error getting inbox");
	    }
	}
	
	
	/* Method : getAttachments() 
	 * Return Type : void
	 * Purpose : The purpose of this method is get all the attachments from the current page.
	 */
	private static void getAttachments(Properties properties) {
		
		try {
			
	        final MessageCollectionPage messages = Graph.getInbox(properties);
	        LocalDate currentDate = LocalDate.now();
	        
	        //The below statements gets the previous date in the format "dd/MM/yy"
	        LocalDate previousDate = currentDate.minusDays(1);
	        DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("dd/MM/yy");
	        
	        for (Message message: messages.getCurrentPage()) {
	        	if(message.subject.contains("Rate360") && message.receivedDateTime.format(dateformatter).equals(dateformatter.format(previousDate))) {
	            System.out.println("Message: " + message.subject);
	            System.out.println("Message has attachment ?: " + message.hasAttachments); 
	            
	            if(message.hasAttachments) {
	            	AttachmentCollectionPage attachmentPage = Graph.getAttachments(properties, message.id);
	            	for(Attachment attachment : attachmentPage.getCurrentPage()) {
	            		if(attachment instanceof FileAttachment) {
	            			//Specifies the target directory where the downloaded attachments needs to be stored.
	            			File fileDown = new File("D:\\IHG\\ABC\\" + attachment.name);
	            			Files.write(fileDown.toPath(), ((FileAttachment) attachment).contentBytes);
	            		}
	            	}
	            }
	        }
	    }
	        
	    } catch (Exception e) {
	    	logger.info("Error getting attachments");
	    	logger.error("Error getting user");
	    }
	}

	
	/* Method : greetUser() 
	 * Return Type : void
	 * Purpose : The purpose of this method is greet the user before displaying all the options.
	 */
	private static void greetUser(Properties properties) {
	    try {
	        final User user = Graph.getUser(properties);
	        // For Work/school accounts, email is in mail property
	        // Personal accounts, email is in userPrincipalName
	        final String email = user.mail == null ? user.userPrincipalName : user.mail;
	        System.out.println("Hello, " + user.displayName + "!");
	        System.out.println("Email: " + email);
	    } catch (Exception e) {
	    	logger.info("Error getting user");
	    	logger.error("Error getting user");
	        //e.printStackTrace();
	    }
	}
	
}

