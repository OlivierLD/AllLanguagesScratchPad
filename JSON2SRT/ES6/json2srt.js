/**
 * Convert JSON to SRT
 * Usage:
 *   node json2srt.js -i ../input.json -o converted.srt --max-l 1 --max-c 80
 */
"use strict";

process.title = 'JSON2SRT';

let fs = require('fs');
let path = require("path");

let VERBOSE = false;
let workDir = process.cwd();

console.log("----------------------------------------------------");
console.log("Running from " + workDir);
console.log("Usage: node " + path.basename(__filename) + " --help");
console.log("----------------------------------------------------");

const INPUT_PROMPT = "-i";
const OUTPUT_PROMPT = "-o";
const MAX_C_PROMPT = "--max-c";
const MAX_L_PROMPT = "--max-l";
const MAX_D_PROMPT = "--max-d";
const MAX_P_PROMPT = "--max-p";
const MIN_D_PROMPT = "--min-d";
const HELP_PROMPT = "-h";
const HELP_PROMPT2 = "--help";
const HELP_PROMPT3 = "-help";

let prmValues = {
  maxPause: 200,
  minDuration: 800,
  maxDuration: 7000,
  maxCharPerLine: 80,
  maxLinesPerSegment: 1
};

let wordList = [];
let segments = [];

let reset = (wordBuilder) => {
  wordBuilder._prePunct = ""; 
  wordBuilder._trueToken = ""; 
  wordBuilder._postPunct = ""; 
  wordBuilder.beginTime = -1; 
  wordBuilder.endTime = -1;
};

let flushWord = (wordBuilder) => {
  if (!(wordBuilder._trueToken.trim().length === 0 && wordBuilder._prePunct.trim().length === 0 && wordBuilder._postPunct.trim().length === 0)) {
    let word = { 
      _word: wordBuilder._trueToken, 
      _punctBefore: wordBuilder._prePunct, 
      _punctAfter: wordBuilder._postPunct, 
      _begin: wordBuilder.beginTime, 
      _end: wordBuilder.endTime 
    };
    wordList.push(word);
  }
  reset(wordBuilder);
};

let condForFlushOnPrePunct = (wordBuilder) => {
  return !(wordBuilder._trueToken.trim().length === 0 && wordBuilder._postPunct.trim().length === 0);
}
let condForFlushOnTrueToken = (wordBuilder) => {
  return !(wordBuilder._trueToken.trim().length === 0 && wordBuilder._postPunct.trim().length === 0);
};

let addPrePunct = (wordBuilder, pre_p, begin, end) => {
  if (condForFlushOnPrePunct(wordBuilder)) { 
    flushWord(wordBuilder); 
  }
  if (wordBuilder.beginTime != -1 && wordBuilder.beginTime != begin) {
      console.log("[W]: addPrePunct: begin = " + begin + ", beginTime = " + beginTime);
  }
  wordBuilder.beginTime = begin;
  wordBuilder._prePunct = wordBuilder._prePunct + pre_p;
};

let addPostPunct = (wordBuilder, post_p, begin, end) => {
  if (wordBuilder.endTime != -1 && wordBuilder.endTime != end) {
      console.log("[W]: addTrueToken: begin = " + begin + ", beginTime = " + wordBuilder.beginTime);
  }
  wordBuilder.endTime = end;
  wordBuilder._postPunct = wordBuilder._postPunct + post_p;
};

let addTrueToken = (wordBuilder, tt, begin, end) => {
  if (condForFlushOnTrueToken(wordBuilder)) { 
    flushWord(wordBuilder); 
  }
  if (wordBuilder.beginTime != -1 && wordBuilder.beginTime != begin) {
    console.log("[W]: addTrueToken: begin = " + begin + ", beginTime = " + wordBuilder.beginTime);
  }
  wordBuilder.beginTime = begin; 
  wordBuilder.endTime = end;
  wordBuilder._trueToken = wordBuilder._trueToken + tt;
};

