package app;

import app.components.matrix.M_Matrix;
import app.components.matrix.MatrixBrain;
import app.components.sysExpl.SystemExplorer;
import app.components.coordinator.TaskCooridnatorThread;
import app.components.tasks.TaskQueue;
import app.components.tasks.TaskType;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static final Properties properties = readProperties();
    public static SystemExplorer systemExplorer;
    public static TaskCooridnatorThread taskCooridnatorThread;
    public static MatrixBrain matrixBrain;
    public static ForkJoinPool matrixExtractor;
    public static ForkJoinPool matrixMultiplier;


    public static void main(String[] args) {
        systemExplorer = new SystemExplorer(properties.getProperty("start_dir"), Integer.valueOf(properties.getProperty("sys_explorer_sleep_time")));
        Thread systemExplorerThread = new Thread(systemExplorer);
        systemExplorerThread.start();
        matrixExtractor = new ForkJoinPool();
        matrixBrain = new MatrixBrain();
        taskCooridnatorThread = new TaskCooridnatorThread();
        taskCooridnatorThread.start();
        matrixMultiplier = new ForkJoinPool();
        while (true) {
            try{
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("stop")) {
                    System.out.println("Stopping...");
                    systemExplorer.works = false;
                    TaskQueue.getInstance().addTask(TaskType.POISON);
                    matrixBrain.executorService.shutdown();
                    matrixMultiplier.shutdown();
                    matrixExtractor.shutdown();
                    break;
                } else
                if (input.startsWith("dir ") && input.length() > 4) {
                    systemExplorer.addCustomStartingDir(input.substring(4));
                }
                else if (input.startsWith("multiply") && input.substring(8).contains(",")) {
                    String[] x = input.split("[, ]");
                    String mat1Name = x[1].trim();
                    String mat2Name = x[2].trim();
                    M_Matrix mat1 = matrixBrain.getMatrix(mat1Name);
                    M_Matrix mat2 = matrixBrain.getMatrix(mat2Name);
                    if(mat1==null || mat2==null){
                        System.out.println("Matrix not found");
                        continue;
                    }
                    if (input.contains("-async")) {
                        //multiply matrices asynchronously
                        Main.matrixBrain.multiplyMatricesAsync(input,mat1,mat2);
                    } else {
                        TaskQueue.getInstance().addTask(TaskType.MULTIPLY, mat1, mat2, input);
                    }
                    //multiply matrices

                }
                else if (input.startsWith("info")) {
                    if (input.contains("-all")) {
                        List<M_Matrix> result = matrixBrain.getAllMatrices();
                        if (input.contains("-asc")) {
                            result = matrixBrain.sortAsc(result);
                        }
                        if (input.contains("-desc")) {
                            result = matrixBrain.sortDesc(result);
                        }
                        if (input.contains("-s")) {
                            String num = input.substring(input.indexOf("-s") + 2);
                            String[] x = num.split("[, -]");
                            String num12="0";
                            for(String s:x){
                                if(s!=null &&!s.isEmpty() ){
                                    num12=s;
                                    break;
                                }
                            }
                            int numberOfFiles = Integer.parseInt(num12.trim());
                            result = matrixBrain.getFirstN(numberOfFiles, result);
                        } else if (input.contains("-e")) {
                            String num = input.substring(input.indexOf("-e") + 2);
                            String[] x = num.split("[, -]");
                            String num12="0";
                            for(String s:x){
                                if(s!=null &&!s.isEmpty() ){
                                    num12=s;
                                    break;
                                }
                            }
                            int numberOfFiles = Integer.parseInt(num12.trim());
                            result = matrixBrain.getLastN(numberOfFiles, result);
                        }
                        for (M_Matrix m : result) {
                            System.out.println(m.name + " | rows = " + m.rows + " | cols=" + m.cols + " | " + m.file);
                        }

                    }else {
                        String name = input.substring(5).trim();
                        M_Matrix matrix = matrixBrain.getMatrix(name);
                        if (matrix != null) {
                            System.out.println(matrix.getHeader());
                        } else {
                            System.out.println("Matrix not found");
                        }
                    }

                }


               else  if (input.startsWith("save") && input.contains("-name") && input.contains("-file")) {
                    int indexOfName = input.indexOf("-name");
                    int indexOfFile = input.indexOf("-file");
                    if (indexOfFile < indexOfName) {

                    } else {
                        String name = input.substring(input.indexOf("-name") + 5, input.indexOf("-file")).trim();
                        String file = input.substring(input.indexOf("-file") + 5).trim();
                        M_Matrix matrixToBeSaved = matrixBrain.getMatrix(name);
                        matrixBrain.saveMatrixToFile(matrixToBeSaved, file);
                    }


                }
               else if (input.startsWith("clear")) {
                    String name = input.substring(6).trim();
                    M_Matrix matrixToBeDeleted=null;
                    try {
                        if (name.contains(".rix")) {
                            matrixToBeDeleted = matrixBrain.getMatrixByFile(name);
                        } else {
                            matrixToBeDeleted = matrixBrain.getMatrix(name);
                        }
                        if (matrixToBeDeleted != null) {
                            matrixBrain.deleteMatrix(matrixToBeDeleted);
                            systemExplorer.foundFiles.remove(matrixToBeDeleted.file);
                        }
                        else
                            System.out.println("Matrix not found");
                    }catch (Exception e){
                        System.out.println("Matrix not found");
                        System.out.println(e.getMessage());
                    }

                }else{
                    System.out.println("Invalid input");
                }
            }catch (Exception e){
                System.out.println("Invalid input");
            }

        }


    }


    public static Properties readProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("src\\resources\\application.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}




