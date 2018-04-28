package eppic.commons.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestFileWalk {

    @Test
    public void testWalk() throws Exception {
        String tmpDirPath = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpDirPath, "testfilewalk");
        tmpDir.mkdir();
        File subDir = new File(tmpDir, "subdir");
        subDir.mkdir();
        File file = new File(subDir, "testfile");
        file.createNewFile();
        File symLink = new File(subDir, "symlink");
        Files.createSymbolicLink(symLink.toPath(), file.toPath());

        assertTrue(tmpDir.isDirectory());
        assertTrue(subDir.isDirectory());
        assertTrue(file.exists());
        assertTrue(file.isFile());

        Files.walk(tmpDir.toPath())
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .forEach(File::delete);

        Files.walk(tmpDir.toPath())
                .filter(Files::isDirectory)
                .map(Path::toFile)
                .forEach(File::delete);

        assertFalse(file.exists());
        assertFalse (subDir.exists());
        assertTrue(tmpDir.exists());

        tmpDir.delete();
        assertFalse(tmpDir.exists());
    }
}
