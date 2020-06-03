package plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Faction {
    private String name = "defaultName";
    private String description = "defaultDescription";
    private ArrayList<String> members = new ArrayList<String>();
    private String owner = "defaultOwner";

    // player constructor
    Faction(String initialName, String creator) {
        setName(initialName);
        setOwner(creator);
    }

    // server constructor
    Faction(String initialName) {
        setName(initialName);
    }

    ArrayList<String> getMemberList() {
        ArrayList<String> membersCopy = members;
        return membersCopy;
    }

    int getPopulation() {
        return members.size();
    }

    void setOwner(String playerName) {
        owner = playerName;
    }

    boolean isOwner(String playerName) {
        if (playerName.equalsIgnoreCase(owner)) {
            return true;
        }
        else {
            return false;
        }
    }

    String getOwner() {
        return owner;
    }

    void setName(String newName) {
        name = newName;
    }

    String getName() {
        return name;
    }

    void setDescription(String newDesc) {
        description = newDesc;
    }

    String getDescription() {
        return description;
    }

    void addMember(String playerName) {
        members.add(playerName);
    }

    void removeMember(String playerName) {
        members.remove(playerName);
    }

    boolean isMember(String playerName) {
        boolean membership = false;
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).equalsIgnoreCase(playerName)) {
                membership = true;
            }
        }
        return membership;
    }

    boolean save() {
        try {
            File saveFile = new File(name + ".txt");
            if (saveFile.createNewFile()) {
                System.out.println("Save file for faction " + name + " created.");
            } else {
                System.out.println("Save file for faction " + name + " already exists. Altering.");
            }

            FileWriter saveWriter = new FileWriter(name + ".txt");

            // actual saving takes place here
            saveWriter.write(name + "\n");
            saveWriter.write(owner + "\n");
            saveWriter.write(description + "\n");

            for (int i = 0; i < members.size(); i++) {
                saveWriter.write(members.get(i) + "\n");
            }

            saveWriter.close();

            System.out.println("Successfully saved faction " + name + ".");
            return true;

        } catch (IOException e) {
            System.out.println("An error occurred saving the faction named " + name);
            e.printStackTrace();
            return false;
        }

    }

    boolean load(String filename) {
        try {
            File loadFile = new File(filename);
            Scanner loadReader = new Scanner(loadFile);

            // actual loading
            if (loadReader.hasNextLine()) {
                setName(loadReader.nextLine());
            }
            if (loadReader.hasNextLine()) {
                setOwner(loadReader.nextLine());
            }
            if (loadReader.hasNextLine()) {
                setDescription(loadReader.nextLine());
            }

            while (loadReader.hasNextLine()) {
                String temp = loadReader.nextLine();
                members.add(temp);
            }

            loadReader.close();
            System.out.println("Faction " + name + " successfully loaded.");
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred loading the file " + filename + ".");
            e.printStackTrace();
            return false;
        }
    }

}