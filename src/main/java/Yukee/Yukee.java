package Yukee;
import Yukee.task.Todo;
import Yukee.task.Event;
import Yukee.task.Task;
import Yukee.storage.Storage;
import Yukee.task.TaskList;
import Yukee.parser.Parser;
import Yukee.exception.YukeeException;
import Yukee.task.Deadline;
import java.util.ArrayList;

/**
 * The main program class responsible for managing user interactions and executing commands.
 */
public class Yukee {
    private final Storage storage;
    private final TaskList taskList;
    private final Ui ui;

    /**
     * Constructs a new instance of Yukee, initializing the storage, task list, and user interface.
     *
     * @param filePath the file path where task data is stored
     */
    public Yukee(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        TaskList loadedTasks;

        try {
            loadedTasks = new TaskList(storage.load());
        } catch (YukeeException e) {
            ui.showLoadingError();
            loadedTasks = new TaskList();
        }

        taskList = loadedTasks;
    }

    /**
     * Runs the program, reading user input and executing corresponding commands.
     */
    public void run() {
        ui.showWelcome();
        boolean isExit = false;

        while (!isExit) {
            String input = ui.readCommand();
            try {
                String[] parsedCommand = Parser.parse(input);
                String command = parsedCommand[0].toLowerCase();

                switch (command) {
                    case "bye":
                        ui.showGoodbye();
                        isExit = true;
                        break;
                    case "list":
                        taskList.printTasks();
                        break;
                    case "todo":
                        if (parsedCommand.length < 2) {
                            throw new YukeeException("The description of a todo cannot be empty.");
                        }
                        taskList.addTask(new Todo(parsedCommand[1]));
                        ui.showAddTask(taskList.getLastTask(), taskList.size());
                        storage.save(taskList.getTasks());
                        break;
                    case "deadline":
                        if (parsedCommand.length < 2 || !parsedCommand[1].contains("/by")) {
                            throw new YukeeException("The description of a deadline must contain '/by'.");
                        }
                        String[] deadlineParts = parsedCommand[1].split(" /by ");
                        taskList.addTask(new Deadline(deadlineParts[0], deadlineParts[1]));
                        ui.showAddTask(taskList.getLastTask(), taskList.size());
                        storage.save(taskList.getTasks());
                        break;
                    case "event":
                        if (parsedCommand.length < 2 || !parsedCommand[1].contains("/from") || !parsedCommand[1].contains("/to")) {
                            throw new YukeeException("The description of an event must contain '/from' and '/to'.");
                        }
                        String[] eventParts = parsedCommand[1].split(" /from | /to ");
                        taskList.addTask(new Event(eventParts[0], eventParts[1], eventParts[2]));
                        ui.showAddTask(taskList.getLastTask(), taskList.size());
                        storage.save(taskList.getTasks());
                        break;
                    case "mark":
                        if (parsedCommand.length < 2) {
                            throw new YukeeException("Please provide the task number to mark as done.");
                        }
                        int markIndex = Integer.parseInt(parsedCommand[1]) - 1;
                        taskList.markTaskAsDone(markIndex);
                        ui.showMarkTask(taskList.getTask(markIndex));
                        storage.save(taskList.getTasks());
                        break;
                    case "unmark":
                        if (parsedCommand.length < 2) {
                            throw new YukeeException("Please provide the task number to unmark.");
                        }
                        int unmarkIndex = Integer.parseInt(parsedCommand[1]) - 1;
                        taskList.markTaskAsNotDone(unmarkIndex);
                        ui.showUnmarkTask(taskList.getTask(unmarkIndex));
                        storage.save(taskList.getTasks());
                        break;
                    case "delete":
                        if (parsedCommand.length < 2) {
                            throw new YukeeException("Please provide the task number to delete.");
                        }
                        int deleteIndex = Integer.parseInt(parsedCommand[1]) - 1;
                        Task deletedTask = taskList.deleteTask(deleteIndex);
                        ui.showDeleteTask(deletedTask, taskList.size());
                        storage.save(taskList.getTasks());
                        break;
                    case "find":
                        if (parsedCommand.length < 2) {
                            throw new YukeeException("The find command must be followed by a keyword.");
                        }
                        String keyword = parsedCommand[1];
                        ArrayList<Task> foundTasks = taskList.findTasks(keyword);
                        ui.showFoundTasks(foundTasks);
                        break;

                    case "help":
                        ui.showHelp();
                        break;
                    default:
                        ui.showError("Unknown command: " + command);
                }
            } catch (YukeeException e) {
                ui.showError(e.getMessage());
            } catch (Exception e) {
                ui.showError("An error occurred: " + e.getMessage());
            }
        }
    }

    /**
     * Main method to start the Yukee program.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        new Yukee("./src/main/java/yukee.txt").run();
    }
}