package nl.tno.torxakis.wxsgenerator;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WxsGeneratorMain {
    final private static String WXS_FILE_NAME = "TorXakis.wxs";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: WxsGenerator <version> <config file path>");
            return;
        }
        String version = args[0];
        System.out.println("version = " + version);
        String configPath = args[1];
        System.out.println("configPath = " + configPath);

        final String tagFolderPathString = ".\\v" + version;
        WxsConfig config = new WxsConfig(configPath, tagFolderPathString);
        if (config.isInvalid()) {
            System.out.println(String.format("Config file %s is invalid.", configPath));
            System.out.println("- " + String.join(System.lineSeparator() + "- ", config.getErrors()));
            return;
        }

        System.out.println("WxsGeneratorMain launched with:");
        String z3Url = config.getZ3Url();
        System.out.println("z3Url = " + z3Url);
        String cvc4Url = config.getCvc4Url();
        System.out.println("cvc4Url = " + cvc4Url);
        String eclipsePluginUrl = config.getEclipsePluginUrl();
        System.out.println("eclipsePluginUrl= " + eclipsePluginUrl);

        final String torxakisFolder = tagFolderPathString + "\\" + config.getTorxakisFolder();
        System.out.println("torxakisFolder = " + torxakisFolder);
        final String nppFolder = tagFolderPathString + "\\" + config.getNppFolder();
        System.out.println("nppFolder = " + nppFolder);

        final String examplesPath = torxakisFolder + "\\examps";
        final String editorPluginsFolderPath = ".\\EditorPlugins";

        System.out.println("Ensuring WindowsInstaller directory...");
        File winInstallerDirectory = new File(tagFolderPathString + "\\WindowsInstaller");
        if (!winInstallerDirectory.exists()) {
            winInstallerDirectory.mkdir();
        }
        final String winInstallerFolderPath = winInstallerDirectory.getPath();
        final String editorPluginsTargetPath = winInstallerFolderPath + "\\EditorPlugins";

        try {
            String z3zipFileName = getFileNameFromUrl(z3Url);
            File z3zipFile = new File(tagFolderPathString, z3zipFileName);
            if (z3zipFile.exists()) {
                System.out.println("z3 file already exists:" + z3zipFileName);
            } else {
                System.out.println("Downloading z3 from:" + z3Url);
                download(z3Url, tagFolderPathString);
            }
            String z3UnzipFolderPath = Paths.get(tagFolderPathString, z3zipFileName.substring(0, z3zipFileName.length() - 4)).toString();
            File z3UnzipFolder = new File(z3UnzipFolderPath);
            if (z3UnzipFolder.exists()) {
                deleteDirectory(z3UnzipFolder);
            }
            System.out.println(String.format("Unzipping %s to %s", z3zipFileName, tagFolderPathString));    //Here we use the fact that Z3 adds a directory with the name (without extension) of the zip file
            unzip(z3zipFile.getPath(), tagFolderPathString);

            String cvc4FileName = getFileNameFromUrl(cvc4Url);
            Path cvc4FolderPath = Paths.get(tagFolderPathString, cvc4FileName.substring(0, cvc4FileName.length() - 4));
            File cvc4File = new File(cvc4FolderPath.toString(), "cvc4.exe");
            if (cvc4File.exists()) {
                System.out.println("cvc4 file already exists:" + cvc4FileName);
            } else {
                System.out.println("Downloading cvc4 from:" + cvc4Url);
                Path cvc4exeFilePath = download(cvc4Url, tagFolderPathString);
                File cvc4Folder = new File(cvc4FolderPath.toString());
                if (!cvc4Folder.exists()) {
                    cvc4Folder.mkdir();
                }
                Files.move(cvc4exeFilePath, Paths.get(cvc4FolderPath.toString(), "cvc4.exe"));
            }

            System.out.println("Downloading Eclipse Plugin from:" + eclipsePluginUrl);
            Path eclipsePluginZipFilePath = download(eclipsePluginUrl, tagFolderPathString);

            System.out.println("prepareEditorPluginsFolder() with:");
            System.out.println("eclipsePluginZipFilePath = " + eclipsePluginZipFilePath);
            System.out.println("nppPluginPath = " + nppFolder);
            System.out.println("editorPluginsFolderPath = " + editorPluginsFolderPath);
            System.out.println("editorPluginsTargetPath = " + editorPluginsTargetPath);
            prepareEditorPluginsFolder(eclipsePluginZipFilePath.toString(), nppFolder, editorPluginsFolderPath, editorPluginsTargetPath);

            System.out.println("generateLicenseRtf() with:");
            System.out.println("tagFolderPathString = " + tagFolderPathString);
            System.out.println("winInstallerFolderPath = " + winInstallerFolderPath);
            generateLicenseRtf(tagFolderPathString, winInstallerFolderPath);

            TDirectory exampsContent = getDirectory(examplesPath);
            TDirectory z3Content = getDirectory(z3UnzipFolderPath);
            TDirectory cvc4Content = getDirectory(cvc4FolderPath.toString());
            TDirectory editorPluginsContent = getDirectory(editorPluginsTargetPath);
            WxsGenerator generator =
                    new WxsGenerator(z3Content, cvc4Content, exampsContent, editorPluginsContent, version, tagFolderPathString);

            String result = generator.generate();

            // Write result to file
            PrintWriter out = new PrintWriter(winInstallerFolderPath + "\\" + WXS_FILE_NAME);
            out.print(result);
            out.close();
            System.out.println("Finished generating installer wxs file");
        } catch (Exception e) {
            System.out.println("ERROR: Could not generate installer wxs file!");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static String getFileNameFromUrl(String url) {
        String[] urlParts = url.split("/");
        return urlParts[urlParts.length - 1];
    }

    private static Path download(String sourceURL, String targetDirectory) throws IOException {
        URL url = new URL(sourceURL);
        String fileName = sourceURL.substring(sourceURL.lastIndexOf('/') + 1, sourceURL.length());
        Path targetPath = new File(targetDirectory + File.separator + fileName).toPath();
        Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath;
    }

    private static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        if (!dir.exists()) dir.mkdirs();
        byte[] buffer = new byte[1024];
        try (FileInputStream fis = new FileInputStream(zipFilePath);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                if (!ze.isDirectory()) {
                    System.out.println("Unzipping to " + newFile.getAbsolutePath());
                    //create directories for sub directories in zip
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateLicenseRtf(String tagFolderPath, String winInstallerPath) throws IOException {
        String txtFileName = tagFolderPath + "\\torxakis\\license";
        String rtfFileName = winInstallerPath + "\\license.rtf";

        List<String> txtLines = Files.readAllLines(Paths.get(txtFileName));
        FileWriter fw = new FileWriter(rtfFileName);

        // Write header
        fw.write("{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1033{\\fonttbl{\\f0\\fnil\\fcharset0 Calibri;}}{\\colortbl ;\\red0\\green0\\blue255;}{\\*\\generator Msftedit 5.41.21.2512;}\\viewkind4\\uc1\\pard\\sl240\\slmult1\\lang9\\f0\\fs22 ");

        for (String txtLine : txtLines) {
            fw.write(txtLine + "\\par\n");
        }
        fw.write("\0");
        fw.close();
    }

    private static void prepareEditorPluginsFolder(String eclipsePluginZipFilePath, String nppPluginPath, String editorPluginsFolderPath, String editorPluginsTargetPath) throws IOException {
        deleteDirectory(new File(editorPluginsTargetPath));
        copyFolders(Paths.get(editorPluginsFolderPath), Paths.get(editorPluginsTargetPath));

        // Eclipse
        Files.move(Paths.get(eclipsePluginZipFilePath), Paths.get(editorPluginsTargetPath + "\\Eclipse\\EclipsePlugin.zip"));

        // Notepad++
        String nppTargetPath = editorPluginsTargetPath + "\\Notepad++";
        copyFolders(Paths.get(nppPluginPath), Paths.get(nppTargetPath));
        deleteDirectory(new File(nppTargetPath + "\\.git"));
    }

    private static void copyFolders(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attributes) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static boolean deleteDirectory(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return false;
        }

        String[] files = dir.list();
        for (int i = 0, len = files.length; i < len; i++) {
            File f = new File(dir, files[i]);
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                f.delete();
            }
        }
        return dir.delete();
    }

    private static TDirectory getDirectory(String rootFolderName) {
        TDirectory root = new TDirectory(rootFolderName);
        visitAllDirsAndFiles(new File(rootFolderName), root);

        return root.getDirectories().get(0);
    }

    private static void visitAllDirsAndFiles(File dir, TDirectory directory) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            TDirectory childDirectory = new TDirectory(dir.toString());
            directory.getDirectories().add(childDirectory);
            for (int i = 0; i < children.length; i++) {
                visitAllDirsAndFiles(new File(dir, children[i]), childDirectory);
            }
        } else {
            if (dir.isFile()) {
                directory.getFiles().add(new TFile(dir.toString()));
            }
        }
    }
}
