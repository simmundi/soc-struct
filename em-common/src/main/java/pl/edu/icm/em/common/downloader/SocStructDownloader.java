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

package pl.edu.icm.em.common.downloader;


import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.em.common.EmConfig;
import pl.edu.icm.trurl.util.Status;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Math.max;

public class SocStructDownloader {
    private static final int BUFFER_SIZE = 4096;
    private final WorkDir workDir;
    private final SocStructDownloadableFiles socStructDownloadableFiles;


    @WithFactory
    public SocStructDownloader(WorkDir workDir, SocStructDownloadableFiles socStructDownloadableFiles) {
        this.workDir = workDir;
        this.socStructDownloadableFiles = socStructDownloadableFiles;
    }

    public static void main(String[] args) throws IOException {
        var config = EmConfig.configurer(args).getConfig();
        config.get(SocStructDownloaderFactory.IT).maybeDownloadFiles();
    }

    public void maybeDownloadFiles() throws IOException {
        download(socStructDownloadableFiles.POPULATION_CRC);
        download(socStructDownloadableFiles.POPULATION_ORC);
    }
    @FunctionalInterface
    interface ThrowingConsumer<T> {
        void accept(T t) throws IOException;
    }

    private void withConnection(URL url, ThrowingConsumer<HttpURLConnection> connectionConsumer) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK)
            throw new IOException("File download failed. Server returned HTTP response code: " + responseCode);
        connectionConsumer.accept(connection);
        connection.disconnect();
    }

    private void download(DownloadableFile file) throws IOException {
        URL url = file.getUrl();
        withConnection(url, (connection) -> {
                    var filename = file.getFilename();
                    int contentLength = connection.getContentLength();
                    var outputFile = new File(filename);
                    boolean download = !socStructDownloadableFiles.isOk(file);
                    if (download) {
                        try (
                                var bufferedInputStream = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
                                var outputStream = workDir.openForWriting(outputFile)
                        ) {
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int bytesRead;
                            var status = Status.of("\nDownloading file: " + filename + ", from: " + url, max(1, contentLength / (10 * BUFFER_SIZE)));
                            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                                status.tick();
                            }
                            status.done();
                        }
                    } else {
                        System.out.println(filename + " already exists and passed verification, skipping");
                    }
                }
        );
    }

}
