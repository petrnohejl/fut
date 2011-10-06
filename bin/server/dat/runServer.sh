#!/bin/bash

#args="-database.0 file:./db_mailer -dbname.0 mailer -no_system_exit true -port 9090"
args="-tcp -tcpPort 9090"
libdir="../lib"

cp=$(find $libdir -name *.jar -exec printf {}: ';')

exec java -server -Xms24m -cp $cp "org.h2.tools.Server" $args
