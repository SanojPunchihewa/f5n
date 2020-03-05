[![Build Status](https://travis-ci.com/SanojPunchihewa/f5n.svg?branch=master)](https://travis-ci.com/SanojPunchihewa/f5n)
## f5n

Motivated from [f5p](https://github.com/hasindu2008/f5p) - we build a network of mobiles to process [ONT](https://nanoporetech.com/) data. The work is under construction! 

### Overview
f5n can run the following on ARM devices [more details](https://hasindu2008.github.io/linux-tools-on-phone/)

 1. [minimap2](https://github.com/lh3/minimap2) sequence alignment
 2. [samtools](https://github.com/samtools/samtools) sort
 3. [samtools](https://github.com/samtools/samtools) index
 4. [f5c](https://github.com/hasindu2008/f5c) index
 5. [f5c](https://github.com/hasindu2008/f5c) call-methylation
 6. [f5c](https://github.com/hasindu2008/f5c) event alignment

We build a standalone application and a cluster of nodes where a master orchestrates the other nodes. 
In this way ONT data can be corrected for errors in the field, on the fly!

### Flowchart
![Diagram](https://github.com/hiruna72/f5n/blob/master/flowchart.png)

### Real-time sequence analysis (in alpha phase)
The source code for the Server can be found [here](https://github.com/AnjanaSenanayake/f5n_server) 

![Diagram](https://github.com/hiruna72/f5n/blob/master/server_mobile_connection.png)

### Installation
f5n application can be obtained from:

[Google Play](https://play.google.com/store/apps/details?id=com.mobilegenomics.f5n)

Additionally you can get development builds from Github releases
Chose `ARCHITECTURE` to be `armeabi-v7a` or `arm64-v8a`. To find what is the `ARCHITECTURE` of your phone the easiest way is to google it. Once you download the apk copy it to your phone and install.
```sh
VERSION=0.0.4
ARCHITECTURE=armeabi-v7a
wget "https://github.com/SanojPunchihewa/f5n/releases/download/$VERSION/mobilegenomics-f5n-v$VERSION-$ARCHITECTURE-release.apk"
```

Signature keys of builds are different. Before you switch the installation source, you will have to uninstall the f5n application.

### Demo

Configure single/multiple tools             |  Help
:-------------------------:|:-------------------------:
![](/gif/demo_gif.gif)  |  ![](/gif/help_gif.gif)

### Datasets

You can use publicly available [datasets](https://github.com/nanopore-wgs-consortium/NA12878/blob/master/Genome.md)

### How to contribute
Please contribute to our work by testing,debugging and developing our product. A guide to how to contribute is available [here](https://github.com/MarcDiethelm/contributing/blob/master/README.md)
 
 ### Submit an issue
 Please submit your issues [here](https://github.com/SanojPunchihewa/f5n/issues). Make sure your issue is not an existing one.
 If a similar issue already exists comment or vote under the existing issue.
