#
# See https://pypi.org/project/websocket-client/
# pip3 install websocket
# pip3 install websocket-client
#
# This is just a Web`socket example.
#
import websocket
import _thread
import time
import sys
from typing import List


def on_message(ws, message):
    print(f">> Message: {message}")


def on_error(ws, error):
    print(error)


def on_close(ws, close_status_code, close_msg):
    print(f"### closed {close_status_code} {close_msg} ###")


def on_open(ws):
    def run(*args):
        for i in range(3):
            time.sleep(1)
            ws.send("Hello %d" % i)
        time.sleep(1)
        ws.close()
        print("thread terminating...")

    _thread.start_new_thread(run, ())


# Main part
def main(args: List[str]) -> None:
    websocket.enableTrace(True)
    ws = websocket.WebSocketApp("ws://echo.websocket.org/",
                                on_open=on_open,
                                on_message=on_message,
                                on_error=on_error,
                                on_close=on_close)

    ws.run_forever()


if __name__ == "__main__":
    main(sys.argv)
