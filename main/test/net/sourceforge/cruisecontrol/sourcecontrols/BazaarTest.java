package net.sourceforge.cruisecontrol.sourcecontrols;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import net.sourceforge.cruisecontrol.CruiseControlException;
import net.sourceforge.cruisecontrol.Modification;

import org.junit.Before;
import org.junit.Test;

public class BazaarTest {

	Bazaar bzr;
	
	@Before
	public void setUp() throws Exception {
		bzr = new Bazaar();
	}
	
	@Test
	public void testParseLog() throws IOException {
		String bzrLog =   "revno: 3429 [merge]\n"
				+  "committer: Pellan Eilner <peilner@gmail.com>\n"
				+  "branch nick: schooltool\n"
				+  "timestamp: Wed 2012-07-04 03:09:26 -0400\n"
				+  "message:\n"
				+  "  new sections sheets to replace FlatSectionsTable (https://launchpad.net/bugs/1020836)\n"
				+  "  remove the ophaned old Sections exporter and tests (https://launchpad.net/bugs/1020837)\n"
				+  "  new LinkedSectionImport sheet for import only (https://launchpad.net/bugs/1020838)\n"
				+  "  import errors need to be collated and returned in a textarea (https://launchpad.net/bugs/1020839)\n"
				+  "  ResourceImporter needs to process desription cells (https://launchpad.net/bugs/1020840)\n"
				+  "  special error message for numeric values in id cells (https://launchpad.net/bugs/1020841)\n"
				+  "  exporters must skip sections with no courses (https://launchpad.net/bugs/1020842)\n"
				+  "removed:\n"
				+  "  src/schooltool/export/stests/flat_sections_table.txt\n"
				+  "  src/schooltool/export/stests/flat_sections_table.xls\n"
				+  "added:\n"
				+  "  src/schooltool/export/stests/errant_linked_section_import.xls\n"
				+  "  src/schooltool/export/stests/errant_section_sheets.xls\n"
				+  "  src/schooltool/export/stests/linked_section_import.txt\n"
				+  "  src/schooltool/export/stests/linked_section_import.xls\n"
				+  "  src/schooltool/export/stests/section_sheets.txt\n"
				+  "  src/schooltool/export/stests/section_sheets.xls\n"
				+  "modified:\n"
				+  "  CHANGES.txt\n"
				+  "  src/schooltool/export/empty_data.xls\n"
				+  "  src/schooltool/export/export.py\n"
				+  "  src/schooltool/export/importer.py\n"
				+  "  src/schooltool/export/sample_data.xls\n"
				+  "  src/schooltool/export/sample_data_small.xls\n"
				+  "  src/schooltool/export/stests/courses.txt\n"
				+  "  src/schooltool/export/stests/errant_data.txt\n"
				+  "  src/schooltool/export/templates/f_import.pt\n"
				+  "  src/schooltool/export/tests/test_export.py\n"
				+  "  src/schooltool/skin/flourish/resources/form.css\n"
				+  "    ------------------------------------------------------------\n"
				+  "    revno: 3286.2.101\n"
				+  "    committer: Pellan Eilner <peilner@gmail.com>\n"
				+  "    branch nick: schooltool\n"
				+  "    timestamp: Wed 2012-07-04 03:08:05 -0400\n"
				+  "    message:\n"
				+  "      updated CHANGES.txt\n"
				+  "    modified:\n"
				+  "      CHANGES.txt\n"
				+  "    ------------------------------------------------------------\n"
				+  "    revno: 3286.2.100\n"
				+  "    committer: Pellan Eilner <peilner@gmail.com>\n"
				+  "    branch nick: schooltool\n"
				+  "    timestamp: Wed 2012-07-04 02:43:31 -0400\n"
				+  "    message:\n"
				+  "      changed validation of all id cells to use new validation methods\n"
				+  "    modified:\n"
				+  "      src/schooltool/export/importer.py\n"
				+  "      src/schooltool/export/stests/errant_data.txt\n"
				+  "    ------------------------------------------------------------\n"
				+  "    revno: 3286.2.99\n"
				+  "    committer: Pellan Eilner <peilner@gmail.com>\n"
				+  "    branch nick: schooltool\n"
				+  "    timestamp: Wed 2012-07-04 02:02:31 -0400\n"
				+  "    message:\n"
				+  "      invalid data tests for new section sheets\n"
				+  "      reordered validation to validate as much as possible\n"
				+  "      changed invalid boolean and invalid course id list messages\n"
				+  "      fixed sorting of section export to use term start date\n"
				+  "    added:\n"
				+  "      src/schooltool/export/stests/errant_linked_section_import.xls\n"
				+  "      src/schooltool/export/stests/errant_section_sheets.xls\n"
				+  "    modified:\n"
				+  "      src/schooltool/export/export.py\n"
				+  "      src/schooltool/export/importer.py\n"
				+  "      src/schooltool/export/stests/errant_data.txt";
		
		
		
		SourceControlProperties props = new SourceControlProperties();
		final List<Modification> mods = new ArrayList<Modification>();
        props.assignPropertyName("hasChanges?");
        props.assignPropertyOnDeleteName("hasDeletions?");
        
        Bazaar.parseLog(new StringReader(bzrLog), mods, props);

        assertEquals(4, mods.size());
        
        Map<String, String> properties = props.getPropertiesAndReset();
        assertEquals("true", properties.get("hasChanges?"));
        assertEquals("true", properties.get("hasDeletions?"));
        
        Modification mod = mods.get(2);
        assertEquals("3286.2.100", mod.revision);
        assertEquals("Pellan Eilner", mod.userName);
        assertEquals("peilner@gmail.com", mod.emailAddress);
        assertEquals(2, mod.files.size());
        assertEquals("importer.py", mod.files.get(0).fileName);        
        assertEquals("errant_data.txt", mod.files.get(1).fileName);
        assertEquals("src/schooltool/export", mod.files.get(0).folderName);
        assertEquals("src/schooltool/export/stests", mod.files.get(1).folderName);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-4"));
        cal.set(2012, 6, 4, 2, 43, 31); // note month is 0-based for calendar. i.e. 6 = July
        assertEquals(cal.getTime().toString(), mod.modifiedTime.toString());

        assertEquals("changed validation of all id cells to use new validation methods", mod.comment.trim());
        
        mod = mods.get(3);
        assertEquals("added", mod.files.get(1).getAction());
        assertEquals("modified", mod.files.get(3).getAction());
        
        int totalFiles = mods.get(0).files.size() + mods.get(1).files.size() + mods.get(2).files.size() + mods.get(3).files.size();
        assertEquals(27, totalFiles);
	}

