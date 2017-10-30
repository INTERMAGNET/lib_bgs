# Software project description

|  |  |
| ------------------------ | ------- |
| Project Name:            | lib_bgs |
| Main developer(s):       | Jane Exton, Simon Flower, Peter Stevenson  |
| Main user(s):            | Jane Exton, Simon Flower, Peter Stevenson, Chris Turbitt |
| Computer language(s):    | Java |
| Development environment: | Netbeans |
| Build Tool:              | Maven |
| Source Control:          | Git |
| BGS team:                | Geomagnetism |

A Java library containing classes that may be useful across projects.

This code is a direct descendent of lib_bgs previously in subversion:-

http://kwvmxsource.ad.nerc.ac.uk/svn/corporate/Geomag/GenericLibraries/lib_bgs/

Subversion history is preserved in Git.

All new lib_bgs code development should use this git project.

git clone https://kwvmxgit.ad.nerc.ac.uk/geomag_libraries/lib_bgs.git


## Description
General geomagnetism utilities. Any reusable code should be put in this library.


## How to install the compiled product

From the bottom level project directory give the command 'mvn deploy'. This will compile the code and then install it to the BGS Artifactory server, from where other projects can include it as a dependency. You can find the library under:

    libs-release-local  : uk/ac/bgs : geomag : lib_bgs
    libs-snapshot-local : uk/ac/bgs : geomag : lib_bgs
