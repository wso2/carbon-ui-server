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

package org.wso2.carbon.uiserver.internal.deployment.msf4j;

import org.osgi.framework.ServiceRegistration;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.uiserver.internal.http.HttpTransport;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test cases for {@link MicroserviceRegistration} class.
 *
 * @since 0.15.0
 */
public class MicroserviceRegistrationTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testGetRegisteredHttpTransport() {
        HttpTransport httpTransport = createHttpTransport();
        ServiceRegistration serviceRegistration = mock(ServiceRegistration.class);
        MicroserviceRegistration microserviceRegistration = new MicroserviceRegistration(httpTransport,
                                                                                         serviceRegistration);

        Assert.assertEquals(microserviceRegistration.getRegisteredHttpTransport(), httpTransport);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUnregister() {
        HttpTransport httpTransport = createHttpTransport();
        ServiceRegistration serviceRegistration = mock(ServiceRegistration.class);
        new MicroserviceRegistration(httpTransport, serviceRegistration).unregister();

        verify(serviceRegistration).unregister();
    }

    private static HttpTransport createHttpTransport() {
        return new HttpTransport("foo", "bar", "http", "localhost", 9292);
    }
}