	@Test
	public void testBzrRevisionSpec() {
		Calendar cal = Calendar.getInstance();
		cal.set(2012, 4, 22, 2, 51,25);
		
		assertEquals("date:2012-05-22,02:51:25", Bazaar.bzrRevisionSpec(cal.getTime()));
	}
	
	@Test
	public void testValidate() throws IOException {
        try {
            bzr.validate();
            fail("should throw an exception when no attributes are set");
        } catch (CruiseControlException e) {
            // expected
        }
        
        bzr = new Bazaar();
        bzr.setLocalWorkingCopy("invalid directory");
        try {
            bzr.validate();
            fail("should throw an exception when an invalid "
                 + "'localWorkingCopy' attribute is set");
        } catch (CruiseControlException e) {
            // expected
        }
        
        
        File tempFile = File.createTempFile("temp", "txt");
        tempFile.deleteOnExit();

        bzr = new Bazaar();
        bzr.setLocalWorkingCopy(tempFile.getParent());
        try {
        	bzr.validate();
        } catch (CruiseControlException e) {
            fail("should not throw an exception when at least a valid "
                 + "'localWorkingCopy' attribute is set");
        }

        bzr = new Bazaar();
        bzr.setLocalWorkingCopy(tempFile.getAbsolutePath());
        try {
        	bzr.validate();
            fail("should throw an exception when 'localWorkingCopy' is "
                 + "file instead of directory.");
        } catch (CruiseControlException e) {
            // expected
        }
	}
	
	@Test
    public void testParseErrorLog() throws IOException {
		
        final List<Modification> mods = new ArrayList<Modification>();
        SourceControlProperties props = new SourceControlProperties();
        props.assignPropertyName("hasChanges?");
        props.assignPropertyOnDeleteName("hasDeletions?");
        
        Bazaar.parseLog(new StringReader("bzr: ERROR: Requested revision: 'date:2012-11-14,15:24:16' does not exist in branch:"), mods, props);
        
        assertEquals(0, mods.size());
        
        Map pm = props.getPropertiesAndReset();
        
        assertEquals(null, pm.get("hasChanges?"));
        assertEquals(null, pm.get("hasDeletions?"));
    }


}
