package app.components.matrix;


import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//represents the matrix after operations

public class M_Matrix {
    public String name;
    public Integer rows;
    public Integer cols;
    public BigInteger[][] matrix;
    public Map<Map<Integer,Integer>,Integer> matrixMap;
    public List<String> rawData;
    public File file;
    public M_Matrix(String name, Integer rows, Integer cols) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;
        matrix = new BigInteger[rows][cols];
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                matrix[i][j]= BigInteger.valueOf(0);
            }
        }
        matrixMap = new HashMap<>();
    }
    public M_Matrix(){
        matrixMap = new HashMap<>();

    }
   // public
    public M_Matrix(List<String> lines){
        this.rawData = lines;
    }

    public void setRawData(List<String> rawData) {
        this.rawData = rawData;
    }

    public List<String> getRawData() {
        return rawData;
    }
    public void addRawData(String line){
        this.rawData.add(line);
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("matrix_name="+name);
        stringBuilder.append(", rows="+rows+", cols="+cols+"\n");
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                if(matrix[j][i].compareTo(BigInteger.ZERO)==0)
                    continue;
                stringBuilder.append(j+","+i+" = "+matrix[j][i]);
                stringBuilder.append(" \n");
            }

        }
        return stringBuilder.toString();
    }
    public String getHeader(){
        if(file==null){
            return "matrix_name="+name+", rows="+rows+", cols="+cols + "\n";
        }
        return "matrix_name="+name+", rows="+rows+", cols="+cols + ", file="+file.getAbsolutePath()+"\n";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof M_Matrix)
            if(this.name!=null && ((M_Matrix)obj).name!=null)
                    return this.name.equals(((M_Matrix)obj).name);
         return false;
    }
}
