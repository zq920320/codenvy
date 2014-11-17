#!/bin/bash
# docker pull vkuznyetsov/odyssey
docker run -i -t -e USRID=$(id -u) -e USRGR=$(id -g) -v $(pwd):/home/user/app -v ${HOME}/.m2:/home/user/.m2 odyssey sh -c 'sudo chown user -R /home/user/app && sudo chown user -R /home/user/.m2 && mvn clean install && sudo chown $USRID:$USRGR -R /home/user/app && sudo chown $USRID:$USRGR -R /home/user/.m2'


