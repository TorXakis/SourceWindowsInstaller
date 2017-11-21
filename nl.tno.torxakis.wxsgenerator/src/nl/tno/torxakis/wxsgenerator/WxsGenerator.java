package nl.tno.torxakis.wxsgenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

class WxsGenerator {
    private final static String COMPONENT_PREFIX = "Component";
    private final static String FILE_PREFIX = "File";
    private final static String FEATURE_PREFIX = "Feature";
    private final static String DIRECTORY_PREFIX = "Directory";
    private static final String ENVIRONMENT_PREFIX = "Environment";

    private int directoryId = 0;
    private int componentId = 0;
    private int fileId = 0;
    private int featureId = 0;
    private int environmentId = 0;

    private List<String> pathDirectories = new ArrayList<>();
    private List<String> addToPath = new ArrayList<>();

    private List<FeatureInfo> featureInfos = new ArrayList<>();
    private FeatureInfo rootFi;
    private String versionNumber = "";
    private String tagFolderPath = "";
    private TDirectory z3Directory = null;
    private TDirectory cvc4Directory = null;
    private TDirectory exampsDirectory = null;
    private TDirectory editorPluginsDirectory = null;

    WxsGenerator(
            TDirectory z3Directory,
            TDirectory cvc4Directory,
            TDirectory exampsDirectory,
            TDirectory editorPluginsDirectory,
            String versionNumber,
            String tagFolderPath) {
        this.z3Directory = z3Directory;
        this.cvc4Directory = cvc4Directory;
        this.exampsDirectory = exampsDirectory;
        this.editorPluginsDirectory = editorPluginsDirectory;
        this.versionNumber = versionNumber;
        this.tagFolderPath = tagFolderPath;
    }

    String generate() {
        String out = "";
        pathDirectories.add(z3Directory.getName() + "\\bin");
        pathDirectories.add(cvc4Directory.getName());

        rootFi = new FeatureInfo(null, getFeatureId(), "TorXakis", "The complete package", "expand", 1, "INSTALLDIR", false);
        featureInfos.add(rootFi);


        out += "<?xml version='1.0' encoding='windows-1252'?>\n";
        out += "<Wix xmlns='http://schemas.microsoft.com/wix/2006/wi'>\n";
        out += generateProduct("\t");

        out += "</Wix>";

        return out;
    }

    private String generateProduct(String indent) {
        String out = "";

        out += indent + "<Product Name='TorXakis' Id='" + java.util.UUID.randomUUID() + "' UpgradeCode='" + java.util.UUID.randomUUID() + "'\n";
        out += indent + "\tLanguage='1033' Codepage='1252' Version='" + versionNumber + "' Manufacturer='torxakis.org'>\n";

        out += generatePackage(indent + "\t");
        out += generateHomePath(indent + "\t");
        out += generateMedia(indent + "\t");
        out += generateTargetDir(indent + "\t");
        out += generateFeatures(indent + "\t");
        out += generateUi(indent + "\t");

        out += indent + "</Product>\n";


        return out;
    }

    private String generatePackage(String indent) {
        String out = "";

        out += indent + "<Package Id='*' Keywords='Installer' Description='TorXakis Installer'\n";
        out += indent + "\tComments='Copyright (c) 2015-" + Calendar.getInstance().get(Calendar.YEAR) + " torxakis.org' Manufacturer='torxakis.org' InstallScope='perMachine'\n";
        out += indent + "\tInstallerVersion='100' Languages='1033' Compressed='yes' SummaryCodepage='1252' />\n";

        return out;
    }

    private String generateMedia(String indent) {
        String out = "";

        out += indent + "<Media Id='1' Cabinet='TorXakis.cab' EmbedCab='yes' DiskPrompt='CD-ROM #1' />\n";
        out += indent + "<Property Id='DiskPrompt' Value='TorXakis Installation [1]' />\n";

        return out;
    }

