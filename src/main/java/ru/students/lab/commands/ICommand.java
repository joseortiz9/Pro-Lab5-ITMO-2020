package ru.students.lab.commands;

import ru.students.lab.database.Credentials;

import java.io.IOException;

/** 
 * Интерфейс, содержащий функции для работы с командами
 * @autor Хосе Ортис
 * @version 1.0
*/
public interface ICommand {

    int TYPE_NO_INPUT = 0;
    int TYPE_INPUT_DRAGON = 1;
    int TYPE_INPUT_CREDENTIAL = 2;

    /**
    * Функция для выполнения команды по работе с коллекцией
     * @param context - the context usable by every command to communicate with the collection and file manager
     * @param credentials - credentials of the User that sent the request
     */
    Object execute(ExecutionContext context, Credentials credentials) throws IOException;
}
