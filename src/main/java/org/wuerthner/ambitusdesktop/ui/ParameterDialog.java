package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.model.Arrangement;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JCheckBox;
import javax.swing.border.*;
import javax.swing.JFileChooser;
import javax.swing.SwingConstants;
import javax.swing.JList;
import javax.swing.BoxLayout;

/**
 *
 * ParameterDialog pd = new ParameterDialog( new String[]{"Test"},
 *  new String[]{"List", "Box 1", "Box 2"},
 *  new Object[]{new String[]{"[LIST]", "Hugo", "Emil", "Egon", "Hein", "Egon"}, new Boolean(true), new Boolean(true)},
 *  null);
 *
 * Parameters:
 *  String[] head:     Array of headlines. The first entry is treated specially
 *  String[] labels:   The labels of the requested input fields
 *  Object[] defaults: The default values - the object type defines the type of input field (see below)
 *  String[] tooltips: The corresponding tooltips
 *  Component parent:  The main window, the ParameterDialog is being centered at
 *
 * The type of presented widget depends on the type of the default object:
 *  String   - displays a TextField
 *  String[] - displays either a JComboBox or a JList (see below)
 *  Boolean  - displays a JCheckBox
 *  File     - displays a button to call a file selection dialog
 *
 * defaults String[]:
 * if defaults[0] is "[LIST]", a JList is displayed. defaults[1]...defaults[n-1] contain the options, defaults[n] contains the selected values, separated by "|"
 * otherwise a JComboBox is displayed. defaults[0]...defaults[n-1] contain the options, defaults[n] contains the selected value.
 **/
public class ParameterDialog extends JDialog {

    static final private String ACTION_FIELD           = "fd";
    static final private String ACTION_OK              = "ok";
    static final private String ACTION_CANCEL          = "cn";

