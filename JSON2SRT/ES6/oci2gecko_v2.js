/**
 * Convert (oci)JSON to Gecko Json.
 * Uses JavaScript classes.
 * 
 * Usage:
 *   node oci2gecko_v2.js -i ../input.json -o converted.json --max-l 1 --max-c 80
 * 
 * Do look at the VERBOSE and DO_VERIFY variables.
 */
"use strict";

process.title = 'Oci2Gecko-Class';

const VERBOSE = false;
const DO_VERIFY = true;

let fs = require('fs');
let path = require('path');

console.log("----------------------------------------------------");
console.log("Running from " + process.cwd());
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
const HELP_PROMPT4 = "-?";

let prmValues = {
    maxPause: 200,
    minDuration: 800,
    maxDuration: 7000,
    maxCharPerLine: 80,
    maxLinesPerSegment: 1
};
  
//------------- Class  Word ---------------
// helper class to represent a word token from json file
// possibly accompanied with a preceding or following punctuation sign
//-------------------------------------------- 
class Word {
    constructor(word, punctBefore, punctAfter, begin, end, tokens) {
        this._word = word;                  // string
        this._punctBefore = punctBefore;    // string
        this._punctAfter = punctAfter;      // string
        this._begin = begin;                // int
        this._end = end;                    // int
        this._tokens = tokens               // list of words
    }
    
    getGlyphs() {
        return this._punctBefore + this._word + this._punctAfter;
    }
    
    hasStopPunctuationAtEnd() {
        if (this._punctAfter.length > 0) {
            let lastChar = this._punctAfter.charAt(this._punctAfter.length-1);
            if (lastChar === '.' || lastChar === '?' || lastChar === ';' || lastChar === '!') {
                return true;
            }
        }               
        return false;
    }
    
    hasStopPunctuationAtBegin() {
        if (this._punctBefore.length > 0) {
            let firstChar = this._punctBefore.charAt(0);
            if (firstChar === '¿' || firstChar === '¡') {
                return true;
            }
        }               
        return false;
    }
    
    duration() {
        return this._end - this._begin;
    }
}

//------------- Class  Segment ---------------
// helper class to represent a segment
// it mainly contains a list of word tokens
//-------------------------------------------- 
class Segment {
    _words = [];
    
    // convert time from millisecond to SRT format
    time_int2str(value) {
        let ms = value % 1000;
        let s = Math.trunc(Math.floor((value-ms)/1000) % 60);
        let m = Math.trunc(Math.floor(((value-ms) - 1000 *s) / 60000) % 60);
        let h = Math.trunc(Math.floor(((value-ms) - 1000 *s - 60000 * m) / 3600000));
        
        return h.toLocaleString('en-US', {minimumIntegerDigits: 2,useGrouping: false}) + ":" + m.toLocaleString('en-US', {minimumIntegerDigits: 2,useGrouping: false}) + ":" + s.toLocaleString('en-US', {minimumIntegerDigits: 2,useGrouping: false}) + "," + ms.toLocaleString('en-US', {minimumIntegerDigits: 3,useGrouping: false});
    }
    
    getWords() {
        return this._words;
    }
    
    nbWords() {
        return this._words.length;
    }
    
    wordString() {
        let words = [];
        this._words.forEach(word => words.push(word.getGlyphs()));
        return words.join(" ");
    }
    
    pushBack(word) {
        if (Array.isArray(word)) {
            word.forEach(el => this._words.push(el));
        } else {
            this._words.push(word);
        }
    }
    
    popBack() {
        if (this._words.length > 0) {
            this._words.pop();
        }
    }
    
    pushFront(words) {
        this._words.unshift(words);
    }
    
    begin()  {
        if (this._words.length > 0) {
            return this._words[0]._begin;
        } else {
            return 0;
        }
    }
    
    end()  {
        if (this._words.length > 0) {
            return this._words[this._words.length - 1]._end;
        } else {
            return 0;
        }
    }
    
    setBegin(begin) {
        if (this._words.length > 0) {
            this._words[0]._begin = begin;
        }
    }   
    
