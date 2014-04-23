/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
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
package com.codenvy.organization.invite;

import org.codenvy.mail.MailSenderClient;
import org.everrest.core.impl.RuntimeDelegateImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.ext.RuntimeDelegate;
import java.util.regex.Matcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** Test for invite Service */
@Listeners(value = {MockitoTestNGListener.class})
public class InviteServiceTest {

    @Mock
    MailSenderClient mailSenderClient;
    @InjectMocks
    InviteService    inviteService;

    @BeforeClass
    public void init() {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    }

    @Test
    public void shouldCorrectlyDetectsEmailWithNames() {
        Matcher matcher = InviteService.EMAIL_PATTERN.matcher("Sergey <blah@gmail.com>");
        assertTrue(matcher.matches());
        assertEquals(matcher.group(1), "blah@gmail.com");


    }

    @Test
    public void shouldCorrectlyDetectsEmail() {
        Matcher matcher = InviteService.EMAIL_PATTERN.matcher("blah@gmail.com");
        assertTrue(matcher.matches());
        assertEquals(matcher.group(1), "blah@gmail.com");


    }
}