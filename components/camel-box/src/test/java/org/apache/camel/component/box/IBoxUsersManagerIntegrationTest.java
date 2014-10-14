/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Camel Api Route test generated by camel-component-util-maven-plugin
 * Generated on: Tue Jun 24 22:42:08 PDT 2014
 */
package org.apache.camel.component.box;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.box.boxjavalibv2.dao.BoxEmailAlias;
import com.box.boxjavalibv2.dao.BoxFolder;
import com.box.boxjavalibv2.dao.BoxUser;
import com.box.boxjavalibv2.requests.requestobjects.BoxEmailAliasRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxSimpleUserRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxUserDeleteRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxUserRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxUserUpdateLoginRequestObject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.box.internal.BoxApiCollection;
import org.apache.camel.component.box.internal.IBoxUsersManagerApiMethod;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for com.box.boxjavalibv2.resourcemanagers.IBoxUsersManager APIs.
 */
public class IBoxUsersManagerIntegrationTest extends AbstractBoxTestSupport {

    private static final Logger LOG = LoggerFactory.getLogger(IBoxUsersManagerIntegrationTest.class);
    private static final String PATH_PREFIX = BoxApiCollection.getCollection().getApiName(IBoxUsersManagerApiMethod.class).getName();
    private static final String CAMEL_EMAIL_ALIAS = "camel.test@localhost.com";
    private static final String UPDATED_EMAIL_ALIAS = "Updated." + CAMEL_EMAIL_ALIAS;
    private static final String CAMEL_USER_NAME = "Camel User";
    private static final String CAMEL_JOB_TITLE = "Extreme Camel Rider";

    @Ignore("Causes error Bad Request from SDK")
    @Test
    public void testAddEmailAlias() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        // parameter type is String
        headers.put("CamelBox.userId", testUserId);
        // parameter type is com.box.boxjavalibv2.requests.requestobjects.BoxEmailAliasRequestObject
        final BoxEmailAliasRequestObject requestObject =
                BoxEmailAliasRequestObject.addEmailAliasRequestObject(CAMEL_EMAIL_ALIAS);
        headers.put("CamelBox.emailAliasRequest", requestObject);

        BoxEmailAlias result = requestBodyAndHeaders("direct://ADDEMAILALIAS", null, headers);

        assertNotNull("addEmailAlias result", result);
        LOG.debug("addEmailAlias: " + result);

        deleteEmailAlias();
    }

    public BoxUser createEnterpriseUser() throws Exception {
        // using com.box.boxjavalibv2.requests.requestobjects.BoxUserRequestObject message body for single parameter "userRequest"
        final BoxUserRequestObject enterpriseUserRequestObject =
                BoxUserRequestObject.createEnterpriseUserRequestObject(CAMEL_EMAIL_ALIAS, CAMEL_USER_NAME);
        BoxUser result = requestBody("direct://CREATEENTERPRISEUSER", enterpriseUserRequestObject);

        assertNotNull("createEnterpriseUser result", result);
        return result;
    }

    public void deleteEmailAlias() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        // parameter type is String
        headers.put("CamelBox.userId", testUserId);
        // parameter type is String
        headers.put("CamelBox.emailId", CAMEL_EMAIL_ALIAS);
        // parameter type is com.box.restclientv2.requestsbase.BoxDefaultRequestObject
