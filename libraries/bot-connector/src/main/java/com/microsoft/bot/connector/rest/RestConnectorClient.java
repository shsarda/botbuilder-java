/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.bot.connector.rest;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.bot.connector.Attachments;
import com.microsoft.bot.connector.ConnectorClient;
import com.microsoft.bot.connector.Conversations;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.retry.RetryStrategy;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Bot Connector REST API allows your bot to send and receive messages
 * to channels configured in the
 * [Bot Framework Developer Portal](https://dev.botframework.com). The
 * Connector service uses industry-standard REST
 * and JSON over HTTPS.
 *
 * Client libraries for this REST API are available. See below for a list.
 *
 * Many bots will use both the Bot Connector REST API and the associated
 * [Bot State REST API](/en-us/restapi/state). The
 * Bot State REST API allows a bot to store and retrieve state associated
 * with users and conversations.
 *
 * Authentication for both the Bot Connector and Bot State REST APIs is
 * accomplished with JWT Bearer tokens, and is
 * described in detail in the [Connector
 * Authentication](/en-us/restapi/authentication) document.
 */
public class RestConnectorClient extends AzureServiceClient implements ConnectorClient {
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    @Override
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    @Override
    public RestClient getRestClient() {
        return super.restClient();
    }

    /** Gets or sets the preferred language for the response. */
    private String acceptLanguage;
    private String user_agent_string;

    /**
     * @see ConnectorClient#getAcceptLanguage()
     */
    @Override
    public String getAcceptLanguage() {
        return this.acceptLanguage;
    }

    /**
     * @see ConnectorClient#setAcceptLanguage(String)
     */
    @Override
    public void setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }

    /**
     * RetryStrategy as defined in Microsoft Rest Retry
     */
    private RetryStrategy retryStrategy = null;
    public void setRestRetryStrategy(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
    }
    public RetryStrategy getRestRetryStrategy() {
        return this.retryStrategy;
    }

    /** Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30. */
    private int longRunningOperationRetryTimeout;

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    @Override
    public int getLongRunningOperationRetryTimeout() {
        return this.longRunningOperationRetryTimeout;
    }

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     */
    @Override
    public void setLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
        this.longRunningOperationRetryTimeout = longRunningOperationRetryTimeout;
    }

    /** When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true. */
    private boolean generateClientRequestId;

    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @return the generateClientRequestId value.
     */
    @Override
    public boolean getGenerateClientRequestId() {
        return this.generateClientRequestId;
    }

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @param generateClientRequestId the generateClientRequestId value.
     */
    @Override
    public void setGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
    }

    /**
     * The Attachments object to access its operations.
     */
    private Attachments attachments;

    /**
     * Gets the Attachments object to access its operations.
     * @return the Attachments object.
     */
    @Override
    public Attachments getAttachments() {
        return this.attachments;
    }

    /**
     * The Conversations object to access its operations.
     */
    private Conversations conversations;

    /**
     * Gets the Conversations object to access its operations.
     * @return the Conversations object.
     */
    @Override
    public Conversations getConversations() {
        return this.conversations;
    }

    /**
     * Initializes an instance of ConnectorClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public RestConnectorClient(ServiceClientCredentials credentials) {
        this("https://api.botframework.com", credentials);
    }

    /**
     * Initializes an instance of ConnectorClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public RestConnectorClient(String baseUrl, ServiceClientCredentials credentials) {
        super(baseUrl, credentials);
        initialize();
    }

    /**
     * Initializes an instance of ConnectorClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public RestConnectorClient(RestClient restClient){
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.attachments = new RestAttachments(restClient().retrofit(), this);
        this.conversations = new RestConversations(restClient().retrofit(), this);
        this.azureClient = new AzureClient(this);


        // Format according to https://github.com/Microsoft/botbuilder-dotnet/blob/d342cd66d159a023ac435aec0fdf791f93118f5f/doc/UserAgents.md
        String build_version;
        final Properties properties = new Properties();
        try {
            InputStream propStream = RestConnectorClient.class.getClassLoader().getResourceAsStream("project.properties");
            properties.load(propStream);
            build_version = properties.getProperty("version");
        } catch (IOException e) {
            e.printStackTrace();
            build_version = "4.0.0";
        }

        String os_version = System.getProperty("os.name");
        String java_version = System.getProperty("java.version");
        this.user_agent_string = String.format("BotBuilder/%s (JVM %s; %s)", build_version, java_version, os_version);
    }


    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */

    @Override
    public String getUserAgent() {
        return this.user_agent_string;
    }

    // this is to override the AzureServiceClient version
    @Override
    public String userAgent() {
        return getUserAgent();
    }

    /**
     * This is a copy of what the Azure Client does to create a RestClient.  This returns
     * a RestClient.Builder so that the app can create a custom RestClient, and supply
     * it to ConnectorClient during construction.
     *
     * One use case of this is for supplying a Proxy to the RestClient.  Though it is
     * recommended to set proxy information via the Java system properties.
     *
     * @param baseUrl
     * @param credentials
     * @return
     */
    public static RestClient.Builder getDefaultRestClientBuilder(String baseUrl, ServiceClientCredentials credentials){
        return new RestClient.Builder(new OkHttpClient.Builder(), new Retrofit.Builder())
                .withBaseUrl(baseUrl)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory());
    }
}
