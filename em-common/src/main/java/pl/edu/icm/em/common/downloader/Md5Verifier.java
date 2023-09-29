package pl.edu.icm.em.common.downloader;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.config.WorkDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Verifier implements SocStructFileVerifier {
    private final WorkDir workDir;

    @WithFactory
    public Md5Verifier(WorkDir workDir) {
        this.workDir = workDir;
    }

    @Override
    public boolean isOk(DownloadableFile downloadableFile) throws IOException {
        File file = new File(downloadableFile.getFilename());
        return workDir.exists(file) && computeMd5Sum(workDir.openForReading(file)).compareToIgnoreCase(downloadableFile.getMd5()) == 0;
    }

    private static String computeMd5Sum(InputStream is) throws IOException {
        int buffer = 1 << 16; // 2^16 == 65536 
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try (DigestInputStream dis = new DigestInputStream(is, md)) {
            byte[] data = new byte[buffer];
            while (true) {
                if (dis.read(data) == -1) break;
            }
        }
        byte[] digested = md.digest();
        return bytesToHex(digested);
    }

    // from https://stackoverflow.com/a/9855338
    private static String bytesToHex(byte[] bytes) {
        byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
