Borg Architecture
=================

This document describes main Application Architecture and resources location.

Templates
---------

Application uses JSF technology for templating and Facelets for composition.

All templates are stored in [/src/main/webapp](../src/main/webapp) directory with suffix .xhtml.
This directory is main web app folder including templates and all static resources accessible from root of application (see next chapter for static resources).

Main template is [template.xhtml](../src/main/webapp/layout/template.xhtml) which defines HTML header, page header and footer.
Each page defines only page body e.g. [home.xhtml](../src/main/webapp/home.xhtml)

Directories in [/src/main/webapp](../src/main/webapp) has following meaning:

* ["root"](../src/main/webapp/) - Root folder for pages like root page (home.xhtml) or logout.xhtml
* [common](../src/main/webapp/common) - Common templates which can be included from any page
* [error](../src/main/webapp/error) - Error pages
* [layout](../src/main/webapp/layout) - Main layout templates
* [view](../src/main/webapp/view) - Blog Post
* [manage](../src/main/webapp/manage) - Application administration
* [reader](../src/main/webapp/reader) - Mobile reader


Static resources - JS/CSS
-------------------------

All static resources are located in [/src/main/webapp/resources](../src/main/webapp/resources) directory.

### Application resources

* [js/app.js](../src/main/webapp/resources/js/app.js) - Main App Javascript
* [js/md5.js](../src/main/webapp/resources/js/md5.js) - MD5 Utility
* [css/screen.css](../src/main/webapp/resources/css/screen.css) - Main App CSS
* [css/print.css](../src/main/webapp/resources/css/print.css) - App CSS used for print layout



### Third Party libraries

* jQuery - jQuery Library - referenced directly in [template.xhtml](../src/main/webapp/layout/template.xhtml)
* [bootstrap](../src/main/webapp/resources/bootstrap) - [Twitter Bootstrap](http://twitter.github.com/bootstrap/)
* [bbq](../src/main/webapp/resources/bbq) - [jQuery BBQ Plugin](http://benalman.com/projects/jquery-bbq-plugin/)
* [datatables](../src/main/webapp/resources/datatables) - [jQuery DataTables Plugin](https://datatables.net/)
* [animate-enhanced](../src/main/webapp/resources/animate-enhanced) - [jQuery Animate Enhanced Plugin](http://github.com/benbarnett/jQuery-Animate-Enhanced)
* AddThis - AddThis library - referenced directly in [template.xhtml](../src/main/webapp/layout/template.xhtml)




