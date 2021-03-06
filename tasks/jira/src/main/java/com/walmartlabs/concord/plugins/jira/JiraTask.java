package com.walmartlabs.concord.plugins.jira;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Walmart Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.squareup.okhttp.Credentials;
import com.walmartlabs.concord.common.ConfigurationUtils;
import com.walmartlabs.concord.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.*;

/**
 * Created by ppendha on 6/18/18.
 */
@Named("jira")
public class JiraTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(JiraTask.class);

    // IN params
    private static final String ACTION_KEY = "action";
    private static final String JIRA_ASSIGNEE_KEY = "assignee";
    private static final String JIRA_AUTH_KEY = "auth";
    private static final String JIRA_COMMENT_KEY = "comment";
    private static final String JIRA_COMPONENTID = "componentId";
    private static final String JIRA_COMPONENTNAME = "componentName";
    private static final String JIRA_CUSTOM_FIELDS_ATTR_KEY = "customFieldsTypeFieldAttr";
    private static final String JIRA_CUSTOM_FIELDS_KV_KEY = "customFieldsTypeKv";
    private static final String JIRA_DESCRIPTION_KEY = "description";
    private static final String JIRA_FILE_PATH_KEY = "filePath";
    private static final String JIRA_ISSUE_COMPONENTS_KEY = "components";
    private static final String JIRA_ISSUE_ID_KEY = "issueId";
    private static final String JIRA_COMPONENT_ID_KEY = "componentId";
    private static final String JIRA_ISSUE_KEY = "issueKey";
    private static final String JIRA_ISSUE_LABELS_KEY = "labels";
    private static final String JIRA_ISSUE_PRIORITY_KEY = "priority";
    private static final String JIRA_ISSUE_STATUS_KEY = "issueStatus";
    private static final String JIRA_ISSUE_STATUS_OPERATOR_KEY = "statusOperator";
    private static final String JIRA_ISSUE_TYPE_KEY = "issueType";
    private static final String JIRA_PARENT_ISSUE_KEY = "parentIssueKey";
    private static final String JIRA_PASSWORD_KEY = "password";
    private static final String JIRA_PROJECT_KEY = "projectKey";
    private static final String JIRA_REQUESTOR_UID_KEY = "requestorUid";
    private static final String JIRA_SUMMARY_KEY = "summary";
    private static final String JIRA_TRANSITION_COMMENT_KEY = "transitionComment";
    private static final String JIRA_TRANSITION_ID_KEY = "transitionId";
    private static final String JIRA_URL_KEY = "apiUrl";
    private static final String JIRA_USER_ID_KEY = "userId";
    private static final String JIRA_FIELDS_KEY = "fields";
    private static final String DEBUG_KEY = "debug";
    private static final int DEFAULT_START_AT = 0;
    private static final int DEFAULT_MAX_RESULTS = 50;


    private static final String[] ALL_IN_PARAMS = {
            ACTION_KEY,
            JIRA_ASSIGNEE_KEY,
            JIRA_AUTH_KEY,
            JIRA_COMMENT_KEY,
            JIRA_COMPONENTID,
            JIRA_COMPONENTNAME,
            JIRA_CUSTOM_FIELDS_ATTR_KEY,
            JIRA_CUSTOM_FIELDS_KV_KEY,
            JIRA_DESCRIPTION_KEY,
            JIRA_FILE_PATH_KEY,
            JIRA_ISSUE_COMPONENTS_KEY,
            JIRA_ISSUE_ID_KEY,
            JIRA_ISSUE_KEY,
            JIRA_ISSUE_LABELS_KEY,
            JIRA_ISSUE_PRIORITY_KEY,
            JIRA_ISSUE_STATUS_KEY,
            JIRA_ISSUE_STATUS_OPERATOR_KEY,
            JIRA_ISSUE_TYPE_KEY,
            JIRA_PARENT_ISSUE_KEY,
            JIRA_PASSWORD_KEY,
            JIRA_PROJECT_KEY,
            JIRA_REQUESTOR_UID_KEY,
            JIRA_SUMMARY_KEY,
            JIRA_TRANSITION_COMMENT_KEY,
            JIRA_TRANSITION_ID_KEY,
            JIRA_URL_KEY,
            JIRA_USER_ID_KEY,
            JIRA_FIELDS_KEY,
            DEBUG_KEY
    };

    private static final String SECRET_NAME_KEY = "name";
    private static final String ORG_KEY = "org";
    private static final String USERNAME_KEY = "username";
    private static final String BASIC_KEY = "basic";
    private static final String SECRET_KEY = "secret";

    private final SecretService secretService;

    @InjectVariable("jiraParams")
    private Map<String, Object> defaults;

    @Inject
    public JiraTask(SecretService secretService) {
        this.secretService = secretService;
    }

    @Override
    public void execute(Context ctx) {
        Map<String, Object> cfg = createCfg(ctx);
        Action action = getAction(cfg);

        String jiraUrl = formatUrl(MapUtils.assertString(cfg, JIRA_URL_KEY));
        log.info("Using JIRA URL: {}", jiraUrl);

        switch (action) {
            case CREATEISSUE: {
                log.info("Starting 'CreateIssue' Action");
                createIssue(ctx, cfg, jiraUrl);
                break;
            }
            case ADDCOMMENT: {
                log.info("Starting 'AddComment' Action");
                addComment(ctx, cfg, jiraUrl);
                break;
            }
            case ADDATTACHMENT: {
                log.info("Starting 'AddAttachment' Action");
                addAttachment(ctx, cfg, jiraUrl);
                break;
            }
            case CREATECOMPONENT: {
                log.info("Starting 'CreateComponent' Action");
                createComponent(ctx, cfg, jiraUrl);
                break;
            }
            case DELETECOMPONENT: {
                log.info("Starting 'DeleteComponent' Action");
                deleteComponent(ctx, cfg, jiraUrl);
                break;
            }
            case TRANSITION: {
                log.info("Starting 'Transition' Action");
                transition(ctx, cfg, jiraUrl);
                break;
            }
            case DELETEISSUE: {
                log.info("Starting 'DeleteIssue' Action");
                deleteIssue(ctx, cfg, jiraUrl);
                break;
            }
            case UPDATEISSUE: {
                log.info("Starting 'UpdateIssue' Action");
                updateIssue(ctx, cfg, jiraUrl);
                break;
            }
            case CREATESUBTASK: {
                log.info("Starting 'CreateSubTask' Action");
                createSubTask(ctx, cfg, jiraUrl);
                break;
            }
            case CURRENTSTATUS: {
                log.info("Starting 'CurrentStatus' Action");
                currentStatus(ctx, cfg, jiraUrl);
                break;
            }
            case GETISSUES: {
                log.info("Starting 'GetIssues' Action");
                getIssues(ctx, cfg, jiraUrl);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported action type: " + action);
        }
    }

    public String getStatus(@InjectVariable("context") Context ctx, String issueKey) {
        Map<String, Object> cfg = createCfg(ctx);
        String jiraUrl = formatUrl(MapUtils.assertString(cfg, JIRA_URL_KEY));
        return getStatus(ctx, cfg, jiraUrl, issueKey);
    }

    private Map<String, Object> createCfg(Context ctx) {
        Map<String, Object> m = new HashMap<>(defaults != null ? defaults : Collections.emptyMap());

        put(m, com.walmartlabs.concord.sdk.Constants.Context.WORK_DIR_KEY, ctx);
        for (String k : ALL_IN_PARAMS) {
            put(m, k, ctx);
        }

        return m;
    }

    private String createIssue(Context ctx, Map<String, Object> cfg, String url) {
        String projectKey = MapUtils.assertString(cfg, JIRA_PROJECT_KEY);
        String summary = MapUtils.assertString(cfg, JIRA_SUMMARY_KEY);
        String description = MapUtils.assertString(cfg, JIRA_DESCRIPTION_KEY);
        String requestorUid = MapUtils.getString(cfg, JIRA_REQUESTOR_UID_KEY);
        String issueType = MapUtils.assertString(cfg, JIRA_ISSUE_TYPE_KEY);
        String issuePriority = MapUtils.getString(cfg, JIRA_ISSUE_PRIORITY_KEY, null);
        Map<String, Object> assignee = MapUtils.getMap(cfg, JIRA_ASSIGNEE_KEY, null);
        List<String> labels = MapUtils.getList(cfg, JIRA_ISSUE_LABELS_KEY, null);
        List<String> components = MapUtils.getList(cfg, JIRA_ISSUE_COMPONENTS_KEY, null);
        Map<String, String> customFieldsTypeKv = MapUtils.getMap(cfg, JIRA_CUSTOM_FIELDS_KV_KEY, null);
        Map<String, Object> customFieldsTypeAtt = MapUtils.getMap(cfg, JIRA_CUSTOM_FIELDS_ATTR_KEY, null);

        String issueId;

        try {
            //Build JSON data
            Map<String, Object> objProj = Collections.singletonMap("key", projectKey);
            Map<String, Object> objPriority = Collections.singletonMap("name", issuePriority);
            Map<String, Object> objIssueType = Collections.singletonMap("name", issueType);

            Map<String, Object> objMain = new HashMap<>();
            objMain.put("project", objProj);
            objMain.put("summary", summary);
            objMain.put("description", description);

            if (requestorUid != null) {
                objMain.put("reporter", Collections.singletonMap("name", requestorUid));
            }

            objMain.put("priority", objPriority);
            objMain.put("issuetype", objIssueType);

            if (labels != null && !labels.isEmpty()) {
                objMain.put("labels", labels);
            }

            if (components != null && !components.isEmpty()) {
                objMain.put("components", components);
            }

            if (assignee != null && !assignee.isEmpty()) {
                objMain.put("assignee", assignee);
            }

            if (customFieldsTypeKv != null && !customFieldsTypeKv.isEmpty()) {
                for (Map.Entry<String, String> e : customFieldsTypeKv.entrySet()) {
                    objMain.put(e.getKey(), String.valueOf(e.getValue()));
                }
            }

            if (customFieldsTypeAtt != null && !customFieldsTypeAtt.isEmpty()) {
                for (Map.Entry<String, Object> e : customFieldsTypeAtt.entrySet()) {
                    objMain.put(e.getKey(), e.getValue());
                }
            }
            Map<String, Object> objFields = Collections.singletonMap("fields", objMain);

            log.info("Creating new issue in '{}'...", projectKey);

            Map<String, Object> results = new JiraClient(cfg)
                    .url(url + "issue/")
                    .jiraAuth(buildAuth(ctx, cfg))
                    .successCode(201)
                    .post(objFields);

            issueId = results.get("key").toString().replaceAll("\"", "");
            ctx.setVariable(JIRA_ISSUE_ID_KEY, issueId);
            log.info("Issue #{} created in Project# '{}'", issueId, projectKey);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while creating an issue: " + e.getMessage(), e);
        }

        return issueId;
    }

    private void createSubTask(Context ctx, Map<String, Object> cfg, String url) {
        String parentKey = MapUtils.assertString(cfg, JIRA_PARENT_ISSUE_KEY);

        Map<String, Object> customFieldsTypeAtt = new HashMap<>(MapUtils.getMap(cfg, JIRA_CUSTOM_FIELDS_ATTR_KEY, Collections.emptyMap()));
        customFieldsTypeAtt.put("parent", Collections.singletonMap("key", parentKey));

        Map<String, Object> newCfg = new HashMap<>(cfg);
        newCfg.put(JIRA_CUSTOM_FIELDS_ATTR_KEY, customFieldsTypeAtt);
        newCfg.put(JIRA_ISSUE_TYPE_KEY, "Sub-task");

        createIssue(ctx, newCfg, url);
    }

    private void createComponent(Context ctx, Map<String, Object> cfg, String url) {
        String projectKey = MapUtils.assertString(cfg, JIRA_PROJECT_KEY);
        String componentName = MapUtils.assertString(cfg, JIRA_COMPONENTNAME);

        try {
            Map<String, Object> m = new HashMap<>();
            m.put("name", componentName);
            m.put("project", projectKey);

            Map<String, Object> results = new JiraClient(cfg)
                    .url(url + "component/")
                    .jiraAuth(buildAuth(ctx, cfg))
                    .successCode(201)
                    .post(m);

            String componentId = results.get("id").toString();
            componentId = componentId.replaceAll("\"", "");
            ctx.setVariable(JIRA_COMPONENT_ID_KEY, componentId);
            log.info("Component '{}' created successfully and its Id is '{}'", componentName, componentId);
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while creating a component: " + e.getMessage(), e);
        }
    }

    private void deleteComponent(Context ctx, Map<String, Object> cfg, String url) {
        int componentId = MapUtils.assertInt(cfg, JIRA_COMPONENTID);

        try {
            new JiraClient(cfg)
                    .url(url + "component/" + componentId)
                    .jiraAuth(buildAuth(ctx, cfg))
                    .successCode(204)
                    .delete();

            log.info("Component# '{}' removed successfully.", componentId);
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while deleting a component: " + e.getMessage(), e);
        }
    }

    private void addAttachment(Context ctx, Map<String, Object> cfg, String url) {
        String issueKey = MapUtils.assertString(cfg, JIRA_ISSUE_KEY);
        String filePath = MapUtils.assertString(cfg, JIRA_FILE_PATH_KEY);

        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }

        try {
            new JiraClient(cfg)
                    .url(url + "issue/" + issueKey + "/attachments")
                    .successCode(200)
                    .jiraAuth(buildAuth(ctx, cfg))
                    .post(file);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while attaching a file: " + e.getMessage(), e);
        }
    }

    private void addComment(Context ctx, Map<String, Object> cfg, String url) {
        String issueKey = MapUtils.assertString(cfg, JIRA_ISSUE_KEY);
        String comment = MapUtils.assertString(cfg, JIRA_COMMENT_KEY);
        boolean debug = MapUtils.getBoolean(cfg, DEBUG_KEY, false);

        try {
            Map<String, Object> m = Collections.singletonMap("body", comment);

            new JiraClient(cfg)
                    .url(url + "issue/" + issueKey + "/comment")
                    .jiraAuth(buildAuth(ctx, cfg))
                    .successCode(201)
                    .post(m);

            if (debug) {
                log.info("Comment '{}' added to Issue #{}", comment, issueKey);
            } else {
                log.info("Comment added to Issue #{}", issueKey);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error occurred while adding a comment: " + e.getMessage(), e);
        }
    }

    private void transition(Context ctx, Map<String, Object> cfg, String url) {
        String issueKey = MapUtils.assertString(cfg, JIRA_ISSUE_KEY);
        String transitionId = Integer.toString(MapUtils.getInt(cfg, JIRA_TRANSITION_ID_KEY, -1));
        String transitionComment = MapUtils.assertString(cfg, JIRA_TRANSITION_COMMENT_KEY);
        Map<String, String> transitionFieldsTypeKv = MapUtils.getMap(cfg, JIRA_CUSTOM_FIELDS_KV_KEY, null);
        Map<String, String> transitionFieldsTypeAtt = MapUtils.getMap(cfg, JIRA_CUSTOM_FIELDS_ATTR_KEY, null);

        try {
            //Build JSON data
            Map<String, Object> objId = Collections.singletonMap("id", transitionId);
            Map<String, Object> objTransition = Collections.singletonMap("transition", objId);
            Map<String, Object> objBody = Collections.singletonMap("body", transitionComment);
            Map<String, Object> objAdd = Collections.singletonMap("add", objBody);
            ArrayList<Map<String, Object>> commentsArray = new ArrayList<>();
            commentsArray.add(objAdd);
            Map<String, Object> objComment = Collections.singletonMap("comment", commentsArray);
            Map<String, Object> objupdate = Collections.singletonMap("update", objComment);

            Map<String, Object> objMain = new HashMap<>();
            if (transitionFieldsTypeKv != null && !transitionFieldsTypeKv.isEmpty()) {
                for (Map.Entry<String, String> e : transitionFieldsTypeKv.entrySet()) {
                    objMain.put(e.getKey(), String.valueOf(e.getValue()));
                }
            }

            if (transitionFieldsTypeAtt != null && !transitionFieldsTypeAtt.isEmpty()) {
                for (Map.Entry<String, String> e : transitionFieldsTypeAtt.entrySet()) {
                    objMain.put(e.getKey(), e.getValue());
                }
            }

            Map<String, Object> objFields = Collections.singletonMap("fields", objMain);
            objupdate = ConfigurationUtils.deepMerge(objFields, ConfigurationUtils.deepMerge(objTransition, objupdate));

            new JiraClient(cfg)
                    .url(url + "issue/" + issueKey + "/transitions")
                    .jiraAuth(buildAuth(ctx, cfg))
                    .successCode(204)
                    .post(objupdate);

            log.info("Transition is successful on Issue #{} to transitionId #{}", issueKey, transitionId);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while doing a transition: " + e.getMessage(), e);
        }
    }

    private void deleteIssue(Context ctx, Map<String, Object> cfg, String url) {
        String issueKey = MapUtils.assertString(cfg, JIRA_ISSUE_KEY);

        try {
            new JiraClient(cfg)
                    .url(url + "issue/" + issueKey)
                    .jiraAuth(buildAuth(ctx, cfg))
                    .successCode(204)
                    .delete();

            log.info("Issue #{} deleted successfully.", issueKey);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while deleting an issue: " + e.getMessage(), e);
        }
    }

    private void updateIssue(Context ctx, Map<String, Object> cfg, String url) {
        String issueKey = MapUtils.assertString(cfg, JIRA_ISSUE_KEY);
        Map<String, Object> fields = MapUtils.assertMap(cfg, JIRA_FIELDS_KEY);

        log.info("Updating {} fields for issue #{}", fields, issueKey);

        try {
            new JiraClient(cfg)
                    .url(url + "issue/" + issueKey)
                    .jiraAuth(buildAuth(ctx, cfg))
                    .successCode(204)
                    .put(Collections.singletonMap("fields", fields));

            log.info("Issue #{} updated successfully.", issueKey);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while updating an issue: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void getIssues(Context ctx, Map<String, Object> cfg, String url) {
        String projectKey = MapUtils.assertString(cfg, JIRA_PROJECT_KEY);
        String issueType = MapUtils.assertString(cfg, JIRA_ISSUE_TYPE_KEY);
        String issueStatus = MapUtils.getString(cfg, JIRA_ISSUE_STATUS_KEY, null);
        String statusOperator = MapUtils.getString(cfg, JIRA_ISSUE_STATUS_OPERATOR_KEY, "=");


        try {
            String jqlQuery = configureStatus(projectKey, issueType, issueStatus, statusOperator);

            log.info("Fetching full list of issue IDs from project '{}' of type '{}' and with status '{} {}'...",
                    projectKey, issueType, statusOperator, issueStatus);

            List<String> issueList = new LinkedList<>();
            int startAt = DEFAULT_START_AT;
            int maxResults = DEFAULT_MAX_RESULTS;

            while (true) {
                Map<String, Object> objMain = new HashMap<>();

                objMain.put("jql", jqlQuery);
                objMain.put("startAt", startAt);
                objMain.put("maxResults", maxResults);

                List<String> fieldList = Collections.singletonList("key");
                objMain.put("fields", fieldList);

                Map<String, Object> results = new JiraClient(cfg)
                        .url(url + "search")
                        .jiraAuth(buildAuth(ctx, cfg))
                        .successCode(200)
                        .post(objMain);

                List<Map> issueMap = (List<Map>) results.get("issues");

                for (Map issue : issueMap) {
                    String key = (String) issue.get("key");
                    issueList.add(key);
                }
                if (issueMap.size() < maxResults) {
                    break;
                }
                startAt += maxResults;
            }

            if (!issueList.isEmpty()) {
                ctx.setVariable("issueList", issueList);
                ctx.setVariable("issueCount", issueList.size());

            } else {
                throw new RuntimeException("Zero Issues found in project '" + projectKey + "' of type '" + issueType + "' " +
                        "and with status '" + statusOperator + " " + issueStatus + "'");
            }

        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while fetching issue ids from project '" + projectKey + "'", e);
        }
    }

    private String buildAuth(Context ctx, Map<String, Object> cfg) throws Exception {
        Map<String, Object> auth = MapUtils.getMap(cfg, JIRA_AUTH_KEY, null);
        if (auth == null) {
            String uid = MapUtils.assertString(cfg, JIRA_USER_ID_KEY);
            String pwd = MapUtils.assertString(cfg, JIRA_PASSWORD_KEY);

            return Credentials.basic(uid, pwd);
        }

        Map<String, Object> basic = MapUtils.getMap(auth, BASIC_KEY, null);
        if (basic == null) {
            Map<String, Object> secret = MapUtils.assertMap(auth, SECRET_KEY);
            Map<String, String> credentials = getSecretData(ctx, secret);

            return Credentials.basic(credentials.get(USERNAME_KEY), credentials.get(JIRA_PASSWORD_KEY));
        }

        String username = MapUtils.assertString(basic, USERNAME_KEY);
        String password = MapUtils.assertString(basic, JIRA_PASSWORD_KEY);

        return Credentials.basic(username, password);
    }

    private Map<String, String> getSecretData(Context ctx, Map<String, Object> input) throws Exception {
        String secretName = MapUtils.assertString(input, SECRET_NAME_KEY);
        String org = MapUtils.getString(input, ORG_KEY);
        String password = MapUtils.getString(input, JIRA_PASSWORD_KEY);

        String txId = (String) ctx.getVariable(Constants.Context.TX_ID_KEY);
        String workDir = (String) ctx.getVariable(Constants.Context.WORK_DIR_KEY);

        return secretService.exportCredentials(ctx, txId, workDir, org, secretName, password);
    }

    private void currentStatus(Context ctx, Map<String, Object> cfg, String url) {
        String issueKey = MapUtils.assertString(cfg, JIRA_ISSUE_KEY);
        String currentStatus = getStatus(ctx, cfg, url, issueKey);
        ctx.setVariable(JIRA_ISSUE_STATUS_KEY, currentStatus);
    }

    private String getStatus(Context ctx, Map<String, Object> cfg, String url, String issueKey) {
        try {
            Map<String, Object> results = new JiraClient(cfg)
                    .url(url + "issue/" + issueKey + "?fields=status")
                    .jiraAuth(buildAuth(ctx, cfg))
                    .successCode(200)
                    .get();

            Map<String, Object> fields = MapUtils.get(results, "fields", null);
            if (fields != null) {
                Map<String, Object> statusInfo = MapUtils.get(fields, "status", null);
                if (statusInfo != null) {
                    return MapUtils.assertString(statusInfo, "name");
                }
            }

            throw new IllegalStateException("Unexpected data received from JIRA: " + fields);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while getting the current status: " + e.getMessage(), e);
        }
    }

    private static void put(Map<String, Object> m, String k, Context ctx) {
        Object v = ctx.getVariable(k);
        if (v == null) {
            return;
        }

        m.put(k, v);
    }

    private static Action getAction(Map<String, Object> cfg) {
        return Action.valueOf(MapUtils.assertString(cfg, ACTION_KEY).trim().toUpperCase());
    }

    private static String formatUrl(String s) {
        if (s == null) {
            return null;
        }

        if (s.endsWith("/")) {
            return s;
        }

        return s + "/";
    }

    private String configureStatus(String projectKey, String issueType, String issueStatus, String statusOperator) {
        String jqlQuery = "project = " + projectKey + " AND " + "issuetype = " + issueType;
        if (issueStatus != null && !issueStatus.isEmpty()) {
            if ("=".equals(statusOperator)) {
                jqlQuery = jqlQuery + " AND " + "status = " + issueStatus;
            } else if ("!=".equals(statusOperator)) {
                jqlQuery = jqlQuery + " AND " + "status != " + issueStatus;
            } else {
                throw new IllegalArgumentException("Invalid statusOperator. Allowed values are only '=', '!=' ");
            }
        }
        return jqlQuery;
    }

    private enum Action {
        ADDCOMMENT,
        CREATECOMPONENT,
        CREATEISSUE,
        DELETECOMPONENT,
        DELETEISSUE,
        TRANSITION,
        UPDATEISSUE,
        CREATESUBTASK,
        CURRENTSTATUS,
        ADDATTACHMENT,
        GETISSUES
    }
}
