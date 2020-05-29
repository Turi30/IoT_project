package iot_project_unipi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
        TerminalBuilder builder = TerminalBuilder.builder();
        Terminal terminal;
        try {
            echo("Enter the number of the resource to get data: ");
            terminal = builder.build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            int index = Integer.parseInt(reader.readLine());
            if (index >= App.resources_array.size()) {
                echo("Index out of sensor indexes!");
                return;
            }

            if (!App.resources_array.get(index).hasMethod("GET")) {
                echo("This resourse doesn't have get method!");
                return;
            }

            String res = App.resources_array.get(index).get(MediaTypeRegistry.APPLICATION_JSON)
                    .getResponseText();

            JSONParser parser = new JSONParser();
            try {
                JSONObject jsonObject = (JSONObject) parser.parse(res);
                for (Object o : jsonObject.keySet()) {
                    System.out
                            .println("The " + o + " is: " + (jsonObject.get((String) o)));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println(e);
        }


    }

    @ShellMethod("Post method")
    public void post_method() {
        TerminalBuilder builder = TerminalBuilder.builder();
        Terminal terminal;
        try {
            echo("Enter the number of the resource to post data: ");
            terminal = builder.build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            int index = Integer.parseInt(reader.readLine());

            if (index >= App.resources_array.size()) {
                echo("Index out of sensor indexes!");
                return;
            }

            if (!App.resources_array.get(index).hasMethod("POST")) {
                echo("This resourse doesn't have post method!");
                return;
            }

            String format = App.resources_array.get(index).getPostPutFormat();

            HashMap<String, Object> payload = new HashMap<>();

            for (String s : format.split("&")) {
                if (s.contains("|")) {
                    echo("Select one of the following options for the "
                            + s.substring(0, s.indexOf("=")));
                    // payload = payload.concat(" \"" + s.substring(0, s.indexOf("=")) + "\":\"");
                    for (String possibility : s.substring(s.indexOf("=") + 1).split("\\|"))
                        echo(possibility);
                    reader = LineReaderBuilder.builder().terminal(terminal).build();
                    String input = reader.readLine();
                    if (!s.substring(s.indexOf("=") + 1).contains(input)) {
                        echo("Wrong parameter! Retry");
                        return;
                    }
                    String key = s.substring(0, s.indexOf("="));
                    Object value = input;
                    payload.put(key, value);
                } else {
                    echo("Write a parameter for the " + s.substring(0, s.indexOf("=")));
                    reader = LineReaderBuilder.builder().terminal(terminal).build();
                    String key = s.substring(0, s.indexOf("="));
                    Object value = Integer.parseInt(reader.readLine());
                    payload.put(key, value);
                }
            }
            echo(new JSONObject(payload).toJSONString());
            App.resources_array.get(index).post(new JSONObject(payload).toJSONString(),
                    MediaTypeRegistry.APPLICATION_JSON, MediaTypeRegistry.APPLICATION_JSON);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

}
