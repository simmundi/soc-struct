package pl.edu.icm.em.common.downloader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class NoneVerifierTest {
    @Mock
    DownloadableFile downloadableFile;

    @Test
    void isOk() {
        var verifier = new NoneVerifier();
        assertThat(verifier.isOk(downloadableFile)).isFalse();
    }
}