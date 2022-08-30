package manager;

import constants.Status;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;
import tasks.TaskType;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FileBackedTasksManager
        extends InMemoryTaskManager implements TaskManager {
    private final File file;
    public static TaskManager taskManager = Managers.getInMemoryTaskManager();
    public FileBackedTasksManager(File file) {
        this.file = file;
    }


    private void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {

            bufferedWriter.write("id,type,name,status,description,epic by subtask");
            bufferedWriter.newLine();

            tasksToFile(bufferedWriter, TaskManager.tasks);
            tasksToFile(bufferedWriter, TaskManager.epics);

            // к сожалению не придумал как сделать 1 метод под все типы задач
            for (SubTask value : TaskManager.subtasks.values()) {
                bufferedWriter.write(String.format("%s,%s,%s,%s,%s,%s",
                        value.getId(), value.getTaskType(), value.getName(), value.getStatus(),
                        value.getDescription(), value.getEpicId()));
                bufferedWriter.newLine();
            }
            bufferedWriter.newLine();
            historyOfTasksToFile(bufferedWriter, historyManager);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи");
        }
    }

    private <T extends Task> void tasksToFile(BufferedWriter bufferedWriter, Map<Integer, T> tasks) throws IOException {
        for (T value : tasks.values()) {
            bufferedWriter.write(String.format("%s,%s,%s,%s,%s",
                    value.getId(), value.getTaskType(), value.getName(),
                    value.getStatus(), value.getDescription()));
            bufferedWriter.newLine();
        }
    }

    private void historyOfTasksToFile(BufferedWriter bufferedWriter,
                                      HistoryManager historyManager) throws IOException {
        List<Task> history = historyManager.getHistory();
        for (Task task : history) {
            bufferedWriter.write(String.format("%s,", task.getId()));
        }
    }

    static class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(final String message) {
            super(message);
        }
    }


    public static FileBackedTasksManager loadFromFile(File file) {
        final FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);

        try {
            String stringFromFile = Files.readString(file.toPath());

            List<Task> tasks = parseTasksFromCSV(stringFromFile);
            for (Task task : tasks) {
                switch (task.getTaskType()) {
                    case TASK -> taskManager.addTask(task);
                    case EPIC -> taskManager.addEpic((Epic) task);
                    case SUBTASK -> taskManager.addSubtask((SubTask) task);
                }
            }

            List<Integer> listOfIDs = parseTaskHistoryFromCSV(stringFromFile);
            for (Integer listOfID : listOfIDs) {
                if (taskManager.tasks.containsKey(listOfID)) {
                    taskManager.getTask(listOfID);
                } else if (taskManager.epics.containsKey(listOfID)) {
                    taskManager.getEpic(listOfID);
                } else if (taskManager.subtasks.containsKey(listOfID)) {
                    taskManager.getSubtask(listOfID);
                }
            }



        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения");
        }
        return fileBackedTasksManager;
    }

    public static List<Task> parseTasksFromCSV(String value) {
        List<Task> tasks = new ArrayList<>();
        String[] split = value.split("\n");

        for (int i = 1; i < split.length - 2; i++) {
            String[] valueFromFile = split[i].split(",");
            Integer epicBySubtask = null;

            int id = Integer.parseInt(valueFromFile[0]);
            TaskType type = TaskType.valueOf(valueFromFile[1]);
            String name = valueFromFile[2];
            Status status = Status.valueOf(valueFromFile[3]);
            String description = valueFromFile[4];
            if (type.equals(TaskType.SUBTASK)) {
                epicBySubtask = Integer.valueOf(valueFromFile[5]);
            }

            switch (type) {
                case TASK -> tasks.add(new Task(id, name, description, status));
                case EPIC -> tasks.add(new Epic(id, name, description, status));
                case SUBTASK -> tasks.add(new SubTask(id, name, description, status, epicBySubtask));
            }
        }
        return tasks;
    }

    public static List<Integer> parseTaskHistoryFromCSV(String value) {
        List<Integer> listOfId = new ArrayList<>();

        String[] valuesFromFile = value.split("\n\n");
        String historyIDs = valuesFromFile[1];
        String[] historyID = historyIDs.split(",");

        for (String idFromHistory : historyID) {
            listOfId.add(Integer.valueOf(idFromHistory));
        }
        return listOfId;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(SubTask subTask) {
        super.addSubtask(subTask);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void removeTask(int taskId) {
        super.removeTask(taskId);
        save();
    }

    @Override
    public void removeEpic(int epicId) {
        super.removeEpic(epicId);
        save();
    }

    @Override
    public void removeSubtask(int subtaskId) {
        super.removeSubtask(subtaskId);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(SubTask subTask) {
        super.updateSubtask(subTask);
        save();
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> list = super.getAllTasks();
        save();
        return list;
    }

    @Override
    public List<Epic> getALLEpics() {
        List<Epic> list = super.getALLEpics();
        save();
        return list;
    }

    @Override
    public List<SubTask> getALLSubtasks() {
        List<SubTask> list = super.getALLSubtasks();
        save();
        return list;
    }

    @Override
    public Task getTask(int taskId) {
        Task task = super.getTask(taskId);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = super.getEpic(epicId);
        save();
        return epic;
    }

    @Override
    public SubTask getSubtask(int subtaskId) {
        SubTask subtask = super.getSubtask(subtaskId);
        save();
        return subtask;
    }

    @Override
    public List<SubTask> getSubtasksByEpic(int epicId) {
        List<SubTask> list = super.getSubtasksByEpic(epicId);
        save();
        return list;
    }

    @Override
    public Status updateEpicStatus(Epic epic) {
        Status status = super.updateEpicStatus(epic);
        save();
        return status;
    }

    public static void main(String[] args) {
        // Test 1 - write data to file
//        writeToFileTest();

        // Test 2 - read data from file
        readFromFileTest();
    }

    private static void writeToFileTest() {
        TaskManager manager = Managers.getFileBackedTaskManager();

        Task task1 = new Task("task 1", "description by task 1", Status.NEW);
        manager.addTask(task1);

        Epic epic1 = new Epic("epic 1", "description by epic 1");
        manager.addEpic(epic1);

        Epic epic2 = new Epic("epic 2", "description by epic 2");
        manager.addEpic(epic2);

        SubTask subTask1 = new SubTask("subtask 1", "description by subtask 1", Status.DONE, epic2.getId());
        SubTask subTask2 = new SubTask("subtask 2", "description by subtask 2", Status.DONE, epic2.getId());
        manager.addSubtask(subTask1);
        manager.addSubtask(subTask2);

        manager.getEpic(epic1.getId());
        manager.getEpic(epic2.getId());
        manager.getSubtask(subTask1.getId());
        manager.getSubtask(subTask2.getId());
        manager.getTask(task1.getId());
    }

    private static void readFromFileTest() {
        loadFromFile(new File("resources/tasksHistory.csv"));

        Task task1 = taskManager.getTask(0);
        Task epic2 = taskManager.getEpic(2);
        Task subtask1 = taskManager.getSubtask(3);

//        System.out.println(task1);
//        System.out.println(epic2);
//        System.out.println(subtask1);


        List<Task> history = historyManager.getHistory();

//        List<Task> history = inMemoryHistoryManager.getHistory();
        for (Task task : history) {
            System.out.println(task);
        }


    }
}
