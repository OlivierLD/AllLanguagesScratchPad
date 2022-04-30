# Some video tests
- Videos
- Subtitles
- `videojs`
- Etc...

Free videos: <https://www.pexels.com/>

YouTube Tutorial: <https://www.youtube.com/watch?v=ntaNl6kaoHk>

To checkout:
- Get started <https://videojs.com/getting-started/>
- NPM: <https://www.npmjs.com/package/video.js?activeTab=readme>
- For YouTube
  - <https://github.com/videojs/videojs-youtube>
  - <https://www.npmjs.com/package/videojs-youtube>


# Play YouTube clips
You need to install a couple of NodeJS modules.  
From the `video` folder, run 
```
$ npm install
```
Then, you can
```
$ npm start
```
And reach [http://localhost:8080/watch_three/subtitles.html](http://localhost:8080/watch_three/subtitles.html) from your browser.  
It also implements subtitles on the YouTube video.

> _Note_: The implementation/example above (`watch_three/subtitles.html`) does not require any server-side programming.  
> Look in the code of the page, it only refers to `video.js` (through it minimized version `video.min.js`) and `Youtube.js` (through its minimized 
> version `Youtube.min.js`) from the client-side (the web page).  
> It actually does not even require a server (`npm start`), you can statically load the page in your browser, as long as the pathes for `video.js` and `Youtube.js` are correct.
