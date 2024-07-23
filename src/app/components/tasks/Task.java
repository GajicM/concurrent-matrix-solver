package app.components.tasks;

import app.components.matrix.M_Matrix;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public interface Task  {
    TaskType getType();
    // M_Matrix matrixA, matrixB;
    // File potentialMatrixFile;

    Future<M_Matrix> initiate(RecursiveTask<M_Matrix> t);
}
