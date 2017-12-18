/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uis.internal.deployment.listener;

import org.osgi.framework.ServiceRegistration;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.ServerConfiguration;
import org.wso2.carbon.uis.internal.deployment.msf4j.MicroserviceRegistration;
import org.wso2.carbon.uis.internal.exception.AppDeploymentEventListenerException;
import org.wso2.carbon.uis.internal.http.HttpTransport;
import org.wso2.carbon.uis.internal.http.msf4j.MicroservicesRegistrar;
import org.wso2.carbon.uis.spi.RestApiProvider;
import org.wso2.msf4j.Microservice;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link RestApiDeployer} class.
 *
 * @since 0.15.0
 */
public class RestApiDeployerTest {

    @Test
    public void testAppDeploymentEventWithZeroRestApiProviders() {
        RestApiDeployer restApiDeployer = new RestApiDeployer(Collections.emptySet(), createMicroservicesRegistrar(),
                                                              new ServerConfiguration());
        restApiDeployer.appDeploymentEvent(createApp());
    }

    @Test
    public void testAppDeploymentEventWithInvalidContextPath() {
        App app = createApp();
        final String appName = app.getName();

        RestApiProvider restApiProvider = mock(RestApiProvider.class);
        when(restApiProvider.getAppName()).thenReturn(appName);
        RestApiDeployer restApiDeployer = new RestApiDeployer(Collections.singleton(restApiProvider),
                                                              createMicroservicesRegistrar(),
                                                              new ServerConfiguration());

        when(restApiProvider.getMicroservices(any()))
                .thenReturn(Collections.singletonMap("", mock(Microservice.class)));
        Assert.assertThrows(AppDeploymentEventListenerException.class,
                            () -> restApiDeployer.appDeploymentEvent(app));

        when(restApiProvider.getMicroservices(any()))
                .thenReturn(Collections.singletonMap("bar", mock(Microservice.class)));
        Assert.assertThrows(AppDeploymentEventListenerException.class,
                            () -> restApiDeployer.appDeploymentEvent(app));
    }

