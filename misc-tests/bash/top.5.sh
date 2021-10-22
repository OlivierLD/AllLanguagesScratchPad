#!/bin/bash
#
#cat > input.txt << EOF
#Lorem Ipsum is simply dummy text of the printing and typesetting industry.
#Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown
#printer took a galley of type and scrambled it to make a type specimen book. It has survived not only
#five centuries, but also the leap into electronic typesetting, remaining essentially unchanged.
#It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages,
#and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.
#EOF
#
cat > input.txt << EOF
Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium,
totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.
Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos
qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur,
adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem.
Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi c
onsequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur,
vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?
EOF
#
# Print the top X most used words.
#
RESULT=$(
  echo -en "[" && \
  echo -en $(cat input.txt | \
             tr "." "\n" | tr "," "\n" | tr "?" "\n" | tr " " "\n" | tr [:lower:] [:upper:] | \
             grep -v "^$" | sort | uniq -c | sort -r | head -n 7 | \
             awk '{ print "{ \"word\": \"" $2 "\", \"count\": " $1 " }"}' | paste -s -d, - ) && \
  echo -e "]"
)
#
JQ_PRESENT=$(which jq)
if [[ "${JQ_PRESENT}" == "" ]]
then
  echo ${RESULT}
else
  echo ${RESULT} | jq
fi
