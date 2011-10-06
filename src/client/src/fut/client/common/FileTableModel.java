/**
 *
 */
package fut.client.common;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import fut.common.FileObject;

/**
 * @author Peter Bielik
 * @version 1.0
 * @since 1.0 - 13.12.2010
 */
public class FileTableModel extends AbstractTableModel {
    private List<FileObject> data;
    int rows = 0;

    public void setFileData(List<FileObject> data) {
	System.err.println("FileTableModel : Data arrived : " + data.size());
	this.data = data;
	this.rows = data.size();
	fireTableDataChanged();
    }

    public byte[] getData(int index) {
	if (index > -1 && index < data.size()) {
	    return data.get(index).getData();
	}
	return new byte[0];
    }
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
	return rows;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
	return 3;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
	FileObject tmp = data.get(rowIndex);

	switch(columnIndex) {
	case 0: return tmp.getFilename();
	case 1: return tmp.getTime().toString();
	case 2: return tmp.getData().length;
	default: return "<unknowen>";
	}
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column) {
        switch (column) {
        case 0 : return "Name";
        case 1 : return "Time";
        case 2 : return "Size in bytes";
        default : return "<unknowen>";
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

}
