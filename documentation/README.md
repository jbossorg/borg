Borg Documentation
==================

The Borg application is Blog post aggregator and archiver.

### Application features

1. Parsing remote feeds supporting [ATOM](http://en.wikipedia.org/wiki/Atom_(standard)) and [RSS](http://en.wikipedia.org/wiki/RSS) standards.
2. Archiving posts to rational DB. By default Mysql database but can be any JPA compliant rational database.
3. Using [Distributed Contribution Platform](https://github.com/jbossorg/dcp-api) REST API as a back-end identified as [Blog post content type](https://github.com/jbossorg/dcp-api/blob/master/documentation/rest-api/content/blogpost.md). Alternatively Borg's DB itself can be used but needs application customization.
4. Feeds categorization into groups
5. Feed administration based on group/feed administrator roles

### UI main features

1. Responsive UI using [Twitter Bootstrap](http://twitter.github.com/bootstrap/)
2. Mobile friendly app with offline support using HTML5 standard
3. Integration with [CAS single sign on server](http://www.jasig.org/cas) e.g. [jboss.org SSO server](https://sso.jboss.org).

### Architecture

Go to [Architecture](architecture.md) page.

Development
-----------

Go to [Borg Development Info](development.md) page.

