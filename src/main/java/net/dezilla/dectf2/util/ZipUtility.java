package net.dezilla.dectf2.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.dezilla.dectf2.Util;

public class ZipUtility {

	public static void unzip(File zip, File path) throws IOException {
        if(!path.exists())
            path.mkdirs();
        byte[] buffer = new byte[1024];
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry entry;
            while((entry = in.getNextEntry()) != null) {
                File file = new File(path, entry.getName().replace('\\', '/'));
                if(entry.isDirectory())
                    file.mkdirs();
                else {
                    file.getParentFile().mkdirs();
                    try (OutputStream out = new FileOutputStream(file)) {
                        int length;
                        while((length = in.read(buffer)) != -1)
                            out.write(buffer, 0, length);
                    }
                }
            }
            in.closeEntry();
        }
    }

    public static void unzipMap(File zip, File path) throws IOException {
        unzip(zip, path);
		new File(path, "uploaded").delete();
        Util.deleteFolder(new File(path, "players"));
		File uid = new File(path, "uid.dat");
		if (uid.exists())
			uid.delete();
    }

    public static final void zip(File path, File zip) {
        zip = zip.getAbsoluteFile();
        zip.getParentFile().mkdirs();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
            zipHelper(path, path, zos);
        } catch (IOException e) {
            zip.delete();
        }
    }

    private static final void zipHelper(File path, File base, ZipOutputStream zos) throws IOException {
        byte[] buffer = new byte[1024];
        for(File file : path.listFiles())
            if(file.isDirectory())
                zipHelper(file, base, zos);
            else
                try (FileInputStream in = new FileInputStream(file)) {
                    ZipEntry entry = new ZipEntry(file.getPath().replace('\\', '/').substring(base.getPath().length() + 1));
                    zos.putNextEntry(entry);
                    int length;
                    while((length = in.read(buffer)) != -1)
                        zos.write(buffer, 0, length);
                }
    }
	
}
