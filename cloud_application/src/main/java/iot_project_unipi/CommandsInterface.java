package iot_project_unipi;

import java.util.Scanner;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class CommandsInterface {
    public void addResource(Resource r) {
        App.resources_array.add(r);
    }

    @ShellMethod("Print message to display")
    public void echo() {
        System.out.println("echo");
    }

    @ShellMethod("Print resource path")
    public void list() {
        for (int i = 0; i < App.resources_array.size(); i++)
            System.out.println(i + ": " + App.resources_array.get(i).getName());
    }

    @ShellMethod("Rename resource name")
    public void rename() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the number of the resource to be renamed: ");
        int index = scanner.nextInt();
        scanner = new Scanner(System.in);
        System.out.println("Enter the new name for the resource: ");
        String name = scanner.nextLine();
        App.resources_array.get(index).renameResource(name);;
    }
}