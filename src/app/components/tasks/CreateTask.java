package app.components.tasks;

import app.Main;
import app.components.matrix.M_Matrix;

import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class CreateTask implements Task {
    private final File file;
    public CreateTask(File arg1) {
        this.file = arg1;
    }

    @Override
    public TaskType getType() {
        return TaskType.CREATE;
    }

    @Override
    public Future<M_Matrix> initiate(RecursiveTask<M_Matrix> t) {
      return Main.matrixExtractor.submit(t);
    }

    public File getFile() {
        return file;
    }
}