let getGlyphs = (word) => {
  return word._punctBefore + word._word + word._punctAfter;
};

let loadJson = (ociJsonFile) => {
  let jsonObj = JSON.parse(fs.readFileSync(ociJsonFile, 'utf8'));

  let transcriptions = jsonObj['transcriptions'];
  if (VERBOSE) {
    console.log(`Got ${transcriptions.length} transcription(s)`);
  }
  if (transcriptions.length === 0) {
    System.out.println("[E] no transcriptions in json file");
  } else {
    // just take the first transcription
    let jTokens = transcriptions[0]["tokens"];

    let extendedWordBuilder = {
      _prePunct: '',
      _trueToken: '',
      _postPunct: '',
      beginTime: -1,
      endTime: -1
    };

    if (VERBOSE) {
      console.log(`Found ${jTokens.length} token(s).`);
    }

    jTokens.forEach(token => {
      let wordText = token["token"];
      let beginText = token["startTime"];
      let endText = token["endTime"];
      let typeText = token["type"];
      beginText = beginText.substring(0, beginText.length - 1); // Remove the 's'
      endText = endText.substring(0, endText.length - 1); // Remove the 's'

      // convert duration in ms
      let begin = Math.round(1000 * parseFloat(beginText));
      let end = Math.round(1000 * parseFloat(endText));

      if (typeText === "PUNCTUATION") {
        // Facing a punctuation
        let isPrePunct = wordText === "¿";
        if (isPrePunct) {
            addPrePunct(extendedWordBuilder, wordText, begin, end);
        } else {
            addPostPunct(extendedWordBuilder, wordText, begin, end);
        }
      } else {
          addTrueToken(extendedWordBuilder, wordText, begin, end);
      }
    });
    flushWord(extendedWordBuilder);
  }
};

let segmentPushBack = (segment, word) => {
  segment._words.push(word);
};

let segmentPopBack = (segment) => {
  if (segment._words.length > 0) {
    segment._words.pop();
  }
};

let segmentPushFrontWords = (seg, words) => {
  seg._words.unshift(words);
};

let segmentPushBackWords = (seg, words) => {
  words.forEach(word => seg._words.push(word));
};

let segmentEnd = (seg) => {
  if (seg._words.length > 0) {
    return seg._words[seg._words.length - 1]._end;
  } else {
    return 0;
  }
};

let segmentBegin = (seg) => {
  if (seg._words.length > 0) {
    return seg._words[0]._begin;
  } else {
    return 0;
  }
};

let segmentDuration = (seg) => {
  return segmentEnd(seg) - segmentBegin(seg);
};

let setSegmentBegin = (seg, value) => {
  if (seg._words.length > 0) {
    seg._words[0]._begin = value;
  }
};

let setSegmentEnd = (seg, value) => {
  if (seg._words.length > 0) {
    seg._words[seg._words.length - 1]._end = value;
  }
};

let wordHasStopPunctuationAtEnd = (word) => {
  if (word._punctAfter.length > 0) {
      let lastChar = word._punctAfter[word._punctAfter.length - 1];
      if (lastChar === '.' || lastChar === '?' || lastChar === ';' || lastChar === '!') {
          return true;
      }
  }
  return false;
};

let wordHasStopPunctuationAtBegin = (word) => {
  if (word._punctBefore.length > 0) {
      let firstChar = _punctBefore[0];
      if (firstChar == '¿' || firstChar == '¡') {
          return true;
      }
  }
  return false;
};

let wordDuration = (word) => {
  return word._end - word._begin;
};

