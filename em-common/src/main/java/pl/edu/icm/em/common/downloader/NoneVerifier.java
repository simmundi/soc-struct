package pl.edu.icm.em.common.downloader;

import net.snowyhollows.bento.annotation.WithFactory;

public class NoneVerifier implements SocStructFileVerifier {
    @WithFactory
    public NoneVerifier() {
    }

    @Override
    public boolean isOk(DownloadableFile file) {
        return false;
    }
}