    private String generateHomePath(String indent) {
        return indent + "<SetDirectory Id='HOMEDIR' Value='[%HOMEDRIVE][%HOMEPATH]' />\n" +
               indent + "<Property Id=\"USERHOMEFOLDER\" >\n" +
               indent + "\t<DirectorySearch Id=\"UserHome\" Path=\"[HOMEDIR]\" ></DirectorySearch>\n" +
               indent + "</Property>\n" +
               indent + "<Condition Message=\"User home directory (%HOMEPATH% and %HOMEDRIVE%) is set to '[%HOMEPATH]' in drive [%HOMEDRIVE], but it is unavailable.\">\n" +
               indent + "\t<![CDATA[USERHOMEFOLDER]]>\n" +
               indent + "</Condition>\n";
    }

    private String generateTargetDir(String indent) {
        String out = "";

        out += indent + "<Directory Id='TARGETDIR' Name='SourceDir'>\n";

        out += generateHomeFolder(indent + "\t");
        out += generateProgramFilesFolder(indent + "\t");
        out += generatePathEnvironmentVariable(indent + "\t");
        out += generatePrograms(indent + "\t");

        out += indent + "</Directory>\n";

        return out;
    }

    private String generateHomeFolder(String indent) {
        String out = "";

        out += indent + "<Directory Id='HOMEDIR' Name='UserHome'>\n";
        out += generateTorxakisConfig(rootFi, indent + "\t");
        out += indent + "</Directory>\n";

        return out;
    }

    private String generateTorxakisConfig(FeatureInfo parent, String indent) {
        FeatureInfo fi = new FeatureInfo(parent, "yamlFeature", "Configuration", "TorXakis config YAML file", 1);
        parent.getChildren().add(fi);
        featureInfos.add(fi);
        String componentId = "yamlComponent";
        fi.getComponentIds().add(componentId);

        return indent + "<Component Id='" + componentId + "' Guid='" + java.util.UUID.randomUUID() + "'>\n" +
                indent + "\t<File Id='yamlFile' Name='.torxakis.yaml' DiskId='1' Source='" + tagFolderPath + "\\torxakis\\.torxakis.yaml'/>\n" +
                indent + "</Component>\n";
    }

    private String generateProgramFilesFolder(String indent) {
        String out = "";
        FeatureInfo fi;

        out += indent + "<Directory Id='ProgramFilesFolder' Name='PFiles'>\n";
        out += indent + "\t<Directory Id='INSTALLDIR' Name='TorXakis'>\n";

        out += generateTorxakisExecutable(rootFi, indent + "\t\t");
        out += generateDocumentation(rootFi, indent + "\t\t");

        fi = new FeatureInfo(rootFi, getFeatureId(), "Z3", "Z3 Problem Solver", 1, false);
        rootFi.getChildren().add(fi);
        out += generateDirectory(indent + "\t\t", z3Directory, fi);
        featureInfos.add(fi);

        fi = new FeatureInfo(rootFi, getFeatureId(), "CVC4", "CVC4 Problem Solver", 1);
        rootFi.getChildren().add(fi);
        out += generateDirectory(indent + "\t\t", cvc4Directory, fi);
        featureInfos.add(fi);

        fi = new FeatureInfo(rootFi, getFeatureId(), "Examples", "TorXakis examples", 1);
        rootFi.getChildren().add(fi);
        out += generateDirectory(indent + "\t\t", exampsDirectory, fi);
        featureInfos.add(fi);

        fi = new FeatureInfo(rootFi, getFeatureId(), "Plug-ins", "Editor Plug-ins", 1);
        rootFi.getChildren().add(fi);
        out += generateDirectory(indent + "\t\t", editorPluginsDirectory, fi);
        featureInfos.add(fi);

        out += indent + "\t</Directory>\n";
        out += indent + "</Directory>\n";

        return out;
    }