    setEnd(end) {
        if (this._words.length > 0) {
            this._words[this._words.length - 1]._end = end;
        }
    }
    
    duration() {
        return this.end() - this.begin();
    }
    
    asString() {
        return "[" + this.begin() + " " + this.end() + "] (" + this.time_int2str(this.begin()) + " -> " + this.time_int2str(this.end()) + ") " + this.wordString() ;
    }
    
    getLines(maxCharsPerLine) {
        let lines = [];
        this._words.forEach(w => {
            let word = w.getGlyphs();
            if (lines.length == 0) {
                // first word
                lines.push("" + word);
            } else if (word.length >= maxCharsPerLine) {
                // very long word, push a dedicated line
                lines.push("" + word);
            } else if (word.length + lines[lines.length - 1].length  < maxCharsPerLine) {
                // append to current line
                let lastLine = lines[lines.length - 1];
                lines[lines.length - 1] =  lastLine + " " + word;
            } else {
                // need to push to next line
                lines.push("" + word);
            }
        });
        return lines;
    }
}

//--------- Class  ExtendedWordBuilder -------
// used to extend a word with some punctuation tokens 
// as prefixes or suffixes to current word
//--------------------------------------------
class ExtendedWordBuilder {
    _prePunct = "";
    _trueToken = "";
    _postPunct = "";
    _beginTime = -1;
    _endTime = -1;
    _tokens = [] // list of tokens corresponding to one extendedWord
    
    _wordList = [];
    
    reset() {
        this._prePunct = ""; 
        this._trueToken = ""; 
        this._postPunct = ""; 
        this._beginTime = -1; 
        this._endTime = -1;
        this._tokens = []
    }
    
    getWordList() {
        return this._wordList;
    }
    
    flushWord() {
        if (!(this._trueToken === "" && this._prePunct === "" && this._postPunct === "")) {
            let word = new Word(this._trueToken, this._prePunct, this._postPunct, this._beginTime, this._endTime, this._tokens);
            this._wordList.push(word);
        }
        this.reset();
    }
    
    okToFlushOnPrePunct() {
        return !(this._trueToken === "" && this._postPunct === "");
    }
    
    okToFlushOnTrueToken() {
        return !(this._trueToken === "" && this._postPunct === "");
    }
    
    addPrePunct(pre_p, begin, end, token) {
        if (this.okToFlushOnPrePunct()) { this.flushWord(); }
        if (this._beginTime !== -1 && this._beginTime !== begin) {
            console.log(`[W]: addPrePunct: begin = ${begin}, _beginTime = ${this._beginTime}, pre_p= ${pre_p}`);
        }
        this._beginTime = begin;
        this._prePunct = this._prePunct + pre_p;
        this._tokens.push(token)
    }
    
    addPostPunct(post_p, begin, end, token) {
        if (this._endTime !== -1 && this._endTime !== end) {
            console.log(`[W]: addPostToken: end = ${end}, _endTime= ${this._endTime}, post_p= ${post_p}`);
            // console.log(`  Token: ${JSON.stringify(token, null, 2)}`);
        }
        this._endTime = end;
        this._postPunct = this._postPunct + post_p;
        this._tokens.push(token)
    }
    
    addTrueToken(tt, begin, end, token) {
        if (this.okToFlushOnTrueToken()) { this.flushWord(); }
        if (this._beginTime != -1 && this._beginTime != begin) {
            console.log(`[W]: addTrueToken: begin = ${begin}, _beginTime = ${this._beginTime}, tt= ${tt}`);
        }
        this._beginTime = begin; 
        this._endTime = end;
        this._trueToken = this._trueToken + tt;
        this._tokens.push(token)
    }
}

//--------- Class  OCIJson2OScrib -----------------
// Main class to convert OCI json to OScribe format
//-------------------------------------------------
class OCIJson2OScribe {
    // Segmentation parameters
    _maxPause = 200;
    _minDuration = 800;
    _maxDuration = 7000;
    _maxCharsPerLine = 80;
    _maxLinesPerSegment = 1;

