package unix.core.command.ls;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import unix.core.base.CommandParser;
import unix.core.base.SpecificCommandBuilder;

public class LsCommandBuilder implements SpecificCommandBuilder
{
	public void execute(CommandParser command, PrintWriter out) throws Exception
	{
		LsArguments lsArg = (LsArguments) command.getArguments();

		//GET Argument Directories/options
		ArrayList<String> directories = lsArg.getDirectories();
		Map<String, String> options = lsArg.getOptions();
		
		// ls --help
		if (options.get("help").equals("true"))
		{
			this.printHelp();
			return;
		}
		
		//EXECUTE LS-Command on each one of the directories
		for (String directory: directories)
		{
			out.println(); //Separator-Line between each LS command
			
			LsCommand ls = new LsCommand(directory);
			
			/***************************************SEARCH-WILDCARD*****************************************/
			if (!(lsArg.getWildcards()==null)) ls = ls.searchPattern(lsArg.getWildcards());
			/***********************************************************************************************/
			
			/********************************************FILTERS********************************************/
			// -f                         do not sort, enable -aU, disable -ls --color
			if(options.get("f").equals("true"))
			{
				options=lsArg.put("a", "true");
				options=lsArg.put("U", "true");
				options=lsArg.put("l", "false");
				options=lsArg.put("s", "false");
				options=lsArg.put("color", "false");
			}
			// -d, --directory            list directories themselves, not their contents
			if((options.get("d").equals("true") || options.get("directory").equals("true") )
					&& !lsArg.getMarkDirectories()) ls = ls.directory();
			// -B, --ignore-backups       do not list implied entries ending with ~
			if(options.get("B").equals("true") || options.get("ignore-backups").equals("true")) ls = ls.ignoreBackups();
			// -A, --almost-all           do not list implied . and ..
			if((options.get("A").equals("true") || options.get("almost-all").equals("true"))
					&& options.get("a").equals("false")
					&& options.get("all").equals("false")) ls = ls.almostAll();
			// default listing			ignore entries starting with .
			if(options.get("A").equals("false") && options.get("almost-all").equals("false")
					&& options.get("a").equals("false") && options.get("all").equals("false")) ls = ls.defaultListing();
			/***********************************************************************************************/
			
			/********************************************SORTERS********************************************/
			// -U, sort=none                       do not sort; list entries in directory order
			if(!options.get("sort").equals("false") || options.get("U").equals("true"))
			{
				 options=lsArg.put("S", "false");
				 options=lsArg.put("t", "false");
				 options=lsArg.put("v", "false");
				 options=lsArg.put("X", "false");
			}
			// -c, --time=ctime    		with -l: show ctime and sort by name; otherwise: sort by ctime, newest first
			if((options.get("c").equals("true") || options.get("time").equals("ctime"))
					&& options.get("l").equals("false")) 
							options=lsArg.put("t", "true");
			// -S, sort=size            sort by file size, largest first
			if(options.get("S").equals("true") || options.get("sort").equals("size")) ls = ls.sortBySize();
			// -X, sort=extension       sort alphabetically by entry extension
			if(options.get("X").equals("true") || options.get("sort").equals("extension")) ls = ls.sortByExtension();
			// -t, sort=time            sort by modification time, newest first
			if(options.get("t").equals("true") || options.get("sort").equals("time")) ls = ls.sortByModificationTime();
			
			// -u with -l: 				show access-time and sort by name;
			if(options.get("u").equals("true") || options.get("time").equals("atime")) ls = ls.showAccessTime();
			// -u, --time=atime with -lt, without -l			sort by access-time;
			// 
			if((options.get("u").equals("true") || options.get("time").equals("atime")) 
					&& (options.get("t").equals("true") || options.get("l").equals("false")) ) 
							ls = ls.sortByAccessTime();
			
			// -r, --reverse              reverse order while sorting
			if(options.get("r").equals("true")) ls = ls.reverseSort();
			// --group-directories-first	group directories before files
			if(options.get("group-directories-first").equals("true")) ls = ls.directoriesFirst();
			/***********************************************************************************************/
			
			/***************************************FORMAT to DISPLAY***************************************/
			// --time-style=STYLE		with -l, show times using style STYLE: full-iso, long-iso, iso, locale, 
			if(options.get("time-style").equals("full-iso")
					|| options.get("time-style").equals("long-iso")
					|| options.get("time-style").equals("iso")) options=lsArg.put("full-time", "true");
			// --full-time            like -l --time-style
			if(options.get("full-time").equals("true")) ls = ls.fullTime();
			// -d */				Directories with an '/'
			if(options.get("d").equals("true") && lsArg.getMarkDirectories()) ls = ls.fileIndicator("slash");
			// -p, --indicator-style=slash		append / indicator to directories
			if(options.get("p").equals("true") || options.get("indicator-style").equals("slash")) ls = ls.fileIndicator("slash");
			// --file-type            likewise, except do not append '*'
			if(options.get("file-type").equals("true")) ls = ls.fileIndicator("file-type");
			// --block-size=SIZE      scale sizes by SIZE before printing them
			if(!options.get("block-size").equals("false")) ls = ls.sizeFormat(options.get("block-size"), 1024);
			// -h, --human-readable       with -l and/or -s, print human readable sizes
			if(options.get("h").equals("true") || options.get("human-readable").equals("true")) ls = ls.sizeFormat(null, 0);
			// --si                   likewise, but use powers of 1000 not 1024
			if(options.get("si").equals("true")) ls = ls.sizeFormat(null, 1000);
			// -F, --classify, --indicator-style=classify           append indicator (one of */=>@|) to entries  
			if(options.get("F").equals("true") || options.get("classify").equals("true") || options.get("indicator-style").equals("classify")) 
				ls = ls.fileIndicator("classify");
			// -L, --dereference          show target file information for SymLinks
			if(options.get("L").equals("true") || options.get("dereference").equals("true")) ls = ls.dereference();
			// -Q, --quote-name           enclose entry names in double quotes
			if(options.get("Q").equals("true") || options.get("quote-name").equals("true")) ls = ls.quotingStyle("c");
			// --quoting-style=WORD   	use quoting style WORD for entry names
			if(!options.get("quoting-style").equals("false")) ls = ls.quotingStyle(options.get("quoting-style"));
			// -N, --literal              print entry names without quoting
			if(options.get("N").equals("true") || options.get("literal").equals("true")) ls = ls.quotingStyle("literal");
			/***********************************************************************************************/
			
			
			/***************************************ELEMENTS to DISPLAY************************************/
			// -g                         	like -l, but do not list owner
			if(options.get("g").equals("true")) ls = ls.noOwner();
			// -o, -G                         like -l, but do not list group information
			if(options.get("G").equals("true") || options.get("o").equals("true")) ls = ls.noGroup();
			// --author               with -l, print the author of each file
			if(options.get("author").equals("true")) ls = ls.showAuthor();
			// -s, --size                 print the allocated size of each file, in blocks
			if(options.get("s").equals("true") || options.get("size").equals("true")) ls = ls.allocatedSize();
			// ls *						LIST all subdir
			if(lsArg.getListAllSubdir()) ls = ls.listSubDir();
			/***********************************************************************************************/
			
			/********************************************DISPLAY********************************************/
			// -l, --format=long, verbose                    use a long listing format
			if(options.get("l").equals("true") || options.get("format").equals("long") || options.get("format").equals("verbose")) 
				ls.longView(command, out);
			// -1, --format=single-column                    list one file per line.  Avoid '\n' with -q or -b
			else if(options.get("1").equals("true") || options.get("format").equals("single-column")) ls.singleColumnView(out);
			// -x, --format=across, horizontal               list entries by lines instead of by columns
			else if(options.get("x").equals("true") || options.get("format").equals("across") || options.get("format").equals("horizontal")) 
				ls.horizontalView("0", out);
			// -m, --format=commas                        	fill width with a comma separated list of entries
			else if(options.get("m").equals("true") || options.get("format").equals("commas")) ls.commaView(out);
			// -C, --format=vertical                        list entries by columns
			else ls.verticalView("0", out);
			/***********************************************************************************************/
			
		}
	}

