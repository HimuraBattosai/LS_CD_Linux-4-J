package unix.core.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import unix.core.command.cd.CdCommandBuilder;
import unix.core.command.ls.LsCommandBuilder;

public class Shell 
{
	String currentDirectory;

	public Shell() {
		super();
		this.currentDirectory = System.getProperty("user.dir");
	}
	
	public void execute(String commandLine) throws Exception
	{
		try
		{
			// Syntax Analyzer --> Parse command and get Type and Arguments
			CommandParser command = new CommandParser(commandLine, this.currentDirectory);
			
			// OUT TARGET [Exemple : ls -l > out.txt] default is Console (OutFile=null)
			PrintWriter out = this.getWriter(command.getArguments().getOutFile());
			
			// Call Builder to Handle command
			if(command.getCommandType().equals("ls")) (new LsCommandBuilder()).execute(command, out);
			if(command.getCommandType().equals("cd")) this.currentDirectory = (new CdCommandBuilder()).execute(command, this.currentDirectory);
			
			// CLOSE Writer (for File writing not Console)
			if(command.getArguments().getOutFile()!=null) out.close();
		}
		catch(Exception e) {System.out.println("'"+commandLine+"' is not recognized as an internal or external command,\r\n" + 
				"operable program or batch file.");}
	}
	
	
	
	/**************************************************SETTERS/GETTERS********************************************************/
	public String getCurrentDirectory() {
		return currentDirectory;
	}

	public void setCurrentDirectory(String currentDirectory) {
		this.currentDirectory = currentDirectory;
	}
	/************************************************************************************************************************/
	

	public PrintWriter getWriter(String outFile) throws FileNotFoundException
	{
		// if no file specified Print to console
		if (outFile==null) return new PrintWriter(System.out, true);
		else return new PrintWriter(new File(this.currentDirectory+"\\"+outFile));
	}

}
