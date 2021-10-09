package org.wuerthner.ambitusdesktop;

import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.model.MidiTrack;
import org.wuerthner.ambitus.model.NoteEvent;
import org.wuerthner.ambitus.service.Quantize;
import org.wuerthner.ambitus.type.NamedRange;
import org.wuerthner.ambitusdesktop.service.MidiService;
import org.wuerthner.ambitusdesktop.ui.*;
import org.wuerthner.cwn.api.CwnBarEvent;
import org.wuerthner.cwn.api.CwnSelection;
import org.wuerthner.cwn.api.CwnTrack;
import org.wuerthner.cwn.api.Trias;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.Location;
import org.wuerthner.cwn.score.ScoreUpdate;
import org.wuerthner.sport.api.ModelElement;
import org.wuerthner.sport.api.Modifier;
import org.wuerthner.sport.core.XMLElementWriter;
import org.wuerthner.sport.core.XMLReader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Ambitus implements PanelUpdater, ToolbarUpdater, ScoreUpdater {
    static int WIDTH = 1600; //1600;
    static int HEIGHT = 600; //1024;

    private final ScorePanel content;
    private final JScrollPane trackPanel;
    private final JPanel navigation;
    private final JPanel rangePanel;
    private final ScoreModel scoreModel = new ScoreModel(WIDTH);

    private NoteToolBar noteToolBar;
    private FunctionToolBar functionToolBar;

    public Ambitus() {
        final JFrame frame = new JFrame("Ambitus");


        navigation = new JPanel();
        BoxLayout layout = new BoxLayout(navigation, BoxLayout.Y_AXIS);
        navigation.setLayout(layout);
        navigation.setAlignmentX(Component.LEFT_ALIGNMENT);

        updatePanel();
        trackPanel = new JScrollPane(navigation, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        content = makeContent();
        JComponent toolbar = makeToolBar();

        rangePanel = new JPanel();
        rangePanel.setPreferredSize(new Dimension(200, HEIGHT));


        frame.setLayout(new BorderLayout());
        frame.getContentPane().add(toolbar, BorderLayout.PAGE_START);
        JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitpane.setLeftComponent(trackPanel);
        splitpane.setRightComponent(content);
        splitpane.setDividerLocation(160);
        frame.getContentPane().add(splitpane, BorderLayout.CENTER);
        frame.getContentPane().add(rangePanel, BorderLayout.EAST);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(WIDTH, HEIGHT);
        frame.setVisible(true);
        content.addMouseMotionListener(content);
        content.addMouseListener(content);
        new ScoreKeyListener(content, scoreModel, this);
    }

    private JComponent makeToolBar() {
        //
        // 1st toolbar
        //
        functionToolBar = new FunctionToolBar(scoreModel, this, this, this, content, WIDTH);

        noteToolBar = new NoteToolBar(scoreModel, this);
        JToolBar toolbar2 = noteToolBar.getToolBar();

        // ToolPanel
        JPanel toolPanel = new JPanel();
        toolPanel.setLayout(new BorderLayout());
        toolPanel.add(functionToolBar.getFunctionToolbar(), BorderLayout.PAGE_START);
        toolPanel.add(toolbar2, BorderLayout.PAGE_END);
        return toolPanel;
    }


    @Override
    public void updateToolbar() {
        if (functionToolBar!=null) {
            functionToolBar.updateToolbar();
        }
    }

    public void updateNoteBar() {
        noteToolBar.setSelection(scoreModel.getSelection());
    }

    public void updatePanel() {
        navigation.removeAll();
        JLabel label = new JLabel("Track Mixer");
        label.setBorder(BorderFactory.createEmptyBorder(8, 20, 2, 2));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigation.add(label);
        for (MidiTrack track : scoreModel.getArrangement().getActiveMidiTrackList()) {
            JPanel track1 = TrackWidget.createTrack(track, content);
            track1.setAlignmentX(Component.LEFT_ALIGNMENT);
            navigation.add(track1);
        }
        JLabel space = new JLabel();
        space.setPreferredSize(new Dimension(20, HEIGHT));
        space.setAlignmentX(Component.LEFT_ALIGNMENT);
        navigation.add(space);
        //
        // RANGE BUTTONS
        //
        List<NamedRange> rangeList = scoreModel.getArrangement().getAttributeValue(Arrangement.rangeList);
        if (rangePanel!=null) {
            rangePanel.removeAll();
            if (rangeList.isEmpty()) {
                rangePanel.setVisible(false);
            } else {
                rangePanel.setVisible(true);
                for (NamedRange range : rangeList) {
                    JButton rangeBtn = new JButton(range.name);
                    rangeBtn.setPreferredSize(new Dimension(180, 20));
                    rangeBtn.setFocusPainted(false);
                    AbstractAction rangeAction = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int max = scoreModel.getArrangement().getNumberOfActiveMidiTracks() - 1;
                            scoreModel.select(
                                    new Location(null, range.start, 0, 0, 0, false, 0, 0, 0),
                                    new Location(null, range.end, 0, max, 0, false, 0, 0, 0));
                            Trias trias = PositionTools.getTrias(scoreModel.getTrackList().get(0), range.start);
                            int offset = (trias.bar - 1 < 0 ? trias.bar : trias.bar - 1);
                            scoreModel.getArrangement().setTransientBarOffset(offset);
                            scoreModel.getScoreParameter().setBarOffset(offset);
                            content.updateScore(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                        }
                    };
                    rangeBtn.addActionListener(rangeAction);
                    rangePanel.add(rangeBtn);
                }
            }
            rangePanel.updateUI();
        }
        // toolbar
        updateToolbar();
    }

    private ScorePanel makeContent() {
        ScorePanel scorePanel = new ScorePanel(scoreModel, this, this);
        return scorePanel;
    }

    public static void main(String[] args) {
        Ambitus ambitus = new Ambitus();
    }

    @Override
    public void update(ScoreUpdate update) {
        content.updateScore(update);
    }
}