    @Test
    public void testAppDeploymentEventNoConfigurationZeroRegistrations() {
        App app = createApp();
        final String appName = app.getName();

        Set<RestApiProvider> restApiProviders = Collections.singleton(createRestApiProvider(appName));
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), anyString())).thenReturn(Collections.emptySet());
        RestApiDeployer restApiDeployer = new RestApiDeployer(restApiProviders, microservicesRegistrar,
                                                              new ServerConfiguration());

        Assert.assertThrows(AppDeploymentEventListenerException.class,
                            () -> restApiDeployer.appDeploymentEvent(app));
    }

    @Test
    public void testAppDeploymentEventNoConfiguration() {
        App app = createApp();
        final String appName = app.getName();
        final String appContextPath = app.getContextPath();

        Set<RestApiProvider> restApiProviders = Collections.singleton(createRestApiProvider(appName));
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), anyString()))
                .thenReturn(Collections.singleton(createMicroserviceRegistration()));
        RestApiDeployer restApiDeployer = new RestApiDeployer(restApiProviders, microservicesRegistrar,
                                                              new ServerConfiguration());

        restApiDeployer.appDeploymentEvent(app);
        verify(microservicesRegistrar).register(any(), eq(appContextPath + "/bar"));
    }

    @Test
    public void testAppDeploymentEventInvalidTransportId() {
        App app = createApp();
        final String appName = app.getName();
        final String appContextPath = app.getContextPath();

        final String transportId = "someTransportId";
        Set<RestApiProvider> restApiProviders = Collections.singleton(createRestApiProvider(appName));

        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), eq(appContextPath + "/bar"), eq(transportId)))
                .thenThrow(IllegalArgumentException.class);

        ServerConfiguration.AppConfiguration appConfiguration = mock(ServerConfiguration.AppConfiguration.class);
        when(appConfiguration.getTransportId()).thenReturn(Optional.of(transportId));
        ServerConfiguration serverConfiguration = mock(ServerConfiguration.class);
        when(serverConfiguration.getConfigurationForApp(eq(appName))).thenReturn(Optional.of(appConfiguration));


        RestApiDeployer restApiDeployer = new RestApiDeployer(restApiProviders, microservicesRegistrar,
                                                              serverConfiguration);
        Assert.assertThrows(AppDeploymentEventListenerException.class,
                            () -> restApiDeployer.appDeploymentEvent(app));
    }

    @Test
    public void testAppDeploymentEventValidTransportId() {
        App app = createApp();
        final String appName = app.getName();
        final String appContextPath = app.getContextPath();

        final String transportId = "some-id";
        Set<RestApiProvider> restApiProviders = Collections.singleton(createRestApiProvider(appName));

        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), eq(appContextPath + "/bar"), eq(transportId)))
                .thenReturn(createMicroserviceRegistration());

        ServerConfiguration.AppConfiguration appConfiguration = mock(ServerConfiguration.AppConfiguration.class);
        when(appConfiguration.getTransportId()).thenReturn(Optional.of(transportId));
        ServerConfiguration serverConfiguration = mock(ServerConfiguration.class);
        when(serverConfiguration.getConfigurationForApp(eq(appName))).thenReturn(Optional.of(appConfiguration));


        RestApiDeployer restApiDeployer = new RestApiDeployer(restApiProviders, microservicesRegistrar,
                                                              serverConfiguration);
        try {
            restApiDeployer.appDeploymentEvent(app);
        } catch (Exception e) {
            Assert.fail("Cannot register REST API Microservice for transport '" + transportId + "'.", e);
        }
    }

    @Test
    public void testAppUndeploymentEvent() {
        App app = createApp();
        final String appName = app.getName();

        Set<RestApiProvider> restApiProviders = Collections.singleton(createRestApiProvider(appName));
        MicroserviceRegistration microserviceRegistration = spy(createMicroserviceRegistration());
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), anyString()))
                .thenReturn(Collections.singleton(microserviceRegistration));
        RestApiDeployer restApiDeployer = new RestApiDeployer(restApiProviders, microservicesRegistrar,
                                                              new ServerConfiguration());

        restApiDeployer.appDeploymentEvent(app);
        restApiDeployer.appUndeploymentEvent(appName);
        verify(microserviceRegistration).unregister();
    }

    @Test
    public void testClose() {
        App app = createApp();
        final String appName = app.getName();

        Set<RestApiProvider> restApiProviders = Collections.singleton(createRestApiProvider(appName));
        MicroserviceRegistration microserviceRegistration = spy(createMicroserviceRegistration());
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), anyString()))
                .thenReturn(Collections.singleton(microserviceRegistration));
        RestApiDeployer restApiDeployer = new RestApiDeployer(restApiProviders, microservicesRegistrar,
                                                              new ServerConfiguration());

        restApiDeployer.appDeploymentEvent(app);
        restApiDeployer.close();
        verify(microserviceRegistration).unregister();
    }

    private static RestApiProvider createRestApiProvider(String appName) {
        RestApiProvider restApiProvider = mock(RestApiProvider.class);
        when(restApiProvider.getAppName()).thenReturn(appName);
        when(restApiProvider.getMicroservices(any()))
                .thenReturn(Collections.singletonMap("/bar", mock(Microservice.class)));
        return restApiProvider;
    }

    private static MicroservicesRegistrar createMicroservicesRegistrar() {
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), anyString(), anyString()))
                .thenReturn(createMicroserviceRegistration());
        when(microservicesRegistrar.register(any(), anyString()))
                .thenReturn(Collections.singleton(createMicroserviceRegistration()));
        return microservicesRegistrar;
    }

    @SuppressWarnings("unchecked")
    private static MicroserviceRegistration createMicroserviceRegistration() {
        HttpTransport httpTransport = new HttpTransport("something", "some-id", "http", "localhost", 9090);
        return new MicroserviceRegistration(httpTransport, mock(ServiceRegistration.class));
    }

    private static App createApp() {
        App app = mock(App.class);
        when(app.getName()).thenReturn("foo");
        when(app.getContextPath()).thenReturn("/foo");
        return app;
    }
}
