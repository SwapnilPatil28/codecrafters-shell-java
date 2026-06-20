import java.io.File;
import java.util.Scanner;

public class Main {

    public static String type(String command)
    {
        String[] commands = {"exit", "echo", "type"};
        String path = System.getenv("PATH");
        String[] pathDirs = path.split(":");
        boolean isBuiltIn = false;
        for(int i=0; i<commands.length; i++)
        {
            if(commands[i].equals(command)) 
            {
                return command + " is a shell builtin";
            }
        }
        for(int i=0; i<pathDirs.length; i++) 
        {
            File file = new File(pathDirs[i], command);
            if(file.exists() && file.canExecute()) 
            {
                return command + " is " + file.getAbsolutePath();
            }
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
            if(command.equals("exit"))   break;
            else if(command.startsWith("echo"))
            {
                System.out.println((command.length()>5)?command.substring(5):"");
            }
            else if(command.startsWith("type"))
            {
                String nextCom = command.substring(5);
                System.out.println(type(nextCom));
            }
            else System.out.println(command+": command not found");
        }
    }
}