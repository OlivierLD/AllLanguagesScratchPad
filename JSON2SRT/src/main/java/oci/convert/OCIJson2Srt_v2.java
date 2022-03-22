package oci.convert;

import java.io.*;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.Iterator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

public class OCIJson2Srt_v2 {

    // convert time from millisecond to SRT format
    static String time_int2str(int value) {
        int ms = value % 1000;
        int s = (int) Math.floor((value - ms) / 1000) % 60;
        int m = (int) Math.floor(((value - ms) - 1000 * s) / 60000) % 60;
        int h = (int) Math.floor(((value - ms) - 1000 * s - 60000 * m) / 3600000);

        return String.format("%02d", h) + ":" + String.format("%02d", m) + ":" + String.format("%02d", s) + "," + String.format("%03d", ms);
    }


    // helper class to represent a word token from json file
    // possibly accompanied by a preceding or following punctuation sign
    public class Word {
        String _word;
        String _punctBefore;
        String _punctAfter;
        int _begin;
        int _end;

        public Word(String word, String punctBefore, String punctAfter, int begin, int end) {
            _word = word;
            _punctBefore = punctBefore;
            _punctAfter = punctAfter;
            _begin = begin;
            _end = end;
        }

        public String getGlyphs() {
            return _punctBefore + _word + _punctAfter;
        }

        public boolean hasStopPunctuationAtEnd() {
            if (_punctAfter.length() > 0) {
                char lastChar = _punctAfter.charAt(_punctAfter.length() - 1);
                if (lastChar == '.' || lastChar == '?' || lastChar == ';' || lastChar == '!') {
                    return true;
                }
            }
            return false;
        }

        public boolean hasStopPunctuationAtBegin() {
            if (_punctBefore.length() > 0) {
                char firstChar = _punctBefore.charAt(0);
                if (firstChar == '¿' || firstChar == '¡') {
                    return true;
                }
            }
            return false;
        }

        public int duration() {
            return _end - _begin;
        }

    }


    // helper class to represent a segment in SRT
    // it mainly contains a list of word tokens
    public class Segment {
        ArrayList<Word> _words = new ArrayList<>();

        ArrayList<Word> getWords() {
            return _words;
        }

        int nbWords() {
            return _words.size();
        }

        String wordString() {
            ArrayList<String> words = new ArrayList<>();
            for (int i = 0; i < _words.size(); ++i) {
                words.add(_words.get(i).getGlyphs());
            }
            String joined = String.join(" ", words);
            return joined;
        }

        void pushBack(ArrayList<Word> words) {
            for (int i = 0; i < words.size(); ++i) {
                _words.add(words.get(i));
            }
        }

        void pushBack(Word word) {
            _words.add(word);
        }

        void popBack() {
            if (_words.size() > 0) {
                _words.remove(_words.size() - 1);
            }
        }

        void pushFront(ArrayList<Word> words) {
            for (int i = 0; i < words.size(); ++i) {
                _words.add(i, words.get(i));
            }
        }

        int begin() {
            if (_words.size() > 0) {
                return _words.get(0)._begin;
            } else {
                return 0;
            }
        }

        int end() {
            if (_words.size() > 0) {
                return _words.get(_words.size() - 1)._end;
            } else {
                return 0;
            }
        }

        void setBegin(int begin) {
            if (_words.size() > 0) {
                _words.get(0)._begin = begin;
            }
        }

        void setEnd(int end) {
            if (_words.size() > 0) {
                _words.get(_words.size() - 1)._end = end;
            }
        }

        int duration() {
            return end() - begin();
        }

        String asString() {
            return "[" + begin() + " " + end() + "] (" + time_int2str(begin()) + " -> " + time_int2str(end()) + ") " + wordString();
        }

        ArrayList<String> getLines(int maxCharsPerLine) {
            ArrayList<String> lines = new ArrayList<String>();
            for (int i = 0; i < _words.size(); ++i) {
                String word = _words.get(i).getGlyphs();

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
            }
            return lines;
        }
    }


    // list of word tokens (from json)
    ArrayList<Word> _wordList;

