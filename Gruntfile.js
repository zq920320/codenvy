/*global module:true */

module.exports = function( grunt ) {
  'use strict';
  //
  // Grunt configuration:
  //
  // https://github.com/cowboy/grunt/blob/master/docs/getting_started.md
  //
  grunt.initConfig({

    // Project configuration
    // ---------------------

    // specify an alternate install location for Bower
    bower: {
      dir: 'app/components'
    },

    // Coffee to JS compilation
    coffee: {
      compile: {
        files: {
          'temp/scripts/*.js': 'app/site/scripts/**/*.coffee'
        },
        options: {
          basePath: 'app/site/scripts'
        }
      }
    },

    // compile .scss/.sass to .css using Compass
    compass: {
      dist: {
        // http://compass-style.org/help/tutorials/configuration-reference/#configuration-properties
        options: {
          css_dir: 'app/site/styles',
          sass_dir: 'app/site/styles',
          images_dir: 'app/site/images',
          javascripts_dir: 'temp/scripts',
          force: true
        }
      }
    },

    // generate application cache manifest
    manifest:{
      dest: '',
      port: 0
    },

    // headless testing through PhantomJS
    mocha: {
      all: ['test/index.html']
    },

    // default watch configuration
    watch: {
      coffee: {
        files: 'app/site/scripts/**/*.coffee',
        tasks: 'coffee reload'
      },
      compass: {
        files: [
          'app/site/styles/**/*.{scss,sass}'
        ],
        tasks: 'compass reload'
      },
      reload: {
        files: [
          'app/*.html',
          'app/_layouts/*.html',
          'app/_site/styles/**/*.css',
          'app/_site/scripts/**/*.js',
          'app/_site/images/**/*',
          'test/spec/**/*'
        ],
        tasks: 'reload'
      }
    },

    // default lint configuration, change this to match your setup:
    // https://github.com/cowboy/grunt/blob/master/docs/task_lint.md#lint-built-in-task
    // lint: {

    // },

    // specifying JSHint options and globals
    // https://github.com/cowboy/grunt/blob/master/docs/task_lint.md#specifying-jshint-options-and-globals
    lint: {
      files: [
        'Gruntfile.js',
        'app/site/scripts/*.js',
        'app/site/scripts/models/**/*.js',
        'app/site/scripts/views/**/*.js',
        'spec/**/*.js'
      ],
      options: {
        options: {
            curly: true,
            eqeqeq: true,
            immed: true,
            latedef: true,
            newcap: true,
            noarg: true,
            sub: true,
            undef: true,
            boss: true,
            eqnull: true,
            browser: true,
            unused: true
        },
        globals: {
            jQuery: true,
            define: true,
            require: true,
            Modernizr: true
        }
      }
    },

    // Build configuration
    // -------------------

    // the staging directory used during the process
    staging: 'temp',
    // final build output
    output: 'dist',

    mkdirs: {
      staging: 'app/'
    },

    // Below, all paths are relative to the staging directory, which is a copy
    // of the app/ directory. Any .gitignore, .ignore and .buildignore file
    // that might appear in the app/ tree are used to ignore these values
    // during the copy process.

    // concat css/**/*.css files, inline @import, output a single minified css
    css: {
      'site/styles/main.css': ['site/styles/**/*.css']
    },

    // renames JS/CSS to prepend a hash of their contents for easier
    // versioning
    rev: {
      js: 'site/scripts/*.js',
      css: 'site/styles/**/*.css',
      img: 'site/imagess/**'
    },

    // usemin handler should point to the file containing
    // the usemin blocks to be parsed
    'usemin-handler': {
      html: '_site/**/*.html' //'index.html'
    },

    // update references in HTML/CSS to revved files
    usemin: {
      html: ['**/*.html'],
      css: ['**/*.css']
    },

    // HTML minification
    html: {
      files: ['**/*.html']
    },

    // Optimizes JPGs and PNGs (with jpegtran & optipng)
    img: {
      dist: '<config:rev.img>'
    },

    // rjs configuration. You don't necessarily need to specify the typical
    // `path` configuration, the rjs task will parse these values from your
    // main module, using http://requirejs.org/docs/optimization.html#mainConfigFile
    //
    // name / out / mainConfig file should be used. You can let it blank if
    // you're using usemin-handler to parse rjs config from markup (default
    // setup)
    rjs: {
      // no minification, is done by the min task
      optimize: 'none',
      baseUrl: './site/scripts',
      wrap: true,
      name: 'main',
      mainFile:'_site/index.html'
      //mainConfigFile: 'scripts/main.js'
    },

    // While Yeoman handles concat/min when using
    // usemin blocks, you can still use them manually
    concat: {
      dist: ''
    },

    min: {
      dist: ''
    }
  });


  // Alias the `test` task to run the `mocha` task instead
  grunt.registerTask('test', 'server:test');

  //grunt.registerTask('test', 'server:phantom mocha');

};
