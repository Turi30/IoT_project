package iot_project_unipi;

import java.util.Scanner;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class CommandsInterface {
    /*
     * public void addResource(Resource r) { App.resources_array.add(r); }
     */

    @ShellMethod("Print message to display")
    public void echo(String message) {
        System.out.println(message);
    }

    @ShellMethod("Print resource name")
    public void list() {
        if (App.resources_array.size() == 0) {
            echo("No sensor present!");
            return;
        }
        for (int i = 0; i < App.resources_array.size(); i++)
            echo(i + ": " + App.resources_array.get(i).getName());
    }

    @ShellMethod("Rename resource name")
    public void rename() {
        Scanner scanner = new Scanner(System.in);
        echo("Enter the number of the resource to be renamed and the new name: ");
        int index = scanner.nextInt();
        String name = scanner.nextLine();

        if (index >= App.resources_array.size()) {
            echo("Index out of sensor indexes!");
            return;
        }

        App.resources_array.get(index).renameResource(name);

    }

    @ShellMethod("Get parameter of sensor")
    public void get_parameter() {
        Scanner scanner = new Scanner(System.in);
        echo("Enter the number of the resource to get data: ");
        int index = scanner.nextInt();

        if (index >= App.resources_array.size()) {
            echo("Index out of sensor indexes!");
            return;
        }

        if (!App.resources_array.get(index).hasMethod("GET")) {
            echo("This resourse doesn't have get method!");
            return;
        }

        String res = App.resources_array.get(index).get().getResponseText();
        System.out
                .println("The " + App.resources_array.get(index).getResourceType() + " is: " + res);


    }

}
