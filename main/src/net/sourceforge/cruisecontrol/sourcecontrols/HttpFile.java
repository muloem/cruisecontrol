/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001, ThoughtWorks, Inc.
 * 651 W Washington Ave. Suite 600
 * Chicago, IL 60661 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package net.sourceforge.cruisecontrol.sourcecontrols;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import net.sourceforge.cruisecontrol.CruiseControlException;
import net.sourceforge.cruisecontrol.Modification;
import net.sourceforge.cruisecontrol.SourceControl;

import org.apache.log4j.Logger;

/**
 * Checks a single file on a web server that supports the last-modified header
 *
 * @author <a href="mailto:yourgod@users.sourceforge.net">Brad Clarke</a>
 */
public class HttpFile implements SourceControl {
    private static Logger log = Logger.getLogger(HttpFile.class);
    private String urlString;

    public void setProperty(String unused) {
        throw new UnsupportedOperationException("attribute 'property' is not supported");
    }

    public void setPropertyOnDelete(String unused) {
        throw new UnsupportedOperationException("attribute 'propertyOnDelete' is not supported");
    }

    public void setURL(String urlString) {
        this.urlString = urlString;
    }

    public Hashtable getProperties() {
        return new Hashtable();
    }

    public void validate() throws CruiseControlException {
        if (urlString == null) {
            throw new CruiseControlException("'url' is a required attribute for HttpFile");
        }
        try {
            new URL(this.urlString);
        } catch (MalformedURLException e) {
            throw new CruiseControlException("'url' is not a valid connection string", e);
        }
    }

    /**
     * For this case, we don't care about the quietperiod, only that
     * one user is modifying the build.
     *
     * @param lastBuild date of last build
     * @param now IGNORED
     */
    public List getModifications(Date lastBuild, Date now) {
        long lastModified;
        final URL url;
        try {
            url = new URL(this.urlString);
        } catch (MalformedURLException e) {
            // already checked
            return new ArrayList();
        }
        try {
            final URLConnection con = url.openConnection();
            lastModified = con.getLastModified();
            con.getInputStream().close();
        } catch (IOException e) {
            log.error("Could not connect to 'url'", e);
            return new ArrayList();
        }
        List modifiedList = new ArrayList();
        if (lastModified > lastBuild.getTime()) {
            Modification mod = new Modification();
            mod.userName = "User";
            mod.fileName = url.getFile();
            mod.modifiedTime = new Date(lastModified);
            mod.comment = "";
            modifiedList.add(mod);
        }
        return modifiedList;
    }
}