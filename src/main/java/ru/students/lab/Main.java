package ru.students.lab;

import org.jetbrains.annotations.NotNull;
import ru.students.lab.client.IHandlerInput;
import ru.students.lab.client.UserInputHandler;
import ru.students.lab.managers.CollectionManager;
import ru.students.lab.managers.CommandManager;
import ru.students.lab.managers.FileManager;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class Main {

    public static void main(@NotNull String[] args) {

        IHandlerInput userInputHandler = new UserInputHandler();

        if (args.length > 0) {
            try {
                String pathToFile = Paths.get(args[0]).toAbsolutePath().toString();
                FileManager fileManager = new FileManager(pathToFile);
                CollectionManager collectionManager = new CollectionManager(fileManager.getCollectionFromFile());
                CommandManager manager = new CommandManager(userInputHandler, fileManager, collectionManager);
                manager.startInteraction();
            } catch (InvalidPathException ex) {
                userInputHandler.printLn(1, "Invalid file's path or/and security problem trying to access it");
            } catch (SecurityException ex) {
                userInputHandler.printLn(1, "Security problems trying to access to the file (Can not be read or edited)");
            } catch (IOException ex) {
                userInputHandler.printLn(1, "I/O problems, I have no idea what you just did: " + ex.getMessage());
            }
        } else {
            userInputHandler.printLn(1, "No path passed as parameter, please specify a file name.");
        }

        //fill the xml file with random data
        //new TesterFiller().collectionToXML(pathToData);

    }
}
