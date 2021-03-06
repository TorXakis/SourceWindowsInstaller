package nl.tno.torxakis.wxsgenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class WxsConfig {
    private boolean isInvalid = true;
    private List<String> errors = new ArrayList<>();
    private String z3Url;
    private String cvc4Url;
    private String torxakisFolder;
    private String eclipsePluginUrl;
    private String nppFolder;

    WxsConfig(String configFilePath, String tagFolder) {
        File configFile = new File(configFilePath);
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

        validate(tagFolder);
    }

    private void process(String line) {
        String[] parts = line.split("=");
        if (parts.length != 2) {
            System.out.println("WARNING - Bad configuration line: " + line);
            return;
        }
        switch (parts[0]) {
            case "z3Url":
                z3Url = parts[1];
                break;
            case "cvc4Url":
                cvc4Url = parts[1];
                break;
            case "torxakisFolder":
                torxakisFolder = parts[1];
                break;
            case "eclipsePluginUrl":
                eclipsePluginUrl = parts[1];
                break;
            case "nppFolder":
                nppFolder = parts[1];
                break;
            default:
                System.out.println("WARNING - Unknown configuration " + parts[0] + " with value " + parts[1]);
        }
    }

    private void validate(String tagFolder) {
        if (isBlank(z3Url)) {
            errors.add("z3Url is blank.");
        }

        if (isBlank(cvc4Url)) {
            errors.add("cvc4Url is blank.");
        }

        if (isBlank(torxakisFolder)) {
            errors.add("torxakisFolder is blank.");
        } else {
            File torxakisDirectory = new File(tagFolder + "\\" + torxakisFolder);
            if (!torxakisDirectory.exists()) {
                errors.add(String.format("torxakisFolder '%s' does not exist", torxakisDirectory.getPath()));
            }
        }

        if (isBlank(eclipsePluginUrl)) {
            errors.add("eclipsePluginUrl is blank.");
        }

        if (isBlank(nppFolder)) {
            errors.add("nppFolder is blank.");
        } else {
            File nppDirectory = new File(tagFolder + "\\" + nppFolder);
            if (!nppDirectory.exists()) {
                errors.add(String.format("nppFolder '%s' does not exist", nppDirectory.getPath()));
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

    String getZ3Url() {
        return z3Url;
    }

    String getCvc4Url() {
        return cvc4Url;
    }

    String getTorxakisFolder() {
        return torxakisFolder;
    }

    String getEclipsePluginUrl() {
        return eclipsePluginUrl;
    }

    String getNppFolder() {
        return nppFolder;
    }
}
