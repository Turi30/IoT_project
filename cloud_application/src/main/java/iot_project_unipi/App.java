package iot_project_unipi;

import java.util.ArrayList;
import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapServer;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.shell.jline.PromptProvider;

@SpringBootApplication
@ComponentScan(basePackages = {"iot_project_unipi"})
public class App extends CoapServer {

    static public ArrayList<Resource> resources_array;
    static public ArrayList<Room> rooms_array;

    public static void main(String[] args) {
        CaliforniumLogger.disableLogging();

        // Initialize the resources arrray
        resources_array = new ArrayList<Resource>();
        // Initialize the rooms array
        rooms_array = new ArrayList<Room>();

        // Create a new instance of the application
        App server = new App();
        // Create and assign a new server to receive the registration requests
        server.add(new ResourcesRegistration("registration"));
        // Start the server
        server.start();

        // Start the shell to receive commands
        SpringApplication.run(App.class, args);

        // Close and destroy the server
        server.destroy();
    }

    // Change the prompt of the Spring shell
    @Bean
    public PromptProvider CustomPromptProvider() {
        return () -> new org.jline.utils.AttributedString("CloudApplication:>",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }
}
