package fut.server.db;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import fut.common.DeviceObject;
import fut.common.FileObject;
import fut.common.GeodataObject;
import fut.server.common.Crypto;
import fut.server.common.Log;
import fut.server.common.Props;
import fut.server.common.Sql;

/**
 * Class DBLogic. This class provides abstraction layer between underlaying
 * database and the rest of the appplication.<br>
 * As primary SQL server was selected H2 Sql server, but I am trying to keep
 * code as general as possible (in order to have a chance to migrate to another
 * SQL database engine).<br>
 *
 *
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 20.10.2010
 */
public final class DBLogic {


    private static volatile DBLogic INSTANCE = null;
    private static final String ID = "fut";
    private Sql sql = null;
    private Props prop = null;
    private Log log = null;
//    private Calendar calendar = null;

    private Map<String, PreparedStatement> cache = null;


    private  DBLogic() {
        log = new Log(this.getClass());
        sql = Sql.getInstance();
        prop = Props.getInstance();
        cache = new HashMap<String, PreparedStatement>(128);
//	calendar = Calendar.getInstance();
    }

    private void prepareStatements() {
        /*
         * subory a geodata, proste musia mat aj ID
         * TODO : preskumat, kde by malo byt setNull, koli SQL
         * Files >> //XXX pridat k datam aj ID ? opytat sa kuba
         */

        //users
        prepare("select id from users where username= ? ;","get_user_id");
        prepare("insert into users (username, key) values (?, ?) ;","create_user");
        prepare("delete from users where username= ? ;","delete_user");
        prepare("update users set username= ?, key= ? where id= ? ;","update_user");
        prepare("select key from users where username= ? ;","get_user_key");

//	prepare("select id from device where users_id = ? ;","get_user_devices");


        //user properties
        prepare("select p.name, p.value from properties as p, users as u where p.users_id = u.id and u.username = ?  ;","get_properties");
        prepare("insert into properties (name, value, users_id) values (?, ?, select id from users where username = ?) ;","add_property");
        prepare("update properties set value=?  where name=? and users_id = (select id from users where username = ?) ;","update_property");
        prepare("delete from properties where name= ? and users_id = (select id from users where username = ?) ;","delete_property");
        //prepare("select name, value from properties where users_id= ? ;","get_properties");
        //prepare("insert into properties (name, value, users_id) values (?, ?, ?) ;","add_property");
        //prepare("update properties set value=?  where name=? and users_id=? ;","update_property");
        //prepare("delete from properties where name= ? and users_id= ? ;","delete_property");


        //Device
        prepare("select d.id, d.imei, d.imsi, d.sim, d.operator, d.stolen, d.users_id from device as d,users as u where d.users_id = u.id and u.username = ? ;","get_device");
        prepare("insert into device (imei, imsi, sim, operator, users_id) values (?, ?, ?, ?, (select u.id from users as u where u.username = ?)) ;","create_device");
        prepare("update device set imei=? ,imsi=? ,sim=? ,operator=?  where users_id = (select u.id from users as u where u.username = ?) ;","update_device");
        prepare("delete from device where users_id = (select u.id from users as u where u.username = ?);","delete_device");
        prepare("select stolen from device, users where users_id = users.id and users.username = ? ;","is_device_stolen");
        prepare("update device as d set d.stolen= ? where d.users_id = (select u.id from users as u where u.username = ?) ;","set_device_stolen");
        //old, user-device specific
        //prepare("update device set stolen= ? where id= ? and users_id= ? ;","set_device_stolen");
        //prepare("select stolen from device where id= ? and users_id= ? ;","is_device_stolen");
        //prepare("insert into device (imei, imsi, sim, operator, users_id) values (?, ?, ?, ?, ?) ;","create_device");
        //prepare("update device set imei=? ,imsi=? ,sim=? ,operator=? ,stolen=?  where id= ? ;","update_device");
        //prepare("delete from device where id= ? and users_id= ? ;","delete_device");

        //geodata
        prepare("insert into geodata (longitude, latitude, altitude, time, azimuth, speed, accuracy, geo_source_id, device_id) values (?, ?, ?, ?, ?, ?, ?, ?, (select d.id from device as d, users as u where d.users_id = u.id and u.username = ?) ) ;","create_geo");
        prepare("update geodata set longitude= ?, latitude= ?, altitude= ?, time= ?, azimuth= ?, speed= ?, accuracy= ?, geo_source_id= ? where id= ? and device_id= (select d.id from device as d, users as u where d.users_id = u.id and u.username = ?) ;","update_geo");
        prepare("delete from geodata where id= ? and device_id = (select d.id from device as d, users as u where d.users_id = u.id and u.username = ?) ;","delete_geo");
        prepare("select g.id, g.longitude, g.latitude, g.altitude, g.time, g.azimuth, g.speed, g.accuracy, g.geo_source_id, (select ss.type from geo_source as ss where ss.id = g.geo_source_id  ) from geodata as g, device as d, users as u where u.username = ? and d.users_id = u.id and g.device_id = d.id order by g.time desc limit ?;","get_geo");
        prepare("select id, type from geo_source ;","list_geo_sources");

        //transaction
        //select currval('sync_id_seq');
        //prepare("select nextval('sync_id_seq') ;","get_next_sync_id"); // not used so far
        //prepare("insert into transaction (time, sync_id, file_id, device_id) values (?, ?, ?, ?) ;","create_transaction");
        //prepare("delete from transaction where sync_id= ? and device_id= ? ;","delete_transaction");
        //prepare("select id, time, sync_id, file_id from transaction where device_id= ?;","get_transaction_by_device");//XXX new,  mozno ?
        //prepare("select id, time, device_id, file_id from transaction where sync_id= ?;","get_transaction_by_sync_id");//XXX new, mozno ?

        //Files
        //XXX zakomponovanie data_type...?
        prepare("insert into files (filename, data, meta, time, data_type_id, device_id) values (?, ?, ?, ?, ?, (select d.id from device as d, users as u where d.users_id = u.id and u.username = ?)) ;","create_file");
        prepare("delete from files where id=? and device_id = (select d.id from device as d, users as u where d.users_id = u.id and u.username = ?) ;","delete_file");

        prepare("select f.id, f.filename, f.meta, f.time, f.data_type_id, (select d.type from data_type as d where d.id = ?), f.device_id, f.data from files as f where f.data_type_id = ? and f.device_id = (select dd.id from device as dd, users as uu where dd.users_id = uu.id and uu.username = ?) limit ? ;","get_file_data");

        prepare("select meta from files where id=? ;","get_file_meta");
        prepare("update files set meta= ? where id= ? ;","set_file_meta");
        prepare("select id, type from data_type ;","list_data_types");
        // maybe...
        //prepare("update files set filename=?, data=? where id=? and mail_id=? ;","update_attachment");
        //prepare("delete from files where mail_id=? ;","delete_all_attachments");
        //	prepare("select count(mail_id) from files where mail_id=? ;","has_attachment");
        //	prepare("select id, filename, mail_id from files where mail_id=? ;","list_attachments");


        //	//mails & files
        //	prepare("select t1.id, t1.mailid ,t1.subject, t1.stamp, t2.mailaddress, t1.isnew " +
        //		"from mails as t1, address as t2, translate as t3 " +
        //		"where t1.from_seq = t3.source_id and " +
        //		"t3.destination_id = t2.id and " +
        //		"t1.folder_id = ?  ;","list_mails_for_preview");
        //
        //
    }

