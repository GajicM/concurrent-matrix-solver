package app.components.tasks;

import app.components.matrix.M_Matrix;

import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class PoisonTask implements Task{
    @Override
    public TaskType getType() {
        return TaskType.POISON;
    }

    @Override
    public Future<M_Matrix> initiate(RecursiveTask<M_Matrix> t) {
        return null;
    }
}
