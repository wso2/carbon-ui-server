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
import org.wso2.carbon.uis.api.Configuration;
import org.wso2.carbon.uis.api.ServerConfiguration;
import org.wso2.carbon.uis.internal.deployment.msf4j.MicroserviceRegistration;
import org.wso2.carbon.uis.internal.exception.AppDeploymentEventListenerException;
import org.wso2.carbon.uis.internal.http.HttpTransport;
import org.wso2.carbon.uis.internal.http.msf4j.MicroservicesRegistrar;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link AppTransportBinder} class.
 *
 * @since 0.15.0
 */
public class AppTransportBinderTest {

    @Test
    public void testAppDeploymentEventNoConfigurationZeroRegistrations() {
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), anyString())).thenReturn(Collections.emptySet());
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        AppTransportBinder appTransportBinder = new AppTransportBinder(microservicesRegistrar, serverConfiguration);

        App app = createApp();
        final String appContextPath = app.getContextPath();

        Assert.assertThrows(AppDeploymentEventListenerException.class,
                            () -> appTransportBinder.appDeploymentEvent(app));
        verify(microservicesRegistrar).register(any(), eq(appContextPath));
    }

    @Test
    public void testAppDeploymentEventNoConfiguration() {
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), anyString()))
                .thenReturn(Collections.singleton(createMicroserviceRegistration()));
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        AppTransportBinder appTransportBinder = new AppTransportBinder(microservicesRegistrar, serverConfiguration);

        App app = createApp();
        final String appContextPath = app.getContextPath();

        appTransportBinder.appDeploymentEvent(app);
        verify(microservicesRegistrar).register(any(), eq(appContextPath));
    }

    @Test
    public void testAppDeploymentEventInvalidTransportId() {
        App app = createApp();
        final String appName = app.getName();
        final String appContextPath = app.getContextPath();

        final String transportId = "someTransportId";
        ServerConfiguration.AppConfiguration appConfiguration = mock(ServerConfiguration.AppConfiguration.class);
        when(appConfiguration.getTransportId()).thenReturn(Optional.of(transportId));
        ServerConfiguration serverConfiguration = mock(ServerConfiguration.class);
        when(serverConfiguration.getConfigurationForApp(eq(appName))).thenReturn(Optional.of(appConfiguration));

        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), eq(appContextPath), eq(transportId)))
                .thenThrow(IllegalArgumentException.class);

        AppTransportBinder appTransportBinder = new AppTransportBinder(microservicesRegistrar, serverConfiguration);
        Assert.assertThrows(AppDeploymentEventListenerException.class,
                            () -> appTransportBinder.appDeploymentEvent(app));
    }

    @Test
    public void testAppDeploymentEventValidTransportId() {
        App app = createApp();
        final String appName = app.getName();
        final String appContextPath = app.getContextPath();

        final String transportId = "bar";
        ServerConfiguration.AppConfiguration appConfiguration = mock(ServerConfiguration.AppConfiguration.class);
        when(appConfiguration.getTransportId()).thenReturn(Optional.of(transportId));
        ServerConfiguration serverConfiguration = mock(ServerConfiguration.class);
        when(serverConfiguration.getConfigurationForApp(eq(appName))).thenReturn(Optional.of(appConfiguration));

        MicroserviceRegistration microserviceRegistration = createMicroserviceRegistration();
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), eq(appContextPath), eq(transportId)))
                .thenReturn(microserviceRegistration);

        AppTransportBinder appTransportBinder = new AppTransportBinder(microservicesRegistrar, serverConfiguration);
        try {
            appTransportBinder.appDeploymentEvent(app);
        } catch (Exception e) {
            Assert.fail("Cannot register web app Microservice for transport '" + transportId + "'.", e);
        }
    }

    @Test
    public void testAppUndeploymentEventWithInvalidAppName() {
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), anyString()))
                .thenReturn(Collections.singleton(createMicroserviceRegistration()));
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        AppTransportBinder appTransportBinder = new AppTransportBinder(microservicesRegistrar, serverConfiguration);

        appTransportBinder.appDeploymentEvent(createApp());
        Assert.assertThrows(AppDeploymentEventListenerException.class,
                            () -> appTransportBinder.appUndeploymentEvent("foobar"));
    }

    @Test
    public void testAppUndeploymentEvent() {
        MicroserviceRegistration microserviceRegistration = spy(createMicroserviceRegistration());
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), anyString()))
                .thenReturn(Collections.singleton(microserviceRegistration));
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        AppTransportBinder appTransportBinder = new AppTransportBinder(microservicesRegistrar, serverConfiguration);

        App app = createApp();

        appTransportBinder.appDeploymentEvent(app);
        appTransportBinder.appUndeploymentEvent(app.getName());
        verify(microserviceRegistration).unregister();
    }

    @Test
    public void testClose() {
        MicroserviceRegistration microserviceRegistration = spy(createMicroserviceRegistration());
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.register(any(), anyString()))
                .thenReturn(Collections.singleton(microserviceRegistration));
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        AppTransportBinder appTransportBinder = new AppTransportBinder(microservicesRegistrar, serverConfiguration);

        appTransportBinder.appDeploymentEvent(createApp());
        appTransportBinder.close();
        verify(microserviceRegistration).unregister();
    }

    @SuppressWarnings("unchecked")
    private static MicroserviceRegistration createMicroserviceRegistration() {
        HttpTransport httpTransport = new HttpTransport("foo", "bar", "http", "localhost", 9090);
        return new MicroserviceRegistration(httpTransport, mock(ServiceRegistration.class));
    }

    private static App createApp() {
        App app = mock(App.class);
        when(app.getName()).thenReturn("foo");
        when(app.getContextPath()).thenReturn("/foo");
        when(app.getConfiguration()).thenReturn(Configuration.DEFAULT_CONFIGURATION);
        return app;
    }
}
