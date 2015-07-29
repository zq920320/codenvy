/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
import com.google.common.io.Files;
import com.google.common.io.Resources;

import org.everrest.assured.EverrestJetty;
import org.everrest.assured.util.AvailablePortFinder;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import javax.mail.Session;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class MailSenderTest {
    private final Random portRandomizer = new Random();
    private Session          mailSession;
    private MailSender       mailSender;
    private MailSenderClient mailSenderClient;
    private File             testConfig;

    private SimpleSmtpServer server;

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

    @BeforeMethod
    public void setup(ITestContext context) throws IOException {
        //      mailSender = new MailSender("/mail-configuration.properties");
        int nextAvailable = AvailablePortFinder.getNextAvailable(10000 + portRandomizer.nextInt(2000));
        String testConfigContent = Resources.toString(Resources.getResource("mail-configuration.properties"), Charset.defaultCharset())
                 .replace("mail.smtp.port=9000", "mail.smtp.port=" + nextAvailable);
        testConfig = File.createTempFile("mail-config","properties");
        testConfig.deleteOnExit();
        Files.append(testConfigContent, testConfig, Charset.defaultCharset());


        server = SimpleSmtpServer.start(nextAvailable);
        mailSender = new MailSender(new SessionHolder(testConfig.getAbsolutePath()));
        mailSenderClient = new MailSenderClient("http://localhost:" + context.getAttribute(EverrestJetty.JETTY_PORT) + "/rest/");
    }

    @AfterMethod
    public void stop() {
        server.stop();
        testConfig.delete();
    }

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

}
