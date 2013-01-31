/*
    Require setup + entry point for the application
*/

require.config({

    shim: {
        'underscore' : {exports: '_' },
        'backbone' : {exports: 'Backbone', deps: ['underscore']},
        'skrollr' : {exports: 'skrollr' },
        'modernizr' : {exports: 'Modernizr' },
        'validation' : {deps:["jquery"]}
    },

    paths: {
        jquery: 'vendor/jquery.min',
        validation: 'vendor/jquery.validate',
        underscore: 'vendor/underscore',
        backbone: 'vendor/backbone',
        modernizr: 'vendor/modernizr.min',
        skrollr: 'vendor/skrollr',
        text : 'vendor/text',
        templates: '../templates'
    }

});

require(['app'], function(Application) {
    Application.run();
});
