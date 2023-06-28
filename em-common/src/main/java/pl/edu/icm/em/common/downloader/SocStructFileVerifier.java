package pl.edu.icm.em.common.downloader;

import net.snowyhollows.bento.annotation.ImplementationSwitch;

import java.io.IOException;

@ImplementationSwitch(
        configKey = "pdyn2.io.downloads.verificationType",
        cases = {
                @ImplementationSwitch.When(name = "md5", implementation = Md5Verifier.class, useByDefault = true),
                @ImplementationSwitch.When(name = "none", implementation = NoneVerifier.class),
        }
)
public interface SocStructFileVerifier {
    boolean isOk(DownloadableFile file) throws IOException;
}
