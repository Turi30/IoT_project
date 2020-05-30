package iot_project_unipi;

import java.io.IOException;
import java.util.HashMap;
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

    @ShellMethod("Print message to display")
    public void echo(String message) {
        System.out.println(message);
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
        TerminalBuilder builder = TerminalBuilder.builder();
        Terminal terminal;
        try {
            terminal = builder.build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            echo("Enter the number of the resource to be renamed: ");
            int index = Integer.parseInt(reader.readLine());

            if (index >= App.resources_array.size()) {
                echo("Index out of sensor indexes!");
                return;
            }

            echo("Enter the new name: ");
            String name = reader.readLine();

            App.resources_array.get(index).renameResource(name);
        } catch (IOException e) {
            System.err.println(e);
        }

    }

    @ShellMethod("Get parameter of sensor or actuator in a specific room")
    public void get_parameter_local() {
        TerminalBuilder builder = TerminalBuilder.builder();
        Terminal terminal;
        try {
            terminal = builder.build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            echo("Enter the number of the room: ");
            int index_room = Integer.parseInt(reader.readLine());
            if (index_room >= App.rooms_array.size()) {
                echo("Index out of room indexes!");
                return;
            }

            echo("Enter the number of the resource to get data: ");
            int index_res = Integer.parseInt(reader.readLine());
            if (index_res >= App.rooms_array.get(index_room).getNumberResources()) {
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
        } catch (IOException e) {
            System.err.println(e);
        }


    }

    @ShellMethod("Get parameter of sensor or actuator in general")
    public void get_parameter_global() {
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
                    System.out.println("The " + o + " is: " + (jsonObject.get((String) o)));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println(e);
        }


    }

    @ShellMethod("Post method")
    public void post_method_global() {
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
                    for (String possibility : s.substring(s.indexOf("=") + 1).split("\\|"))
                        echo(possibility);
                    String input = reader.readLine();
                    /*
                     * if (!s.substring(s.indexOf("=") + 1).contains(input)) {
                     * echo("Wrong parameter! Retry"); return; }
                     */
                    String key = s.substring(0, s.indexOf("="));
                    Object value = input;
                    payload.put(key, value);
                    if (input.equals("off"))
                        break;
                } else {
                    echo("Write a parameter for the " + s.substring(0, s.indexOf("=")));
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

    @ShellMethod("Post method")
    public void post_method_local() {
        TerminalBuilder builder = TerminalBuilder.builder();
        Terminal terminal;
        try {
            terminal = builder.build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            echo("Enter the number of the room: ");
            int index_room = Integer.parseInt(reader.readLine());
            if (index_room >= App.rooms_array.size()) {
                echo("Index out of room indexes!");
                return;
            }

            echo("Enter the number of the resource to post data: ");
            int index_res = Integer.parseInt(reader.readLine());
            if (index_res >= App.rooms_array.get(index_room).getNumberResources()) {
                echo("Index out of sensor indexes!");
                return;
            }

            if (!App.rooms_array.get(index_res).getResource(index_room).hasMethod("POST")) {
                echo("This resourse doesn't have POST method!");
                return;
            }

            String format = App.rooms_array.get(index_room).getResource(index_res).getPostPutFormat();

            HashMap<String, Object> payload = new HashMap<>();

            for (String s : format.split("&")) {
                if (s.contains("|")) {
                    echo("Select one of the following options for the "
                            + s.substring(0, s.indexOf("=")));
                    for (String possibility : s.substring(s.indexOf("=") + 1).split("\\|"))
                        echo(possibility);
                    String input = reader.readLine();
                    /*
                     * if (!s.substring(s.indexOf("=") + 1).contains(input)) {
                     * echo("Wrong parameter! Retry"); return; }
                     */
                    String key = s.substring(0, s.indexOf("="));
                    Object value = input;
                    payload.put(key, value);
                    if (input.equals("off"))
                        break;
                } else {
                    echo("Write a parameter for the " + s.substring(0, s.indexOf("=")));
                    String key = s.substring(0, s.indexOf("="));
                    Object value = Integer.parseInt(reader.readLine());
                    payload.put(key, value);
                }
            }
            echo(new JSONObject(payload).toJSONString());
            App.rooms_array.get(index_room).getResource(index_res).post(new JSONObject(payload).toJSONString(),
                    MediaTypeRegistry.APPLICATION_JSON, MediaTypeRegistry.APPLICATION_JSON);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @ShellMethod("Create room")
    public void create_room() {
        TerminalBuilder builder = TerminalBuilder.builder();
        Terminal terminal;
        try {
            terminal = builder.build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            echo("Enter the name to assign to the room: ");

            String name = reader.readLine();

            Room r = new Room(name);

            App.rooms_array.add(r);
        } catch (IOException e) {
            System.err.println(e);
        }
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

    @ShellMethod("Rename room")
    public void rename_room() {
        TerminalBuilder builder = TerminalBuilder.builder();
        Terminal terminal;
        try {
            terminal = builder.build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            echo("Enter the number of the room to be renamed: ");
            int index = Integer.parseInt(reader.readLine());

            if (index >= App.rooms_array.size()) {
                echo("Index out of room indexes!");
                return;
            }

            echo("Enter the new name: ");
            String name = reader.readLine();

            App.rooms_array.get(index).renameRoom(name);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @ShellMethod("Assign resource to a room")
    public void assign_room_resource() {
        TerminalBuilder builder = TerminalBuilder.builder();
        Terminal terminal;
        try {
            terminal = builder.build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            echo("Enter the number of the room: ");
            int index_room = Integer.parseInt(reader.readLine());

            if (index_room >= App.rooms_array.size()) {
                echo("Index out of room indexes!");
                return;
            }

            echo("Enter the number of the resource to assign: ");
            int index_res = Integer.parseInt(reader.readLine());

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

        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @ShellMethod("Print all the resources inside a room")
    public void list_room_resources() {
        TerminalBuilder builder = TerminalBuilder.builder();
        Terminal terminal;
        try {
            terminal = builder.build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();
            echo("Enter the number of the room: ");
            int index = Integer.parseInt(reader.readLine());

            if (index >= App.rooms_array.size()) {
                echo("Index out of room indexes!");
                return;
            }

            App.rooms_array.get(index).printResourcesInRoom();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

}
