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

    public static void main(String[] args) {
        CaliforniumLogger.disableLogging();
        resources_array = new ArrayList<Resource>();
        App server = new App();
        server.add(new RegistrationResource("registration"));
        server.start();
        SpringApplication.run(App.class, args);
        server.destroy();
    }

    @Bean
    public PromptProvider CustomPromptProvider() {
        return () -> new org.jline.utils.AttributedString("CloudApplication:>",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }
}
