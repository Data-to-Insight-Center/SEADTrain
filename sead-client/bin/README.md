SEAD-Client - Publish Stream ROs Automatically
------------

1. Change the parameters in the stream_ro_publish.sh file

2. Schedule the script to send POST requests

daily:
~~~
sh bin/sead-client-schedular.sh -sr bin/stream_ro_publish.sh -d -hd <hour of day>
ex: sh bin/sead-client-schedular.sh -sr bin/stream_ro_publish.sh -d -hd 1
~~~
The example shedules to send POST request daily at hour 1

weekly:
~~~
sh bin/sead-client-schedular.sh -sr bin/stream_ro_publish.sh -w -dw <day of week> -hdw <hour of day>
ex: sh bin/sead-client-schedular.sh -sr bin/stream_ro_publish.sh -w -dw 6 -hdw 2
~~~
The example shedules to send POST request weekly on 6th day at hour 2
