/*
  This grunt file config lives alongside Gruntfile.js used by yeoman
  Any tasks confgiured here can be run with
    grunt <name-of-the-task>
*/

module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    lint: {
      all:[
        'grunt.js',
        'Gruntfile.js',
        'app/scripts/*.js',
        'app/scripts/models/**/*.js',
        'app/scripts/views/**/*.js',
        'spec/**/*.js'
      ]
    },
    jshint: {
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
          Modernizr: true,
          module: true
      }
    }
  });

  // Default task.
  grunt.registerTask('default', 'lint sample');
};