    private String generateTorxakisExecutable(FeatureInfo parent, String indent) {
        String out = "";
        FeatureInfo fi = new FeatureInfo(parent, getFeatureId(), "TorXakis", "The main executable", 1, false);
        parent.getChildren().add(fi);
        featureInfos.add(fi);

        String componentId = getComponentId();

        fi.getComponentIds().add(componentId);

        out += indent + "<!-- Torxakis Executable-->\n";
        out += indent + "<Component Id='" + componentId + "' Guid='" + java.util.UUID.randomUUID() + "' KeyPath='yes'>\n";
        out += indent + "\t<File Id='" + getFileId() + "' Name='txsserver.exe' DiskId='1' Source='" + tagFolderPath + "\\torxakis\\bin\\txsserver.exe' />\n";
        out += indent + "\t<File Id='" + getFileId() + "' Name='txsui.exe' DiskId='1' Source='" + tagFolderPath + "\\torxakis\\bin\\txsui.exe' />\n";
        out += indent + "\t<File Id='" + getFileId() + "' Name='torxakis.bat' DiskId='1' Source='" + tagFolderPath + "\\torxakis\\bin\\torxakis.bat' />\n";
        out += indent + "\t<File Id='" + getFileId() + "' Name='torxakisPort.bat' DiskId='1' Source='" + tagFolderPath + "\\torxakis\\bin\\torxakisPort.bat' />\n";
        out += indent + "\t<File Id='" + getFileId() + "' Name='torxakisServer.bat' DiskId='1' Source='" + tagFolderPath + "\\torxakis\\bin\\torxakisServer.bat' />\n";
        out += indent + "\t<File Id='" + getFileId() + "' Name='license' DiskId='1' Source='" + tagFolderPath + "\\torxakis\\license' />\n";
        out += indent + "\t<File Id='defaultYamlFile' Name='.torxakis.default.yaml' DiskId='1' Source='" + tagFolderPath + "\\torxakis\\.torxakis.yaml' />\n";
        out += indent + "</Component>\n";
        out += indent + "\n";

        return out;
    }

    private String generateDocumentation(FeatureInfo parent, String indent) {
        FeatureInfo fi = new FeatureInfo(parent, getFeatureId(), "Documentation",
                "Explanation of Modelling Examples", 1);
        parent.getChildren().add(fi);
        featureInfos.add(fi);

        String componentId = getComponentId();

        fi.getComponentIds().add(componentId);

        return
                indent + "<!-- Torxakis Documentation -->\n" +
                        indent + "<Directory Id='" + getDirectoryId() + "' Name='Docs'>\n" +
                        indent + "\n" +
                        indent + "\t<Component Id='" + componentId + "' Guid='" + java.util.UUID.randomUUID() + "'>\n" +
                        indent + "\t\t<File Id='" + getFileId() + "' Name='Modelling Examples.html' DiskId='1' Source='" + tagFolderPath + "\\WindowsInstaller\\Modelling Examples.html' KeyPath='yes'/>\n" +
                        indent + "\t</Component>\n" +
                        indent + "\n" +
                        indent + "</Directory>\n" +
                        indent + "\n";
    }

    private String generateUi(String indent) {
        String out = "";
        String installerFolder = tagFolderPath + "\\WindowsInstaller\\";

        out += indent + "<WixVariable Id='WixUILicenseRtf' Value='" + installerFolder + "license.rtf' />\n";
        out += indent + "<WixVariable Id='WixUIDialogBmp' Value='" + installerFolder + "dialog.bmp' />\n";
        out += indent + "<WixVariable Id='WixUIBannerBmp' Value='" + installerFolder + "banner.bmp' />\n";
        out += indent + "<UI Id='UI'>\n";
        out += indent + "\t<UIRef Id='WixUI_Mondo'/>\n";
        out += indent + "\t<UIRef Id='WixUI_ErrorProgressText' />\n";
        out += indent + "</UI>\n";

        return out;
    }

    private String generatePathEnvironmentVariable(String indent) {
        String componentId = getComponentId();
        rootFi.getComponentIds().add(componentId);

        String out = indent + "<Component Id='" + componentId + "' Guid='" + java.util.UUID.randomUUID() + "' KeyPath='yes'>\n";

        out += indent + "\t<Environment Id='" + getEnvironmentId() + "' Name='PATH' Value='[INSTALLDIR]' Permanent='no' Part='first' Action='set' System='yes' />\n";

        for (String dirId : addToPath) {
            out += indent + "\t<Environment Id='" + getEnvironmentId() + "' Name='PATH' Value='[" + dirId + "]' Permanent='no' Part='first' Action='set' System='yes' />\n";
        }

        out += indent + "</Component>\n";

        return out;
    }

