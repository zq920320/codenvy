/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.mail;

import com.codenvy.mail.shared.dto.AttachmentDto;
import com.codenvy.mail.shared.dto.EmailBeanDto;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Provide service of email sending. */
@Path("/mail")
public class MailSender {
    private static final Logger LOG = LoggerFactory.getLogger(MailSender.class);

    private SessionHolder sessionHolder;

    @Inject
    public MailSender(SessionHolder sessionHolder) {
        this.sessionHolder = sessionHolder;
    }

    /**
     * Send mail message.
     * If you need to send more than one copy of email, then write needed
     * receivers to EmailBean using setTo() method.
     *
     * @param emailBean
     *         - bean that contains all message parameters
     * @return - the Response with corresponded status (200)
     */
    @POST
    @Path("send")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendMail(EmailBeanDto emailBean) {
        List<File> tempFiles = new ArrayList<>();
        try {
            MimeMessage message = new MimeMessage(sessionHolder.getMailSession());
            Multipart contentPart = new MimeMultipart();

            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(emailBean.getBody(), "UTF-8", getSubType(emailBean.getMimeType()));
            contentPart.addBodyPart(bodyPart);

            if (emailBean.getAttachments() != null) {
                for (AttachmentDto attachment : emailBean.getAttachments()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();

                    byte[] content = Base64.getDecoder().decode(attachment.getContent());

                    String tempDir = System.getProperty("java.io.tmpdir");
                    File tempFile = new File(tempDir, attachment.getFileName());
                    tempFiles.add(tempFile);
                    Files.write(content, tempFile);

                    attachmentPart.attachFile(tempFile);
                    attachmentPart.setContentID("<" + attachment.getContentId() + ">");
                    contentPart.addBodyPart(attachmentPart);
                }
            }

            message.setContent(contentPart);
            message.setSubject(emailBean.getSubject(), "UTF-8");
            message.setFrom(new InternetAddress(emailBean.getFrom(), true));
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(emailBean.getTo()));

            if (emailBean.getReplyTo() != null) {
                message.setReplyTo(InternetAddress.parse(emailBean.getReplyTo()));
            }
            LOG.info("Sending from {} to {} with subject {}", emailBean.getFrom(), emailBean.getTo(), emailBean.getSubject());

            Transport.send(message);
            LOG.debug("Mail send");
        } catch (MessagingException | IOException e) {
            LOG.error(e.getLocalizedMessage());
            throw new WebApplicationException(e);
        } finally {
            tempFiles.forEach(File::delete);
        }

        return Response.ok().build();
    }

    /**
     * Get the specified MIME subtype from given primary MIME type.
     * <p/>
     * It is needed for setText method in MimeBodyPar because it works only with text MimeTypes.
     * setText method in MimeBodyPar already adds predefined "text/" to given subtype.
     *
     * @param mimeType
     *         primary MIME type
     * @return MIME subtype
     */
    private String getSubType(String mimeType) {
        return mimeType.substring(mimeType.lastIndexOf("/") + 1);
    }
}
