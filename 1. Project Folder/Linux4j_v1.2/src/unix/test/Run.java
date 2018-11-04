package unix.test;
import java.util.Scanner;

import unix.core.base.Shell;

public class Run {

	public static void main(String[] args) throws Exception 
	{
		Shell unixShell = new Shell();
		

		Scanner reader = new Scanner(System.in);
		String commandLine = null;
		
		while(1==1)
		{
			System.out.print(unixShell.getCurrentDirectory()+">");
			commandLine = reader.nextLine();
			
			unixShell.execute(commandLine);	
		}
	}

}
