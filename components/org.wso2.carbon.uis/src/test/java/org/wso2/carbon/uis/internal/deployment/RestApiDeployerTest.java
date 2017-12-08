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

import org.testng.annotations.Test;
import org.wso2.carbon.uis.api.App;
import org.wso2.carbon.uis.api.Configuration;
import org.wso2.carbon.uis.internal.http.msf4j.MicroserviceRegistration;
import org.wso2.carbon.uis.internal.http.msf4j.MicroservicesRegistrar;
import org.wso2.carbon.uis.spi.RestApiProvider;
import org.wso2.msf4j.Microservice;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
        RestApiDeployer restApiDeployer = new RestApiDeployer(Collections.emptySet(), createMicroservicesRegistrar());
        restApiDeployer.appDeploymentEvent(createApp("foo"));
    }

    @Test
    public void testAppDeploymentEventForHttp() {
        Set<RestApiProvider> restApiProviders = Collections.singleton(createRestApiProvider("foo"));
        MicroservicesRegistrar microservicesRegistrar = createMicroservicesRegistrar();
        RestApiDeployer restApiDeployer = new RestApiDeployer(restApiProviders, microservicesRegistrar);

        restApiDeployer.appDeploymentEvent(createApp("foo"));
        verify(microservicesRegistrar).registerMicroservice(any(), eq("/foo/bar"));
    }

    @Test
    public void testAppDeploymentEventForHttps() {
        Set<RestApiProvider> restApiProviders = Collections.singleton(createRestApiProvider("foo"));
        MicroservicesRegistrar microservicesRegistrar = createMicroservicesRegistrar();
        RestApiDeployer restApiDeployer = new RestApiDeployer(restApiProviders, microservicesRegistrar);

        Configuration configuration = mock(Configuration.class);
        when(configuration.isHttpsOnly()).thenReturn(true);
        App app = createApp("foo");
        when(app.getConfiguration()).thenReturn(configuration);

        restApiDeployer.appDeploymentEvent(app);
        verify(microservicesRegistrar).registerSecuredMicroservice(any(), eq("/foo/bar"));
    }

    @Test
    public void testAppUndeploymentEventWithZeroRestApiProviders() {
        RestApiDeployer restApiDeployer = new RestApiDeployer(Collections.emptySet(), createMicroservicesRegistrar());
        restApiDeployer.appDeploymentEvent(createApp("foo"));
        restApiDeployer.appUndeploymentEvent("foo");
    }

    @Test
    public void testAppUndeploymentEvent() {
        Set<RestApiProvider> restApiProviders = Collections.singleton(createRestApiProvider("foo"));
        MicroservicesRegistrar microservicesRegistrar = createMicroservicesRegistrar();
        RestApiDeployer restApiDeployer = new RestApiDeployer(restApiProviders, microservicesRegistrar);
        App app = createApp("foo");

        restApiDeployer.appDeploymentEvent(app);
        restApiDeployer.appUndeploymentEvent(app.getName());
    }

    @Test
    public void testClose() {
        Set<RestApiProvider> restApiProviders = Collections.singleton(createRestApiProvider("foo"));
        MicroservicesRegistrar microservicesRegistrar = createMicroservicesRegistrar();
        RestApiDeployer restApiDeployer = new RestApiDeployer(restApiProviders, microservicesRegistrar);

        restApiDeployer.appDeploymentEvent(createApp("foo"));
        restApiDeployer.close();
    }

    private static RestApiProvider createRestApiProvider(String appName) {
        RestApiProvider restApiProvider = mock(RestApiProvider.class);
        when(restApiProvider.getAppName()).thenReturn(appName);
        when(restApiProvider.getMicroservices()).thenReturn(Collections.singletonMap("/bar", mock(Microservice.class)));
        return restApiProvider;
    }

    private static MicroservicesRegistrar createMicroservicesRegistrar() {
        MicroservicesRegistrar microservicesRegistrar = mock(MicroservicesRegistrar.class);
        when(microservicesRegistrar.registerMicroservice(any(), anyString()))
                .thenReturn(mock(MicroserviceRegistration.class));
        when(microservicesRegistrar.registerSecuredMicroservice(any(), anyString()))
                .thenReturn(mock(MicroserviceRegistration.class));
        return microservicesRegistrar;
    }

    private static App createApp(String appName) {
        App app = mock(App.class);
        when(app.getName()).thenReturn(appName);
        when(app.getContextPath()).thenReturn("/" + appName);
        when(app.getConfiguration()).thenReturn(Configuration.DEFAULT_CONFIGURATION);
        return app;
    }
}
