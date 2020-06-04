package iot_project_unipi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Queue;
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

/*
 * Class that create all the commands to interface with the application
 */
@ShellComponent
public class Interface {

    public void echo(String message) {
        System.out.println(message);
    }

    public String readLine() {
        TerminalBuilder builder = TerminalBuilder.builder();
        Terminal terminal;
        String s = "";
        try {
            terminal = builder.build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            s = reader.readLine();
        } catch (IOException e) {
            System.err.println(e);
        }
        return s;
    }

    @ShellMethod("Print all the resources")
    public void list_resources() {
        if (App.resources_array.size() == 0) {
            echo("No sensor present!");
            return;
        }
        for (int i = 0; i < App.resources_array.size(); i++)
            echo(i + ": " + App.resources_array.get(i).getName());
    }

    @ShellMethod("Rename resource")
    public void rename_resource() {
        echo("Enter the number of the resource to be renamed: ");
        int index = Integer.parseInt(readLine());

        if (index >= App.resources_array.size()) {
            echo("Index out of sensor indexes!");
            return;
        }

        echo("Enter the new name: ");
        App.resources_array.get(index).renameResource(readLine());
    }

    @ShellMethod("Get parameters of sensor or actuator in a given room")
    public void get_parameter_local() {

        echo("Enter the number of the room: ");
        int index_room = Integer.parseInt(readLine());
        if (index_room >= App.rooms_array.size()) {
            echo("Index out of room indexes!");
            return;
        }

        echo("Enter the number of the resource to get data: ");
        int index_res = Integer.parseInt(readLine());
        if (index_res >= App.rooms_array.get(index_room).getResourcesNumber()) {
            echo("Index out of sensor indexes!");
            return;
        }

        if (!App.rooms_array.get(index_res).getResource(index_room).hasMethod("GET")) {
            echo("This resourse doesn't have get method!");
            return;
        }

        String res = App.rooms_array.get(index_res).getResource(index_room)
                .get(MediaTypeRegistry.APPLICATION_JSON).getResponseText();

        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(res);
            for (Object o : jsonObject.keySet()) {
                System.out.println("The " + o + " is: " + (jsonObject.get((String) o)));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @ShellMethod("Get parameters of sensor or actuator in general")
    public void get_parameter_global() {

        echo("Enter the number of the resource to get data: ");
        int index = Integer.parseInt(readLine());
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
                System.out.println("The " + o + " is: " + (jsonObject.get((String) o)));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @ShellMethod("Perform POST method on a specific resource")
    public void post_method_global() {

        echo("Enter the number of the resource to post data: ");
        int index = Integer.parseInt(readLine());

        if (index >= App.resources_array.size()) {
            echo("Index out of sensor indexes!");
            return;
        }

        if (!App.resources_array.get(index).hasMethod("POST")) {
            echo("This resourse doesn't have post method!");
            return;
        }

        String format = App.resources_array.get(index).getPostPutFormat();
        format = format.replace("\"", "");

        HashMap<String, Object> payload = new HashMap<>();

        for (String s : format.split("&")) {
            if (s.contains("|")) {
                boolean correct_input = false;
                echo("Select one of the following options for the "
                        + s.substring(0, s.indexOf("=")));
                echo("[" + s.substring(s.indexOf("=") + 1) + "]");
                String input = readLine();

                for (String opt : s.substring(s.indexOf("=") + 1).split("\\|"))
                    if (opt.equals(input)) {
                        correct_input = true;
                        break;
                    }
                if (!correct_input) {
                    echo("Wrong parameter! Retry");
                    return;
                }

                String key = s.substring(0, s.indexOf("="));
                Object value = input;
                payload.put(key, value);
                if (input.equals("off"))
                    break;
            } else {
                echo("Write a parameter for the " + s.substring(0, s.indexOf("=")));
                String key = s.substring(0, s.indexOf("="));
                Object value = Integer.parseInt(readLine());
                payload.put(key, value);
            }
        }

        App.resources_array.get(index).post(new JSONObject(payload).toJSONString(),
                MediaTypeRegistry.APPLICATION_JSON, MediaTypeRegistry.APPLICATION_JSON);
    }


    @ShellMethod("Perform POST method on a specific resource in a given room")
    public void post_method_local() {

        echo("Enter the number of the room: ");
        int index_room = Integer.parseInt(readLine());
        if (index_room >= App.rooms_array.size()) {
            echo("Index out of room indexes!");
            return;
        }

        echo("Enter the number of the resource to post data: ");
        int index_res = Integer.parseInt(readLine());
        if (index_res >= App.rooms_array.get(index_room).getResourcesNumber()) {
            echo("Index out of sensor indexes!");
            return;
        }

        if (!App.rooms_array.get(index_res).getResource(index_room).hasMethod("POST")) {
            echo("This resourse doesn't have POST method!");
            return;
        }

        String format = App.rooms_array.get(index_room).getResource(index_res).getPostPutFormat();
        format = format.replace("\"", "");

        HashMap<String, Object> payload = new HashMap<>();

        for (String s : format.split("&")) {
            if (s.contains("|")) {
                boolean correct_input = false;
                echo("Select one of the following options for the "
                        + s.substring(0, s.indexOf("=")));
                echo("[" + s.substring(s.indexOf("=") + 1) + "]");
                String input = readLine();

                for (String opt : s.substring(s.indexOf("=") + 1).split("\\|"))
                    if (opt.equals(input)) {
                        correct_input = true;
                        break;
                    }
                if (!correct_input) {
                    echo("Wrong parameter! Retry");
                    return;
                }

                String key = s.substring(0, s.indexOf("="));
                Object value = input;
                payload.put(key, value);
                if (input.equals("off"))
                    break;
            } else {
                echo("Write a parameter for the " + s.substring(0, s.indexOf("=")));
                String key = s.substring(0, s.indexOf("="));
                Object value = Integer.parseInt(readLine());
                payload.put(key, value);
            }
        }

        App.rooms_array.get(index_room).getResource(index_res).post(
                new JSONObject(payload).toJSONString(), MediaTypeRegistry.APPLICATION_JSON,
                MediaTypeRegistry.APPLICATION_JSON);

    }

    @ShellMethod("Create a new room")
    public void create_room() {

        echo("Enter the name to assign to the room: ");
        Room r = new Room(readLine());

        App.rooms_array.add(r);

    }

    @ShellMethod("Print all the rooms")
    public void list_rooms() {
        if (App.rooms_array.size() == 0) {
            echo("No room present!");
            return;
        }
        for (int i = 0; i < App.rooms_array.size(); i++)
            echo(i + ": " + App.rooms_array.get(i).getName());
    }

    @ShellMethod("Rename a room")
    public void rename_room() {

        int index = Integer.parseInt(readLine());

        if (index >= App.rooms_array.size()) {
            echo("Index out of room indexes!");
            return;
        }

        echo("Enter the new name: ");
        App.rooms_array.get(index).renameRoom(readLine());

    }

    @ShellMethod("Assign a resource to a specific room")
    public void assign_room_resource() {

        echo("Enter the number of the room: ");
        int index_room = Integer.parseInt(readLine());

        if (index_room >= App.rooms_array.size()) {
            echo("Index out of room indexes!");
            return;
        }

        echo("Enter the number of the resource to assign: ");
        int index_res = Integer.parseInt(readLine());

        if (index_res >= App.resources_array.size()) {
            echo("Index out of resource indexes!");
            return;
        }

        if (App.resources_array.get(index_res).getInRoom()) {
            echo("Resource already assigned to a room!");
            return;
        }

        App.resources_array.get(index_res).setInRoom(true);
        Resource r = App.resources_array.get(index_res);
        App.rooms_array.get(index_room).addResourceInRoom(r);
        echo(App.resources_array.get(index_res).getName() + " assigned to the "
                + App.rooms_array.get(index_room).getName());;
    }

    @ShellMethod("Print all the resources inside a room")
    public void list_room_resources() {

        echo("Enter the number of the room: ");
        int index = Integer.parseInt(readLine());

        if (index >= App.rooms_array.size()) {
            echo("Index out of room indexes!");
            return;
        }

        App.rooms_array.get(index).printResourcesInRoom();
    }

    @ShellMethod("Print the last values(up to 20) of the resource if this is observable")
    public void get_values_history() {

        echo("Enter the number of the resource to get the data history: ");
        int index = Integer.parseInt(readLine());
        if (index >= App.resources_array.size()) {
            echo("Index out of sensor indexes!");
            return;
        }

        if (!App.resources_array.get(index).isObservable()) {
            echo("This resource is not observable!");
            return;
        }

        JSONParser parser = new JSONParser();
        try {
            Queue<String> q = App.resources_array.get(index).getQueueObserve();
            System.out.print("The last values of the ");
            for (String s : q) {
                JSONObject jsonObject = (JSONObject) parser.parse(s);
                for (Object o : jsonObject.keySet()) {
                    System.out.print(o + " are: ");
                    break;
                }
                break;
            }
            for (String s : q) {
                JSONObject jsonObject = (JSONObject) parser.parse(s);
                for (Object o : jsonObject.keySet()) {
                    System.out.print(jsonObject.get((String) o) + " ");
                }
            }
            echo("");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
