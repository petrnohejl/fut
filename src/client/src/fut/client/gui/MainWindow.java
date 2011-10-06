/**
 *
 */
package fut.client.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import fut.client.Main;
import fut.client.common.FileTableModel;
import fut.client.common.HyperLink;
import fut.client.common.Props;
import fut.client.common.Session;
import fut.client.net.Net;
import fut.common.DeviceObject;
import fut.common.FileObject;
import fut.common.GeodataObject;

/**
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 3.12.2010
 */
public class MainWindow extends JFrame{
    //http://code.google.com/intl/sk-SK/apis/maps/documentation/mapsdata/developers_guide_java.html
    //http://code.google.com/intl/sk-SK/apis/maps/documentation/staticmaps/
    private static final String panel_info = "INFO";
    private static final String panel_geolocation = "GEOLOCATION";
    private static final String panel_browsefiles = "BROWSEFILES";

    private JPanel content;
    private JTextPane text;
    private JTextPane map;
    private JLabel status;
    private JTable filesTable;
    private FileTableModel filesModel;

    private int numFiles = 5;
    private String fileType = "" ;


    private ActionListener numberOfRecords_radioButtonListener = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	    final int numGeoLocations  = Integer.valueOf(e.getActionCommand());
	    SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    //	            http://maps.google.com/maps/api/staticmap?center=Brooklyn+Bridge,New+York,NY&zoom=14&size=512x512&maptype=roadmap
		    //	        	&markers=color:blue|label:S|40.702147,-74.015794&markers=color:green|label:G|40.711614,-74.012318
		    //	        	&markers=color:red|color:red|label:C|40.718217,-73.998284&sensor=false
		    setStatus("Generating map...");
		    Net net = Session.getInstance().getNetwork();
		    List<GeodataObject> data = net.getGeodata(Session.getInstance().getUsername(), numGeoLocations);
		    StringBuilder sb = new StringBuilder(1024);

		    if (data == null ) {
			sb.append("<html><br><br><Strong>No data available !</strong></html>");
		    } else {
			StringBuilder sb_path = new StringBuilder(1024);
			sb.append("<html><center><img src='http://maps.google.com/maps/api/staticmap?");
			sb.append("&sensor=false&size=800x600&format=png&maptype=roadmap"); //&zoom=14
			sb_path.append("&path=color:blue|weight:4");
			for (GeodataObject go : data ) {
			    sb.append("&markers=color:blue|shadow:false|").append(go.getLatitude()).append(",").append(go.getLongitude());
			    sb_path.append("|").append(go.getLatitude()).append(",").append(go.getLongitude());
			}

			sb.append(sb_path);
			sb.append("'></center></html>");
		    }

