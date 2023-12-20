import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.StringSelection;

import java.io.StringBufferInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;


public class QueryOutput extends JFrame {
    JTextArea editorPaneOutput;
    
    public QueryOutput() {
        this.setTitle("Query Output");
        
        JPanel panelCenter = new JPanel();
        getContentPane().add(panelCenter, BorderLayout.CENTER);
        panelCenter.setLayout(new BoxLayout(panelCenter, BoxLayout.X_AXIS));
    
        JScrollPane scrollPane = new JScrollPane();
        panelCenter.add(scrollPane);
            
        editorPaneOutput = new JTextArea();
        editorPaneOutput.setFont(new Font("Ubuntu Mono", Font.PLAIN, 16));
        setSize(650,700);
        setLocation(660,10);
        panelCenter.add(editorPaneOutput);
        scrollPane.setViewportView(editorPaneOutput);
        
        JPopupMenu popuMenu = new JPopupMenu();
        JMenuItem menuItem;
        
        menuItem = new JMenuItem("Copy as HTML");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenuCopyAsHTML();
            }
        });
        
        popuMenu.add(menuItem);
        
           menuItem = new JMenuItem("Copy as Google Meet Markdown");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                popupMenuCopyAsMarkdown();
            }
        });
        
        popuMenu.add(menuItem);
        
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
        editorPaneOutput.addMouseListener(popupMenuListener);
    }
    
    private static class HtmlSelection implements Transferable {

        private static java.util.List<DataFlavor> htmlFlavors = new ArrayList<>(3);

        static {

            try {
                htmlFlavors.add(new DataFlavor("text/html;class=java.lang.String"));
                htmlFlavors.add(new DataFlavor("text/html;class=java.io.Reader"));
                htmlFlavors.add(new DataFlavor("text/html;charset=unicode;class=java.io.InputStream"));
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

        }

        private String html;

        public HtmlSelection(String html) {
            this.html = html;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return (DataFlavor[]) htmlFlavors.toArray(new DataFlavor[htmlFlavors.size()]);
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return htmlFlavors.contains(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (String.class.equals(flavor.getRepresentationClass())) {
                return html;
            } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                return new StringReader(html);
            } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                return new StringBufferInputStream(html);
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }

    private void popupMenuCopyAsHTML() {
        String outputText = new String();

        outputText = editorPaneOutput.getSelectedText();

        if(outputText == null) {
            outputText = editorPaneOutput.getText();
        }
    
        outputText = outputText.replace("\n", "<br>").replace(" ", "&nbsp;");

        StringBuilder sb = new StringBuilder(64);
        sb.append("<html><body>");
        sb.append("<p style = 'font-family:Ubuntu Mono;font-size:12px;'>");
        sb.append("<span style = 'color:#21618C;background:#ffffff;'>");
        sb.append(outputText);
        sb.append("</span>");
        sb.append("</p>");        
        sb.append("</body></html>");
        
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new HtmlSelection(sb.toString()), null);
    }
    
    private void popupMenuCopyAsMarkdown() {
        String outputText = new String();

        outputText = editorPaneOutput.getSelectedText();

        if(outputText == null) {
            outputText = editorPaneOutput.getText();
        }
    
        outputText = "```" + outputText.replace("\n\n\n", "\n") + "```";
        
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(outputText), null);
    }
}
