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

import org.wso2.carbon.uis.internal.reference.AppReference;

import java.util.Set;

/**
 * A listener that observes web app deployments.
 *
 * @since 0.8.3
 */
public interface AppDeploymentEventListener {

    /**
     * Invoked when an app is deployed.
     *
     * @param appReference a reference to the app
     */
    void appDeploymentEvent(AppReference appReference);

    @Deprecated
    void appsDeploymentEvents(Set<AppReference> appReferences);

    /**
     * Invoked when an app is undeployed.
     *
     * @param appName name of the undeploying app.
     */
    void appUndeploymentEvent(String appName);
}
