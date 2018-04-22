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

package org.wso2.carbon.uiserver.internal.io.http;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.uiserver.api.App;
import org.wso2.carbon.uiserver.api.Extension;
import org.wso2.carbon.uiserver.api.Theme;
import org.wso2.carbon.uiserver.api.http.HttpRequest;
import org.wso2.carbon.uiserver.api.http.HttpResponse;
import org.wso2.carbon.uiserver.internal.exception.BadRequestException;
import org.wso2.carbon.uiserver.internal.exception.FileOperationException;
import org.wso2.carbon.uiserver.internal.exception.ResourceNotFoundException;
import org.wso2.carbon.uiserver.internal.http.ResponseBuilder;
import org.wso2.carbon.uiserver.internal.io.util.MimeMapper;
import org.wso2.carbon.uiserver.internal.io.util.PathUtils;
import org.wso2.carbon.uiserver.internal.reference.AppReference;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.carbon.uiserver.api.http.HttpResponse.CONTENT_TYPE_IMAGE_PNG;
import static org.wso2.carbon.uiserver.api.http.HttpResponse.CONTENT_TYPE_WILDCARD;
import static org.wso2.carbon.uiserver.api.http.HttpResponse.HEADER_CACHE_CONTROL;
import static org.wso2.carbon.uiserver.api.http.HttpResponse.HEADER_LAST_MODIFIED;
import static org.wso2.carbon.uiserver.api.http.HttpResponse.STATUS_NOT_MODIFIED;

/**
 * Dispatches HTTP requests for static resources.
 *
 * @since 0.8.0
 */
public class StaticRequestDispatcher {