let splitSegmentOnPunctuation = (segment, minDuration) => {
  let newSegments = [];
  segment._words.forEach(word => {
    if (newSegments.length === 0) {
      newSegments.push({ _words: [] });
    }
    let currentSegment = newSegments[newSegments.length - 1];
    if (wordHasStopPunctuationAtBegin(word) && segmentDuration(currentSegment) >= minDuration) {
      // current segment is already long enough, create new segment starting with current word
      newSegments.push({ _words: [] });
      currentSegment = newSegments[newSegments.length - 1];
      segmentPushBack(currentSegment, word);
    } else if (wordHasStopPunctuationAtEnd(word) && segmentDuration(currentSegment) + wordDuration(word) >= minDuration) {
        // current segment with this word is long enough, add current word and create new segment
        segmentPushBack(currentSegment, word);
        newSegments.push({ _words: [] });
    } else {
      segmentPushBack(currentSegment, word);
    }
  });

  // verify if last segment is not too small, otherwise merge with previous one
  if (newSegments.length >= 2 && segmentDuration(newSegments[newSegments.length - 1]) < minDuration) {
    let lastSegment = newSegments[newSegments.length - 1];
    let previousSegment = newSegments[newSegments.length - 2];

    lastSegment._words.forEach(word => {
      segmentPushBack(previousSegment, word);
    });
    newSegments.pop();
  }

  return newSegments;
};

let getSegmentLines = (seg, maxCharsPerLine) => {
  let lines = [];
  seg._words.forEach(_word => {
      let word = getGlyphs(_word);

      if (lines.length === 0) {
          // first word
          lines.push("" + word);
      } else if (word.length >= maxCharsPerLine) {
          // very long word, add a dedicated line
          lines.push("" + word);
      } else if (word.length + lines[lines.length - 1].length < maxCharsPerLine) {
          // append to current line
          let lastLine = lines[lines.length - 1];
          lines[lines.length - 1] = (lastLine + " " + word);
      } else {
          // need to add to next line
          lines.push("" + word);
      }
  });
  return lines;
};

let segmentWordString = (segment) => {
  let words = [];
  segment._words.forEach((word, idx) => {
    words.push(getGlyphs(word));
  });
  let joined = words.join(" ");
  return joined;
};

// split a too long segments into several segments
let splitSegment = (segment, nbSplit, maxCharsPerLine, maxLinesPerSegment) => {
  let newSegments = [];
  let avgSegmentDuration = parseInt((segmentDuration(segment) / nbSplit).toFixed(0));

  // split by time, respecting the number of lines
  newSegments.push({ _words: [] });

  segment._words.forEach((word, idx) => {
      let lastSegment = newSegments[newSegments.length - 1];

      // add first, in case too long, pop back and add into another segment
      segmentPushBack(lastSegment, word);

      if (lastSegment._words.length > 1 && getSegmentLines(lastSegment, maxCharsPerLine).length > maxLinesPerSegment) {
          // there was already some words, and with this new one, number of lines is too large
          segmentPopBack(lastSegment);
          newSegments.push({ _words: [] });
          segmentPushBack(newSegments[newSegments.length - 1], word);
      } else if (idx !== (segment._words.length - 1) && segmentDuration(newSegments[newSegments.length - 1]) >= avgSegmentDuration) {
          // the average duration is exceeded
          newSegments.push({ _words: [] });
      }
  });
  return newSegments;
};

