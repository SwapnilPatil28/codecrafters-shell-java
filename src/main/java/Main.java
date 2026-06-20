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

    public static List<String> parseCommand(String command) 
    {
        List<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean inWord = false;
        boolean escaped = false;

        for(int i = 0; i < command.length(); i++) 
        {
            char c = command.charAt(i);

            if(escaped) 
            {
                sb.append(c);
                inWord = true;
                escaped = false;
            }
            else if(c == '\\' && !inSingleQuotes && !inDoubleQuotes) 
            {
                escaped = true;
            }
            else if(c == '\'' && !inDoubleQuotes) 
            {
                inSingleQuotes = !inSingleQuotes;
                inWord = true;
            } 
            else if(c == '"' && !inSingleQuotes) 
            {
                inDoubleQuotes = !inDoubleQuotes;
                inWord = true;
            }
            else if(c == ' ' && !inSingleQuotes && !inDoubleQuotes) 
            {
                if(inWord) 
                {
                    args.add(sb.toString());
                    sb.setLength(0);
                    inWord = false;
                }
            } 
            else 
            {
                sb.append(c);
                inWord = true;
            }
        }
        
        if(inWord) 
        {
            args.add(sb.toString());
        }
        
        return args;
    }

    public static void main(String[] args) throws Exception
    {
        Scanner sc = new Scanner(System.in);
        while(true)
        {
            System.out.print("$ ");
            String command = sc.nextLine();
            if(command.trim().isEmpty()) continue;

            List<String> parsedArgs = parseCommand(command);
            if(parsedArgs.isEmpty()) continue;

            String[] parts = parsedArgs.toArray(new String[0]);
            String program = parts[0];

            if(program.equals("exit")) break;
            else if(program.equals("echo"))
            {
                for(int i = 1; i < parts.length; i++) 
                {
                    System.out.print(parts[i]);
                    if (i < parts.length - 1) System.out.print(" ");
                }
                System.out.println();
            }
            else if(program.equals("type"))
            {
                if(parts.length > 1) 
                {
                    System.out.println(type(parts[1]));
                }
            }
            else if(program.equals("pwd"))
            {
                System.out.println(System.getProperty("user.dir"));
            }
            else if(program.equals("cd"))
            {
                if (parts.length > 1) 
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