/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.em.common;


import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.trurl.util.Status;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static java.lang.Math.max;
import static java.util.Arrays.stream;

public class SocStructDownloader {
    private static final int BUFFER_SIZE = 4096;
    private final WorkDir workDir;
    private final SocStructMetadataProvider socstructMetadataProvider;

    @WithFactory
    public SocStructDownloader(WorkDir workDir, SocStructMetadataProvider socstructMetadataProvider) {
        this.workDir = workDir;
        this.socstructMetadataProvider = socstructMetadataProvider;
    }

    public static void main(String[] args) throws IOException {
        var config = EmConfig.configurer(args).getConfig();
        config.get(SocStructDownloaderFactory.IT).maybeDownloadFiles();
    }

    public void maybeDownloadFiles() throws IOException {
        download(socstructMetadataProvider.getCrcUrl());
        download(socstructMetadataProvider.getOrcUrl());
    }

    public String fetchPopulationFilename() throws IOException {
        return withConnection(socstructMetadataProvider.getOrcUrl(), (connection) -> {
            try {
                return socstructMetadataProvider.getFilename(socstructMetadataProvider.getOrcUrl().toString(), connection);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).orElseThrow();
    }

    private <T> Optional<T> withConnection(URL fileUrl, Function<HttpURLConnection, T> connectionTFunction) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("File download failed. Server returned HTTP response code: " + responseCode);
        }
        T result = connectionTFunction.apply(connection);
        connection.disconnect();
        return Optional.ofNullable(result);
    }

    private void download(URL fileUrl) throws IOException {
        withConnection(fileUrl, (connection) -> {
                    var filename = socstructMetadataProvider.getFilename(fileUrl.toString(), connection);
                    int contentLength = connection.getContentLength();
                    var outputFile = new File("input/" + filename);

                    File[] files;
                    try {
                        files = Optional.ofNullable(
                                workDir.listFiles(outputFile.getParentFile(), (f) -> f.getName().equals(filename))
                        ).orElseThrow(() -> new FileNotFoundException(outputFile.getParentFile().getAbsolutePath() + " does not exist"));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    var existingFile = stream(files).findFirst();
                    AtomicBoolean download = new AtomicBoolean(true);
                    existingFile.ifPresent(f -> {
                        if (f.length() == contentLength) {
                            System.out.println(f.getAbsolutePath() + " already exists and file size matches, skipping");
                            download.set(false);
                        } else {
                            System.out.println(f.getAbsolutePath() + " already exists, but file size does not match, re downloading");
                        }
                    });

                    if (download.get()) {
                        try (
                                var bufferedInputStream = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
                                var outputStream = workDir.openForWriting(outputFile)
                        ) {
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int bytesRead;
                            var status = Status.of("\nDownloading file: " + filename + ", from: " + fileUrl, max(1, contentLength / (10 * BUFFER_SIZE)));
                            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                                status.tick();
                            }
                            status.done();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                }
        );
    }

}
