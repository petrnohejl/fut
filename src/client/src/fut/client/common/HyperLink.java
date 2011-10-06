/**
 *
 */
package fut.client.common;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;


/**
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 12.12.2010
 */
public class HyperLink implements HyperlinkListener {
	//support for links
//	Log log = null;

	public HyperLink() {
//	    log = new Log(getClass());
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
	    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		if (e.getURL() != null) {
		    try {
			int n = JOptionPane.showConfirmDialog(
				null,
				"Do you really want to open following URL ?\n" +
				e.getURL().toExternalForm(),
				"Open URL ?",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (n == JOptionPane.YES_OPTION) {
			    java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
			}
		    } catch (IOException e1) {
//			log.warning("Problem opening external web page", e1);
		    } catch (URISyntaxException e2) {
//			log.warning("Problem opening external web page", e2);
		    }
		}
	    }
	}
    }

