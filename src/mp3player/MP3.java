package mp3player;

import Utils.FileUtils;
import java.io.Serializable;


public class MP3 implements Serializable{

    private String name;
    private String path;
    
    public MP3(String name,String path){
    this.name=name;
    this.path=path;
    }
 @Override
    // для корректного отображения объекта MP3 при добавлении в плейлист
    public String toString() {
        return FileUtils.getFileNameWithoutExtension(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MP3)) return false;
            
        MP3 mp3 = (MP3)obj;
        return path.equals(mp3.getPath());
    }
   
    
    
   
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }   
}
