#
# See https://pypi.org/project/websocket-client/
# pip3 install websocket-client
#
from typing import Any
from typing import List
import sys
import websocket
import _thread
import time
import json


INSTANCE_IP: str = "100.111.136.104"
WEB_SOCKET_URI: str = f"ws://{INSTANCE_IP}/voice/stream/recognize/en-us/generic"
AUDIO_FILE: str = "foo.wav"
response_received: bool = False


def on_message(ws: websocket._app.WebSocketApp, message: str) -> None:
    # print(f">> Message type: {type(message)}")
    global response_received
    response_received = True
    json_obj = json.loads(message)
    try:
        transcription = json_obj['nbest'][0]['utterance']
    except Exception:
        transcription = json_obj

    print(f"Service Response: {json.dumps(transcription, indent=2)}")


def on_error(ws: websocket._app.WebSocketApp, error: Any) -> None:
    print(f"Error: {error} (type: {type(error)})")


def on_close(ws: websocket._app.WebSocketApp, close_status_code: int, close_msg: str) -> None:
    print("### Connection Closed ###")


def on_open(ws: websocket._app.WebSocketApp) -> None:
    print("### Connection Opened ###")

    def run(*args: str):
        with open(AUDIO_FILE, 'rb') as f:
            audio_content = f.read()
        ws.send(audio_content, websocket.ABNF.OPCODE_BINARY)
        while not response_received:
            time.sleep(0.25)
        print("Closing WS Connection...")
        print("Thread terminating.")
        ws.close()
    _thread.start_new_thread(run, ())


def main(args: List[str]) -> None:
    print(f"Sending {AUDIO_FILE} for processing")
    websocket.enableTrace(False)   # True)
    ws = websocket.WebSocketApp(WEB_SOCKET_URI,
                                on_open=on_open,
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close,
                                header={'Content-Type: audio/wav'})
    # print(f"Type of ws: {type(ws)}")
    ws.run_forever()
    print("Bye")


if __name__ == "__main__":
    main(sys.argv)
