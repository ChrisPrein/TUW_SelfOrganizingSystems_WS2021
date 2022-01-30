/*
 * Copyright 2004-2010 Institute of Software Technology and Interactive Systems, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.ifs.tuwien.ac.at/dm/somtoolbox/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.tuwien.ifs.somtoolbox.apps;

import org.jfree.base.Library;
import org.jfree.ui.about.Contributor;
import org.jfree.ui.about.ProjectInfo;

import at.tuwien.ifs.somtoolbox.SOMToolboxMetaConstants;
import at.tuwien.ifs.somtoolbox.apps.viewer.SOMViewer;
import at.tuwien.ifs.somtoolbox.util.CollectionUtils;

/**
 * Information about the Java SOMToolbox One instance of this class is assigned to {@link SOMToolboxApp#INFO}
 * 
 * @author Rudolf Mayer
 * @version $Id: $
 */
public class SOMToolboxInfo extends ProjectInfo {

    /** The singleton instance of the project info object. */
    private static SOMToolboxInfo singleton;

    /**
     * Returns the single instance of this class.
     * 
     * @return The single instance of information about the Java SOMToolbox.
     */
    public static synchronized SOMToolboxInfo getInstance() {
        if (singleton == null) {
            singleton = new SOMToolboxInfo();
        }
        return singleton;
    }

    private SOMToolboxInfo() {
        super("Java SOMToolbox", SOMToolboxMetaConstants.getVersion(), SOMToolboxApp.INFO_TEXT,
                SOMViewer.APPLICATION_ICON, SOMToolboxApp.DEV_BY_STRING, " Apache License, Version 2.0",
                SOMToolboxApp.LICENSE_TEXT);

        setContributors(CollectionUtils.toList(new Contributor("Rudolf Mayer", ""), //
                new Contributor("Michael Dittenbach", ""), //
                new Contributor("Jakob Frank", ""),//
                new Contributor("Thomas Lidy", "")));

        // Third-party libraries
        addLibrary(new Library("Colt ", "1.2.0 ", "Colt License ", "http://acs.lbl.gov/~hoschek/colt/"));
        addLibrary(new Library("Apache Commons Collections ", "3.2.1 ", "Apache Software License, version 2.0 ",
                "http://jakarta.apache.org/commons/collections"));
        addLibrary(new Library("Apache Commons Lang ", "2.3 ", "Apache Software License, version 2.0 ",
                "http://jakarta.apache.org/commons/lang"));
        addLibrary(new Library("Apache Commons Logging ", "1.1 ", "Apache Software License, version 2.0 ",
                "http://jakarta.apache.org/commons/logging"));
        addLibrary(new Library("Apache Commons Math ", "1.2 ", "Apache Software License, version 2.0 ",
                "http://jakarta.apache.org/commons/math"));
        addLibrary(new Library("Michael Thomas Flanagan's Java Scientific Library ", "2009/11/02 ",
                "Michael Thomas Flanagan's License ", "http://www.ee.ucl.ac.uk/~mflanaga/java/"));
        addLibrary(new Library("javax.servlet.jar ", "javax.servlet API v.3.0 ",
                "Common Development and Distribution License (CDDL) Version 1.0 ",
                "http://java.sun.com/javaee/ http://java.sun.com/products/servlet/"));
        addLibrary(new Library("jcommon.jar, jfreechart.jar ", "1.0.5, 1.0.2 ", "GNU LGPL ",
                "http://www.jfree.org/jfreechart/"));
        addLibrary(new Library("jdom-1.0.jar ", "1.0 ",
                "\"Apache style\" license (http://www.jdom.org/docs/faq.html#a0030), Apache License ",
                "http://www.jdom.org/"));
        addLibrary(new Library("jgrid ", "2005/06/08 ", "GNU LGPL 2.1 ", "http://sourceforge.net/projects/jeppers/"));
        addLibrary(new Library("jID3.jar ", "0.46 (2005/12/10) ", "GNU LGPL ", "http://jid3.blinkenlights.org/"));
        addLibrary(new Library("JLayer ", "1.0.1 ", "GNU LGPL ", "http://www.javazoom.net/javalayer/javalayer.html "));
        addLibrary(new Library("JMathArray ", "20081031 ", "BSD license ", "http://code.google.com/p/jmatharray/"));
        addLibrary(new Library("JMathPlot ", "20081031 ", "BSD license ", "http://code.google.com/p/jmathplot/"));
        addLibrary(new Library("JSAP ", "2.1 ", "GNU LGPL ", "http://www.martiansoftware.com/jsap/index.html"));
        addLibrary(new Library("Lingpipe ", "2.2.0 ", "Alias-i Royalty Free License Version 1 ",
                "http://alias-i.com/lingpipe/"));
        addLibrary(new Library("log4j ", "1.2.5 ", "Apache Software License, version 2.0 ",
                "http://logging.apache.org/log4j/"));
        addLibrary(new Library("looks ", "1.2.2 ", "BSD license ", "https://looks.dev.java.net/"));
        addLibrary(new Library("Lucene ", "2.9.1 ", "Apache Software License, version 2.0 ",
                "http://lucene.apache.org/java/docs/"));
        addLibrary(new Library("PDFBox ", "0.7.2 ", "Apache Software License, version 2.0 ",
                "http://pdfbox.apache.org/"));
        addLibrary(new Library("Piccolo, Piccolox ", "1.2 ", "BSD License ", "http://www.cs.umd.edu/hcil/piccolo/"));
        addLibrary(new Library("POI, POI scratchpad ", "2.5.1, 2.1 ", "Apache Software License, version 2.0 ",
                "http://poi.apache.org/"));
        addLibrary(new Library("Prefuse ", "beta-20071021 ", "BSD License ", "http://prefuse.org/"));
        addLibrary(new Library("Delaunay Triangulation (Voronoi) ", "August 2005 ", "Free source code ",
                "http://www.cs.cornell.edu/home/chew/Delaunay.html"));
        addLibrary(new Library("Ant XMLTask ", "1.16 ", "Apache Software License ",
                "http://www.oopsconsultancy.com/software/xmltask/"));
        addLibrary(new Library("SwingX ", "1.6 ", "GNU LGPL ", "https://swingx.dev.java.net/"));

        // External Resources
        addLibrary(new Library("Silk Icons", "1.3", "Creative Commons Attribution 2.5 License",
                "http://www.famfamfam.com/lab/icons/silk/"));
        addLibrary(new Library("Icons etc", "", "Royalty Free", "http://icons.mysitemyway.com/"));
        addLibrary(new Library("Chainlink Icon", "", "Free for commercial use (Include link to authors website)",
                "http://www.iconfinder.com/icondetails/10447/128/chain_link_web_icon"));
    }
}
