/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.codenvy.mail;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

import org.everrest.assured.EverrestJetty;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import javax.mail.Session;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class MailSenderTest {
    private Session mailSession;

    private MailSender       mailSender;
    private MailSenderClient mailSenderClient;


    private SimpleSmtpServer server;

    @BeforeMethod
    public void setup(ITestContext context) throws IOException {
        //      mailSender = new MailSender("/mail-configuration.properties");
        server = SimpleSmtpServer.start(9000);
        mailSender = new MailSender(new SessionHolder("/mail-configuration.properties"));
        mailSenderClient = new MailSenderClient("http://localhost:" + context.getAttribute(EverrestJetty.JETTY_PORT) + "/rest/");
    }


    @AfterMethod
    public void stop(){
        server.stop();
    };

    @Test
    public void shouldBeAbleToSendMessage() throws IOException, MessagingException {
        mailSenderClient.sendMail("noreply@cloud-ide.com",
                                  "dev-test@cloud-ide.com",
                                  "dev-test@cloud-ide.com",
                                  "Subject",
                                  "text/html",
                                  "hello user");
        assertMail(server, "noreply@cloud-ide.com",
                   "dev-test@cloud-ide.com",
                   "dev-test@cloud-ide.com",
                   "Subject",
                   "text/html",
                   "hello user");


    }


    @Test
    public void shouldBeAbleToSendMessageWithReplacedVars() throws IOException, MessagingException {
        mailSenderClient.sendMail("noreply@cloud-ide.com",
                                  "dev-test@cloud-ide.com",
                                  "dev-test@cloud-ide.com",
                                  "Subject",
                                  "text/html",
                                  "hello ${username} user", Collections.singletonMap("username", "Dead Moroz"));
        assertMail(server, "noreply@cloud-ide.com",
                   "dev-test@cloud-ide.com",
                   "dev-test@cloud-ide.com",
                   "Subject",
                   "text/html",
                   "hello Dead Moroz user");


    }

    @Test
    public void shouldBeAbleToSendMessageWithFormattedFields() throws Exception {
        mailSenderClient.sendMail("Exo IDE <noreply@cloud-ide.com>",
                                  "dev-test@cloud-ide.com",
                                  "Developers to reply <dev-test@cloud-ide.com>",
                                  "Subject",
                                  "text/html",
                                  "hello user");
        assertMail(server,
                   "Exo IDE <noreply@cloud-ide.com>",
                   "dev-test@cloud-ide.com",
                   "Developers to reply <dev-test@cloud-ide.com>",
                   "Subject",
                   "text/html",
                   "hello user");
    }

    @Test
    public void shouldBeAbleToSendMessageToFewEmails() throws Exception {

        mailSenderClient.sendMail("noreply@cloud-ide.com",
                                  "dev-test@cloud-ide.com, dev-test1@cloud-ide.com, dev-test2@cloud-ide.com",
                                  "dev-test@cloud-ide.com",
                                  "Subject",
                                  "text/html",
                                  "hello user");
        assertMail(server, "noreply@cloud-ide.com",
                   "dev-test@cloud-ide.com, dev-test1@cloud-ide.com, dev-test2@cloud-ide.com",
                   "dev-test@cloud-ide.com",
                   "Subject",
                   "text/html",
                   "hello user");


    }


    public static void assertMail(SimpleSmtpServer server, String from, String to, String replyTo, String subject, String mimeType,
                                  String body) {
        assertEquals(server.getReceivedEmailSize(), 1);
        Iterator emailIter = server.getReceivedEmail();
        SmtpMessage email = (SmtpMessage)emailIter.next();

        assertEquals(email.getHeaderValue("Subject"), subject);
        assertEquals(email.getHeaderValue("From"), from);
        assertEquals(email.getHeaderValue("Reply-To"), replyTo);
        assertEquals(email.getHeaderValue("To"), to);
        assertTrue(email.getHeaderValue("Content-Type").contains(mimeType));
        assertEquals(email.getBody(), body);
    }

}