    // list of segment (to populate SRT)
    ArrayList<Segment> _segments;

    // SRT parameters
    int _maxPause = 200;
    int _minDuration = 800;
    int _maxDuration = 7000;
    int _maxCharsPerLine = 80;
    int _maxLinesPerSegment = 1;


    // setter/getter methods
    int getMaxPause() {
        return _maxPause;
    }

    void setMaxPause(int value) {
        _maxPause = value;
    }

    int getMinDuration() {
        return _minDuration;
    }

    void setMinDuration(int value) {
        _minDuration = value;
    }

    int getMaxDuration() {
        return _maxDuration;
    }

    void setMaxDuration(int value) {
        _maxDuration = value;
    }

    int getMaxCharsPerLine() {
        return _maxCharsPerLine;
    }

    void setMaxCharsPerLine(int value) {
        _maxCharsPerLine = value;
    }

    int getMaxLinesPerSegment() {
        return _maxLinesPerSegment;
    }

    void setMaxLinesPerSegment(int value) {
        _maxLinesPerSegment = value;
    }

    class ExtendedWordBuilder {
        String _prePunct;
        String _trueToken;
        String _postPunct;
        int beginTime;
        int endTime;

        ExtendedWordBuilder() {
            reset();
        }

        void reset() {
            _prePunct = "";
            _trueToken = "";
            _postPunct = "";
            beginTime = -1;
            endTime = -1;
        }

        void flushWord() {
            if (!(_trueToken.isEmpty() && _prePunct.isEmpty() && _postPunct.isEmpty())) {
                Word word = new Word(_trueToken, _prePunct, _postPunct, beginTime, endTime);
                _wordList.add(word);
            }
            reset();
        }

        boolean condForFlushOnPrePunct() {
            return !(_trueToken.isEmpty() && _postPunct.isEmpty());
        }

        boolean condForFlushOnTrueToken() {
            return !(_trueToken.isEmpty() && _postPunct.isEmpty());
        }

        void addPrePunct(String pre_p, int begin, int end) {
            if (condForFlushOnPrePunct()) {
                flushWord();
            }
            if (beginTime != -1 && beginTime != begin) {
                System.out.println("[W]: addPrePunct: begin = " + begin + ", beginTime = " + beginTime);
            }
            beginTime = begin;
            _prePunct = _prePunct + pre_p;
        }

        void addPostPunct(String post_p, int begin, int end) {
            if (endTime != -1 && endTime != end) {
                System.out.println("[W]: addPostToken: begin = " + begin + ", beginTime = " + beginTime);
            }
            endTime = end;
            _postPunct = _postPunct + post_p;
        }

        void addTrueToken(String tt, int begin, int end) {
            if (condForFlushOnTrueToken()) {
                flushWord();
            }
            if (beginTime != -1 && beginTime != begin) {
                System.out.println("[W]: addTrueToken: begin = " + begin + ", beginTime = " + beginTime);
            }
            beginTime = begin;
            endTime = end;
            _trueToken = _trueToken + tt;
        }
    }

