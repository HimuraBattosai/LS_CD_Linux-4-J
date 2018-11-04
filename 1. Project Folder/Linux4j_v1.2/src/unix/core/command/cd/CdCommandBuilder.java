package unix.core.command.cd;

import java.io.File;

import unix.core.base.CommandParser;
import unix.core.base.SpecificCommandBuilder;

public class CdCommandBuilder implements SpecificCommandBuilder {
	
	public String execute(CommandParser command, String currentDirectory) throws Exception
	{
		CdArguments cdArg = (CdArguments) command.getArguments();
		
		if(cdArg.getDirectories().size()==0) return currentDirectory;
		
		File file = new File(cdArg.getDirectories().get(0));	
		if(file.exists()) return file.getCanonicalPath();	
		else return currentDirectory;
	}

}
