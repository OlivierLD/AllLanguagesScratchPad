# May require:
# $ pip3 install --proxy http://...:80 requests
# $ pip3 install json
#
# Good requests 101-tutorial at https://realpython.com/api-integration-in-python/
#
# This is a REST Client.
#
from typing import Any
import sys
import requests
import json
import traceback
from typing import List

AUDIO_FILE: str = "foo.wav"

TENANT_ID: str = "odaserviceinstance00"
MACHINE: str = "100.111.136.104"
OPTION: str = "generic"

MACHINE_ARG_PREFIX: str = '--machine:'
AUDIO_FILE_ARG_PREFIX: str = "--audio-file:"


def do_request(uri: str) -> Any:
    # print("Using {}".format(uri))
    # headers = {'Content-Type' : 'audio/wav', 'Bots-TenantId': TENANT_ID, 'channelId': 'recognize'}
    headers = {'Content-Type': 'audio/wav'}
    with open(AUDIO_FILE, 'rb') as f:
        audio_content = f.read()
    resp = requests.post(uri, data=audio_content, headers=headers)
    if resp.status_code != 200 and resp.status_code != 201:
        raise Exception('POST {} {}'.format(uri, resp.status_code))
    else:
        json_obj = json.loads(resp.content)
        # print('Status {}\nReceived {}'.format(resp.status_code, json.dumps(json_obj, indent=2)))
        return json_obj


# Main part
def main(args: List[str]) -> None:
    if len(args) > 1:
        print(f"{len(args)} arguments:")
        for arg in args:
            print(f"\t{arg}")
    global MACHINE, AUDIO_FILE, OPTION
    for arg in sys.argv:
        if arg[:len(MACHINE_ARG_PREFIX)] == MACHINE_ARG_PREFIX:
            print(f"Managing parameter {MACHINE_ARG_PREFIX}")
            MACHINE = arg[len(MACHINE_ARG_PREFIX):]
        elif arg[:len(AUDIO_FILE_ARG_PREFIX)] == AUDIO_FILE_ARG_PREFIX:
            print(f"Managing parameter {AUDIO_FILE_ARG_PREFIX}")
            AUDIO_FILE = arg[len(AUDIO_FILE_ARG_PREFIX):]

    rest_url: str = f"http://{MACHINE}/voice/recognize/en-us/{OPTION}"
    try:
        response: Any = do_request(rest_url)
        transcription: str
        try:
            transcription = response['nbest'][0]['utterance']
        except Exception:
            transcription = response
        print("Service Response: {}".format(json.dumps(transcription, indent=2)))
    except KeyboardInterrupt:
        print("\n\t\tUser interrupted, exiting.")
    except Exception:
        traceback.print_exc(file=sys.stdout)

    print("Bye!")


if __name__ == "__main__":
    main(sys.argv)
