package org.wuerthner.ambitusdesktop;

import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.type.NamedRange;
import org.wuerthner.ambitusdesktop.ui.*;
import org.wuerthner.cwn.api.Trias;
import org.wuerthner.cwn.position.PositionTools;
import org.wuerthner.cwn.score.ScoreUpdate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class Ambitus implements PanelUpdater, ToolbarUpdater, ScoreUpdater {
    static int WIDTH = 1600; //1600;
    static int HEIGHT = 1024; //1024;

    private final ScorePanel content;
    private final JPanel rangePanel;
    private final ScoreModel scoreModel = new ScoreModel(WIDTH);
    private final PlayerToolBar playerToolBar;

    private FunctionToolBar functionToolBar;
    private NoteToolBar noteToolBar;
    private SymbolToolBar symbolToolBar;


    public Ambitus() {
        String version = getClass().getPackage().getImplementationVersion();
        if (version==null) version = "IDE";
        final JFrame frame = new JFrame("Ambitus " + version);
        updatePanel();
        content = new ScorePanel(scoreModel, this, this);
        JComponent toolbar = makeFunctionToolBar();

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
        content.addMouseWheelListener(content);


        playerToolBar = new PlayerToolBar(scoreModel, this, this, content);
        new ScoreKeyListener(content, scoreModel, this, this, playerToolBar);
        updateToolbar();
    }

    private JComponent makeFunctionToolBar() {
        //
        // 1st toolbar
        //
        functionToolBar = new FunctionToolBar(scoreModel, this, this, this, content, WIDTH);

        noteToolBar = new NoteToolBar(scoreModel, this, this, content);
        JToolBar toolbar2 = noteToolBar.getToolBar();

        symbolToolBar = new SymbolToolBar(scoreModel, this, this);
        symbolToolBar.setVisible(false);

        // ToolPanel
        JPanel toolPanel = new JPanel();
        toolPanel.setLayout(new BorderLayout());
        toolPanel.add(functionToolBar.getFunctionToolbar(), BorderLayout.PAGE_START);
        toolPanel.add(toolbar2, BorderLayout.CENTER);
        toolPanel.add(symbolToolBar.getToolBar(), BorderLayout.PAGE_END);
        return toolPanel;
    }


    @Override
    public void updateToolbar() {
        if (functionToolBar!=null) {
            functionToolBar.updateToolbar();
        }
        if (playerToolBar!=null) {
            playerToolBar.updateToolbar();
        }
    }

    public void updateNoteBar() {
        if (scoreModel.getNoteSelector().isNote()) {
            noteToolBar.setSelection(scoreModel.getSelection());
        } else {
            noteToolBar.clearSelection();
        }
        noteToolBar.updateFlags();
    }

    public void updateSymbolBar() {
        if (scoreModel.getNoteSelector().isSymbol()) {
            symbolToolBar.setSelection(scoreModel.getSelection());
        } else {
            symbolToolBar.clearSelection();
        }
    }

    public void updateNoteOrAccent() {
        symbolToolBar.setVisible(scoreModel.accents());
        noteToolBar.updateSelector(scoreModel.getNoteSelector());
        symbolToolBar.updateSelector(scoreModel.getNoteSelector());
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
//                            int max = scoreModel.getArrangement().getNumberOfActiveMidiTracks() - 1;
//                            scoreModel.select(
//                                    new Location(null, range.start, 0, 0, 0, false, 0, 0, 0),
//                                    new Location(null, range.end, 0, max, 0, false, 0, 0, 0), true);
                            Trias trias = PositionTools.getTrias(scoreModel.getTrackList().get(0), range.start);
                            int offset = (trias.bar - 1 < 0 ? trias.bar : trias.bar);
                            scoreModel.getArrangement().setTransientBarOffset(offset);
                            scoreModel.getScoreParameter().setBarOffset(offset);
                            content.updateScore(new ScoreUpdate(ScoreUpdate.Type.RELAYOUT));
                            updateToolbar();
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

    @Override
    public void update(ScoreUpdate update) {
        content.updateScore(update);
    }

    public static void main(String[] args) {
        new Ambitus();
    }
}
