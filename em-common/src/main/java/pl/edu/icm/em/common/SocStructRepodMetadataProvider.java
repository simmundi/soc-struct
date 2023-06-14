package pl.edu.icm.em.common;

import net.snowyhollows.bento.annotation.WithFactory;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

public class SocStructRepodMetadataProvider implements SocStructMetadataProvider {

    public static String crcUrl = "https://repod.icm.edu.pl/api/access/datafile/16831";
    public static String orcUrl = "https://repod.icm.edu.pl/api/access/datafile/16832";

    @WithFactory
    public SocStructRepodMetadataProvider() {
    }

    @Override
    public String getFilename(String urlString, HttpURLConnection connection) {
        return ofNullable(connection)
                .flatMap(c ->
                        ofNullable(c.getHeaderField("Content-Disposition"))
                                .map(h -> h.split(";"))
                                .flatMap(sp ->
                                        stream(sp)
                                                .filter(s -> s.strip().startsWith("filename="))
                                                .findFirst()
                                                .map(s -> s.split("=")[1])
                                )
                ).orElse(urlString.substring(urlString.lastIndexOf("/") + 1)).strip();
    }

    @Override
    public URL getCrcUrl() throws MalformedURLException {
        return new URL(crcUrl);
    }

    @Override
    public URL getOrcUrl() throws MalformedURLException {
        return new URL(orcUrl);
    }
}
