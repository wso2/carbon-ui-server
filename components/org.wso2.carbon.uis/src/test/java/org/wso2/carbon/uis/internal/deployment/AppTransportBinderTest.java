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

package org.wso2.carbon.uis.internal.deployment;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.Configuration;
import org.wso2.carbon.uis.internal.http.HttpTransport;
import org.wso2.carbon.uis.internal.http.msf4j.MicroserviceRegistration;
import org.wso2.carbon.uis.internal.http.msf4j.MicroservicesRegistrar;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link AppTransportBinder} class.
 *
 * @since 0.15.0
 */
public class AppTransportBinderTest {

    @Test
    public void testAppDeploymentEventForHttp() {
        MicroservicesRegistrar microservicesRegistrar = createMicroservicesRegistrar();
        AppTransportBinder appTransportBinder = new AppTransportBinder(microservicesRegistrar);
        App app = createApp();
        final String appContextPath = app.getContextPath();

        appTransportBinder.appDeploymentEvent(app);
        verify(microservicesRegistrar).registerMicroservice(any(), eq(appContextPath));
    }

    @Test
    public void testAppDeploymentEventForHttps() {
        MicroservicesRegistrar microservicesRegistrar = createMicroservicesRegistrar();
        AppTransportBinder appTransportBinder = new AppTransportBinder(microservicesRegistrar);

        App app = createApp();
        Configuration configuration = mock(Configuration.class);
        when(configuration.isHttpsOnly()).thenReturn(true);
        when(app.getConfiguration()).thenReturn(configuration);
        final String appContextPath = app.getContextPath();

        appTransportBinder.appDeploymentEvent(app);
        verify(microservicesRegistrar).registerSecuredMicroservice(any(), eq(appContextPath));
    }

    @Test
    public void testAppUndeploymentEventWithException() {
        MicroservicesRegistrar microservicesRegistrar = createMicroservicesRegistrar();
        AppTransportBinder appTransportBinder = new AppTransportBinder(microservicesRegistrar);

        appTransportBinder.appDeploymentEvent(createApp());
        Assert.assertThrows(IllegalArgumentException.class, () -> appTransportBinder.appUndeploymentEvent("bar"));
    }

    @Test
    public void testAppUndeploymentEvent() {
        MicroservicesRegistrar microservicesRegistrar = createMicroservicesRegistrar();
        AppTransportBinder appTransportBinder = new AppTransportBinder(microservicesRegistrar);
        App app = createApp();

        appTransportBinder.appDeploymentEvent(app);
        appTransportBinder.appUndeploymentEvent(app.getName());
    }

    @Test
    public void testClose() {
        MicroservicesRegistrar microservicesRegistrar = createMicroservicesRegistrar();
        AppTransportBinder appTransportBinder = new AppTransportBinder(microservicesRegistrar);
        App app = createApp();

        appTransportBinder.appDeploymentEvent(app);
        appTransportBinder.close();
    }

    private static MicroservicesRegistrar createMicroservicesRegistrar() {
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        MicroserviceRegistration httpMicroserviceRegistration = createMicroserviceRegistration(false);
        when(microservicesRegistrar.registerMicroservice(any(), anyString()))
                .thenReturn(httpMicroserviceRegistration);
        MicroserviceRegistration httpsMicroserviceRegistration = createMicroserviceRegistration(true);
        when(microservicesRegistrar.registerSecuredMicroservice(any(), anyString()))
                .thenReturn(httpsMicroserviceRegistration);
        return microservicesRegistrar;
    }

    private static MicroserviceRegistration createMicroserviceRegistration(boolean isHttps) {
        MicroserviceRegistration microserviceRegistration = mock(MicroserviceRegistration.class);
        if (isHttps) {
            when(microserviceRegistration.getRegisteredHttpTransports())
                    .thenReturn(Collections.singleton(new HttpTransport("some-id", "https", "localhost", 9292)));
        } else {
            when(microserviceRegistration.getRegisteredHttpTransports())
                    .thenReturn(Collections.singleton(new HttpTransport("some-id", "http", "localhost", 9292)));
        }
        return microserviceRegistration;
    }

    private static App createApp() {
        App app = mock(App.class);
        when(app.getName()).thenReturn("foo");
        when(app.getContextPath()).thenReturn("/foo");
        when(app.getConfiguration()).thenReturn(Configuration.DEFAULT_CONFIGURATION);
        return app;
    }
}
