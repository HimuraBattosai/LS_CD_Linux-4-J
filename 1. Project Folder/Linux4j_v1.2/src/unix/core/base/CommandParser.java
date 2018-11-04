package unix.core.base;
import java.util.ArrayList;
import java.util.Arrays;

import unix.core.command.cd.CdArguments;
import unix.core.command.ls.LsArguments;

public class CommandParser 
{
	private String commandType;
	private Arguments arguments;
	
	
	public CommandParser() {
		super();
	}


	public CommandParser(String commandLine, String currentDirectory) throws Exception 
	{
		ArrayList<String> secondArgument = new ArrayList<String>();
		ArrayList<String> longOptions = new ArrayList<String>();
		
		// GET The CommandType [ls, cd, cat, echo, pwd]
		this.commandType = commandLine.split(" ", 2)[0].toLowerCase();
		
		
		// GET THE ARGUMENTS LINE (Options + 2nd-Argument)
		String argumentLine="";
		if(commandLine.split(" ", 2).length > 1) argumentLine = commandLine.split(" ", 2)[1];
		
		
		// Extract [Short-Options, Long-Options, 2nd-Argument] from ARGUMENTS LINE
		String[] arguments = argumentLine.split(" ");
		String optionsString="";
		for(String argument: arguments)
		{
			if (argument.length()>1 && argument.charAt(0)=='-' && argument.charAt(1)=='-') longOptions.add(argument.substring(2));
			else if(argument.length()>0 && argument.charAt(0)=='-') optionsString += argument.substring(1);
			else secondArgument.add(argument);
		}
		
		//REMOVE Empty entries from 2nd-Argument
		secondArgument = new ArrayList<String>(Arrays.asList(
				Arrays.stream(secondArgument.toArray(new String[0])).filter(x -> !x.isEmpty()).toArray(String[]::new)));
		
		//Separate the optionString to get every single Short-Option (In the cases of ls -lA, seperate l\A)
		ArrayList<String> shortOptions = new ArrayList<String>(Arrays.asList(optionsString.split("")));
		
		//Put the Short-Options and Long-Options into ONE ARRAYLIST
		longOptions.addAll(shortOptions);
		
		//REMOVE Empty entries from Options-Array
		ArrayList<String> optionsArray = new ArrayList<String>(Arrays.asList(
						Arrays.stream(longOptions.toArray(new String[0])).filter(x -> !x.isEmpty()).toArray(String[]::new)));
		
		
		// ArgumentHandler for LS-Command => Handle the Arguments by LS-Standards and Requirements
		if(this.commandType.equals("ls")) this.arguments = new LsArguments(currentDirectory, optionsArray, secondArgument);
		
		// ArgumentHandler for CD-Command => Handle the Arguments by LS-Standards and Requirements
		if(this.commandType.equals("cd")) this.arguments = new CdArguments(currentDirectory, secondArgument);
	}
	
	
	/**************************************************SETTERS/GETTERS********************************************************/
	public String getCommandType() {
		return commandType;
	}
	public void setCommandType(String commandType) {
		this.commandType = commandType;
	}

	
	public Arguments getArguments() {
		return arguments;
	}
	public void setArguments(Arguments arguments) {
		this.arguments = arguments;
	}
	/************************************************************************************************************************/
}
