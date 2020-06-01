package iot_project_unipi;

import java.util.ArrayList;

public class Room {
    private String name;
    private ArrayList<Resource> room_resources_array;

    public Room(String name) {
        super();

        this.name = name;
        this.room_resources_array = new ArrayList<Resource>();
    }

    public void addResourceInRoom(Resource r) {
        this.room_resources_array.add(r);
    }

    public String getName() {
        return this.name;
    }

    public int getResourcesNumber() {
        return this.room_resources_array.size();
    }

    public void renameRoom(String s) {
        this.name = s;
    }

    public Resource getResource(int index) {
        return this.room_resources_array.get(index);
    }

    public void printResourcesInRoom() {
        System.out.println("The resources in the " + this.getName() + " are:");
        for (int i = 0; i < this.room_resources_array.size(); i++)
            System.out.println(i + ": " + this.room_resources_array.get(i).getName());
    }
}
