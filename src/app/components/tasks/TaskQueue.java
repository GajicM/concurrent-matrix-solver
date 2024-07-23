package app.components.tasks;

import app.components.matrix.M_Matrix;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;

public class TaskQueue {
    private ArrayBlockingQueue<Task> taskQueue;
    private TaskQueue(){
        taskQueue=new ArrayBlockingQueue<>(100);
    }
    private static final TaskQueue instance=new TaskQueue();
    public static TaskQueue getInstance() {
        return TaskQueue.instance;
    }

    public void addTask(TaskType taskType, Object... args) {
        if(taskType==TaskType.MULTIPLY){
            //uzima dve matrice
            taskQueue.add(new MultiplyTask((M_Matrix)args[0],(M_Matrix)args[1],(String)args[2]));
        }
        if (taskType==TaskType.CREATE){
            //uzima fajl
            taskQueue.add(new CreateTask((File)args[0]));
        }
        if(taskType==TaskType.POISON){
            taskQueue.add(new PoisonTask());
        }

    }

    public ArrayBlockingQueue<Task> getTaskQueue() {
        return taskQueue;
    }
}