    // load OCI json file and create list of word tokens
    void loadJson(String jsonfile) {
        try {
            _wordList = new ArrayList<>();

            Path filePath = Paths.get(jsonfile);
            String jsonTxt = Files.readString(filePath);

            JSONObject jo = new JSONObject(jsonTxt);
            JSONArray jtranscriptions = jo.getJSONArray("transcriptions");
            if (jtranscriptions.length() == 0) {
                System.out.println("[E] no transcriptions in json file");
            } else {
                // just take the first transcription
                JSONArray jtokens = jtranscriptions.getJSONObject(0).getJSONArray("tokens");
                System.out.println(String.format("Found %d token(s).", jtokens.length()));

                ExtendedWordBuilder extendedWordBuilder = new ExtendedWordBuilder();

                for (int i = 0; i < jtokens.length(); ++i) {
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

                    if (typeText.equals("PUNCTUATION")) {
                        // Facing a punctuation
                        boolean isPrePunct = wordText.equals("¿");
                        if (isPrePunct) {
                            extendedWordBuilder.addPrePunct(wordText, begin, end);
                        } else {
                            extendedWordBuilder.addPostPunct(wordText, begin, end);
                        }
                    } else {
                        extendedWordBuilder.addTrueToken(wordText, begin, end);
                    }
                }
                extendedWordBuilder.flushWord();
            }
            System.out.println(String.format("We have %d word(s).", _wordList.size()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // debug print words
        //for (int i = 0; i < _wordList.size(); ++i) {
        //	System.out.println(i + "   " + _wordList.get(i).getGlyphs());
        //}
    }

    // split a too long segments into several segments
    ArrayList<Segment> splitSegment(Segment segment, int nbSplit, int maxCharsPerLine, int maxLinesPerSegment) {
        ArrayList<Segment> newSegments = new ArrayList<>();
        int avgSegmentDuration = (int) (segment.duration() / nbSplit);

        // split by time, respecting the number of lines
        newSegments.add(new Segment());
        for (int i = 0; i < segment._words.size(); ++i) {
            Word word = segment._words.get(i);
            Segment lastSegment = newSegments.get(newSegments.size() - 1);

            // add first, in case too long, pop back and add into another segment
            lastSegment.pushBack(word);

            if (lastSegment.getWords().size() > 1 && lastSegment.getLines(maxCharsPerLine).size() > maxLinesPerSegment) {
                // there was already some words, and with this new one, number of lines is too large
                lastSegment.popBack();
                newSegments.add(new Segment());
                newSegments.get(newSegments.size() - 1).pushBack(word);
            } else if (i != (segment.getWords().size() - 1) && newSegments.get(newSegments.size() - 1).duration() >= avgSegmentDuration) {
                // the average duration is exceeded
                newSegments.add(new Segment());
            }
        }
        return newSegments;
    }


    ArrayList<Segment> splitSegmentOnPunctuation(Segment segment, int minDuration) {
        // this method will split a segment based on punctuation (".", "?", "¿" , ... but not on ",")
        // but new segment must conform to the minDuration parameter for the split
        ArrayList<Segment> newSegments = new ArrayList<>();
        for (int i = 0; i < segment._words.size(); ++i) {
            Word word = segment._words.get(i);
            if (newSegments.size() == 0) {
                newSegments.add(new Segment());
            }
            Segment currentSegment = newSegments.get(newSegments.size() - 1);

            if (word.hasStopPunctuationAtBegin() && currentSegment.duration() >= minDuration) {
                // current segment is already long enough, create new segment starting with current word
                newSegments.add(new Segment());
                currentSegment = newSegments.get(newSegments.size() - 1);
                currentSegment.pushBack(word);
            } else if (word.hasStopPunctuationAtEnd() && currentSegment.duration() + word.duration() >= minDuration) {
                // current segment with this word is long enough, add current word and create new segment
                currentSegment.pushBack(word);
                newSegments.add(new Segment());
            } else {
                currentSegment.pushBack(word);
            }
        }

        // verify if last segment is not too small, otherwise merge with previous one
        if (newSegments.size() >= 2 && newSegments.get(newSegments.size() - 1).duration() < minDuration) {
            Segment lastSegment = newSegments.get(newSegments.size() - 1);
            Segment previousSegment = newSegments.get(newSegments.size() - 2);
            for (int i = 0; i < lastSegment._words.size(); ++i) {
                Word word = lastSegment._words.get(i);
                previousSegment.pushBack(word);
            }
            newSegments.remove(newSegments.size() - 1);
        }
        return newSegments;
    }

    // create a list of segments from a list of word tokens
    void createSegments() {

        ArrayList<Segment> segments = new ArrayList<>();

        // need at list a word
        if (_wordList.size() == 0) {
            System.out.println("[W] no words");
            _segments = segments;
            return;
        }


        int endTime = _wordList.get(_wordList.size() - 1)._end;

        // part 1:  create first Segmentation, only based on pauses
        for (int i = 0; i < _wordList.size(); ++i) {
            Word word = _wordList.get(i);
            if (segments.size() == 0 || (word._begin - segments.get(segments.size() - 1).end() > _maxPause)) {
                segments.add(new Segment());
            }
            Segment lastSegment = segments.get(segments.size() - 1);
            lastSegment.pushBack(word);
        }

        System.out.println(String.format("Step 1 - %d segments", segments.size()));

        // part 2:  for too small segments, increase silence duration on the left/right, or merge with the closest segment
        ArrayList<Segment> segments2 = new ArrayList<>();

        for (int i = 0; i < segments.size(); ++i) {
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
                        segments2.get(segments2.size() - 1).pushBack(seg.getWords());
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
                        segments2.get(segments2.size() - 1).pushBack(seg.getWords());
                    }
                }
            }
        }

        System.out.println(String.format("Step 2 - %d segments", segments2.size()));

        // part 3: split segments by punctuation
        segments = segments2;
        ArrayList<Segment> segments3 = new ArrayList<Segment>();
        for (int i = 0; i < segments.size(); ++i) {
            ArrayList<Segment> newSegmentsPunct = splitSegmentOnPunctuation(segments.get(i), _minDuration);
            for (int j = 0; j < newSegmentsPunct.size(); ++j) {
                segments3.add(newSegmentsPunct.get(j));
            }
        }

        System.out.println(String.format("Step 3 - %d segments", segments3.size()));

        // part 4: split too long segments
        segments = segments3;
        ArrayList<Segment> segments4 = new ArrayList<>();

        for (int i = 0; i < segments.size(); ++i) {
            Segment seg = segments.get(i);
            ArrayList<String> formattedLines = seg.getLines(_maxCharsPerLine);
            int nbNeededLines = formattedLines.size();

            if (seg.duration() <= _maxDuration && nbNeededLines <= _maxLinesPerSegment) {
                // not too long, not too many lines
                segments4.add(seg);
            } else {
                // do the split

                //System.out.println("------ split ----- " + segments.size());
                //System.out.println(seg.asString());

                int nbNeededSplitForLines = (int) Math.ceil(nbNeededLines / _maxLinesPerSegment);
                int nbNeededSplitForDuration = (int) Math.ceil(seg.duration() / _maxDuration);
                int nbSplit = Math.max(nbNeededSplitForLines, nbNeededSplitForDuration);

                ArrayList<Segment> newSegments = splitSegment(seg, nbSplit, _maxCharsPerLine, _maxLinesPerSegment);


                // it may happen more splits than nbSplit were done, that may result in a single word for the last segment
                // in that case, ask for a larger number of splits
                if (newSegments.size() > nbSplit) {
                    System.out.println("ask for a larger number of split ");
                    newSegments = splitSegment(seg, nbSplit + 1, _maxCharsPerLine, _maxLinesPerSegment);
                }


                for (int j = 0; j < newSegments.size(); ++j) {
                    segments4.add(newSegments.get(j));
                }

                //System.out.println("-->");
                //for (int j = 0; j < newSegments.size(); ++j) {
                //	System.out.println(newSegments.get(j).asString());
                //}

            }
        }
        System.out.println(String.format("Step 4 - %d segments", segments4.size()));

        _segments = segments4;
    }


