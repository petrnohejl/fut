/**
 *
 */
package fut.client.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.dominicsayers.isemail.IsEMail;
import com.dominicsayers.isemail.dns.DNSLookupException;

import fut.client.Main;
import fut.client.common.InvalidUserException;
import fut.client.common.Props;
import fut.client.common.Session;

/**
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 29.11.2010
 */
public class Login {
    private JDialog window;
    private JTextField login;
    private JPasswordField psswd;

    public Login() {
	create();
    }

    private void create() {
	window = new JDialog();
	window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	window.setTitle("FUT-client :: " + Props.getInstance().getProperty("futclient.host", Main.propID));
	window.setIconImage(new ImageIcon("res/icon.png").getImage());
	File icon = new File(System.getProperty("user.dir"),"res");
	try {
	    window.setIconImage(
		    Toolkit.getDefaultToolkit().getImage(
			    new File(icon, "icon.png").toURI().toURL()
			    ));
	} catch (MalformedURLException ignored) {
	}

	login = new JTextField();
	psswd = new JPasswordField();

	JPanel panel = new JPanel(new BorderLayout());
	JPanel tmp = new JPanel();
	tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
	panel.add(new JLabel(
	"<html><b><i>   Welcome to the FUT, please log-in or register as new user.</i></b></html>"),
	BorderLayout.NORTH);

	tmp.add(Box.createVerticalGlue());
	tmp.add(new JLabel("E-mail : "));
	tmp.add(login);
	tmp.add(new JLabel("Password :"));
	tmp.add(psswd);
	tmp.add(Box.createVerticalGlue());

	panel.add(tmp, BorderLayout.CENTER);

	tmp = new JPanel(new FlowLayout());

	JButton button1 = new JButton("Login");
	button1.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable() {

		    @Override
		    public void run() {
			action_ok();
		    }
		});
	    }
	});
	button1.setDefaultCapable(true);

	JButton button2 = new JButton("Cancel");
	button2.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable() {

		    @Override
		    public void run() {
			action_cancel();
		    }
		});
	    }
	});

	JButton button3 = new JButton("Register");
	button3.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable() {

		    @Override
		    public void run() {
			action_register();
		    }
		});
	    }
	});

	login.addKeyListener(new KeyListener() {
	    @Override
	    public void keyTyped(KeyEvent e) {
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
	    }

	    @Override
	    public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		    psswd.requestFocus();
		}
	    }
	});

	psswd.addKeyListener(new KeyListener() {

	    @Override
	    public void keyTyped(KeyEvent e) {
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
	    }

	    @Override
	    public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		    SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
			    action_ok();
			}
		    });
		}
	    }
	});

	tmp.add(button1);
	tmp.add(button2);
	tmp.add(button3);
	panel.add(tmp, BorderLayout.SOUTH);

	window.setContentPane(panel);
	window.setModal(true);
	window.pack();
	window.setResizable(false);
	window.setLocationRelativeTo(null);
	window.setVisible(true);
    }

    private void action_ok() {
	try {
		if (login.getText().isEmpty()) {
			JOptionPane.showMessageDialog(window, "You must fill the username!", "Error", JOptionPane.OK_OPTION);
			return;
		} else if (psswd.getPassword().length == 0) {
			JOptionPane.showMessageDialog(window, "You must fill the password!", "Error", JOptionPane.OK_OPTION);
			return;
		}
	    Session.getInstance().login(login.getText(), new String(psswd.getPassword()));
	    window.setVisible(false);
	    new MainWindow();
	    window.dispose();
	} catch (UnknownHostException e) {
		JOptionPane.showMessageDialog(window, e.getLocalizedMessage(), "Error", JOptionPane.OK_OPTION);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(window, e.getLocalizedMessage(), "Error", JOptionPane.OK_OPTION);
	} catch (InvalidUserException e) {
	    JOptionPane.showMessageDialog(window, "Bad username or password", "Error", JOptionPane.OK_OPTION);
	}
    }

    private void action_cancel() {
	System.exit(0);
    }

    private void action_register() {
	String usr = login.getText();
	String pwd = new String(psswd.getPassword());
	if (usr.equalsIgnoreCase(pwd)) {
	    JOptionPane.showMessageDialog(window, "Login and password must not be the same !", "Error", JOptionPane.OK_OPTION);
	} else {

	    try {
		if (login.getText().isEmpty()) {
			JOptionPane.showMessageDialog(window, "You must fill the username!", "Error", JOptionPane.OK_OPTION);
		} else if (!IsEMail.is_email(usr)) {
			JOptionPane.showMessageDialog(window, "Invalid email !", "Error", JOptionPane.OK_OPTION);
		} else if (psswd.getPassword().length == 0) {
			JOptionPane.showMessageDialog(window, "You must fill the password!", "Error", JOptionPane.OK_OPTION);
		} else {
		    Session ses = Session.getInstance();
		    ses.register(usr, pwd);
		    JOptionPane.showMessageDialog(window, "Registration completed.", "Registration", JOptionPane.INFORMATION_MESSAGE);
		    action_ok();
		}
	    } catch (UnknownHostException e) {
	    	JOptionPane.showMessageDialog(window, e.getLocalizedMessage(), "Error", JOptionPane.OK_OPTION);
	    } catch (HeadlessException e) {
		e.printStackTrace();
	    } catch (DNSLookupException e) {
		e.printStackTrace();
	    } catch (IOException e) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(window, e.getLocalizedMessage(), "Error", JOptionPane.OK_OPTION);
	    } catch (InvalidUserException e) {
		JOptionPane.showMessageDialog(window, "Bad username or password", "Error", JOptionPane.OK_OPTION);
	    }

	}
    }
}
