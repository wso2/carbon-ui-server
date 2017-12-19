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

package org.wso2.carbon.uiserver.internal.deployment.parser;

import org.wso2.carbon.uiserver.internal.exception.ConfigurationException;
import org.wso2.carbon.uiserver.internal.exception.FileOperationException;
import org.wso2.carbon.uiserver.internal.reference.FileReference;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Parser for YAML configuration files in web apps.
 *
 * @since 0.12.1
 */
public class YamlFileParser {

    /**
     * Parses the given YAML configuration file and de-serialize the content into given bean type.
     *
     * @param yamlFile reference to the YAML configuration file
     * @param type     class of the bean to be used when de-serializing
     * @param <T>      type of the bean class to be used when de-serializing
     * @return returns the populated bean instance
     * @throws ConfigurationException if cannot read or parse the content of the specified YAML file
     */
    public static <T> T parse(FileReference yamlFile, Class<T> type) throws ConfigurationException {
        T loadedBean;
        try {
            loadedBean = new Yaml(new ClassLoaderConstructor(type)).loadAs(yamlFile.getContent(), type);
        } catch (FileOperationException e) {
            throw new ConfigurationException(
                    "Cannot read the YAML configuration file '" + yamlFile.getFilePath() + "'.", e);
        } catch (Exception e) {
            throw new ConfigurationException(
                    "Cannot parse the YAML configuration file '" + yamlFile.getFilePath() + "'.", e);
        }
        if (loadedBean == null) {
            // Either configuration file is empty or has only comments.
            throw new ConfigurationException(
                    "Cannot load the YAML configuration file '" + yamlFile.getFilePath() + "' as it is empty.");
        }
        return loadedBean;
    }

    /**
     * OSGi friendly YAML constructor that loads classes through a given class object.
     *
     * @since 0.12.1
     */
    private static class ClassLoaderConstructor extends Constructor {

        private final ClassLoader classLoader;

        /**
         * Creates a new constructor.
         *
         * @param aClass class that uses for class loading
         */
        public ClassLoaderConstructor(Class aClass) {
            super(Object.class);
            this.classLoader = aClass.getClassLoader();
        }

        @Override
        protected Class<?> getClassForName(String name) throws ClassNotFoundException {
            return Class.forName(name, true, classLoader);
        }
    }
}
