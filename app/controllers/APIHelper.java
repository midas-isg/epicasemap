package controllers;

import interactors.ClientRule;

import java.util.ArrayList;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import play.Logger;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

public class APIHelper extends Controller {
	public static Result email(String sender, String senderEmail, ArrayList<String> recipients, String subject, String bodyText, String bodyHTML) {
		String result;
		Email email = new Email();
		email.setFrom(sender + " <" + senderEmail + ">");
		
		for(int i = 0; i < recipients.size(); i++) {
			email.addTo(recipients.get(i));
		}
		
		// adds attachment
		//email.addAttachment("attachment.pdf", new File("/some/path/attachment.pdf"));
		// adds inline attachment from byte array
		//email.addAttachment("data.txt", "data".getBytes(), "text/plain", "Simple data", EmailAttachment.INLINE);
		// sends text, HTML or both...
		
		email.setSubject(subject);
		email.setBodyText(bodyText);
		email.setBodyHtml(bodyHTML);
		result = MailerPlugin.send(email);
		
		return ok();
	}
	
	public static Result getJSONByEncodedURL(String encodedURL) throws UnsupportedEncodingException {
		String url = URLDecoder.decode(encodedURL, "UTF-8"); //TODO: replace hard-coded character encoding
		ClientRule clientRule = new ClientRule(url);
		
		return Results.ok(clientRule.get(url).asJson());
	}
}
