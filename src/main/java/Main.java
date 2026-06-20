import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static String getExecutablePath(String command)
    {
        if(command.contains("/")) 
        {
            File file = new File(command);
            if(file.exists() && file.canExecute()) 
            {
                return file.getAbsolutePath();
            }
            return null;
        }

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
        String[] commands = {"exit", "echo", "type", "pwd", "cd", "jobs"};
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
            else if(c == '\\') 
            {
                if(inSingleQuotes) 
                {
                    sb.append(c);
                    inWord = true;
                } 
                else if(inDoubleQuotes) 
                {
                    if(i + 1 < command.length()) 
                    {
                        char next = command.charAt(i + 1);
                        if(next == '\\' || next == '"' || next == '$' || next == '\n') 
                        {
                            sb.append(next);
                            i++;
                            inWord = true;
                        } 
                        else 
                        {
                            sb.append(c);
                            inWord = true;
                        }
                    } 
                    else 
                    {
                        sb.append(c);
                        inWord = true;
                    }
                } 
                else 
                {
                    escaped = true;
                }
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

            String outputFile = null;
            String errorFile = null;
            boolean appendOutput = false;
            boolean appendError = false;
            
            for(int i = 0; i < parsedArgs.size(); i++) 
            {
                String arg = parsedArgs.get(i);
                if(arg.equals(">") || arg.equals("1>")) 
                {
                    if(i + 1 < parsedArgs.size()) 
                    {
                        outputFile = parsedArgs.get(i + 1);
                        appendOutput = false;
                        parsedArgs.remove(i + 1);
                        parsedArgs.remove(i);
                        i--;
                    }
                }
                else if(arg.equals(">>") || arg.equals("1>>")) 
                {
                    if(i + 1 < parsedArgs.size()) 
                    {
                        outputFile = parsedArgs.get(i + 1);
                        appendOutput = true;
                        parsedArgs.remove(i + 1);
                        parsedArgs.remove(i);
                        i--;
                    }
                }
                else if(arg.equals("2>")) 
                {
                    if(i + 1 < parsedArgs.size()) 
                    {
                        errorFile = parsedArgs.get(i + 1);
                        appendError = false;
                        parsedArgs.remove(i + 1);
                        parsedArgs.remove(i);
                        i--;
                    }
                }
                else if(arg.equals("2>>")) 
                {
                    if(i + 1 < parsedArgs.size()) 
                    {
                        errorFile = parsedArgs.get(i + 1);
                        appendError = true;
                        parsedArgs.remove(i + 1);
                        parsedArgs.remove(i);
                        i--;
                    }
                }
            }

            String[] parts = parsedArgs.toArray(new String[0]);
            if(parts.length == 0) continue;
            String program = parts[0];

            PrintStream out = System.out;
            if(outputFile != null) 
            {
                File f = new File(outputFile);
                if(f.getParentFile() != null && !f.getParentFile().exists()) 
                {
                    f.getParentFile().mkdirs();
                }
                out = new PrintStream(new FileOutputStream(f, appendOutput));
            }

            PrintStream err = System.err;
            if(errorFile != null) 
            {
                File f = new File(errorFile);
                if(f.getParentFile() != null && !f.getParentFile().exists()) 
                {
                    f.getParentFile().mkdirs();
                }
                err = new PrintStream(new FileOutputStream(f, appendError));
            }

            if(program.equals("exit")) 
            {
                if(out != System.out) out.close();
                if(err != System.err) err.close();
                break;
            }
            else if(program.equals("echo"))
            {
                for (int i = 1; i < parts.length; i++) 
                {
                    out.print(parts[i]);
                    if (i < parts.length - 1) out.print(" ");
                }
                out.println();
            }
            else if(program.equals("type"))
            {
                if (parts.length > 1) 
                {
                    out.println(type(parts[1]));
                }
            }
            else if(program.equals("pwd"))
            {
                out.println(System.getProperty("user.dir"));
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
                        err.println("cd: " + pathArg + ": No such file or directory");
                    }
                }
            }
            else if(program.equals("jobs"))
            {
                
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
                        
                        if(outputFile != null || errorFile != null) 
                        {
                            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                            
                            if(outputFile != null) 
                            {
                                if(appendOutput) pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(outputFile)));
                                else pb.redirectOutput(new File(outputFile));
                            }
                            else 
                            {
                                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                            }
                            
                            if(errorFile != null) 
                            {
                                if(appendError) pb.redirectError(ProcessBuilder.Redirect.appendTo(new File(errorFile)));
                                else pb.redirectError(new File(errorFile));
                            }
                            else 
                            {
                                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                            }
                        } 
                        else 
                        {
                            pb.inheritIO(); 
                        }
                        
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
                    err.println(command + ": command not found");
                }
            }

            if(out != System.out) 
            {
                out.close();
            }
            if(err != System.err) 
            {
                err.close();
            }
        }
    }
}