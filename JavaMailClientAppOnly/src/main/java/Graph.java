import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.User;
import com.microsoft.graph.models.UserSendMailParameterSet;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.MessageCollectionPage;
import com.microsoft.graph.requests.UserCollectionPage;

import okhttp3.Request;

public class Graph {
	
	private static Properties _properties;
	private static ClientSecretCredential _clientSecretCredential;
	public static GraphServiceClient<Request> _appClient;
	
	/* Method : getInbox() 
	 * Return Type : MessageCollectionPage
	 * Purpose : The purpose of this method is to get the messages from the inbox
	 * in the mail.
	 */
	public static MessageCollectionPage getInbox(Properties properties) throws Exception {
	    // Ensure client isn't null
	    if (_appClient == null) {
	        throw new Exception("Graph has not been initialized for user auth");
	    }

	    return _appClient.users(Graph.getUserId(properties))
	        .mailFolders("inbox")
	        .messages()
	        .buildRequest()
	        .select("from,isRead,receivedDateTime,subject,hasAttachments,attachments")
	        .top(2000000)
	        .orderBy("receivedDateTime DESC")
	        .get();
	}
	
	/* Method : getAttachments(String id) 
	 * Return Type : AttachementCollectionPage
	 * Purpose : The purpose of this method is to get the attachments from the messages 
	 * in the mail.
	 */
	public static AttachmentCollectionPage getAttachments(Properties properties, String id) throws Exception{
		//Ensure client is not null
		if(_appClient == null) {
			throw new Exception("Graph has not been initialized for user auth");
		}
		
		return _appClient.users(Graph.getUserId(properties))
			   .mailFolders("inbox")
			   .messages(id)
			   .attachments()
			   .buildRequest()
			   .get();
	}
	
	/* Method : ensureGraphForAppOnlyAuth() 
	 * Return Type : void
	 * Purpose : The purpose of this method is to ensure that the graph is authenticated 
	 * for app only.
	 */
	public static void ensureGraphForAppOnlyAuth(Properties properties) throws Exception {
	    // Ensure _properties isn't null
		
		_properties = properties;
		List<String> list = new ArrayList<String>();
		list.add("https://graph.microsoft.com/.default");
		
	    if (_properties == null) {
	        throw new Exception("Properties cannot be null");
	    }

	    if (_clientSecretCredential == null) {
	        final String clientId = _properties.getProperty("app.clientId");
	        final String tenantId = _properties.getProperty("app.tenantId");
	        final String clientSecret = _properties.getProperty("app.clientSecret");

	        _clientSecretCredential = new ClientSecretCredentialBuilder()
	            .clientId(clientId)
	            .tenantId(tenantId)
	            .clientSecret(clientSecret)
	            .build();
	       
	    }

	    if (_appClient == null) {
	        final TokenCredentialAuthProvider authProvider =
	            new TokenCredentialAuthProvider(Collections.unmodifiableList(list), _clientSecretCredential);
	        		
	        _appClient = GraphServiceClient.builder()
	            .authenticationProvider(authProvider)
	            .buildClient();
	    
	    }
	}
	
	/* Method : getUserId() 
	 * Return Type : String
	 * Purpose : The purpose of this method is return the userId present in the file.
	 */
	public static String getUserId(Properties properties) {
		_properties = properties;
		final String userId = _properties.getProperty("userId");
		return userId;
	}
	
	
	/* Method : getUser() 
	 * Return Type : User
	 * Purpose : The purpose of this method is to get the current user information.
	 */
	public static User getUser(Properties properties) throws Exception {
	    // Ensure client isn't null
	    if (_appClient == null) {
	        throw new Exception("Graph has not been initialized for user auth");
	    }
	    
	    User user = _appClient.users(Graph.getUserId(properties))
	    			.buildRequest()
	    			.get();
	    
	    return user;
	}
	
}
