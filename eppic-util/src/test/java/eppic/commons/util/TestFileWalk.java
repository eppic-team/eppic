package eppic.commons.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class TestFileWalk {

    @Test
    public void testWalk() throws Exception {
        String tmpDirPath = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpDirPath, "testfilewalk");
        tmpDir.mkdir();
        File subDir = new File(tmpDir, "subdir");
        subDir.mkdir();
        File subsubdir = new File(subDir, "subsubdir");
        subsubdir.mkdir();
        File file = new File(subDir, "testfile");
        file.createNewFile();
        File symLink = new File(subDir, "symlink");
        Files.createSymbolicLink(symLink.toPath(), file.toPath());

        assertTrue(tmpDir.isDirectory());
        assertTrue(subDir.isDirectory());
        assertTrue(subsubdir.isDirectory());
        assertTrue(file.exists());
        assertTrue(file.isFile());

        Files.walk(tmpDir.toPath())
                .sorted(Comparator.reverseOrder()) // important: so that the deepest dirs appear first in deletion
                .map(Path::toFile)
                .forEach(File::delete);

        assertFalse(file.exists());
        assertFalse(subDir.exists());
        assertFalse(subsubdir.exists());
        assertFalse(tmpDir.exists());
    }
}
