package pl.edu.icm.em.common.downloader;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.util.AbstractCategory;

import java.net.MalformedURLException;
import java.net.URL;

public class DownloadableFile extends AbstractCategory {
    private final URL url;
    private final String filename;
    private final String md5;

    @WithFactory
    public DownloadableFile(String name, int ordinal, String url, String filename, String md5) {
        super(name, ordinal);
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.filename = filename;
        this.md5 = md5;
    }

    public URL getUrl() {
        return url;
    }

    public String getFilename() {
        return filename;
    }

    public String getMd5() {
        return md5;
    }
}
