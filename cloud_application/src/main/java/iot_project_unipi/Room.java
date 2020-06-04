package iot_project_unipi;

import java.util.ArrayList;

/*
 * This class create a new room that contains a resources array and offer some methods to get
 * informationa and modify it
 */
public class Room {
    private String name;
    private ArrayList<Resource> room_resources_array;

    // Create a room and initialize the array
    public Room(String name) {
        this.name = name;
        this.room_resources_array = new ArrayList<Resource>();
    }

    // Add a resource to array
    public void addResourceInRoom(Resource r) {
        this.room_resources_array.add(r);
    }

    // Return the name of the room
    public String getName() {
        return this.name;
    }

    // Return the number of the resources in the room
    public int getResourcesNumber() {
        return this.room_resources_array.size();
    }

    // Rename the room
    public void renameRoom(String s) {
        this.name = s;
    }

    // Return the resource stored to a given index
    public Resource getResource(int index) {
        return this.room_resources_array.get(index);
    }

    // Print all the resources in the room
    public void printResourcesInRoom() {
        System.out.println("The resources in the " + this.getName() + " are:");
        for (int i = 0; i < this.room_resources_array.size(); i++)
            System.out.println(i + ": " + this.room_resources_array.get(i).getName());
    }
}
