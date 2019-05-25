package com.guy.burton.looper;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.URI;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.util.StringTokenizer;

public class FileDropPanel extends JPanel {

    private final JLabel filelabel;
    private File file;

    public FileDropPanel()
    {
        super.setBackground(Color.GRAY);
        super.setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Transferable transferable = dtde.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> transferData = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        if (!transferData.isEmpty()) {
                            setFile(transferData.get(0));
                        }
                        return;
                    }
                    DataFlavor nixFileDataFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
                    String data = (String) transferable.getTransferData(nixFileDataFlavor);
                    for(StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens();)
                    {
                        String token = st.nextToken().trim();
                        if(token.startsWith("#") || token.isEmpty())
                        {
                            // comment line, by RFC 2483
                            continue;
                        }
                        setFile(new File(new URI(token)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
        filelabel = new JLabel("Drop audio file here");
        add(filelabel);
    }
    
    public void setFile(File file)
    {
        this.file = file;
        filelabel.setText(file.getName());
    }

    public File getFile() {
        return file;
    }
}
