package app.components.tasks.threads;

import app.Main;
import app.components.matrix.M_Matrix;
import app.components.tasks.CreateTask;
import app.components.tasks.TaskQueue;
import app.components.tasks.TaskType;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class CreateTaskThread extends RecursiveTask<M_Matrix> {
    private CreateTask createTask;
    private static final int SEGMENT_SIZE = Integer.parseInt(Main.properties.getProperty(("maximum_file_chink_size")));
    private File file;
    private byte[] bytes;
    private M_Matrix matrix;
    private int start;
    private int end;
    
    public CreateTaskThread(CreateTask createTask) {
        this.createTask = createTask;
        this.file = createTask.getFile();
        this.matrix = new M_Matrix();
        this.matrix.file =file;
        start = 0;
        end = (int) file.length();
    }

    public CreateTaskThread(File file, int start, int end) {
        this.file = file;
        this.start = start;
        this.end = end;
        this.matrix = new M_Matrix();
        this.matrix.file=file;
    }


    @Override
    protected M_Matrix compute() {
        List<String> lines = new ArrayList<>();

        try {

            // If file size is smaller than threshold, read the file directly

            if (end - start <= SEGMENT_SIZE) {
               byte[] bytes=readPartOfFileAsync(file, start, end-start);

               if(bytes!=null){
                   String content = new String(bytes);
                   //   System.out.println(content);
                   lines.add(content);
               }

            } else {
                // Split the file into segments and create subtasks
                int mid = (end - start) / 2 + start ; // start = 0, end = 1001 -> mid = 500

                CreateTaskThread leftTask = new CreateTaskThread(file, start, mid);
                CreateTaskThread rightTask = new CreateTaskThread(file, mid, end);
                leftTask.fork();
                M_Matrix right = rightTask.compute();
                M_Matrix left = leftTask.join();
                lines.addAll(right.getRawData());
                lines.addAll(left.getRawData());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        matrix.setRawData(lines);
        if(createTask!=null){
            fillMatrixData();
            fillMatrixBetter();
            TaskQueue.getInstance().addTask(TaskType.MULTIPLY,matrix,matrix,"multiply "+matrix.name+","+matrix.name);
        }
        return matrix;
    }

    public static byte[] readPartOfFileAsync(File filePath, long start, long length) throws IOException, InterruptedException, ExecutionException {
        ByteBuffer buffer = null;
        int totalBytesRead = 0;
        try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath.toPath(), StandardOpenOption.READ)) {
            ByteBuffer lineSizeBuffer = ByteBuffer.allocate( 60);
            byte[] lineSizeBytes = new byte[60];
            Future<Integer> readFutureForSize = fileChannel.read(lineSizeBuffer, start);
            if(readFutureForSize.get()<=0){
                return null;
            }
            lineSizeBuffer.flip();
            for (int j = 0; j < 60; j++) {
                lineSizeBytes[j]= lineSizeBuffer.get(j);
            }
            String strin= new String(lineSizeBytes);
            int firstIndex= strin.indexOf("\n");
            int secondIndex= strin.indexOf("\n",firstIndex+1);
            Integer lineSize=secondIndex-firstIndex;
            if(lineSize<=0){
                lineSize= -lineSize;
            }
        //    System.out.println(lineSize);
            //lineSize=14; //dodajem jos 5 bajtova da bih sigurno uhvatio ceo red
            //lineSize=20;
            if(start-lineSize>0){
                ByteBuffer bufferStart = ByteBuffer.allocate( lineSize);
                byte[] startBytes = new byte[lineSize];
                Future<Integer> readFuture = fileChannel.read(bufferStart, start-lineSize);
                if(readFuture.get()<=0){

                    return null;
                }
                bufferStart.flip();
                for (int j = 0; j < lineSize; j++) {
                    startBytes[j]= bufferStart.get(j);
                }
                int lastIndexOfNL= new String(startBytes).lastIndexOf("\n");
                if(lastIndexOfNL!=-1){
                    start=start-lineSize+lastIndexOfNL;
                }
            }
            if(start+length+lineSize<=fileChannel.size()){
                ByteBuffer bufferEnd = ByteBuffer.allocate( lineSize);
                byte[] endBytes = new byte[lineSize];
                Future<Integer> readFuture = fileChannel.read(bufferEnd, start+length);
                if(readFuture.get()<=0){
                    return null;
                }
                bufferEnd.flip();
                for (int j = 0; j < lineSize; j++) {
                    endBytes[j]= bufferEnd.get(j);
                }
                int firstIndexOfNL= new String(endBytes).indexOf("\n");

                if(firstIndexOfNL!=-1){
                    length=length+firstIndexOfNL;
                }
            }
            //OVDE VIDETI DA NE BUDE STROGO 10 i 20 VEC TIPA LINE SIZE  i LINE SIZE/2
            if(fileChannel.size()-start-length<=lineSize/2){
                length+=lineSize/2;
            }


            //Iskreno ovome ni bog ne moze pomoci
            if(start+length+lineSize>=fileChannel.size()){
                //ne pitaj me nocas nista, pusti me da sutim
                length=(int) (fileChannel.size()-start);
            }
            buffer = ByteBuffer.allocate((int) length);
            while (totalBytesRead < length) {
                Future<Integer> readFuture = fileChannel.read(buffer, start + totalBytesRead);

                int bytesRead = readFuture.get();
                if (bytesRead <= 0) {
                    break;
                }
                totalBytesRead += bytesRead;
            }
        }
        if (buffer == null) {
            System.out.println("Buffer is null");
        }

        buffer.flip(); // Prepare for reading
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data); // Copy data from buffer to byte array

        return data;
    }

    private void fillMatrixData(){

        for(String lines:matrix.getRawData()){
            //  0,0 = 7129160
            String[] line=lines.split("\n");
            for(String l:line){
                if(l.isEmpty()) continue;

                if(l.contains("matrix_name")||l.contains("rows")||l.contains("cols")){
                    //   matrix_name=A1C1, rows=146, cols=146
                    System.out.println(l);
                    matrix.rows=Integer.parseInt(l.split(",")[1].split("=")[1].trim());
                    matrix.cols=Integer.parseInt(l.split(",")[2].split("=")[1].trim());
                    matrix.name=l.split(",")[0].split("=")[1];
                    continue;
                }

                String[] data=l.split(" = ");
                String[] index=data[0].split(",");
                Map<Integer,Integer> indexMap = new HashMap<>();


                //    System.out.println(l);
                try{
                    indexMap.put(Integer.parseInt(index[0].trim()),Integer.parseInt(index[1].trim()));
                    if(data.length>1)
                        matrix.matrixMap.put(indexMap,Integer.parseInt(data[1].trim()));
                    else System.out.println(l);
                }catch (Exception e){
                }

            }
        }

    }
    private void fillMatrixBetter(){
        if(matrix.name!=null && matrix.rows!=null && matrix.cols!=null){
            matrix.matrix = new BigInteger[matrix.rows][matrix.cols];
            for(Map<Integer,Integer> index:matrix.matrixMap.keySet()){
                matrix.matrix[index.keySet().toArray(new Integer[0])[0]][index.values().toArray(new Integer[0])[0]]= BigInteger.valueOf(matrix.matrixMap.get(index));
            }
            for (int i = 0; i < matrix.rows; i++) {
                for (int j = 0; j < matrix.cols; j++) {
                    if(matrix.matrix[i][j]==null){
                        matrix.matrix[i][j]= BigInteger.valueOf(0);
                    }
                }

            }




        }
    }


}



