/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bgs.geophys.library.Gdas.GdasCollect;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 * @author smf
 */
public class GdasCollectVersion
{
    // there is one version number for all GdasCollect software
    public static final String VERSION_NO = "1.3";

    // resource containing release notes
    private static final String RELEASE_NOTES_RESOURCE = "ReleaseNotes.txt";

    // method to return release note information
    public static URL getReleaseNotesURL ()
    {
        return new GdasCollectVersion().getClass().getResource(RELEASE_NOTES_RESOURCE);
    }

    // method to return release note information
    public static String getReleaseNotes ()
    {
        String string, line;
        InputStream is;
        InputStreamReader isr;
        BufferedReader br;

        string = "";
        try
        {
            is = new GdasCollectVersion().getClass().getResourceAsStream (RELEASE_NOTES_RESOURCE);
            isr = new InputStreamReader(is);
            br = new BufferedReader (isr);

            while ((line = br.readLine()) != null)
                string += line + "\n";

            br.close();
            isr.close();
            is.close();
        }
        catch (IOException e) { }

        return string;
    }
}
