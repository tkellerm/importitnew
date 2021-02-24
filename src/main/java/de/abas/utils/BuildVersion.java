package de.abas.utils;

public class BuildVersion {

    private BuildVersion(){
        throw new IllegalStateException("Utility class");
    }


    public static String getBuildVersion(){
        return BuildVersion.class.getPackage().getImplementationVersion();
    }
}
