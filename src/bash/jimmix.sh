#! /bin/sh

DIRNAME=`dirname $0`
PROGNAME=`basename $0`

# Setup HTTP_INVOKER_HOME
if [ "x$HTTP_INVOKER_HOME" = "x" ]; then
    HTTP_INVOKER_HOME=`cd $DIRNAME/..; pwd`
fi
export HTTP_INVOKER_HOME

# Setup the JVM
if [ "x$JAVA_HOME" != "x" ]; then
    JAVA=$JAVA_HOME/bin/java 
else
    JAVA="java"
fi  

HTTP_INVOKER_BOOT_CLASSPATH="$HTTP_INVOKER_HOME/bin/jimmix.jar"

# Setup the classpath
if [ "x$HTTP_INVOKER_CLASSPATH" = "x" ]; then
    HTTP_INVOKER_CLASSPATH="$HTTP_INVOKER_BOOT_CLASSPATH"
    for jar in `ls $HTTP_INVOKER_HOME/lib/`; do
      HTTP_INVOKER_CLASSPATH="$HTTP_INVOKER_CLASSPATH:$HTTP_INVOKER_HOME/lib/$jar"
    done;
else
    HTTP_INVOKER_CLASSPATH="$HTTP_INVOKER_CLASSPATH:$HTTP_INVOKER_BOOT_CLASSPATH"
fi  

exec "$JAVA" \
    $JAVA_OPTS \
    -Dprogram.name="$PROGNAME" \
    -classpath $HTTP_INVOKER_CLASSPATH \
    jimmix.Run "$@"

