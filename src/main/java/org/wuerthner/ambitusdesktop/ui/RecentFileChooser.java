package org.wuerthner.ambitusdesktop.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;

public class RecentFileChooser {

    public static class RecentFileList extends JPanel {

        private final JList<File> list;
        private final FileListModel listModel;
        private final JFileChooser fileChooser;

        public RecentFileList(JFileChooser chooser) {
            fileChooser = chooser;
            listModel = new FileListModel();
            list = new JList<>(listModel);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setCellRenderer(new FileListCellRenderer());

            setLayout(new BorderLayout());
            add(new JScrollPane(list));

            list.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        File file = list.getSelectedValue();
                        if (file.exists()) {
                            fileChooser.setSelectedFile(file);
                        }
                    }
                }
            });
        }

        public void clearList() {
            listModel.clear();
        }

        public void add(File file) {
            listModel.add(file);
        }

        public void load() {
            listModel.load();
        }

        public class FileListModel extends AbstractListModel<File> {

            private List<File> files;

            public FileListModel() {
                files = new ArrayList<>();
            }

            public void add(File file) {
                if (!files.contains(file)) {
                    if (files.isEmpty()) {
                        files.add(file);
                    } else {
                        files.add(0, file);
                    }
                    fireIntervalAdded(this, 0, 0);
                    save();
                }
            }

            public void clear() {
                int size = files.size() - 1;
                if (size >= 0) {
                    files.clear();
                    fireIntervalRemoved(this, 0, size);
                }
            }

            @Override
            public int getSize() {
                return files.size();
            }

            @Override
            public File getElementAt(int index) {
                return files.get(index);
            }

            public void save() {
                StringBuilder sb = new StringBuilder(128);
                for (int index = 0; index < listModel.getSize(); index++) {
                    File file = listModel.getElementAt(index);
                    if (sb.length() > 0) {
                        sb.append(File.pathSeparator);
                    }
                    sb.append(file.getPath());
                }
                Preferences p = Preferences.userNodeForPackage(RecentFileChooser.class);
                p.put("RectentFileList.fileList", sb.toString());
            }

            public void load() {
                Preferences p = Preferences.userNodeForPackage(RecentFileChooser.class);
                String listOfFiles = p.get("RectentFileList.fileList", null);
                if (listOfFiles != null) {
                    String[] files = listOfFiles.split(File.pathSeparator);
                    for (String fileRef : files) {
                        File file = new File(fileRef);
                        if (file.exists()) {
                            add(file);
                        }
                    }
                }
            }
        }

        public class FileListCellRenderer extends DefaultListCellRenderer {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof File) {
                    File file = (File) value;
                    Icon ico = FileSystemView.getFileSystemView().getSystemIcon(file);
                    setIcon(ico);
                    setToolTipText(file.getParent());
                    setText(file.getName());
                }
                return this;
            }

        }
    }
}
