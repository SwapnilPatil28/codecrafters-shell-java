import java.io.File;
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
        String[] commands = {"exit", "echo", "type", "pwd"};
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
            else 
            {
                String execPath = getExecutablePath(program);
                if(execPath != null) 
                {
                    try 
                    {
                        ProcessBuilder pb = new ProcessBuilder(parts);
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