let createSegments = (listOfWords) => {
  let segs = [];

  if (listOfWords.length === 0) {
    console.log("[W] no words");
    segments = segs;
    return;
  }

  let endTime = listOfWords[listOfWords.length - 1]._end;

  listOfWords.forEach(word => {
    if (segs.length === 0 || (word._begin - segmentEnd(segs[segs.length - 1]) > prmValues.maxPause)) {
      segs.push({ _words: [] });
    }
    let lastSegment = segs[segs.length -1];
    segmentPushBack(lastSegment, word);
  });  

  if (VERBOSE) {
    console.log(`Step 1 - ${segs.length} segments`);
  }

  let segs2 = [];
  segs.forEach((seg, idx) => {
    if (segmentDuration(seg) >= prmValues.minDuration) {
      segs2.push(seg);
    } else {
      let missingDuration = prmValues.minDuration - segmentDuration(seg);
      let halfMissingDuration = missingDuration / 2;
      if (segs2.length === 0 && idx === segs.length - 1) {
        // single segment, artificially update timestamps
        setSegmentBegin(seg, Math.max(0, segmentBegin(seg) - halfMissingDuration));
        setSegmentEnd(seg, Math.min(endTime, segmentEnd(seg) + halfMissingDuration));
        segs2.push(seg);
      } else if (segs2.length === 0) {
        let diffWithNext =  segmentBegin(segs[idx + 1]) - segmentEnd(seg.end);
        let availableBegin = segmentBegin(seg);
        if (diffWithNext >= halfMissingDuration && availableBegin >= halfMissingDuration) {
            // artificially update timestamps (enough silence at begin and end)
            setSegmentBegin(seg, segmentBegin(seg) - halfMissingDuration);
            setSegmentEnd(seg, segmentEnd(seg) + halfMissingDuration);
            segs2.push(seg);
        } else if (diffWithNext > missingDuration) {
            // artificially update timestamps
            setSegmentBegin(seg, segmetBegin(seg) - availableBegin);
            setSegmentEnd(seg, segmentEnd(seg) + missingDuration - availableBegin);
            segs2.push(seg);
        } else {
            // merge with next segment
            segmentPushFrontWords(segs[idx + 1], seg._words);
        }
      } else if (idx === (segs.length - 1)) { // Last one
        let diffWithPrevious = segmentBegon(seg) - segmentEnd(segs2[segs2.length - 1]);
        let availableEnd = endTime - segmentEnd(seg);
        if (diffWithPrevious >= halfMissingDuration && availableEnd >= halfMissingDuration) {
            // artificially update timestamps (enough silence at begin and end)
            setSegmentBegin(seg, segmentBegin(seg) - halfMissingDuration);
            setSegmentEnd(seg, segmentEnd(seg) + halfMissingDuration);
            segs2.push(seg);
        } else if (diffWithPrevious > missingDuration) {
            // artificially update timestamps
            setSegmentBegin(seg, segmentBegin(seg) - (missingDuration - availableEnd));
            setSegmentEnd(seg, segmentEnd(seg) + availableEnd);
            segs2.push(seg);
        } else {
            // merge with previous segment
            segmentPushBackWords(segs2[segs2.length - 1], seg._words);
        }
      } else {  // not first, not last element
        let diffWithPrevious = segmentBegin(seg) - segmentEnd(segs2[segs2.length - 1]);
        let diffWithNext = segmentBegin(segs[idx + 1]) - segmentEnd(seg);

        if (diffWithPrevious >= halfMissingDuration && diffWithNext >= halfMissingDuration) {
            // artificially update timestamps (enough silence at begin and end)
            setSegmentBegin(seg, segmentBegin(seg) - halfMissingDuration);
            setSegmentEnd(seg, segmentEnd(seg) + halfMissingDuration);
            segs2.push(seg);
        } else if (diffWithNext < diffWithPrevious) {
            // merge with next
            segmentPushFrontWords(segs[idx + 1], seg._words);
        } else {
            // merge with previous segment
            segmentPushBackWords(segs2[segs2.length - 1], seg._words);
        }
      }
    }
  });  
  if (VERBOSE) {
    console.log(`Step 2 - ${segs2.length} segments`);
  }

  // Part 3: split segments by punctuation
  segs = segs2;

  let segs3 = [];
  segs.forEach(seg => {
    let newSegmentPunct = splitSegmentOnPunctuation(seg, prmValues.minDuration);
    newSegmentPunct.forEach(seg => {
      segs3.push(seg);
    });
  });

  if (VERBOSE) {
    console.log(`Step 3 - ${segs3.length} segments`);
  }

  // part 4: split too long segments
  segs = segs3;
  let segs4 = [];
  let alreadyTold = false; // For VERBOSE
  segs.forEach((seg, idx) => {
    let formattedLines = getSegmentLines(seg, prmValues.maxCharPerLine);
    let nbNeededLines = formattedLines.length;
    if (segmentDuration(seg) <= prmValues.maxDuration && nbNeededLines <= prmValues.maxLinesPerSegment) {
      // not too long, not too many lines
      segs4.push(seg);
    } else {
      // do the split
      let nbNeededSplitForLines = Math.ceil(nbNeededLines / prmValues.maxLinesPerSegment);
      let nbNeededSplitForDuration = Math.ceil(segmentDuration(seg) / prmValues.maxDuration);
      let nbSplit = Math.max(nbNeededSplitForLines, nbNeededSplitForDuration);

      let newSegments = splitSegment(seg, nbSplit, prmValues.maxCharPerLine, prmValues.maxLinesPerSegment);
      if (VERBOSE && !alreadyTold) {
        console.log(`Index of seg: ${idx}, nbNeededSplitForLines ${nbNeededSplitForLines}, nbNeededSplitForDuration ${nbNeededSplitForDuration}, nbSplit ${nbSplit}`);
        console.log(`New Segments: ${newSegments.length} elements.`)
        alreadyTold = true;
      }


      // it may happen more splits than nbSplit were done, that may result in a single word for the last segment
      // in that case, ask for a larger number of splits
      if (newSegments.length > nbSplit) {
          console.log("ask for a larger number of split ");
          newSegments = splitSegment(seg, nbSplit + 1, prmValues.maxCharPerLine, prmValues.maxLinesPerSegment);
      }

      newSegments.forEach(seg => {
        segs4.push(seg);
      });
    }
  });
  if (VERBOSE) {
    console.log(`Step 4 - ${segs4.length} segments`);
  }

  segments = segs4;
};

