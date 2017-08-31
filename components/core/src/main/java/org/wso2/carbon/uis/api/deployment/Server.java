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

package org.wso2.carbon.uis.api.deployment;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uis.api.http.HttpConnector;
import org.wso2.carbon.uis.api.http.HttpRequest;
import org.wso2.carbon.uis.api.http.HttpResponse;
import org.wso2.carbon.uis.internal.RequestDispatcher;
import org.wso2.carbon.uis.internal.deployment.AppFinder;
import org.wso2.carbon.uis.internal.deployment.AppRegistry;
import org.wso2.carbon.uis.internal.io.ArtifactAppFinder;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * Created by sajith on 8/22/17.
 */
public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final AppFinder appFinder;
    private final AppRegistry appRegistry;
    private final HttpConnector httpConnector;
    private final Function<HttpRequest, HttpResponse> httpListener;

    public Server(Path appsRepository, HttpConnector httpConnector) {
        this(new ArtifactAppFinder(appsRepository), httpConnector);
    }

    public Server(AppFinder appFinder, HttpConnector httpConnector) {
        this(appFinder, new AppRegistry(appFinder), httpConnector);
    }

    Server(AppFinder appFinder, AppRegistry appRegistry, HttpConnector httpConnector) {
        this.appFinder = appFinder;
        this.appRegistry = appRegistry;
        this.httpConnector = httpConnector;
        RequestDispatcher requestDispatcher = new RequestDispatcher();
        this.httpListener = httpRequest -> requestDispatcher.serve(httpRequest, appRegistry);
    }

    public void start() {
        List<Pair<String, String>> availableApps = appFinder.getAvailableApps();
        LOGGER.debug("'" + availableApps.size() + "' web app(s) found.");
        for (Pair<String, String> appNameContextPath : availableApps) {
            httpConnector.registerApp(appNameContextPath.getLeft(), appNameContextPath.getRight(), httpListener);
        }
    }

    public void stop() {

    }
}
