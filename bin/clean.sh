THIS_HOME="/home/zhaoyang/dataGenerator"

list="\
${THIS_HOME}/logs/* \
${THIS_HOME}/nohup.out \
${THIS_HOME}/output/*"

#clean files
for f in $list; do
    if test -e "$f"; then
        echo "delete [" ${f} "]"
        rm "$f" -rf
    fi
done
echo "OK"