    private static final DateTimeFormatter HTTP_DATE_FORMATTER;
    private static final ZoneId GMT_TIME_ZONE;
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticRequestDispatcher.class);

    private final App app;
    private final Map<Path, ZonedDateTime> resourcesLastModifiedDates;

    static {
        // See https://tools.ietf.org/html/rfc7231#section-7.1.1.1
        HTTP_DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz");
        GMT_TIME_ZONE = ZoneId.of("GMT");
    }

    /**
     * Creates a new request dispatcher.
     *
     * @param app web app to be served
     */
    public StaticRequestDispatcher(App app) {
        this.app = app;
        this.resourcesLastModifiedDates = new ConcurrentHashMap<>();
    }

    /**
     * Serves the default favicon.
     *
     * @param request HTTP request to be served
     * @return HTTP response that carries the default favicon
     */
    public HttpResponse serveDefaultFavicon(HttpRequest request) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("default-favicon.png");
        if (inputStream == null) {
            LOGGER.error("Cannot find default favicon 'default-favicon.png' in classpath.");
            return ResponseBuilder.notFound("Cannot find default favicon").build();
        } else {
            return ResponseBuilder.ok(inputStream, CONTENT_TYPE_IMAGE_PNG).build();
        }
    }

    /**
     * Serves to the supplied HTTP request and returns a HTTP response.
     *
     * @param request HTTP request to be served
     * @return a HTTP response that carries the result
     */
    public HttpResponse serve(HttpRequest request) {
        try {
            Path resourcePath = resolveResource(request);
            ZonedDateTime lastModifiedDate = getLastModifiedDate(resourcePath);
            if (lastModifiedDate == null) {
            /* Since we have failed to read last modified date of 'resourcePath' file, we cannot set cache headers.
            Therefore just serve the file without any cache headers. */
                return ResponseBuilder.ok(resourcePath, getContentType(request, resourcePath))
                        .headers(app.getConfiguration().getResponseHeaders().forStaticResources())
                        .build();
            } else {
                ZonedDateTime ifModifiedSinceDate = getIfModifiedSinceDate(request);
                if ((ifModifiedSinceDate != null) && Duration.between(ifModifiedSinceDate, lastModifiedDate).isZero()) {
                    // Resource is NOT modified since the last serve.
                    return ResponseBuilder.status(STATUS_NOT_MODIFIED).build();
                } else {
                    return ResponseBuilder.ok(resourcePath, getContentType(request, resourcePath))
                            .header(HEADER_LAST_MODIFIED, HTTP_DATE_FORMATTER.format(lastModifiedDate))
                            .header(HEADER_CACHE_CONTROL, "public,max-age=2592000")
                            .headers(app.getConfiguration().getResponseHeaders().forStaticResources())
                            .build();
                }
            }
        } catch (BadRequestException e) {
            return ResponseBuilder.badRequest("Static resource URI '" + request.getUri() + "' is invalid.").build();
        } catch (ResourceNotFoundException e) {
            return ResponseBuilder.notFound("Requested resource '" + request.getUri() + "' does not exists.").build();
        } catch (FileOperationException e) {
            LOGGER.error("An error occurred when manipulating paths for static resource request '{}'.", request, e);
            return ResponseBuilder.serverError("A server error occurred while serving for static resource request.")
                    .build();
        }
    }

    private ZonedDateTime getLastModifiedDate(Path resourcePath) {
        return resourcesLastModifiedDates.computeIfAbsent(resourcePath, StaticRequestDispatcher::readLastModifiedDate);
    }

    private Path resolveResource(HttpRequest request) throws BadRequestException {
        if (request.isAppStaticResourceRequest()) {
            // /public/app/...
            return resolveResourceInApp(app, request.getUriWithoutContextPath());
        } else if (request.isExtensionStaticResourceRequest()) {
            // /public/extensions/...
            return resolveResourceInExtension(app, request.getUriWithoutContextPath());
        } else if (request.isThemeStaticResourceRequest()) {
            // /public/themes/...
            return resolveResourceInTheme(app, request.getUriWithoutContextPath());
        } else {
            // /public/...
            throw new BadRequestException("Invalid static resource URI '" + request.getUri() + "'.");
        }
    }

    private static Path resolveResourceInApp(App app, String uriWithoutContextPath) throws BadRequestException {
        /* Correct 'uriWithoutContextPath' value must be in "/public/app/{sub-directory}/{rest-of-the-path}" format.
         * So there should be at least 4 slashes. Don't worry about multiple consecutive slashes. They are covered in
         * HttpRequest.isValid() method which is called before this method.
         */

        int slashesCount = 0, thirdSlashIndex = -1;
        for (int i = 0; i < uriWithoutContextPath.length(); i++) {
            if (uriWithoutContextPath.charAt(i) == '/') {
                slashesCount++;
                if (slashesCount == 3) {
                    thirdSlashIndex = i;
                } else if (slashesCount == 4) {
                    break;
                }
            }
        }
        if (slashesCount != 4) {
            throw new BadRequestException("Invalid static resource URI '" + uriWithoutContextPath + "'.");
        }

        // {sub-directory}/{rest-of-the-path}
        String relativeFilePath = uriWithoutContextPath.substring(thirdSlashIndex + 1, uriWithoutContextPath.length());
        return selectPath(app.getPaths(), AppReference.DIR_NAME_PUBLIC_RESOURCES, relativeFilePath);
    }

    private static Path resolveResourceInExtension(App app, String uriWithoutContextPath)
            throws BadRequestException, ResourceNotFoundException {
        /* Correct 'uriWithoutContextPath' value must be in
         * "/public/extensions/{extension-type}/{extension-name}/{rest-of-the-path}" format.
         * So there should be at least 5 slashes. Don't worry about multiple consecutive slashes. They are covered
         * in HttpRequest.isValid() method which is called before this method.
         */

        int slashesCount = 0, thirdSlashIndex = -1, fourthSlashIndex = -1, fifthSlashIndex = -1;
        for (int i = 0; i < uriWithoutContextPath.length(); i++) {
            if (uriWithoutContextPath.charAt(i) == '/') {
                slashesCount++;
                if (slashesCount == 3) {
                    thirdSlashIndex = i;
                } else if (slashesCount == 4) {
                    fourthSlashIndex = i;
                } else if (slashesCount == 5) {
                    fifthSlashIndex = i;
                    break;
                }
            }
        }
        if (slashesCount != 5) {
            throw new BadRequestException("Invalid static resource URI '" + uriWithoutContextPath + "'.");
        }
        String extensionType = uriWithoutContextPath.substring(thirdSlashIndex + 1, fourthSlashIndex);
        String extensionName = uriWithoutContextPath.substring(fourthSlashIndex + 1, fifthSlashIndex);
        Extension extension = app.getExtension(extensionType, extensionName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Extension '" + extensionType + ":" + extensionName + "' found in URI '" +
                        uriWithoutContextPath + "' does not exists."));

        // {rest-of-the-path}
        String relativeFilePath = uriWithoutContextPath.substring(fifthSlashIndex + 1, uriWithoutContextPath.length());
        return selectPath(extension.getPaths(), relativeFilePath);
    }

    private static Path resolveResourceInTheme(App app, String uriWithoutContextPath)
            throws BadRequestException, ResourceNotFoundException {
        /* Correct 'uriWithoutContextPath' value must be in
         * "/public/themes/{theme-name}/{sub-directory}/{rest-of-the-path}" format.
         * So there should be at least 5 slashes. Don't worry about multiple consecutive slashes. They are covered
         * in HttpRequest.isValid() method which is called before this method.
         */

        int slashesCount = 0, thirdSlashIndex = -1, fourthSlashIndex = -1;
        for (int i = 0; i < uriWithoutContextPath.length(); i++) {
            if (uriWithoutContextPath.charAt(i) == '/') {
                slashesCount++;
                if (slashesCount == 3) {
                    thirdSlashIndex = i;
                } else if (slashesCount == 4) {
                    fourthSlashIndex = i;
                } else if (slashesCount == 5) {
                    break;
                }
            }
        }
        if (slashesCount != 5) {
            throw new BadRequestException("Invalid static resource URI '" + uriWithoutContextPath + "'.");
        }
        String themeName = uriWithoutContextPath.substring(thirdSlashIndex + 1, fourthSlashIndex);
        Theme theme = app.getTheme(themeName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Theme '" + themeName + "' found in URI '" + uriWithoutContextPath + "' does not exists."));

        // {sub-directory}/{rest-of-the-path}
        String relativeFilePath = uriWithoutContextPath.substring(fourthSlashIndex + 1, uriWithoutContextPath.length());
        return selectPath(theme.getPaths(), relativeFilePath);
    }

    private static ZonedDateTime readLastModifiedDate(Path resourcePath)
            throws ResourceNotFoundException, FileOperationException {
        if (!Files.isReadable(resourcePath)) {
            throw new ResourceNotFoundException("Static resource file '" + resourcePath + "' is not readable.");
        }

        BasicFileAttributes fileAttributes;
        try {
            fileAttributes = Files.readAttributes(resourcePath, BasicFileAttributes.class);
        } catch (NoSuchFileException | FileNotFoundException e) {
            // This shouldn't be happening because we checked the file's readability before. But just in case.
            throw new ResourceNotFoundException("Static resource file '" + resourcePath + "' does not exists.", e);
        } catch (UnsupportedOperationException | IOException e) {
            throw new FileOperationException(
                    "Cannot read file attributes from static resource file '" + resourcePath + "'.", e);
        }
        if (fileAttributes.isRegularFile()) {
            return ZonedDateTime.ofInstant(fileAttributes.lastModifiedTime().toInstant(), GMT_TIME_ZONE);
        } else {
            /*
             * From book "OCP: Oracle Certified Professional Java SE 8 Programmer II Study Guide" page 478:
             *      Java defines a regular file as one that contains content, as opposed to a symbolic link,
             *      directory, resource (e.g. port, pipe), or other non-regular files that may be present in some
             *      operating systems. [...] It is possible for isRegularFile() to return true for a symbolic link,
             *      as long as the link resolves to a regular file.
             * Hence, checking 'isRegularFile' of a file is enough to determine its existence and not being a directory.
             */
            throw new ResourceNotFoundException("Static resource file '" + resourcePath + "' is not a regular file.");
        }
    }

    private static ZonedDateTime getIfModifiedSinceDate(HttpRequest request) {
        // If-Modified-Since: Sat, 29 Oct 1994 19:43:31 GMT
        String ifModifiedSinceHeader = request.getHeaders().get("If-Modified-Since");
        if (ifModifiedSinceHeader == null) {
            return null; // 'If-Modified-Since' does not exists in HTTP headres.
        }

        try {
            return ZonedDateTime.parse(ifModifiedSinceHeader, HTTP_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            LOGGER.debug("Cannot parse 'If-Modified-Since' HTTP header value '{}'.", ifModifiedSinceHeader, e);
            return null;
        }
    }

    private static String getContentType(HttpRequest request, Path resource) {
        String extensionFromUri = FilenameUtils.getExtension(request.getUriWithoutContextPath());
        Optional<String> contentType = MimeMapper.getMimeType(extensionFromUri);
        if (contentType.isPresent()) {
            return contentType.get();
        }

        String extensionFromPath = PathUtils.getExtension(resource);
        return MimeMapper.getMimeType(extensionFromPath).orElse(CONTENT_TYPE_WILDCARD);
    }

    private static Path selectPath(List<String> parentDirectories, String... relativeFilePath) {
        Path path = null;
        for (String parentDirectory : parentDirectories) {
            path = Paths.get(parentDirectory, relativeFilePath);
            if (Files.exists(path)) {
                return path;
            }
        }
        return path;
    }
}
