var gulp = require('gulp'),
    minifyCSS = require('gulp-minify-css'), // CSS minifying
    watch = require('gulp-watch'),
    //imagemin = require('gulp-imagemin'), // Img minifying
    uglify = require('gulp-uglify'),    // JS minifying
    concat = require('gulp-concat'),    // Files merging
    rev = require('gulp-rev'),          // Files versioning
    compass = require('gulp-compass'),  // Compass to work with CSS
    rjs = require("gulp-requirejs"),    //A gulp interface to r.js
    connect = require('gulp-connect'),  // Webserver
    rename = require("gulp-rename"),    //Rename files
    useref = require('gulp-useref'),    //Removes <!-- build:js ... blocks from html
    replaceRevRef = require('gulp-rev-manifest-replace'), //Plugin to replace assets urls based on generated manifest file
    reverse = require('reversible'),
    rimraf = require('gulp-rimraf');                      // Remove files and folders
    //wait = require('gulp-wait'),
    //lr = require('tiny-lr'), // Минивебсервер для livereload
    //server = lr(),
    //livereload = require('gulp-livereload'), // Livereload for Gulp

var buildConfig = {
        jekyllStageConfig : "_config.stage.yml",
        jekyllProdConfig : "_config.prod.yml",
        jekyllGHConfig : "_config.gh.yml",
        jekyllEEConfig : "_config.enterprise.yml",
    };

var paths = {
        src: './app/',
        prod: './target/prod/',
        stage: './target/stage/',
        gh: './target/gh/',
        temp: 'target/temp/',
        dist: 'target/dist/',
        config: 'build/',
        site: 'app/_site/'
};

// This task creates local server
gulp.task('connect', ['gh'], function() {
  connect.server({
    root: paths.gh
  });
});

// --------------------------- Building Prod -----------------------------
//----------------
//----------
gulp.task('prod',['copy_src','prod_cfg','css','rjs','jekyll','myrev','replace','rmbuild','copy_prod'], function(){

})
// Copies src to temp folder
gulp.task('copy_src', function(){
  return gulp.src(paths.src + '**/*.*')
  .pipe(gulp.dest(paths.temp))
})

gulp.task('prod_cfg', function(){
  return gulp.src(paths.config + buildConfig.jekyllProdConfig)
  .pipe(rename('_config.yml'))
  .pipe(gulp.dest(paths.temp))
})

gulp.task('css', ['copy_src'], function() {
  return gulp.src(paths.temp+'site/styles/*.scss')
  .pipe(compass({
    //config_file: './compass-config.rb',
    css: paths.temp +'site/styles',
    sass: paths.temp +'site/styles'
  }))
  .pipe(minifyCSS())
  .pipe(gulp.dest(paths.prod + 'site/styles/'));
});

// Builds projects using require.js's optimizer + Minify files with UglifyJS
gulp.task('rjs',['copy_src'], function(){
      return  rjs({
            mainConfigFile: paths.temp +'site/scripts/main.js',
            //optimize: 'none', //hardcoded in requirejs plugin
            baseUrl: paths.temp + 'site/scripts',
            wrap: true,
            name: 'main',
            mainFile: paths.temp+'site/index.html',
            out: 'amd-main.js'
        })
      .pipe(gulp.src(paths.temp +'site/scripts/vendor/require.js'))
      .pipe(reverse({objectMode: true})) // requirejs should be at the begining
      .pipe(concat('amd-app.js'))
      .pipe(uglify())
      .pipe(gulp.dest(paths.prod + 'site/scripts'));
 });

gulp.task('jekyll',['copy_src','prod_cfg'], function () {
         console.log('Jekyll ......... ');
     return require('child_process')
        .spawn('jekyll', ['build'], {stdio: 'inherit', cwd: paths.temp});

});

// Replaces references with reved names
  gulp.task ('myrev', ['copy_src','prod_cfg','css','rjs','jekyll'], function(){
    return gulp.src([paths.prod + 'site/scripts/amd-app.js', paths.prod + 'site/styles/*.css'],{base:paths.prod})
    //.pipe(wait(1500))
    .pipe(rev())
    .pipe(gulp.dest(paths.prod))
    .pipe(rev.manifest())
    .pipe(gulp.dest(paths.prod));
  });

// Start replacing rev references
gulp.task('replace',['copy_src','prod_cfg','css','rjs','jekyll','myrev'], function(){

  var manifest = require(paths.prod+'rev-manifest.json')
  return gulp.src([paths.prod+'**/*.html'])
  .pipe(replaceRevRef({
    base: process.cwd()+'/target/prod',
    manifest: manifest,
    path: ''/*,
    cdnPrefix: 'http://absolute.path/cdn/'*/
  }))
  .pipe(gulp.dest(paths.prod));

});

