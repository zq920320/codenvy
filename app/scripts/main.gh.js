/*
    This is a copy of main.js with baseUrl adjusted for github
*/

require.config({
    baseUrl : "http://codenvy.github.com/odyssey/scripts/",
    shim: {
        'underscore' : {exports: '_' },
        'backbone' : {exports: 'Backbone', deps: ['underscore']},
        'skrollr' : {exports: 'skrollr' },
        'validation' : {deps:["jquery"]},
        'json' : {exports: 'JSON'},
        'handlebars' :  {exports: 'Handlebars'},
        'marketo': {exports: 'Munchkin', deps:['jquery']}
    },

    paths: {
        marketo: '//munchkin.marketo.net/munchkin',
        jquery: 'vendor/jquery.min',
        validation: 'vendor/jquery.validate',
        underscore: 'vendor/underscore',
        backbone: 'vendor/backbone',
        skrollr: 'vendor/skrollr',
        text : 'vendor/text',
        json : 'vendor/json2',
        handlebars : 'vendor/handlebars',
        templates: 'http://codenvy.github.com/odyssey/templates'
    }

});

require(['app'], function(Application) {
    Application.run();
});
