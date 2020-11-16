# diagrams.net-experiment

A [diagrams.net](https://www.diagrams.net/) hello world.

diagrams.net:

* Allows you to use a few different options to store the diagrams: locally as XML files, Google drive, etc.
* There's a nice integration with Google Drive - you just click the diagram file and it opens the editor.
* When storing the diagrams locally, you can use diarams.net desktop app.
* The diagrams.net desktop app has a CLI that allows you to render the diagrams - see `build.sh` (the current version only works on Mac OS and requires the diagrams.net app to be installed)
    * Batch mode only exports the first page of each diagram
    * Batch mode doesn't allow you to specify the file name mapping
    * Non-batch mode allows you to export each page individually and specify custom target file names
