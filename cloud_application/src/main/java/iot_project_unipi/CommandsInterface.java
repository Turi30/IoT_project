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

    @ShellMethod("Get parameter of sensor or actuator")
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
        echo("The " + App.resources_array.get(index).getResourceType() + " is: " + res);


    }

    @ShellMethod("Post method")
    public void post_method() {
        Scanner scanner = new Scanner(System.in);
        echo("Enter the number of the resource to post data: ");
        int index = scanner.nextInt();

        if (index >= App.resources_array.size()) {
            echo("Index out of sensor indexes!");
            return;
        }

        if (!App.resources_array.get(index).hasMethod("POST")) {
            echo("This resourse doesn't have post method!");
            return;
        }

        String format = App.resources_array.get(index).getPostPutFormat();

        if (format.contains("&")) {
            for (String s : format.split("&")) {
                if (s.contains("|")) {
                    echo("Select one of the following options for the "
                            + s.substring(0, s.indexOf("=")));
                    for (String pos : s.substring(s.indexOf("=") + 1).split("|"))
                        System.out.println(pos + " ");
                    echo("");
                } else {
                    echo("Write a parameter for the " + s.substring(0, s.indexOf("=")));
                }
            }
        } else {
            echo("Enter the val");
            if (format.contains("|")) {
                echo("Select one of the following options for the "
                        + format.substring(0, format.indexOf("=")));
                for (String pos : format.substring(format.indexOf("=") + 1).split("|"))
                    System.out.print(pos + " ");
                echo("");
            } else {
                echo("Write a parameter for the " + format.substring(0, format.indexOf("=")));
            }
        }
    }

}