let verifySegments = (listOfWords, segments) => {
  let wordsFromJson = [];
  listOfWords.forEach((word, idx) => {
    wordsFromJson.push(getGlyphs(word));
  });
  let wordStringFromJson = wordsFromJson.join(' ');

  let wordsFromSegments = [];
  segments.forEach((seg, idx) => {
    wordsFromSegments.push(segmentWordString(seg));
  });
  let wordStringFromSegments = wordsFromSegments.join(" ");

  if (wordStringFromJson !== wordStringFromSegments) {
    console.log("[ERRROR] word sequence is different");
    console.log("wordStringFromJson size=" + wordStringFromJson.length);
    console.log("wordStringFromSegments size=" + wordStringFromSegments.length);
  } else {
      console.log("[OK] same word sequence");
  }

  segments.forEach((segment, idx) => {
    if (segmentEnd(segment) <= segmentBegin(segment)) {
      console.log("{ERRROR] ill formed timestamps for segment id=" + Sidx + " begin=" + segmentBegin(segment) + " end=" + SegmentEnd(segment));
    }
    if (idx < segments.length - 1) {
        let nextSegment = segments[idx + 1];
        if (segmentBegin(nextSegment) < segmentEnd(segment)) {
            System.out.println("{ERRROR] ill formed timestamps for segments id=" + idx + "/" + (idx + 1) + " end of current=" + segmentEnd(segment) + " begin of next=" + segmentBegin(nextSegment));
        }
    }
  });
};

let lpad = (str, len, pad) => {
  let s = str;
  while (s.length < len) {
    s = (pad === undefined ? ' ' : pad) + s;
  }

  return s;
};

let time_int2str = (value) => {
  let ms = value % 1000;
  let s = Math.floor((value - ms) / 1000) % 60;
  let m = Math.floor(((value - ms) - 1000 * s) / 60000) % 60;
  let h = Math.floor(((value - ms) - 1000 * s - 60000 * m) / 3600000);

  let formatted = `${lpad(h.toString(), 2, '0')}:${lpad(m.toString(), 2, '0')}:${lpad(s.toString(), 2, '0')},${lpad(ms.toString(), 3, '0')}`;
  // let formatted = `${h}:${m}:${s},${lpad(ms.toFixed(0), 3, '0')}`;
  // console.log(`With ${value}, ms:${ms}, formatted: ${formatted}`);
  // return String.format("%02d", h) + ":" + String.format("%02d", m) + ":" + String.format("%02d", s) + "," + String.format("%03d", ms);
  return formatted;
};

