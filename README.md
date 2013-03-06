Dependencies
=======

[package.json](https://github.com/codenvy/odyssey/blob/master/package.json) contains detailed information on both runtime and development dependencies.

To run odyssey locally and to be able to build project for deployment you will need:

- [Yeoman](http://yeoman.io/)
- [Grunt 0.4+](http://gruntjs.com/getting-started)
- [Jekyll](http://jekyllrb.com/)


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

How to build
=======

Security first - make sure you are wearing a helmet.

```
cd build
grunt build
```

Build populates dist/ with 3 sets of bundles : stage (for staging), prod (for production) and gh (for github hosted version of the site). The sets contain all the static content needed to run the site.
