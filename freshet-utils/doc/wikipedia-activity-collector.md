# Collecting Wikipedia Activities for Building Test Data Set

This was implemented based on Samza examples project. Listen to Wikipedia IRC channel for activity messages
and append them to CSV file after parsing the activity.

## CSV format

channel,source,time,title,user,diff-bytes,diff-url,summary,is-minor,is-talk,is-bot-edit,is-new,is-unpatrolled,is-special,unparsed-flags
