import java.awt.*;
import java.awt.List;
import java.util.*;
import java.io.*;

import javax.swing.*;

import java.awt.event.*;
import java.sql.*;

import javax.swing.event.*;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class QueryBuilder extends JFrame implements WindowListener,
        ClipboardOwner {
    private static final long serialVersionUID = 1L;
    private JTextField textFieldTable;
    private JTextField textFieldColumn;
    private JList listDatabases;
    private JList listSchemas;
    private JList listTables;
    private JList listTablesRecent;
    private JList listColumns;
    private JTextArea editorPaneQuery;
    
    private JCheckBoxMenuItem queryInOutputCheckBoxMenuItem;
    private JCheckBoxMenuItem transposeInOutputCheckBoxMenuItem;
    private QueryOutput frameOutput;
    
    private String key = new String("*****"); 

    Connection connection_meta;
    Map<String, Connection> connections_target = new HashMap<String, Connection>();

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    QueryBuilder frameBuilder = new QueryBuilder();
                    frameBuilder.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public QueryBuilder() {
        setTitle("Query Builder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650,700);
        setLocation(10,10);
        getContentPane().setLayout(new BorderLayout(0, 0));
        addWindowListener(this);

        JPanel panelNorth = new JPanel();
        getContentPane().add(panelNorth, BorderLayout.NORTH);
        panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.X_AXIS));

        Component horizontalStrut_1 = Box.createHorizontalStrut(20);
        panelNorth.add(horizontalStrut_1);

        textFieldTable = new JTextField();
        panelNorth.add(textFieldTable);
        textFieldTable.setColumns(10);

        textFieldTable.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        warn();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        warn();
                    }

                    public void insertUpdate(DocumentEvent e) {
                        warn();
                    }

                    public void warn() {

                        if (listSchemas.getSelectedIndex() != -1) {
                            refreshTableList(listDatabases.getSelectedValue().toString(), listSchemas.getSelectedValue().toString(), textFieldTable.getText());
                        } else {
                            refreshTableList("", "", textFieldTable.getText());

                        }
                    }
                });

        Component horizontalStrut = Box.createHorizontalStrut(20);
        panelNorth.add(horizontalStrut);

        textFieldColumn = new JTextField();
        panelNorth.add(textFieldColumn);
        textFieldColumn.setColumns(10);

        Component horizontalStrut_2 = Box.createHorizontalStrut(20);
        panelNorth.add(horizontalStrut_2);

        JButton btnReferenceTable = new JButton("Reference Table");
        panelNorth.add(btnReferenceTable);

        btnReferenceTable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String column = listColumns.getSelectedValue().toString();
                
                String table = new String();
                
                if (listTables.getSelectedIndex() != -1) {
                    table = listTables.getSelectedValue().toString();
                }
                else if (listTablesRecent.getSelectedIndex() != -1) {
                    table = listTablesRecent.getSelectedValue().toString();
                }
                

                try {
                    Statement stmt = connection_meta.createStatement();
                    ResultSet rs = stmt
                            .executeQuery("SELECT pk_table_name FROM relationships WHERE fk_column_name = '"
                                    + column.substring(0, column.indexOf(' '))
                                    + "' AND fk_table_name = '"
                                    + table
                                    + "'");

                    while (rs.next()) {
                        String ref_table = rs.getString("pk_table_name");
                        
                        DefaultListModel listModel = (DefaultListModel) listTablesRecent.getModel();
                        
                        if (!listModel.contains(ref_table)) {
                            listModel.addElement(ref_table);
                        }
                    }

                    rs.close();
                    stmt.close();

                } catch (Exception ex) {
                    System.out.println("Reference Extract:" + ex.getMessage());
                }

            }
        });

        JButton btnColumnLookup = new JButton("Colum Lookup");
        panelNorth.add(btnColumnLookup);

        btnColumnLookup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                DefaultListModel listModel = new DefaultListModel();
                String column = listColumns.getSelectedValue().toString();

                listModel.addElement("%");
                textFieldTable.setText("");

                try {
                    Statement stmt = connection_meta.createStatement();
                    ResultSet rs = stmt
                            .executeQuery("SELECT DISTINCT c.table_name FROM columns c WHERE c.column_name = '"
                                    + column.substring(0, column.indexOf(' '))
                                    + "' AND c.schema_name LIKE '%"
                                    + listSchemas.getSelectedValue().toString()
                                    + "%'");

                    while (rs.next()) {
                        listModel.addElement(rs.getString("table_name").replace("?", ""));
                    }
                    listTables.setModel(listModel);

                    rs.close();
                    stmt.close();

                } catch (Exception ex) {
                    System.out.println("Column Lookup: " + ex.getMessage());
                }

            }
        });

        textFieldColumn.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        warn();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        warn();
                    }

                    public void insertUpdate(DocumentEvent e) {
                        warn();
                    }

                    public void warn() {
			String database = "";
                        String schema_table = "";
                        String schema = "";
                        String table = "";
                        
			if (listDatabases.getSelectedIndex() != -1)
                            database = listDatabases.getSelectedValue().toString();
                        
                        if (listSchemas.getSelectedIndex() != -1)
                            schema = listSchemas.getSelectedValue().toString();
                        
                        if (listTables.getSelectedIndex() != -1)
                            table = listTables.getSelectedValue().toString();
                        
                        if (listTablesRecent.getSelectedIndex() != -1) {
                            schema_table = listTablesRecent.getSelectedValue().toString();
                            String[] schema_table_split = schema_table.split("\\.");
                            schema = schema_table_split[0];
                            table = schema_table_split[1];
                        }
                        
                        refreshColumnList(database, schema, table, textFieldColumn.getText());
                    }
                });

        JPanel panelCenter = new JPanel();
        getContentPane().add(panelCenter, BorderLayout.CENTER);
        panelCenter.setLayout(new BoxLayout(panelCenter, BoxLayout.X_AXIS));


        // DATABASES
        JScrollPane scrollPaneDatabases = new JScrollPane();
        panelCenter.add(scrollPaneDatabases);

        listDatabases = new JList();
        listDatabases.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listDatabases.setFont(new Font("Menlo", Font.PLAIN, 16));
        scrollPaneDatabases.setViewportView(listDatabases);

        ListSelectionListener listSelectionListenerDatabases = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                boolean adjust = listSelectionEvent.getValueIsAdjusting();
                if (!adjust) {
                    JList list = (JList) listSelectionEvent.getSource();

                    int index = list.getSelectedIndex();
                    if (index >= 0) {
                        Object o = list.getModel().getElementAt(index);
                        refreshSchemaList(o.toString());
                    }
                }
            }
        };
        listDatabases.addListSelectionListener(listSelectionListenerDatabases);

        // SCHEMAS
        JScrollPane scrollPaneSchemas = new JScrollPane();
        panelCenter.add(scrollPaneSchemas);

        listSchemas = new JList();
        listSchemas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSchemas.setFont(new Font("Menlo", Font.PLAIN, 16));
        scrollPaneSchemas.setViewportView(listSchemas);

        ListSelectionListener listSelectionListenerSchemas = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                boolean adjust = listSelectionEvent.getValueIsAdjusting();
                if (!adjust) {
                    JList list = (JList) listSelectionEvent.getSource();

                    int index = list.getSelectedIndex();
                    if (index >= 0) {
                        Object o = list.getModel().getElementAt(index);
                        refreshTableList(listDatabases.getSelectedValue().toString(), o.toString(), textFieldTable.getText());
                    }
                }
            }
        };
        listSchemas.addListSelectionListener(listSelectionListenerSchemas);

        // TABLES
        JPanel panelCenterTables = new JPanel();
        panelCenterTables.setLayout(new BoxLayout(panelCenterTables, BoxLayout.Y_AXIS));
        panelCenter.add(panelCenterTables);
                
        JSplitPane splitPaneTables = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPaneTables.setDividerLocation(500);

        JScrollPane scrollPaneTables = new JScrollPane();
        JScrollPane scrollPaneTablesRecent = new JScrollPane();

        splitPaneTables.setTopComponent(scrollPaneTables);
        splitPaneTables.setBottomComponent(scrollPaneTablesRecent);
        
        panelCenterTables.add(splitPaneTables);

        listTables = new JList();
        listTables.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listTables.setFont(new Font("Menlo", Font.PLAIN, 16));
        scrollPaneTables.setViewportView(listTables);
        
        listTablesRecent = new JList();
        listTablesRecent.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listTablesRecent.setFont(new Font("Menlo", Font.PLAIN, 16));
        DefaultListModel listModelTablesRecent = new DefaultListModel();
        listTablesRecent.setModel(listModelTablesRecent);
        scrollPaneTablesRecent.setViewportView(listTablesRecent);

        ListSelectionListener listSelectionListenerTables = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                boolean adjust = listSelectionEvent.getValueIsAdjusting();
                if (!adjust) {
                    JList list = (JList) listSelectionEvent.getSource();

                    int index = list.getSelectedIndex();
                    if (index >= 0) {
                        Object o = list.getModel().getElementAt(index);
                        refreshColumnList(
				listDatabases.getSelectedValue().toString(),
				listSchemas.getSelectedValue().toString(),
				o.toString(),
                                textFieldColumn.getText());

                        setClipboardContents(o.toString());
                        
                        listTablesRecent.clearSelection();
                    }
                }
            }
        };
        listTables.addListSelectionListener(listSelectionListenerTables);
        
        ListSelectionListener listSelectionListenerTablesRecent = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                boolean adjust = listSelectionEvent.getValueIsAdjusting();
                if (!adjust) {
                    JList list = (JList) listSelectionEvent.getSource();

                    int index = list.getSelectedIndex();
                    if (index >= 0) {
                        String schema_table = list.getModel().getElementAt(index).toString();
                        String[] schema_table_split = schema_table.split("\\.");                     
                         
                        refreshColumnList(
					  listDatabases.getSelectedValue().toString(),
					  schema_table_split[0],
                                          schema_table_split[1],
					  textFieldColumn.getText());
                        
                        setClipboardContents(schema_table_split[1]);
                        listTables.clearSelection();
                    }
                }
            }
        };
        listTablesRecent.addListSelectionListener(listSelectionListenerTablesRecent);

        MouseListener mouseListenerTableList = new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JList list = (JList) mouseEvent.getSource();
                
                if (mouseEvent.getClickCount() == 2
                        && listSchemas.getSelectedIndex() > 0) {
                    int index = list.locationToIndex(mouseEvent.getPoint());
                    if (index >= 1) {
                        String table = list.getModel().getElementAt(index).toString();
                        
                        String schema_table = listSchemas.getSelectedValue().toString()+'.'+ table;
                        String schema_table_alias = schema_table + ' ' + alias(table);                        

                        DefaultListModel listModel = (DefaultListModel) listTablesRecent.getModel();
                        if (!listModel.contains(schema_table)) {
                            listModel.addElement(schema_table);
                        }

                        editorPaneQuery.replaceSelection(schema_table_alias);
                        editorPaneQuery.grabFocus();
                        editorPaneQuery.requestFocus();
                    }
                }
            }
        };
        listTables.addMouseListener(mouseListenerTableList);

        MouseListener mouseListenerTableRecentList = new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JList list = (JList) mouseEvent.getSource();

                if (mouseEvent.getClickCount() == 2) {
                    int index = list.locationToIndex(mouseEvent.getPoint());
                    if (index >= 0) {
                        String schema_table = list.getModel().getElementAt(index).toString();
                       
                        String[] schema_table_split = schema_table.split("\\.");                     
                         String schema_table_alias = schema_table + ' ' + alias(schema_table_split[1]);

                        editorPaneQuery.replaceSelection(schema_table_alias);
                        editorPaneQuery.grabFocus();
                        editorPaneQuery.requestFocus();

                    }
                }
            }
        };
        listTablesRecent.addMouseListener(mouseListenerTableRecentList);

        // COLUMNS
        JScrollPane scrollPaneColumns = new JScrollPane();
        panelCenter.add(scrollPaneColumns);

        listColumns = new JList();
        listColumns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listColumns.setFont(new Font("Menlo", Font.PLAIN, 16));
        listColumns.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        scrollPaneColumns.setViewportView(listColumns);

        ListSelectionListener listSelectionListenerColumns = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                boolean adjust = listSelectionEvent.getValueIsAdjusting();
                if (!adjust) {
                    JList list = (JList) listSelectionEvent.getSource();

                    int index = list.getSelectedIndex();
                    if (index >= 0) {
                        String column = list.getModel().getElementAt(index)
                                .toString();
                        setClipboardContents(column.substring(0,
                                column.indexOf(' ')));
                    }
                }
            }
        };
        listColumns.addListSelectionListener(listSelectionListenerColumns);

        MouseListener mouseListenerColumnList = new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();

                if (mouseEvent.getClickCount() == 2
                        && (listTables.getSelectedIndex() > 0 || listTablesRecent.getSelectedIndex() >= 0)) {
                    int index = theList.locationToIndex(mouseEvent.getPoint());
                    if (index >= 0) {
                        
                        String column = theList.getModel().getElementAt(index).toString();
                        
                        String table = new String();
                        
                        if (listTables.getSelectedIndex() != -1) {
                            table = listTables.getSelectedValue().toString();
                        }
                        else if (listTablesRecent.getSelectedIndex() != -1) {
                            String schema_table = listTablesRecent.getSelectedValue().toString();
                            String[] schema_table_split = schema_table.split("\\.");
                            table = schema_table_split[1]; 
                        }
                        
                        column = alias(table)
                                + '.'
                                + column.substring(0, column.indexOf(' '));

                        editorPaneQuery.replaceSelection(column);
                        editorPaneQuery.grabFocus();
                        editorPaneQuery.requestFocus();
                    }
                }
            }
        };
        listColumns.addMouseListener(mouseListenerColumnList);

        listColumns.setCellRenderer(new DefaultListCellRenderer() {

            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value,
                        index, isSelected, cellHasFocus);

                setText(value.toString());
                if (value.toString().contains("_KEY")
                        || value.toString().contains("PERSONID")) {
                    setForeground(new Color(23, 150, 140));
                } else if (value.toString().contains("KEY_ID")) {
                    setForeground(new Color(255, 125, 0));
                }
                return c;
            }

        });

        JPanel panelSouth = new JPanel();
        getContentPane().add(panelSouth, BorderLayout.SOUTH);
        panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.X_AXIS));
        
        JScrollPane scrollPaneEditor = new JScrollPane();
        panelSouth.add(scrollPaneEditor);

        editorPaneQuery = new JTextArea();
        editorPaneQuery.setForeground(Color.BLUE);
        editorPaneQuery.setFont(new Font("Menlo", Font.PLAIN, 16));
        
        String defaultText = "\n";
        for (int i = 1; i < 10; i++) {
            defaultText = defaultText + "\n";
        }
        editorPaneQuery.setText(defaultText);
        
        scrollPaneEditor.setViewportView(editorPaneQuery);
        
        JPopupMenu popuMenu;
        JMenuItem menuItem;
        
        popuMenu = new JPopupMenu();
        
    menuItem = new JMenuItem("Run");
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenuActionRunQuery();
            }
        });
      popuMenu.add(menuItem);

    menuItem = new JMenuItem("Copy");
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenuActionCopy();
            }
        });
      popuMenu.add(menuItem);
        
    menuItem = new JMenuItem("Format Lowcase");
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenuActionFormatSQL(true);
            }
        });
      popuMenu.add(menuItem);
    
      
    menuItem = new JMenuItem("Format Upcase");
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenuActionFormatSQL(false);
            }
        });
      popuMenu.add(menuItem);
        
      popuMenu.addSeparator();
        
    menuItem = new JMenuItem("Generate Select");
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenuActionGenerateSelect();
            }
        });
      popuMenu.add(menuItem);
      
    menuItem = new JMenuItem("Generate Select Count");
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenuActionGenerateSelectCount();
            }
        });
      popuMenu.add(menuItem);
      
      menuItem = new JMenuItem("Generate Frequency");
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenuActionGenerateFrequency();
            }
        });
      popuMenu.add(menuItem);
        
    menuItem = new JMenuItem("Generate Join");
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenuActionGenerateJoin();
            }
        });
      popuMenu.add(menuItem);
    
        
    menuItem = new JMenuItem("Make List");
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenuActionMakeList();
            }
        });
      popuMenu.add(menuItem);
        
      popuMenu.addSeparator();
        
    menuItem = new JMenuItem("Save SQL");
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedSQL = editorPaneQuery.getSelectedText();
                
                if (selectedSQL != null) {
                    saveSQL(selectedSQL);
                }
            }
        });
      popuMenu.add(menuItem);
        
    menuItem = new JMenuItem("Get SQL");
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                String selectedName = new String();
                
                if (listColumns.getSelectedIndex() != -1) {
                    selectedName = listColumns.getSelectedValue().toString();
                    selectedName = selectedName.substring(0, selectedName.indexOf(' '));
                }else if (listTables.getSelectedIndex() != -1) {
                    selectedName = listTables.getSelectedValue().toString();
                }
                
                if (selectedName.compareTo("") != 0) {
                    String sql;
                    sql = getSQL(selectedName);
                    
                    if (sql.compareTo("") != 0) {
                        editorPaneQuery.replaceSelection(sql);
                    }
                }
            }
        });
      popuMenu.add(menuItem);
        
      popuMenu.addSeparator();
        
    menuItem = new JMenuItem("Clear Output");
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearOutput();
            }
        });
        popuMenu.add(menuItem);
        
        popuMenu.addSeparator();
        
        queryInOutputCheckBoxMenuItem = new JCheckBoxMenuItem("Query In Output");
        popuMenu.add(queryInOutputCheckBoxMenuItem);
        
        transposeInOutputCheckBoxMenuItem = new JCheckBoxMenuItem("Transpose In Output");
        popuMenu.add(transposeInOutputCheckBoxMenuItem);
        
        MouseListener popupMenuListener = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popuMenu.show(e.getComponent(),
                               e.getX(), e.getY());
                }
          }
        };
        editorPaneQuery.addMouseListener(popupMenuListener);

        connect();

        refreshDatabaseList();
        listSchemas.setSelectedValue("DATALAKE", true);
        //refreshTableList("DATALAKE", "", "");
        // refreshColumnList("", "", "");

    }

    private void connect() {

        try {            
            // SQLite
            DriverManager.registerDriver ((Driver)Class.forName("org.sqlite.JDBC").newInstance());
            connection_meta = DriverManager.getConnection ("jdbc:sqlite:QueryBuilder.sqlite");
            
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "test");
        }

    }

    private void refreshDatabaseList() {
        DefaultListModel listModel = new DefaultListModel();

        listModel.addElement("%");

        try {
            Statement stmt = connection_meta.createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT distinct database_name from columns ORDER BY database_name");
    
            while (rs.next()) {
                listModel.addElement(rs.getString("database_name").replace("?",""));
            }
            listDatabases.setModel(listModel);

            rs.close();
            stmt.close();

        } catch (Exception ex) {
            System.out.println("Schema Extract: " + ex.getMessage()+ " " +ex.getClass());
        }
    }

    private void refreshSchemaList(String filterDatabase) {
        DefaultListModel listModel = new DefaultListModel();

        listModel.addElement("%");

        try {
            Statement stmt = connection_meta.createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT distinct schema_name from columns WHERE database_name LIKE '%"
                            + filterDatabase
                            + "%' ORDER BY schema_name");
    
            while (rs.next()) {
                listModel.addElement(rs.getString("schema_name").replace("?",""));
            }
            listSchemas.setModel(listModel);

            rs.close();
            stmt.close();

        } catch (Exception ex) {
            System.out.println("Schema Extract: " + ex.getMessage()+ " " +ex.getClass());
        }
    }

    private void refreshTableList(String filterDatabase, String filterSchema, String filterTable) {
        DefaultListModel listModel = new DefaultListModel();

        listModel.addElement("%");

        try {
            Statement stmt = connection_meta.createStatement();
            
            ResultSet rs = stmt
                    .executeQuery("SELECT DISTINCT table_name FROM columns WHERE "
                            + "database_name LIKE '" + filterDatabase + "' "
                            + "and schema_name LIKE '" + filterSchema + "' "
                            + "and table_name LIKE '%" + filterTable + "%' "
                            + "ORDER BY table_name");            

            while (rs.next()) {
                listModel.addElement(rs.getString("table_name").replace("?", ""));
            }
            listTables.setModel(listModel);

            rs.close();
            stmt.close();

        } catch (Exception ex) {
            System.out.println("Tables Extract:" + ex.getMessage()+ " " +ex.getClass());
        }
    }

    private void refreshColumnList(String filterDatabase, String filterSchema, String filterTable,
            			  String filterColumn) {
        DefaultListModel listModel = new DefaultListModel();

        String query;

        try {
            Statement stmt = connection_meta.createStatement();

            if (filterTable.compareTo("%") != 0) {
                if (filterColumn.compareTo("") == 0) {
                    query = "SELECT column_name, data_type, nullable, column_id FROM columns WHERE"
                            + " database_name LIKE '"
			    + filterDatabase
                            + "' and schema_name LIKE '"
                            + filterSchema
                            + "' and table_name LIKE '"
                            + filterTable
                            + "' and column_name LIKE '%"
                            + filterColumn + "%' ORDER BY column_id";
                } else {
                    query = "SELECT column_name, data_type, nullable, column_id FROM columns WHERE"
                            + " database_name LIKE '"
			    + filterDatabase
                            + "' and schema_name LIKE '"
                            + filterSchema
                            + "' and table_name LIKE '"
                            + filterTable
                            + "' and column_name LIKE '%"
                            + filterColumn + "%' ORDER BY column_name";
                }
            } else {
                query = "SELECT * FROM (SELECT column_name, data_type, nullable, count(*) as column_id FROM columns WHERE"
                        + " database_name LIKE '%"
		    	+ filterDatabase
			+ "%' and schema_name LIKE '%"
                        + filterSchema
                        + "%' and table_name LIKE '%"
                        + filterTable
                        + "%' and column_name LIKE '%"
                        + filterColumn
                        + "%' GROUP BY column_name, data_type, nullable"
                        + " ORDER BY 1, 4 DESC) LIMIT 100";
            }

            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                listModel.addElement(
                          rpad(rs.getString("column_name"), 50, ' ')
                        + rpad(rs.getString("data_type"), 26, ' ')
                        + rpad(rs.getString("nullable"), 10, ' ')
                        + Integer.toString(rs.getInt("column_id")));  
            }
            listColumns.setModel(listModel);

            rs.close();
            stmt.close();

        } catch (Exception ex) {
            System.out.println("Column Extract: " + ex.getMessage()+ " " +ex.getClass());
        }
    }

    private String alias(String name) {

        if (name == null)
          return "";
          
        // ignore common suffixes
        name = name.replace("_ORC", "")
                   .replace("_SUM", "")
                   .replace("_VIEW", "")
                   .replace("_STG", "")
                   .replace("_V1", "")
                   .replace("_V2", "")
                   .replace("_V3", "")
                   .replace("_V4", "")
                   .replace("_bv", "")
                   ;
          
        String aliastmp;
        
        // find last two underscores
        int index2 = name.lastIndexOf('_') - 1;
        int index1 = name.lastIndexOf('_', index2);
        if (index2 == -2) index2 += 1;
        
        // get characters following undescores
        aliastmp = name.substring(index1 + 1, index1 + 2) + name.substring(index2 + 2, index2 + 3);
        
        // in case of reserved word
        switch (aliastmp) {
            case "AS": aliastmp = "SA"; break;
            case "OF": aliastmp = "FO"; break;
            case "IS": aliastmp = "SI"; break;
            case "OR": aliastmp = "RO"; break;
            case "IN": aliastmp = "NI"; break;
            case "DO": aliastmp = "OD"; break;
        }

        return aliastmp.toLowerCase();
    }

    public static String rpad(String str, Integer length, char car) {
        return str
                + String.format("%" + (length - str.length()) + "s", "")
                        .replace(" ", String.valueOf(car));
    }

    public void windowClosing(WindowEvent arg0) {
        try {
            // disconnect from metadata database (sqlite)
            connection_meta.close();
            
            // disconnect from target databases
            Connection connections_target_target;
            for (String database : connections_target.keySet()) {
                System.out.println("Disconnecting from ... " + database);    
                connections_target_target = connections_target.get(database);
                connections_target_target.close();
            }    
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void setClipboardContents(String aString) {
        StringSelection stringSelection = new StringSelection(aString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    public void lostOwnership(Clipboard aClipboard, Transferable aContents) {
        // do nothing
    }

    public void windowActivated(WindowEvent arg0) {
    }

    public void windowClosed(WindowEvent arg0) {
    }

    public void windowDeactivated(WindowEvent arg0) {
    }

    public void windowDeiconified(WindowEvent arg0) {
    }

    public void windowIconified(WindowEvent arg0) {
    }

    public void windowOpened(WindowEvent arg0) {
    }

    private void popupMenuActionCopy() {
        String editorText = new String();

            editorText = editorPaneQuery.getSelectedText();

            if(editorText == null) {
                editorText = editorPaneQuery.getText();
            }
        setClipboardContents(editorText.trim());
        //setClipboardContents(editorText.trim().replace("\n", "").replace("\r", ""));        
    }
    
    private void popupMenuActionFormatSQL(boolean inLowerCase) {
        boolean isSelection = true;
        
        java.util.List<String> keywords = java.util.List.of("SELECT", "FROM", "WHERE", "AND", "ORDER", "GROUP", "HAVING");
        
        String editorText = new String();
        editorText = editorPaneQuery.getSelectedText();

        if(editorText == null) {
            editorText = editorPaneQuery.getText();
            isSelection = false;
        }
        
        editorText = editorText.trim();
        editorText = editorText.replace("\n", " ");
        StringTokenizer tokens = new StringTokenizer(editorText, " "); 
        
        String token;
        String sql = new String(""); 
        while(tokens.hasMoreTokens()) {             
            token = tokens.nextToken();
            
            // uppercase except text constants
            if(token.indexOf("'") < 0) {
                if (inLowerCase) {
                    // don't change case (BQ)
                    //token = token.toLowerCase();
                }
                else {
                    token = token.toUpperCase();
                }
            }
            
            // columns on new line each
            if(token.indexOf(".") > 0 && token.indexOf(",") > 0) {
                token = token.replace(",", ",\n ");
            }                
          
            // keywords on new line
            if (keywords.indexOf(token.toUpperCase()) > 0) {
               sql += "\n";
            }
            else {
                sql += " ";
            }
            
            // intend AND           
            if(token.toUpperCase().compareTo("AND") == 0)
                token = "    " + token;
            
            // add token to sql stmt           
            sql += token;
            
            // first column on new line
            if(token.toUpperCase().compareTo("SELECT") == 0)
                sql += "\n ";
        } 
        
        sql = sql.trim();
    
        if (isSelection) {
            editorPaneQuery.replaceSelection(sql);
        } else {
            editorPaneQuery.setText("\n\n" + sql);
        }
    }
    
    private void popupMenuActionRunQuery() {
        String editorText = new String();
        String output;
    
        if(frameOutput == null) {
            frameOutput = new QueryOutput();
            frameOutput.setVisible(true);
            output = new String();
        }
        else {
            frameOutput.setVisible(true);
            output = frameOutput.editorPaneOutput.getText();
            output += "\n";
        }

        editorText = editorPaneQuery.getSelectedText();

        if(editorText == null) {
            editorText = editorPaneQuery.getText();
        }
        
        String database = new String();
        String dbms = new String();
        String driver = new String();
        String url = new String();
        String user = new String();
        String password = new String();


        database = listDatabases.getSelectedValue().toString();

        // get database connection info
        try {
            Statement stmt = connection_meta.createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT * FROM databases WHERE database_name = '"+database+"'");

            while (rs.next()) {
                dbms = rs.getString("dbms");
                driver = rs.getString("driver");
                url = rs.getString("url");
                user = rs.getString("user");
                password = rs.getString("password");
            }
            
            if (password != null) {        
                password = decrypt(key, password);
            }
                    
            rs.close();
            stmt.close();
            
        } catch (Exception ex) {
            System.out.println("Database Lookup: " + ex.getMessage());
            System.exit(0);
        }        
            
        String sql = new String();
        String sqlWithOutTop;
        
        try {
            long stopwatch_start = System.currentTimeMillis();
            
            // check if already connected to target database
            Connection connection_target = connections_target.get(database);
            if (connection_target == null) {
                System.out.println("Connecting to ... " + database);
                DriverManager.registerDriver ((Driver)Class.forName(driver).newInstance());
                connection_target = DriverManager.getConnection(url, user, password);
                            
                // store connection
                connections_target.put(database, connection_target);
            }
            
            Statement stmt = connection_target.createStatement();
            stmt.setMaxRows(100);
            
            if(database.compareTo("DATALAKE") == 0) {
                    System.out.println("Setting Properties in ... " + database);
                    stmt.execute("set hive.vectorized.execution.enabled=false");
                    stmt.execute("set hiveconf:hive.exec.parallel=true");
                    stmt.execute("set hiveconf:hive.tez.auto.reducer.parallelism=true");
            }
                        
            ResultSet rs;
            ResultSetMetaData meta;
            
            int columncount;
            String row[];
            String header[];
            Integer datasize[];

            String[] sqls;
            
            ArrayList<String[]> data;
                
            sqls = editorText.trim().split(";");
            
            stmt = connection_target.createStatement();
            
            
            for (int sqli = 0; sqli < sqls.length; sqli++) {
                
                sql = sqls[sqli].trim();
                sqlWithOutTop = sql;
                
                // limit to 100 records
                if (dbms.compareTo("SQLSERVER") == 0) {
                    if(sql.indexOf("SELECT DISTINCT") == 0) {
                        sql = sql.replaceFirst("SELECT DISTINCT", "SELECT DISTINCT TOP(100)");
                    }
                    else {
                        sql = sql.replaceFirst("SELECT", "SELECT TOP(100)");
                    }
                }
                else if(dbms.compareTo("ORACLE") == 0) {
                    sql = "SELECT * FROM ("+sql+") TOP100 WHERE ROWNUM <= 100";
                }
                else if(dbms.compareTo("BIGQUERY") == 0 && sql.indexOf("LIMIT") == -1) {
                    sql = sql + " LIMIT 10";
                }
                
                rs = stmt.executeQuery(sql);
                
                long stopwatch_stop = System.currentTimeMillis();        
                long stopwatch_diff = (stopwatch_stop - stopwatch_start)/1000;
                
                meta = rs.getMetaData();
                
                columncount = meta.getColumnCount();
                    
                header = new String[columncount];
                datasize = new Integer[columncount];
                    
                data = new ArrayList<String[]>();
                
                for(int i=0; i<columncount; i++) {
                    header[i] = meta.getColumnLabel(i+1);
                    datasize[i] = header[i].length();
                }
                
                while (rs.next()) {
                    row = new String[columncount];
    
                    for (int i = 0; i < columncount; i++) {
    
                        if (rs.getObject(i + 1) == null) {
                            row[i] = "";
                        } else {
                            row[i] = rs.getObject(i + 1).toString();
                        }
    
                        if (datasize[i] < row[i].length()) {
                            datasize[i] = row[i].length();
                        }
                    }
                    data.add(row);
                }
    
                rs.close();
              
              // add source query to output
              if(queryInOutputCheckBoxMenuItem.getState()) {
                  output += sqlWithOutTop + ";\n\n";
              }
    
              // header
              for (int i=0; i<columncount; i++) {
                  output += String.format("%-"+datasize[i].toString()+"s", header[i]);
                  if (i<columncount-1) {
                      //output += "\t";
                      output += "  ";
                  }
              }
              
              output += "\n";
              
              // header separator
              for (int i=0; i<columncount; i++) {
                  output += String.format("%-"+datasize[i].toString()+"s", "-").replace(" ", "-");
                  if (i<columncount-1) {
                      //output += "\t";
                      output += "  ";
                  }
              }
              
              output += "\n";
              
               // data
               int rowcount = 0;
              
               for (String[] rowtemp : data) {
                for (int i=0; i<columncount; i++) {
                    output += String.format("%-"+datasize[i].toString()+"s", rowtemp[i]);
                    if (i<columncount-1) {
                        //output += "\t";
                        output += "  ";
                    }
                }
                output += "\n";
                rowcount++;
              }
              
              if(!queryInOutputCheckBoxMenuItem.getState()) {
                output += "\n";
                output += rowcount + " row(s) in " + parseDrivingTable(sqlWithOutTop) + " (" + database + ")  "  +  Long.toString(stopwatch_diff) + "s";
              }
              output += "\n\n";
              
              
              // transpose test
              if(transposeInOutputCheckBoxMenuItem.getState()) {
                int len = data.size();
                
                String outputTranspose = new String();
                if (len == 1) {
                    String[] rowtemp = data.get(0);
                    
                    int maxHeaderSize = 0;
                    for (int i=0; i<columncount; i++) {
                        if(header[i].length() > maxHeaderSize) {
                            maxHeaderSize = header[i].length();
                        }
                    }
                    
                    output += parseDrivingTable(sqlWithOutTop) + " (" + database + ")" + "\n";
                    output += String.format("%-50s", "-").replace(" ", "-") + "\n";
                            
                    for (int i=0; i<columncount; i++) {
                        output += String.format("%-"+maxHeaderSize+"s", header[i]) + " : " + rowtemp[i] + "\n";
                    }
    
                }
                
              }
                
            
            }

            
        stmt.close();
        
        frameOutput.editorPaneOutput.setText(output);

        } catch (Exception ex) {
            System.out.println(ex.getMessage()+ " " +ex.getClass());
            JOptionPane.showMessageDialog(this, ex.getMessage() + "\n\n" + sql, this.getTitle(), JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void popupMenuActionMakeList() {
        String clip;
        String[] lines;

        clip = editorPaneQuery.getSelectedText();

        lines = clip.split("\\n");

        Arrays.sort(lines);

        clip = "(\n";
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].trim().replace("\r", "");
            clip = clip + "'" + lines[i] + "'";
            if (i < lines.length - 1) {
                clip = clip + ",\n";
            }
        }
        clip = clip + "\n)";

        editorPaneQuery.replaceSelection(clip);
    }
    
    private void popupMenuActionGenerateSelect() {
        
        if (listSchemas.getSelectedIndex() != -1 && listTables.getSelectedIndex() != -1) {
            String sql = "SELECT ";
            
            String database = listDatabases.getSelectedValue().toString();
            String schema = listSchemas.getSelectedValue().toString();
            String table = listTables.getSelectedValue().toString();
        
            if (listColumns.getSelectedIndex() != -1) {
                java.util.List columns = listColumns.getSelectedValuesList();
    
                Iterator iterator = columns.iterator();
                while(iterator.hasNext()) {
                    String column = (String) iterator.next();
                    column = column.substring(0, column.indexOf(' '));
                    
                    sql = sql + alias(table)+"."+column;
   
                    if(iterator.hasNext()) {
                        sql = sql + ", ";
                
                    }
                }
                
            }
            else {
                sql = sql + "*";
            }
            
            sql = sql + "\nFROM ";
            
            DefaultListModel listModel = (DefaultListModel) listTablesRecent.getModel();
            if (!listModel.contains(schema+'.'+table)) {
                listModel.addElement(schema+'.'+table);
            }
            
            //BQ
            //table = schema + '.'+ table + ' ' + alias(table);
            table = '`' +  database + '.' + schema + '.'+ table + "` AS " + alias(table);
            sql = sql + table;
            
            editorPaneQuery.replaceSelection(sql);
            editorPaneQuery.grabFocus();
            editorPaneQuery.requestFocus();
        }
    }
    
    private void popupMenuActionGenerateSelectCount() {
        
        if (listSchemas.getSelectedIndex() != -1 && listTables.getSelectedIndex() != -1) {
            String sql = "SELECT COUNT(1) AS cnt \nFROM ";
            
            String database = listDatabases.getSelectedValue().toString();
            String schema = listSchemas.getSelectedValue().toString();
            String table = listTables.getSelectedValue().toString();
        
            DefaultListModel listModel = (DefaultListModel) listTablesRecent.getModel();
            if (!listModel.contains(schema+'.'+table)) {
                listModel.addElement(schema+'.'+table);
            }
            
            //BQ
            //table = schema + '.'+ table + ' ' + alias(table);
            table = '`' +  database + '.' + schema + '.'+ table + "` AS " + alias(table);
            sql = sql + table;
            
            editorPaneQuery.replaceSelection(sql);
            editorPaneQuery.grabFocus();
            editorPaneQuery.requestFocus();
        }
    }
    
    private void popupMenuActionGenerateFrequency() {
        
        if (listSchemas.getSelectedIndex() != -1 && listTables.getSelectedIndex() != -1 && listColumns.getSelectedIndex() != -1) {
            
            String database = listDatabases.getSelectedValue().toString();
            String schema = listSchemas.getSelectedValue().toString();
            String table = listTables.getSelectedValue().toString();
          java.util.List columns = listColumns.getSelectedValuesList();
    
      String column_list = "";    
            Iterator iterator = columns.iterator();
            while(iterator.hasNext()) {
                String column = (String) iterator.next();
                column = column.substring(0, column.indexOf(' '));
                
                column_list = column_list + alias(table)+"."+column;
 
                if(iterator.hasNext()) {
                    column_list = column_list + ", ";
            
                }
            }
                
            String sql = "SELECT " + column_list + ", COUNT(1) AS cnt" +
                        //BQ 
                        //"\nFROM " + schema + '.' + table + ' ' + alias(table) +
                         "\nFROM " + '`' +  database + '.' + schema + '.'+ table + "` AS " + alias(table) +
                         "\nGROUP BY " + column_list +
                         "\nORDER BY 1 DESC";
            
            DefaultListModel listModel = (DefaultListModel) listTablesRecent.getModel();
            if (!listModel.contains(schema+'.'+table)) {
                listModel.addElement(schema+'.'+table);
            }
                
            editorPaneQuery.replaceSelection(sql);
            editorPaneQuery.grabFocus();
            editorPaneQuery.requestFocus();
        }
    }
    
    private void popupMenuActionGenerateJoin() {
        
        
        if (listSchemas.getSelectedIndex() != 1 && listColumns.getSelectedIndex() != -1) {
            
            String schema = listSchemas.getSelectedValue().toString();
            
            String column = listColumns.getSelectedValue().toString();
            column = column.substring(0, column.indexOf(' '));
        
            String table = new String();
            
            if (listTables.getSelectedIndex() != -1) {
                table = listTables.getSelectedValue().toString();
            }
            else if (listTablesRecent.getSelectedIndex() != -1) {
                table = listTablesRecent.getSelectedValue().toString();
            }
            
            String sql;
            String selectclause = new String();
            String fromclause = new String();
            String whereclause = new String();
            
            sql = editorPaneQuery.getSelectedText();
            if (sql != null) {
                int selectendindex = 0;
                int fromendindex = 0;
                int whereendindex = 0;
                
                int selectstartindex = sql.indexOf("SELECT", 0);
                if ((selectendindex = sql.indexOf("FROM", selectstartindex)) != -1) {
                }
                else if ((selectendindex = sql.indexOf("\n", selectstartindex)) != -1) {
                }
                else {
                    selectendindex = sql.length();
                }
                
                int fromstartindex = sql.indexOf("FROM", 0);
                if ((fromendindex = sql.indexOf("WHERE", fromstartindex)) != -1) {
                }
                else if ((fromendindex = sql.indexOf("\n", fromstartindex)) != -1) {
                }
                else {
                    fromendindex = sql.length();
                }
                
                int wherestartindex = 0;
                if ((wherestartindex = sql.indexOf("WHERE", fromstartindex)) != -1) {
                    whereendindex = sql.length();
                }
                
                
                selectclause = sql.substring(selectstartindex, selectendindex);
                fromclause = sql.substring(fromstartindex, fromendindex);
                
                fromclause = fromclause.replace("\n", "");
                
                if (wherestartindex > 1 && whereendindex > wherestartindex) {
                    whereclause = sql.substring(wherestartindex, whereendindex);
                    whereclause = "\n" + whereclause + "\n" + "AND " +alias(table)+'.'+column;
                }
                else {
                    whereclause = "\nWHERE "+alias(table)+'.'+column;
                }
            }
            else {
                selectclause = "SELECT *\n";
                fromclause = "FROM "+schema+ '.'+ table + ' ' + alias(table);
                whereclause = "\nWHERE "+alias(table)+'.'+column;
            }
            
            
            DefaultListModel listModel = (DefaultListModel) listTablesRecent.getModel();
            if (!listModel.contains(schema+'.'+table)) {
                listModel.addElement(schema+'.'+table);
            }
    
            try {
                Statement stmt = connection_meta.createStatement();
                ResultSet rs = stmt
                        .executeQuery("SELECT pk_table_name, pk_column_name FROM relationships WHERE fk_column_name = '"
                                + column
                                + "' AND fk_table_name = '"
                                + table
                                + "'");
    
                while (rs.next()) {
                    String pk_table_name = rs.getString("pk_table_name");
                    String pk_column_name = rs.getString("pk_column_name");
                    
                    listModel = (DefaultListModel) listTablesRecent.getModel();
                    if (!listModel.contains(pk_table_name)) {
                        listModel.addElement(pk_table_name);
                    }
                    
                    fromclause = fromclause + ", " +schema + "." + pk_table_name + " " + alias(pk_table_name);
                    whereclause = whereclause + " = " + alias(pk_table_name) + "." + pk_column_name;
                }
                
                editorPaneQuery.replaceSelection(selectclause+fromclause+whereclause);
                editorPaneQuery.grabFocus();
                editorPaneQuery.requestFocus();
    
                rs.close();
                stmt.close();
    
            } catch (Exception ex) {
                System.out.println("Reference Extract:" + ex.getMessage());
            }
        }
    }
    
    private void saveSQL (String sql) {
        String insertStmt;
        
        sql = sql.replace("'", "''");
        
        try {
            Statement stmt = connection_meta.createStatement();
            
            insertStmt = "INSERT INTO sqllog VALUES (CURRENT_TIMESTAMP, '"+sql+";')";
            
            System.out.println(insertStmt);
            
            stmt.execute(insertStmt);
            stmt.close();

        }
        catch (Exception ex) {
            System.out.println("insertIntoSQLLog: " + ex.getMessage()+ " " +ex.getClass());
        }
        
    }
    
    private String getSQL(String searchText) {
        String sql = new String();
        
        try {
            Statement stmt = connection_meta.createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT SQLTEXT FROM SQLLOG WHERE SQLTEXT LIKE '%"+searchText+"%' ORDER BY SQLTIME DESC LIMIT 5");
            
            while(rs.next()) {
                sql = sql + rs.getString(1) + "\n\n";
            }
            
            rs.close();
            stmt.close();
            
            return(sql);    

        }
        catch (Exception ex) {
            System.out.println("insertIntoSQLLog: " + ex.getMessage()+ " " +ex.getClass());
            return("");
        }
        
    }
    
    private void clearOutput() {
        frameOutput.editorPaneOutput.setText("");
    }
    
    private String parseDrivingTable(String sql) {
        String table_name = new String();
        
        int startindex = 0;
        int endindex = 0;
    
        if ((startindex = sql.indexOf("FROM", startindex)) != -1) {
            if ((startindex = sql.indexOf(" ", startindex)) != -1) {
                if (
                    ((endindex = sql.indexOf(" ", startindex+1)) != -1) ||
                    ((endindex = sql.indexOf("\n", startindex)) != -1) ||
                    ((endindex = sql.indexOf(",", startindex)) != -1) ||
                    ((endindex = sql.indexOf(";", startindex)) != -1) ||
                    ((endindex = sql.length()) > startindex)
                    )
                 {
                    table_name= sql.substring(startindex+1, endindex);
                 }
            }
        }
        return table_name;
    }
    
    private byte[] hex2byte(String str)
    {
       str = str.toLowerCase();
       
       byte[] bytes = new byte[str.length() / 2];
       for (int i = 0; i < bytes.length; i++)
       {
          bytes[i] = (byte) Integer
                .parseInt(str.substring(2 * i, 2 * i + 2), 16);
       }
       return bytes;
    }
    
    private String decrypt(String secretKey, String encryptedText) throws Exception {
        byte[] salt = {
            (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03
        };
        int iterationCount = 19;
     
        KeySpec keySpec = new PBEKeySpec(secretKey.toCharArray(), salt, iterationCount);
        SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
        AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
        
        Cipher cipher = Cipher.getInstance(key.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
       
        byte[] enc = hex2byte(encryptedText);
        byte[] utf8 = cipher.doFinal(enc); 
        String plainStr = new String(utf8, "UTF-8");
        
        return plainStr;
    }
}
