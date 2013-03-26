/*global module:true */

module.exports = function( grunt ) {
  'use strict';
  //
  // Grunt configuration:
  //
  // https://github.com/cowboy/grunt/blob/master/docs/getting_started.md
  //
  grunt.initConfig({


    buildConfig : {
        temp : "./temp",
        jekyllStageConfig : "_config.stage.yml",
        jekyllProdConfig : "_config.prod.yml",
        jekyllGHConfig : "_config.gh.yml"
    },


    jshint: {
        app : {
            files: {
                src: [
                    'Gruntfile.js',
                    '../app/scripts/*.js',
                    '../app/scripts/models/**/*.js',
                    '../app/scripts/views/**/*.js'
                ]
            },
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
                unused: true,

                globals: {
                    jQuery: true,
                    define: true,
                    require: true,
                    Modernizr: true,
                    escape: true,
                    tenantName: true //fix this in models/account.js
                }
            }
        },

        test: {
          options: {
            unused : false,
            expr : true,
            es5 : true,
            globals: {
                jQuery: true,
                define: true,
                require: true,
                Modernizr: true,
                describe: true,
                it: true,
                expect: true,
                sinon: true,
                afterEach: true,
                beforeEach: true
            }
          },
          files: {
            src: ['../test/spec/**/*.js']
          }
        }
    },

    copy: {

        temp: {
            files: [{expand: true, cwd: '../app/', src: ['**'], dest: '<%= buildConfig.temp %>'}]
        },

        gh_stage: {
            files: [
                // scripts

                {expand: true, cwd: '../app/scripts/', src: ['**'], dest: 'dist/gh/scripts/'},

                // styles

                {expand: true, cwd: '../app/_site/styles/', src : '*.css', dest: 'dist/gh/styles/'},

                // images

                {expand: true, cwd: '../app/images/', src: ['**'], dest: 'dist/gh/images/'},

                // fonts

                {expand: true, cwd: '../app/fonts/', src: ['**'], dest: 'dist/gh/fonts/'},

                // pages

                {expand: true, cwd: '../app/_site/', src: ['*.html','**/*.html'], dest: 'dist/gh/'},

                // templates

                {expand: true, cwd: '../app/templates/', src: ['*.html'], dest: 'dist/gh/templates/'},
                
                // robots.txt

                {expand: true, cwd: '../app/_site/', src: ['*.txt'], dest: 'dist/gh/'}
            ]
        },

        stage: {
            files: [
                // scripts

                {expand: true, cwd: '../app/scripts/', src: ['**'], dest: 'dist/stage/scripts/'},

                // styles

                {expand: true, cwd: '../app/_site/styles/', src : '*.css', dest: 'dist/stage/styles/'},

                // images

                {expand: true, cwd: '../app/images/', src: ['**'], dest: 'dist/stage/images/'},

                // fonts

                {expand: true, cwd: '../app/fonts/', src: ['**'], dest: 'dist/stage/fonts/'},

                // pages

                {expand: true, cwd: '../app/_site/', src: ['*.html','**/*.html'], dest: 'dist/stage/'},

                // templates

                {expand: true, cwd: '../app/templates/', src: ['*.html'], dest: 'dist/stage/templates/'},
                
                // robots.txt

                {expand: true, cwd: '../app/_site/', src: ['*.txt'], dest: 'dist/stage/'}
            ]
        },

        prod: {
            files: [
                // scripts

                {
                    expand: true,
                    cwd: '../dist/scripts/',
                    src: ['vendor/modernizr*.js','*.amd-app.js'],
                    dest: 'dist/prod/scripts/'
                },

                // styles

                {
                    expand: true,
                    cwd: '../dist/styles/',
                    src : '*.css',
                    dest: 'dist/prod/styles/'
                },

                // images

                {
                    expand: true,
                    cwd: '../dist/images/',
                    src: ['**'],
                    dest: 'dist/prod/images/'
                },

                // fonts

                {
                    expand: true,
                    cwd: '../dist/fonts/',
                    src: ['**'],
                    dest: 'dist/prod/fonts/'
                },

                // pages

                {
                    expand: true,
                    cwd: '../dist/_site/',
                    src: ['*.html','**/*.html'],
                    dest: 'dist/prod/'
                },
                
                // robots.txt

                {expand: true, cwd: '../app/_site/', src: ['*.txt'], dest: 'dist/prod/'}
            ]
        }
    },

    shell : {

        init : {
            command: 'rm -rf dist',
            options: {
                stdout: true,
                failOnError: true
            }
        },

        jekyll_stage_config : {
            command: 'cp <%= buildConfig.jekyllStageConfig %> <%= buildConfig.temp %>/_config.yml',
            options: {
                stdout: true,
                failOnError: true
            }
        },

        jekyll_gh_config : {
            command: 'cp <%= buildConfig.jekyllGHConfig %> <%= buildConfig.temp %>/_config.yml',
            options: {
                stdout: true,
                failOnError: true
            }
        },

        jekyll_prod_config : {
            command: 'cp <%= buildConfig.jekyllProdConfig %> <%= buildConfig.temp %>/_config.yml',
            options: {
                stdout: true,
                failOnError: true
            }
        },

        jekyll : {
            command: 'jekyll',
            options: {
                stdout: true,
                failOnError: true,
                execOptions: {
                    cwd: '<%= buildConfig.temp %>'
                }
            }
        },

        yeoman : {
            command: 'yeoman build',
            options: {
                stdout: true,
                failOnError: true,
                execOptions: {
                    cwd: '../'
                }
            }
        },

        clean_dist : {
            command: 'rm -rf ../dist && rm -rf ../temp && rm -rf <%= buildConfig.temp %>',
            options: {
                stdout: true,
                failOnError: true,
                execOptions: {
                    cwd: './'
                }
            }
        }
    }

  });

  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-shell');
  grunt.loadNpmTasks('grunt-contrib-jshint');

  grunt.registerTask('build',
        [
            //check code quality before building
            'jshint:test', 'jshint:app',

            // get rid of the previous dist jazz
            'shell:init',

            // copy all application files to the temporary folder
            'copy:temp',


            // ------------------ Github pages STAGING

            // copy staging _config.yml to the temporary folder
            'shell:jekyll_gh_config',

            // run jekyll build for github staging
            'shell:jekyll',

            // run yeoman build on top of staging Jekyll build
            'shell:yeoman',

            // copy staging build output
            'copy:gh_stage',


            // ------------------ STAGING

            // copy staging _config.yml to the temporary folder
            'shell:jekyll_stage_config',

            // run jekyll build for staging
            'shell:jekyll',

            // run yeoman build on top of staging Jekyll build
            'shell:yeoman',

            // copy staging build output
            'copy:stage',


            // ------------------ PRODUCTION

            // copy staging _config.yml to the temporary folder
            'shell:jekyll_prod_config',

            // run jekyll build for production
            'shell:jekyll',

            // run yeoman build on top of production Jekyll build
            'shell:yeoman',

            // copy production goodness
            'copy:prod',

            // clean up
            'shell:clean_dist'
        ]
    );
};
