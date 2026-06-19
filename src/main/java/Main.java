import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        while(true)
        {
            System.out.print("$ ");
            String s = sc.nextLine();
            if(s.equals("exit"))   break;
            else if(s.startsWith("echo "))
            {
                System.out.println((s.length()>5)?s.substring(5):"");
            }
            else if(s.startsWith("type "))
            {
                if(s.substring(5).equals("exit")||s.substring(5).equals("echo")||s.substring(5).equals("type"))     System.out.println(s.substring(5)+" is a shell builtin");
                else System.out.println(s.substring(5)+": not found");
            }
            else System.out.println(s+": command not found");
        }
    }
}