//        headers.put("CamelBox.defaultRequest", null);

        requestBodyAndHeaders("direct://DELETEEMAILALIAS", null, headers);
    }

    public void deleteEnterpriseUser(String userId) throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        // parameter type is String
        headers.put("CamelBox.userId", userId);
        // parameter type is com.box.boxjavalibv2.requests.requestobjects.BoxUserDeleteRequestObject
        final BoxUserDeleteRequestObject requestObject =
                BoxUserDeleteRequestObject.deleteEnterpriseUserRequestObject(false, true);
        headers.put("CamelBox.userDeleteRequest", requestObject);

        requestBodyAndHeaders("direct://DELETEENTERPRISEUSER", null, headers);
        // pause for user to be deleted completely
        Thread.sleep(2000);
    }

    @Test
    public void testGetAllEnterpriseUser() throws Exception {
        final Map<String, Object> headers = new HashMap<String, Object>();
        // parameter type is com.box.restclientv2.requestsbase.BoxDefaultRequestObject
        headers.put("CamelBox.defaultRequest", null);
        // parameter type is String
        headers.put("CamelBox.filterTerm", null);

        List result = requestBodyAndHeaders("direct://GETALLENTERPRISEUSER", null, headers);

        assertNotNull("getAllEnterpriseUser result", result);
        LOG.debug("getAllEnterpriseUser: " + result);
    }

    @Test
    public void testGetCurrentUser() throws Exception {
        // using com.box.restclientv2.requestsbase.BoxDefaultRequestObject message body for single parameter "defaultRequest"
        BoxUser result = requestBody("direct://GETCURRENTUSER", null);

        assertNotNull("getCurrentUser result", result);
        LOG.debug("getCurrentUser: " + result);
    }

    @Test
    public void testGetEmailAliases() throws Exception {
        final BoxUser enterpriseUser = createEnterpriseUser();

        try {
            final Map<String, Object> headers = new HashMap<String, Object>();
            // parameter type is String
            headers.put("CamelBox.userId", enterpriseUser.getId());
            // parameter type is com.box.restclientv2.requestsbase.BoxDefaultRequestObject
//            headers.put("CamelBox.defaultRequest", null);

            List result = requestBodyAndHeaders("direct://GETEMAILALIASES", null, headers);

            assertNotNull("getEmailAliases result", result);
            LOG.debug("getEmailAliases: " + result);
        } finally {
            deleteEnterpriseUser(enterpriseUser.getId());
        }
    }

    @Ignore("Developer account errors out with 'This does not currently support moving content into non-root folders'")
    @Test
    public void testMoveFolderToAnotherUser() throws Exception {
        final BoxUser enterpriseUser = createEnterpriseUser();

        try {
            final String toUserId = enterpriseUser.getId();
            moveTestFolder(testUserId, toUserId);
            moveTestFolder(toUserId, testUserId);
        } finally {
            deleteEnterpriseUser(enterpriseUser.getId());
        }
    }

    private void moveTestFolder(String fromUserId, String toUserId) {
        final Map<String, Object> headers = new HashMap<String, Object>();
        // parameter type is String
        headers.put("CamelBox.userId", fromUserId);
        // parameter type is String
        headers.put("CamelBox.folderId", testFolderId);
        // parameter type is com.box.boxjavalibv2.requests.requestobjects.BoxSimpleUserRequestObject
        final BoxSimpleUserRequestObject requestObject =
                BoxSimpleUserRequestObject.moveFolderToAnotherUserRequestEntity(toUserId, false);
        headers.put("CamelBox.simpleUserRequest", requestObject);

        BoxFolder result = requestBodyAndHeaders("direct://MOVEFOLDERTOANOTHERUSER", null, headers);

        assertNotNull("moveFolderToAnotherUser result", result);
        LOG.debug("moveFolderToAnotherUser: " + result);
    }

    @Test
    public void testUpdateUserInformaiton() throws Exception {
        final BoxUser enterpriseUser = createEnterpriseUser();

        try {
            final Map<String, Object> headers = new HashMap<String, Object>();
            // parameter type is String
            headers.put("CamelBox.userId", enterpriseUser.getId());
            // parameter type is com.box.boxjavalibv2.requests.requestobjects.BoxUserRequestObject
            final BoxUserRequestObject requestObject =
                    BoxUserRequestObject.updateUserInfoRequestObject(false);
            requestObject.setJobTitle(CAMEL_JOB_TITLE);
            headers.put("CamelBox.userRequest", requestObject);

            BoxUser result = requestBodyAndHeaders("direct://UPDATEUSERINFORMAITON", null, headers);

            assertNotNull("updateUserInformaiton result", result);
            assertEquals("updateUserInformaiton job title", CAMEL_JOB_TITLE, result.getJobTitle());
            LOG.debug("updateUserInformaiton: " + result);
        } finally {
            deleteEnterpriseUser(enterpriseUser.getId());
        }
    }

    @Ignore("Requires multiple confirmed email aliases, do disabled by default")
    @Test
    public void testUpdateUserPrimaryLogin() throws Exception {
        final BoxUser enterpriseUser = createEnterpriseUser();

        try {
            final Map<String, Object> headers = new HashMap<String, Object>();
            // parameter type is String
            headers.put("CamelBox.userId", enterpriseUser.getId());
            // parameter type is com.box.boxjavalibv2.requests.requestobjects.BoxUserUpdateLoginRequestObject
            final BoxUserUpdateLoginRequestObject requestObject =
                    BoxUserUpdateLoginRequestObject.updateUserPrimaryLoginRequestObject(UPDATED_EMAIL_ALIAS);
            headers.put("CamelBox.userUpdateLoginRequest", requestObject);

            BoxUser result = requestBodyAndHeaders("direct://UPDATEUSERPRIMARYLOGIN", null, headers);

            assertNotNull("updateUserPrimaryLogin result", result);
            assertEquals("updateUserPrimaryLogin primary login", UPDATED_EMAIL_ALIAS, result.getLogin());
            LOG.debug("updateUserPrimaryLogin: " + result);
        } finally {
            deleteEnterpriseUser(enterpriseUser.getId());
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                // test route for addEmailAlias
                from("direct://ADDEMAILALIAS")
                        .to("box://" + PATH_PREFIX + "/addEmailAlias");

                // test route for createEnterpriseUser
                from("direct://CREATEENTERPRISEUSER")
                        .to("box://" + PATH_PREFIX + "/createEnterpriseUser?inBody=userRequest");

                // test route for deleteEmailAlias
                from("direct://DELETEEMAILALIAS")
                        .to("box://" + PATH_PREFIX + "/deleteEmailAlias");

                // test route for deleteEnterpriseUser
                from("direct://DELETEENTERPRISEUSER")
                        .to("box://" + PATH_PREFIX + "/deleteEnterpriseUser");

                // test route for getAllEnterpriseUser
                from("direct://GETALLENTERPRISEUSER")
                        .to("box://" + PATH_PREFIX + "/getAllEnterpriseUser");

                // test route for getCurrentUser
                from("direct://GETCURRENTUSER")
                        .to("box://" + PATH_PREFIX + "/getCurrentUser?inBody=defaultRequest");

                // test route for getEmailAliases
                from("direct://GETEMAILALIASES")
                        .to("box://" + PATH_PREFIX + "/getEmailAliases");

                // test route for moveFolderToAnotherUser
                from("direct://MOVEFOLDERTOANOTHERUSER")
                        .to("box://" + PATH_PREFIX + "/moveFolderToAnotherUser");

                // test route for updateUserInformaiton
                from("direct://UPDATEUSERINFORMAITON")
                        .to("box://" + PATH_PREFIX + "/updateUserInformaiton");

                // test route for updateUserPrimaryLogin
                from("direct://UPDATEUSERPRIMARYLOGIN")
                        .to("box://" + PATH_PREFIX + "/updateUserPrimaryLogin");

            }
        };
    }
}