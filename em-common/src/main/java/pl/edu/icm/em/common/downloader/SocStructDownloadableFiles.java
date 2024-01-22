package pl.edu.icm.em.common.downloader;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.category.CategoryManager;

import java.io.IOException;

public class SocStructDownloadableFiles extends CategoryManager<DownloadableFile> {
    public final DownloadableFile POPULATION_ORC;
    public final DownloadableFile POPULATION_CRC;
    private final SocStructFileVerifier socstructFileVerifier;

    @WithFactory
    public SocStructDownloadableFiles(Bento bento, SocStructFileVerifier socstructFileVerifier) {
        super(bento, "pdyn2.io.downloads", DownloadableFileFactory.IT);
        this.socstructFileVerifier = socstructFileVerifier;
        POPULATION_ORC = this.getByName("POPULATION_ORC");
        POPULATION_CRC = this.getByName("POPULATION_ORC_CRC");
    }

    public boolean isOk(DownloadableFile file) throws IOException {
        return socstructFileVerifier.isOk(file);
    }

    @Override
    public DownloadableFile[] emptyArray() {
        return new DownloadableFile[0];
    }

}
