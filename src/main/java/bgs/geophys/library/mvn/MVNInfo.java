/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgs.geophys.library.mvn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 *
 * @author smf
 */
public class MVNInfo {
    
    private final Model model;
    
    public MVNInfo (String mvn_group_id, String mvn_artifact_id) 
    throws FileNotFoundException, IOException, XmlPullParserException
    {
        // find and load values from the pom.xml
        MavenXpp3Reader reader = new MavenXpp3Reader();
        if ((new File("pom.xml")).exists())
            model = reader.read(new FileReader("pom.xml"));
        else
            model = reader.read(new InputStreamReader(
                    MVNInfo.class.getResourceAsStream(
                        "/META-INF/maven/" + mvn_group_id + "/" + mvn_artifact_id + "/pom.xml")));
    }
    
    public String getPOMProjectID () 
    { 
        if (model.getId() != null) return model.getId();
        return model.getParent().getId(); 
    }
    public String getPOMProjectGroupID () 
    { 
        if (model.getGroupId() != null) return model.getGroupId();
        return model.getParent().getGroupId(); 
    }
    public String getPOMProjectArtifactID () 
    { 
        if (model.getArtifactId() != null) return model.getArtifactId();
        return model.getParent().getArtifactId(); 
    }
    public String getPOMProjectVersion () 
    { 
        if (model.getVersion() != null) return model.getVersion();
        return model.getParent().getVersion(); 
    }
    
    public String getPOMProperty (String name) { return model.getProperties().getProperty(name); }
    
}
