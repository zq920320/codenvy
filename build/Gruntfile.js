/*global module:true */

module.exports = function( grunt ) {
  'use strict';
  //
  // Grunt configuration:
  //
  // https://github.com/cowboy/grunt/blob/master/docs/getting_started.md
  //
    grunt.initConfig({
    verbosity: {
        hidden: {
          tasks: ['copy']
        }
    },

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
                    '../app/site/scripts/*.js',
                    '../app/site/scripts/models/**/*.js',
                    '../app/site/scripts/views/**/*.js'
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

                {expand: true, cwd: '../app/site/scripts/', src: ['**'], dest: '../target/dist/gh/site/scripts/'},

                // styles

                {expand: true, cwd: '../app/site/styles/', src : '*.css', dest: '../target/dist/gh/site/styles/'},

                // images

                {expand: true, cwd: '../app/site/images/', src: ['**'], dest: '../target/dist/gh/site/images/'},

                // fonts

                {expand: true, cwd: '../app/site/fonts/', src: ['**'], dest: '../target/dist/gh/site/fonts/'},

                // pages

                {expand: true, cwd: '../app/_site/', src: ['*.html','**/*.html','*.xml'], dest: '../target/dist/gh/'},

                // templates

                {expand: true, cwd: '../app/site/templates/', src: ['*.html'], dest: '../target/dist/gh/site/templates/'},
                
                // robots.txt

                {expand: true, cwd: '../app/_site/', src: ['*.txt'], dest: '../target/dist/gh/'}
            ]
        },

        stage: {
            files: [
                // scripts

                {expand: true, cwd: '../app/site/scripts/', src: ['**'], dest: '../target/dist/stage/site/scripts/'},

                // styles

                {expand: true, cwd: '../app/site/styles/', src : '*.css', dest: '../target/dist/stage/site/styles/'},

                // images

                {expand: true, cwd: '../app/site/images/', src: ['**'], dest: '../target/dist/stage/site/images/'},

                // fonts

                {expand: true, cwd: '../app/site/fonts/', src: ['**'], dest: '../target/dist/stage/site/fonts/'},

                // pages

                {expand: true, cwd: '../app/_site/', src: ['*.html','**/*.html','*.xml'], dest: '../target/dist/stage/'},

                // templates

                {expand: true, cwd: '../app/site/templates/', src: ['*.html'], dest: '../target/dist/stage/site/templates/'},
                
                // robots.txt

                {expand: true, cwd: '../app/_site/', src: ['*.txt'], dest: '../target/dist/stage/'}

            ]
        },

        prod: {
            files: [
                // scripts

                {
                    expand: true,
                    cwd: '../dist/site/scripts/',
                    src: ['vendor/modernizr*.js','*.amd-app.js'],
                    dest: '../target/dist/prod/site/scripts/'
                },

                // styles

                {
                    expand: true,
                    cwd: '../dist/site/styles/',
                    src : '*.css',
                    dest: '../target/dist/prod/site/styles/'
                },

                // images

                {
                    expand: true,
                    cwd: '../dist/site/images/',
                    src: ['**'],
                    dest: '../target/dist/prod/site/images/'
                },

                // fonts

                {
                    expand: true,
                    cwd: '../dist/site/fonts/',
                    src: ['**'],
                    dest: '../target/dist/prod/site/fonts/'
                },

                // pages

                {
                    expand: true,
                    cwd: '../dist/_site/',
                    src: ['*.html','**/*.html','*.xml'],
                    dest: '../target/dist/prod/'
                },
                
                // robots.txt

                {expand: true, cwd: '../app/_site/', src: ['*.txt'], dest: '../target/dist/prod/'}
            ]
        }
    },

    shell : {

        init : {
            command: 'rm -rf dist',
            options: {
                stdout: false,
                failOnError: true
            }
        },

        jekyll_stage_config : {
            command: 'cp <%= buildConfig.jekyllStageConfig %> <%= buildConfig.temp %>/_config.yml',
            options: {
                stdout: false,
                failOnError: true
            }
        },

        jekyll_gh_config : {
            command: 'cp <%= buildConfig.jekyllGHConfig %> <%= buildConfig.temp %>/_config.yml',
            options: {
                stdout: false,
                failOnError: true
            }
        },

        jekyll_prod_config : {
            command: 'cp <%= buildConfig.jekyllProdConfig %> <%= buildConfig.temp %>/_config.yml',
            options: {
                stdout: false,
                failOnError: true
            }
        },

        jekyll : {
            command: 'jekyll',
            options: {
                stdout: false,
                failOnError: true,
                execOptions: {
                    cwd: '<%= buildConfig.temp %>'
                }
            }
        },

        yeoman : {
            command: 'yeoman build',
            options: {
                stdout: false,
                failOnError: true,
                execOptions: {
                    cwd: '../',
                    maxBuffer: 1024000
                }
            }
        },

    remove_css : {
    command: 'find ../app/site/styles -name "*.css" -print0 | xargs -0 rm',
    options: {
                stdout: false,
                failOnError: true,
                execOptions: {
                    cwd: './'
                }
            }
},
        clean_dist : {
            command: 'rm -rf ../dist && rm -rf ../temp && rm -rf <%= buildConfig.temp %>',
            options: {
                stdout: false,
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
  grunt.loadNpmTasks('grunt-verbosity');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.registerTask('build',
        [
            // hiding copy output
//            'verbosity:hidden',
            //check code quality before building
            'jshint:test', 'jshint:app',

            // get rid of the previous dist jazz
            'shell:init',

            // copy all application files to the temporary folder
            'copy:temp',


            // ------------------ Github pages STAGING

            // copy staging _config.yml to the temporary folder
            //'shell:jekyll_gh_config',

            // run jekyll build for github staging
            //'shell:jekyll',

            // run yeoman build on top of staging Jekyll build
            //'shell:yeoman',

            // copy staging build output
            //'copy:gh_stage',


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
            'shell:clean_dist',
            // remove /styles/*.css
            'shell:remove_css'
        ]
    );
};
