package com.cs4274.news_butler.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

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
	
	/*
	 * Method to pull sent items from Gmail
	 */
	public void readSentItems(String user, String token, String filePath, FBSQLiteHelper datasource, long previousLearned) throws Exception{
		this.filepath = filePath;
		Message[] messageReverse = null;
		int emailNumber = 0;
		
		// This is using a patched JavaMail for Android			
		IMAPStore store = OAuth2Authenticator.connectToImap(
					"imap.gmail.com", 
					993, 
					user, 
					token, 
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
        Message[] msgs = folder.getMessages(start,end); 
        messageReverse = reverseMessageOrder(msgs);	            
          
       
        for (int i=0;i<messageReverse.length;i++) {          	              	       	

        	if (previousLearned == -1L) { // never learn gmail before
         		String body = getBody(messageReverse[i]).replaceAll("[0-9]",""); 
         		long emailDate = getMessageDate(messageReverse[i].getSentDate()); 
         		datasource.addMessage(body, emailDate);
         	}
         	else {
         		long emailDate = getMessageDate(messageReverse[i].getSentDate());         		
         		if ( emailDate > previousLearned ) {// only learn emails that has not haven learnt before
         			String body = getBody(messageReverse[i]).replaceAll("[0-9]","");
         			datasource.addMessage(body, emailDate);
         		}
         	}       	   	
        }
	}
		
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
	
	private long getMessageDate(Date messageDate) {	
		return messageDate.getTime() / 1000L;
	}
	

}