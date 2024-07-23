package app.components.matrix;

import app.components.tasks.TaskQueue;
import app.components.tasks.TaskType;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MatrixBrain {
    public List<M_Matrix> matrices;
    public Map<String, Future<M_Matrix>> asyncResults; //save/multiplyasync
    public List<Future<M_Matrix>> matricesFuture;
    public ExecutorService executorService = Executors.newCachedThreadPool();
    public List<String> inputs;



public MatrixBrain() {
    matrices = new CopyOnWriteArrayList<>();
    matricesFuture = new CopyOnWriteArrayList<>();
    asyncResults = new ConcurrentHashMap<>();
    inputs = new CopyOnWriteArrayList<>();
}

    public List<M_Matrix> getAllMatrices() {
        return matrices;
    }

    //kada citam iz fajla ili sinhrono mnozenje
    public void addMatrixFuture(Future<M_Matrix> matrix) {
        try {
            M_Matrix mMatrix = matrix.get();

            if (mMatrix != null && !matrices.contains(mMatrix)) {
                matrices.add(mMatrix);
                System.out.println("Matrix added " + mMatrix.getHeader());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public M_Matrix getMatrix(String mat1Name) {
        for (M_Matrix m : matrices) {
            if (m.name.equalsIgnoreCase(mat1Name)) {
                return m;
            }
        }
        return null;
    }
    public M_Matrix getMatrixByFile(String filepname) {
        for (M_Matrix m : matrices) {
            if (m.file!=null && m.file.getName().equalsIgnoreCase(filepname)) {
                return m;
            }
        }
        return null;
    }
    //TODO staviti da gleda contains po name-u
    public  void addMatrixFromMultiplication(String input, Future<M_Matrix> matrix) {

        if (input != null && input.contains("-async")) {
            asyncResults.put(input, matrix);
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    while (!matrix.isDone()) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        M_Matrix mMatrix = matrix.get();
                        if (mMatrix != null && !matrices.contains(mMatrix)) {
                            matrices.add(mMatrix);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        else {//sinhroni deo, da ne moram u task cooridnatoru ovo da pitam
            try {
                M_Matrix mMatrix = matrix.get();
                if (mMatrix != null && !matrices.contains(mMatrix)) {
                    matrices.add(mMatrix);
                    System.out.println(mMatrix.name + " calculation finished.");
                }else if(mMatrix!=null){
                    System.out.println("task done already for matrix "+mMatrix.getHeader());
                }
                else{
                   // System.out.println("Matrices cannot be multiplied");
                    return;
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public  void multiplyMatricesAsync(String input,M_Matrix mat1,M_Matrix mat2) throws ExecutionException, InterruptedException {
        if (asyncResults.containsKey(input)) {
            Future<M_Matrix> f = asyncResults.get(input);
            if(f==null){
                System.out.println("task not started yet");
            }
            else if (f.isDone()) {
                M_Matrix m = f.get();
                if (m != null && !matrices.contains(m)) {
                    matrices.add(m);
                    System.out.println(m.name + "calculation finished.");
                } else if(m!=null) {
                    System.out.println("task done already "+m.getHeader());
                } else {
                    System.out.println("Matrices cannot be multiplied");
                }
            } else {
                System.out.println("task not done yet");
            }
        } else {
            if(inputs.contains(input)){
                System.out.println("task started already");
            }else {
                inputs.add(input);
                TaskQueue.getInstance().addTask(TaskType.MULTIPLY, mat1, mat2, input);
            }
        }


    }

    public List<M_Matrix> sortAsc(List<M_Matrix> matrices) {
        matrices.sort((m1, m2) -> m1.name.compareTo(m2.name));
        return matrices;
    }
    public List<M_Matrix> sortDesc(List<M_Matrix> matrices){
        matrices.sort((m1,m2)->m2.name.compareTo(m1.name));
        return matrices;
    }
    public List<M_Matrix> getFirstN(int n,List<M_Matrix> matrices){
        if(n>matrices.size()){
            return matrices;
        }
        return matrices.subList(0,n);
    }
    public List<M_Matrix> getLastN(int n,List<M_Matrix> matrices){
        if(n>matrices.size()){
            return matrices;
        }
        return matrices.subList(matrices.size()-n,matrices.size());
    }

    public void saveMatrixToFile(M_Matrix matrix, String file) {
    executorService.submit(new Runnable() {
        @Override
        public void run() {
            if (matrices.contains(matrix)) {
                File f = new File(file);
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(f);
                    writer.println(matrix.toString());

                    writer.close();
                    System.out.println("File saved");
                    matrix.file=f;
                } catch (Exception e) {
                    System.out.println("Error saving file");
                }

            }
        }
    });

    }

    public void deleteMatrix(M_Matrix matrix) {
        matrices.remove(matrix);
        for(Map.Entry<String,Future<M_Matrix>> entry:asyncResults.entrySet()){
            if(entry.getValue().isDone()){
                try {
                    M_Matrix m=entry.getValue().get();
                    if(m.equals(matrix)){
                        asyncResults.remove(entry.getKey());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for(Future<M_Matrix> f:matricesFuture){
            if(f.isDone()){
                try {
                    M_Matrix m=f.get();
                    if(m.equals(matrix)){
                        matricesFuture.remove(f);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println("matrix deleted");
    }


}
