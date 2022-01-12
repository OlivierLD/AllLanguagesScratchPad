#
# See https://pypi.org/project/websocket-client/
# pip3 install websocket-client
#
import websocket
import _thread
import time
import json

WEB_SOCKET_URI:str = "ws://100.111.136.104/voice/stream/recognize/en-us/generic"
AUDIO_FILE:str = "foo.wav"
response_received:bool = False


def on_message(ws, message) -> None:
    # print(f">> Message: {message}")
    global response_received
    response_received = True
    json_obj = json.loads(message)
    try:
        transcription = json_obj['nbest'][0]['utterance']
    except:
        transcription = json_obj
    print(f"Service Response: {json.dumps(transcription, indent=2)}")


def on_error(ws, error) -> None:
    print(f"Error: {error}")


def on_close(ws, close_status_code, close_msg) -> None:
    print("### Connection Closed ###")


def on_open(ws) -> None:
    print("### Connection Opened ###")

    def run(*args):
        with open(AUDIO_FILE, 'rb') as f:
            audio_content = f.read()
        ws.send(audio_content, websocket.ABNF.OPCODE_BINARY)
        while not response_received:
            time.sleep(0.25)
        print("Closing WS Connection...")
        print("Thread terminating.")
        ws.close()
    _thread.start_new_thread(run, ())


if __name__ == "__main__":
    websocket.enableTrace(False)   # True)
    ws = websocket.WebSocketApp(WEB_SOCKET_URI,
                                on_open=on_open,
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close,
                                header={'Content-Type: audio/wav'})
    ws.run_forever()
    print("Bye")
