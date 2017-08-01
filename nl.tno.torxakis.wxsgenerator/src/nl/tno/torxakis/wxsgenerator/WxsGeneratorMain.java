package nl.tno.torxakis.wxsgenerator;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WxsGeneratorMain {
    final static String WXS_FILE_NAME = "TorXakis.wxs";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: WxsGenerator <config file path>");
            return;
        }
        String configPath = args[0];
        System.out.println("configPath = " + configPath);

        WxsConfig config = new WxsConfig(configPath);
        if (config.isInvalid()){
            System.out.println(String.format("Config file %s is invalid.", configPath));
            System.out.println("- " + String.join(System.lineSeparator() + "- ", config.getErrors()));
            return;
        }

        System.out.println("WxsGeneratorMain launched with:");
        System.out.println("z3Folder = " + config.getZ3Folder());
        System.out.println("cvc4Folder = " + config.getCvc4Folder());
        System.out.println("version = " + config.getVersion());
        System.out.println("torxakisFolder = " + config.getTorxakisFolder());
        System.out.println("eclipseFolder= " + config.getEclipseFolder());
        System.out.println("nppFolder = " + config.getNppFolder());

        final String tagFolderPath = ".\\v" + config.getVersion();
        final String examplesPath = config.getTorxakisFolder() + "\\examps";
        final String eclipsePluginPath = config.getEclipseFolder() + "\\nl.tno.torxakis.language.update-site";
        final String editorPluginsFolderPath = ".\\EditorPlugins";

        System.out.println("Ensuring WindowsInstaller directory...");
        File winInstallerDirectory = new File(tagFolderPath + "\\WindowsInstaller");
        if (!winInstallerDirectory.exists()) {
            winInstallerDirectory.mkdir();
        }
        final String winInstallerFolderPath = winInstallerDirectory.getPath();
        final String editorPluginsTargetPath = winInstallerFolderPath + "\\EditorPlugins";

        try {
            System.out.println("prepareEditorPluginsFolder() with:");
            System.out.println("eclipsePluginPath = " + eclipsePluginPath);
            System.out.println("nppPluginPath = " + config.getNppFolder());
            System.out.println("editorPluginsFolderPath = " + editorPluginsFolderPath);
            System.out.println("editorPluginsTargetPath = " + editorPluginsTargetPath);
            prepareEditorPluginsFolder(eclipsePluginPath, config.getNppFolder(), editorPluginsFolderPath, editorPluginsTargetPath);

            System.out.println("generateLicenseRtf() with:");
            System.out.println("tagFolderPath = " + tagFolderPath);
            System.out.println("winInstallerFolderPath = " + winInstallerFolderPath);
            generateLicenseRtf(tagFolderPath, winInstallerFolderPath);

            TDirectory exampsContent = getDirectory(examplesPath);
            TDirectory z3Content = getDirectory(config.getZ3Folder());
            TDirectory cvc4Content = getDirectory(config.getCvc4Folder());
            TDirectory editorPluginsContent = getDirectory(editorPluginsTargetPath);
            WxsGenerator generator =
                    new WxsGenerator(z3Content, cvc4Content, exampsContent, editorPluginsContent, config.getVersion(), tagFolderPath);

            String result = generator.generate();

            // Write result to file
            PrintWriter out = new PrintWriter(winInstallerFolderPath + "\\" + WXS_FILE_NAME);
            out.print(result);
            out.close();
            System.out.println("Finished generating installer wxs file");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("ERROR: Could not generate installer wxs file!");
            e.printStackTrace();
            System.exit(-1);
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

    private static void prepareEditorPluginsFolder(String eclipsePluginPath, String nppPluginPath, String editorPluginsFolderPath, String editorPluginsTargetPath) throws IOException {
        copyFolders(Paths.get(editorPluginsFolderPath), Paths.get(editorPluginsTargetPath));

        // Eclipse
        createZipFileFromDirectory(eclipsePluginPath, editorPluginsTargetPath + "\\Eclipse");

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

    public static boolean deleteDirectory(File dir) {
        if(! dir.exists() || !dir.isDirectory())    {
            return false;
        }

        String[] files = dir.list();
        for(int i = 0, len = files.length; i < len; i++)    {
            File f = new File(dir, files[i]);
            if(f.isDirectory()) {
                deleteDirectory(f);
            }else   {
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

    public static void visitAllDirsAndFiles(File dir, TDirectory directory) {
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

    public static void createZipFileFromDirectory(String srcDir, String eclipsePluginTargetFolderName) {
        String zipFile = eclipsePluginTargetFolderName + "\\EclipsePlugin.zip";

        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            File dir = new File(srcDir);

            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    zipDirectory(srcDir, files[i], zos);
                } else {
                    zipFile(srcDir, files[i], zos);
                }
            }

            // close the ZipOutputStream
            zos.close();
        } catch (IOException ioe) {
            System.out.println("Error creating zip file" + ioe);
        }

    }

    private static void zipFile(String root, File file, ZipOutputStream zos) throws IOException {
        int length;

        // create byte buffer
        byte[] buffer = new byte[1024];

        FileInputStream fis = new FileInputStream(file);

        // begin writing a new ZIP entry, positions the stream to the start of the entry data
        zos.putNextEntry(new ZipEntry(file.getAbsolutePath().replace(root + "\\", "")));

        while ((length = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, length);
        }
        zos.closeEntry();
        fis.close();
    }

    private static void zipDirectory(String root, File srcDir, ZipOutputStream zos) throws IOException {
        // begin writing a new ZIP entry, positions the stream to the start of the entry data
        zos.putNextEntry(new ZipEntry(srcDir.getName() + "\\"));
        zos.closeEntry();

        try {
            File dir = new File(srcDir.getAbsolutePath());

            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    zipDirectory(root, files[i], zos);
                } else {
                    zipFile(root, files[i], zos);
                }
            }
        } catch (IOException ioe) {
            System.out.println("Error creating zip file" + ioe);
        }
    }
}
