package nl.tno.torxakis.wxsgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WxsGeneratorMain {
    final static String WXS_FILE_NAME = "TorXakis.wxs";

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Usage: WxsGenerator <z3_folder> <CVC4_folder> <version> <torxakis_folder> <eclipse_plugin_folder> <npp_plugin_folder>");
            return;
        }

        String z3Folder = args[0];
        String cvc4Folder = args[1];
        String version = args[2];
        String torxakisFolderName = args[3];
        String eclipseFolderName = args[4];
        String nppFolderName = args[5];

        System.out.println("WxsGeneratorMain launched with:");
        System.out.println("z3Folder = " + z3Folder);
        System.out.println("cvc4Folder = " + cvc4Folder);
        System.out.println("version = " + version);
        System.out.println("torxakisFolderName = " + torxakisFolderName);
        System.out.println("eclipseFolderName= " + eclipseFolderName);
        System.out.println("nppFolderName = " + nppFolderName);

        final String tagFolderPath = ".\\v" + version;
        final String torxakisFolderPath = tagFolderPath + "\\" + torxakisFolderName;
        final String examplesPath = torxakisFolderPath + "\\examps";
        final String eclipsePluginPath = tagFolderPath + "\\" + eclipseFolderName + "\\nl.tno.torxakis.language.update-site";
        final String nppPluginPathName = tagFolderPath + "\\" + nppFolderName;
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
            System.out.println("tagFolderPath = " + tagFolderPath);
            prepareEditorPluginsFolder(eclipsePluginPath, editorPluginsFolderPath, editorPluginsTargetPath);

            System.out.println("generateLicenseRtf() with:");
            System.out.println("tagFolderPath = " + tagFolderPath);
            System.out.println("winInstallerFolderPath = " + winInstallerFolderPath);
            generateLicenseRtf(tagFolderPath, winInstallerFolderPath);

            TDirectory exampsContent = getDirectory(examplesPath);
            TDirectory z3Content = getDirectory(z3Folder);
            TDirectory cvc4Content = getDirectory(cvc4Folder);
            TDirectory editorPluginsContent = getDirectory(editorPluginsTargetPath);
            WxsGenerator generator =
                    new WxsGenerator(z3Content, cvc4Content, exampsContent, editorPluginsContent, version, tagFolderPath);

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

    private static void prepareEditorPluginsFolder(String eclipsePluginPath, String editorPluginsFolderPath, String editorPluginsTargetPath) throws IOException {
        copyFolders(Paths.get(editorPluginsFolderPath), Paths.get(editorPluginsTargetPath));

        // Eclipse
        String eclipsePluginTargetFolderPath = editorPluginsTargetPath + "\\Eclipse";
        createZipFileFromDirectory(eclipsePluginPath, eclipsePluginTargetFolderPath);

        // Notepad++ - disabled until we have proper .pdf
//        copyNotepadPP(branchFolderName, editorPluginsFolderName + "\\Notepad++");
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

//    private static void copyNotepadPP(String branchFolderName, String notepadFolder) {
//        InputStream inStream = null;
//        OutputStream outStream = null;
//
//        try {
//            String sourceFileName = branchFolderName + "\\Notepad++\\TorXakis.xml";
//            String destinationFileName = notepadFolder + "\\TorXakis.xml";
//
//            File source = new File(sourceFileName);
//            File destination = new File(destinationFileName);
//
//            inStream = new FileInputStream(source);
//            outStream = new FileOutputStream(destination);
//
//            byte[] buffer = new byte[1024];
//            int length;
//
//            //copy the file content in bytes
//            while ((length = inStream.read(buffer)) > 0) {
//
//                outStream.write(buffer, 0, length);
//
//            }
//
//            inStream.close();
//            outStream.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private static TDirectory getDirectory(String rootFolderName) {
        TDirectory root = new TDirectory(rootFolderName);
        visitAllDirsAndFiles(new File(rootFolderName), root);

        return root.getDirectories().get(0);
    }

    public static void visitAllDirsAndFiles(File dir, TDirectory directory) {
        if (dir.isDirectory()) {
//			System.out.println("Directory " + dir);
            String[] children = dir.list();
            TDirectory childDirectory = new TDirectory(dir.toString());
            directory.getDirectories().add(childDirectory);
            for (int i = 0; i < children.length; i++) {
                visitAllDirsAndFiles(new File(dir, children[i]), childDirectory);
            }
        } else {
            if (dir.isFile()) {
                directory.getFiles().add(new TFile(dir.toString()));
//			    System.out.println("File " + dir);
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
