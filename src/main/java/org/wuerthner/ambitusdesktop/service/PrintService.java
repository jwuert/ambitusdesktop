package org.wuerthner.ambitusdesktop.service;

import org.wuerthner.ambitus.model.Arrangement;
import org.wuerthner.ambitus.model.MidiTrack;
import org.wuerthner.ambitus.type.NamedRange;
import org.wuerthner.ambitusdesktop.ScoreModel;
import org.wuerthner.cwn.api.CwnTrack;
import org.wuerthner.cwn.api.DurationType;
import org.wuerthner.cwn.api.ScoreParameter;
import org.wuerthner.cwn.score.ScorePrinter;
import org.wuerthner.sport.api.ModelElement;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PrintService {
    private static String WIN_PROGRAMFILES = System.getenv("programfiles");

    public void print(Arrangement arrangement, ScoreModel scoreModel) {
        try {
            Properties ambitusConfig = new Properties();
            File userDir = new File(System.getProperties().getProperty("user.dir"));
            if (userDir.exists() && userDir.isDirectory()) {
                File ambitusConfigPath = new File(userDir, "ambitus.conf");
                if (ambitusConfigPath.exists()) {
                    ambitusConfig.load(new FileInputStream(ambitusConfigPath));
                }
            }
            System.out.println("userDir: " + userDir);
            System.out.println("config: " + ambitusConfig);

            String fileBase = Long.toString(System.nanoTime());
            File lilypondFile = createOutputFile(fileBase, "ly");
            String pdfFilePrefix = new File(lilypondFile.getName().substring(0, lilypondFile.getName().length() - 3)).toString();
            writeLilypond(lilypondFile, arrangement, scoreModel);
            BufferedReader br = null;
            if (isLinux()) {
                String lilypond = ambitusConfig.getProperty("lilypond", "lilypond");
                br = exec(lilypond + " " + lilypondFile.getCanonicalPath(), "", lilypondFile.getParentFile());
            } else if (isWindows()) {
                String lilypond = ambitusConfig.getProperty("lilypond",
                        "\"" + WIN_PROGRAMFILES + "\\lilypond\\bin\\lilypond.exe\"");
                System.out.println("program files: " + WIN_PROGRAMFILES);
                System.out.println("lilypond: " + lilypond);
                br = exec(lilypond + " -dgui " + lilypondFile.getCanonicalPath(), "", lilypondFile.getParentFile());
            }
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("lilypond: " + line);
            }
            if (isLinux()) {
                String pdfreader = ambitusConfig.getProperty("pdfreader", "evince");
                Runtime.getRuntime().exec(pdfreader + " " + new File(lilypondFile.getParentFile(), pdfFilePrefix + ".pdf").getCanonicalPath(), null, lilypondFile.getParentFile());
            } else if (isWindows()) {
                String pdfreader = ambitusConfig.getProperty("pdfreader",
                        "\"" + WIN_PROGRAMFILES + "\\Adobe\\Acrobat DC\\Acrobat\\Acrobat.exe\"");
                System.out.println("pdfreader:  " + pdfreader);
                Runtime.getRuntime().exec(pdfreader + " " + new File(lilypondFile.getParentFile(), pdfFilePrefix + ".pdf").getCanonicalPath(), null,
                        lilypondFile.getParentFile());

                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + new File(lilypondFile.getParentFile(), pdfFilePrefix + ".pdf").getCanonicalPath(), null, lilypondFile.getParentFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createOutputFile(String name, String ext) throws IOException {
        File file = File.createTempFile("temp", name + "." + ext);
        return file;
    }

    private void writeLilypond(File outputFile, Arrangement arrangement, ScoreModel scoreModel) {
        try {
            String title = arrangement.getAttributeValue(Arrangement.name);
            String subtitle = arrangement.getAttributeValue(Arrangement.subtitle);
            String composer = arrangement.getAttributeValue(Arrangement.composer);
            boolean autoBeam = arrangement.getAttributeValue(Arrangement.autoBeamPrint);
            Iterable<CwnTrack> children = arrangement.getTrackList();
            CwnTrack[] tracks = StreamSupport.stream(children.spliterator(), false).filter(t -> t instanceof MidiTrack).toArray(s -> new CwnTrack[s]);
            List<CwnTrack> trackList = Arrays.asList(tracks);
            int ppq = arrangement.getAttributeValue(Arrangement.pulsePerQuarter);
            int resolutionInTicks = arrangement.getResolutionInTicks();
            int groupLevel = arrangement.getAttributeValue(Arrangement.groupLevel);
            int stretchFactor = arrangement.getStretchFactor();
            int flags = arrangement.getFlags();
            List<DurationType> typeList = scoreModel.getScoreParameter().durationTypeList;
            long endPosition = arrangement.findLastPositionBarStart();
            Map<Long,String> rangeMap = arrangement.getRangeList().stream().collect(Collectors.toMap(nr -> nr.start, nr -> nr.name));

            // ScoreParameter scoreParameter = new ScoreParameter(startDisplayPosition, endDisplayPosition, ppq, resolutionInTicks, groupLevel, stretchFactor, Score.ALLOW_DOTTED_RESTS | Score.SPLIT_RESTS);
            ScoreParameter scoreParameter = new ScoreParameter(ppq, resolutionInTicks, groupLevel, stretchFactor, flags,
                    typeList, new ArrayList<>(), 0, arrangement.getCaret(), rangeMap);

            ScorePrinter printer = new ScorePrinter();
            String lyString = printer.print(title, subtitle, composer, autoBeam, scoreParameter, trackList, endPosition);
            Files.write(outputFile.toPath(), lyString.getBytes());

            // JsonObjectBuilder jsonPrint = Json.createObjectBuilder();
            // context.sendMessage(jsonPrint.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    private static boolean isLinux() {
        return System.getProperty("os.name").startsWith("Linux");
    }

    /**
     * This method executes a system command.
     **/
    private BufferedReader exec(String command, String input, File dir) {
        BufferedReader reader = null;
        try {
            Process p = Runtime.getRuntime().exec(command, null, dir);
            new OutputThread(p, input).start();
            BufferedInputStream buffer = new BufferedInputStream(p.getInputStream());
            reader = new BufferedReader(new InputStreamReader(buffer));
        } catch (IOException e) {
            System.err.println(e);
        }
        return reader;
    }

    private class OutputThread extends Thread {
        private final Process _process;
        private final String _input;

        public OutputThread(Process process, String input) {
            _process = process;
            _input = input;
        }

        public void run() {
            try {
                BufferedOutputStream bufferout = new BufferedOutputStream(_process.getOutputStream());
                PrintWriter commandInput = new PrintWriter((new OutputStreamWriter(bufferout)), true);
                commandInput.println(_input);
                commandInput.close();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

}
