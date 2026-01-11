package org.wuerthner.ambitusdesktop.service;

import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.model.MidiTrack;
import org.wuerthner.ambitus.tool.SelectionTools;
import org.wuerthner.ambitus.type.NamedRange;
import org.wuerthner.ambitusdesktop.ScoreModel;
import org.wuerthner.cwn.api.CwnTrack;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExportService {
    private final SelectionTools selectionTools = new SelectionTools();
    private final SequenceService sequenceService = new SequenceService();

    public void export(Arrangement arrangement, ScoreModel scoreModel, int exposeValue, String tempi) {
        // parameters
        List<Integer> tempoList = Arrays.stream(tempi.split(",")).map(t -> Integer.valueOf(t.trim())).collect(Collectors.toList());
        for (int tempo : tempoList) {
            // ranges
            List<NamedRange> rangeList = arrangement.getRangeList();
            rangeList.add(new NamedRange("Total", arrangement.findLastPosition()));
            for (int rangeIndex = 0; rangeIndex<rangeList.size(); rangeIndex++) {
                NamedRange range = rangeList.get(rangeIndex);
                Optional<NamedRange> nextRange = rangeIndex+1<rangeList.size() ? Optional.of(rangeList.get(rangeIndex+1)) : Optional.empty();
                String rangeName;
                long endPosition = 0;
                rangeName = range.name + " - ";
                arrangement.setTransientBarOffsetPosition(range.start);
                if (nextRange.isPresent()) {
                    endPosition = nextRange.get().start;
                }

                String composer = arrangement.getAttributeValue(Arrangement.composer);
                String name = arrangement.getAttributeValue(Arrangement.name);

                try {

                    if (exposeValue == 0) {
                        String fileBase = composer + " - " + name + " - " + rangeName + " - " + tempo;
                        File midiFile = createOutputFile(composer, name, fileBase + ".mid");
                        writeMidi(arrangement, endPosition, midiFile, -1, 0, tempo);
                        if (midiFile.exists()) {
                            // mp3
                            File mp3File = createOutputFile(composer, name, fileBase + ".mp3");
                            writeMP3(arrangement, midiFile, mp3File);
                            mp3File.renameTo(new File(mp3File.getAbsolutePath().replaceAll("_", " ")));
                        } else {
                            System.err.println("Failure in creation of midi file: " + midiFile);
                        }
                    } else {
                        List<CwnTrack> trackList = arrangement.getTrackList();
                        for (int exposedTrack = 0; exposedTrack < trackList.size(); exposedTrack++) {
                            int exposeStrength = scoreModel.getPlayStrength();
                            CwnTrack track = trackList.get(exposedTrack);
                            if (!track.getMute()) {
                                String fileBase = composer + " - " + name + " - " + rangeName + track.getName() + " - " + tempo;
                                File midiFile = createOutputFile(composer, name, track.getName(), fileBase + ".mid");
                                writeMidi(arrangement, endPosition, midiFile, exposedTrack, exposeStrength, tempo);
                                if (midiFile.exists()) {
                                    // mp3
                                    File mp3File = createOutputFile(composer, name, track.getName(), fileBase + ".mp3");
                                    writeMP3(arrangement, midiFile, mp3File);
                                    mp3File.renameTo(new File(mp3File.getAbsolutePath().replaceAll("_", " ")));
                                } else {
                                    System.err.println("Failure in creation of midi file: " + midiFile);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeMidi(Arrangement arrangement, long endPosition, File outputFile, int exposedTrack, int exposeStrength, int tempo) {
        try {
            Sequence sequence = sequenceService.createSequence(arrangement, endPosition, null, exposedTrack, exposeStrength, tempo);
            MidiSystem.write(sequence, 1, outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeMP3(Arrangement arrangement, File midiFile, File mp3File) {
        String result = executeCommand("/home/wuerthne/Scripts/midi2mp3 " + midiFile.getAbsolutePath() + " " + mp3File.getAbsolutePath());
    }

    private File createOutputFile(String composer, String opus, String trackName, String fileName) throws IOException {
        String tempDirectory = System.getProperty("java.io.tmpdir");
        File ambitusDir = new File(tempDirectory, "ambitus");
        if (trackName == null) {
            trackName = "all";
        }
        trackName = trackName==null ? "Track" : trackName;
        composer = composer==null ? "Comp" : composer;
        opus = opus==null ? "Opus" : opus;
        File trackDir = new File(ambitusDir, trackName.replaceAll(" ", "_"));
        File composerDir = new File(trackDir, composer.replaceAll(" ", "_"));
        File opusDir = new File(composerDir, opus.replaceAll(" ", "_"));
        opusDir.mkdirs();
        File file = new File(opusDir, fileName.replaceAll(" ", "_"));
        return file;
    }

    private File createOutputFile(String composer, String opus, String fileName) throws IOException {
        return createOutputFile(composer, opus, null, fileName);
    }

    private String executeCommand(String command) {
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}