    private void prepare(String query, String name) { //creates prepared statement & save it to the cache
        PreparedStatement ps;
        try {
            if (name.startsWith("create"))
                ps = sql.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
            else
                ps = sql.prepareStatement(query);

        } catch (SQLException e) {
            log.warning("unable to create prepared sql statement", e);
            throw new RuntimeException(e); //there is no point to continue
        }
        cache.put(name, ps);
    }

    private String getHash(String s) { //generates MD5 hash for passwords
        try {
            return Crypto.getMD5Hash("~1A#b3" + s + "%M2@_0;"); //MD5 salt :)
        } catch (NoSuchAlgorithmException e) {
            log.severe("problem getting cryptographic support !",e);
            throw new RuntimeException(e);
        }
    }



    /**
     * Gets the single instance of DBLogic.
     *
     * @return single instance of DBLogic
     */
    public static DBLogic getInstance() {
        if (INSTANCE==null) {//double check idiom
            synchronized (DBLogic.class) {
                if (INSTANCE==null) {
                    INSTANCE = new DBLogic();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Creates connection to the database.
     */
    public void connect() {
        String jdbc = prop.getProperty("db.jdbc", ID);
        String url = prop.getProperty("db.url", ID);

        try {
            //try to login
            sql.connect(jdbc, url, "sa","1Administration", false);
            sql.setTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
            //	    checkDB();
            cache.clear();
            prepareStatements();


        } catch (ClassNotFoundException e) {
            log.severe("JDBC driver ("+jdbc+") not found ! quit...", e);
            JOptionPane.showMessageDialog(null, "JDBC driver ("+jdbc+") not found !!\n" +
            "Application will exit...");
            System.exit(1);

        } catch (SQLException e) {
            try {
                //first attempt failed , try to connect as SA with no password
                sql.connect(jdbc, url, "sa","", false);
                sql.setTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
                //		checkDB();
                cache.clear();
                prepareStatements();

            } catch (ClassNotFoundException e1) {
                log.severe("JDBC driver ("+jdbc+") not found ! quit...", e);
                JOptionPane.showMessageDialog(null, "JDBC driver ("+jdbc+") not found !!\n" +
                "Application will quit...");
                System.exit(1);

            } catch (SQLException e1) {
                log.warning("Unable to connect to SQL database ("+url+")", e);
                JOptionPane.showMessageDialog(null, "Unable to connect to SQL database ("
                        + url+ "). \nReason :\n" + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }

    }

    /**
     * Disconnects from database.
     */
    public void disconnect() {
        try {
            sql.execute("shutdown compact;");
        } catch (SQLException ignored) {
        }

        sql.close();
        sql.closeConnection();
        cache.clear();
    }


    /**
     * Gets user ID in the system.
     *
     * @param username
     *            the name of the user in system.
     * @return if login is successful, user's ID is return, otherwise
     * negative integer (-1) is returned.
     */
    public long getUserId(String username) {
        PreparedStatement stmt = cache.get("get_user_id");
        ResultSet rs;

        try {
            stmt.setString(1, Sql.escape(username));
            rs = stmt.executeQuery();

            if (rs.next()) {
                //		System.out.println("login result : " + rs.getInt(1));
                return rs.getLong(1); //return user's ID
            }

        } catch (SQLException e) {
            log.warning("problem getting user ID", e);
        }

        return -1;
    }


    /**
     * Creates new user.
     *
     * @param username
     *            the username
     * @param psswd
     *            the psswd
     * @return the id of newly created user, or -1 if something was wrong
     */
    public long createUser(String username, String psswd) {
        PreparedStatement user = cache.get("create_user");
        ResultSet rs = null;
        Savepoint savepoint = null;
        long user_id = -1;

        try {
            savepoint = sql.createSavePoint("create_user");
            user.setString(1, Sql.escape(username));
            user.setString(2, Sql.escape(psswd)); // !!! nezabezpecene heslo , ponechane na uzivatela
            user.execute();


            rs = user.getGeneratedKeys();
            while (rs.next()) {
                user_id = rs.getLong(1);
            }

            sql.commit();

            createDevice(0, 0, "0", 0, username);
            addProperty("alarm", "false", username);
            return user_id;
        } catch (SQLException e) {
            log.warning("Problem while creating new user", e);
            user_id = -1;
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }
        }

        return user_id;
    }

    /**
     * Updates user.
     *
     * @param newUsername
     *            the new username
     * @param newPasswd
     *            the new passwd
     * @param user_id
     *            the user id
     * @return true, if successful
     */
    public boolean updateUser(String newUsername, String newPasswd, long user_id) {
        PreparedStatement ps = cache.get("update_user");
        Savepoint savepoint = null;

        try {
            savepoint = sql.createSavePoint("update_user");
            ps.setString(1, Sql.escape(newUsername));
            ps.setString(2, Sql.escape(newPasswd));
            ps.setLong(3, user_id);
            if (ps.executeUpdate() > 0) {
                sql.commit();
                return true;
            }
        } catch (SQLException e) {
            log.warning("Problem updating user in DB", e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }

        }

        return false;
    }


    /**
     * Delete user.
     *
     * @param username
     *            the username
     * @return true, if successful
     */
    public boolean deleteUser(String username) {
        PreparedStatement ps = cache.get("delete_user");
        Savepoint savepoint = null;

        try {
            savepoint = sql.createSavePoint("delete_user");
            ps.setString(1, Sql.escape(username));
            if (!ps.execute()) {
                sql.commit();
                return true;
            }

        } catch (SQLException e) {
            log.warning("Problem while deleting user from database", e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }
        }

        return false;
    }


    /**
     * Gets the user password, used for encryption.<br>
     * On success, the secret is returned, entered email otherwise !
     *
     * @param email
     *            the email
     * @return the user password
     */
    public String getUserPassword(String email) {
        PreparedStatement stmt = cache.get("get_user_key");
        ResultSet rs;

        try {
            stmt.setString(1, Sql.escape(email));
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString(1);
            }

        } catch (SQLException e) {
            log.severe("problem getting user key for encryption", e);
        }

        return email;
    }

//    /**
//     * Gets the user devices.
//     *
//     * @param user_id
//     *            the user_id
//     * @return the user devices
//     */
//    public List<Long> getUserDevices(long user_id) {
//	//"select id from device where users_id = ? ;","get_user_devices"
//	List<Long> dev = new ArrayList<Long>(2);
//	PreparedStatement stmt = cache.get("get_user_devices");
//	ResultSet rs;
//
//	try {
//	    stmt.setLong(1, user_id);
//	    rs = stmt.executeQuery();
//
//	    while (rs.next()) {
//		dev.add(rs.getLong(1));
//	    }
//
//	} catch (SQLException e) {
//	    log.severe("problem getting user devices", e);
//	}
//
//	return dev;
//    }


    public List<DeviceObject> getDevices(String username) {
        //("select id, imei, imsi, sim, operator, stolen, users_id from device as d,users as u where d.users_id = u.id and u.username = ? ;","get_device");
        List<DeviceObject> dev = new ArrayList<DeviceObject>(2);
        PreparedStatement stmt = cache.get("get_device");
        ResultSet rs;

        try {
            stmt.setString(1, Sql.escape(username));
            rs = stmt.executeQuery();

            while (rs.next()) {
                dev.add( new DeviceObject(rs.getLong(1),
                        rs.getLong(2),
                        rs.getLong(3),
                        rs.getString(4),
                        rs.getInt(5),
                        rs.getByte(6) > 0 ? true : false,
                        rs.getLong(7)));
            }

        } catch (SQLException e) {
            log.severe("problem getting user devices", e);
        }

        return dev;
    }

    /**
     * Creates the device.
     *
     * @param imei
     *            the imei
     * @param imsi
     *            the imsi
     * @param sim
     *            the sim
     * @param operator
     *            the operator
     * @param user_id
     *            the user_id
     * @return the long
     */
    public long createDevice(long imei, long imsi, String sim, int operator, String username) {
        //("insert into device (imei, imsi, sim, operator, users_id) values (?, ?, ?, ?, (select u.id from users as u where u.username = ?)) ;","create_device");

        PreparedStatement dev = cache.get("create_device");
        ResultSet rs = null;
        Savepoint savepoint = null;
        long device_id = -1;

        try {
            savepoint = sql.createSavePoint("create_device");
            dev.setLong(1, imei);
            dev.setLong(2, imsi);
            dev.setString(3, Sql.escape(sim));
            dev.setInt(4, operator);
            dev.setString(5, Sql.escape(username));
            dev.execute();

            rs = dev.getGeneratedKeys();
            while (rs.next()) {
                device_id = rs.getInt(1);
            }

            sql.commit();
        } catch (SQLException e) {
            log.warning("Problem while creating new device", e);
            device_id = -1;
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }
        }

        return device_id;
    }

    /**
     * Update device.
     *
     * @param imei
     *            the imei
     * @param imsi
     *            the imsi
     * @param sim
     *            the sim
     * @param operator
     *            the operator
     * @param stolen
     *            the stolen
     * @param device_id
     *            the device_id
     * @return true, if successful
     */
    public boolean updateDevice(long imei, long imsi, String sim, int operator,  String username) {
        //("update device set imei=? ,imsi=? ,sim=? ,operator=? ,stolen=?  where users_id = (select u.id from users as u where u.username = ?) ;","update_device");
        PreparedStatement dev = cache.get("update_device");
        Savepoint savepoint = null;

        try {
            savepoint = sql.createSavePoint("update_device");
            dev.setLong(1, imei);
            dev.setLong(2, imsi);
            dev.setString(3, Sql.escape(sim));
            dev.setInt(4, operator);
            dev.setString(5, Sql.escape(username));

            if (dev.executeUpdate() > 0) {
                sql.commit();
                List<DeviceObject> tmp = getDevices(username);
                System.out.println("Control from db : " + tmp.get(0).getImsi());
                return true;
            }
        } catch (SQLException e) {
            log.warning("Problem updating device in DB", e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }

        }

        return false;
    }

    /**
     * Delete device.
     *
     * @param device_id
     *            the device_id
     * @param user_id
     *            the user_id
     * @return true, if successful
     */
    public boolean deleteDevice(String username) {
//"delete from device where users_id = (select u.id from users as u where u.username = ?);","delete_device");
        PreparedStatement ps = cache.get("delete_device");
        Savepoint savepoint = null;

        try {
            savepoint = sql.createSavePoint("delete_device");
            ps.setString(1, Sql.escape(username));
            if (!ps.execute()) {
                sql.commit();
                return true;
            }

        } catch (SQLException e) {
            log.warning("Problem while deleting device from database", e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }
        }

        return false;

    }

    /**
     * Checks if is device stolen.
     *
     * @param username
     *            the user login
     * @return true, if is device stolen
     */
    public boolean isDeviceStolen(String username) {
        //("select stolen from device, users where users_id = users.id and users.username = ? ;","is_device_stolen")
        PreparedStatement stmt = cache.get("is_device_stolen");
        ResultSet rs;
        boolean result = false;

        try {
            stmt.setString(1, Sql.escape(username));
            rs = stmt.executeQuery();

            if (rs.next()) {
                result = rs.getShort(1) > 0;
            }

        } catch (SQLException e) {
            log.severe("problem determinig if user device is stolen", e);
        }

        return result;

    }

    /**
     * Sets the device stolen.
     *
     * @param username
     *            the user login
     * @param stolen
     *            determines if device was stolen
     * @return true, if successful
     */
    public boolean setDeviceStolen(String username, boolean stolen) {
        //("update device set stolen= ? where users_id = users.id and users.username = ? ;","set_device_stolen");
        PreparedStatement dev = cache.get("set_device_stolen");
        Savepoint savepoint = null;

        try {
            savepoint = sql.createSavePoint("set_device_stolen");
            dev.setByte(1, (byte) (stolen ? 63 : 0));
            dev.setString(2, Sql.escape(username));

            if (dev.executeUpdate() > 0) {
                sql.commit();
                return true;
            }
        } catch (SQLException e) {
            log.warning("Problem updating the stolen flag of a device in DB", e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }
        }
        return false;
    }

    /**
     * Creates the geodata.
     *
     * @param longitude
     *            the longitude
     * @param latitude
     *            the latitude
     * @param altitude
     *            the altitude
     * @param timestamp
     *            the timestamp
     * @param azimuth
     *            the azimuth
     * @param speed
     *            the speed
     * @param accuracy
     *            the accuracy
     * @param geo_source_id
     *            the geo_source_id
     * @param device_id
     *            the device_id
     * @return the long
     */
    public long createGeodata(Double longitude, Double latitude, Double altitude, java.sql.Timestamp timestamp, Float azimuth, Float speed, Float accuracy, long geo_source_id, String username) {
        //("insert into geodata (longitude, latitude, altitude, time, azimuth, speed, accuracy, geo_source_id, device_id) values (?, ?, ?, ?, ?, ?, ?, ?, (select d.id from device as d, users as u where d.users_id = u.id and u.username = ?) ) ;","create_geo");

        PreparedStatement dev = cache.get("create_geo");
        ResultSet rs = null;
        Savepoint savepoint = null;
        long geo_id = -1;

        try {
            savepoint = sql.createSavePoint("create_geo");
            if (longitude == null) {
                dev.setNull(1, Types.BIGINT);
            } else {
                dev.setDouble(1, longitude);
            }
            if (latitude == null) {
                dev.setNull(2, Types.BIGINT);
            } else {
                dev.setDouble(2, latitude);
            }
            if (altitude == null) {
                dev.setNull(3, Types.BIGINT);
            } else {
                dev.setDouble(3, altitude);
            }
            dev.setTimestamp(4, timestamp);//, calendar);
            if (azimuth == null) {
                dev.setNull(5, Types.FLOAT);
            } else {
                dev.setDouble(5, azimuth);
            }
            if (speed == null) {
                dev.setNull(6, Types.FLOAT);
            } else {
                dev.setDouble(6, speed);
            }
            if (accuracy == null) {
                dev.setNull(7, Types.FLOAT);
            } else {
                dev.setDouble(7, accuracy);
            }
            dev.setDouble(8, geo_source_id);
            dev.setString(9, Sql.escape(username));
            dev.execute();

            rs = dev.getGeneratedKeys();
            while (rs.next()) {
                geo_id = rs.getInt(1);
            }

            sql.commit();
        } catch (SQLException e) {
            log.warning("Problem while creating new geo data", e);
            geo_id = -1;
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }
        }
        return geo_id;
    }

    /**
     * Gets the geodata.
     *
     * @param username
     *            the user login
     * @param limit
     *            the limit of records to return from DB
     * @return the geodata
     */
    public List<GeodataObject> getGeodata(String username, int limit) {
        //("select g.id, g.longitude, g.latitude, g.altitude, g.time, g.azimuth, g.speed, g.accuracy, g.geo_source_id, (select ss.type from geo_source as ss where ss.id = g.geo_source_id  ) from geodata as g, geo_source as s, device as d, users as u where u.username = ? and d.users_id = u.id and g.device_id = d.id order by g.time desc limit ?;","get_geo");
        PreparedStatement ps = cache.get("get_geo");
        ResultSet rs;
        List<GeodataObject> tmp = new ArrayList<GeodataObject>(limit);
        GeodataObject go;
        long id;
        Double longitude;

        try {
            ps.setString(1, Sql.escape(username));
            ps.setInt(2, limit);
            rs = ps.executeQuery();

            while (rs.next()) {
                tmp.add(new GeodataObject(
                        rs.getLong(1),
                        rs.getDouble(2),
                        rs.getDouble(3),
                        rs.getDouble(4),
                        rs.getTimestamp(5),
                        rs.getFloat(6),
                        rs.getFloat(7),
                        rs.getFloat(8),
                        rs.getLong(9),
                        rs.getString(10)
                        ));
            }

        } catch (SQLException e) {
            log.severe("problem getting geodata", e);
        }

        return tmp;
    }

    /**
     * Update geodata.
     *
     * @param longitude
     *            the longitude
     * @param latitude
     *            the latitude
     * @param altitude
     *            the altitude
     * @param timestamp
     *            the timestamp
     * @param azimuth
     *            the azimuth
     * @param speed
     *            the speed
     * @param accuracy
     *            the accuracy
     * @param new_geo_source_id
     *            the new_geo_source_id
     * @param old_device_id
     *            the old_device_id
     * @param old_geo_id
     *            the old_geo_id
     * @return true, if successful
     */
    //XXX Jestrab : zmazat ?
    public boolean updateGeodata(Double longitude, Double latitude, Double altitude, java.sql.Timestamp timestamp, Float azimuth, Float speed, Float accuracy, long new_geo_source_id, long old_geo_id, String username) {
        //("update geodata set longitude= ?, latitude= ?, altitude= ?, time= ?, azimuth= ?, speed= ?, accuracy= ?, geo_source_id= ? where id= ? and device_id= (select d.id from device as d, users as u where d.users_id = u.id and u.username = ?) ;","update_geo");

        PreparedStatement dev = cache.get("update_geo");
        Savepoint savepoint = null;

        try {
            savepoint = sql.createSavePoint("update_geo");

            if (longitude == null) {
                dev.setNull(1, Types.BIGINT);
            } else {
                dev.setDouble(1, longitude);
            }
            if (latitude == null) {
                dev.setNull(2, Types.BIGINT);
            } else {
                dev.setDouble(2, latitude);
            }
            if (altitude == null) {
                dev.setNull(3, Types.BIGINT);
            } else {
                dev.setDouble(3, altitude);
            }
            dev.setTimestamp(4, timestamp);//, calendar);
            if (azimuth == null) {
                dev.setNull(5, Types.FLOAT);
            } else {
                dev.setDouble(5, azimuth);
            }
            if (speed == null) {
                dev.setNull(6, Types.FLOAT);
            } else {
                dev.setDouble(6, speed);
            }
            if (accuracy == null) {
                dev.setNull(7, Types.FLOAT);
            } else {
                dev.setDouble(7, accuracy);
            }

            dev.setLong(8, new_geo_source_id);
            dev.setLong(9, old_geo_id);
            dev.setString(10, Sql.escape(username));


            if (dev.executeUpdate() > 0) {
                sql.commit();
                return true;
            }
        } catch (SQLException e) {
            log.warning("Problem updating geo data in DB", e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }

        }
        return false;
    }

    /**
     * Delete geodata.
     *
     * @param geo_id
     *            the geo_id
     * @param device_id
     *            the device_id
     * @return true, if successful
     */
    public boolean deleteGeodata(long geo_id, String username) {
        //("delete from geodata where id= ? and device_id = (select d.id from device as d, users as u where d.users_id = u.id and u.username = ?) ;","delete_geo");

        PreparedStatement ps = cache.get("delete_geo");
        Savepoint savepoint = null;

        try {
            savepoint = sql.createSavePoint("delete_geo");
            ps.setLong(1, geo_id);
            ps.setString(2, Sql.escape(username));
            if (!ps.execute()) {
                sql.commit();
                return true;
            }

        } catch (SQLException e) {
            log.warning("Problem while deleting geo data from database", e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }
        }
        return false;
    }

    /**
     * List geo sources.
     *
     * @return the map
     */
    public Map<String, Long> listGeoSources() {
        //("select id, type from geo_source ;","list_geo_sources");
        Map<String, Long> sources = new HashMap<String, Long>();
        PreparedStatement ps = cache.get("list_geo_sources");
        ResultSet rs;

        try {
            rs = ps.executeQuery();

            while (rs.next()) {
                sources.put(rs.getString(2), rs.getLong(1));
            }

        } catch (SQLException e) {
            log.severe("problem getting geo sources", e);
        }
        return sources;
    }


//    /**
//     * Creates the transaction.
//     *
//     * @param timestamp
//     *            the timestamp
//     * @param sync_id
//     *            the sync_id
//     * @param file_id
//     *            the file_id
//     * @param device_id
//     *            the device_id
//     * @return the long
//     */
//    public long createTransaction(java.sql.Timestamp timestamp, long sync_id, long file_id, long device_id) {
//	//("insert into transaction (time, sync_id, file_id, device_id) values (?, ?, ?, ?) ;","create_transaction");
//	PreparedStatement ps = cache.get("create_transaction");
//	ResultSet rs = null;
//	Savepoint savepoint = null;
//	long transaction_id = -1;
//
//	try {
//	    savepoint = sql.createSavePoint("create_trensaction");
//	    ps.setTimestamp(1, timestamp, calendar);
//	    ps.setDouble(1, sync_id);
//	    ps.setDouble(2, file_id);
//	    ps.setDouble(3, device_id);
//	    ps.execute();
//
//	    rs = ps.getGeneratedKeys();
//	    while (rs.next()) {
//		transaction_id = rs.getInt(1);
//	    }
//
//	    sql.commit();
//	} catch (SQLException e) {
//	    log.warning("Problem while creating new transaction record", e);
//	    transaction_id = -1;
//	    try {
//		sql.rollback(savepoint);
//	    } catch (SQLException e1) {
//		log.severe("SQL transaction rollback failed", e1);
//	    }
//	}
//	return transaction_id;
//    }
//
//    /**
//     * Delete transaciotn.
//     *
//     * @param sync_id
//     *            the sync_id
//     * @param device_id
//     *            the device_id
//     * @return true, if successful
//     */
//    public boolean deleteTransaction(long sync_id, long device_id) {
//	//Caution ! deletes allrecords from given transaction
//	//("delete from transaction where sync_id= ? and device_id= ? ;","delete_transaction");
//	PreparedStatement ps = cache.get("delete_transaction");
//	Savepoint savepoint = null;
//
//	try {
//	    savepoint = sql.createSavePoint("delete_transaction");
//	    ps.setLong(1, sync_id);
//	    ps.setLong(2, device_id);
//	    if (!ps.execute()) {
//		sql.commit();
//		return true;
//	    }
//
//	} catch (SQLException e) {
//	    log.warning("Problem while deleting transaction from database", e);
//	    try {
//		sql.rollback(savepoint);
//	    } catch (SQLException e1) {
//		log.severe("SQL transaction rollback failed", e1);
//	    }
//	}
//	return false;
//
//    }


    /**
     * List data types.
     *
     * @return the map
     */
    public Map<String,Long> listDataTypes() {
        //"select id, type from data_type ;","list_data_types"
        Map<String, Long> types = new HashMap<String, Long>();
        PreparedStatement ps = cache.get("list_data_types");
        ResultSet rs;

        try {
            rs = ps.executeQuery();

            while (rs.next()) {
                types.put(rs.getString(2), rs.getLong(1));
            }

        } catch (SQLException e) {
            log.severe("problem getting geo sources", e);
        }
        return types;
    }

    /**
     * Creates the file.
     *
     * @param filename
     *            the filename
     * @param data
     *            the data
     * @param meta
     *            the meta
     * @param data_type_id
     *            the data_type_id
     * @param device_id
     *            the device_id
     * @return the long
     */
    public long createFile(String filename, byte[] data, String meta, long data_type_id, String username) {
        //("insert into files (filename, data, meta, time, data_type_id, device_id) values (?, ?, ?, ?, ?, (select d.id from device as d, users as u where d.users_id = u.id and u.username = ?)) ;","create_file")

        PreparedStatement ps = cache.get("create_file");
        ResultSet rs;
        Savepoint savepoint = null;
        long file_id = -1;

        try {
            savepoint = sql.createSavePoint("create_file");
            ps.setString(1, filename);
            //ps.setBlob(2, new ByteArrayInputStream(data));
            ps.setBinaryStream(2, new ByteArrayInputStream(data), data.length);
            ps.setString(3, meta);
            ps.setTimestamp(4, new java.sql.Timestamp(new Date().getTime()));
            ps.setLong(5, data_type_id);
            ps.setString(6, Sql.escape(username));

            ps.executeUpdate();
            rs = ps.getGeneratedKeys();

            if (rs.next()) {
                file_id = rs.getLong(1);
            }
            sql.commit();


            return file_id;
        } catch (SQLException e) {
            log.warning("Problem creating file "+ filename, e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }
        }

        return -1;
    }


    /**
     * Delete file.
     *
     * @param file_id
     *            the file_id
     * @param device_id
     *            the device_id
     * @return true, if successful
     */
    public boolean deleteFile(long file_id, String username) {
        //("delete from files where id=? and device_id = (select d.id from device as d, users as u where d.users_id = u.id and u.username = ?) ;","delete_file");

        PreparedStatement ps = cache.get("delete_file");
        Savepoint savepoint = null;

        try {
            savepoint = sql.createSavePoint("delete_file");
            ps.setLong(1, file_id);
            ps.setString(2, Sql.escape(username));
            ps.executeUpdate();

            sql.commit();
            return true;

        } catch (SQLException e) {
            log.warning("Problem deleting file", e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }
        }

        return false;
    }


    /**
     * Gets the file data.
     *
     * @param file_id
     *            the file_id
     * @param device_id
     *            the device_id
     * @param out
     *            the out
     * @return the file data
     */
    public List<FileObject> getFileData(long data_type_id, String username, int limit) {
	//("select f.id, f.filename, f.meta, f.time, f.data_type_id, (select d.type from data_type as d where d.id = ?), f.device_id, f.data from files as f where f.data_type_id = ? and f.device_id = (select dd.id from device as dd, users as uu where dd.users_id = uu.id and uu.username = ?) limit ? ;","get_file_data");

	PreparedStatement ps = cache.get("get_file_data");
        ResultSet rs;
        List<FileObject> files = new ArrayList<FileObject>(5);
        Blob data=null;
        byte buf[] = new byte[8*1024];
        InputStream input = null;
        ByteArrayOutputStream out;
        int len=0;

        try {
            ps.setLong(1, data_type_id);
            ps.setLong(2, data_type_id);
            ps.setString(3, Sql.escape(username));
            ps.setInt(4, limit);
            rs = ps.executeQuery();
            while (rs.next()) {
                data = rs.getBlob(8);
                out = new ByteArrayOutputStream(8 * 1024);

                input = new BufferedInputStream(data.getBinaryStream(), 8*1024);

                while ((len = input.read(buf, 0, 8*1024)) > -1 ) {
                    out.write(buf, 0, len);
                }

                files.add(new FileObject(rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getTimestamp(4),
                        rs.getLong(5),
                        rs.getString(6),
                        rs.getLong(7),
                        out.toByteArray()
                        ));
            }

        } catch (SQLException e) {
            log.warning("Problem getting data from file", e);
        } catch (IOException e) {
            log.warning("Problem saving data", e);
        } finally {
            try {

                if (input != null) {
                    input.close();
                }
            } catch (IOException ignored) {
            }

        }

        return files;
    }


    /**
     * Gets the file meta.
     *
     * @param file_id
     *            the file_id
     * @return the file meta
     */
    public String getFileMeta(long file_id) {
        String data="";
        PreparedStatement ps = cache.get("get_file_meta");
        ResultSet rs = null;

        try {
            ps.setLong(1, file_id);
            rs = ps.executeQuery();
            while (rs.next()) {
                data = rs.getString(1);
            }

            if (data == null) data = "";
        } catch (SQLException e) {
            log.warning("Problem getting file metadata", e);
        }

        return data;
    }


    /**
     * Sets the file meta.
     *
     * @param meta
     *            the meta
     * @param file_id
     *            the file_id
     * @return true, if successful
     */
    public boolean setFileMeta(String meta, long  file_id) {
        //"update files set meta= ? where id= ? ;","set_file_meta"
        PreparedStatement ps = cache.get("set_file_meta");
        Savepoint savepoint = null;

        try {
            savepoint = sql.createSavePoint("set_file_meta");
            ps.setString(1, Sql.escape(meta));
            ps.setLong(2, file_id);
            if (ps.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            log.warning("Problem setting file metadata", e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("Rollback operation failed.", e1);
            }
        }

        return false;
    }


    /**
     * Gets the settings.
     *
     * @param username
     *            the username
     * @param userid
     *            the userid
     */


    public void loadUserProperties(String username) {
        //loads all user settings from table properties and stores them to property manager prop.
        //("select p.name, p.value from properties as p, users as u where p.users_id = u.id and u.username = ?  ;","get_properties");

        PreparedStatement ps = cache.get("get_properties");
        ResultSet rs = null;

        try {
            ps.setString(1, Sql.escape(username));
            rs = ps.executeQuery();

            while(rs.next()) {
                prop.setProperty(rs.getString(1), rs.getString(2), username);
            }

        } catch (SQLException e) {
            log.warning("Problem getting user settings from database", e);
        }
    }


    /**
     * Adds the property.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @param username
     *            the username
     * @param userID
     *            the user id
     * @return true, if successful
     */
    public boolean addProperty(String name, String value, String username) {
//("insert into properties (name, value, users_id) values (?, ?, select id from users where username = ?) ;","add_property");
        PreparedStatement ps = cache.get("add_property");
        Savepoint savepoint = null;

        try {
            savepoint = sql.createSavePoint("add_property");
            ps.setString(1, Sql.escape(name));
            ps.setString(2, Sql.escape(value));
            ps.setString(3, Sql.escape(username));
            if (ps.executeUpdate() > 0) {
                sql.commit();
                prop.setProperty(name, value, username);
                return true;
            }

        } catch (SQLException e) {
            log.warning("Problem while adding new property", e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }
        }

        return false;
    }

    /**
     * Update property.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     * @param username
     *            the username
     * @param userID
     *            the user id
     * @return true, if successful
     */
    public boolean updateProperty(String name, String value,String username) {
//("update properties set value=?  where name=? and users_id = (select id from users where username = ?) ;","update_property");

        PreparedStatement ps = cache.get("update_property");
        Savepoint savepoint = null;

        try {
            savepoint = sql.createSavePoint("update_property");
            ps.setString(1, Sql.escape(value));
            ps.setString(2, Sql.escape(name));
            ps.setString(3, Sql.escape(username));
            if (ps.executeUpdate() > 0) {
                sql.commit();
                prop.setProperty(name, value, username);
                return true;
            }

        } catch (SQLException e) {
            log.warning("Problem while updating property", e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }
        }

        return false;
    }

    /**
     * Delete property.
     *
     * @param name
     *            the name
     * @param username
     *            the username
     * @param userID
     *            the user id
     * @return true, if successful
     */
    public boolean deleteProperty(String name, String username) {
        //("delete from properties where name= ? and users_id = (select id from users where username = ?) ;","delete_property");
        PreparedStatement ps = cache.get("delete_property");
        Savepoint savepoint = null;

        try {
            savepoint = sql.createSavePoint("delete_property");
            ps.setString(1, Sql.escape(name));
            ps.setString(2, Sql.escape(username));
            if (ps.executeUpdate() > 0) {
                sql.commit();
                prop.removeProperty(name, username);
                return true;
            }

        } catch (SQLException e) {
            log.warning("Problem deleting a property", e);
            try {
                sql.rollback(savepoint);
            } catch (SQLException e1) {
                log.severe("SQL transaction rollback failed", e1);
            }
        }

        return false;
    }




}
