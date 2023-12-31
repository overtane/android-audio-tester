# Audio Compatibility Tester

An app for testing streaming and mixing low latency PCM audio in Android,

This is initial proof-of-concept using AudioTrack and AudioRecord. The final goal is to use native level [AAUDIO interface](https://developer.android.com/ndk/guides/audio/aaudio/aaudio) and [Oboe library](https://github.com/google/oboe)

## Build

Download the source code and use Android Studio for building. There are two projects
- The main app [AudioTester](AudioTester/README.md)
- The companion app [SoundBrowser](SoundBrowser/README.md)

## Documentation

- [Instructions](AudioTester/docs/instructions.md)
- [Design document](AudioTester/docs/design.md)

## License and copyrights

MIT [LICENSE](LICENSE)

Copyright © 2023 Olli Vertanen

Duck icon copyright 2023 [icon-icons.com](https://icon-icons.com) under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

