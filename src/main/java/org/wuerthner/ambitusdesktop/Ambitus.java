package org.wuerthner.ambitusdesktop;

import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.type.NamedRange;
import org.wuerthner.ambitusdesktop.ui.*;
import org.wuerthner.cwn.api.Trias;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.Location;
import org.wuerthner.cwn.score.ScoreUpdate;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class Ambitus implements PanelUpdater, ToolbarUpdater, ScoreUpdater {
    static int WIDTH = 1600; //1600;
    static int HEIGHT = 1024; //1024;

    private final ScorePanel content;
    private final JPanel rangePanel;
    private final ScoreModel scoreModel = new ScoreModel(WIDTH);

    private NoteToolBar noteToolBar;
    private FunctionToolBar functionToolBar;

    public Ambitus() {
        final JFrame frame = new JFrame("Ambitus");
        updatePanel();
        content = makeContent();
        JComponent toolbar = makeToolBar();

        rangePanel = new JPanel();
        rangePanel.setPreferredSize(new Dimension(200, HEIGHT));
        rangePanel.setVisible(false);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().add(toolbar, BorderLayout.PAGE_START);
        frame.getContentPane().add(content, BorderLayout.CENTER);
        frame.getContentPane().add(rangePanel, BorderLayout.EAST);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(WIDTH, HEIGHT);
        frame.setVisible(true);
        content.addMouseMotionListener(content);
        content.addMouseListener(content);
        new ScoreKeyListener(content, scoreModel, this);
        updateToolbar();
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