    constructor(cliPrms) {
        this._maxPause = cliPrms.maxPause;
        this._minDuration = cliPrms.minDuration;
        this._maxDuration = cliPrms.maxDuration;
        this._maxCharsPerLine = cliPrms.maxCharPerLine;
        this._maxLinesPerSegment = cliPrms.maxLinesPerSegment;    
    }

    _VERBOSE = false; // debug
    
    _wordList = [];
    _segments = [];
    
    //--------- loadTranscription(jsonObj) ------------
    // create list of word tokens from OCI jsonObj
    //-------------------------------------------------
    loadTranscription(jsonObj) {
        let jTokens = jsonObj["transcriptions"][0]["tokens"]
        let extendedWordBuilder = new ExtendedWordBuilder();
        
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
                let isPrePunct = (wordText === "¿" || wordText === "¡");
                if (isPrePunct) {
                    extendedWordBuilder.addPrePunct(wordText, begin, end, token);
                } else {
                    extendedWordBuilder.addPostPunct(wordText, begin, end, token);
                }
            } else {
                extendedWordBuilder.addTrueToken(wordText, begin, end, token);
            }
        });
        extendedWordBuilder.flushWord();

        this._wordList = extendedWordBuilder.getWordList()
        
        if (this._VERBOSE) {
            console.log(`loadTranscription: ${jTokens.length} input tokens --> ${this._wordList.length} punctuated words`);
        }  
        
        // Debug print words
        if (this._VERBOSE) {
            console.log('_wordList.length: ' + this._wordList.length)
            this._wordList.forEach((word, i) => console.log(i + "   " + word.getGlyphs()));
        }
    }
    
    //-------------- createSegments() ----------------------
    // create a list of segments from a list of word tokens
    //------------------------------------------------------
    createSegments() {
        
        let segments = [];
        
        // need at list a word
        if (this._wordList.length === 0) {
            console.log("[W] no words");
            this._segments = segments;
            return;
        }
                
        let endTime = this._wordList[this._wordList.length- 1]._end;
        
        // Part 1:  create first Segmentation, only based on pauses
        this._wordList.forEach((word, i) => {
            if (segments.length === 0 || (word._begin - segments[segments.length - 1].end() > this._maxPause)) {
                segments.push(new Segment());
            }
            let lastSegment = segments[segments.length - 1];
            lastSegment.pushBack(word);
        });
        
        if (this._VERBOSE) {
            console.log(`createSegments - part 1: ${segments.length} segments`);
        }
        
        // Part 2:  for too small segments, increase silence duration on the left/right, or merge with closest segment
        let segments2 = [];
        segments.forEach((seg, i) => {
            if (seg.duration() >= this._minDuration) {
                segments2.push(seg);
            } else {
                // segment is too small, 2 possibilities
                //  - increase time stamps and trim silence around the segment (if enough silence)
                //  - or merge to right of left segment
                let missingDuration = this._minDuration - seg.duration();
                let halfMissingDuration = missingDuration / 2;
            
                if (segments2.length === 0 && i === segments.length -1) {
                    // single segment, artificially update timestamps
                    seg.setBegin(Math.max(0, seg.begin() - halfMissingDuration));
                    seg.setEnd(Math.min(endTime, seg.end() + halfMissingDuration));
                    segments2.push(seg);
                } else if (segments2.length === 0) {
                    // first one
                    let diffWithNext = segments[i+1].begin() - seg.end();
                    let availableBegin = seg.begin();
                    if (diffWithNext >= halfMissingDuration && availableBegin >= halfMissingDuration) {
                        // artificially update timestamps (enough silence at begin and end)
                        seg.setBegin(seg.begin() - halfMissingDuration);
                        seg.setEnd(seg.end() + halfMissingDuration);
                        segments2.push(seg);
                    } else if (diffWithNext > missingDuration) {
                        //artificially update timestamps
                        seg.setBegin(seg.begin() - availableBegin);
                        seg.setEnd(seg.end() + missingDuration - availableBegin);
                        segments2.push(seg);
                    } else {
                        // merge with next segment
                        segments[i+1].pushFront(seg.getWords())  ;
                    }
                } else if (i === segments.length -1) {
                    // last one
                    let diffWithPrevious = seg.begin() - segments2[segments2.length - 1].end();
                    let availableEnd = endTime - seg.end();
                    if (diffWithPrevious >= halfMissingDuration && availableEnd >= halfMissingDuration) {
                        // artificially update timestamps (enough silence at begin and end)
                        seg.setBegin(seg.begin() - halfMissingDuration);
                        seg.setEnd(seg.end() + halfMissingDuration);
                        segments2.push(seg);
                    } else if (diffWithPrevious > missingDuration) {
                         // artificially update timestamps
                        seg.setBegin(seg.begin() - (missingDuration - availableEnd));
                        seg.setEnd(seg.end() + availableEnd);
                        segments2.push(seg);
                    } else {
                        // merge with previous segment
                        segments2[segments2.length - 1].pushBack(seg.getWords());
                    }
                } else {
                    // not first, not last element
                    let diffWithPrevious = seg.begin() - segments2[segments2.length - 1].end();
                    let diffWithNext = segments[i+1].begin() - seg.end();
                    
                    if (diffWithPrevious >= halfMissingDuration && diffWithNext >= halfMissingDuration) {
                         // artificially update timestamps (enough silence at begin and end)
                        seg.setBegin(seg.begin() - halfMissingDuration);
                        seg.setEnd(seg.end() + halfMissingDuration);
                        segments2.push(seg);
                    } else if (diffWithNext < diffWithPrevious) {
                        // merge with next
                        segments[i+1].pushFront(seg.getWords());
                    } else {
                        // merge with previous segment
                        segments2[segments2.length - 1].pushBack(seg.getWords());
                    }
                }
            }
        });
        
        if (this._VERBOSE) {
            console.log(`createSegments - part 2: ${segments2.length} segments`);
        }
        
        // Part 3: split segments by punctuation
        segments = segments2;
        let segments3 = [];
        segments.forEach((seg, i) => {
            let newSegmentsPunct = this.splitSegmentOnPunctuation(seg, this._minDuration);
            newSegmentsPunct.forEach(nsp => segments3.push(nsp));
        });
        
        if (this._VERBOSE) {
            console.log(`createSegments - part 3: ${segments3.length} segments`);
        }        
        
        // part 4: split too long segments 
        segments = segments3;
        let segments4 = [];
        segments.forEach((seg, i) => {
            let formattedLines = seg.getLines(this._maxCharsPerLine);
            let nbNeededLines = formattedLines.length;
            
            if (seg.duration() <= this._maxDuration &&  nbNeededLines <= this._maxLinesPerSegment) {
                // not too long, not too many lines
                segments4.push(seg);
            } else {
                // do the split
                //console.log("------ split ----- " + segments.length);
                //console.log(seg.asString());
                
                let nbNeededSplitForLines = Math.trunc(Math.ceil(nbNeededLines / this._maxLinesPerSegment));
                let nbNeededSplitForDuration = Math.trunc(Math.ceil(seg.duration()/ this._maxDuration));
                let nbSplit = Math.max(nbNeededSplitForLines, nbNeededSplitForDuration);
                
                let newSegments = this.splitSegment(seg, nbSplit, this._maxCharsPerLine, this._maxLinesPerSegment);
                              
                // it may happen more splits than nbSplit were done, that may result in a single word for the last segment
                // in that case, ask for a larger number of splits
                if (newSegments.length > nbSplit) {
                    //console.log("ask for a larger number of split ");
                    newSegments = this.splitSegment(seg, nbSplit + 1, this._maxCharsPerLine, this._maxLinesPerSegment);
                }
                newSegments.forEach(seg => segments4.push(seg));
                
                //console.log("-->");
                //for (let j = 0; j < newSegments.length; ++j) {
                //  console.log(newSegments[j].asString());
                //}
            }
        }); 
        this._segments = segments4;
        if (this._VERBOSE) {
            console.log(`createSegments - part 4: ${segments4.length} segments`);
            console.log(segments4);
        } 

    }
    
    //--------- splitSegmentOnPunctuation() -----------
    //-------------------------------------------------
    splitSegmentOnPunctuation(segment, minDuration) {
        // this method will split a segment based on punctuation (".", "?", "¿" , ... but not on ",")
        // but new segment must conform to the minDuration parameter for the split
        let newSegments = [];
        segment._words.forEach((word, i) => {

            if (newSegments.length === 0) {
                newSegments.push(new Segment());
            }
            let currentSegment = newSegments[newSegments.length - 1];
            
            if (word.hasStopPunctuationAtBegin() && currentSegment.duration() >= minDuration ) {
                // current segment is already long enough, create new segment starting with current word
                newSegments.push(new Segment());
                currentSegment = newSegments[newSegments.length -1];
                currentSegment.pushBack(word);
            } else if (word.hasStopPunctuationAtEnd() && currentSegment.duration() + word.duration() >= minDuration ) {
                // current segment with this word is long enough, add current word and create new segment
                currentSegment.pushBack(word);
                newSegments.push(new Segment());
            } else {
                currentSegment.pushBack(word);
            }
        });
        
        // verify if last segment is not too small, otherwise merge with previous one
        if (newSegments.length >= 2 && newSegments[newSegments.length - 1].duration() < minDuration) {
            let lastSegment = newSegments[newSegments.length - 1];
            let previousSegment = newSegments[newSegments.length - 2];
            lastSegment._words.forEach(word => previousSegment.pushBack(word)); 
            newSegments.pop();
        }
        return newSegments;
    }
    
    //-------------- splitSegment() -------------------
    // split a too long segments into several segments
    //-------------------------------------------------
    splitSegment(segment, nbSplit, maxCharsPerLine, maxLinesPerSegment) {
        let newSegments = [];
        let avgSegmentDuration = Math.trunc(segment.duration() / nbSplit);
        
        // split by time, respecting the number of lines
        newSegments.push(new Segment());
        segment._words.forEach((word, i) => {
            let lastSegment = newSegments[newSegments.length - 1];
            // add first, in case too long, pop back and add into another segment
            lastSegment.pushBack(word);
            
            if (lastSegment.getWords().length > 1 && lastSegment.getLines(maxCharsPerLine).length > maxLinesPerSegment) {
                // there was already some words, and with this new one, number of lines is too large
                lastSegment.popBack();
                newSegments.push(new Segment());
                newSegments[newSegments.length - 1].pushBack(word);
            } else if (i !== (segment.getWords().length -1) && newSegments[newSegments.length - 1].duration() >= avgSegmentDuration) {
                // the average duration is exceeded
                newSegments.push(new Segment());
            }
        });
        return newSegments;      
    }
    
    //-------------- verifySegments() -------------------
    // debug method to check if everything seems correct
    // do not call it in production
    //---------------------------------------------------
    verifySegments() {
        // verify if word sequence is still the same
        let wordsFromJson = [];
        this._wordList.forEach(word => wordsFromJson.push(word.getGlyphs()));
        let wordStringFromJson = wordsFromJson.join(" ");
    
        let wordsFromSegments = [];
        this._segments.forEach(seg => wordsFromSegments.push(seg.wordString()));
        let wordStringFromSegments = wordsFromSegments.join(" ");
        
        if (wordStringFromJson !== wordStringFromSegments) {
            console.log("[E] word sequence is different");
            console.log("wordStringFromJson size=" + wordStringFromJson.length);
            console.log("wordStringFromSegments size=" + wordStringFromSegments.length);
        } else {
            // console.log("[OK] same word sequence");
        }

        // Verify timestamps
        this._segments.forEach((segment, i) => {
            if (segment.end() <= segment.begin()) {
                console.log(`[E] ill formed timestamps for segment id=${i} begin=${segment.begin()} end=${segment.end()}`);
            }
            if (i < this._segments.length -1) {
                let nextSegment = this._segments[i+1];
                if (nextSegment.begin() <  segment.end()) {
                    console.log(`[E] ill formed timestamps for segments id=${i}/${i+1} end of current=${segment.end()} begin of next=${nextSegment.begin()}`);
                }
            }
        });
    }
    
    //----------- computeOScribeJsonObj() ---------------
    // create output json object 
    //---------------------------------------------------
    computeOScribeJsonObj(jsonObj) {
  
        let newJsonObject = {
            schemaVersion: "2.0",
            monologues: []
        };
    
        this._segments.forEach((seg, idx) => {
            
            let oneLine = {
                speaker: {
                    id: "N/A"
                },
                start: seg._words[0]._begin / 1000,
                end: seg._words[seg._words.length - 1]._end / 1000,
                terms: []
            };
            
            seg._words.forEach((word, idx) => {
                let foundToken = word._tokens;
                
                foundToken.forEach((token, idx) => {
                    let tokenStartTime = token["startTime"];
                    let tokenEndTime = token["endTime"];
                    // Remove the trailing 's'
                    let beginText = tokenStartTime.substring(0, tokenStartTime.length - 1);
                    let endText = tokenEndTime.substring(0, tokenEndTime.length - 1);
                    // convert duration in ms
                    let begin = parseFloat(beginText);
                    let end = parseFloat(endText);
        
                    let term = {
                        start:begin,
                        end: end,
                        text: token.token,
                        type: token.type, 
                        confidence: parseFloat(token.confidence)
                    };
                    
                    oneLine.terms.push(term);
                });
            });           
            newJsonObject.monologues.push(oneLine);
        });  
        return newJsonObject;
    }
}

