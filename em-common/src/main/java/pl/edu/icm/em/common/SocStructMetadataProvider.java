package pl.edu.icm.em.common;

import net.snowyhollows.bento.annotation.ImplementationSwitch;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@ImplementationSwitch(configKey = "socStructURLSource", cases = {
        @ImplementationSwitch.When(name = "repod", implementation = SocStructRepodMetadataProvider.class, useByDefault = true)
})
public interface SocStructMetadataProvider {
    URL getCrcUrl() throws MalformedURLException;

    URL getOrcUrl() throws MalformedURLException;

    String getFilename(String urlString, HttpURLConnection connection);

    default String getFilename(String urlString) {
        return getFilename(urlString, null);
    }
}
