/**
 *
 */
package fut.client;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import fut.client.common.Props;
import fut.client.gui.Login;

/**
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 29.11.2010
 */
public class Main {
    public  static String propID = "fut_client";

    public static void main(String[] args) {
        initProperties();
        initGui();
    }

    private static void initGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            //ignored
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Login();
            }
        });
    }


    private static void initProperties() {
        Props prop = Props.getInstance();
        File root = new File(System.getProperty("user.dir"));
        File file = new File(new File(root, "conf"), "fut_client.properties");

        if (file.exists()) {
            try {
                prop.loadProperty(file, propID);
            } catch (IOException e) {
                loadDefaults(false);
            }
        } else {
            loadDefaults(true);
        }
        resolveProperties();
    }


    private static void loadDefaults(boolean save) {
        Props prop = Props.getInstance();

        prop.setProperty("futclient.host", "127.0.0.1", propID);
        prop.setProperty("futclient.port", "12345", propID );

        if (save) {

            File root = new File(System.getProperty("user.dir"), "conf");
            File tmp = new File(root, "fut_client.properties");
            try {
                if (!root.exists()) {
                    if (root.mkdir()) {
                        prop.store(tmp, propID);
                    }
                } else {
                    prop.store(tmp, propID);
                }
            } catch (IOException emptyCatch) {
                //not interesting, still able to continue
            }
        }

    }

    private static void resolveProperties() {

        Props prop = Props.getInstance();
        File root = new File(System.getProperty("user.dir"));
        File file = null;
        String tmp = "";
        String tmp2 = "";
        Pattern link = Pattern.compile("\\$\\{(.*?)\\}"); //matches ${*}, eg.  ${db.file}
        Matcher match = null;

        // ############ datatype check ################
        //test if port is integer
        tmp = prop.getProperty("futclient.port", "12345", propID);
        try {
            Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            prop.setProperty("futserver.port", "12345", propID);
        }

        //###########  links resolving #########################

        Enumeration<?> en = prop.getPropertyNames(propID);
        for (String key = ""; en.hasMoreElements(); key = (String) en.nextElement()) {

            if ((tmp2 = prop.getProperty(key, propID)) != null) {
                match = link.matcher(tmp2);

                if (match.find()) {
                    for (int i = 1; i <= match.groupCount(); i++) {
                        tmp2 = tmp2.replaceAll("\\$\\{" + match.group(i) + "\\}", prop.getProperty(match.group(i), propID));
                    }
                    prop.setProperty(key, tmp2, propID);
                }
            }
        }



//        // ########### relativity ##################
//        //resolve all relative dirs/files to absolute paths
//        tmp = prop.getProperty("futserver.log.config", "log_file.properties", propID);
//        file = new File(new File(root, "conf"), tmp);
//        prop.setProperty("futserver.log.config", file.getAbsolutePath(), propID);

        //where is 'conf' dir ?
        prop.setProperty("futclient.confdir", new File(root, "conf").getAbsolutePath(), propID);
    }


}