let writeSRT = (srtFile, segments) => {
  
  function spitOut(fileName, data) {
    fs.writeFileSync(fileName, data, {flag: 'a+'});
  }

  fs.open(srtFile, 'w', function (err, file) {
    if (err) {
      throw err;
    }
    // console.log('Saved!');
  });

  // for (let i=0; i<10; i++) {
  //   spitOut(srtFile, `Line #${i}\n`);
  // }

  segments.forEach((segment, idx) => {
    spitOut(srtFile, `${idx + 1}\n`);
    let oneLine = time_int2str(segmentBegin(segment)) + " --> " + time_int2str(segmentEnd(segment));
    spitOut(srtFile, `${oneLine}\n`);
    let formattedLines = getSegmentLines(segment, prmValues.maxCharPerLine);
    formattedLines.forEach((line, idx) => {
      spitOut(srtFile, `${line}\n`);
    });
    spitOut(srtFile, "\n");
  });
  
};

// Main starts here
let main = (args) => {

  let jsonFile = null;
  let srtFile = null;
  
  for (let i=0; i<args.length; i++) {
    // console.log("arg #%d: %s", i, args[i]);
    switch (args[i]) {
      case INPUT_PROMPT:
        jsonFile = args[i + 1];
        break;
      case OUTPUT_PROMPT:  
        srtFile = args[i + 1];
        break;
      case MAX_C_PROMPT:
        prmValues.maxCharPerLine = parseInt(args[i + 1]);
        break;
      case MAX_L_PROMPT:
        prmValues.maxLinesPerSegment = parseInt(args[i + 1]);
        break;
      case MAX_D_PROMPT:
        prmValues.maxDuration = parseInt(args[i + 1]);
        break;
      case MAX_P_PROMPT:
        prmValues.maxPause = parseInt(args[i + 1]);
        break;
      case MIN_D_PROMPT:
        prmValues.minDuration = parseInt(args[i + 1]);
        break;
      case HELP_PROMPT:
      case HELP_PROMPT2:
      case HELP_PROMPT3:
            console.log(`Parameters of ${__filename} are:`);
            console.log(`${INPUT_PROMPT} inputFile.json (mandatory)`);
            console.log(`${OUTPUT_PROMPT} outputFile.srt (mandatory)`);
            console.log(`${MAX_C_PROMPT} max characters per line (default ${prmValues.maxCharPerLine})`);
            console.log(`${MAX_L_PROMPT} max lines per segment (default ${prmValues.maxLinesPerSegment})`);
            console.log(`${MAX_D_PROMPT} max duration (default ${prmValues.maxDuration})`);
            console.log(`${MAX_P_PROMPT} max pause (default ${prmValues.maxPause})`);
            console.log(`${MIN_D_PROMPT} min duration (default ${prmValues.minDuration})`);
            process.exit(0);
        break;
    }
  }
  
  if (jsonFile === null) {
    throw new Error("No -i parameter...");
  }
  
  if (srtFile === null) {
    throw new Error("No -o parameter...");
  }
  
  if (VERBOSE) {
    console.log(`Moving on. Converting ${jsonFile} into ${srtFile}`);
  }
  // Reset. Should not be required
  wordList = [];
  segments = [];

  // Step 1
  loadJson(jsonFile);
  if (VERBOSE) {
    console.log(`We have ${wordList.length} words`);
    // for (let i=0; i<Math.min(wordList.length, 10); i++) {
    //   console.log(JSON.stringify(wordList[i], null, 2));
    // }
    console.log(JSON.stringify(wordList[0], null, 2));
    console.log(". . .");
    console.log(JSON.stringify(wordList[wordList.length - 1], null, 2));
  }
  // Step 2
  createSegments(wordList);
  if (VERBOSE) {
    console.log(`We have ${segments.length} segment(s)`);
  }

  // Step 3
  verifySegments(wordList, segments);

  // Step 4
  writeSRT(srtFile, segments);

  console.log("Conversion Completed");
};

// Let's go!
main(process.argv);
