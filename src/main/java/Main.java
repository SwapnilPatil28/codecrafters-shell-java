import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static String getExecutablePath(String command)
    {
        String path = System.getenv("PATH");
        if(path == null) return null;
        
        String[] pathDirs = path.split(":");
        for(int i=0; i<pathDirs.length; i++) 
        {
            File file = new File(pathDirs[i], command);
            if(file.exists() && file.canExecute()) 
            {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static String type(String command)
    {
        String[] commands = {"exit", "echo", "type", "pwd", "cd"};
        for(int i=0; i<commands.length; i++)
        {
            if(commands[i].equals(command)) 
            {
                return command + " is a shell builtin";
            }
        }
        
        String execPath = getExecutablePath(command);
        if(execPath != null) 
        {
            return command + " is " + execPath;
        }
        
        return command + ": not found";
    }

    public static void main(String[] args) throws Exception
    {
        Scanner sc = new Scanner(System.in);
        while(true)
        {
            System.out.print("$ ");
            String command = sc.nextLine();
            if(command.trim().isEmpty()) continue;

            String[] parts = command.split(" ");
            String program = parts[0];

            if(program.equals("exit")) break;
            else if(program.equals("echo"))
            {
                System.out.println((command.length()>5)?command.substring(5):"");
            }
            else if(program.equals("type"))
            {
                String nextCom = command.substring(5);
                System.out.println(type(nextCom));
            }
            else if(program.equals("pwd"))
            {
                System.out.println(System.getProperty("user.dir"));
            }
            else if(program.equals("cd"))
            {
                String pathArg = parts[1];
                String targetPath = pathArg;
                
                if(targetPath.startsWith("~"))
                {
                    targetPath = System.getenv("HOME") + targetPath.substring(1);
                }
                
                Path currentPath = Paths.get(System.getProperty("user.dir"));
                Path resolvedPath = currentPath.resolve(targetPath).normalize();
                File dir = resolvedPath.toFile();
                
                if(dir.exists() && dir.isDirectory())
                {
                    System.setProperty("user.dir", dir.getAbsolutePath());
                }
                else
                {
                    System.out.println("cd: " + pathArg + ": No such file or directory");
                }
            }
            else 
            {
                String execPath = getExecutablePath(program);
                if(execPath != null) 
                {
                    try 
                    {
                        ProcessBuilder pb = new ProcessBuilder(parts);
                        pb.directory(new File(System.getProperty("user.dir")));
                        pb.inheritIO(); 
                        
                        Process p = pb.start();
                        p.waitFor();
                    } 
                    catch (Exception e) 
                    {
                        System.out.println(e.getMessage());
                    }
                } 
                else 
                {
                    System.out.println(command + ": command not found");
                }
            }
        }
    }
}