		    map.setText(sb.toString());
		    setStatus("Ready");
		}
	    });
	}
    };

    private ActionListener numberOfFiles_radioButtonListener = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	    numFiles = Integer.valueOf(e.getActionCommand());
	}
    };

    private ActionListener exitListener = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	    exitApplication();
	}
    };

    private ActionListener device_info = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	    SwingUtilities.invokeLater(new Runnable() {

		@Override
		public void run() {
		    device_info();
		}
	    });
	}
    };

    private ActionListener device_settings = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	    SwingUtilities.invokeLater(new Runnable() {

		@Override
		public void run() {
		    device_settings();
		}
	    });
	}
    };

    private ActionListener device_stolen = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	    final JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
	    SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    device_stolen(item);
		}
	    });
	}
    };

    private ActionListener device_browseFiles = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	    showCard(panel_browsefiles);
	}
    };

    private ActionListener device_geoLocation = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	    SwingUtilities.invokeLater(new Runnable() {

		@Override
		public void run() {
		    device_geoLocation();
		}
	    });
	}
    };

    private ActionListener device_setAlarm = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	    final JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
	    SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    device_setAlarm(item);
		}
	    });
	}
    };

    private ActionListener fileTypes_comboBoxListener = new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	    String item = (String) ((JComboBox)e.getSource()).getModel().getSelectedItem();

	    if (item == null || item.startsWith("<")) {
		fileType = "";
	    }else {
		fileType = item.toLowerCase();
	    }
	}
    };

    public MainWindow() {
	createGui();
	setStatus("Ready");
    }

    private void exitApplication() {
	setVisible(false);
	System.exit(0);
    }

    private void device_info() {
	setStatus("Retrieving device info...");
	DeviceObject device;
	StringBuilder sb = new StringBuilder(1024);
	Net net = Session.getInstance().getNetwork();
	List<DeviceObject> devices = net.getDevice(Session.getInstance().getUsername());
	boolean isAlarm = net.getAlarm(Session.getInstance().getUsername());

	text.setText("");
	showCard(panel_info);
	if (devices != null) {
	    sb.append("<html><center><h1><i>Device info</i></h1>");

	    if (devices.size() > 0) {
		for (int i=0; i<devices.size() ; i++ ) {
		    device = devices.get(i);
		    sb.append("<br><br><strong>Device #").append(i+1).append("</strong><br>");
		    sb.append("<table border='1' cellpadding='5'>");
		    sb.append("<tr><td>Device ID : </td><td>").append(device.getId()).append("</td></tr>");
		    sb.append("<tr><td><a href='http://en.wikipedia.org/wiki/IMEI'>IMEI</a> : </td><td>").append(device.getImei()).append("</td></tr>");
		    sb.append("<tr><td><a href='http://en.wikipedia.org/wiki/IMSI'>IMSI</a> : </td><td>").append(device.getImsi()).append("</td></tr>");
		    sb.append("<tr><td>Sim : </td><td>").append(device.getSim()).append("</td></tr>");
		    sb.append("<tr><td>Operator* : </td><td>").append(device.getOperator()).append("</td></tr>");
		    sb.append("<tr><td>Stolen ? : </td><td>").append(device.isStolen()).append("</td></tr>");
		    sb.append("<tr><td>Alarm ? : </td><td>").append(isAlarm).append("</td></tr>");
		    sb.append("</table>");
		    sb.append("* = <a href='http://en.wikipedia.org/wiki/MCC'>MCC</a>");
		    sb.append(" + <a href='http://en.wikipedia.org/wiki/Mobile_Network_Code'>MNC</a>");
		}
	    } else {
		sb.append("<br><br><Strong>No device has been found !</strong><br>");
	    }
	    sb.append("</center></html>");
	} else {
	    sb.append("<html><center><h1>Device data unavailable !</h1></center></html>");
	}

	text.setText(sb.toString());
	setStatus("Ready");
    }

    private void device_settings() {
	//XXX nedela
	/*
	 * contacts
call history
accounts

stolen delay :
5,10,15 min
1, 3 hodiny

control delay :
15,30 min
1,3,6 hodin


----
ip:port
change psswd
	 */
    }

    private void device_browseFiles() {
	List<FileObject> files = null;

	Net net = Session.getInstance().getNetwork();
	if (fileType.equals("history")) { //history
	    files = net.getHistory(Session.getInstance().getUsername(), numFiles);
	}else if(fileType.equals("contacts")) { //contacts
	    files = net.getContacts(Session.getInstance().getUsername(), numFiles);
	}else if (fileType.equals("accounts")) { //accounts
	    files = net.getAccounts(Session.getInstance().getUsername(), numFiles);
	} else {
	    return ;
	}

	if (files == null ) {
	    List<FileObject> tmp = Collections.emptyList();
	    filesModel.setFileData(tmp);
	} else {
	    filesModel.setFileData(files);
	}
    }

    private void saveFile() {
	if (filesTable.getSelectedRow() > -1) {
	    JFileChooser fc = new JFileChooser();
	    int returnVal = fc.showSaveDialog(this);

	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		File file = fc.getSelectedFile();
		OutputStream out = null;
		try {
		    out = new BufferedOutputStream(new FileOutputStream(file),8 * 1024);
		    out.write(filesModel.getData(filesTable.getSelectedRow()));
		    out.flush();
		} catch (IOException e) {
		    System.err.println("Unable to save a file : "+ e.getMessage());
		}finally {
		    try {
			if (out != null) {
			    out.close();
			}
		    } catch (IOException e) {
			System.err.println("FATAL : " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		    }
		}
	    }
	}
    }


    private void device_stolen(JCheckBoxMenuItem item) {
	setStatus("Stolen device ?!");
	Session ses = Session.getInstance();
	boolean activate = item.isSelected();

	int op = JOptionPane.showConfirmDialog(this, "Are you sure, you want to "+(activate ? "" : "de")+"activate 'stolen mode' on a device ?", "Set stolen mode ?", JOptionPane.YES_NO_OPTION);

	if (op == JOptionPane.YES_OPTION) {
	    ses.getNetwork().sendStolen(ses.getUsername(), activate);
	    item.setSelected(activate);
	} else {
	    item.setSelected(!item.isSelected());
	}

	setStatus("Ready");
    }

    private void device_setAlarm(JCheckBoxMenuItem item) {
	setStatus("Let's make some noise :)");
	Session ses = Session.getInstance();
	boolean activate = item.isSelected();

	int op = JOptionPane.showConfirmDialog(this, "Are you sure, you want to "+(item.isSelected() ? "" : "de")+"activate alarm on a device ?", "Set alarm ?", JOptionPane.YES_NO_OPTION);

	if (op == JOptionPane.YES_OPTION) {
	    ses.getNetwork().sendAlarm(ses.getUsername(), activate);
	    item.setSelected(activate);
	} else {
	    item.setSelected(!item.isSelected());
	}
	setStatus("Ready");
    }


    private void device_geoLocation() {
	showCard(panel_geolocation);
    }

    private void setStatus(String txt) {
	status.setText("   " + txt);
    }

    private void createGui() {
	JPanel panel = new JPanel(new BorderLayout());
	JPanel tmp = new JPanel(new BorderLayout());
	ButtonGroup group;
	JPanel tmp2;
	JRadioButton rbutton;
	JButton btn;

	status = new JLabel();
	content = new JPanel(new CardLayout());
	setTitle("FUT desktop client :: " + Props.getInstance().getProperty("futclient.host", Main.propID));
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setIconImage(new ImageIcon("res/icon.png").getImage());


	//**********************************************
	//Device info
	text = new JTextPane();//new HTMLDocument());
	text.setEditable(false);
	text.setContentType("text/html");
	text.addHyperlinkListener(new HyperLink());
	tmp.add(new JScrollPane(text), BorderLayout.CENTER);
	content.add(tmp, panel_info);
	//**********************************************
	// GPS records
	tmp2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
	tmp = new JPanel(new BorderLayout());
	//	tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));
	tmp2.add(new JLabel("<html><i>Choose number of records to show : </i></html>"));

	group  = new ButtonGroup();

	for (int i=5 ; i<=20 ; i += 5) {
	    rbutton = new JRadioButton("  " + i + "  ");
	    rbutton.addActionListener(numberOfRecords_radioButtonListener);
	    rbutton.setActionCommand(Integer.toString(i));
	    group.add(rbutton);
	    tmp2.add(rbutton);
	}

	map = new JTextPane();
	map.setEditable(false);
	map.setContentType("text/html");
	map.addHyperlinkListener(new HyperLink());
	tmp.add(tmp2, BorderLayout.NORTH);
	tmp.add(new JScrollPane(map), BorderLayout.CENTER);
	content.add(tmp, panel_geolocation);
	//**********************************************
	//Browse files
	tmp2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
	tmp = new JPanel(new BorderLayout());
	tmp2.add(new JLabel("<html><i>Choose a type of file : </i></html>"));
	JComboBox combo = new JComboBox(new String[] {"<Types>","Accounts","Contacts","History"});
	//new ArrayListModel<String>(new String[] {"<Types>","Accounts","Contacts","History"}));
	combo.setEditable(false);
	combo.addActionListener(fileTypes_comboBoxListener);
	tmp2.add(combo);
	tmp2.add(new JLabel("<html><i>Choose number of files to show : </i></html>"));

	group  = new ButtonGroup();

	for (int i=5 ; i<=20 ; i += 5) {
	    rbutton = new JRadioButton("  " + i + "  ");
	    rbutton.addActionListener(numberOfFiles_radioButtonListener);
	    rbutton.setActionCommand(Integer.toString(i));
	    group.add(rbutton);
	    tmp2.add(rbutton);
	}

	btn = new JButton("Show files");
	btn.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
		    @Override
		    public void run() {
			device_browseFiles();
		    }
		});
	    }
	});

	tmp2.add(btn);
	tmp.add(tmp2, BorderLayout.NORTH);

	tmp2 = new JPanel();
	tmp2.setLayout(new BoxLayout(tmp2, BoxLayout.Y_AXIS));
	filesModel = new FileTableModel();
	filesTable = new JTable(filesModel);

	tmp2.add(new JScrollPane(filesTable));

	btn = new JButton("Download");
	btn.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
		    @Override
		    public void run() {
			saveFile();
		    }
		});
	    }
	});
	btn.setAlignmentX(Component.CENTER_ALIGNMENT);
	tmp2.add(btn);


	tmp.add(tmp2, BorderLayout.CENTER);
	content.add(tmp, panel_browsefiles);
	// ********************************************
	content.add(new JLabel("<html><center><h1><i> Welcome to FUT...</i></h1></center></html>", JLabel.CENTER), "intro");

	panel.add(content, BorderLayout.CENTER);
	panel.add(status,BorderLayout.SOUTH);

	setContentPane(panel);
	showCard("intro");
	setJMenuBar(createManu());
	setMinimumSize(new Dimension(200, 200));
	setPreferredSize(new Dimension(800, 600));
	pack();
	setLocationRelativeTo(null);
	setVisible(true);
    }

    private void showCard(String ID) {
	((CardLayout)content.getLayout()).show(content, ID);
    }

    private JMenuBar createManu() {
	JMenuBar bar = new JMenuBar();
	bar.add(createMenuMain());
	bar.add(createMenuDevice());
	return bar;
    }

    private JMenu createMenuMain() {
	JMenu m = new JMenu("Main");
	m.add(createButton("Ends application", "Exit", null, exitListener));
	return m;
    }

    private JMenu createMenuDevice() {
	final JCheckBoxMenuItem alarm;
	final JCheckBoxMenuItem stolen;
	JMenu m = new JMenu("Device");

	m.add(createButton("Shows device informations", "Info", null, device_info));
	m.add(createButton("Shows files from device", "Browse files", null, device_browseFiles));
	m.add(createButton("Shows geo-location records", "Geo-location", null, device_geoLocation));
	alarm = createCheckButton("Sets alarm on the device", "Alarm", null, device_setAlarm, false);
	stolen = createCheckButton("Sets status 'stolen' for a device", "Stolen", null, device_stolen, false);
	m.add(alarm);
	m.add(stolen);
	//m.add(createButton("Settings for a device", "Settings", null, device_settings));


	SwingUtilities.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		Net net = Session.getInstance().getNetwork();
		stolen.setSelected(net.getStolen(Session.getInstance().getUsername()));
		alarm.setSelected(net.getAlarm(Session.getInstance().getUsername()));
	    }
	});

	return m;
    }


    private JMenuItem createButton(String tooltip, String caption, Icon icon, ActionListener listener) {
	JMenuItem tmp = new JMenuItem();
	tmp.setIcon(icon);
	// tmp.setActionCommand(actionCommand);
	tmp.setText(caption);
	tmp.setName(caption);
	tmp.setToolTipText(tooltip);
	tmp.addActionListener(listener);
	return tmp;
    }

    private JCheckBoxMenuItem createCheckButton(String tooltip, String caption, Icon icon, ActionListener listener, boolean checked) {
	JCheckBoxMenuItem tmp = new JCheckBoxMenuItem();
	tmp.setIcon(icon);
	// tmp.setActionCommand(actionCommand);
	tmp.setText(caption);
	tmp.setName(caption);
	tmp.setToolTipText(tooltip);
	tmp.addActionListener(listener);
	tmp.setSelected(checked);

	return tmp;
    }
}
