
# Hardcoded for windows right now.
# Need to make this configurable for our CI systems and to work on windows or linux.
# 
# Optionally - you can run 'jekyll/jekyll jekyll serve' to get a local server on port 9080
# NOTE - these files will not work without a hosted server right now - they are not static stand alone 
MSYS_NO_PATHCONV=1 docker run --rm -it -p 9080:4000 --name jekyll \
       -v /C/codenvy/codenvy/docs:/srv/jekyll \
       -v /C/codenvy/codenvy/docs/_site:/srv/jekyll/_site \
           jekyll/jekyll jekyll serve