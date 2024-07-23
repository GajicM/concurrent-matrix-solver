package app.components.coordinator;

import app.Main;
import app.components.matrix.M_Matrix;
import app.components.tasks.*;
import app.components.tasks.threads.CreateTaskThread;
import app.components.tasks.threads.MultiplyTaskThread;

import java.util.concurrent.Future;

public class TaskCooridnatorThread extends Thread {

    public TaskCooridnatorThread() {
    }

    @Override
    public void run() {
        Task currentTask= TaskQueue.getInstance().getTaskQueue().poll();
         while(true){

            while(currentTask!=null){


                if(currentTask.getType()== TaskType.CREATE){
                    CreateTask createTask=(CreateTask)currentTask;

                    Future<M_Matrix> result=createTask.initiate(new CreateTaskThread(createTask));
                    Main.matrixBrain.addMatrixFuture(result);


                }
                else if(currentTask.getType()==TaskType.MULTIPLY){
                    MultiplyTask multiplyTask=(MultiplyTask)currentTask;

                    Future<M_Matrix> result=multiplyTask.initiate(new MultiplyTaskThread(multiplyTask));
                    Main.matrixBrain.addMatrixFromMultiplication(multiplyTask.input,result);

                }
                else if(currentTask.getType()==TaskType.POISON){ //ovo bi trebalo da ga zavrsi
                    break;
                }


                currentTask=TaskQueue.getInstance().getTaskQueue().poll();
            }
             if(currentTask!=null && currentTask.getType()==TaskType.POISON){ //ovo bi trebalo da ga zavrsi
                 break;
             }
            while(currentTask==null){
                try {
                    Thread.sleep(2000);
                    currentTask=TaskQueue.getInstance().getTaskQueue().poll();


                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }
}
