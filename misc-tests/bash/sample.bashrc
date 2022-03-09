# .bashrc

# Source global definitions
if [ -f /etc/bashrc ]; then
    . /etc/bashrc
fi

# Uncomment the following line if you don't like systemctl's auto-paging feature:
# export SYSTEMD_PAGER=

# User specific aliases and functions
export HTTP_PROXY=http://www-proxy-hqdc.us.oracle.com:80
export HTTPS_PROXY=$HTTP_PROXY
export NO_PROXY="localhost,.data.digitalassistant.oci.oc-test.com,.oraclecorp.com,.oracle.com,phx.ocir.io,10.244.*,100.111.136.85,100.111.136.*,`echo 100.102.86.{1..254},` 100.102.86.255"

export http_proxy=$HTTP_PROXY
export https_proxy=$HTTPS_PROXY
export no_proxy=$NO_PROXY


export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"  # This loads nvm
[ -s "$NVM_DIR/bash_completion" ] && \. "$NVM_DIR/bash_completion"  # This loads nvm bash_completion

alias s1='screen -D -R s1'
alias s2='screen -D -R s2'
alias s3='screen -D -R s3'
alias s4='screen -D -R s4'
alias s5='screen -D -R s5'


alias lt='ls -ltr --time-style=full-iso'

alias k='kubectl'
alias ex-d0-0='export KUBECONFIG="/home/opc/git/bots/oke-runner/paasdevmob/us-phoenix-1/d0-cluster-0/kube.config"; CLUSTER_NAME=d0-0'
alias ex-s0-0='export KUBECONFIG="/home/opc/git/bots/oke-runner/odastage/us-phoenix-1/s0-cluster-0/kube.config"; CLUSTER_NAME=s0-0'
alias ex-s0-1='export KUBECONFIG="/home/opc/git/bots/oke-runner/odastage/us-phoenix-1/s0-cluster-1/kube.config"; CLUSTER_NAME=s0-1'
alias ex-s0-2='export KUBECONFIG="/home/opc/git/bots/oke-runner/odastage/us-phoenix-1/s0-cluster-2/kube.config"; CLUSTER_NAME=s0-2'
alias ex-t0-1='export KUBECONFIG="/home/opc/git/bots/oke-runner/odaalpha/us-phoenix-1/t0-cluster-1/kube.config"; CLUSTER_NAME=t0-1'
alias cdb='cd /home/opc/git/bots'
alias cdbs='cd /home/opc/git/bots-speech'

alias dev7='scl enable devtoolset-7 bash'

alias glo='git log'
alias gdi='git diff'
alias gst='git status'
alias gc='git checkout'
alias gcm='git checkout master'


# history option
export HISTTIMEFORMAT="%h %d %H:%M:%S "
export HISTSIZE=10000
export HISTFILESIZE=10000

shopt -s histappend
export PROMPT_COMMAND='history -a; history -n'

alias dtail="docker logs -f oda-speech-server | grep '^{' | jq -r '. | \"\(.ts) - \(.msg)\"'"

function dlog {
    local minutes="${1:-30}";
    docker logs --since=${minutes}m oda-speech-server | grep '^{' | jq -r '. | "\(.ts),\(.msg)"' | stdbuf -o0 awk -F"," '{OFS=" - "; $1=strftime("%Y-%m-%d %H:%M:%S", $1/1000)"."substr($1,11,3); print $0}';
}

function klog {
  local instance="${1:-1}"
  local minutes="${2:-30}";
  local podname=`k get pod -o wide | grep speech-server-en-us | cut -f1 -d ' ' | sed -n "${instance}p"`

  echo "Log since $minutes minutes on ${podname}"
  #k logs --since=${minutes}m ${podname};
  k logs --since=${minutes}m ${podname} | grep '^{' | jq -r '. | "\(.ts),\(.msg)"' | awk -F"," '{OFS=" - "; $1=strftime("%Y-%m-%d %H:%M:%S", $1/1000)"."substr($1,11,3); print $0}';
}

function kexec {
  local instance="${1:-1}"
  local podname=`k get pod -o wide | grep speech-server-en-us | cut -f1 -d ' ' | sed -n "${instance}p"`

  echo "Exec on ${podname}"
  k exec -it ${podname} -- bash;
}

function klogs1 { k logs $(k get pod -o wide | grep speech-server-en-us | cut -f1 -d ' ' | sed -n '1p'); }
function klogs2 { k logs $(k get pod -o wide | grep speech-server-en-us | cut -f1 -d ' ' | sed -n '2p'); }

################ OCI SPEECH ######################################

# . /home/opc/.oke_ocispeech

########## END #############@@

export IGNOREEOF=1


function hg() { history | grep $1; }
function tz() { tar cvfz ${1//\//}.tar.gz $1; }
function fw() { find -type f ! -path "./asrsupervisor/models/*" ! -path "./Session/*" -print0 | xargs -0 grep "$1"; }
function pjson { python -m json.tool "$1"; }
function pjson { cat "$1" | python -c "import json, sys, collections; print(json.dumps(json.loads(sys.stdin.read(), object_pairs_hook=collections.OrderedDict), indent=4))"; }
function sshopc { ssh -i ~/.ssh/sshkey $1; }

export CLUSTER_NAME="none"
unset CLUSTER_NAMESPACE

parse_git_branch() {
     git branch 2> /dev/null | sed -e '/^[^*]/d' -e 's/* \(.*\)/ (\1)/'
}

timer_start() {
  timer=${timer:-$SECONDS}
}
timer_stop() {
  timer_show=$(($SECONDS - $timer))
  unset timer
}
trap 'timer_start' DEBUG

export PROMPT_COMMAND="$PROMPT_COMMAND; timer_stop"


export PS1="\u@\h [\${CLUSTER_NAME}] [\${timer_show}s] \[\033[32m\]\w\[\033[33m\]\$(parse_git_branch)\[\033[00m\] $ "

export PATH=/home/opc/bin:$PATH

export DOCKER_CLIENT_TIMEOUT=120
export COMPOSE_HTTP_TIMEOUT=120

[[ -e "/home/opc/lib/oracle-cli/lib/python3.6/site-packages/oci_cli/bin/oci_autocomplete.sh" ]] && source "/home/opc/lib/oracle-cli/lib/python3.6/site-packages/oci_cli/bin/oci_autocomplete.sh"

# export PATH=/home/opc/git/bots-speech/:$PATH