    // just a debug method to check if everithing when fine
    // do not call it in production
    void verifySegments() {
        // verify if word sequence is still the same
        ArrayList<String> wordsFromJson = new ArrayList<>();
        for (int i = 0; i < _wordList.size(); ++i) {
            wordsFromJson.add(_wordList.get(i).getGlyphs());
        }
        String wordStringFromJson = String.join(" ", wordsFromJson);

        ArrayList<String> wordsFromSegments = new ArrayList<>();
        for (int i = 0; i < _segments.size(); ++i) {
            wordsFromSegments.add(_segments.get(i).wordString());
        }
        String wordStringFromSegments = String.join(" ", wordsFromSegments);

        if (!wordStringFromJson.equals(wordStringFromSegments)) {
            System.out.println("[ERRROR] word sequence is different");
            System.out.println("wordStringFromJson size=" + wordStringFromJson.length());
            System.out.println("wordStringFromSegments size=" + wordStringFromSegments.length());
        } else {
            //System.out.println("[OK] same word sequence");
        }

        // verify timestamps
        for (int i = 0; i < _segments.size(); ++i) {
            Segment segment = _segments.get(i);
            if (segment.end() <= segment.begin()) {
                System.out.println("{ERRROR] ill formed timestamps for segment id=" + String.valueOf(i) + " begin=" + String.valueOf(segment.begin()) + " end=" + String.valueOf(segment.end()));
            }
            if (i < _segments.size() - 1) {
                Segment nextSegment = _segments.get(i + 1);
                if (nextSegment.begin() < segment.end()) {
                    System.out.println("{ERRROR] ill formed timestamps for segments id=" + String.valueOf(i) + "/" + String.valueOf(i + 1) + " end of current=" + String.valueOf(segment.end()) + " begin of next=" + String.valueOf(nextSegment.begin()));
                }
            }
        }
    }


