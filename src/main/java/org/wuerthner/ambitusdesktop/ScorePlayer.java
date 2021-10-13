package org.wuerthner.ambitusdesktop;

import org.wuerthner.ambitusdesktop.service.MidiService;

import javax.swing.*;

public class ScorePlayer implements Runnable {
    final private JPanel panel;
    final private ToolbarUpdater updater;
    final private PositionUpdater positionUpdater;

    public ScorePlayer(JPanel panel, ToolbarUpdater updater, PositionUpdater positionUpdater) {
        this.panel = panel;
        this.updater = updater;
        this.positionUpdater = positionUpdater;
    }

    @Override
    public void run() {
        boolean loop = true;
        try {
            Thread.sleep(20);
            while (loop) {
                Thread.sleep(50);
                panel.updateUI();
                positionUpdater.updatePosition();
                if (!MidiService.isRunning()) {
                    loop = false;
                    updater.updateToolbar();
                    positionUpdater.updatePosition();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}