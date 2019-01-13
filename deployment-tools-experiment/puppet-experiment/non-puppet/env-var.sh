sudo docker run --rm --name test -v `pwd`:/opt/test --env message='hello there' ubuntu /bin/sh -c "\
printenv message &&\
echo 'done'\
"
