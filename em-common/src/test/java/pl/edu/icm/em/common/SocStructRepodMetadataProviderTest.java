package pl.edu.icm.em.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SocStructRepodMetadataProviderTest {
    SocStructRepodMetadataProvider socStructRepodMetadataProvider = new SocStructRepodMetadataProvider();
    @Mock
    HttpURLConnection httpURLConnection;

    @Test
    @DisplayName("should get name from URL without connection")
    void getFilenameWithoutConnection() {
        assertThat(socStructRepodMetadataProvider.getFilename("http://www.icm.edu.pl/test_filename.orc"))
                .isEqualTo("test_filename.orc");
    }

    @Test
    @DisplayName("should get name from URL if response header does not contain filename")
    void getFilenameDifferentConnection() {
        assertThat(socStructRepodMetadataProvider.getFilename("http://www.icm.edu.pl/test_filename.orc",
                httpURLConnection))
                .isEqualTo("test_filename.orc");
    }

    @Test
    @DisplayName("should get name from URL if response header does not contain filename")
    void getFilenameCorrectConnection() {
        when(httpURLConnection.getHeaderField("Content-Disposition")).thenReturn(
                "filename=filename_from_header.orc;"
        );
        assertThat(socStructRepodMetadataProvider.getFilename("http://www.icm.edu.pl/test_filename.orc",
                httpURLConnection))
                .isEqualTo("filename_from_header.orc");
    }

    @Test
    void getCrcUrl() throws MalformedURLException {
        assertThat(socStructRepodMetadataProvider.getCrcUrl().toString()).isEqualTo(SocStructRepodMetadataProvider.crcUrl);
    }

    @Test
    void getOrcUrl() throws MalformedURLException {
        assertThat(socStructRepodMetadataProvider.getOrcUrl().toString()).isEqualTo(SocStructRepodMetadataProvider.orcUrl);
    }
}