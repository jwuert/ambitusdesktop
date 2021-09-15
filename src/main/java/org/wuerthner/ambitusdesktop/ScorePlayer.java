package org.wuerthner.ambitusdesktop;

import org.wuerthner.ambitusdesktop.service.MidiService;

import javax.swing.*;

public class ScorePlayer implements Runnable {
    final private JPanel panel;
    final private ToolbarUpdater updater;

    public ScorePlayer(JPanel panel, ToolbarUpdater updater) {
        this.panel = panel;
        this.updater = updater;
    }

    @Override
    public void run() {
        boolean loop = true;
        try {
            Thread.sleep(20);
            while (loop) {
                Thread.sleep(50);
                panel.updateUI();

                if (!MidiService.isRunning()) {
                    loop = false;
                    updater.updateToolbar();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}