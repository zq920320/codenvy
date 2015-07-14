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
package com.codenvy.ide.permissions.client.indicator;


import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import com.codenvy.ide.permissions.client.PermissionsLocalizationConstant;
import com.codenvy.ide.permissions.client.part.PermissionsPartPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.List;

import static com.codenvy.ide.permissions.client.indicator.PermissionsIndicatorView.ActionDelegate;
import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link com.codenvy.ide.permissions.client.indicator.PermissionsIndicatorAction} tests.
 *
 * @author Kevin Pollet
 */
@RunWith(GwtMockitoTestRunner.class)
public class PermissionsIndicatorActionTest {
    private static final String CONTAINS_IGNORE_CASE_METHOD_NAME          = "containsIgnoreCase";
    private static final String IS_CURRENT_PROJECT_READ_ONLY_METHOD_NAME  = "isCurrentProjectReadOnly";
    private static final String GET_CURRENT_PROJECT_PERMISSIONS_AS_STRING = "getCurrentProjectPermissionsAsString";

    private PermissionsIndicatorAction permissionsIndicatorAction;

    private PermissionsLocalizationConstant locale;

    @Mock
    private AppContext context;

    @Mock
    private CurrentProject currentProject;

    @Mock
    private ProjectDescriptor currentProjectDescriptor;

    @Mock
    private PermissionsIndicatorViewImpl view;

    @Mock
    private PermissionsPartPresenter permissionsPresenter;

    @Mock
    private ActionEvent actionEvent;

    @Before
    public void beforeTest() {
        locale = GWT.create(PermissionsLocalizationConstant.class);
        permissionsIndicatorAction = new PermissionsIndicatorAction(context, view, locale, permissionsPresenter);

        when(context.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectDescription()).thenReturn(currentProjectDescriptor);
        when(actionEvent.getPresentation()).thenReturn(new Presentation());
    }

    @Test
    public void testUpdateWithNoProject() {
        when(context.getCurrentProject()).thenReturn(null);

        permissionsIndicatorAction.update(actionEvent);

        verify(view).setDelegate(any(ActionDelegate.class));
        verifyNoMoreInteractions(view);
        verify(actionEvent).getPresentation();
    }

    @Test
    public void testUpdateWithReadOnlyProject() {
        when(currentProjectDescriptor.getPermissions()).thenReturn(asList("read"));

        permissionsIndicatorAction.update(actionEvent);

        verify(view).setPermissions("R----");
        verify(view).setReadOnly(true);
        verify(actionEvent).getPresentation();
    }

    @Test
    public void testUpdateWithNonReadOnlyProject() {
        when(currentProjectDescriptor.getPermissions()).thenReturn(asList("read", "write"));

        permissionsIndicatorAction.update(actionEvent);

        verify(view).setPermissions("RW---");
        verify(view).setReadOnly(false);
        verify(actionEvent).getPresentation();
    }

    @Test
    public void testOnMouseOverWithReadOnlyProject() {
        when(currentProjectDescriptor.getPermissions()).thenReturn(asList("read"));

        permissionsIndicatorAction.onMouseOver();

        verify(view).setTooltipTitle(anyString());
        verify(view).setTooltipMessage(locale.permissionsIndicatorTooltipMessageReadOnly());
        verify(view).showTooltip();
    }

    @Test
    public void testOnMouseOverWithNonReadOnlyProject() {
        when(currentProjectDescriptor.getPermissions()).thenReturn(asList("write"));

        permissionsIndicatorAction.onMouseOver();

        verify(view).setTooltipTitle(anyString());
        verify(view).setTooltipMessage(locale.permissionsIndicatorTooltipMessage());
        verify(view).showTooltip();
    }

    @Test
    public void testOnMouseOut() {
        permissionsIndicatorAction.onMouseOut();

        verify(view).hideTooltip();
    }

    @Test
    public void testOnClick() {
        permissionsIndicatorAction.onClick();

        verify(permissionsPresenter).showDialog();
    }

    @Test
    public void testGetCurrentProjectPermissionsAsStringWithRead() throws Exception {
        when(currentProjectDescriptor.getPermissions()).thenReturn(asList("read"));

        final Method method = PermissionsIndicatorAction.class.getDeclaredMethod(GET_CURRENT_PROJECT_PERMISSIONS_AS_STRING);
        method.setAccessible(true);

        Assert.assertEquals("R----", method.invoke(permissionsIndicatorAction));
    }

    @Test
    public void testGetCurrentProjectPermissionsAsStringWithFullRights() throws Exception {
        when(currentProjectDescriptor.getPermissions()).thenReturn(asList("read", "write", "run", "build", "update_acl"));

        final Method method = PermissionsIndicatorAction.class.getDeclaredMethod(GET_CURRENT_PROJECT_PERMISSIONS_AS_STRING);
        method.setAccessible(true);

        Assert.assertEquals("RWBXU", method.invoke(permissionsIndicatorAction));
    }

    @Test
    public void testIsCurrentProjectReadOnlyWithReadOnlyProject() throws Exception {
        when(currentProjectDescriptor.getPermissions()).thenReturn(asList("read"));

        final Method method = PermissionsIndicatorAction.class.getDeclaredMethod(IS_CURRENT_PROJECT_READ_ONLY_METHOD_NAME);
        method.setAccessible(true);

        Assert.assertTrue((Boolean)method.invoke(permissionsIndicatorAction));
    }

    @Test
    public void testIsCurrentProjectReadOnlyWithNonReadOnlyProject() throws Exception {
        when(currentProjectDescriptor.getPermissions()).thenReturn(asList("write"));

        final Method method = PermissionsIndicatorAction.class.getDeclaredMethod(IS_CURRENT_PROJECT_READ_ONLY_METHOD_NAME);
        method.setAccessible(true);

        Assert.assertFalse((Boolean)method.invoke(permissionsIndicatorAction));
    }

    @Test
    public void testContainsIgnoreCaseWithExistingValue() throws Exception {
        final List<String> list = asList("foo", "bar");
        final Method method =
                PermissionsIndicatorAction.class.getDeclaredMethod(CONTAINS_IGNORE_CASE_METHOD_NAME, List.class, String.class);
        method.setAccessible(true);

        Assert.assertTrue((Boolean)method.invoke(permissionsIndicatorAction, list, "FOO"));
    }

    @Test
    public void testContainsIgnoreCaseWithMissingValue() throws Exception {
        final List<String> list = asList("foo", "bar");
        final Method method =
                PermissionsIndicatorAction.class.getDeclaredMethod(CONTAINS_IGNORE_CASE_METHOD_NAME, List.class, String.class);
        method.setAccessible(true);

        Assert.assertFalse((Boolean)method.invoke(permissionsIndicatorAction, list, "BUZ"));
    }
}
