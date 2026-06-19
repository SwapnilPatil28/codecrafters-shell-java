import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        while(true)
        {
            System.out.print("$ ");
            String s = sc.next();
            if(s.equals("exit"))   break;
            if(s.equals("echo"))
            {
                String s1 = sc.nextLine();
                System.out.println(s1);
                continue;
            }
            System.out.println(s+": command not found");
        }
    }
}