// Removes <!-- build:js ... blocks from html for prod configeration
gulp.task('rmbuild', ['copy_src','prod_cfg','css','rjs','jekyll','myrev','replace'], function(){
  return gulp.src(paths.prod+'**/*.html')
  .pipe(useref())
  .pipe(gulp.dest(paths.prod));

});

gulp.task('copy_prod',['copy_src','prod_cfg','css','rjs','jekyll','myrev','replace','rmbuild'], function(){
  gulp.src([paths.prod+'/**/*.html', // all HTML
    paths.prod+'**/amd-app-*.js', // minified JS
    paths.prod+'**/*-*.css', // minified CSS
    paths.prod+'**/*.jpg',
    paths.prod+'**/*.png',
    paths.prod+'**/*.svg',
    paths.prod+'**/*.txt',
    paths.prod+'**/modernizr.custom.*.js'] // robots.txt
    )
  .pipe(gulp.dest(paths.dist+'prod'));
});

// Cleans gulp's folders
gulp.task('clean',function(){
  return gulp.src([paths.temp,paths.prod,paths.stage,paths.dist],{ read: false }) // much faster
    .pipe(rimraf());
})

// --------------------------- Building Stage -----------------------------
//----------------
//----------
gulp.task('stage',['copy_src','stage_cfg','css_stage','jekyll_stage','copy_stage'], function(){

})
// Copies src to temp folder
gulp.task('copy_src', function(){
  return gulp.src(paths.src + '**/*.*')
  .pipe(gulp.dest(paths.temp))
})

gulp.task('stage_cfg', function(){
  return gulp.src(paths.config + buildConfig.jekyllStageConfig)
  .pipe(rename('_config.yml'))
  .pipe(gulp.dest(paths.temp))
})

gulp.task('css_stage', ['copy_src'], function() {
  return gulp.src(paths.temp+'site/styles/*.scss')
  .pipe(compass({
    //config_file: './compass-config.rb',
    css: paths.temp +'site/styles',
    sass: paths.temp +'site/styles'
  }))
  .pipe(gulp.dest(paths.stage + 'site/styles/'));
});

gulp.task('jekyll_stage',['copy_src','stage_cfg'], function () {
         console.log('Jekyll ......... ');
     return require('child_process')
        .spawn('jekyll', ['build'], {stdio: 'inherit', cwd: paths.temp});

});

gulp.task('copy_stage',['copy_src','stage_cfg','css_stage','jekyll_stage'], function(){
  gulp.src([paths.stage+'/**/*.html', // all HTML
    paths.stage+'**/*.js',
    paths.stage+'**/*.css',
    paths.stage+'**/*.jpg',
    paths.stage+'**/*.png',
    paths.stage+'**/*.svg',
    paths.stage+'**/*.txt'  // robots.txt
    ])
  .pipe(gulp.dest(paths.dist+'stage'));
});

// Cleans gulp's folders
gulp.task('clean',function(){
  return gulp.src([paths.temp,paths.prod,paths.stage,paths.dist],{ read: false }) // much faster
    .pipe(rimraf());
})

// --------------------------- Building GitHub pages (localhost:8080) -----------------------------
//----------------
//----------
gulp.task('gh',['copy_src','gh_cfg','css_gh','jekyll_gh','copy_gh'], function(){

})
// Copies src to temp folder
gulp.task('copy_src', function(){
  return gulp.src(paths.src + '**/*.*')
  .pipe(gulp.dest(paths.temp))
})

gulp.task('gh_cfg', function(){
  return gulp.src(paths.config + buildConfig.jekyllGHConfig)
  .pipe(rename('_config.yml'))
  .pipe(gulp.dest(paths.temp))
})

gulp.task('css_gh', ['copy_src'], function() {
  return gulp.src(paths.temp+'site/styles/*.scss')
  .pipe(compass({
    //config_file: './compass-config.rb',
    css: paths.temp +'site/styles',
    sass: paths.temp +'site/styles'
  }))
  .pipe(gulp.dest(paths.gh + 'site/styles/'));
});

gulp.task('jekyll_gh',['copy_src','gh_cfg'], function () {
         console.log('Jekyll ......... ');
     return require('child_process')
        .spawn('jekyll', ['build'], {stdio: 'inherit', cwd: paths.temp});

});

gulp.task('copy_gh',['copy_src','gh_cfg','css_gh','jekyll_gh'], function(){
  gulp.src([paths.gh+'/**/*.html', // all HTML
    paths.gh+'**/*.js',
    paths.gh+'**/*.css',
    paths.gh+'**/*.jpg',
    paths.gh+'**/*.png',
    paths.gh+'**/*.svg',
    paths.gh+'**/*.txt'  // robots.txt
    ])
  .pipe(gulp.dest(paths.dist+'gh'));
});

gulp.task('watch', function(){
return 
gulp.watch('**/*.*',['gh']);
//.pipe(gulp.dest(paths.temp+'watch'));

});