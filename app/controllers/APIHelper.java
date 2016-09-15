package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import interactors.ClientRule;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

public class APIHelper extends Controller {
	public static Result email(String sender, String senderEmail, ArrayList<String> recipients, String subject, String bodyText, String bodyHTML) {
		Email email = new Email();
		email.setFrom(sender + " <" + senderEmail + ">");
		
		for(int i = 0; i < recipients.size(); i++) {
			email.addTo(recipients.get(i));
		}

		email.setSubject(subject);
		email.setBodyText(bodyText);
		email.setBodyHtml(bodyHTML);
		MailerPlugin.send(email);
		
		return ok();
	}
	
	public static Result getJSONByEncodedURL(String encodedURL) throws UnsupportedEncodingException {
		String url = URLDecoder.decode(encodedURL, "UTF-8"); //TODO: replace hard-coded character encoding
		ClientRule clientRule = new ClientRule(url);
		
		return Results.ok(clientRule.get(url).asJson());
	}
}
