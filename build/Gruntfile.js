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
        jekyllConfig : "_config.yml"
    },


    copy: {

        temp: {
            files: [{expand: true, cwd: '../app/', src: ['**'], dest: '<%= buildConfig.temp %>'}]
        },


        jekyll_config: {
            files : [{expand: true, cwd: './', src: ['<%= buildConfig.jekyllConfig %>'], dest: '<%= buildConfig.temp %>'}]
        },

        main: {
            files: [
                //{src: ['app/styles/main.css'], dest: 'dist/stage/styles/', flatten: true},

                /*
                    Staging
                    ---------------------------------------------------------------------------------
                */

                // scripts

                {expand: true, cwd: '../app/scripts/', src: ['**'], dest: 'dist/stage/scripts/'},

                // styles

                {expand: true, cwd: '../app/styles/', src : '*.css', dest: 'dist/stage/styles/'},

                // images

                {expand: true, cwd: '../app/images/', src: ['**'], dest: 'dist/stage/images/'},

                // fonts

                {expand: true, cwd: '../app/fonts/', src: ['**'], dest: 'dist/stage/fonts/'},

                // templates

                {expand: true, cwd: '../app/_site/', src: ['*.html'], dest: 'dist/stage/'},

                /*
                    Production
                    ---------------------------------------------------------------------------------
                */

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

                // templates

                {
                    expand: true,
                    cwd: '../dist/_site/',
                    src: ['*.html'],
                    dest: 'dist/prod/'
                }
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
        },
    }

  });

  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-shell');

  grunt.registerTask('build',
        [
            'shell:init',
            'copy:temp',
            'copy:jekyll_config',
            'shell:jekyll',
            'shell:yeoman',
            'copy:main',
            'shell:clean_dist'
        ]
    );
};
