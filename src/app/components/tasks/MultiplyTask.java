package app.components.tasks;

import app.Main;
import app.components.matrix.M_Matrix;
import jdk.nashorn.internal.objects.annotations.Getter;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RunnableFuture;

public class MultiplyTask implements Task {
    public String input;
    private M_Matrix mat1;
    private M_Matrix mat2;
    public MultiplyTask(M_Matrix arg, M_Matrix arg1,String input) {
        this.mat1 = arg;
        this.mat2 = arg1;
        this.input=input;
    }

    @Override
    public TaskType getType() {
        return TaskType.MULTIPLY;
    }

    @Override
    public Future<M_Matrix> initiate(RecursiveTask<M_Matrix> t) {
        return Main.matrixExtractor.submit(t);
    }

    public M_Matrix getMat1() {
        return mat1;
    }

    public M_Matrix getMat2() {
        return mat2;
    }
}
