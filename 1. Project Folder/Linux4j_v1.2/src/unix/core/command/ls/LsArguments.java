package unix.core.command.ls;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import unix.core.base.Arguments;

public class LsArguments implements Arguments
{
	private Map<String,String> options;
	private ArrayList<String> directories;
	
	private String outFile; 
	
	private Boolean listAllSubdir;
	private Boolean markDirectories;
	private String wildcards;
	
	
	public LsArguments() {
		super();
	}



	public LsArguments(String currentDirectory, ArrayList<String> optionsArray, ArrayList<String> secondArgument) throws Exception 
	{
		super();
		this.directories = new ArrayList<String>();
		this.listAllSubdir=false;
		this.markDirectories=false;
		this.wildcards=null;
		
		//Generate Empty LS-Options with false-value
		this.options = this.initiateLsOptions();
		
		// Set OptionsArray --> Options Map (value/boolean)
		// Ex. ls -l 					---> boolean(l) = true
		// Ex. ls --block-size=M 		---> value(block-size) = M
		for(String option : optionsArray) 
		{
			String[] optionWithParam = option.split("=");
			if(optionWithParam.length==1 || (optionWithParam.length>1 && optionWithParam[1].equals("")))
			{
				if(this.options.replace(option, "true")==null) throw new Exception();
			}
			else this.options.replace(optionWithParam[0], optionWithParam[1]);
		}
		
		
		//Get Directories, additional options and OUT-file
		Boolean isOutFile=false;
		for (String arg: secondArgument)
		{
			if(isOutFile) this.outFile=arg;
			else
			{
				//Option List allSubdirectories == ls *
				if(arg.length()==1 && arg.charAt(0)=='*') {this.listAllSubdir=true; continue;}
				
				//Option Mark Directories == ls -d */
				else if(arg.length()==2 && arg.charAt(0)=='*' && arg.charAt(1)=='/') {this.markDirectories=true; continue;}
				
				//Search with Wildcard == ls *.txt
				else if(arg.indexOf('*')>=0) {this.wildcards=arg; continue;}
				
				//Save result to outputFile == ls > out.txt
				if(arg.length()==1 && arg.charAt(0)=='>') {isOutFile=true; continue;}
				
				//Parent Directory [..] == ls ..
				if (arg.length()>1 && arg.charAt(0) == '.' && arg.charAt(1) == '.')
				{
					this.directories.add(currentDirectory+"/"+arg);
					continue;
				}
				
				//Home Directory [~ or \] == ls ~ , ls \
				if (arg.length()>0 && (arg.charAt(0) == '~' || arg.charAt(0) == '\\'))
				{
					this.directories.add(System.getProperty("user.home"));
					continue;
				}
				
				//Relative-Absolute Path == ls Desktop , ls C:/Users/
				if ( arg.charAt(0) == '/' || (arg.length()>2 && arg.charAt(1) == ':' && arg.charAt(2) == '\\'))
				{
					this.directories.add(new File(arg).getAbsolutePath());
				}
				else
				{
					this.directories.add(currentDirectory+"/"+arg);
				}
			}
		}
		
		//IF no Directory was specified, use Shell-Directory
		if (this.directories.size()==0) this.directories.add(currentDirectory);
		
	}
	
	
	
	public Map<String,String> initiateLsOptions()
	{
		Map<String,String> options = new HashMap<String,String>();
		
		options.put("a", "false"); options.put("all", "false");
		options.put("A", "false"); options.put("almost-all", "false");
		options.put("author", "false");
		// -b, --escape               
		options.put("block-size", "false");
		options.put("B", "false"); options.put("ignore-backups", "false");
		options.put("c", "false");
		options.put("C", "false");
		// --color[=WHEN]         
		options.put("d", "false"); options.put("directory", "false");
		// -D, --dired                
		options.put("f", "false");
		options.put("F", "false"); options.put("classify", "false");
		options.put("file-type", "false");
		options.put("format", "false");
		options.put("full-time", "false");
		options.put("g", "false");
		options.put("group-directories-first", "false");
		options.put("G", "false"); options.put("no-group", "false");
		options.put("h", "false"); options.put("human-readable", "false");
		options.put("si", "false");
		// -H, --dereference-command-line
		// --dereference-command-line-symlink-to-dir
		// --hide=PATTERN         
		options.put("indicator-style", "false");
		// -i, --inode        
		// -I, --ignore=PATTERN       
		// -k, --kibibytes            
		options.put("l", "false");
		options.put("L", "false"); options.put("dereference", "false");
		options.put("m", "false");
		// -n, --numeric-uid-gid      
		options.put("N", "false"); options.put("literal", "false");
		options.put("o", "false");
		options.put("p", "false");
		// -q, --hide-control-chars   
		// --show-control-chars   
		options.put("Q", "false"); options.put("quote-name", "false");
		options.put("quoting-style", "false");
		options.put("r", "false");
		// -R, --recursive            
		options.put("s", "false"); options.put("size", "false");
		options.put("S", "false");
		options.put("sort", "false");
		options.put("time", "false");
		options.put("time-style", "false");
		options.put("t", "false");
		// -T, --tabsize=COLS         
		options.put("u", "false");
		options.put("U", "false");
		// -v                         
		options.put("w", "false");
		options.put("x", "false");
		options.put("X", "false");
		// -Z, --context              
		options.put("1", "false");
		// --append-exe           
		options.put("help", "false");
		// --version  
		
		return options;
		
	}



	
	/**************************************************SETTERS/GETTERS********************************************************/
	public Map<String, String> getOptions() {
		return options;
	}
	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	public Map<String, String> put(String key, String value)
	{
		this.options.put(key, value);
		return this.options;
	}

	public ArrayList<String> getDirectories() {
		return directories;
	}
	public void setDirectories(ArrayList<String> directories) {
		this.directories = directories;
	}

	public String getOutFile() {
		return outFile;
	}
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}

	public Boolean getListAllSubdir() {
		return listAllSubdir;
	}
	public void setListAllSubdir(Boolean listAllSubdir) {
		this.listAllSubdir = listAllSubdir;
	}
	
	public Boolean getMarkDirectories() {
		return markDirectories;
	}
	public void setMarkDirectories(Boolean markDirectories) {
		this.markDirectories = markDirectories;
	}

	public String getWildcards() {
		return wildcards;
	}
	public void setWildcards(String wildcards) {
		this.wildcards = wildcards;
	}
	
	
	
}
