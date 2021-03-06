package Utils;

import java.io.File;
import javax.swing.filechooser.FileFilter;


public class Mp3FileFilter extends FileFilter{
    
    private String fileExtension;
    private String fileDescription;
    
public Mp3FileFilter(String fileExtension,String fileDescription){
this.fileExtension=fileExtension;
this.fileDescription=fileDescription;
}
    
    @Override
    public boolean accept(File file) {
        return file.isDirectory() || file.getAbsolutePath().endsWith(fileExtension);
    }

    @Override
    public String getDescription() {
        return fileDescription+" (*."+fileExtension+")";
    }

}
