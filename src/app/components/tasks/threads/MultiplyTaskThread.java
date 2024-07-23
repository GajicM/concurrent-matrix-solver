package app.components.tasks.threads;

import app.Main;
import app.components.matrix.M_Matrix;
import app.components.tasks.MultiplyTask;

import java.util.concurrent.RecursiveTask;

public class MultiplyTaskThread extends RecursiveTask<M_Matrix> {
    MultiplyTask multiplyTask;
    private static final int SEGMENT_SIZE = Integer.parseInt(Main.properties.getProperty(("maximum_rows_size")));
    private int start;
    private int end;
    private M_Matrix mat1, mat2, result;

    public MultiplyTaskThread(MultiplyTask multiplyTask) {
        mat1 = multiplyTask.getMat1();
        mat2 = multiplyTask.getMat2();
        start = 0;
        end = mat1.rows;
        if (multiplyTask.input.contains("-name")) {
            String sub = multiplyTask.input.substring(multiplyTask.input.indexOf("-name") + 5);
            String matName = sub.split("[- -]")[1].trim();
            result = new M_Matrix(matName, mat1.rows, mat2.cols);
        }else
            result = new M_Matrix(mat1.name + mat2.name, mat1.rows, mat2.cols);
        if(!multiplyTask.input.contains("-async"))
            System.out.println("Calculating "+mat1.name + " x "+mat2.name);
    }

    public MultiplyTaskThread(int start, int end, M_Matrix mat1, M_Matrix mat2, M_Matrix result) {
        this.start = start;
        this.end = end;
        this.mat1 = mat1;
        this.mat2 = mat2;
        this.result = result;
    }

    /*Kada se pokrene zadatak za množenje matrica, potrebno je navesti dve matrice, A i B, koje će biti pomnožene.
     Matrix Multiplier koristi thread pool da deli zadatak množenja na manje segmente dok segment obrade ne bude dovoljno mali da ne premašuje definisani limit.
     U konfiguracionoj datoteci se navodi minimalan (preporučen) broj redova odnosno kolona koji je prihvatljiv za množenje od strane jedne niti.
    Svaka nit u pool-u zadužena je za izračunavanje određenog segmenta konačne matrice, bazirano na podelama matrica A i B.
    Nakon što su svi segmenti obrađeni, rezultati se kombinuju u novu matricu, koja se potom prosleđuje Matrix Brain komponenti za dalje korišćenje ili skladištenje.
    */
    @Override
    protected M_Matrix compute() {
        if (!mat1.rows.equals(mat2.cols) && !mat1.cols.equals(mat2.rows)) {
            System.out.println("Matrices "+mat1.name + ","+mat2.name+" cannot be multiplied");
            return null;
        }

        if (end - start <= SEGMENT_SIZE) {
            //logika za mnozenje
            multiplyRows(start, end);
        } else {
            // Split the file into segments and create subtasks
            int mid = (end - start) / 2 + start; // start = 0, end = 1001 -> mid = 500
            MultiplyTaskThread leftTask = new MultiplyTaskThread(start, mid, mat1, mat2, result);
            MultiplyTaskThread rightTask = new MultiplyTaskThread(mid, end, mat1, mat2, result);
            leftTask.fork();
            M_Matrix right = rightTask.compute();
            M_Matrix left = leftTask.join();
        }


     /*   if(createTask!=null){
            fillMatrixData();
            fillMatrixBetter();
            TaskQueue.getInstance().addTask(TaskType.MULTIPLY,matrix,matrix);
        }*/
        //logika za spajanje rezultata
        return result;
    }

    private void multiplyRows(int start, int end) {
        int cols1 = mat1.matrix[0].length;
        int cols2 = mat2.matrix[0].length;

//        if (cols1 != mat2.matrix.length) {
//            throw new IllegalArgumentException("Number of columns in the first matrix must be equal to the number of rows in the second matrix.");
//        }
        int i=-1, j = -1, k=-1;
        try {

            for (i = start; i < end; i++) {
                for ( j = 0; j < cols2; j++) {
                    for (k = 0; k < cols1; k++) {

                        result.matrix[i][j] =  result.matrix[i][j].add(mat1.matrix[i][k].multiply( mat2.matrix[k][j]));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" i = "+i+ " j = "+ j+" k = "+k);
        }

    }
}