    // write the list of segments into a SRT file
    void writeSRT(String file) {
        try {
            File fout = new File(file);
            FileOutputStream fos = new FileOutputStream(fout);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            for (int i = 0; i < _segments.size(); i++) {
                Segment segment = _segments.get(i);
                osw.write(String.valueOf(i + 1) + "\n");
                osw.write(time_int2str(segment.begin()) + " --> " + time_int2str(segment.end()) + "\n");
                ArrayList<String> formattedLines = segment.getLines(_maxCharsPerLine);
                for (int l = 0; l < formattedLines.size(); ++l) {
                    osw.write(formattedLines.get(l) + "\n");
                }
                osw.write("\n");
            }
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        OCIJson2Srt_v2 ocijson2srt = new OCIJson2Srt_v2();

        // parse parameters
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(Option.builder("i").longOpt("input").hasArg().required(true).desc("input OCI json file").build());
        options.addOption(Option.builder("o").longOpt("output").hasArg().required(true).desc("output SRT file)").build());
        options.addOption(null, "max-c", true, "max chars per line (default is " + ocijson2srt.getMaxCharsPerLine() + ")");
        options.addOption(null, "max-l", true, "max lines per segment (default is " + ocijson2srt.getMaxLinesPerSegment() + ")");
        options.addOption(null, "max-d", true, "max duration per segment (default is " + ocijson2srt.getMaxDuration() + ")");
        options.addOption(null, "max-p", true, "max pause in a segment (default is " + ocijson2srt.getMaxPause() + ")");
        options.addOption(null, "min-d", true, "min duration for a segment (default is " + ocijson2srt.getMinDuration() + ")");
        options.addOption("h", "help", false, "show help");

        String ociJsonFile = null;
        String srtFile = null;

        try {
            CommandLine commandLine = parser.parse(options, args);

            ociJsonFile = commandLine.getOptionValue("input");
            srtFile = commandLine.getOptionValue("output");

            if (commandLine.hasOption("max-c")) {
                ocijson2srt.setMaxCharsPerLine(Integer.parseInt(commandLine.getOptionValue("max-c")));
            }
            if (commandLine.hasOption("max-l")) {
                ocijson2srt.setMaxLinesPerSegment(Integer.parseInt(commandLine.getOptionValue("max-l")));
            }
            if (commandLine.hasOption("max-d")) {
                ocijson2srt.setMaxDuration(Integer.parseInt(commandLine.getOptionValue("max-d")));
            }
            if (commandLine.hasOption("max-p")) {
                ocijson2srt.setMaxPause(Integer.parseInt(commandLine.getOptionValue("max-p")));
            }
            if (commandLine.hasOption("min-d")) {
                ocijson2srt.setMinDuration(Integer.parseInt(commandLine.getOptionValue("min-d")));
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

        // load json and create array of words
        ocijson2srt.loadJson(ociJsonFile);

        // create segments
        ocijson2srt.createSegments();

        System.out.println(String.format("There are %d segment(s)", ocijson2srt._segments.size()));

        // only for debug !!
        //   - verify if the word sequence is the same between json and srt
        //   - verify timestamps
        ocijson2srt.verifySegments();

        // write SRT
        ocijson2srt.writeSRT(srtFile);

    }
}