package com.cs4274.news_butler.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;

import org.jsoup.Jsoup;
import android.util.Log;

import com.cs4274.news_butler.SettingsActivity;
import com.google.code.oauth2.OAuth2Authenticator;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;


public class ReadGMail {	
	
	private String GmailSourceFile = "Gmail.txt";
	private String filepath;
	private static int NO_OF_EMAILS_TO_LEARN = 50;
	//private String content = "";
	File myFile;
	
/*	
	public String readSentItems(String user, String token, String filePath) throws InterruptedException, ExecutionException{
		this.filepath = filePath;
		
		return new connectToImapTask().execute(user, token).get();
		
	}
*/	
	public List<String> readSentItems(String user, String token, String filePath) throws Exception{
		this.filepath = filePath;
		Message[] messageReverse = null;
		int emailNumber = 0;
		List<String> gmailMessages = new ArrayList<String>();
		
		// This is using a patched JavaMail for Android			
		IMAPStore store = OAuth2Authenticator.connectToImap(
					"imap.gmail.com", 
					993, 
					user, 
					token, 
					true);
		
		IMAPFolder folder = (IMAPFolder) store.getFolder("[Gmail]/Sent Mail"); 
        folder.open(Folder.READ_ONLY); 
        emailNumber = folder.getMessageCount();
        
        Message[] msgs = folder.getMessages(1,emailNumber); 
        messageReverse = reverseMessageOrder(msgs);	            
        
       
        for (int i=0;i<messageReverse.length;i++) {  
         	String body = getBody(messageReverse[i]).replaceAll("[0-9]","");        	
         	gmailMessages.add(body);
        }
       
		//return new connectToImapTask().execute(user, token).get();
		return gmailMessages;
	}
	
/*	
	private class connectToImapTask extends AsyncTask <String,Void,String> {

		@Override
		protected String doInBackground(String... credentials) {			
			Message[] messageReverse = null;
			int emailNumber = 0;
			try {
				
				// This is using a patched JavaMail for Android			
				IMAPStore store = OAuth2Authenticator.connectToImap(
							"imap.gmail.com", 
							993, 
							credentials[0], 
							credentials[1], 
							true);
						
		        IMAPFolder folder = (IMAPFolder) store.getFolder("[Gmail]/Sent Mail"); 
	            folder.open(Folder.READ_ONLY); 
	            int end = folder.getMessageCount();
	            
	       
	            if (end > NO_OF_EMAILS_TO_LEARN) {
	            	emailNumber = NO_OF_EMAILS_TO_LEARN;
	            }
	            else {
	            	emailNumber = end;
	            }
	            
					int start = end - emailNumber + 1;
		            Message[] msgs = folder.getMessages(start, end); 
		            messageReverse = reverseMessageOrder(msgs);	            
		            
		            content = "";
		            for (int i=0;i<messageReverse.length;i++) {  
		             	String body = getBody(messageReverse[i]).replaceAll("[0-9]","");
		             	content = content + body + " ";
		            }
		            
		            //exportGmail(messageReverse);	
	            
		    } catch (Exception e) {	 
	            e.printStackTrace();
		    } 
			
			return content;
			//return messageReverse;
		}

		@Override
		protected void onPostExecute(String result) {	
			//new indexSourcesTask().execute();
			//content = result;
		}			
	}
*/	
	/*
	 * Reverse the messages so that the latest one appear first	
	 */
	private Message[] reverseMessageOrder(Message[] messages) {
        Message revMessages[]= new Message[messages.length];
        int i=messages.length-1;
        for (int j=0;j<messages.length;j++,i--) {
             revMessages[j] = messages[i];

        }
        return revMessages;

   }
	
	/*
	 * Method to extract the body of the email
	 */
	private String getBody(Message message) throws IOException, MessagingException {
		String result = "";
		if(message instanceof MimeMessage)
        {
            MimeMessage m = (MimeMessage)message;
            Object contentObject = m.getContent();
            if(contentObject instanceof Multipart)
            {
                BodyPart clearTextPart = null;
                BodyPart htmlTextPart = null;
                Multipart content = (Multipart)contentObject;
                int count = content.getCount();
                for(int i=0; i<count; i++)
                {
                    BodyPart part =  content.getBodyPart(i);
                    if (part.getDisposition() != null) {
                    		return " ";
                    }
                    else if(part.isMimeType("text/plain"))
                    {
                        clearTextPart = part;
                        break;
                    }
                    else if(part.isMimeType("text/html"))
                    {
                        htmlTextPart = part;
                    }
                }

                if(clearTextPart!=null)
                {
                    result = (String) clearTextPart.getContent();
                }
                else if (htmlTextPart!=null)
                {
                    String html = (String) htmlTextPart.getContent();
                    result = Jsoup.parse(html).text();
                }

            }
             else if (contentObject instanceof String) // a simple text message
            {
                result = (String) contentObject;
            }
            else // not a mime message
            {
                System.out.println("Not a MIME message");
                result = " ";
            }
        }
		return result;
	}
	
	/*
	 * Method to export Gmail messages to txt file
	 */
	private void exportGmail(Message[] result) throws MessagingException {
		myFile = new File(filepath, GmailSourceFile);
		String nextLine = "\n";
		
		try {
             FileOutputStream fos = new FileOutputStream(myFile);
             for (int i=0;i<result.length;i++) {  
             	String body = getBody(result[i]).replaceAll("[0-9]","");
             	fos.write(body.getBytes());
             	fos.write(nextLine.getBytes());
             }
                       
             fos.close();         
         } catch (IOException e) {
             e.printStackTrace();
         }
        			 
		 	 
	}
	/*
	 * AsyncTask to index the sources
	 */
	
	/*
	public class indexSourcesTask extends AsyncTask <Void,Void,Void> {
			
			@Override
			protected Void doInBackground(Void... params) {	
				try {
					IndexSources.createIndex(filepath,SettingsActivity.USER);				
				} catch (Exception e) {				
					e.printStackTrace();
				}
					
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				try {
					IndexSources.computeTopTermQuery();					
				} catch (Exception e) {				
					e.printStackTrace();
				}
			}
				
		}
		*/
	
}