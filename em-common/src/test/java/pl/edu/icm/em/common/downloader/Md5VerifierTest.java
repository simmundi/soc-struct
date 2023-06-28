package pl.edu.icm.em.common.downloader;

import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Md5VerifierTest {
    private final String md5 = "DB163B8E7BF80041BA10A469E783F107";
    @Mock
    DownloadableFile downloadableFile;
    @Mock
    WorkDir workDir;

    @BeforeEach
    void setup() {
        when(workDir.exists(any())).thenReturn(true);
        when(workDir.openForReading(any())).thenReturn(getClass().getResourceAsStream("/md5file"));
    }

    @Test
    @DisplayName("should be ok")
    void isOk() throws IOException {
        when(downloadableFile.getFilename()).thenReturn("md5file");
        when(downloadableFile.getMd5()).thenReturn(md5);
        var verifier = new Md5Verifier(workDir);
        assertThat(verifier.isOk(downloadableFile)).isTrue();
    }

    @Test
    @DisplayName("should be case insensitive")
    void isOkLower() throws IOException {
        when(downloadableFile.getFilename()).thenReturn("md5file");
        when(downloadableFile.getMd5()).thenReturn(md5.toLowerCase());
        var verifier = new Md5Verifier(workDir);
        assertThat(verifier.isOk(downloadableFile)).isTrue();
    }

    @Test
    @DisplayName("should not be ok")
    void isNotOkLower() throws IOException {
        when(downloadableFile.getFilename()).thenReturn("md5file");
        when(downloadableFile.getMd5()).thenReturn("a4b61ff");
        var verifier = new Md5Verifier(workDir);
        assertThat(verifier.isOk(downloadableFile)).isFalse();
    }
}