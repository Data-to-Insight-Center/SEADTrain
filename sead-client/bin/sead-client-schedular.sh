display_help() {
    echo
    echo "Usage: $0 -sr <script> --weekly|--daily [-day|-hour]" >&2
    echo
    echo "   -sr, --script  Script to schedule ex: bin/start-ingestor.sh"
    echo "   -w, --weekly   Run the TextIt Ingest script weekly"
    echo "   -dw, --day     Day of the week the script should run on[0-6]"
    echo "   -hdw, --hDayW  Hour of the Day of the week the script should run on[0-23]"
    echo "   -d, --daily    Run the TextIt Ingest script daily"
    echo "   -hd, --hour    Hour of day the script should run on [0-23]"
    echo
}

while :
do
    case "$1" in
      -sr | --script)   script="$2"
                        shift 2
                        ;;
      -h | --help)      display_help
                        exit 0
                        ;;
      -d | --daily)     daily="daily"
                        shift
                        ;;
      -hd | --hour)     hour="$2"
                        shift 2
                        ;;
      -w | --weekly)    weekly="weekly"
                        shift
                        ;;
      -dw | --day)      day="$2"
                        shift 2
                        ;;
      -hdw | --hDayW)   hDayW="$2"
                        shift 2
                        ;;
      -*)               echo "Error: Unknown option: $1" >&2
                        exit 1
                        ;;
      *)                break
                        ;;
    esac
done

if [ "$daily" != "" ] && [ "$weekly" != "" ]; then
    echo "Error: Cannot specify both -w and -d options"
    display_help
    exit 1
fi

if [ "$daily" == "" ] && [ "$weekly" == "" ]; then
    echo "Error: Please specify either -w or -d option"
    display_help
    exit 1
fi

if [ "$hour" != "" ]; then
    if [[ ! "$hour" =~ ^[0-9]+$ ]] || [ "$hour" -gt 23 ]; then
        echo "Error: 'hour' should be between 0-23"
        display_help
        exit 1
    fi
else
    hour="0"
fi

if [ "$day" != "" ]; then
    if [[ ! "$day" =~ ^[0-6]+$ ]] || [ "$day" -gt 6 ]; then
        echo "Error: 'day' should be between 0-6"
        display_help
        exit 1
    fi
else
    day="0"
fi

if [ "$hDayW" != "" ]; then
    if [[ ! "$hDayW" =~ ^[0-9]+$ ]] || [ "$hDayW" -gt 23 ]; then
        echo "Error: 'hDayW' should be between 0-23"
        display_help
        exit 1
    fi
else
    hDayW="0"
fi

this=$0
bin=`dirname "$this"`
bin=`cd "$bin"; pwd`
HOME=`cd "$bin"; cd ..; pwd`

start_script=$HOME"/"$script
log_file=$HOME"/cron_log.txt"

echo "Running "$0" script"
if [ "$daily" != "" ]; then
    echo "The script is scheduled to run daily, at hour "$hour
fi
if [ "$weekly" != "" ]; then

    echo "The script is scheduled to run weekly, on day "$day" at hour "$hDayW
fi
echo "Log file location is "$log_file""

chmod +x $start_script

#write out current crontab
crontab -l > textitcron

#echo new cron into cron file
if [ "$daily" != "" ]; then
    echo "0 "$hour" * * * "$start_script" >> "$log_file" 2>&1" >> textitcron
else
    echo "0 "$hDayW" * * "$day" "$start_script" >> "$log_file" 2>&1" >> textitcron
fi

#echo "*/1 * * * * "$start_script" >> "$log_file" 2>&1" >> textitcron

#install new cron file
crontab textitcron

rm textitcron
