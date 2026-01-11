package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.model.InfoTrack;
import org.wuerthner.ambitus.model.MidiTrack;
import org.wuerthner.ambitusdesktop.ScoreModel;
import org.wuerthner.ambitusdesktop.ScoreUpdater;
import org.wuerthner.ambitusdesktop.ToolbarUpdater;
import org.wuerthner.cwn.api.CwnTrack;
import org.wuerthner.cwn.score.ScoreUpdate;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

public class TrackListDialog extends JDialog {
    static final private String ACTION_OK = "ok";
    static final private String ACTION_NAME = "name";
    static final private String ACTION_VISIBLE = "visible";
    static final private String ACTION_MUTE = "mute";
    static final private String ACTION_CHANNEL = "channel";
    static final Font PLAIN_FONT   = new Font("Arial", Font.PLAIN, 11);
    static final private JLabel head = new JLabel("Tracklist", SwingConstants.CENTER);
    static final Border emptyBorder   = BorderFactory.createEmptyBorder(2,12,2,12);
    static final Border largeEmptyBorder   = BorderFactory.createEmptyBorder(6,12,6,12);
    static final Border etchlBorder   = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),BorderFactory.createEmptyBorder(4,4,4,4));
    static final Border etchlBorder2  = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),BorderFactory.createEmptyBorder(0,0,0,0));
    static final Border loweredBorder = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),BorderFactory.createEmptyBorder(0,4,0,4));
    private final TrackListDialog.MainListener _ml  = new TrackListDialog.MainListener();
    private final FocusListener _fl = new TrackListDialog.MainFocus();
    private final ScoreModel scoreModel;
    private final ScoreUpdater scoreUpdater;
    private final ToolbarUpdater toolbarUpdater;

    public TrackListDialog(ScoreModel scoreModel, ScoreUpdater scoreUpdater, ToolbarUpdater toolbarUpdater, Component parent) {
       // this.parent = parent;
        this.scoreModel = scoreModel;
        this.scoreUpdater = scoreUpdater;
        this.toolbarUpdater = toolbarUpdater;
        // setModal(true);

        List<CwnTrack> trackList = scoreModel.getArrangement().getTrackList();
        int size = trackList.size();

        // ok
        final JButton ok = new JButton("OK");
        ok.setActionCommand(ACTION_OK);
        ok.addActionListener( _ml );
        getRootPane().setDefaultButton(ok);

        // build panel
        JPanel switches = new JPanel();
        switches.setBorder(emptyBorder);
        switches.setLayout(new BoxLayout(switches, BoxLayout.Y_AXIS));
        JPanel p;
        p = new JPanel(new GridLayout(1,7,5,5));
        p.add(new JLabel("No."));
        p.add(new JLabel("Name"));
        p.add(new JLabel("Type"));
        p.add(new JLabel("Visible"));
        p.add(new JLabel("Mute"));
        p.add(new JLabel("Channel"));
        p.add(new JLabel("Volume"));
        p.add(new JLabel("Instrument"));
        p.setBorder(etchlBorder);
        switches.add(p);
        int count = 0;
        for (CwnTrack track: trackList) {
            // name
            JTextField nameField = new JTextField();
            nameField.setActionCommand(ACTION_NAME+count);
            nameField.setBorder(etchlBorder);
            nameField.setText(track.getName());
            nameField.setBackground(Color.lightGray);
            nameField.addActionListener( _ml );
            nameField.addFocusListener( _fl );

            // visible
            JCheckBox visibleBox = new JCheckBox();
            visibleBox.setActionCommand(ACTION_VISIBLE+count);
            visibleBox.setSelected(track.getVisible());
            visibleBox.addActionListener( _ml );

            // mute
            JCheckBox muteBox = new JCheckBox();
            muteBox.setActionCommand(ACTION_MUTE+count);
            muteBox.setSelected(track.getMute());
            muteBox.addActionListener( _ml );

            // channel
            JComboBox<String> channelBox = new JComboBox<>(CwnTrack.CHANNELS);
            channelBox.setActionCommand(ACTION_CHANNEL+count);
            String channel = (track.getChannel()+1>9?"":"0")+(track.getChannel()+1);
            channelBox.setSelectedItem(channel);
            channelBox.addActionListener( _ml );

            // UI
            p = new JPanel(new GridLayout(1,5,5,5));
            p.add(new JLabel(""+ ++count));
            p.add(nameField);
            p.add(new JLabel(track.isInfoTrack() ? "Info": "MIDI"));
            p.add(visibleBox);
            p.add(muteBox);
            p.add(channelBox);
            p.add(new JLabel(""+track.getVolume()));
            p.add(new JLabel(""+track.getInstrument()));
            p.setBorder(emptyBorder);
            switches.add(p);
        }


        p = new JPanel(new GridLayout(1, 2, 5, 5));
        p.add(ok);
        p.setBorder(largeEmptyBorder);
        switches.add(p);

        //
        // build GUI
        //
        head.setLayout(new GridLayout(0,1, 2, 2));
        head.setBorder(largeEmptyBorder);

        BorderLayout bl = new BorderLayout(1, 1);
        getContentPane().setLayout(bl);

        getContentPane().add(head, BorderLayout.NORTH);
        getContentPane().add(switches, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
        requestFocus();
    }

    private class MainListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();

            String val = null;
            if (cmd.equals(ACTION_OK)) {
                dispose();
            } else if (cmd.startsWith(ACTION_NAME)) {
                int index = Integer.valueOf(cmd.substring(ACTION_NAME.length()));
                CwnTrack track = scoreModel.getTrackList().get(index);
                String value = ((JTextField) e.getSource()).getText();
                if (!track.isInfoTrack()) {
                    scoreModel.getArrangement().setTrackName((MidiTrack) track, value);
                } else {
                    scoreModel.getArrangement().setTrackName((InfoTrack) track, value);
                }
                requestFocus();
            } else if (cmd.startsWith(ACTION_VISIBLE)) {
                int index = Integer.parseInt(cmd.substring(ACTION_VISIBLE.length()));
                CwnTrack track = scoreModel.getTrackList().get(index);
                if (!track.isInfoTrack()) {
                    boolean value = ((JCheckBox)e.getSource()).isSelected();
                    ((MidiTrack)track).setAttributeValue(MidiTrack.visible, value);
                }
            } else if (cmd.startsWith(ACTION_MUTE)) {
                int index = Integer.parseInt(cmd.substring(ACTION_MUTE.length()));
                CwnTrack track = scoreModel.getTrackList().get(index);
                if (!track.isInfoTrack()) {
                    boolean value = ((JCheckBox)e.getSource()).isSelected();
                    ((MidiTrack)track).setAttributeValue(MidiTrack.mute, value);
                }
            } else if (cmd.startsWith(ACTION_CHANNEL)) {
                int index = Integer.parseInt(cmd.substring(ACTION_CHANNEL.length()));
                CwnTrack track = scoreModel.getTrackList().get(index);
                int value = Integer.parseInt(""+((JComboBox<?>) e.getSource()).getSelectedItem())-1;
                if (!track.isInfoTrack()) {
                    scoreModel.getArrangement().setTrackChannel((MidiTrack)track, value);
                }
            }
            scoreUpdater.update(new ScoreUpdate(ScoreUpdate.Type.REBUILD));
            toolbarUpdater.updateToolbar();
        }
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
}