    static final Border etchlBorder   = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),BorderFactory.createEmptyBorder(4,4,4,4));
    static final Border etchlBorder2  = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),BorderFactory.createEmptyBorder(0,0,0,0));
    static final Border loweredBorder = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),BorderFactory.createEmptyBorder(0,4,0,4));
    static final Border emptyBorder   = BorderFactory.createEmptyBorder(2,12,2,12);

    public static final Font BOLD_FONT    = new Font("Arial", Font.BOLD, 12);
    public static final Font PLAIN_FONT   = new Font("Arial", Font.PLAIN, 11);

    private JComponent[] _label;
    private JComponent[] _field;
    private JButton      _cancel;
    private JComponent   _head;

    private JFileChooser _file_chooser = new JFileChooser();

    private MainListener _ml  = new MainListener();
    private FocusListener _fl = new MainFocus();
    private String[] _param;
    private Component _parent = null;

    public ParameterDialog(String[] labels, Object[] defaults, Component parent) {
        _parent = parent;
        _head = new JLabel("Parameter Dialog");
        init(labels, defaults, labels);
    }

    public ParameterDialog(String[] labels, Object[] defaults, String[] tooltips, Component parent) {
        _parent = parent;
        _head = new JLabel("Parameter Dialog");
        init(labels, defaults, tooltips);
    }

    public ParameterDialog(String[] head, String[] labels, Object[] defaults, Component parent) {
        _parent = parent;
        _head = new JPanel();
        JLabel lab;
        ((JPanel) _head).setLayout(new GridLayout(0,1, 2, 2));
        lab = new JLabel(" "); lab.setFont(PLAIN_FONT);	((JPanel) _head).add(lab);
        for (int i=0; i<head.length; i++) {
            if (i==0) {
                lab = new JLabel(head[i], SwingConstants.CENTER);
                lab.setFont(BOLD_FONT);
                if (head.length>1) {
                    ((JPanel) _head).add(lab);
                    lab = new JLabel(" ");
                    lab.setFont(PLAIN_FONT);
                    setTitle(head[i]);
                }
            } else {
                lab =new JLabel(head[i]);
                lab.setFont(PLAIN_FONT);
            }
            ((JPanel) _head).add(lab);
        }
        lab = new JLabel(" "); lab.setFont(PLAIN_FONT);	((JPanel) _head).add(lab);
        init(labels, defaults, labels);
    }

    public ParameterDialog(String[] head, String[] labels, Object[] defaults, String[] tooltips, Component parent) {
        _parent = parent;
        _head = new JPanel();
        JLabel lab;
        ((JPanel) _head).setLayout(new GridLayout(0,1, 2, 2));
        lab = new JLabel(" "); lab.setFont(PLAIN_FONT);	((JPanel) _head).add(lab);
        for (int i=0; i<head.length; i++) {
            if (i==0) {
                lab = new JLabel(head[i], SwingConstants.CENTER);
                lab.setFont(BOLD_FONT);
                if (head.length>1) {
                    ((JPanel) _head).add(lab);
                    lab = new JLabel(" ");
                    lab.setFont(PLAIN_FONT);
                }
                setTitle(head[i]);
            } else {
                lab =new JLabel(head[i]);
                lab.setFont(PLAIN_FONT);
            }
            ((JPanel) _head).add(lab);
        }
        lab = new JLabel(" "); lab.setFont(PLAIN_FONT);	((JPanel) _head).add(lab);
        init(labels, defaults, tooltips);
    }

    private void init(String[] labels, Object[] defaults, String[] tooltips) {
        setModal(true);
        int fields = labels.length;
        _label = new JComponent[fields];
        _field = new JComponent[fields];
        for (int i=0; i<fields; i++) {
            if (defaults == null || (defaults[i] instanceof String)) {
                //
                // String
                //
                _label[i] = new JLabel(labels[i] + ": ", SwingConstants.RIGHT);
                _label[i].setName(labels[i]);
                ((JLabel) _label[i]).setToolTipText(tooltips[i]);
                _field[i] = new JTextField(24);
                ((JTextField)_field[i]).setActionCommand(ACTION_FIELD+i);
                ((JTextField)_field[i]).addActionListener( _ml );
                _field[i].setBorder(etchlBorder);
                if (defaults!=null)
                    ((JTextField)_field[i]).setText((String)defaults[i]);
                _field[i].addFocusListener( _fl );
            } else if (defaults[i] instanceof String[]) {
                //
                // String[] => Popup or List
                //
                if ( ((String[]) defaults[i]).length>0 && ((String[]) defaults[i])[0].equals("[LIST]") ) {
                    //
                    // List
                    //
                    _label[i] = new JLabel(labels[i] + ": ", SwingConstants.RIGHT);
                    _label[i].setName(labels[i]);
                    ((JLabel) _label[i]).setToolTipText(tooltips[i]);
                    Object[] param = (Object[]) defaults[i];                               // param: [LIST], P1, P2, P3, P4, ..., Pn, S1|S2|...|Sm
                    String[] list = new String[param.length-2];                            // list:          P1, P2, P3, P4, ..., Pn
                    // String[] vals = StringTools.slice((String)param[param.length-1], "|"); // vals:                                   S1, S2, ..., Sm
                    String[] vals = ((String)param[param.length-1]).split("|");
                    Arrays.sort(vals);
                    int ind = 0;
                    int[] indices = new int[vals.length];
                    for (int j=0; j<list.length; j++) { list[j] = (String) param[j+1]; if (Arrays.binarySearch(vals, list[j])>=0) indices[ind++] = j; }
                    _field[i] = new JList(list);
                    if (!((String)param[param.length-1]).equals("")) {
                        //
                        // set selection!
                        //
                        ((JList)_field[i]).setSelectedIndices(indices);
                    }
                    _field[i].setBorder(etchlBorder2);
                } else {
                    //
                    // Popup
                    //
                    _label[i] = new JLabel(labels[i] + ": ", SwingConstants.RIGHT);
                    _label[i].setName(labels[i]);
                    ((JLabel) _label[i]).setToolTipText(tooltips[i]);
                    Object[] param = (Object[]) defaults[i];
                    String[] list = new String[param.length-1];
                    for (int j=0; j<list.length; j++) list[j] = (String) param[j];
                    _field[i] = new JComboBox(list);
                    _field[i].setBorder(etchlBorder2);
                    ((JComboBox)_field[i]).setSelectedItem(param[list.length]);
                }
            } else if (defaults[i] instanceof Boolean) {
                //
                // Boolean
                //
                _label[i] = new JLabel(labels[i] + ": ", SwingConstants.RIGHT);
                _label[i].setName(labels[i]);
                ((JLabel) _label[i]).setToolTipText(tooltips[i]);
                _field[i] = new JCheckBox();
                ((JCheckBox)_field[i]).setActionCommand(ACTION_FIELD+i);
                ((JCheckBox)_field[i]).addActionListener( _ml );
                _field[i].setBorder(etchlBorder);
                if (defaults!=null)
                    ((JCheckBox)_field[i]).setSelected(((Boolean)defaults[i]).booleanValue());
            } else if (defaults[i] instanceof File) {
                //
                // File
                //
                _label[i] = new JPanel(new BorderLayout());
                _label[i].setName(labels[i]);
                JLabel lab = new JLabel(labels[i] + ": ", SwingConstants.RIGHT);
                JButton btn = new JButton("*");
                btn.setToolTipText(tooltips[i]);
                btn.setActionCommand(ACTION_FIELD+i);
                btn.addActionListener( _ml );
                ((JPanel)_label[i]).add(btn, BorderLayout.WEST);
                ((JPanel)_label[i]).add(lab, BorderLayout.EAST);
                String path = "";
                try {
                    path = ((File)defaults[i]).getAbsolutePath();
                } catch (Exception e) { e.printStackTrace(); }
                _field[i] = new JTextField(path);
                ((JTextField) _field[i]).setPreferredSize(new Dimension(120, (int) _field[i].getPreferredSize().getHeight()));
                ((JTextField) _field[i]).addActionListener( _ml );
                _field[i].setBorder(etchlBorder);
                _field[i].addFocusListener( _fl );
            }
            _field[i].setToolTipText(tooltips[i]);
            _field[i].setBackground(Color.lightGray);
        }
        // ok
        final JButton _ok = new JButton("OK");
        _ok.setActionCommand(ACTION_OK);
        // _ok.setDefaultCapable(true);
        _ok.addActionListener( _ml );
        getRootPane().setDefaultButton(_ok);

        // cancel
        _cancel = new JButton("CANCEL");
        _cancel.setActionCommand(ACTION_CANCEL);
        _cancel.addActionListener( _ml );

        //
        // build panel
        //

        JPanel switches = new JPanel();
        switches.setLayout(new BoxLayout(switches, BoxLayout.Y_AXIS));
        JPanel p;
        for (int i=0; i<fields; i++) {
            p = new JPanel(new GridLayout(1, 2, 5, 5));
            p.add(_label[i]); p.add(_field[i]);
            p.setBorder(emptyBorder);
            switches.add(p);
        }
        p = new JPanel(new GridLayout(1, 2, 5, 5));
        p.add(_ok);
        p.add(_cancel);
        p.setBorder(emptyBorder);
        switches.add(p);

        //
        // build GUI
        //
        BorderLayout bl = new BorderLayout(1, 1);
        getContentPane().setLayout(bl);
        getContentPane().add(_head, BorderLayout.NORTH);
        getContentPane().add(switches, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(_parent);
        setVisible(true);
    }

    public String[] getParameters() {
        return _param;
    }

    private class MainFocus implements FocusListener {
        public void focusGained(FocusEvent e) {
            JComponent f = (JComponent) e.getSource();
            f.setBackground(Color.gray);
            f.setForeground(Color.white);
            f.setBorder(loweredBorder);
        }

        public void focusLost(FocusEvent e) {
            JComponent f = (JComponent) e.getSource();
            f.setBackground(Color.lightGray);
            f.setForeground(Color.black);
            if (f instanceof JTextField)
                f.setBorder(etchlBorder);
            else
                f.setBorder(etchlBorder2);
        }
    }

    private class MainListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            String val = null;
            if (cmd.equals(ACTION_OK)) {
                _param = new String[_label.length];
                for (int i=0; i<_label.length; i++) {
                    if (_field[i] instanceof JTextField) {
                        _param[i] = ((JTextField) _field[i]).getText();
                        if (_param[i]==null) _param[i] = "";
                    } else if (_field[i] instanceof JComboBox) {
                        _param[i] = ((JComboBox) _field[i]).getSelectedItem().toString();
                    } else if (_field[i] instanceof JList) {
                        _param[i] = (String) ((JList) _field[i]).getSelectedValuesList().stream().collect(Collectors.joining("|"));
                    } else if (_field[i] instanceof JCheckBox)
                        _param[i] = ""+((JCheckBox) _field[i]).isSelected();
                }
                dispose();
            } else if (cmd.equals(ACTION_CANCEL)) {
                _param = null;
                dispose();
            } else if (cmd.startsWith(ACTION_FIELD)) {
                int index = Integer.valueOf(cmd.substring(ACTION_FIELD.length()));
                String title = _label[index].getName();
                boolean isfile = (_label[index] instanceof JPanel); // Panel means: file contents!
                if (isfile) {
                    int value = _file_chooser.showOpenDialog(null);
                    if (value == JFileChooser.APPROVE_OPTION) {
                        File file = _file_chooser.getSelectedFile();
                        try {
                            ((JTextField)_field[index]).setText(file.getAbsolutePath());
                        } catch (Exception ee) { ee.printStackTrace(); }
                    }
                }
            }
            requestFocus();
        }
    }

    public static String[] makeCombo(String[] array, int index) {
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(array));
        list.add(array[index]);
        return list.toArray(new String[]{});
    }

    public static int get(String[] array, String selectedValue) {
        return Arrays.asList(array).indexOf(selectedValue);
    }

    public static void main(String[] args) {
        ParameterDialog pd = new ParameterDialog( new String[]{"Test"},
                new String[]{"List", "Box 1", "Box 2"},
                new Object[]{new String[]{"[LIST]", "Hugo", "Emil", "Egon", "Hein", "Hugo|Egon"}, new Boolean(true), new Boolean(true)},
                null);
        Object[] arr = pd.getParameters();
        for (int i=0; i<arr.length; i++)
            System.out.println(arr[i]);
    }
}