	public void printHelp()
	{
		System.out.println("Usage: ls [OPTION]... [FILE]...\r\n" + 
				"List information about the FILEs (the current directory by default).\r\n" + 
				"Sort entries alphabetically if none of -cftuvSUX nor --sort is specified.\r\n" + 
				"\r\n" + 
				"Mandatory arguments to long options are mandatory for short options too.\r\n" + 
				"  -a, --all                  do not ignore entries starting with .\r\n" + 
				"  -A, --almost-all           do not list implied . and ..\r\n" + 
				"      --author               with -l, print the author of each file\r\n" + 
				"      --block-size=SIZE      scale sizes by SIZE before printing them; e.g.,\r\n" + 
				"                               '--block-size=M' prints sizes in units of\r\n" + 
				"                               1,048,576 bytes; see SIZE format below\r\n" + 
				"  -B, --ignore-backups       do not list implied entries ending with ~\r\n" + 
				"  -c                         with -lt: sort by, and show, ctime (time of last\r\n" + 
				"                               modification of file status information);\r\n" + 
				"                               with -l: show ctime and sort by name;\r\n" + 
				"                               otherwise: sort by ctime, newest first\r\n" + 
				"  -C                         list entries by columns\r\n" + 
				"  -d, --directory            list directories themselves, not their contents\r\n" + 
				"  -f                         do not sort, enable -aU, disable -ls --color\r\n" + 
				"   -F, --classify             append indicator (one of */=>@|) to entries  \r\n" + 
				"      --file-type            likewise, except do not append '*'\r\n" + 
				"      --format=WORD          across -x, commas -m, horizontal -x, long -l,\r\n" + 
				"                               single-column -1, verbose -l, vertical -C\r\n" + 
				"      --full-time            like -l --time-style=full-iso\r\n" + 
				"  -g                         like -l, but do not list owner\r\n" + 
				"      --group-directories-first\r\n" + 
				"                             group directories before files;\r\n" + 
				"                               can be augmented with a --sort option, but any\r\n" + 
				"                               use of --sort=none (-U) disables grouping\r\n" + 
				"  -G, --no-group             in a long listing, don't print group names\r\n" + 
				"  -h, --human-readable       with -l and/or -s, print human readable sizes\r\n" + 
				"                               (e.g., 1K 234M 2G)\r\n" + 
				"      --si                   likewise, but use powers of 1000 not 1024.\r\n" + 
				"      --indicator-style=WORD  append indicator with style WORD to entry names:\r\n" + 
				"                               none (default), slash (-p),\r\n" + 
				"                               file-type (--file-type), classify (-F)\r\n" + 
				"  -l                         use a long listing format\r\n" + 
				"  -L, --dereference          when showing file information for a symbolic\r\n" + 
				"                               link, show information for the file the link\r\n" + 
				"                               references rather than for the link itself\r\n" + 
				"  -m                         fill width with a comma separated list of entries\r\n" + 
				"  -N, --literal              print entry names without quoting\r\n" + 
				"  -o                         like -l, but do not list group information\r\n" + 
				"  -p, --indicator-style=slash\r\n" + 
				"                             append / indicator to directories\r\n" + 
				"  -Q, --quote-name           enclose entry names in double quotes\r\n" + 
				"      --quoting-style=WORD   use quoting style WORD for entry names:\r\n" + 
				"                               literal, locale, shell, shell-always,\r\n" + 
				"                               shell-escape, shell-escape-always, c, escape\r\n" + 
				"  -r, --reverse              reverse order while sorting\r\n" + 
				"  -s, --size                 print the allocated size of each file, in blocks\r\n" + 
				"  -S                         sort by file size, largest first\r\n" + 
				"      --sort=WORD            sort by WORD instead of name: none (-U), size (-S),\r\n" + 
				"                               time (-t), version (-v), extension (-X)\r\n" + 
				"      --time=WORD            with -l, show time as WORD instead of default\r\n" + 
				"                               modification time: atime or access or use (-u);\r\n" + 
				"                               ctime or status (-c); also use specified time\r\n" + 
				"                               as sort key if --sort=time (newest first)\r\n" + 
				"      --time-style=STYLE     with -l, show times using style STYLE:\r\n" + 
				"                               full-iso, long-iso, iso, locale, or +FORMAT;\r\n" + 
				"                               FORMAT is interpreted like in 'date'; if FORMAT\r\n" + 
				"                               is FORMAT1<newline>FORMAT2, then FORMAT1 applies\r\n" + 
				"                               to non-recent files and FORMAT2 to recent files;\r\n" + 
				"                               if STYLE is prefixed with 'posix-', STYLE\r\n" + 
				"                               takes effect only outside the POSIX locale\r\n" + 
				"  -t                         sort by modification time, newest first\r\n" + 
				"  -u                         with -lt: sort by, and show, access time;\r\n" + 
				"                               with -l: show access time and sort by name;\r\n" + 
				"                               otherwise: sort by access time, newest first\r\n" + 
				"  -U                         do not sort; list entries in directory order\r\n" + 
				"  -x                         list entries by lines instead of by columns\r\n" + 
				"  -X                         sort alphabetically by entry extension\r\n" + 
				"  -1                         list one file per line.  Avoid '\\n' with -q or -b\r\n" + 
				"      --help     display this help and exit\r\n" + 
				"\r\n" + 
				"The SIZE argument is an integer and optional unit (example: 10K is 10*1024).\r\n" + 
				"Units are K,M,G,T (powers of 1024).\r\n" + 
				"\r\n" + 
				"This is a JAVA Application simulating Linux commands in a JRE. \r\n" + 
				"This Application was made and tested in a DOS-environment with JavaSE-10.\r\n" + 
				"This is part of a selection process by INTHEFOREST Inc. [Tokyo-Japan] internship with AIESEC.\r\n" + 
				"Conception, Modeling & Development by EL OUALI Salah Eddine.\r\n" + 
				"");
	}
}
