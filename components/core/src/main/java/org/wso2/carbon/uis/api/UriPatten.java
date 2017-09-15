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

package org.wso2.carbon.uis.api;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents an URI pattern.
 *
 * @since 0.8.0
 */
public class UriPatten implements Comparable<UriPatten> {

    private static final Pattern URI_VARIABLE_PATTERN = Pattern.compile("\\{(.+?)\\}");
    private static final String URI_VARIABLE_REGEX = "([^/]+)";
    private static final String PLUS_MARKED_URI_VARIABLE_REGEX = "(.+)";

    private final String patternString;
    private final Pattern pattern;
    private final List<String> variableNames;

    /**
     * Creates a new {@code UriPatten} instance that represents the specified URI pattern.
     *
     * @param uriPattern URI pattern
     */
    public UriPatten(String uriPattern) {
        Pair<Boolean, List<String>> analyseResult = analyse(uriPattern);
        String indexPathRegex = null;
        // URI pattern cleanup
        if (uriPattern.endsWith("/index")) {
            uriPattern = uriPattern.substring(0, (uriPattern.length() - "index".length()));
            indexPathRegex = "(index)?";
        }
        this.patternString = uriPattern;
        boolean hasPlusMarkedVariable = analyseResult.getLeft();
        this.variableNames = analyseResult.getRight();

        String patternRegex = URI_VARIABLE_PATTERN.splitAsStream(uriPattern)
                .map(Pattern::quote)
                .collect(Collectors.joining(URI_VARIABLE_REGEX));
        if (uriPattern.charAt(uriPattern.length() - 1) == '}') {
            patternRegex += (hasPlusMarkedVariable) ? PLUS_MARKED_URI_VARIABLE_REGEX : URI_VARIABLE_REGEX;
        }
        //append the index path regex if this is uri has ended with /index
        if (indexPathRegex != null) {
            patternRegex = patternRegex + indexPathRegex;
        }
        pattern = Pattern.compile(patternRegex);
    }

    private Pair<Boolean, List<String>> analyse(String uriPattern) {
        if (uriPattern.isEmpty()) {
            throw new IllegalArgumentException("URI pattern cannot be empty.");
        }
        if (uriPattern.charAt(0) != '/') {
            throw new IllegalArgumentException("URI patten must start with a '/'.");
        }

        int delta = 0;
        int currentVariableStartIndex = -1;
        boolean hasPlusMarkedVariable = false;
        List<String> variableNames = new ArrayList<>();
        for (int i = 1; i < uriPattern.length(); i++) {
            char currentChar = uriPattern.charAt(i);
            if (currentChar == '{') {
                delta++;
                if (delta != 1) {
                    throw new IllegalArgumentException(
                            "Illegal URI variable opening '{' found at index " + i + " in URI pattern '" + uriPattern +
                            "'. Cannot declare a variable inside another variable.");
                }
                currentVariableStartIndex = i + 1; // to doge the '{' char
            } else if (currentChar == '}') {
                delta--;
                if (delta != 0) {
                    throw new IllegalArgumentException(
                            "Illegal URI variable closing '}' found at index " + i + " in of URI pattern '" +
                            uriPattern + "'. Cannot find matching opening.");
                }
                if (hasPlusMarkedVariable && (i != uriPattern.length() - 1)) {
                    throw new IllegalArgumentException(
                            "Illegal character found at index " + (i + 1) + " in URI pattern '" + uriPattern +
                            "'. Cannot have any more characters after enclosing a 'one or more matching' type" +
                            " URI variable declaration.");
                }
                variableNames.add(uriPattern.substring(currentVariableStartIndex, i));
            } else if (currentChar == '+') {
                if ((delta == 1) && (uriPattern.charAt(i - 1) == '{')) {
                    currentVariableStartIndex++; // to doge the '+' char
                    hasPlusMarkedVariable = true;
                }
            }
        }
        if (delta > 0) {
            throw new IllegalArgumentException(
                    "Illegal URI variable opening '{' found at index " + (currentVariableStartIndex - 1) +
                    " in URI pattern '" + uriPattern + "' which was never closed.");
        }
        return Pair.of(hasPlusMarkedVariable, variableNames);
    }

    /**
     * Checks whether this URI patterns matches tto the given URI.
     *
     * @param uri URI to be matched
     * @return {@code true} iff macthes, otherwise {@code false}
     */
    public boolean matches(String uri) {
        return pattern.matcher(uri).matches();
    }

    /**
     * Matches the specified URI with this URI pattern and returns values that matches the variables in the URI pattern.
     *
     * @param uri URI to be matched
     * @return {@link Optional#empty() empty optional} if URi doesn't match, otherwise map of matching variable names
     * and matched values
     */
    public Optional<Map<String, String>> match(String uri) {
        Matcher matcher = this.pattern.matcher(uri);
        if (matcher.matches()) {
            if (!variableNames.isEmpty()) {
                Map<String, String> result = new HashMap<>(variableNames.size());
                for (int i = 0; i < variableNames.size(); i++) {
                    String name = variableNames.get(i);
                    String value = matcher.group(i + 1);
                    result.put(name, value);
                }
                return Optional.of(result);
            }
            return Optional.of(Collections.emptyMap());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int compareTo(UriPatten otherUriPattern) {
        if (otherUriPattern == null) {
            return 1;
        }

        String[] a = URI_VARIABLE_PATTERN.split(patternString);
        String[] b = URI_VARIABLE_PATTERN.split(otherUriPattern.patternString);
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            int aLen = a[i].length();
            int bLen = b[i].length();
            if (aLen != bLen) {
                return bLen - aLen;
            }
        }
        if (a.length == b.length) {
            return otherUriPattern.patternString.compareTo(patternString);
        } else {
            return b.length - a.length;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (obj == this) || ((obj instanceof UriPatten)) && (this.compareTo((UriPatten) obj) == 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternString, pattern);
    }

    @Override
    public String toString() {
        return "UriPatten{patternString='" + patternString + "', pattern=" + pattern + "'}";
    }
}