//-------------------- convert_oci_json(jsonObj) -----------------------------
// Adapt oci json format to OScribe
// - field names adaptation
// - create segments
//---------------------------------------------------------------------------
let convertOciJson = (jsonObj) => {

    let ocijson2oscribe = new OCIJson2OScribe(prmValues);
    
    // load json and create array of words
    ocijson2oscribe.loadTranscription(jsonObj);
        
    // create segments
    ocijson2oscribe.createSegments();
    
    // only for debug !!
    if (DO_VERIFY) {
        //   - verify if the word sequence is the same between json and srt
        //   - verify timestamps
        ocijson2oscribe.verifySegments();
    }
    
    // compute formatted json obj
    let newJsonObj = ocijson2oscribe.computeOScribeJsonObj(jsonObj);

    return newJsonObj;
};

let loadJson = (ociJsonFile) => {
    let jsonObj = JSON.parse(fs.readFileSync(ociJsonFile, 'utf8'));
    return convertOciJson(jsonObj);
};
  
let writeNewJSON = (newFile, newJsonObject) => {
    let fileHandle = fs.openSync(newFile, 'w');
    fs.writeFileSync(fileHandle, `${JSON.stringify(newJsonObject, null, 2)}\n`, {flag: 'a+'});
    fs.close(fileHandle);
};
  
// Main starts here
let main = (args) => {
 
   let jsonFile = null;
   let newJson = null;
   
   for (let i=0; i<args.length; i++) {
     // console.log("arg #%d: %s", i, args[i]);
     switch (args[i]) {
       case INPUT_PROMPT:
         jsonFile = args[i + 1];
         break;
       case OUTPUT_PROMPT:  
         newJson = args[i + 1];
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
       case HELP_PROMPT4:
         console.log(`Parameters of ${path.basename(__filename)} are:`);
         console.log(`${INPUT_PROMPT} inputFile.json (mandatory parameter)`);
         console.log(`${OUTPUT_PROMPT} outputFile.json (mandatory parameter)`);
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
   
   if (newJson === null) {
     throw new Error("No -o parameter...");
   }
   
   if (VERBOSE) {
     console.log(`Moving on. Converting ${jsonFile} into ${newJson}`);
   }

   let computed = loadJson(jsonFile);
   writeNewJSON(newJson, computed);
 
   console.log("Conversion Completed");
};
 
// Let's go!
main(process.argv);
 