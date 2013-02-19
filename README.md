Dependencies
=======

[package.json](https://github.com/codenvy/odyssey/blob/master/package.json) contains detailed information on both runtime and development dependencies.


In addition to packages listed in package.json, [Jekyll](http://jekyllrb.com/) is used to organize pages.

Running locally
=======

To run the site on your machine

```
yeoman server
cd ./app
jekyll --auto --server
```

The site will be available at localhost:3501/_site/

Testing
========

Before running tests, make sure you link app/scripts directory symbolically in the test directory

```
cd test
ln -s ../app/scripts scripts
```

To run the test suite

```
yeoman test
```

How to build (Proposal)
=======

To build:

```
grunt build
```

Build populates dist/ with two sets of builds : stage (for staging) and prod (for production). Both sets contain all the static content needed to run the site. Production set can later be deployed to S3.

Templates currently come from app/_site/*.html. This should be replaced with a set of Jekyll includes that can then be integrated into jsp pages (header-body-footer style).
