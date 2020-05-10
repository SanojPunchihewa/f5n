[![Build Status](https://travis-ci.com/SanojPunchihewa/f5n.svg?branch=master)](https://travis-ci.com/SanojPunchihewa/f5n)
## Genopo a.k.a. F5N is a nanopore sequencing analysis toolkit for Android smartphones

### Overview
Genopo can run following analysis tools on mobile devices [more details](https://github.com/SanojPunchihewa/f5n/blob/master/docs/Supplementary_materials.pdf)

 1. [minimap2](https://github.com/lh3/minimap2)
 2. [samtools](https://github.com/samtools/samtools)
 3. [f5c](https://github.com/hasindu2008/f5c)
 4. [nanopolish](https://github.com/jts/nanopolish)
 5. [bcftools](https://github.com/samtools/bcftools)
 6. [artic_c](https://github.com/hiruna72/artic_c) 
 7. [bioawk](https://github.com/lh3/bioawk)
 
In particular, Genopo has buit in pipelines for
 1. [Methylation Calling](https://hasindu2008.github.io/f5c/docs/commands#calling-methylation)
 2. [Event Alignment](https://hasindu2008.github.io/f5c/docs/commands#aligning-events)
 2. [Variant Calling](https://nanopolish.readthedocs.io/en/latest/manual.html#variants)
 3. [Consensus Building](http://samtools.github.io/bcftools/bcftools.html#consensus)
 4. nCoV-2019 novel coronavirus bioinformatics protocol [(Artic pipeline)](https://artic.network/ncov-2019)


### Real-time sequence analysis (in alpha phase)
Real-time methylation calling on nanopore sequencing batch datasets is experimented.
More details can be found at [here](https://github.com/AnjanaSenanayake/f5n_server) 

![Diagram](https://github.com/hiruna72/f5n/blob/master/server_mobile_connection.png)

### Installation
Genopo application can be obtained from:

[Google Play](https://play.google.com/store/apps/details?id=com.mobilegenomics.f5n)

Additionally you can get development builds from Github releases
Chose `ARCHITECTURE` to be `armeabi-v7a` or `arm64-v8a`. To find what is the `ARCHITECTURE` of your phone the easiest way is to google it. Once you download the apk copy it to your phone and install.
```sh
VERSION=0.0.4
ARCHITECTURE=armeabi-v7a
wget "https://github.com/SanojPunchihewa/f5n/releases/download/$VERSION/mobilegenomics-f5n-v$VERSION-$ARCHITECTURE-release.apk"
```

Signature keys of builds are different. Before you switch the installation source, you will have to uninstall the existing Genopo application.

### Demo

Configure single/multiple tools             |  Help    
:-------------------------:|:-------------------------:
![](/gif/demo_gif.gif)  |  ![](/gif/help_gif.gif)

#### Connecting to F5N server, downloading, running and sending results

![](/gif/f5n-cluster-demo.gif)

### Datasets

You can use publicly available [datasets](https://github.com/nanopore-wgs-consortium/NA12878/blob/master/Genome.md)

### How to contribute
Please contribute to our work by testing,debugging and developing our product. A guide to how to contribute is available [here](https://github.com/MarcDiethelm/contributing/blob/master/README.md)
 
 ### Submit an issue
 Please submit your issues [here](https://github.com/SanojPunchihewa/f5n/issues). Make sure your issue is not an existing one.
 If a similar issue already exists comment or vote under the existing issue.