    private String generateFeatures(String indent) {
        String out = "";

        out += generateFeature(indent, rootFi);

        return out;
    }

    private String generateFeature(String indent, FeatureInfo featureInfo) {
        String out = featureInfo.getFeatureHeader(indent);

        for (String componentId : featureInfo.getComponentIds()) {
            out += indent + "\t<ComponentRef Id='" + componentId + "'/>\n";
        }

        if (!featureInfo.getChildren().isEmpty()) {
            for (FeatureInfo fi : featureInfo.getChildren()) {
                out += generateFeature(indent + "\t", fi);
            }
        }

        out += featureInfo.getFeatureFooter(indent);

        return out;
    }

    private String generatePrograms(String indent) {
        String componentId = getComponentId();
        rootFi.getComponentIds().add(componentId);

        String programMenuDir = getDirectoryId();

        String out =
                indent + "<Directory Id='" + getDirectoryId() + "' Name='Programs'>\n" +
                        indent + "\t<Directory Id='" + programMenuDir + "' Name='TorXakis'>\n" +
                        indent + "\t\t<Component Id='" + componentId + "' Guid='" + java.util.UUID.randomUUID() + "'>\n" +
                        indent + "\t\t\t<RemoveFolder Id='" + programMenuDir + "' On='uninstall' />\n" +
                        indent + "\t\t\t<RegistryValue Root='HKCU' Key='Software\\[Manufacturer]\\[ProductName]' Type='string' Value='' KeyPath='yes' />\n" +
                        indent + "\t\t</Component>\n" +
                        indent + "\t</Directory>\n" +
                        indent + "</Directory>\n";
        return out;
    }

    private String generateDirectory(String indent, TDirectory directory, FeatureInfo featureInfo) {
        String out = "";
        String directoryId = getDirectoryId();

        out += indent + "<Directory Id='" + directoryId + "' Name='" + directory.getNameOnly() + "'>\n";

        checkAddToPath(directory, directoryId);

        out += generateComponent(indent + "\t", directory.getFiles(), featureInfo);
        for (TDirectory dir : directory.getDirectories()) {
            out += generateDirectory(indent + "\t", dir, featureInfo);
        }

        out += indent + "</Directory>\n";

        return out;
    }

    private void checkAddToPath(TDirectory directory, String directoryId) {
        for (String path : pathDirectories) {
            if (directory.getName().equals(path)) {
                addToPath.add(directoryId);
                break;
            }
        }
    }

    private String generateComponent(String indent, List<TFile> files, FeatureInfo featureInfo) {
        String out = "";

        if (!files.isEmpty()) {
            String componentId = getComponentId();
            featureInfo.getComponentIds().add(componentId);

            out += indent + "<Component Id='" + componentId + "' Guid='" + java.util.UUID.randomUUID() + "' KeyPath='yes'>\n";

            for (TFile file : files) {
                out += generateFile(indent + "\t", file);
            }

            out += indent + "</Component>\n";
        }

        return out;
    }

    private String generateFile(String indent, TFile file) {
        return indent + "<File Id='" + getFileId() + "' Name='" + file.getNameOnly() + "' Source='" + file.getName() + "'/>\n";

    }

    private String getDirectoryId() {
        String id = DIRECTORY_PREFIX + directoryId;
        directoryId++;
        return id;
    }

    private String getComponentId() {
        String id = COMPONENT_PREFIX + componentId;
        componentId++;
        return id;
    }

    private String getFileId() {
        String id = FILE_PREFIX + fileId;
        fileId++;
        return id;
    }

    private String getFeatureId() {
        String id = FEATURE_PREFIX + featureId;
        featureId++;
        return id;
    }

    private String getEnvironmentId() {
        String id = ENVIRONMENT_PREFIX + environmentId;
        environmentId++;
        return id;
    }

}
