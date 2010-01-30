!!! README.txt
Note: This file is written in [Wiki markup|wp:Wiki markup]. Whilst you can read it as a text file, it is best viewed directly in ProcessLab if you have it running; a link to it can be found in the about page.

!!! Installation
ProcessLab is packaged as a [WAR file |wp:WAR file]. It can be installed, for example under [Apache Tomcat |wp:Apache Tomcat]. By default it should work out-of-the-box but the package should be properly configured to be used in production.

!! Default directory layout
{{{
Tomcat
|- bin
   |- ...
   |- webapps
      |- ...
      |- ProcessLab
         |- ... 
         |- WEB-INF
            |- ...
            |- pages
               |- ...
            |- work
               |- ...
}}}
Tomcat installs web applications like ProcessLab into the webapps directory under where you have Tomcat installed. The installation is standard in most respects but there are two directories that are worth noting. The 'pages' directory and the 'work' directory.

The pages directory contains all the pages that are shipped with ProcessLab, for example a copy of this page can be found here. Used out of the box, ProcessLab will store and changes to the pages in this directory. If ProcessLab is uninstalled the directory is deleted and changes will be lost. When ProcessLab is installed properly, these pages are copied to another location so that changes are not lost.

The work directory contains files the ProcessLab needs to run like text indexes etc. Files in this directory can be deleted without loss of work. In other words you don't need to worry about files stored in ../work - they will take care of themselves.
