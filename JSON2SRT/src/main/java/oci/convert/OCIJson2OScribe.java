package oci.convert;

import org.apache.commons.cli.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OCIJson2OScribe {

    private final static boolean VERBOSE = false;
    private final static String PUNCTUATION = "PUNCTUATION";

    // Convert time from millisecond to SRT format (legacy)
    static String time_int2str(int value) {
        int ms = value % 1_000;
        int s = (int) Math.floor((value - ms) / 1_000.0) % 60;
        int m = (int) Math.floor(((value - ms) - 1_000.0 * s) / 60_000.0) % 60;
        int h = (int) Math.floor(((value - ms) - 1_000.0 * s - 60_000.0 * m) / 3_600_000.0);

        return String.format("%02d", h) + ":" + String.format("%02d", m) + ":" + String.format("%02d", s) + "," + String.format("%03d", ms);
    }

    /**
     * helper class to represent a word token from json file
     * possibly accompanied by a preceding or following punctuation sign
     */
    public static class Word {
        String _word;
        String _punctBefore;
        String _punctAfter;
        int _begin;
        int _end;
        List<JSONObject> _tokens;

        public Word(String word, String punctBefore, String punctAfter, int begin, int end, List<JSONObject> tokens) {
            _word = word;
            _punctBefore = punctBefore;
            _punctAfter = punctAfter;
            _begin = begin;
            _end = end;
            _tokens = tokens;
        }

        public String getGlyphs() {
            return _punctBefore + _word + _punctAfter;
        }

        public boolean hasStopPunctuationAtEnd() {
            if (_punctAfter.length() > 0) {
                char lastChar = _punctAfter.charAt(_punctAfter.length() - 1);
                return lastChar == '.' || lastChar == '?' || lastChar == ';' || lastChar == '!';
            }
            return false;
        }

        public boolean hasStopPunctuationAtBegin() {
            if (_punctBefore.length() > 0) {
                char firstChar = _punctBefore.charAt(0);
                return firstChar == '¿' || firstChar == '¡';
            }
            return false;
        }

        public int duration() {
            return _end - _begin;
        }

    }

    /**
     * helper class to represent a segment in the result
     * it mainly contains a list of word tokens
     */
    public static class Segment {
        List<Word> _words = new ArrayList<>();

        public List<Word> getWords() {
            return _words;
        }

        public int nbWords() {
            return _words.size();
        }

        public String wordString() {
            List<String> words = new ArrayList<>();
            _words.forEach(word -> words.add(word.getGlyphs()));
            return String.join(" ", words);
        }

        public void pushBehind(List<Word> words) {
//            words.forEach(_words::add);
            _words.addAll(words);
        }

        public void pushBehind(Word word) {
            _words.add(word);
        }

        public void popBehind() {
            if (_words.size() > 0) {
                _words.remove(_words.size() - 1);
            }
        }

        public void pushFront(List<Word> words) {
            for (int i = 0; i < words.size(); i++) {
                _words.add(i, words.get(i));
            }
        }

        public int begin() {
            return _words.size() > 0 ? _words.get(0)._begin : 0;
        }

        public int end() {
            return _words.size() > 0 ? _words.get(_words.size() - 1)._end : 0;
        }

        public void setBegin(int begin) {
            if (_words.size() > 0) {
                _words.get(0)._begin = begin;
            }
        }

        public void setEnd(int end) {
            if (_words.size() > 0) {
                _words.get(_words.size() - 1)._end = end;
            }
        }

        public int duration() {
            return end() - begin();
        }

        @Override
        public String toString() {
            return "[" + begin() + " " + end() + "] (" + time_int2str(begin()) + " -> " + time_int2str(end()) + ") " + wordString();
        }

        public List<String> getLines(int maxCharsPerLine) {
            List<String> lines = new ArrayList<>();
            _words.forEach(w -> {
//                String word = _words.get(i).getGlyphs();
                String word = w.getGlyphs();

                if (lines.size() == 0) {
                    // first word
                    lines.add("" + word);
                } else if (word.length() >= maxCharsPerLine) {
                    // very long word, add a dedicated line
                    lines.add("" + word);
                } else if (word.length() + lines.get(lines.size() - 1).length() < maxCharsPerLine) {
                    // append to current line
                    String lastLine = lines.get(lines.size() - 1);
                    lines.set(lines.size() - 1, lastLine + " " + word);
                } else {
                    // need to add to next line
                    lines.add("" + word);
                }
            });
            return lines;
        }
    }

    // list of word tokens (from json)
    private static List<Word> _wordList;

    // list of segment (to populate the result)
    private List<Segment> _segments;

    // Segmentation parameters
    private static int _maxPause = 200;
    private static int _minDuration = 800;
    private static int _maxDuration = 7_000;
    private static int _maxCharsPerLine = 80;
    private static int _maxLinesPerSegment = 1;

    private int getMaxPause() {
        return _maxPause;
    }

    private void setMaxPause(int value) {
        _maxPause = value;
    }

    private int getMinDuration() {
        return _minDuration;
    }

    private void setMinDuration(int value) {
        _minDuration = value;
    }

    private int getMaxDuration() {
        return _maxDuration;
    }

    private void setMaxDuration(int value) {
        _maxDuration = value;
    }

    private int getMaxCharsPerLine() {
        return _maxCharsPerLine;
    }

    private void setMaxCharsPerLine(int value) {
        _maxCharsPerLine = value;
    }

    private int getMaxLinesPerSegment() {
        return _maxLinesPerSegment;
    }

    private void setMaxLinesPerSegment(int value) {
        _maxLinesPerSegment = value;
    }

    /**
     * Utility class, for the word processing.
     */
    private static class ExtendedWordBuilder {
        String _prePunct;
        String _trueToken;
        String _postPunct;
        int beginTime;
        int endTime;
        List<JSONObject> tokens;

        public ExtendedWordBuilder() {
            reset();
        }

        public void reset() {
            _prePunct = "";
            _trueToken = "";
            _postPunct = "";
            beginTime = -1;
            endTime = -1;
            tokens = new ArrayList<>();
        }

        public void flushWord() {
            if (!(_trueToken.isEmpty() && _prePunct.isEmpty() && _postPunct.isEmpty())) {
                Word word = new Word(_trueToken, _prePunct, _postPunct, beginTime, endTime, tokens);
                _wordList.add(word);
            }
            reset();
        }

        public boolean okToFlushOnPrePunct() {
            return !(_trueToken.isEmpty() && _postPunct.isEmpty());
        }

        public boolean okToFlushOnTrueToken() {
            return !(_trueToken.isEmpty() && _postPunct.isEmpty());
        }

        public void addPrePunct(String pre_p, int begin, int end, JSONObject token) {
            if (okToFlushOnPrePunct()) {
                flushWord();
            }
            if (beginTime != -1 && beginTime != begin) {
                System.out.println("[W]: addPrePunct: begin = " + begin + "(end=" + end + "), beginTime = " + beginTime);
            }
            beginTime = begin;
            _prePunct = _prePunct + pre_p;
            tokens.add(token);
        }

        public void addPostPunct(String post_p, int begin, int end, JSONObject token) {
            if (endTime != -1 && endTime != end) {
                System.out.printf("[W]: addPostToken: begin = %s, beginTime = %s\n",
                        NumberFormat.getInstance().format(begin),
                        NumberFormat.getInstance().format(beginTime));
            }
            endTime = end;
            _postPunct = _postPunct + post_p;
            tokens.add(token);
        }

        public void addTrueToken(String tt, int begin, int end, JSONObject token) {
            if (okToFlushOnTrueToken()) {
                flushWord();
            }
            if (beginTime != -1 && beginTime != begin) {
                System.out.println("[W]: addTrueToken: begin = " + begin + ", beginTime = " + beginTime);
            }
            beginTime = begin;
            endTime = end;
            _trueToken = _trueToken + tt;
            tokens.add(token);
        }
    }

    /**
     * load OCI json file and create list of word tokens
     * @param jsonFile the name of the original file
     */
    private void loadJson(String jsonFile) {
        try {
            _wordList = new ArrayList<>();

            Path filePath = Paths.get(jsonFile);
            String jsonTxt = Files.readString(filePath);

            JSONObject jo = new JSONObject(jsonTxt);
            JSONArray jtranscriptions = jo.getJSONArray("transcriptions");
            if (jtranscriptions.length() == 0) {
                System.out.println("[E] no transcriptions in json file");
            } else {
                // just take the first transcription
                JSONArray jtokens = jtranscriptions.getJSONObject(0).getJSONArray("tokens");
                System.out.printf("Found %d token(s).\n", jtokens.length());

                ExtendedWordBuilder extendedWordBuilder = new ExtendedWordBuilder();

                for (int i = 0; i < jtokens.length(); i++) {
                    JSONObject jtoken = jtokens.getJSONObject(i);
                    String wordText = jtoken.getString("token");
                    String beginText = jtoken.getString("startTime");
                    String endText = jtoken.getString("endTime");
                    String typeText = jtoken.getString("type");
                    beginText = beginText.substring(0, beginText.length() - 1);
                    endText = endText.substring(0, endText.length() - 1);
                    // convert duration in ms
                    int begin = Math.round(1_000 * Float.parseFloat(beginText));
                    int end = Math.round(1_000 * Float.parseFloat(endText));

                    if (typeText.equals(PUNCTUATION)) {
                        // Facing a punctuation
                        boolean isPrePunct = wordText.equals("¿") || wordText.equals("¡");
                        if (isPrePunct) {
                            extendedWordBuilder.addPrePunct(wordText, begin, end, jtoken);
                        } else {
                            extendedWordBuilder.addPostPunct(wordText, begin, end, jtoken);
                        }
                    } else {
                        extendedWordBuilder.addTrueToken(wordText, begin, end, jtoken);
                    }
                }
                extendedWordBuilder.flushWord();
            }
            System.out.printf("We have %d word(s).\n", _wordList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // debug print words
        if (VERBOSE) {
            _wordList.forEach(word -> System.out.println(word.getGlyphs()));
        }
    }

    /**
     * Split a too long segment into several segments
     * @param segment original segment
     * @param nbSplit nb chunks
     * @param maxCharsPerLine CLI prm
     * @param maxLinesPerSegment CLI prm
     * @return the list of splitted segments
     */
    private List<Segment> splitSegment(Segment segment, int nbSplit, int maxCharsPerLine, int maxLinesPerSegment) {
        List<Segment> newSegments = new ArrayList<>();
        int avgSegmentDuration = (segment.duration() / nbSplit);

        // split by time, respecting the number of lines
        newSegments.add(new Segment());
        for (int i = 0; i < segment._words.size(); i++) {
            Word word = segment._words.get(i);
            Segment lastSegment = newSegments.get(newSegments.size() - 1);

            // add first, in case too long, pop back and add into another segment
            lastSegment.pushBehind(word);

            if (lastSegment.getWords().size() > 1 && lastSegment.getLines(maxCharsPerLine).size() > maxLinesPerSegment) {
                // there was already some words, and with this new one, number of lines is too large
                lastSegment.popBehind();
                newSegments.add(new Segment());
                newSegments.get(newSegments.size() - 1).pushBehind(word);
            } else if (i != (segment.getWords().size() - 1) && newSegments.get(newSegments.size() - 1).duration() >= avgSegmentDuration) {
                // the average duration is exceeded
                newSegments.add(new Segment());
            }
        }
        return newSegments;
    }

    private List<Segment> splitSegmentOnPunctuation(Segment segment, int minDuration) {
        // this method will split a segment based on punctuation (".", "?", "¿" , ... but not on ",")
        // but new segment must conform to the minDuration parameter for the split
        List<Segment> newSegments = new ArrayList<>();
        segment._words.forEach(word -> {
            if (newSegments.size() == 0) {
                newSegments.add(new Segment());
            }
            Segment currentSegment = newSegments.get(newSegments.size() - 1);

            if (word.hasStopPunctuationAtBegin() && currentSegment.duration() >= minDuration) {
                // current segment is already long enough, create new segment starting with current word
                newSegments.add(new Segment());
                currentSegment = newSegments.get(newSegments.size() - 1);
                currentSegment.pushBehind(word);
            } else if (word.hasStopPunctuationAtEnd() && currentSegment.duration() + word.duration() >= minDuration) {
                // current segment with this word is long enough, add current word and create new segment
                currentSegment.pushBehind(word);
                newSegments.add(new Segment());
            } else {
                currentSegment.pushBehind(word);
            }
        });

        // verify if last segment is not too small, otherwise merge with previous one
        if (newSegments.size() >= 2 && newSegments.get(newSegments.size() - 1).duration() < minDuration) {
            Segment lastSegment = newSegments.get(newSegments.size() - 1);
            Segment previousSegment = newSegments.get(newSegments.size() - 2);
            lastSegment._words.forEach(previousSegment::pushBehind);
            newSegments.remove(newSegments.size() - 1);
        }
        return newSegments;
    }

    /**
     * Create a list of segments from a list of word tokens
     */
    private void createSegments() {

        List<Segment> segments = new ArrayList<>();

        // need at list a word
        if (_wordList.size() == 0) {
            System.out.println("[W] no words");
            _segments = segments;
            return;
        }

        int endTime = _wordList.get(_wordList.size() - 1)._end;

        // part 1:  create first Segmentation, only based on pauses
        for (Word word : _wordList) {
            if (segments.size() == 0 || (word._begin - segments.get(segments.size() - 1).end() > _maxPause)) {
                segments.add(new Segment());
            }
            Segment lastSegment = segments.get(segments.size() - 1);
            lastSegment.pushBehind(word);
        }
        System.out.printf("Step 1 - %d segments\n", segments.size());

        // part 2:  for too small segments, increase silence duration on the left/right, or merge with the closest segment
        List<Segment> segments2 = new ArrayList<>();

        for (int i = 0; i < segments.size(); i++) {
            Segment seg = segments.get(i);

            if (seg.duration() >= _minDuration) {
                segments2.add(seg);
            } else {
                // segment is too small, 2 possibilities
                //  - increase time stamps and trim silence around the segment (if enough silence)
                //  - or merge to right of left segment
                int missingDuration = _minDuration - seg.duration();
                int halfMissingDuration = missingDuration / 2;

                if (segments2.size() == 0 && i == segments.size() - 1) {
                    // single segment, artificially update timestamps
                    seg.setBegin(Math.max(0, seg.begin() - halfMissingDuration));
                    seg.setEnd(Math.min(endTime, seg.end() + halfMissingDuration));
                    segments2.add(seg);
                } else if (segments2.size() == 0) {
                    // first one
                    int diffWithNext = segments.get(i + 1).begin() - seg.end();
                    int availableBegin = seg.begin();
                    if (diffWithNext >= halfMissingDuration && availableBegin >= halfMissingDuration) {
                        // artificially update timestamps (enough silence at begin and end)
                        seg.setBegin(seg.begin() - halfMissingDuration);
                        seg.setEnd(seg.end() + halfMissingDuration);
                        segments2.add(seg);
                    } else if (diffWithNext > missingDuration) {
                        //artificially update timestamps
                        seg.setBegin(seg.begin() - availableBegin);
                        seg.setEnd(seg.end() + missingDuration - availableBegin);
                        segments2.add(seg);
                    } else {
                        // merge with next segment
                        segments.get(i + 1).pushFront(seg.getWords());
                    }
                } else if (i == segments.size() - 1) {
                    // last one
                    int diffWithPrevious = seg.begin() - segments2.get(segments2.size() - 1).end();
                    int availableEnd = endTime - seg.end();
                    if (diffWithPrevious >= halfMissingDuration && availableEnd >= halfMissingDuration) {
                        // artificially update timestamps (enough silence at begin and end)
                        seg.setBegin(seg.begin() - halfMissingDuration);
                        seg.setEnd(seg.end() + halfMissingDuration);
                        segments2.add(seg);
                    } else if (diffWithPrevious > missingDuration) {
                        // artificially update timestamps
                        seg.setBegin(seg.begin() - (missingDuration - availableEnd));
                        seg.setEnd(seg.end() + availableEnd);
                        segments2.add(seg);
                    } else {
                        // merge with previous segment
                        segments2.get(segments2.size() - 1).pushBehind(seg.getWords());
                    }
                } else {
                    // not first, not last element
                    int diffWithPrevious = seg.begin() - segments2.get(segments2.size() - 1).end();
                    int diffWithNext = segments.get(i + 1).begin() - seg.end();

                    if (diffWithPrevious >= halfMissingDuration && diffWithNext >= halfMissingDuration) {
                        // artificially update timestamps (enough silence at begin and end)
                        seg.setBegin(seg.begin() - halfMissingDuration);
                        seg.setEnd(seg.end() + halfMissingDuration);
                        segments2.add(seg);
                    } else if (diffWithNext < diffWithPrevious) {
                        // merge with next
                        segments.get(i + 1).pushFront(seg.getWords());
                    } else {
                        // merge with previous segment
                        segments2.get(segments2.size() - 1).pushBehind(seg.getWords());
                    }
                }
            }
        }
        System.out.printf("Step 2 - %d segments.\n", segments2.size());

        // part 3: split segments by punctuation
        segments = segments2;
        List<Segment> segments3 = new ArrayList<>();
        segments.forEach(seg -> {
            List<Segment> newSegmentsPunct = splitSegmentOnPunctuation(seg, _minDuration);
            segments3.addAll(newSegmentsPunct);
        });
        System.out.printf("Step 3 - %d segments.\n", segments3.size());

        // part 4: split too long segments
        segments = segments3;
        List<Segment> segments4 = new ArrayList<>();
        for (Segment seg : segments) {
            List<String> formattedLines = seg.getLines(_maxCharsPerLine);
            int nbNeededLines = formattedLines.size();

            if (seg.duration() <= _maxDuration && nbNeededLines <= _maxLinesPerSegment) {
                // not too long, not too many lines
                segments4.add(seg);
            } else {
                // Do the split
                if (VERBOSE) {
                    System.out.println("------ split -----");
                    System.out.println(seg);
                }
                int nbNeededSplitForLines = (int) Math.ceil((double)nbNeededLines / (double)_maxLinesPerSegment);
                int nbNeededSplitForDuration = (int) Math.ceil((double)seg.duration() / (double)_maxDuration);
                int nbSplit = Math.max(nbNeededSplitForLines, nbNeededSplitForDuration);

                List<Segment> newSegments = splitSegment(seg, nbSplit, _maxCharsPerLine, _maxLinesPerSegment);

                // it may happen more splits than nbSplit were done, that may result in a single word for the last segment
                // in that case, ask for a larger number of splits
                if (newSegments.size() > nbSplit) {
                    System.out.printf("ask for a larger number of split (seg size: %d, split: %d)\n", newSegments.size(), nbSplit);
                    newSegments = splitSegment(seg, nbSplit + 1, _maxCharsPerLine, _maxLinesPerSegment);
                }
                segments4.addAll(newSegments);

                if (VERBOSE) {
                    System.out.println("-->");
                    newSegments.forEach(_seg -> System.out.printf("(%d words) %s\n", _seg.nbWords(), _seg));
                    System.out.println("<--");
                }
            }
        }
        System.out.printf("Step 4 - %d segments.\n", segments4.size());
        _segments = segments4;
    }

    /**
     * Just a debug method to check if everything went fine
     * do not use it in production.
     */
    private void verifySegments() {
        // verify if word sequence is still the same
        List<String> wordsFromJson = new ArrayList<>();
        _wordList.forEach(word -> wordsFromJson.add(word.getGlyphs()));
        String wordStringFromJson = String.join(" ", wordsFromJson);

        List<String> wordsFromSegments = new ArrayList<>();
        _segments.forEach(seg -> wordsFromSegments.add(seg.wordString()));
        String wordStringFromSegments = String.join(" ", wordsFromSegments);

        if (!wordStringFromJson.equals(wordStringFromSegments)) {
            System.out.println("[ERROR] word sequence is different");
            System.out.println("wordStringFromJson size=" + wordStringFromJson.length());
            System.out.println("wordStringFromSegments size=" + wordStringFromSegments.length());
        // } else {
            // System.out.println("[OK] same word sequence");
        }
        // verify timestamps
        for (int i = 0; i < _segments.size(); i++) {
            Segment segment = _segments.get(i);
            if (segment.end() <= segment.begin()) {
                System.out.println("[ERROR] ill formed timestamps for segment id=" + i + " begin=" + segment.begin() + " end=" + segment.end());
            }
            if (i < _segments.size() - 1) {
                Segment nextSegment = _segments.get(i + 1);
                if (nextSegment.begin() < segment.end()) {
                    System.out.println("[ERROR] ill formed timestamps for segments id=" + i + "/" + (i + 1) + " end of current=" + segment.end() + " begin of next=" + nextSegment.begin());
                }
            }
        }
    }

    /**
     * Write the new JSON file
     * @param file output file name
     */
    private void writeNewJSON(String file) {
        try {
            // Compose new JSON Object
            JSONObject newJson = new JSONObject();
            newJson.put("schemaVersion", "2.0");
            newJson.put("monologues", new JSONArray());

            _segments.forEach(segment -> {
                JSONObject oneLine = new JSONObject(Map.of(
                        "speaker", new JSONObject(Map.of("id", "N/A")),
                        "start", segment._words.get(0)._begin / 1_000.0,
                        "end", segment._words.get(segment._words.size() - 1)._end / 1_000.0,
                        "terms", new JSONArray()));
                segment._words.forEach(word -> {
                    List<JSONObject> foundTokens = word._tokens;
                    foundTokens.forEach(token -> {
                        String tokenStartTime = (String)token.get("startTime");
                        String tokenEndTime = (String)token.get("endTime");
                        // Remove the trailing 's'
                        String beginText = tokenStartTime.substring(0, tokenStartTime.length() - 1);
                        String endText = tokenEndTime.substring(0, tokenEndTime.length() - 1);
                        // convert duration in ms
                        double begin = Double.parseDouble(beginText);
                        double end = Double.parseDouble(endText);
                        JSONObject term = new JSONObject(Map.of(
                                "start", begin,
                                "end", end,
                                "text", (String)token.get("token"),
                                "type", (String)token.get("type"),
                                "confidence", Double.parseDouble((String)token.get("confidence"))
                        ));
                        oneLine.getJSONArray("terms").put(term);
                    });
                });
                newJson.getJSONArray("monologues").put(oneLine);
            });

            // Spit out the new JSON object
            File fout = new File(file);
            FileOutputStream fos = new FileOutputStream(fout);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            String jsonString = newJson.toString(2);
            osw.write(jsonString + "\n");
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {

        OCIJson2OScribe ociJson2OScribe = new OCIJson2OScribe();

        // Parse CLI parameters
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(Option.builder("i").longOpt("input").hasArg().required(true).desc("input OCI json file").build());
        options.addOption(Option.builder("o").longOpt("output").hasArg().required(true).desc("output SRT file)").build());
        options.addOption(null, "max-c", true, "max chars per line (default is " + ociJson2OScribe.getMaxCharsPerLine() + ")");
        options.addOption(null, "max-l", true, "max lines per segment (default is " + ociJson2OScribe.getMaxLinesPerSegment() + ")");
        options.addOption(null, "max-d", true, "max duration per segment (default is " + ociJson2OScribe.getMaxDuration() + ")");
        options.addOption(null, "max-p", true, "max pause in a segment (default is " + ociJson2OScribe.getMaxPause() + ")");
        options.addOption(null, "min-d", true, "min duration for a segment (default is " + ociJson2OScribe.getMinDuration() + ")");
        options.addOption("h", "help", false, "show help");

        String ociJsonFile;
        String newJSONFile;

        try {
            CommandLine commandLine = parser.parse(options, args);

            ociJsonFile = commandLine.getOptionValue("input");
            newJSONFile = commandLine.getOptionValue("output");

            if (commandLine.hasOption("max-c")) {
                ociJson2OScribe.setMaxCharsPerLine(Integer.parseInt(commandLine.getOptionValue("max-c")));
            }
            if (commandLine.hasOption("max-l")) {
                ociJson2OScribe.setMaxLinesPerSegment(Integer.parseInt(commandLine.getOptionValue("max-l")));
            }
            if (commandLine.hasOption("max-d")) {
                ociJson2OScribe.setMaxDuration(Integer.parseInt(commandLine.getOptionValue("max-d")));
            }
            if (commandLine.hasOption("max-p")) {
                ociJson2OScribe.setMaxPause(Integer.parseInt(commandLine.getOptionValue("max-p")));
            }
            if (commandLine.hasOption("min-d")) {
                ociJson2OScribe.setMinDuration(Integer.parseInt(commandLine.getOptionValue("min-d")));
            }
            if (commandLine.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("OCIJson2Srt", "convert an OCI json file into and SRT file\n\n", options, "", true);
            }
        } catch (ParseException exception) {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
            return;
        }

        // Load json and create array of words
        ociJson2OScribe.loadJson(ociJsonFile);

        // Create segments
        ociJson2OScribe.createSegments();

        System.out.printf("We have generated %d segment%s\n",
                ociJson2OScribe._segments.size(),
                ociJson2OScribe._segments.size() > 1 ? "s" : "");

        // only for debug !!
        //   - verify if the word sequence is the same between json and srt
        //   - verify timestamps
        ociJson2OScribe.verifySegments();

        // Write expected JSON
        ociJson2OScribe.writeNewJSON(newJSONFile);

        System.out.println("Done");
    }
}