### Universal Code Generator ###
Universal is an extensible client-server Java tool for generating mathematical model code from a variety of input formats to one of several output formats. The input formats, for example, a comma delimited flat-files are read by Universal and converted into an Systems Biology Markup (SBML) tree in memory on the server. From the SBML tree, model code is generated in the C-programming language (GSL, SUNDIALS or an Octave C-module) or in Matlab M-code, Octave M-code or Scilab. Additionally, because it is extensible, a number of plugin modules have been developed, for example, SBML to GraphViz dot format for network visualisation. Universal is broken down into a Graphical User Interface (GUI) and a code generation engine (CGE). The GUI is written in objective-C/Cocoa while the CGE is written in the Java programming language.

**Interested?** Check out the code in the source tab, or go to the UNIVERSAL project page on Varnerlab.org (http://www.varnerlab.org/codeengine/).

### Requirements: ###
The UNIVERSAL GUI will run on Mac OSX 10.6 and above. The CGE will run on OS X or any Linux/Unix distribution. UNIVERSAL depends upon libSBML v4.20 or above (libSMBL can be downloaded from http://sbml.org/Software/libSBML).  Check out the installation instructions on the downloads tab.

### How do I install UNIVERSAL? ###
Simple! Just download the zip (or pkg) file from the downloads section. Everything is included (except libSMBL).

### Funding: ###
We acknowledge the generous financial support of the Office of Naval Research #N000140610293 for the development of Universal.