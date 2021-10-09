package org.wuerthner.ambitusdesktop.ui;

import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.model.MidiTrack;
import org.wuerthner.ambitusdesktop.ScorePanel;
import org.wuerthner.cwn.api.TimeSignature;
import org.wuerthner.cwn.score.ScoreUpdate;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URL;

public class TrackWidget {
    static final Border etchlBorder   = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),BorderFactory.createEmptyBorder(4,4,4,4));
    static final Border etchlBorderS  = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),BorderFactory.createEmptyBorder(3,0,4,0));
    static final Border etchlBorder2  = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),BorderFactory.createEmptyBorder(0,0,0,0));
    static final Border loweredBorder = BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),BorderFactory.createEmptyBorder(0,4,0,4));
    static final Border emptyBorder   = BorderFactory.createEmptyBorder(2,2,2,2);
    static final int CTRL_HEIGHT = 30;
    static final String[] CHANNELS = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16"};

    public static JPanel createTrack(MidiTrack track, ScorePanel content) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.gray);
        FocusListener focusListener = new MainFocus();
        Arrangement arrangement = (Arrangement)track.getParent();

        // Name
        JTextField nameField = new JTextField(track.getName(), 8);
        nameField.setPreferredSize(new Dimension(120,CTRL_HEIGHT));
        panel.add(nameField);
        nameField.addFocusListener(focusListener);
        setFocusOut(nameField);
        nameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                arrangement.setTrackName(track, nameField.getText());
                content.updateUI();
                panel.requestFocus();
            }
        });

        // Channel
//        SpinnerModel spinnerModel = new SpinnerNumberModel(track.getChannel(), 0, 15, 1); // value, min, max, step
//        JSpinner channelSpinner = new JSpinner(spinnerModel);
//        setFocusOut(channelSpinner);
//        channelSpinner.setPreferredSize(new Dimension(40, CTRL_HEIGHT));
//        panel.add(channelSpinner);
        JComboBox channelBox = new JComboBox(CHANNELS);
        channelBox.setPreferredSize(new Dimension(60, CTRL_HEIGHT));
        channelBox.setSelectedIndex(track.getChannel());
        channelBox.addFocusListener(focusListener);
        setFocusOut(channelBox);
        panel.add(channelBox);
        channelBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Arrangement)track.getParent()).setTrackChannel(track, channelBox.getSelectedIndex());
                content.updateUI();
                panel.requestFocus();
            }
        });

//        // Clef
//        int clef = track.getClef();
//        JComboBox clefBox = new JComboBox(MidiTrack.CLEFS);
//        clefBox.setSelectedIndex(clef);
//        clefBox.addFocusListener(focusListener);
//        setFocusOut(clefBox);
//        clefBox.setPreferredSize(new Dimension(100, CTRL_HEIGHT));
//        panel.add(clefBox);
//        clefBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                arrangement.setTrackClef(track, clefBox.getSelectedIndex());
//                content.updateScore(new ScoreUpdate(track));
//                panel.requestFocus();
//            }
//        });
//
//        // Key
//        int key = track.getKey();
//        JComboBox keyBox = new JComboBox(MidiTrack.KEYS);
//        keyBox.setSelectedIndex(key);
//        keyBox.addFocusListener(focusListener);
//        setFocusOut(keyBox);
//        keyBox.setPreferredSize(new Dimension(60, CTRL_HEIGHT));
//        panel.add(keyBox);
//        keyBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                arrangement.setTrackKey(track, keyBox.getSelectedIndex());
//                content.updateScore(new ScoreUpdate(track));
//                panel.requestFocus();
//            }
//        });
//
//        // Time Signature
//        TimeSignature timeSignature = track.getTimeSignature();
//        JTextField tsField = new JTextField(timeSignature.toString(), 12);
//        tsField.addFocusListener(focusListener);
//        setFocusOut(tsField);
//        tsField.setPreferredSize(new Dimension(100, CTRL_HEIGHT));
//        panel.add(tsField);
//        tsField.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                arrangement.setTrackMetric(track, tsField.getText());
//                content.updateScore(new ScoreUpdate(track));
//                panel.requestFocus();
//            }
//        });

        // Instrument
        int instrument = track.getInstrument();
        JComboBox instrumentBox = new JComboBox(MidiTrack.MIDI_INSTRUMENTS);
        instrumentBox.setSelectedIndex(instrument);
        instrumentBox.addFocusListener(focusListener);
        setFocusOut(instrumentBox);
        instrumentBox.setPreferredSize(new Dimension(200, CTRL_HEIGHT));
        panel.add(instrumentBox);
        instrumentBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Arrangement)track.getParent()).setTrackInstrument(track, instrumentBox.getSelectedIndex());
                content.updateUI();
                panel.requestFocus();
            }
        });

        // Mute
        boolean mute = track.getMute();
        JCheckBox muteBox = new JCheckBox("mute");
        muteBox.setSelected(mute);
        muteBox.addFocusListener(focusListener);
        setFocusOut(muteBox);
        muteBox.setPreferredSize(new Dimension(120, CTRL_HEIGHT));
        panel.add(muteBox);
        muteBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Arrangement)track.getParent()).setTrackMute(track, muteBox.isSelected());
                content.updateUI();
                panel.requestFocus();
            }
        });

        // Delete
        URL url = content.getClass().getResource("/toolbar/minus.png");
        ImageIcon icon = new ImageIcon(url);
        icon = new ImageIcon(icon.getImage().getScaledInstance(CTRL_HEIGHT, CTRL_HEIGHT, Image.SCALE_SMOOTH));
        JButton deleteBtn = new JButton(icon);
        deleteBtn.setBackground(Color.gray);
        deleteBtn.setPreferredSize(new Dimension(CTRL_HEIGHT, CTRL_HEIGHT));
        panel.add(deleteBtn);

        // panel.setMaximumSize(new Dimension(400,30));
        // panel.setMaximumSize(new Dimension(panel.getMinimumSize().width, 30));
        // panel.doLayout();
        return panel;
    }

    private static class MainFocus implements FocusListener {
        public void focusGained(FocusEvent e) {
            JComponent f = (JComponent) e.getSource();
            setFocusIn(f);
        }

        public void focusLost(FocusEvent e) {
            JComponent f = (JComponent) e.getSource();
            setFocusOut(f);
        }
    }

    private static void setFocusIn(JComponent component) {
        component.setBackground(Color.black);
        component.setForeground(Color.white);
        component.setBorder(loweredBorder);
    }

    private static void setFocusOut(JComponent component) {
        component.setBackground(Color.lightGray);
        component.setForeground(Color.black);
        if (component instanceof JTextField)
            component.setBorder(etchlBorder);
        else if (component instanceof JComboBox)
            component.setBorder(emptyBorder);
        else
            component.setBorder(etchlBorder2);
    }
}
