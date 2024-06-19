package de.unitrier.st.fp.s24.ueb09.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        //Make sure the folder exists else notify user
        ensureResourcesFolderExists();
        Server.getInstance().start();
    }
    //aufgrund ihres Feedbacks zu Ueb08:)
    private static void ensureResourcesFolderExists(){
        Path resourcesPath = Paths.get("src/main/resources");
        if(!Files.exists(resourcesPath)){
            try{
                Files.createDirectories(resourcesPath);
                System.out.println("Resources folder created at "+resourcesPath.toAbsolutePath());
            } catch (IOException e) {
                System.out.println("Could not create resources folder");
                e.printStackTrace();
                System.exit(404);
            }
        }
    }
}
