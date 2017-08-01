package nl.tno.torxakis.wxsgenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WxsConfig {
    private boolean isInvalid = true;
    private List<String> errors = new ArrayList<>();
    private File configFile;
    private String z3Folder;
    private String cvc4Folder;
    private String version;
    private String torxakisFolder;
    private String eclipseFolder;
    private String nppFolder;

    WxsConfig(String configFilePath) {
        configFile = new File(configFilePath);
        if (!configFile.exists()) {
            errors.add(String.format("Config file %s doesn't exist", configFilePath));
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                process(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            errors.add(String.format("Can't read %s: %s", configFilePath, e.getMessage()));
            return;
        }

        validate();
    }

    private void process(String line) {
        String[] parts = line.split("=");
        if (parts.length != 2) {
            System.out.println("WARNING - Bad configuration line: " + line);
            return;
        }
        switch (parts[0]) {
            case "z3Folder":
                z3Folder = parts[1];
                break;
            case "cvc4Folder":
                cvc4Folder = parts[1];
                break;
            case "version":
                version = parts[1];
                break;
            case "torxakisFolder":
                torxakisFolder = parts[1];
                break;
            case "eclipseFolder":
                eclipseFolder = parts[1];
                break;
            case "nppFolder":
                nppFolder = parts[1];
                break;
            default:
                System.out.println("WARNING - Unknown configuration " + parts[0] + " with value " + parts[1]);
        }
    }

    private void validate() {
        if (isBlank(z3Folder)) {
            errors.add("z3Folder is blank.");
        } else {
            File z3Directory = new File(z3Folder);
            if (!z3Directory.exists()) {
                errors.add(String.format("z3Folder '%s' does not exist", z3Folder));
            }
        }

        if (isBlank(cvc4Folder)) {
            errors.add("cvc4Folder is blank.");
        } else {
            File cvc4Directory = new File(cvc4Folder);
            if (!cvc4Directory.exists()) {
                errors.add(String.format("cvc4Folder '%s' does not exist", cvc4Folder));
            }
        }

        if (isBlank(version)) {
            errors.add("version is blank.");
        }

        if (isBlank(torxakisFolder)) {
            errors.add("torxakisFolder is blank.");
        } else {
            File torxakisDirectory = new File(torxakisFolder);
            if (!torxakisDirectory.exists()) {
                errors.add(String.format("torxakisFolder '%s' does not exist", torxakisFolder));
            }
        }

        if (isBlank(eclipseFolder)) {
            errors.add("eclipseFolder is blank.");
        } else {
            File eclipseDirectory = new File(eclipseFolder);
            if (!eclipseDirectory.exists()) {
                errors.add(String.format("eclipseFolder '%s' does not exist", eclipseFolder));
            }
        }

        if (isBlank(nppFolder)) {
            errors.add("nppFolder is blank.");
        } else {
            File nppDirectory = new File(nppFolder);
            if (!nppDirectory.exists()) {
                errors.add(String.format("nppFolder '%s' does not exist", nppFolder));
            }
        }

        isInvalid = !errors.isEmpty();
    }

    private static boolean isBlank(String param) {
        return param == null || param.trim().length() == 0;
    }

    boolean isInvalid() {
        return isInvalid;
    }

    List<String> getErrors() {
        return errors;
    }

    public File getConfigFile() {
        return configFile;
    }

    String getZ3Folder() {
        return z3Folder;
    }

    String getCvc4Folder() {
        return cvc4Folder;
    }

    String getVersion() {
        return version;
    }

    String getTorxakisFolder() {
        return torxakisFolder;
    }

    String getEclipseFolder() {
        return eclipseFolder;
    }

    String getNppFolder() {
        return nppFolder;
    }
}
