package net.sourceforge.cruisecontrol.sourcecontrols;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import net.sourceforge.cruisecontrol.CruiseControlException;
import net.sourceforge.cruisecontrol.Modification;
import net.sourceforge.cruisecontrol.SourceControl;
import net.sourceforge.cruisecontrol.sourcecontrols.Git;
import net.sourceforge.cruisecontrol.sourcecontrols.SourceControlProperties;
import net.sourceforge.cruisecontrol.util.Commandline;
import net.sourceforge.cruisecontrol.util.IO;
import net.sourceforge.cruisecontrol.util.StreamLogger;
import net.sourceforge.cruisecontrol.util.ValidationHelper;

public class Bazaar implements SourceControl {

    private static final Logger LOG = Logger.getLogger(Bazaar.class);
    
    private final SourceControlProperties props = new SourceControlProperties();
    
    private String lwc;
    
    private static final Pattern REVISIONPATTERN = Pattern.compile("revno: ([\\d\\.]*)( \\[merge\\])?");
    
    private static final Pattern COMMITTERPATTERN = Pattern.compile("committer: (.*) <(.*)>");
    
    private static final Pattern MODIFICATIONTIMEPATTERN = Pattern.compile("timestamp: (.*)");

    private static final Pattern COMMENTPATTERN = Pattern.compile("message:");
    
    private static final Pattern MODIFIEDFILEPATTERN = Pattern.compile("(added|removed|modified):");

    private static final Pattern MODSEPARATORPATTERN = Pattern.compile("-----*----");

    private static final String NEWLINE = System.getProperty("line.separator");
    
	
   
    /**
     * Returns a list of modifications detailing all the changes between the
     * last build and the latest revision in the repository.
     * 
     * @param  from date/time of last build
     * @param  to current date/time
     * @return the list of modifications, or an empty list if we failed to
     * retrieve the changes.
     */
	public List<Modification> getModifications(Date from, Date to) {
		final List<Modification> mods = new ArrayList<Modification>();
		final Commandline cmd = new Commandline();

        cmd.setExecutable("bzr");
        try {
            cmd.setWorkingDirectory(lwc);
        } catch (CruiseControlException e) {
            LOG.error("Error creating bazaar command", e);
            return mods;
        }
        cmd.createArgument("log");
        cmd.createArgument("-v"); //show files
        cmd.createArgument("--log-format=long");        
        cmd.createArgument("-r");
        
        // Bazaar returns an error if the 'to' date/time is more recent than the
        // date/time of the last commit after the 'from' date/time so we only take
        // the from date
        cmd.createArgument(bzrRevisionSpec(from) + "..");
        cmd.createArgument("--include-merges");
        
        LOG.debug("Executing command: " + cmd);
        
        try {
            final Process p = cmd.execute();
            final Thread stderr = new Thread(StreamLogger.getWarnPumper(LOG, p.getErrorStream()));
            stderr.start();
            
            parseLog(new InputStreamReader(p.getInputStream(), "UTF-8"), mods, props);
            
            p.waitFor();
            stderr.join();
            IO.close(p);
        } catch (Exception e) {
            LOG.error("Error executing bzr log command " + cmd, e);
        }
		
		return mods;
	}

	
    /**
     * This method validates that the local working copy location has been
     * specified.
     *
     * @throws CruiseControlException Thrown when the local working copy
     * location is null
     */
	public void validate() throws CruiseControlException {
        ValidationHelper.assertTrue(lwc != null, "'localWorkingCopy' is a required attribute on the bzr task");

        final File wd = new File(lwc);
        
        ValidationHelper.assertTrue(wd.exists() && wd.isDirectory(), "'localWorkingCopy' must be an existing bzr directory");
	}
	
	public Map<String, String> getProperties() {
		return props.getPropertiesAndReset();
	}
	
    public void setProperty(String p) {
        props.assignPropertyName(p);
    }
    
    public void setPropertyOnDelete(String p) {
        props.assignPropertyOnDeleteName(p);
    }

    /**
     * Sets the local working copy to use when making calls to Bazaar.
     *
     * @param d String indicating the relative or absolute path to the local
     * working copy of the bazaar repository of which to find the log history.
     */
    public void setLocalWorkingCopy(String d) {
        lwc = d;
    }
    
	static String bzrRevisionSpec(Date date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss");	
		return "date:" + df.format(date);
	}
	
	
	static void parseLog(final Reader inputReader, final List<Modification> mods, final SourceControlProperties scProps)
		throws IOException {
		final BufferedReader rd = new BufferedReader(inputReader);
		Modification mod = null;
		String modAction = null;
		boolean isComment = false;
		boolean isFileList = false;

		
        while (true) {
            String l = rd.readLine();
            
            // nothing to read?
            if (l == null) {
                break;
            } else {
            	l = l.trim();
            }
                        
            Matcher matcher = REVISIONPATTERN.matcher(l);
            if (matcher.matches()) {
           		mod = new Modification("bzr");
           		mod.revision = matcher.group(1);
                mods.add(mod);
                scProps.modificationFound();
                continue;
            }

            matcher = COMMITTERPATTERN.matcher(l);
            if (matcher.matches()) {
                mod.userName = matcher.group(1);
                mod.emailAddress = matcher.group(2);
                continue;
            }        
            
            matcher = MODIFICATIONTIMEPATTERN.matcher(l);
            if (matcher.matches()) {
            	DateFormat df = new SimpleDateFormat("E yyyy-MM-dd HH:mm:ss Z");
            	try {
					mod.modifiedTime = df.parse(matcher.group(1));
				} catch (ParseException e) {
					LOG.info("Exception while parsing date/time value " + e.getMessage());
				}
                continue;
            }        
            
            matcher = COMMENTPATTERN.matcher(l);
            if (matcher.find()) {
            	isComment = true;
            	continue;
            }
            
            matcher = MODIFIEDFILEPATTERN.matcher(l);
            if (matcher.find()) {
            	isComment = false;
            	isFileList = true;
            	
            	modAction = matcher.group(1);
            	if (modAction.equals("removed")) {
            		scProps.deletionFound();
            	}
            	continue;
            }
            
            matcher = MODSEPARATORPATTERN.matcher(l);
            if (matcher.matches()) {
            	isFileList = false;
            	continue;
            }
            
            if(isComment) {
            	mod.comment += l + NEWLINE;
            }

            /* for all kinds of modifications create file list */
            if(isFileList) {
            	File file = new File(l);
            	final Modification.ModifiedFile modFile = mod.createModifiedFile(file.getName(), file.getParent());
            	modFile.revision = mod.revision;
            	modFile.action = modAction;
            }
        }
	}
}
