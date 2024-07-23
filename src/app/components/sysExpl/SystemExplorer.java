package app.components.sysExpl;

import app.components.tasks.TaskQueue;
import app.components.tasks.TaskType;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class SystemExplorer implements Runnable{
    private final String START_DIR;
    private final Integer SLEEP_TIME;

    public volatile Boolean works;
    public Set<File> customDirs=new HashSet<>();
   public  HashMap<File,Long> foundFiles=new HashMap<>();
    public SystemExplorer(String startDir, Integer sleepTime){
        START_DIR = startDir;
        SLEEP_TIME = sleepTime;
        works=true;
    }
    public void addCustomStartingDir(String customStartingDir) {
        if (customStartingDir != null) {
            File customFile = new File(customStartingDir);
            Path normalizedPath = customFile.getAbsoluteFile().toPath().normalize();
            customFile = normalizedPath.toFile();

            if (customFile.exists()) {
                customDirs.add(customFile);
            } else {
                System.out.println("Directory/File does not exist");
            }
        }
    }

    @Override
    public void run() {

        File startingFile=new File(START_DIR).getAbsoluteFile();
        while(works) {
            findFilesRecursively(foundFiles, startingFile);
            if(!customDirs.isEmpty()){
                for (File customDir : customDirs) {
                    findFilesRecursively(foundFiles, customDir);
                }
            }

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    }
    private Map<File,Long> findFilesRecursively(HashMap<File,Long> totalFound, File file){
        if(file.isDirectory()){
            File[] files=file.listFiles();
            for (int i = 0; i < Objects.requireNonNull(files).length; i++) {
               totalFound.putAll(findFilesRecursively(totalFound,files[i]));
            }
        }else if(file.getName().endsWith(".rix") &&(!totalFound.containsKey(file))){
            totalFound.put(file, file.lastModified());
            System.out.println(file);
            TaskQueue.getInstance().addTask(TaskType.CREATE, file);
            //put in queue here
        }else if(totalFound.containsKey(file)){
            Optional<File> f=totalFound.keySet().stream().filter(x->x.getName().equalsIgnoreCase(file.getName())).findFirst();
          if(f.isPresent() && totalFound.get(file)!=file.lastModified()){
              totalFound.remove(file);
              totalFound.put(file, file.lastModified());
              TaskQueue.getInstance().addTask(TaskType.CREATE, file);
          }
        }
        return totalFound;
    }


    //Pretraživanje Direktorijuma: System Explorer aktivno pretražuje sve navedene direktorijume i njihove poddirektorijume na
    // osnovu podataka dobijenih iz konfiguracione datoteke. Main/CLI komponenta može dodatno da navodi direktorijume za pretragu kroz posebne komande,
    // koje System Explorer potom obilazi.



}
